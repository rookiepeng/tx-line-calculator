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

package com.rookiedev.microwavetools.data;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.android.billingclient.api.Purchase;
//import com.example.android.classytaxijava.Constants;
//import com.example.android.classytaxijava.billing.BillingClientLifecycle;
//import com.example.android.classytaxijava.data.disk.LocalDataSource;
//import com.example.android.classytaxijava.data.network.WebDataSource;
import com.rookiedev.microwavetools.Constants;
import com.rookiedev.microwavetools.billing.BillingClientLifecycle;
import com.rookiedev.microwavetools.data.disk.LocalDataSource;
import com.rookiedev.microwavetools.data.network.WebDataSource;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.addAll;

public class DataRepository {
    private static volatile DataRepository INSTANCE = null;

    private final LocalDataSource localDataSource;
    private final WebDataSource webDataSource;
    private final BillingClientLifecycle billingClientLifecycle;

    /**
     * {@link MediatorLiveData} to coordinate updates from the database and the network.
     * <p>
     * The mediator observes multiple sources. The database source is immediately exposed.
     * The network source is stored in the database, which will eventually be exposed.
     * The mediator provides an easy way for us to use LiveData for both the local data source
     * and the network data source, without implementing a new callback interface.
     */
    private MediatorLiveData<List<SubscriptionStatus>> subscriptions =
            new MediatorLiveData<>();

    /**
     * Live data with basic content
     */
    private MediatorLiveData<ContentResource> basicContent = new MediatorLiveData<>();

    /**
     * Live data with premium content
     */
    private MediatorLiveData<ContentResource> premiumContent = new MediatorLiveData<>();

    private DataRepository(final LocalDataSource localDataSource,
                           WebDataSource webDataSource,
                           BillingClientLifecycle billingClientLifecycle) {
        this.localDataSource = localDataSource;
        this.webDataSource = webDataSource;
        this.billingClientLifecycle = billingClientLifecycle;

        // Update content from the web.
        // We are using a MediatorLiveData so that we can clear the data immediately
        // when the subscription changes.
        basicContent.addSource(webDataSource.getBasicContent(), new Observer<ContentResource>() {
            @Override
            public void onChanged(ContentResource contentResource) {
                basicContent.postValue(contentResource);
            }
        });
        premiumContent.addSource(webDataSource.getPremiumContent(),
                new Observer<ContentResource>() {
                    @Override
                    public void onChanged(ContentResource contentResource) {
                        premiumContent.postValue(contentResource);
                    }
                });

        // Database changes are observed by the ViewModel.
        subscriptions.addSource(localDataSource.subscriptions,
                new Observer<List<SubscriptionStatus>>() {
                    @Override
                    public void onChanged(List<SubscriptionStatus> subscriptionStatuses) {
                        int numOfSubscriptions = subscriptionStatuses == null ?
                                0 : subscriptionStatuses.size();
                        Log.d("Repository", "Subscriptions updated: "
                                + numOfSubscriptions);
                        subscriptions.postValue(subscriptionStatuses);
                    }
                });

        // Observed network changes are store in the database.
        // The database changes will propagate to the ViewModel.
        // We could write different logic to ensure that the network call completes when
        // the UI component is inactive.
        subscriptions.addSource(webDataSource.getSubscriptions(),
                new Observer<List<SubscriptionStatus>>() {
                    @Override
                    public void onChanged(List<SubscriptionStatus> subscriptionStatuses) {
                        updateSubscriptionsFromNetwork(subscriptionStatuses);
                    }
                });

        // When the list of purchases changes, we need to update the subscription status
        // to indicate whether the subscription is local or not. It is local if the
        // the Google Play Billing APIs return a Purchase record for the SKU. It is not
        // local if there is no record of the subscription on the device.
        subscriptions.addSource(billingClientLifecycle.purchases, new Observer<List<Purchase>>() {
            @Override
            public void onChanged(List<Purchase> purchases) {
                List<SubscriptionStatus> subscriptionStatuses = subscriptions.getValue();
                if (subscriptionStatuses != null) {
                    boolean hasChanged = updateLocalPurchaseTokens(subscriptionStatuses, purchases);
                    if (hasChanged) {
                        localDataSource.updateSubscriptions(subscriptionStatuses);
                    }
                }
            }
        });
    }

    public static DataRepository getInstance(LocalDataSource localDataSource,
                                             WebDataSource webDataSource,
                                             BillingClientLifecycle billingClientLifecycle) {
        if (INSTANCE == null) {
            synchronized (DataRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DataRepository(localDataSource, webDataSource,
                            billingClientLifecycle);
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<Boolean> getLoading() {
        return webDataSource.getLoading();
    }

    public MediatorLiveData<List<SubscriptionStatus>> getSubscriptions() {
        return subscriptions;
    }

    public MediatorLiveData<ContentResource> getBasicContent() {
        return basicContent;
    }

    public MediatorLiveData<ContentResource> getPremiumContent() {
        return premiumContent;
    }

    public void updateSubscriptionsFromNetwork(
            @Nullable List<SubscriptionStatus> remoteSubscriptions) {
        List<SubscriptionStatus> oldSubscriptions = subscriptions.getValue();
        List<Purchase> purchases = billingClientLifecycle.purchases.getValue();
        List<SubscriptionStatus> subscriptions =
                mergeSubscriptionsAndPurchases(oldSubscriptions, remoteSubscriptions, purchases);
        if (remoteSubscriptions != null) {
            acknowledgeRegisteredPurchaseTokens(remoteSubscriptions);
        }
        // Store the subscription information when it changes.
        localDataSource.updateSubscriptions(subscriptions);

        // Update the content when the subscription changes.
        if (remoteSubscriptions != null) {
            // Figure out which content we need to fetch.
            boolean updateBasic = false;
            boolean updatePremium = false;
            for (SubscriptionStatus subscription : remoteSubscriptions) {
                if (Constants.BASIC_SKU.equals(subscription.sku)) {
                    updateBasic = true;
                } else {
                    // Premium subscribers get access to basic content as well.
                    updateBasic = true;
                    updatePremium = true;
                }
            }

            if (updateBasic) {
                // Fetch the basic content.
                webDataSource.updateBasicContent();
            } else {
                // If we no longer own this content, clear it from the UI.
                basicContent.postValue(null);
            }
            if (updatePremium) {
                // Fetch the premium content.
                webDataSource.updatePremiumContent();
            } else {
                // If we no longer own this content, clear it from the UI.
                premiumContent.postValue(null);
            }
        }
    }

    /**
     * Acknowledge subscriptions that have been registered by the server.
     */
    private void acknowledgeRegisteredPurchaseTokens(List<SubscriptionStatus> remoteSubscriptions) {
        for (SubscriptionStatus remoteSubscription : remoteSubscriptions) {
            String purchaseTkn = remoteSubscription.purchaseToken;
            billingClientLifecycle.acknowledgePurchase(purchaseTkn);
        }
    }


    /**
     * TODO(122273956) - Simplify / improve merge algorithm
     * Merge the previous subscriptions and new subscriptions by looking at on-device purchases.
     * <p>
     * We want to return the list of new subscriptions, possibly with some modifications
     * based on old subscriptions and the on-devices purchases from Google Play Billing.
     * Old subscriptions should be retained if they are owned by someone else (subAlreadyOwned)
     * and the purchase token for the subscription is still on this device.
     */
    private List<SubscriptionStatus> mergeSubscriptionsAndPurchases(
            @Nullable List<SubscriptionStatus> oldSubscriptions,
            @Nullable List<SubscriptionStatus> newSubscriptions,
            @Nullable List<Purchase> purchases) {
        List<SubscriptionStatus> subscriptionStatuses = new ArrayList<>();
        if (purchases != null) {
            // Record which purchases are local and can be managed on this device.
            updateLocalPurchaseTokens(newSubscriptions, purchases);
        }
        if (newSubscriptions != null) {
            subscriptionStatuses.addAll(newSubscriptions);
        }
        // Find old subscriptions that are in purchases but not in new subscriptions.
        if (purchases != null && oldSubscriptions != null) {
            for (SubscriptionStatus oldSubscription : oldSubscriptions) {
                if (oldSubscription.subAlreadyOwned && oldSubscription.isLocalPurchase) {
                    // This old subscription was previously marked as "already owned" by
                    // another user. It should be included in the output if the SKU
                    // and purchase token match their previous value.
                    for (Purchase purchase : purchases) {
                        if (purchase.getSku().equals(oldSubscription.sku) &&
                                purchase.getPurchaseToken().equals(oldSubscription.purchaseToken)) {
                            // The old subscription that was already owned subscription should
                            // be added to the new subscriptions.
                            // Look through the new subscriptions to see if it is there.
                            boolean foundNewSubscription = false;
                            if (newSubscriptions != null) {
                                for (SubscriptionStatus newSubscription : newSubscriptions) {
                                    if (TextUtils.equals(newSubscription.sku,
                                            oldSubscription.sku)) {
                                        foundNewSubscription = true;
                                    }
                                }
                            }
                            if (!foundNewSubscription) {
                                // The old subscription should be added to the output.
                                // It matches a local purchase.
                                subscriptionStatuses.add(oldSubscription);
                            }
                        }
                    }
                }
            }
        }
        return subscriptionStatuses;
    }

    /**
     * Modify the subscriptions isLocalPurchase field based on the list of local purchases.
     * Return true if any of the values changed.
     */
    private boolean updateLocalPurchaseTokens(
            @Nullable List<SubscriptionStatus> subscriptions,
            @Nullable List<Purchase> purchases) {
        boolean hasChanged = false;
        if (subscriptions != null) {
            for (SubscriptionStatus subscription : subscriptions) {
                boolean isLocalPurchase = false;
                String purchaseToken = subscription.purchaseToken;
                if (purchases != null) {
                    for (Purchase purchase : purchases) {
                        if (TextUtils.equals(subscription.sku, purchase.getSku())) {
                            isLocalPurchase = true;
                            purchaseToken = purchase.getPurchaseToken();
                        }
                    }
                }
                if (subscription.isLocalPurchase != isLocalPurchase) {
                    subscription.isLocalPurchase = isLocalPurchase;
                    subscription.purchaseToken = purchaseToken;
                    hasChanged = true;
                }
            }
        }
        return hasChanged;
    }

    /**
     * Fetch subscriptions from the server and update local data source.
     */
    public void fetchSubscriptions() {
        webDataSource.updateSubscriptionStatus();
    }

    /**
     * Register subscription to this account and update local data source.
     */
    public void registerSubscription(String sku, String purchaseToken) {
        webDataSource.registerSubscription(sku, purchaseToken);
    }

    /**
     * Transfer subscription to this account and update local data source.
     */
    public void transferSubscription(String sku, String purchaseToken) {
        webDataSource.postTransferSubscriptionSync(sku, purchaseToken);
    }

    /**
     * Register Instance ID.
     */
    public void registerInstanceId(String instanceId) {
        webDataSource.postRegisterInstanceId(instanceId);
    }

    /**
     * Unregister Instance ID.
     */
    public void unregisterInstanceId(String instanceId) {
        webDataSource.postUnregisterInstanceId(instanceId);
    }

    /**
     * Delete local user data when the user signs out.
     */
    public void deleteLocalUserData() {
        localDataSource.deleteLocalUserData();
        basicContent.postValue(null);
        premiumContent.postValue(null);
    }
}
