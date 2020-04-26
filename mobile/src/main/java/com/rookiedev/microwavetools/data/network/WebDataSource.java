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

package com.rookiedev.microwavetools.data.network;

import androidx.lifecycle.LiveData;

import com.rookiedev.microwavetools.AppExecutors;
import com.rookiedev.microwavetools.data.ContentResource;
import com.rookiedev.microwavetools.data.SubscriptionStatus;
import com.rookiedev.microwavetools.data.network.firebase.ServerFunctions;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Execute network requests on the network thread.
 * Fetch data from a {@link ServerFunctions} object and expose with {@link #getSubscriptions()}.
 */
public class WebDataSource {
    private static volatile WebDataSource INSTANCE = null;
    private Executor executor;
    private ServerFunctions serverFunctions;

    public static WebDataSource getInstance(AppExecutors executors,
                                            ServerFunctions callableFunctions) {
        if (INSTANCE == null) {
            synchronized (WebDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WebDataSource(executors.networkIO, callableFunctions);
                }
            }
        }
        return INSTANCE;
    }

    private WebDataSource(Executor executor, ServerFunctions serverFunctions) {
        this.executor = executor;
        this.serverFunctions = serverFunctions;
    }

    /**
     * Live data is true when there are pending network requests.
     */
    public LiveData<Boolean> getLoading() {
        return serverFunctions.getLoading();
    }

    public LiveData<List<SubscriptionStatus>> getSubscriptions() {
        return serverFunctions.getSubscriptions();
    }

    public LiveData<ContentResource> getBasicContent() {
        return serverFunctions.getBasicContent();
    }

    public LiveData<ContentResource> getPremiumContent() {
        return serverFunctions.getPremiumContent();
    }

    public void updateBasicContent() {
        serverFunctions.updateBasicContent();
    }

    public void updatePremiumContent() {
        serverFunctions.updatePremiumContent();
    }

    /**
     * GET request for subscription status.
     */
    public void updateSubscriptionStatus() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (WebDataSource.class) {
                    serverFunctions.updateSubscriptionStatus();
                }
            }
        });
    }

    /**
     * POST request to register subscription.
     */
    public void registerSubscription(final String sku, final String purchaseToken) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (WebDataSource.class) {
                    serverFunctions.registerSubscription(sku, purchaseToken);
                }
            }
        });
    }

    /**
     * POST request to transfer a subscription that is owned by someone else.
     */
    public void postTransferSubscriptionSync(final String sku, final String purchaseToken) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (WebDataSource.class) {
                    serverFunctions.transferSubscription(sku, purchaseToken);
                }
            }
        });
    }

    /**
     * POST request to register an Instance ID.
     */
    public void postRegisterInstanceId(final String instanceId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (WebDataSource.class) {
                    serverFunctions.registerInstanceId(instanceId);
                }
            }
        });
    }

    /**
     * POST request to unregister an Instance ID.
     */
    public void postUnregisterInstanceId(final String instanceId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (WebDataSource.class) {
                    serverFunctions.unregisterInstanceId(instanceId);
                }
            }
        });
    }
}
