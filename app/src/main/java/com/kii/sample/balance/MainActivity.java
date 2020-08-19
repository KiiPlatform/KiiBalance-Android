//
//
// Copyright 2017 Kii Corporation
// http://kii.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//
package com.kii.sample.balance;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiCallback;
import com.kii.cloud.storage.KiiUser;
import com.kii.sample.balance.list.BalanceListFragment;
import com.kii.sample.balance.title.TitleFragment;
import com.kii.util.ViewUtil;

public class MainActivity extends AppCompatActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            Kii.onRestoreInstanceState(savedInstanceState);
        } else {
            initializeScreen();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Kii.onSaveInstanceState(outState);
    }

    private void initializeScreen() {
        KiiUser.loginWithStoredCredentials(new KiiCallback<KiiUser>() {
            @Override
            public void onComplete(KiiUser user, Exception exception) {
                if (exception != null) {
                    // Show the title screen.
                    ViewUtil.toNextFragment(getSupportFragmentManager(), TitleFragment.newInstance(), false);
                } else {
                    // Show the data listing screen.
                    ViewUtil.toNextFragment(getSupportFragmentManager(), BalanceListFragment.newInstance(), false);
                }
            }
        });
    }
}
