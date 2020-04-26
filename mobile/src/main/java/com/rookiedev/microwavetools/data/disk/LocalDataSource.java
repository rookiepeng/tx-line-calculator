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

package com.rookiedev.microwavetools.data.disk;

import androidx.lifecycle.LiveData;

//import com.example.android.classytaxijava.AppExecutors;
//import com.example.android.classytaxijava.data.SubscriptionStatus;
import com.rookiedev.microwavetools.AppExecutors;
import com.rookiedev.microwavetools.data.SubscriptionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class LocalDataSource {
    private static volatile LocalDataSource INSTANCE = null;

    private final Executor executor;
    private final AppDatabase appDatabase;

    /**
     * Get the list of subscriptions from the localDataSource and get notified when the data changes.
     */
    public LiveData<List<SubscriptionStatus>> subscriptions;

    private LocalDataSource(Executor executor, AppDatabase appDatabase) {
        this.executor = executor;
        this.appDatabase = appDatabase;

        subscriptions = appDatabase.subscriptionStatusDao().getAll();
    }

    public static LocalDataSource getInstance(AppExecutors executors, AppDatabase database) {
        if (INSTANCE == null) {
            synchronized (LocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LocalDataSource(executors.diskIO, database);
                }
            }
        }
        return INSTANCE;
    }

    public void updateSubscriptions(final List<SubscriptionStatus> subscriptions) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        // Delete existing subscriptions.
                        appDatabase.subscriptionStatusDao().deleteAll();
                        // Put new subscriptions data into localDataSource.
                        appDatabase.subscriptionStatusDao().insertAll(subscriptions);
                    }
                });
            }
        });
    }

    /**
     * Delete local user data when the user signs out.
     */
    public void deleteLocalUserData() {
        updateSubscriptions(new ArrayList<SubscriptionStatus>());
    }
}
