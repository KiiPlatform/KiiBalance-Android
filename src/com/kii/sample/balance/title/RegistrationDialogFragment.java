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
package com.kii.sample.balance.title;

import com.kii.cloud.storage.KiiUser;
import com.kii.sample.balance.R;
import com.kii.util.ViewUtil;
import com.kii.util.dialog.ProgressDialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This dialog shows user registration form
 */
public class RegistrationDialogFragment extends DialogFragment {
    public static RegistrationDialogFragment newInstance() {
        RegistrationDialogFragment fragment = new RegistrationDialogFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View root = inflater.inflate(R.layout.dialog_login_register, null);
        
        // set labels
        TextView titleText = (TextView) root.findViewById(R.id.textView1);
        titleText.setText(R.string.register_kii_cloud);
        // set button
        Button submitButton = (Button) root.findViewById(R.id.button_submit);
        submitButton.setText(R.string.register);
        submitButton.setOnClickListener(new ClickListener(this, root));
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root);
        
        return builder.create();
    }
    
    /**
     * Show error message
     * @param message is error message
     */
    void showErrorMessage(String message) {
        Dialog dialog = getDialog();
        if (dialog == null) { return; }
        TextView text = (TextView) dialog.findViewById(R.id.text_message);
        text.setVisibility(View.VISIBLE);
        text.setText(message);
    }
    
    private static class ClickListener implements View.OnClickListener {

        private static final String MESSAGE_INVALID_USERNAME = "Invalid Username";
        private static final String MESSAGE_INVALID_PASSWORD = "Invalid Password";
        
        private RegistrationDialogFragment dialog;
        private View root;
        
        public ClickListener(RegistrationDialogFragment dialog, View root) {
            this.dialog = dialog;
            this.root = root;
        }

        /*
         * (non-Javadoc)
         * @see android.view.View.OnClickListener#onClick(android.view.View)
         */
        @Override
        public void onClick(View v) {
            // get params
            String username = ViewUtil.getValueOfEditText(root, R.id.edit_username);
            String password = ViewUtil.getValueOfEditText(root, R.id.edit_password);
            // check
            if (!KiiUser.isValidUserName(username)) {
                dialog.showErrorMessage(MESSAGE_INVALID_USERNAME);
                return;
            }
            if (!KiiUser.isValidPassword(password)) {
                dialog.showErrorMessage(MESSAGE_INVALID_PASSWORD);
                return;
            }
            
            // show progress
            ProgressDialogFragment progress = ProgressDialogFragment.newInstance(v.getContext(), R.string.register, R.string.register);
            progress.show(dialog.getFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);
            
            // call user registration API
            RegisterCallback callback = new RegisterCallback(dialog);
            KiiUser user = KiiUser.createWithUsername(username);
            user.register(callback, password);
        }
    }
}
