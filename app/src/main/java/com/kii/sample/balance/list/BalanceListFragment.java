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

import java.text.NumberFormat;
import java.util.Locale;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.sample.balance.Pref;
import com.kii.sample.balance.R;
import com.kii.sample.balance.kiiobject.Constants;
import com.kii.sample.balance.kiiobject.Field;
import com.kii.sample.balance.title.TitleFragment;
import com.kii.util.ViewUtil;
import com.kii.util.dialog.ProgressDialogFragment;

/**
 * This fragment shows a list of balance
 */
public class BalanceListFragment extends ListFragment {
    private static final NumberFormat AMOUNT_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    
    public static BalanceListFragment newInstance() {
        return new BalanceListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        
        ViewUtil.setClickListener(root, new AddButtonClickListener(this), R.id.button_add);
        
        KiiObjectAdapter adapter = new KiiObjectAdapter();
        setListAdapter(adapter);
        
        return root;
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // show menu
        setHasOptionsMenu(true);
        
        // get items
        KiiUser user = KiiUser.getCurrentUser();
        KiiBucket bucket = user.bucket(Constants.BUCKET_NAME);
        
        // create query instance.
        KiiQuery query = new KiiQuery();
        // sort KiiObject by _created 
        query.sortByAsc(Field._CREATED);
        
        // show progress
        ProgressDialogFragment progress = ProgressDialogFragment.newInstance(getActivity(), R.string.loading, R.string.loading);
        progress.show(getFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);
        
        // call Kii API
        QueryCallback callback = new QueryCallback(this);
        bucket.query(callback, query);
        
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(Menu.NONE, R.id.menu_logout, Menu.NONE, R.string.menu_logout);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_logout:
            logout();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        KiiObject object = (KiiObject) adapter.getItem(position);
        
        // show dialog
        ItemEditDialogFragment dialog = ItemEditDialogFragment.newInstance(this, object.toUri().toString(), 
                object.getString(Field.NAME), object.getInt(Field.TYPE), object.getInt(Field.AMOUNT));
        dialog.show(getFragmentManager(), "");
    }

    /**
     * Logout process : remove access token from SharedPreferences and go to Title Fragment
     */
    private void logout() {
        // clear token
        Pref.setStoredAccessToken(getActivity(), "");
        // next fragment
        ViewUtil.toNextFragment(getFragmentManager(), TitleFragment.newInstance(), false);
    }
    
    /**
     * Add KiiObject to list adapter
     * @param object Kii Object
     */
    void addObjectToList(KiiObject object) {
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        adapter.add(object);
        
        adapter.notifyDataSetChanged();
        refreshTotalAmount();
    }
    
    /**
     * Update object with id
     * @param object KiiObject
     * @param objectId Object ID
     */
    void updateObject(KiiObject object, String objectId) {
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        adapter.updateObject(object, objectId);
        
        adapter.notifyDataSetChanged();
        adapter.computeTotalAmount();
        refreshTotalAmount();
    }
    
    /**
     * Delete object with id
     * @param objectId Object ID
     */
    void deleteObject(String objectId) {
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        adapter.delete(objectId);
        
        adapter.notifyDataSetChanged();
        adapter.computeTotalAmount();
        refreshTotalAmount();
    }
    
    /**
     * Refresh the value of total label
     */
    void refreshTotalAmount() {
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        View root = getView();
        if (root == null) { return; }
        
        TextView totalText = (TextView) root.findViewById(R.id.text_remains);
        int totalAmount = adapter.getTotalAmount();
        totalText.setText(AMOUNT_FORMAT.format(adapter.getTotalAmount() / 100.0));
        if (totalAmount >= 0) {
            totalText.setTextColor(Color.BLACK);
        } else {
            totalText.setTextColor(Color.RED);
        }
    }
}
