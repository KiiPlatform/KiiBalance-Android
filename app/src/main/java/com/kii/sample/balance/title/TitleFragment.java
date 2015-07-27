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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kii.cloud.storage.KiiUser;
import com.kii.sample.balance.Pref;
import com.kii.sample.balance.R;
import com.kii.sample.balance.list.BalanceListFragment;
import com.kii.util.ViewUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This fragment shows Title view.
 * User can do the following
 * <ul>
 * <li>Register with username and password</li>
 * <li>Login with username and password</li>
 * </ul>
 */
public class TitleFragment extends Fragment {
    private static final int REQUEST_LOGIN = 1;
    private static final int REQUEST_REGISTER = 2;

    public static TitleFragment newInstance() {
        return new TitleFragment();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_title, container, false);

        ButterKnife.bind(this, root);

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) { return; }
        switch (requestCode) {
        case REQUEST_LOGIN: {
            showListpage();
            return;
        }
        case REQUEST_REGISTER: {
            showToast(getString(R.string.registration_succeeded));
            showListpage();
            return;
        }
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.unbind(this);
    }

    @OnClick(R.id.button_login)
    void loginClicked() {
        LoginDialogFragment dialog = LoginDialogFragment.newInstance(this, REQUEST_LOGIN);
        dialog.show(getFragmentManager(), "");
    }

    @OnClick(R.id.button_register)
    void registerClicked() {
        RegistrationDialogFragment dialog = RegistrationDialogFragment.newInstance(this, REQUEST_REGISTER);
        dialog.show(getFragmentManager(), "");
    }

    private void showToast(String message) {
        Activity activity = getActivity();
        if (activity == null) { return; }

        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }
    
    private void showListpage() {
        Activity activity = getActivity();
        if (activity == null) { return; }

        // store access token
        KiiUser user = KiiUser.getCurrentUser();
        String token = user.getAccessToken();
        Pref.setStoredAccessToken(activity, token);

        ViewUtil.toNextFragment(getFragmentManager(), BalanceListFragment.newInstance(), false);
    }
}
