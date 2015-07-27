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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kii.cloud.storage.KiiObject;
import com.kii.sample.balance.R;
import com.kii.sample.balance.kiiobject.Field;

/**
 * List adapter for showing KiiObject
 */
public class KiiObjectAdapter extends BaseAdapter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final NumberFormat AMOUNT_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    
    private int totalAmount;
    private List<KiiObject> items = new ArrayList<KiiObject>();
    
    /**
     * Add an item to the head of list
     * @param item
     */
    public void addFirst(KiiObject item) {
        items.add(0, item);
        addTotalAmount(item);
    }
    
    /**
     * Add an item to the last of list
     * @param item
     */
    public void add(KiiObject item) {
        items.add(item);
        addTotalAmount(item);
    }
    
    /**
     * Update object with ObjectID
     * @param object
     * @param objectId
     */
    public void updateObject(KiiObject object, String objectId) {
        // delete and get position
        int position = delete(objectId);
        if (position == -1) { return; } // not found
        items.add(position, object);
    }
    
    /**
     * Delete object with ObjectID
     * @param objectId
     * @return position / -1 if item is not found
     */
    public int delete(String objectId) {
        // get position
        int position = -1;
        for (int i = 0 ; i < items.size() ; ++i) {
            if (items.get(i).toUri().toString().equals(objectId)) {
                position = i;
                break;
            }
        }
        if (position == -1) { return -1; } // not found
        items.remove(position);
        return position;
    }
    
    /**
     * Add the value of this item to total amount
     * @param item
     */
    private void addTotalAmount(KiiObject item) {
        int amount = item.getInt(Field.AMOUNT);
        int type = item.getInt(Field.TYPE);
        if (type == Field.Type.INCOME) {
            totalAmount += amount;
        } else {
            totalAmount -= amount;
        }
    }
    
    /**
     * Remove all items from list
     */
    public void clear() {
        items.clear();
    }
    
    /**
     * Compute total amount
     */
    public void computeTotalAmount() {
        totalAmount = 0;
        for (KiiObject item : items) {
            addTotalAmount(item);
        }
    }
    /**
     * @return total amount
     */
    public int getTotalAmount() {
        return totalAmount;
    }
    
    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return items.size();
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            
            View layout = inflater.inflate(R.layout.list_item, null);

            // set textViews to ViewHolder
            TextView nameText = (TextView) layout.findViewById(R.id.text_name);
            TextView amountText = (TextView) layout.findViewById(R.id.text_amount);
            TextView dateText = (TextView) layout.findViewById(R.id.text_date);
            layout.setTag(new ViewHolder(nameText, amountText, dateText));
            
            convertView = layout;
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        KiiObject object = items.get(position);
        
        TextView nameText = holder.nameText;
        nameText.setText(object.getString(Field.NAME));
        
        TextView amountText = holder.amountText;
        int type = object.getInt(Field.TYPE);
        if (type == Field.Type.INCOME) {
            amountText.setText(AMOUNT_FORMAT.format(object.getInt(Field.AMOUNT) / 100.0));
            amountText.setTextColor(Color.BLACK);
        } else {
            amountText.setText(AMOUNT_FORMAT.format(-object.getInt(Field.AMOUNT) / 100.0));
            amountText.setTextColor(Color.RED);
        }
        
        TextView dateText = holder.dateText;
        dateText.setText(DATE_FORMAT.format(new Date(object.getCreatedTime())));

        return convertView;
    }
    
    private static class ViewHolder {
        public TextView nameText;
        public TextView amountText;
        public TextView dateText;
        public ViewHolder(TextView nameText, TextView amountText, TextView dateText) {
            this.nameText = nameText;
            this.amountText = amountText;
            this.dateText = dateText;
        }
    }

}
