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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

public class ItemEditDialogFragment extends DialogFragment {
    private static final String ARGS_OBJECT_ID = "objectId";
    private static final String ARGS_NAME = "name";
    private static final String ARGS_TYPE = "type";
    private static final String ARGS_AMOUNT = "amount";

    public static ItemEditDialogFragment newInstance(BalanceListFragment target, 
            String objectId, String name, int type, int amount) {
        ItemEditDialogFragment fragment = new ItemEditDialogFragment();
        fragment.setTargetFragment(target, 0);
        
        Bundle args = new Bundle();
        args.putString(ARGS_OBJECT_ID, objectId);
        args.putString(ARGS_NAME, name);
        args.putInt(ARGS_TYPE, type);
        args.putInt(ARGS_AMOUNT, amount);
        fragment.setArguments(args);
        return fragment;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_add, null);
        
        // get args and set default
        Bundle args = getArguments();
        setDefaultValues(root, args);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root);
        
        DialogInterface.OnClickListener listener = new ClickListener(this, root);
        if (isDialogForCreate(args)) {
            builder.setPositiveButton(R.string.add, listener);
            builder.setNegativeButton(R.string.cancel, listener);
        } else {
            builder.setPositiveButton(R.string.update, listener);
            builder.setNeutralButton(R.string.delete, listener);
            builder.setNegativeButton(R.string.cancel, listener);
        }
        return builder.create();
    }
    
    /**
     * @return true if this dialog is for object creation 
     */
    private static boolean isDialogForCreate(Bundle args) {
        String objectId = args.getString(ARGS_OBJECT_ID);
        return (objectId == null);
    }

    /**
     * Set default values
     * @param root
     * @param args
     */
    private void setDefaultValues(View root, Bundle args) {
        if (isDialogForCreate(args)) { return; }
        
        // item name
        EditText nameEdit = (EditText) root.findViewById(R.id.edit_name);
        nameEdit.setText(args.getString(ARGS_NAME));
        
        // item type : income / expense
        int type = args.getInt(ARGS_TYPE);
        if (type == Field.Type.INCOME) {
            RadioButton radio = (RadioButton) root.findViewById(R.id.type_income);
            radio.setChecked(true);
        } else {
            RadioButton radio = (RadioButton) root.findViewById(R.id.type_expense);
            radio.setChecked(true);
        }
        
        // amount
        int amount = args.getInt(ARGS_AMOUNT);
        EditText amountEdit = (EditText) root.findViewById(R.id.edit_amount);
        amountEdit.setText(String.valueOf(amount / 100));
        EditText subAmountEdit = (EditText) root.findViewById(R.id.edit_sub_amount);
        subAmountEdit.setText(String.valueOf(amount % 100));
    }

    private static class ClickListener implements DialogInterface.OnClickListener {
        
        private WeakReference<DialogFragment> dialogRef;
        private View root;
        ClickListener(DialogFragment dialog, View root) {
            this.dialogRef = new WeakReference<DialogFragment>(dialog);
            this.root = root;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            DialogFragment fragment = dialogRef.get();
            if (fragment == null) { return; }
            
            switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (isDialogForCreate(fragment.getArguments())) {
                    createObject(fragment);
                } else {
                    updateObject(fragment);
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                deleteObject(fragment);
                break;
            }
        }

        /**
         * Create KiiObject and upload it to KiiCloud
         * @param fragment : dialog
         */
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
        private void updateObject(DialogFragment fragment) {
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
        private void deleteObject(DialogFragment fragment) {
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
}
