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
package com.kii.util;

import com.kii.sample.balance.R;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

public class ViewUtil {

    /**
     * replace with next fragment
     * @param manager is fragment manager
     * @param next is fragment you want to replace with
     * @param addBackStack true : add current fragment to back stack
     */
    public static void toNextFragment(FragmentManager manager, Fragment next, boolean addBackStack) {
        if (manager == null) { return; }
        FragmentTransaction transaction = manager.beginTransaction();
        if (addBackStack) {
            transaction.addToBackStack("");
        }
        transaction.replace(R.id.main, next);
        transaction.commit();
    }

    /*
     * Display the message with the toast
     */
    public static void showToast(Activity activity, int id) {
        if (activity == null) { return; }
        showToast(activity, activity.getString(id));
    }

    /*
     * Display the message with the toast
     */
    public static void showToast(Activity activity, String message) {
        if (activity == null) { return; }

        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }

}
