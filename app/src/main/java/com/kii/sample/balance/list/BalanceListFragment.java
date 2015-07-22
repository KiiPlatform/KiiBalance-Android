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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;
import com.kii.sample.balance.Pref;
import com.kii.sample.balance.R;
import com.kii.sample.balance.kiiobject.Constants;
import com.kii.sample.balance.kiiobject.Field;
import com.kii.sample.balance.title.TitleFragment;
import com.kii.util.ViewUtil;
import com.kii.util.ProgressDialogFragment;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This fragment shows a list of balance
 */
public class BalanceListFragment extends ListFragment {
    private static final NumberFormat AMOUNT_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private static final int REQUEST_ADD = 1000;
    private static final int REQUEST_EDIT = 1001;

    @Bind(R.id.toolbar) Toolbar mToolBar;
    @Bind(R.id.text_remains) TextView mTotalText;

    public static BalanceListFragment newInstance() {
        return new BalanceListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // show menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);

        ButterKnife.bind(this, root);

        setListAdapter(new KiiObjectAdapter());
        
        return root;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // init ToolBar
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setTitle(R.string.balance);
        activity.setSupportActionBar(mToolBar);

        getItems();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_list, menu);
    }

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

    private void logout() {
        // clear token
        Pref.setStoredAccessToken(getActivity(), "");
        // next fragment
        ViewUtil.toNextFragment(getFragmentManager(), TitleFragment.newInstance(), false);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        KiiObject object = (KiiObject) adapter.getItem(position);
        
        // show dialog
        ItemEditDialogFragment dialog = ItemEditDialogFragment.newInstance(this, REQUEST_EDIT, object.toUri().toString(),
                object.getString(Field.NAME), object.getInt(Field.TYPE), object.getInt(Field.AMOUNT));
        dialog.show(getFragmentManager(), "");
    }

    @OnClick(R.id.button_add)
    void addClicked() {
        // show Dialog
        ItemEditDialogFragment dialog = ItemEditDialogFragment.newInstance(this, REQUEST_ADD, null, null, 0, 0);
        dialog.show(getFragmentManager(), "");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) { return; }

        switch (requestCode) {
        case REQUEST_ADD: {
            String name = data.getStringExtra(ItemEditDialogFragment.RESULT_NAME);
            int type = data.getIntExtra(ItemEditDialogFragment.RESULT_TYPE, Field.Type.EXPENSE);
            int amount = data.getIntExtra(ItemEditDialogFragment.RESULT_AMOUNT, 0);

            createObject(name, type, amount);
            break;
        }
        case REQUEST_EDIT: {
            String action = data.getAction();
            String objectId = data.getStringExtra(ItemEditDialogFragment.RESULT_OBJECT_ID);
            String name = data.getStringExtra(ItemEditDialogFragment.RESULT_NAME);
            int type = data.getIntExtra(ItemEditDialogFragment.RESULT_TYPE, Field.Type.EXPENSE);
            int amount = data.getIntExtra(ItemEditDialogFragment.RESULT_AMOUNT, 0);

            if (ItemEditDialogFragment.ACTION_UPDATE.equals(action)) {
                updateObjectInList(objectId, name, type, amount);
            } else {
                deleteObject(objectId);
            }
            break;
        }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // region Kii Cloud operation

    private void getItems() {
        KiiUser user = KiiUser.getCurrentUser();
        KiiBucket bucket = user.bucket(Constants.BUCKET_NAME);

        // create query instance.
        KiiQuery query = new KiiQuery();
        // sort KiiObject by _created
        query.sortByAsc(Field._CREATED);

        // call Kii API
        bucket.query(new KiiQueryCallBack<KiiObject>() {
            @Override
            public void onQueryCompleted(int token, KiiQueryResult<KiiObject> result, Exception e) {
                super.onQueryCompleted(token, result, e);
                if (e != null) {
                    showToast(e.getMessage());
                    return;
                }
                // add all object to adapter
                KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
                List<KiiObject> list = result.getResult();
                for (KiiObject object : list) {
                    adapter.add(object);
                }

                // check
                if (!result.hasNext()) {
                    adapter.notifyDataSetChanged();
                    refreshTotalAmount();
                    return;
                }
                // try to get rest
                result.getNextQueryResult(this);
            }
        }, query);
    }

    private void createObject(String name, int type, int amount) {
        // if name is blank, use income/expense as name
        if (TextUtils.isEmpty(name)) {
            if (type == Field.Type.INCOME) {
                name = getString(R.string.income);
            } else {
                name = getString(R.string.expense);
            }
        }

        // Create an Object instance
        KiiUser user = KiiUser.getCurrentUser();
        KiiBucket bucket = user.bucket(Constants.BUCKET_NAME);
        KiiObject object = bucket.object();

        object.set(Field.NAME, name);
        object.set(Field.TYPE, type);
        object.set(Field.AMOUNT, amount);

        // show progress
        ProgressDialogFragment progress = ProgressDialogFragment.newInstance(getActivity(), R.string.add, R.string.add);
        progress.show(getFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);

        // call KiiCloud API
        object.save(new KiiObjectCallBack() {
            @Override
            public void onSaveCompleted(int token, KiiObject object, Exception e) {
                super.onSaveCompleted(token, object, e);
                ProgressDialogFragment.hide(getFragmentManager());

                // error check
                if (e != null) {
                    showToast(e.getMessage());
                    return;
                }

                showToast(R.string.add_succeeded);
                addObjectToList(object);
            }
        });
    }

    private void updateObjectInList(final String objectId, String name, int type, int amount) {
        // if name is blank, use income/expense as name
        if (name == null || name.length() == 0) {
            if (type == Field.Type.INCOME) {
                name = getString(R.string.income);
            } else {
                name = getString(R.string.expense);
            }
        }

        // Create an Object instance with its id
        KiiObject object = KiiObject.createByUri(Uri.parse(objectId));

        object.set(Field.NAME, name);
        object.set(Field.TYPE, type);
        object.set(Field.AMOUNT, amount);

        // show progress
        ProgressDialogFragment progress = ProgressDialogFragment.newInstance(getActivity(), R.string.update, R.string.update);
        progress.show(getFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);

        // call KiiCloud API
        object.save(new KiiObjectCallBack() {
            @Override
            public void onSaveCompleted(int token, KiiObject object, Exception e) {
                super.onSaveCompleted(token, object, e);
                ProgressDialogFragment.hide(getFragmentManager());

                // error check
                if (e != null) {
                    showToast(e.getMessage());
                    return;
                }

                showToast(R.string.update_succeeded);
                updateObjectInList(object, objectId);
            }
        });
    }

    private void deleteObject(final String objectId) {
        // Create an Object instance with its id
        KiiObject object = KiiObject.createByUri(Uri.parse(objectId));

        // show progress
        ProgressDialogFragment progress = ProgressDialogFragment.newInstance(getActivity(), R.string.delete, R.string.delete);
        progress.show(getFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);

        // call KiiCloud API
        object.delete(new KiiObjectCallBack() {
            @Override
            public void onDeleteCompleted(int token, Exception e) {
                super.onDeleteCompleted(token, e);
                ProgressDialogFragment.hide(getFragmentManager());

                // error check
                if (e != null) {
                    showToast(e.getMessage());
                    return;
                }

                showToast(R.string.delete_succeeded);
                removeObjectFromList(objectId);
            }
        });
    }

    // endregion
    // region UI refresh APIs

    /**
     * Add KiiObject to list
     * @param object Kii Object
     */
    void addObjectToList(KiiObject object) {
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        adapter.add(object);
        
        adapter.notifyDataSetChanged();
        refreshTotalAmount();
    }
    
    /**
     * Update object in List
     * @param object KiiObject
     * @param objectId Object ID
     */
    void updateObjectInList(KiiObject object, String objectId) {
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        adapter.updateObject(object, objectId);
        
        adapter.notifyDataSetChanged();
        adapter.computeTotalAmount();
        refreshTotalAmount();
    }
    
    /**
     * Remove object from List
     * @param objectId Object ID
     */
    void removeObjectFromList(String objectId) {
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
        if (mTotalText == null) { return; }

        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        int totalAmount = adapter.getTotalAmount();
        mTotalText.setText(AMOUNT_FORMAT.format(adapter.getTotalAmount() / 100.0));
        if (totalAmount >= 0) {
            mTotalText.setTextColor(Color.BLACK);
        } else {
            mTotalText.setTextColor(Color.RED);
        }
    }

    // endregion

    private void showToast(int msgId) {
        showToast(getString(msgId));
    }

    private void showToast(String message) {
        Activity activity = getActivity();
        if (activity == null) { return; }

        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }
}
