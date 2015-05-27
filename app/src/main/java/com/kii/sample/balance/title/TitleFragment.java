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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kii.cloud.storage.KiiUser;
import com.kii.sample.balance.Pref;
import com.kii.sample.balance.R;
import com.kii.sample.balance.list.BalanceListFragment;
import com.kii.util.ViewUtil;

/**
 * This fragment shows Title view.
 * User can do the following
 * <ul>
 * <li>Register with username and password</li>
 * <li>Login with username and password</li>
 * </ul>
 */
public class TitleFragment extends Fragment {
    public static TitleFragment newInstance() {
        return new TitleFragment();
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_title, container, false);
        
        ViewUtil.setClickListener(root, new TitleClickListener(this), R.id.button_login, R.id.button_register);
        
        return root;
    }
    
    /**
     * This method is called when user registration is finished. 
     * See {@link RegisterCallback#onRegisterCompleted(int, KiiUser, Exception)}
     */
    void onRegisterFinished() {
        FragmentActivity activity = getActivity();
        if (activity == null) { return; }
        ViewUtil.showToast(activity, "Register succeeded");
        // to next fragment
        onLoginFinished();
    }
    
    /**
     * This method is called when login is finished.
     * See {@link LoginCallback#onLoginCompleted(int, KiiUser, Exception)}
     */
    void onLoginFinished() {
        FragmentActivity activity = getActivity();
        if (activity == null) { return; }
        // store access token
        KiiUser user = KiiUser.getCurrentUser();
        String token = user.getAccessToken();
        Pref.setStoredAccessToken(activity, token);
        
        ViewUtil.toNextFragment(getFragmentManager(), BalanceListFragment.newInstance(), false);
    }
}
