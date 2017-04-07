//
//
// Copyright 2017 Kii Corporation
// http://kii.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//
package com.kii.sample.balance.list;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;
import com.kii.sample.balance.R;
import com.kii.sample.balance.kiiobject.Constants;
import com.kii.sample.balance.kiiobject.Field;
import com.kii.sample.balance.title.TitleFragment;
import com.kii.util.ViewUtil;
import com.kii.util.ProgressDialogFragment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * This fragment shows a list of income and expense entries.
 */
public class BalanceListFragment extends ListFragment {
    private static final NumberFormat AMOUNT_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private static final int REQUEST_ADD = 1000;
    private static final int REQUEST_EDIT = 1001;

    @BindView(R.id.toolbar) Toolbar mToolBar;
    @BindView(R.id.button_add) FloatingActionButton mAddButton;
    @BindView(R.id.text_remains_label) TextView mTotalTextLabel;
    @BindView(R.id.text_remains_value) TextView mTotalTextValue;

    private Unbinder mButterKnifeUnbinder;

    public static BalanceListFragment newInstance() {
        return new BalanceListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Show the menu.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);

        mButterKnifeUnbinder = ButterKnife.bind(this, root);

        setListAdapter(new KiiObjectAdapter());
        
        return root;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize the toolbar.
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setTitle(R.string.balance);
        activity.setSupportActionBar(mToolBar);

        getItems();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mButterKnifeUnbinder.unbind();
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
        case R.id.menu_refresh:
            getItems();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        // Show the title screen.
        KiiUser.logOut();
        ViewUtil.toNextFragment(getFragmentManager(), TitleFragment.newInstance(), false);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        KiiObject object = (KiiObject) adapter.getItem(position);
        
        // Show the edit dialog.
        ItemEditDialogFragment dialog = ItemEditDialogFragment.newInstance(this, REQUEST_EDIT, object.getString("_id"),
                object.getString(Field.NAME), object.getInt(Field.TYPE), object.getInt(Field.AMOUNT));
        dialog.show(getFragmentManager(), "");
    }

    @OnClick(R.id.button_add)
    void addClicked() {
        // Show the add dialog.
        ItemEditDialogFragment dialog = ItemEditDialogFragment.newInstance(this, REQUEST_ADD, null, "", Field.Type.INCOME, 0);
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

    // region The methods below manipulate data on Kii Cloud.

    private void getItems() {
        KiiUser user = KiiUser.getCurrentUser();
        KiiBucket bucket = user.bucket(Constants.BUCKET_NAME);

        // Create a query instance.
        KiiQuery query = new KiiQuery();
        // Sort KiiObjects by the _created field.
        query.sortByAsc(Field._CREATED);

        final List<KiiObject> objectList = new ArrayList<KiiObject>();

        // Call the KiiCloud API to query KiiObjects.
        ProgressDialogFragment.show(getActivity(), getFragmentManager(), R.string.loading);
        bucket.query(new KiiQueryCallBack<KiiObject>() {
            @Override
            public void onQueryCompleted(int token, KiiQueryResult<KiiObject> result, Exception e) {
                super.onQueryCompleted(token, result, e);
                if (e != null) {
                    ProgressDialogFragment.close(getFragmentManager());
                    ViewUtil.showToast(getActivity(), getString(R.string.error_query) + e.getMessage());
                    return;
                }
                objectList.addAll(result.getResult());

                // Check if more KiiObjects exit.
                if (!result.hasNext()) {
                    ProgressDialogFragment.close(getFragmentManager());
                    KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
                    adapter.clear();
                    for (KiiObject object : objectList) {
                        adapter.add(object);
                    }
                    adapter.notifyDataSetChanged();
                    refreshTotalAmount();
                    mAddButton.setVisibility(View.VISIBLE);
                    return;
                }
                // Get the remaining KiiObjects.
                result.getNextQueryResult(this);
            }
        }, query);
    }

    private void createObject(String name, int type, int amount) {
        // If the name is blank, name the entry Income or Expense.
        if (TextUtils.isEmpty(name)) {
            if (type == Field.Type.INCOME) {
                name = getString(R.string.income);
            } else {
                name = getString(R.string.expense);
            }
        }

        // Create a KiiObject instance.
        KiiUser user = KiiUser.getCurrentUser();
        KiiBucket bucket = user.bucket(Constants.BUCKET_NAME);
        KiiObject object = bucket.object();

        object.set(Field.NAME, name);
        object.set(Field.TYPE, type);
        object.set(Field.AMOUNT, amount);

        // Show the progress.
        ProgressDialogFragment.show(getActivity(), getFragmentManager(), R.string.add);

        // Call the KiiCloud API for adding the KiiObject on Kii Cloud.
        object.save(new KiiObjectCallBack() {
            @Override
            public void onSaveCompleted(int token, KiiObject object, Exception e) {
                super.onSaveCompleted(token, object, e);
                ProgressDialogFragment.close(getFragmentManager());

                // Check an error.
                if (e != null) {
                    ViewUtil.showToast(getActivity(), e.getMessage());
                    return;
                }

                ViewUtil.showToast(getActivity(), R.string.add_succeeded);
                addObjectToList(object);
            }
        });
    }

    private void updateObjectInList(final String objectId, String name, int type, int amount) {
        // If the name is blank, name the entry Income or Expense.
        if (TextUtils.isEmpty(name)) {
            if (type == Field.Type.INCOME) {
                name = getString(R.string.income);
            } else {
                name = getString(R.string.expense);
            }
        }

        // Create a KiiObject instance with its ID.
        KiiUser user = KiiUser.getCurrentUser();
        KiiBucket bucket = user.bucket(Constants.BUCKET_NAME);
        KiiObject object = bucket.object(objectId);

        object.set(Field.NAME, name);
        object.set(Field.TYPE, type);
        object.set(Field.AMOUNT, amount);

        // Show the progress.
        ProgressDialogFragment.show(getActivity(), getFragmentManager(), R.string.update);

        // Call the KiiCloud API for updating the KiiObject on Kii Cloud.
        object.save(new KiiObjectCallBack() {
            @Override
            public void onSaveCompleted(int token, KiiObject object, Exception e) {
                super.onSaveCompleted(token, object, e);
                ProgressDialogFragment.close(getFragmentManager());

                // Check an error.
                if (e != null) {
                    ViewUtil.showToast(getActivity(), e.getMessage());
                    return;
                }

                ViewUtil.showToast(getActivity(), R.string.update_succeeded);
                updateObjectInList(object, objectId);
            }
        });
    }

    private void deleteObject(final String objectId) {
        // Create a KiiObject instance with its ID.
        KiiUser user = KiiUser.getCurrentUser();
        KiiBucket bucket = user.bucket(Constants.BUCKET_NAME);
        KiiObject object = bucket.object(objectId);

        // Show the progress.
        ProgressDialogFragment.show(getActivity(), getFragmentManager(), R.string.delete);

        // Call the KiiCloud API for deleting the KiiObject on Kii Cloud.
        object.delete(new KiiObjectCallBack() {
            @Override
            public void onDeleteCompleted(int token, Exception e) {
                super.onDeleteCompleted(token, e);
                ProgressDialogFragment.close(getFragmentManager());

                // Check an error.
                if (e != null) {
                    ViewUtil.showToast(getActivity(), e.getMessage());
                    return;
                }

                ViewUtil.showToast(getActivity(), R.string.delete_succeeded);
                removeObjectFromList(objectId);
            }
        });
    }

    // endregion
    // region The methods below refresh the user interface.

    /**
     * Add a KiiObject to the list.
     * @param object KiiObject
     */
    private void addObjectToList(KiiObject object) {
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        adapter.add(object);
        
        adapter.notifyDataSetChanged();
        refreshTotalAmount();
    }
    
    /**
     * Update a KiiObject in the list.
     * @param object KiiObject
     * @param objectId Object ID
     */
    private void updateObjectInList(KiiObject object, String objectId) {
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        adapter.updateObject(object, objectId);
        
        adapter.notifyDataSetChanged();
        adapter.computeTotalAmount();
        refreshTotalAmount();
    }
    
    /**
     * Remove a KiiObject from the list.
     * @param objectId Object ID
     */
    private void removeObjectFromList(String objectId) {
        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        adapter.delete(objectId);
        
        adapter.notifyDataSetChanged();
        adapter.computeTotalAmount();
        refreshTotalAmount();
    }
    
    /**
     * Refresh the balance.
     */
    private void refreshTotalAmount() {
        if (mTotalTextLabel == null || mTotalTextValue == null) { return; }

        KiiObjectAdapter adapter = (KiiObjectAdapter) getListAdapter();
        int totalAmount = adapter.getTotalAmount();
        mTotalTextValue.setText(AMOUNT_FORMAT.format(adapter.getTotalAmount() / 100.0));
        if (totalAmount >= 0) {
            mTotalTextValue.setTextColor(Color.BLACK);
        } else {
            mTotalTextValue.setTextColor(Color.RED);
        }
        mTotalTextLabel.setText(R.string.remains);
    }

    // endregion

}
