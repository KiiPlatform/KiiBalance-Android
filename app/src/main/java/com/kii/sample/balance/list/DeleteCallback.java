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
package com.kii.sample.balance.list;

import android.support.v4.app.FragmentActivity;

import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.sample.balance.R;
import com.kii.util.ViewUtil;
import com.kii.util.dialog.ProgressDialogFragment;

/**
 * Callback object for deleting item
 */
public class DeleteCallback extends KiiObjectCallBack {
    
    private BalanceListFragment fragment;
    private String objectId;
    
    public DeleteCallback(BalanceListFragment fragment, String id) {
        super();
        this.fragment = fragment;
        this.objectId = id;
    }

    /*
     * (non-Javadoc)
     * @see com.kii.cloud.storage.callback.KiiObjectCallBack#onDeleteCompleted(int, java.lang.Exception)
     */
    @Override
    public void onDeleteCompleted(int token, Exception e) {
        super.onDeleteCompleted(token, e);
        ProgressDialogFragment.hide(fragment.getFragmentManager());
        
        FragmentActivity activity = fragment.getActivity();
        if (activity == null) { return; }
        
        // error check
        if (e != null) {
            ViewUtil.showToast(activity.getApplicationContext(), e.getMessage());
            return;
        }
        
        ViewUtil.showToast(activity.getApplicationContext(), R.string.delete_succeeded);
        fragment.removeObjectFromList(objectId);
    }
}
