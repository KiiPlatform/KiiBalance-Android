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

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.Kii.Site;
import com.kii.cloud.storage.KiiUser;
import com.kii.sample.balance.kiiobject.Constants;
import com.kii.sample.balance.list.BalanceListFragment;
import com.kii.sample.balance.title.TitleFragment;
import com.kii.util.dialog.ProgressDialogFragment;

public class MainActivity extends AppCompatActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // initialize
            initKiiSDK();

            // check access token
            String token = Pref.getStoredAccessToken(getApplicationContext());
            if (token == null || token.length() == 0) {
                toTitleFragment();
                return;
            }

            // check access token
            ProgressDialogFragment progress = ProgressDialogFragment.newInstance(getString(R.string.login), getString(R.string.login));
            progress.show(getSupportFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);

            // login with token
            AutoLoginCallback callback = new AutoLoginCallback(this);
            KiiUser.loginWithToken(callback, token);
        } else {
            // Restore Kii SDK states
            Kii.onRestoreInstanceState(savedInstanceState);
        }
    }

    /**
     * Initialize KiiSDK
     * Please change APP_ID/APP_KEY to your application
     */
    private void initKiiSDK() {
        
        Kii.initialize(
                Constants.APP_ID,  // Put your App ID
                Constants.APP_KEY, // Put your App Key
                Site.US            // Put your site as you've specified upon creating the app on the dev portal
                );
    }
    
    /**
     * Show title fragment
     */
    public void toTitleFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        
        TitleFragment next = TitleFragment.newInstance();
        transaction.replace(R.id.main, next);
        
        transaction.commit();
    }

    /**
     * Show list fragment
     */
    public void toListFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        
        BalanceListFragment next = BalanceListFragment.newInstance();
        transaction.replace(R.id.main, next);
        
        transaction.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Kii SDK states
        Kii.onSaveInstanceState(outState);
    }
}
