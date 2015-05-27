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
     * call setOnClickListener(listener) to each view
     * @param root is parent view
     * @param listener is listener object
     * @param ids is list of id which you want to set
     */
    public static void setClickListener(View root, View.OnClickListener listener, int... ids) {
        for (int id : ids) {
            View view = root.findViewById(id);
            view.setOnClickListener(listener);
        }
    }
    /**
     * get a value of EditText 
     * @param root
     * @param resId
     * @return value of EditText
     */
    public static String getValueOfEditText(View root, int resId) {
        EditText edit = (EditText) root.findViewById(resId);
        if (edit == null) { throw new RuntimeException("View not found id=" + resId); }
        return edit.getText().toString();
    }
    
    /**
     * get id of checked radio buttion
     * @param root
     * @param resId is id of RadioGroup
     * @return
     */
    public static int getIdOfRadioChecked(View root, int resId) {
        RadioGroup group = (RadioGroup) root.findViewById(resId);
        if (group == null) { throw new RuntimeException("View not found id=" + resId); }
        return group.getCheckedRadioButtonId();
    }
    
    /**
     * get a value of SeekBar 
     * @param root
     * @param resId
     * @return value of SeekBar
     */
    public static int getValueOfSeekBar(View root, int resId) {
        SeekBar bar = (SeekBar) root.findViewById(resId);
        if (bar == null) { throw new RuntimeException("View not found id=" + resId); }
        return bar.getProgress();
    }
    
    /**
     * show toast
     * @param context
     * @param resId is R.string.xxxx to show as toast
     */
    public static void showToast(Context context, int resId) {
        showToast(context, context.getString(resId));
    }
    
    /**
     * show toast
     * @param context
     * @param message 
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * replace with next fragment
     * @param manager is fragment manager
     * @param next is fragment you want to replace with
     * @param addBackstack true : add current fragment to backstack
     */
    public static void toNextFragment(FragmentManager manager, Fragment next, boolean addBackstack) {
        FragmentTransaction transaction = manager.beginTransaction();
        if (addBackstack) {
            transaction.addToBackStack("");
        }
        transaction.replace(R.id.main, next);
        transaction.commit();
    }
}
