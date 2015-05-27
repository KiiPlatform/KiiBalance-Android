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

import android.view.View;

import com.kii.sample.balance.R;
import com.kii.util.listener.BaseClickListener;

public class AddButtonClickListener extends BaseClickListener<BalanceListFragment> {

    public AddButtonClickListener(BalanceListFragment fragment) {
        super(fragment);
    }

    /*
     * (non-Javadoc)
     * @see com.kii.util.listener.BaseClickListener#onClick(android.view.View, android.support.v4.app.Fragment)
     */
    @Override
    protected void onClick(View v, BalanceListFragment fragment) {
        switch (v.getId()) {
        case R.id.button_add:
            showAddDialog(fragment);
            break;
        }
    }

    /**
     * Show dialog for adding new item
     * @param fragment
     */
    private void showAddDialog(BalanceListFragment fragment) {
        ItemEditDialogFragment dialog = ItemEditDialogFragment.newInstance(fragment, null, null, 0, 0);
        dialog.show(fragment.getFragmentManager(), "");
    }

}
