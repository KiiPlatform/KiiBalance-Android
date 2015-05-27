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
package com.kii.util.listener;

import java.lang.ref.WeakReference;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;

abstract public class BaseClickListener<T extends Fragment> implements OnClickListener {

    private WeakReference<T> fragmentRef;
    public BaseClickListener(T fragment) {
        this.fragmentRef = new WeakReference<T>(fragment);
    }
    abstract protected void onClick(View v, T fragment);
    @Override
    public void onClick(View v) {
        T fragment = fragmentRef.get();
        if (fragment == null) { return; }
        onClick(v, fragment);
    }
    
    

}
