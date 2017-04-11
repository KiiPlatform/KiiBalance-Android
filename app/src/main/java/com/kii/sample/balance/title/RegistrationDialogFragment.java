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

import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.sample.balance.R;
import com.kii.util.ProgressDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * This dialog shows the user registration form.
 */
public class RegistrationDialogFragment extends DialogFragment {
    private static final String TAG = "RegistrationDialog";

    private static final String MESSAGE_INVALID_USERNAME = "Invalid Username";
    private static final String MESSAGE_INVALID_PASSWORD = "Invalid Password";
    private static final String MESSAGE_REGISTRATION_FAILED = "Registration is failed.";

    @BindView(R.id.text_message)
    TextView mMessageText;

    @BindView(R.id.edit_username)
    EditText mUsernameEdit;

    @BindView(R.id.edit_password)
    EditText mPasswordEdit;

    @BindView(R.id.button_submit)
    Button mSubmitButton;

    private Unbinder mButterKnifeUnbinder;

    public static RegistrationDialogFragment newInstance(Fragment target, int requestCode) {
        RegistrationDialogFragment fragment = new RegistrationDialogFragment();
        fragment.setTargetFragment(target, requestCode);

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_login_register, container, false);

        mButterKnifeUnbinder = ButterKnife.bind(this, root);

        // Set the button text.
        mSubmitButton.setText(R.string.register);

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
        dialog.setTitle(R.string.register_kii_cloud);
        return dialog;
    }

    @OnClick(R.id.button_submit)
    void submitClicked() {
        // Get a username and password.
        String username = mUsernameEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();

        // Check if the username and password are valid.
        if (!KiiUser.isValidUserName(username)) {
            showErrorMessage(MESSAGE_INVALID_USERNAME);
            return;
        }
        if (!KiiUser.isValidPassword(password)) {
            showErrorMessage(MESSAGE_INVALID_PASSWORD);
            return;
        }

        // Show the progress.
        ProgressDialogFragment.show(getActivity(), getFragmentManager(), R.string.register);

        // Register the user.
        KiiUser user = KiiUser.builderWithName(username).build();
        user.register(new KiiUserCallBack() {
            @Override
            public void onRegisterCompleted(int token, KiiUser user, Exception e) {
                ProgressDialogFragment.close(getFragmentManager());

                if (e != null) {
                    showErrorMessage(MESSAGE_REGISTRATION_FAILED);
                    return;
                }

                // Notify the caller fragment that the user has been registered.
                Fragment target = getTargetFragment();
                if (target == null) {
                    dismiss();
                    return;
                }
                target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                dismiss();
            }
        }, password);
    }
    
    /**
     * Show an error message.
     * @param message is the error message.
     */
    private void showErrorMessage(String message) {
        if (mMessageText == null) { return; }

        mMessageText.setVisibility(View.VISIBLE);
        mMessageText.setText(message);
    }
}
