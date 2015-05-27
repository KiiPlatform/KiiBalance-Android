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

import java.util.List;

import android.support.v4.app.FragmentActivity;

import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.query.KiiQueryResult;
import com.kii.util.ViewUtil;
import com.kii.util.dialog.ProgressDialogFragment;

/**
 * Callback object for query items
 */
public class QueryCallback extends KiiQueryCallBack<KiiObject> {
    
    private BalanceListFragment fragment;
    
    public QueryCallback(BalanceListFragment fragment) {
        super();
        this.fragment = fragment;
    }
    
    /*
     * (non-Javadoc)
     * @see com.kii.cloud.storage.callback.KiiQueryCallBack#onQueryCompleted(int, com.kii.cloud.storage.query.KiiQueryResult, java.lang.Exception)
     */
    @Override
    public void onQueryCompleted(int arg0, KiiQueryResult<KiiObject> result, Exception e) {
        super.onQueryCompleted(arg0, result, e);
        FragmentActivity activity = fragment.getActivity();
        if (activity == null) { return; }
        
        if (e != null) {
            ProgressDialogFragment.hide(fragment.getFragmentManager());
            ViewUtil.showToast(activity.getApplicationContext(), e.getMessage());
            return;
        }
        // add all object to adapter
        KiiObjectAdapter adapter = (KiiObjectAdapter) fragment.getListAdapter();
        List<KiiObject> list = result.getResult();
        for (KiiObject object : list) {
            adapter.add(object);
        }
        
        // check 
        if (!result.hasNext()) {
            ProgressDialogFragment.hide(fragment.getFragmentManager());
            adapter.notifyDataSetChanged();
            fragment.refreshTotalAmount();
            return;
        }
        // try to get rest 
        result.getNextQueryResult(this);
    }
}
