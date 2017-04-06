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
package com.kii.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class ProgressDialogFragment extends DialogFragment {
    private static final String FRAGMENT_TAG = "Progress";
    private static final String ARGS_TITLE = "Title";
    private static final String ARGS_MESSAGE = "Message";

    public static ProgressDialogFragment show(Context context, FragmentManager manager, int titleId) {
        ProgressDialogFragment fragment = newInstance(context.getString(titleId), context.getString(titleId));
        fragment.show(manager, ProgressDialogFragment.FRAGMENT_TAG);
        return fragment;
    }

    public static void close(FragmentManager manager) {
        if (manager == null) {
            return;
        }
        DialogFragment dialog = (DialogFragment) manager.findFragmentByTag(ProgressDialogFragment.FRAGMENT_TAG);
        if (dialog == null) {
            return;
        }
        dialog.dismiss();
    }

    public static ProgressDialogFragment newInstance(String title, String message) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();

        Bundle args = new Bundle();
        args.putString(ARGS_TITLE, title);
        args.putString(ARGS_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // get args
        Bundle args = getArguments();
        String title = args.getString(ARGS_TITLE);
        String msg = args.getString(ARGS_MESSAGE);
        
        ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(title);
        progress.setMessage(msg);
        
        return progress;
    }   
}
