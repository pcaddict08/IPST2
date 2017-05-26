/*
 *  ********************************************************************************************** *
 *  * ********************************************************************************************** *
 *  *                                                                                                *
 *  * Copyright 2017 Steven Foskett, Jimmy Ho, Ryan Porterfield                                      *
 *  *                                                                                                *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software  *
 *  * and associated documentation files (the "Software"), to deal in the Software without           *
 *  * restriction, including without limitation the rights to use, copy, modify, merge, publish,     *
 *  * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the  *
 *  * Software is furnished to do so, subject to the following conditions:                           *
 *  *                                                                                                *
 *  * The above copyright notice and this permission notice shall be included in all copies or       *
 *  * substantial portions of the Software.                                                          *
 *  *                                                                                                *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING  *
 *  * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND     *
 *  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,   *
 *  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.        *
 *  *                                                                                                *
 *  * ********************************************************************************************** *
 *  * **********************************************************************************************
 */

package com.einzig.ipst2.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.einzig.ipst2.R;
import com.einzig.ipst2.activities.MainActivity;
import com.einzig.ipst2.portal.PortalSubmission;

// Custom list item class for menu items
public class ListItemAdapter_PS extends BaseAdapter implements Filterable {
    SubmissionFilter submissionFilter;

    private ArrayList<PortalSubmission> originalItems;
    private ArrayList<PortalSubmission> shownItems;
    private Context context;

    public ListItemAdapter_PS(final ArrayList<PortalSubmission> items, Context context) {
        this.context = context;
        this.originalItems = items;
        this.shownItems = new ArrayList<>(items);
    }

    public int getCount() {
        return this.originalItems.size();
    }

    public PortalSubmission getItem(int position) {
        return this.shownItems.get(position);
    }


    public long getItemId(int position) {
        return this.shownItems.get(position).hashCode();
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        final PortalSubmission item = this.shownItems.get(position);

        RelativeLayout itemLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.row_pslist, parent, false);
        ImageView iconView = (ImageView) itemLayout.findViewById(R.id.status_icon);
        if (iconView != null) {

        }

        TextView uniqueNameLabel = (TextView) itemLayout.findViewById(R.id.nameLabel);
        if (uniqueNameLabel != null)
            uniqueNameLabel.setText(item.getName());


        return itemLayout;
    }

    private class SubmissionFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d(MainActivity.TAG, "FILTERED: " + constraint);
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = new ArrayList<>(originalItems);
                results.count = originalItems.size();
            } else {
                List<PortalSubmission> nList = new ArrayList<>();
                for (PortalSubmission p : originalItems) {
                    if (p.getName().toUpperCase().contains(constraint.toString().toUpperCase()))
                        nList.add(p);
                    Log.d(MainActivity.TAG, "ADDED TO SEARCH RESULTS: " + p.getName());
                }
                results.values = nList;
                results.count = nList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                try {
                    ListItemAdapter_PS.this.shownItems = (ArrayList<PortalSubmission>) results.values;
                    System.out.println("PUBLISHED: " + ListItemAdapter_PS.this.shownItems.size());
                    notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Filter getFilter() {
        if (submissionFilter == null)
            submissionFilter = new SubmissionFilter();
        return submissionFilter;
    }

    public void resetData() {
        this.shownItems = new ArrayList<>(originalItems);
        notifyDataSetChanged();
    }
}