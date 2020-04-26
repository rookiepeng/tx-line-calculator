/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rookiedev.microwavetools.data.network.firebase;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

//import com.example.android.classytaxijava.data.ContentResource;
//import com.example.android.classytaxijava.data.SubscriptionStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.rookiedev.microwavetools.data.ContentResource;
import com.rookiedev.microwavetools.data.SubscriptionStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of [ServerFunctions] using Firebase Callable Functions.
 * https://firebase.google.com/docs/functions/callable
 */
public class ServerFunctionsImpl implements ServerFunctions {
    private static final String TAG = "ServerImpl";

    private static final String SKU_KEY = "sku";
    private static final String PURCHASE_TOKEN_KEY = "token";
    private static final String BASIC_CONTENT_CALLABLE = "content_basic";
    private static final String PREMIUM_CONTENT_CALLABLE = "content_premium";
    private static final String SUBSCRIPTION_STATUS_CALLABLE = "subscription_status";
    private static final String REGISTER_SUBSCRIPTION_CALLABLE = "subscription_register";
    private static final String TRANSFER_SUBSCRIPTION_CALLABLE = "subscription_transfer";
    private static final String REGISTER_INSTANCE_ID_CALLABLE = "instanceId_register";
    private static final String UNREGISTER_INSTANCE_ID_CALLABLE = "instanceId_unregister";

    private MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private MutableLiveData<List<SubscriptionStatus>> subscriptions = new MutableLiveData<>();
    private MutableLiveData<ContentResource> basicContent = new MutableLiveData<>();
    private MutableLiveData<ContentResource> premiumContent = new MutableLiveData<>();

    private static volatile ServerFunctions INSTANCE = null;

    /**
     * Singleton instance of the Firebase Functions API.
     */
    private FirebaseFunctions firebaseFunctions = FirebaseFunctions.getInstance();

    /**
     * Track the number of pending server requests.
     */
    private AtomicInteger pendingRequestCount = new AtomicInteger();

    /**
     * Live data is true when there are pending network requests.
     */
    @Override
    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    private ServerFunctionsImpl() {}

    public static ServerFunctions getInstance() {
        if (INSTANCE == null) {
            synchronized (ServerFunctionsImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServerFunctionsImpl();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Increment request count and update loading value.
     * Must plan on calling {@link #decrementRequestCount} when the request completes.
     */
    private void incrementRequestCount() {
        int newPendingRequestCount = pendingRequestCount.incrementAndGet();
        Log.d(TAG, "Pending Server Requests: " + newPendingRequestCount);
        if (newPendingRequestCount <= 0) {
            Log.wtf(TAG, "Unexpectedly low request count after new request: "
                    + newPendingRequestCount);
        } else {
            loading.postValue(true);
        }
    }

    /**
     * Decrement request count and update loading value.
     * Must call {@link #decrementRequestCount} once, and before, each time you call this method.
     */
    private void decrementRequestCount() {
        int newPendingRequestCount = pendingRequestCount.decrementAndGet();
        Log.d(TAG, "Pending Server Requests: " + newPendingRequestCount);
        if (newPendingRequestCount < 0) {
            Log.wtf(TAG, "Unexpectedly negative request count: "
                    + newPendingRequestCount);
        } else if (newPendingRequestCount == 0) {
            loading.postValue(false);
        }
    }

    /**
     * The latest subscription data from the Firebase server.
     * <p>
     * Use this class by observing the subscriptions LiveData.
     * Any server updates will be communicated through this LiveData.
     */
    @Override
    public MutableLiveData<List<SubscriptionStatus>> getSubscriptions() {
        return subscriptions;
    }

    /**
     * The basic content URL.
     */
    @Override
    public MutableLiveData<ContentResource> getBasicContent() {
        return basicContent;
    }

    /**
     * The premium content URL.
     */
    @Override
    public MutableLiveData<ContentResource> getPremiumContent() {
        return premiumContent;
    }

    /**
     * Expected errors.
     * <p>
     * NOT_FOUND: Invalid SKU or purchase token.
     * ALREADY_OWNED: Subscription is claimed by a different user.
     * INTERNAL: Server error.
     */
    enum ServerError {
        NOT_FOUND, ALREADY_OWNED, PERMISSION_DENIED, INTERNAL
    }

    /**
     * Fetch basic content and post results to {@link #basicContent}.
     * This will fail if the user does not have a basic subscription.
     */
    @Override
    public void updateBasicContent() {
        incrementRequestCount();
        Log.d(TAG, "Calling: " + BASIC_CONTENT_CALLABLE);
        firebaseFunctions
                .getHttpsCallable(BASIC_CONTENT_CALLABLE)
                .call(null)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        decrementRequestCount();
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Basic content update successful");
                            Map<String, Object> result = null;
                            try {
                                result = (Map<String, Object>) task.getResult().getData();
                            } catch (Exception e) {
                                Log.e(TAG, "Invalid basic subscription data");
                                return;
                            }
                            ContentResource content = ContentResource.listFromMap(result);
                            if (content == null) {
                                Log.e(TAG, "Invalid basic subscription data");
                                return;
                            }
                            basicContent.postValue(content);
                        } else {
                            ServerError error =
                                    serverErrorFromFirebaseException(task.getException());
                            if (error == ServerError.PERMISSION_DENIED) {
                                basicContent.postValue(null);
                                Log.e(TAG, "Basic subscription permission denied");
                            } else if (error == ServerError.INTERNAL) {
                                Log.e(TAG, "Basic subscription server error");
                            } else {
                                Log.e(TAG, "Unknown error during basic content update");
                            }
                        }
                    }
                });
    }

    /**
     * Fetch premium content and post results to {@link #premiumContent}.
     * This will fail if the user does not have a premium subscription.
     */
    @Override
    public void updatePremiumContent() {
        incrementRequestCount();
        Log.d(TAG, "Calling: " + PREMIUM_CONTENT_CALLABLE);
        firebaseFunctions
                .getHttpsCallable(PREMIUM_CONTENT_CALLABLE)
                .call(null)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        decrementRequestCount();
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Premium content update successful");
                            Map<String, Object> result = null;
                            try {
                                result = (Map<String, Object>) task.getResult().getData();
                            } catch (Exception e) {
                                Log.e(TAG, "Invalid premium subscription data");
                                return;
                            }
                            ContentResource content = ContentResource.listFromMap(result);
                            if (content == null) {
                                Log.e(TAG, "Invalid premium subscription data");
                                return;
                            }
                            premiumContent.postValue(content);
                        } else {
                            ServerError error =
                                    serverErrorFromFirebaseException(task.getException());
                            if (error == ServerError.PERMISSION_DENIED) {
                                premiumContent.postValue(null);
                                Log.e(TAG, "Basic subscription permission denied");
                            } else if (error == ServerError.INTERNAL) {
                                Log.e(TAG, "Premium server error");
                            } else {
                                Log.e(TAG, "Unknown error during premium content update");
                            }
                        }
                    }
                });
    }

    @Override
    public void updateSubscriptionStatus() {
        incrementRequestCount();
        Log.d(TAG, "Calling: " + SUBSCRIPTION_STATUS_CALLABLE);
        firebaseFunctions
                .getHttpsCallable(SUBSCRIPTION_STATUS_CALLABLE)
                .call(null)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        decrementRequestCount();
                        if (task.isSuccessful()) {
                            Map<String, Object> result = null;
                            try {
                                result = (Map<String, Object>) task.getResult().getData();
                            } catch (Exception e) {
                                Log.e(TAG, "Invalid subscription data");
                                return;
                            }
                            List<SubscriptionStatus> subs = SubscriptionStatus.listFromMap(result);
                            if (subs == null) {
                                Log.e(TAG, "Invalid subscription data");
                                return;
                            }
                            subscriptions.postValue(subs);
                        } else {
                            ServerError error =
                                    serverErrorFromFirebaseException(task.getException());
                            if (error == ServerError.INTERNAL) {
                                Log.e(TAG, "Subscription server error");
                            } else {
                                Log.e(TAG, "Unknown error during subscription status update");
                            }
                        }
                    }
                });
    }

    /**
     * Register a subscription with the server and posts successful results to
     * {@link #subscriptions}.
     */
    @Override
    public void registerSubscription(final String sku, final String purchaseToken) {
        incrementRequestCount();
        Log.d(TAG, "Calling: " + REGISTER_SUBSCRIPTION_CALLABLE);
        Map<String, String> data = new HashMap<>();
        data.put(SKU_KEY, sku);
        data.put(PURCHASE_TOKEN_KEY, purchaseToken);
        firebaseFunctions
                .getHttpsCallable(REGISTER_SUBSCRIPTION_CALLABLE)
                .call(data)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        decrementRequestCount();
                        if (task.isSuccessful()) {
                            Map<String, Object> result = null;
                            try {
                                result = (Map<String, Object>) task.getResult().getData();
                            } catch (Exception e) {
                                Log.e(TAG, "Invalid subscription registration data");
                                return;
                            }
                            List<SubscriptionStatus> subs = SubscriptionStatus.listFromMap(result);
                            if (subs == null) {
                                Log.e(TAG, "Invalid subscriptionregistration data");
                                return;
                            }
                            subscriptions.postValue(subs);
                        } else {
                            ServerError error =
                                    serverErrorFromFirebaseException(task.getException());
                            switch (error) {
                                case NOT_FOUND:
                                    Log.e(TAG,
                                            "Invalid SKU or purchase token during registration");
                                    break;
                                case ALREADY_OWNED:
                                    Log.i(TAG, "Subscription already owned by another user");
                                    List<SubscriptionStatus> oldSubscriptions =
                                            subscriptions.getValue();
                                    SubscriptionStatus newSubscription =
                                            SubscriptionStatus
                                                    .alreadyOwnedSubscription(sku, purchaseToken);
                                    List<SubscriptionStatus> newSubscriptions =
                                            insertOrUpdateSubscription(oldSubscriptions,
                                                    newSubscription);
                                    subscriptions.postValue(newSubscriptions);
                                case INTERNAL:
                                    Log.e(TAG, "Subscription registration server error");
                                    break;
                                default:
                                    Log.e(TAG,
                                            "Unknown error during subscription registration");
                            }
                        }

                    }
                });
    }

    /**
     * Insert or update the subscription to the list of existing subscriptions.
     * <p>
     * If none of the existing subscriptions have a SKU that matches, insert this SKU.
     * If a subscription exists with the matching SKU, the output list will contain the new
     * subscription instead of the old subscription.
     */
    private List<SubscriptionStatus> insertOrUpdateSubscription(
            List<SubscriptionStatus> oldSubscriptions,
            SubscriptionStatus newSubscription) {
        List<SubscriptionStatus> subscriptionStatuses = new ArrayList<>();
        if (oldSubscriptions == null || oldSubscriptions.isEmpty()) {
            subscriptionStatuses.add(newSubscription);
            return subscriptionStatuses;
        }

        boolean subscriptionAdded = false;
        for (SubscriptionStatus subscription : oldSubscriptions) {
            if (TextUtils.equals(subscription.sku, newSubscription.sku)) {
                subscriptionStatuses.add(newSubscription);
                subscriptionAdded = true;
            } else {
                subscriptionStatuses.add(subscription);
            }
        }

        if (!subscriptionAdded) {
            subscriptionStatuses.add(newSubscription);
        }

        return subscriptionStatuses;
    }

    /**
     * Transfer subscription to this account posts successful results to {@link #subscriptions}.
     */
    @Override
    public void transferSubscription(String sku, String purchaseToken) {

    }

    /**
     * Register Instance ID for Firebase Cloud Messaging.
     */
    @Override
    public void registerInstanceId(String instanceId) {
        incrementRequestCount();
        Log.d(TAG, "Calling: " + REGISTER_INSTANCE_ID_CALLABLE);
        firebaseFunctions
                .getHttpsCallable(REGISTER_INSTANCE_ID_CALLABLE)
                .call(null)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        decrementRequestCount();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Instance ID registration successful");
                        } else {
                            Log.e(TAG, "Unknown error during Instance ID registration");
                        }
                    }
                });
    }

    /**
     * Unregister Instance ID for Firebase Cloud Messaging.
     */
    @Override
    public void unregisterInstanceId(String instanceId) {
        incrementRequestCount();
        Log.d(TAG, "Calling: " + UNREGISTER_INSTANCE_ID_CALLABLE);
        firebaseFunctions
                .getHttpsCallable(UNREGISTER_INSTANCE_ID_CALLABLE)
                .call(null)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        decrementRequestCount();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Instance ID un-registration successful");
                        } else {
                            Log.e(TAG, "Unknown error during Instance ID un-registration");
                        }
                    }
                });
    }

    /**
     * Convert Firebase error codes to the app-specific meaning.
     */
    @Nullable
    private ServerError serverErrorFromFirebaseException(@Nullable Exception exception) {
        if (!(exception instanceof FirebaseFunctionsException)) {
            Log.d(TAG, "Unrecognized Exception: " + exception);
            return null;
        }
        FirebaseFunctionsException.Code code = ((FirebaseFunctionsException) exception).getCode();
        switch (code) {
            case NOT_FOUND:
                return ServerError.NOT_FOUND;
            case ALREADY_EXISTS:
                return ServerError.ALREADY_OWNED;
            case PERMISSION_DENIED:
                return ServerError.PERMISSION_DENIED;
            case INTERNAL:
                return ServerError.INTERNAL;
            case RESOURCE_EXHAUSTED:
                Log.e(TAG, "RESOURCE_EXHAUSTED: Check your server quota");
                return ServerError.INTERNAL;
            default:
                Log.d(TAG, "Unexpected Firebase Exception: " + code);
                return null;
        }
    }
}
