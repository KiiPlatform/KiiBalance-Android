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
package com.kii.sample.balance.title;

import android.support.v4.app.Fragment;

import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.util.dialog.ProgressDialogFragment;

class RegisterCallback extends KiiUserCallBack {

    private RegistrationDialogFragment dialog;
    
    public RegisterCallback(RegistrationDialogFragment dialog) {
        this.dialog = dialog;
    }

    @Override
    public void onRegisterCompleted(int token, KiiUser user, Exception e) {
        super.onRegisterCompleted(token, user, e);
        ProgressDialogFragment.hide(dialog.getFragmentManager());
        if (e != null) {
            dialog.showErrorMessage("Register failed");
            return;
        }
        dialog.dismiss();
        Fragment callback = dialog.getTargetFragment();
        if (callback != null && callback instanceof TitleFragment) {
            TitleFragment fragment = (TitleFragment) callback;
            fragment.onRegisterFinished();
        }
    }
}
