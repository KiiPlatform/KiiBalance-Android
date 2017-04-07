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

import com.kii.sample.balance.R;
import com.kii.sample.balance.kiiobject.Field;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

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

    @BindView(R.id.edit_name) EditText mNameEdit;
    @BindView(R.id.radio_type_group) RadioGroup mRadioGroup;
    @BindView(R.id.type_income) RadioButton mIncomeRadio;
    @BindView(R.id.type_expense) RadioButton mExpenseRadio;
    @BindView(R.id.edit_amount) EditText mAmountEdit;
    @BindView(R.id.edit_sub_amount) EditText mSubAmountEdit;

    private Unbinder mButterKnifeUnbinder;

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
        mButterKnifeUnbinder = ButterKnife.bind(this, root);
        
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

        mButterKnifeUnbinder.unbind();
    }

    /**
     * @return true if this dialog is called for adding an entry
     */
    private boolean isDialogForCreate() {
        return (mObjectId == null);
    }

    /**
     * Set the default values.
     */
    private void setDefaultValues() {
        if (isDialogForCreate()) { return; }

        if (mNameEdit == null || mIncomeRadio == null || mExpenseRadio == null ||
                mAmountEdit == null || mSubAmountEdit == null) { return; }

        // Set the name.
        mNameEdit.setText(mName);
        
        // Set the type: income or expense.
        if (mType == Field.Type.INCOME) {
            mIncomeRadio.setChecked(true);
        } else {
            mExpenseRadio.setChecked(true);
        }
        
        // Set the amount.
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

}
