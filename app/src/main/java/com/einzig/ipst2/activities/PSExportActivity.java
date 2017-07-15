/******************************************************************************
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
 ******************************************************************************/

package com.einzig.ipst2.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.einzig.ipst2.R;
import com.einzig.ipst2.export.CSVExportHelper;
import com.einzig.ipst2.util.PermissionsHelper;
import com.einzig.ipst2.util.ThemeHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.einzig.ipst2.activities.MainActivity.REQUEST_CODE_ALL;

public class PSExportActivity extends AppCompatActivity {
    @BindView(R.id.exportportaldata_psexportactivity)
    Button exportportaldata_psexportactivity;
    @BindView(R.id.exportportaldataaccepted_psexportactivity)
    Button exportportaldataaccepted_psexportactivity;
    @BindView(R.id.exportprogress_psexportactivity)
    ProgressBar exportprogress_psexportactivity;

    String exportType = "all";
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psexport);
        ButterKnife.bind(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(R.string.psexportactivity_title);
        }
        ThemeHelper.initActionBar(getSupportActionBar());
        ThemeHelper.styleButton(exportportaldata_psexportactivity, this);
        ThemeHelper.styleButton(exportportaldataaccepted_psexportactivity, this);
    }

    @OnClick(R.id.exportportaldata_psexportactivity)
    public void exportData() {
        exportType = "all";
        if (PermissionsHelper.requestPermissions(this)) {
            startLoading();
        }
    }

    @OnClick(R.id.exportportaldataaccepted_psexportactivity)
    public void exportDataAccepted() {
        exportType = "accepted";
        if (PermissionsHelper.hasWritePermission(this)) {
            startLoading();
        }
    }

    public void loadingDone() {
        exportportaldata_psexportactivity.setVisibility(View.VISIBLE);
        exportportaldataaccepted_psexportactivity.setVisibility(View.VISIBLE);
        exportprogress_psexportactivity.setVisibility(View.INVISIBLE);
    }

    public void startLoading() {
        exportportaldata_psexportactivity.setVisibility(View.INVISIBLE);
        exportportaldataaccepted_psexportactivity.setVisibility(View.INVISIBLE);
        exportprogress_psexportactivity.setVisibility(View.VISIBLE);
        new CSVExportHelper(this, exportType).execute();
    }

    /*
     * Provides the results of permission requests
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ALL) {
            startLoading();
        }
    }
}
