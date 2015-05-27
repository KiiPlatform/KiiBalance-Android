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

import java.lang.ref.WeakReference;

import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.util.dialog.ProgressDialogFragment;

/**
 * Callback class for auto login
 */
public class AutoLoginCallback extends KiiUserCallBack {

    private WeakReference<MainActivity> activityRef;
    public AutoLoginCallback(MainActivity activity) {
        this.activityRef = new WeakReference<MainActivity>(activity);
    }
    
    /**
     * This method is called when login is completed. 
     * If e == null, login is succeeded.
     */
    @Override
    public void onLoginCompleted(int token, KiiUser user, Exception e) {
        super.onLoginCompleted(token, user, e);
        MainActivity activity = activityRef.get();
        if (activity == null) { return; }

        ProgressDialogFragment.hide(activity.getSupportFragmentManager());
        
        // error check 
        if (e != null) {
            // go to normal login
            activity.toTitleFragment();
            return;
        }
        // login is succeeded then go to List Fragment
        activity.toListFragment();
    }
}
