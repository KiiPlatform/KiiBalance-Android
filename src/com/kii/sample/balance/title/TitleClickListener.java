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

import android.view.View;

import com.kii.sample.balance.R;
import com.kii.util.listener.BaseClickListener;

/**
 * Listener for buttons 
 */
public class TitleClickListener extends BaseClickListener<TitleFragment> {

    public TitleClickListener(TitleFragment fragment) {
        super(fragment);
    }
    
    /*
     * (non-Javadoc)
     * @see com.kii.util.listener.BaseClickListener#onClick(android.view.View, android.support.v4.app.Fragment)
     */
    @Override
    public void onClick(View v, TitleFragment fragment) {
        switch (v.getId()) {
        case R.id.button_login:
            showLoginDialog(fragment);
            break;
        case R.id.button_register:
            showRegisterDialog(fragment);
            break;
        }
    }
    
    /**
     * Show dialog for login
     * @param fragment is used for callback
     */
    private void showLoginDialog(TitleFragment fragment) {
        LoginDialogFragment dialog = LoginDialogFragment.newInstance();
        dialog.setTargetFragment(fragment, 0);
        dialog.show(fragment.getFragmentManager(), "");
    }
    
    /**
     * Show dialog for registration
     * @param fragment is used for callback
     */
    private void showRegisterDialog(TitleFragment fragment) {
        RegistrationDialogFragment dialog = RegistrationDialogFragment.newInstance();
        dialog.setTargetFragment(fragment, 0);
        dialog.show(fragment.getFragmentManager(), "");
    }
}
