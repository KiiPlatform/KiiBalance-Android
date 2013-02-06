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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * This class is a helper class for SharedPreferences operation 
 */
public class Pref {
    /**
     * Name of SharedPreferences
     */
    private static final String PREF_NAME = "settings";
    
    /**
     * Keys of SharedPreferences entry
     */
    interface Key {
        static final String APP_ID = "appId";
        static final String APP_KEY = "appKey";
        static final String SITE = "site";
        static final String STORED_ACCESS_TOKEN = "token";
    }

    /**
     * Save access token
     * @param context
     * @param token 
     */
    public static void setStoredAccessToken(Context context, String token) {
        SharedPreferences pref = getSharedPreferences(context);
        Editor edit = pref.edit();
        edit.putString(Key.STORED_ACCESS_TOKEN, token);
        edit.commit();
    }
    
    /**
     * Get access token 
     * @param context
     * @return null if token is not stored in SharedPreferences
     */
    public static String getStoredAccessToken(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        return pref.getString(Key.STORED_ACCESS_TOKEN, null);
    }

    /**
     * @param context
     * @return instance of SharedPreferences 
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
