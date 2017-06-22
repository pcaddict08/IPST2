/******************************************************************************
 *                                                                            *
 * Copyright 2017 Steven Foskett, Jimmy Ho, Ryan Porterfield                  *
 * Permission is hereby granted, free of charge, to any person obtaining a    *
 * copy of this software and associated documentation files (the "Software"), *
 * to deal in the Software without restriction, including without limitation  *
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,   *
 * and/or sell copies of the Software, and to permit persons to whom the      *
 * Software is furnished to do so, subject to the following conditions:       *
 *                                                                            *
 * The above copyright notice and this permission notice shall be included in *
 * all copies or substantial portions of the Software.                        *
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,   *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE*
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER     *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING    *
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER        *
 * DEALINGS IN THE SOFTWARE.                                                  *
 *                                                                            *
 ******************************************************************************/

package com.einzig.ipst2.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.einzig.ipst2.R;
import com.einzig.ipst2.adapters.ListItemAdapter_PS;
import com.einzig.ipst2.portal.PortalSubmission;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.einzig.ipst2.activities.MainActivity.TAG;

public class PSListActivity extends AppCompatActivity {
    @BindView(R.id.listview_pslistactivity)
    ListView listView;
    ArrayList<PortalSubmission> psList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pslist);
        ButterKnife.bind(this);
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        psList = getIntent().getExtras().getParcelableArrayList("psList");
        if (psList != null) {
            Log.d(TAG, "PS LIST SIZE: " + psList.size());
            sortList(psList);
            listView.setAdapter(new ListItemAdapter_PS(psList, PSListActivity.this));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, "Item Selected at index: " + i);
                    try {
                        Intent intent = new Intent(PSListActivity.this, PSDetailsActivity.class);
                        intent.putExtra("ps", (Parcelable) ((ListItemAdapter_PS)listView.getAdapter()).shownItems.get(i));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pslistactivity, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search_pslistactivity).getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (TextUtils.isEmpty(newText)) {
                        ((ListItemAdapter_PS) listView.getAdapter()).resetData();
                    } else {
                        ((ListItemAdapter_PS) listView.getAdapter()).getFilter().filter(newText);
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
            });
        } else
            Log.d(TAG, "MENU ITEM ACTION VIEW FOR SEARCH IS NULL");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        } else if (id == R.id.sortlist_pslistactivity) {
            sortMenuOptionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Method to sort the list based on settings the user has saved */
    public void sortList(ArrayList<PortalSubmission> psList) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortOptionValue = sharedPreferences.getString("sort-type", "");

        if (sortOptionValue.equalsIgnoreCase("respond-date-desc")) {
            Collections.sort(psList, new ListItemAdapter_PS.SortPortalSubmissions_dateresp());
        } else if (sortOptionValue.equalsIgnoreCase("sub-date")) {
            Collections.sort(psList, new ListItemAdapter_PS.SortPortalSubmissions_datesub());
        } else if (sortOptionValue.equalsIgnoreCase("sub-date-desc")) {
            Collections.sort(psList, new ListItemAdapter_PS.SortPortalSubmissions_datesub());
            Collections.reverse(psList);
        } else if (sortOptionValue.equalsIgnoreCase("alph")) {
            Collections.sort(psList, new ListItemAdapter_PS.SortPortalSubmissions_alph());
        } else if (sortOptionValue.equalsIgnoreCase("alph-desc")) {
            Collections.sort(psList, new ListItemAdapter_PS.SortPortalSubmissions_alph());
            Collections.reverse(psList);
        } else {
            Collections.sort(psList, new ListItemAdapter_PS.SortPortalSubmissions_dateresp());
            Collections.reverse(psList);
        }
    }

    /*
    * Sort Option menu option clicked method
    * */
    public void sortMenuOptionSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Sort Criteria")
                .setItems(R.array.sortTypesValues, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String[] some_array = getResources().getStringArray(R.array.sortTypes);
                        System.out.println("SELECTED: " + some_array[which]);
                        SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(PSListActivity.this)
                                        .edit();
                        editor.putString("sort-type", some_array[which].toLowerCase());
                        editor.apply();
                        Intent i = getIntent();
                        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        i.putExtra("psList", PSListActivity.this.psList);
                        startActivity(i);
                        finish();
                        overridePendingTransition(0, 0);
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
