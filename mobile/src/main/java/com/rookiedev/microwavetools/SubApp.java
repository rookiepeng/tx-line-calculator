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

package com.rookiedev.microwavetools;

import android.app.Application;

import com.rookiedev.microwavetools.billing.BillingClientLifecycle;
import com.rookiedev.microwavetools.data.DataRepository;
import com.rookiedev.microwavetools.data.disk.AppDatabase;
import com.rookiedev.microwavetools.data.disk.LocalDataSource;
import com.rookiedev.microwavetools.data.network.WebDataSource;
import com.rookiedev.microwavetools.data.network.firebase.FakeServerFunctions;
import com.rookiedev.microwavetools.data.network.firebase.ServerFunctions;
import com.rookiedev.microwavetools.data.network.firebase.ServerFunctionsImpl;

//import com.example.android.classytaxijava.billing.BillingClientLifecycle;
//import com.example.android.classytaxijava.data.DataRepository;
//import com.example.android.classytaxijava.data.disk.LocalDataSource;
//import com.example.android.classytaxijava.data.disk.AppDatabase;
//import com.example.android.classytaxijava.data.network.WebDataSource;
//import com.example.android.classytaxijava.data.network.firebase.FakeServerFunctions;
//import com.example.android.classytaxijava.data.network.firebase.ServerFunctions;
//import com.example.android.classytaxijava.data.network.firebase.ServerFunctionsImpl;

/**
 * Android Application class. Used for accessing singletons.
 */
public class SubApp extends Application {
    private final AppExecutors executors = new AppExecutors();

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this);
    }

    public LocalDataSource getLocalDataSource() {
        return LocalDataSource.getInstance(executors, getDatabase());
    }

    public ServerFunctions getServerFunctions() {
        if (Constants.USE_FAKE_SERVER) {
            return FakeServerFunctions.getInstance();
        } else {
            return ServerFunctionsImpl.getInstance();
        }
    }

    public WebDataSource getWebDataSource() {
        return WebDataSource.getInstance(executors, getServerFunctions());
    }

    public BillingClientLifecycle getBillingClientLifecycle() {
        return BillingClientLifecycle.getInstance(this);
    }

    public DataRepository getRepository() {
        return DataRepository
                .getInstance(getLocalDataSource(), getWebDataSource(), getBillingClientLifecycle());
    }
}
