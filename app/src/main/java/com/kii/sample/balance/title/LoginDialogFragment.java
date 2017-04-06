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
package com.kii.sample.balance.title;

import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.sample.balance.R;
import com.kii.util.ProgressDialogFragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * This dialog shows user registration form
 */
public class LoginDialogFragment extends DialogFragment {

    @BindView(R.id.text_message)
    TextView mMessageText;

    @BindView(R.id.edit_username)
    EditText mUsernameEdit;

    @BindView(R.id.edit_password)
    EditText mPasswordEdit;

    @BindView(R.id.button_submit)
    Button mSubmitButton;

    private Unbinder mButterKnifeUnbinder;

    public static LoginDialogFragment newInstance(Fragment target, int requestCode) {
        LoginDialogFragment fragment = new LoginDialogFragment();
        fragment.setTargetFragment(target, requestCode);

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_login_register, container, false);

        mButterKnifeUnbinder = ButterKnife.bind(this, root);

        // set text
        mSubmitButton.setText(R.string.login);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mButterKnifeUnbinder.unbind();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.login_kii_cloud);
        return dialog;
    }

    @OnClick(R.id.button_submit)
    void submitClicked() {
        // gets username / password
        String username = mUsernameEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();

        // check
        if (!KiiUser.isValidUserName(username)) {
            showErrorMessage(R.string.message_invalid_username);
            return;
        }
        if (!KiiUser.isValidPassword(password)) {
            showErrorMessage(R.string.message_invalid_password);
            return;
        }

        // show progress
        ProgressDialogFragment.show(getActivity(), getFragmentManager(), R.string.login);

        // call user login API
        KiiUser.logIn(new KiiUserCallBack() {
            @Override
            public void onLoginCompleted(int token, KiiUser user, Exception e) {
                ProgressDialogFragment.close(getFragmentManager());
                if (e != null) {
                    showErrorMessage(R.string.message_login_failed);
                    return;
                }

                // notify caller fragment that registration is done.
                Fragment target = getTargetFragment();
                if (target == null) {
                    dismiss();
                    return;
                }
                target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                dismiss();
            }
        }, username, password);
    }
    
    /**
     * Show error message
     * @param messageID is id of the error message
     */
    void showErrorMessage(int messageID) {
        if (mMessageText == null) { return; }

        String message = getString(messageID);
        mMessageText.setVisibility(View.VISIBLE);
        mMessageText.setText(message);
    }
}
