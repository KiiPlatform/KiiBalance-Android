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

import java.lang.ref.WeakReference;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.sample.balance.R;
import com.kii.sample.balance.kiiobject.Constants;
import com.kii.sample.balance.kiiobject.Field;
import com.kii.util.ViewUtil;
import com.kii.util.dialog.ProgressDialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ItemEditDialogFragment extends DialogFragment {
    private static final String ARGS_OBJECT_ID = "objectId";
    private static final String ARGS_NAME = "name";
    private static final String ARGS_TYPE = "type";
    private static final String ARGS_AMOUNT = "amount";

    static final String ACTION_CREATE = "create";
    static final String ACTION_UPDATE = "update";
    static final String ACTION_DELETE = "delete";

    static final String RESULT_OBJECT_ID = "objectId";
    static final String RESULT_NAME = "name";
    static final String RESULT_TYPE = "type";
    static final String RESULT_AMOUNT = "amount";

    @Bind(R.id.edit_name) EditText mNameEdit;
    @Bind(R.id.radio_type_group) RadioGroup mRadioGroup;
    @Bind(R.id.type_income) RadioButton mIncomeRadio;
    @Bind(R.id.type_expense) RadioButton mExpenseRadio;
    @Bind(R.id.edit_amount) EditText mAmountEdit;
    @Bind(R.id.edit_sub_amount) EditText mSubAmountEdit;

    private String mObjectId;
    private String mName;
    private int mType;
    private int mAmount;

    public static ItemEditDialogFragment newInstance(Fragment target, int requestCode,
            String objectId, String name, int type, int amount) {
        ItemEditDialogFragment fragment = new ItemEditDialogFragment();
        fragment.setTargetFragment(target, requestCode);
        
        Bundle args = new Bundle();
        args.putString(ARGS_OBJECT_ID, objectId);
        args.putString(ARGS_NAME, name);
        args.putInt(ARGS_TYPE, type);
        args.putInt(ARGS_AMOUNT, amount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mObjectId = args.getString(ARGS_OBJECT_ID);
        mName = args.getString(ARGS_NAME);
        mType = args.getInt(ARGS_TYPE);
        mAmount = args.getInt(ARGS_AMOUNT);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_add, null);
        ButterKnife.bind(this, root);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root);
        
        if (isDialogForCreate()) {
            builder.setPositiveButton(R.string.add, mClickListener);
            builder.setNegativeButton(R.string.cancel, mClickListener);
        } else {
            builder.setPositiveButton(R.string.update, mClickListener);
            builder.setNeutralButton(R.string.delete, mClickListener);
            builder.setNegativeButton(R.string.cancel, mClickListener);
        }
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            setDefaultValues();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.unbind(this);
    }

    /**
     * @return true if this dialog is for object creation 
     */
    private boolean isDialogForCreate() {
        return (mObjectId == null);
    }

    /**
     * Set default values
     */
    private void setDefaultValues() {
        if (isDialogForCreate()) { return; }

        if (mNameEdit == null || mIncomeRadio == null || mExpenseRadio == null ||
                mAmountEdit == null || mSubAmountEdit == null) { return; }

        // item name
        mNameEdit.setText(mName);
        
        // item type : income / expense
        if (mType == Field.Type.INCOME) {
            mIncomeRadio.setChecked(true);
        } else {
            mExpenseRadio.setChecked(true);
        }
        
        // amount
        mAmountEdit.setText(String.valueOf(mAmount / 100));
        mSubAmountEdit.setText(String.valueOf(mAmount % 100));
    }

    private final DialogInterface.OnClickListener mClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (isDialogForCreate()) {
                    submit(ACTION_CREATE);
                } else {
                    submit(ACTION_UPDATE);
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                submit(ACTION_DELETE);
                break;
            }
        }
    };

    private void submit(String action) {
        Fragment target = getTargetFragment();
        if (target == null) { return; }

        String name = mNameEdit.getText().toString();
        int type = toType(mRadioGroup.getCheckedRadioButtonId());
        int amount = toInt(mAmountEdit.getText().toString());
        int subAmount = toInt(mSubAmountEdit.getText().toString());

        Intent data = new Intent(action);
        data.putExtra(RESULT_OBJECT_ID, mObjectId);
        data.putExtra(RESULT_NAME, name);
        data.putExtra(RESULT_TYPE, type);
        data.putExtra(RESULT_AMOUNT, amount * 100 + subAmount);

        target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
    }

    private int toType(int id) {
        switch (id) {
        case R.id.type_income: return Field.Type.INCOME;
        case R.id.type_expense: return Field.Type.EXPENSE;
        }
        return Field.Type.EXPENSE;
    }

    private int toInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

        /**
         * Create KiiObject and upload it to KiiCloud
         * @param fragment : dialog
         */
        /*
        private void createObject(DialogFragment fragment) {
            // get input params
            String name = ViewUtil.getValueOfEditText(root, R.id.edit_name);
            int type = toType(ViewUtil.getIdOfRadioChecked(root, R.id.radio_type_group));
            int amount = toInt(ViewUtil.getValueOfEditText(root, R.id.edit_amount));
            int subAmount = toInt(ViewUtil.getValueOfEditText(root, R.id.edit_sub_amount));
            
            // if name is blank, use income/expense as name
            if (name == null || name.length() == 0) {
                if (type == Field.Type.INCOME) {
                    name = fragment.getString(R.string.income);
                } else {
                    name = fragment.getString(R.string.expense);
                }
            }
            
            // Create an Object instance
            KiiUser user = KiiUser.getCurrentUser();
            KiiBucket bucket = user.bucket(Constants.BUCKET_NAME);
            KiiObject object = bucket.object();
            
            object.set(Field.NAME, name);
            object.set(Field.TYPE, type);
            object.set(Field.AMOUNT, amount * 100 + subAmount);

            // show progress
            ProgressDialogFragment progress = ProgressDialogFragment.newInstance(fragment.getActivity(), R.string.add, R.string.add);
            progress.show(fragment.getFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);
            
            // call KiiCloud API
            BalanceListFragment target = (BalanceListFragment)fragment.getTargetFragment();
            AddCallback callback = new AddCallback(target, null);
            object.save(callback);
        }

        /**
         * Update KiiObject which is already in KiiCloud
         * @param fragment : dialog
         */
        /*
        private void updateObjectInList(DialogFragment fragment) {
            // get input params
            String name = ViewUtil.getValueOfEditText(root, R.id.edit_name);
            int type = toType(ViewUtil.getIdOfRadioChecked(root, R.id.radio_type_group));
            int amount = toInt(ViewUtil.getValueOfEditText(root, R.id.edit_amount));
            int subAmount = toInt(ViewUtil.getValueOfEditText(root, R.id.edit_sub_amount));
            
            // if name is blank, use income/expense as name
            if (name == null || name.length() == 0) {
                if (type == Field.Type.INCOME) {
                    name = fragment.getString(R.string.income);
                } else {
                    name = fragment.getString(R.string.expense);
                }
            }
            
            // get object id
            Bundle args = fragment.getArguments();
            String objectId = args.getString(ARGS_OBJECT_ID);
            
            // Create an Object instance with its id
            KiiObject object = KiiObject.createByUri(Uri.parse(objectId));
            
            object.set(Field.NAME, name);
            object.set(Field.TYPE, type);
            object.set(Field.AMOUNT, amount * 100 + subAmount);
            
            // show progress
            ProgressDialogFragment progress = ProgressDialogFragment.newInstance(fragment.getActivity(), R.string.update, R.string.update);
            progress.show(fragment.getFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);
            
            // call KiiCloud API
            BalanceListFragment target = (BalanceListFragment)fragment.getTargetFragment();
            AddCallback callback = new AddCallback(target, objectId);
            object.save(callback);
        }
        
        /**
         * Delete KiiObject from KiiCloud
         * @param fragment 
         */
        /*
        private void removeObjectFromList(DialogFragment fragment) {
            // get Object id
            Bundle args = fragment.getArguments();
            String objectId = args.getString(ARGS_OBJECT_ID);
            
            // Create an Object instance with its id
            KiiObject object = KiiObject.createByUri(Uri.parse(objectId));

            // show progress
            ProgressDialogFragment progress = ProgressDialogFragment.newInstance(fragment.getActivity(), R.string.delete, R.string.delete);
            progress.show(fragment.getFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);
            
            // call KiiCloud API
            BalanceListFragment target = (BalanceListFragment)fragment.getTargetFragment();
            DeleteCallback callback = new DeleteCallback(target, objectId);
            object.delete(callback);
        }

        private int toType(int id) {
            switch (id) {
            case R.id.type_income: return Field.Type.INCOME;
            case R.id.type_expense: return Field.Type.EXPENSE;
            }
            return Field.Type.EXPENSE;
        }
        
        private int toInt(String value) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
    */
}
