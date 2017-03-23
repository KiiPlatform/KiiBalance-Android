/*
 * Copyright 2013 Kii
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
package com.kii.sample.balance;

import android.app.Application;

import com.kii.cloud.storage.Kii;
import com.kii.sample.balance.kiiobject.Constants;

public class MobileApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Kii.initialize(
                getApplicationContext(),
                Constants.APP_ID,  // Put your App ID
                Constants.APP_KEY, // Put your App Key
                Constants.APP_SITE // Put your site as you've specified upon creating the app on the dev portal
        );
    }
}
