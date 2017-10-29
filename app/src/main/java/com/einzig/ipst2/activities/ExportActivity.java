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
import android.widget.Button;

import com.einzig.ipst2.R;
import com.einzig.ipst2.export.CSVExportHelper;
import com.einzig.ipst2.export.JSONExporter;
import com.einzig.ipst2.export.XMLExporter;
import com.einzig.ipst2.util.PermissionsHelper;
import com.einzig.ipst2.util.ThemeHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.einzig.ipst2.activities.MainActivity.REQUEST_CODE_WRITE_EXTERNAL;

/**
 * @author Ryan Porterfield
 * @since 2017-07-28.
 */
public class ExportActivity extends AppCompatActivity {
    @BindView(R.id.exportAllCSV)
    Button exportAllCSV;
    @BindView(R.id.exportAcceptedCSV)
    Button exportAcceptedCSV;
    @BindView(R.id.exportAllJSON)
    Button exportAllJSON;
    @BindView(R.id.exportAcceptedJSON)
    Button exportAcceptedJSON;
    @BindView(R.id.exportAllXML)
    Button exportAllXML;
    @BindView(R.id.exportAcceptedXML)
    Button exportAcceptedXML;

    private void disableButtons() {
        exportAcceptedCSV.setEnabled(false);
        exportAllCSV.setEnabled(false);
        exportAcceptedJSON.setEnabled(false);
        exportAllJSON.setEnabled(false);
        exportAcceptedXML.setEnabled(false);
        exportAllXML.setEnabled(false);
    }

    private void enableButtons() {
        exportAcceptedCSV.setEnabled(true);
        exportAllCSV.setEnabled(true);
        exportAcceptedJSON.setEnabled(true);
        exportAllJSON.setEnabled(true);
        exportAcceptedXML.setEnabled(true);
        exportAllXML.setEnabled(true);
    }

    @OnClick(R.id.exportAcceptedCSV)
    public void exportAcceptedCSV() {
        new CSVExportHelper(this, "accepted").execute();
    }

    @OnClick(R.id.exportAcceptedJSON)
    public void exportAcceptedJSON() {
        new JSONExporter(this, true).execute();
    }

    @OnClick(R.id.exportAcceptedXML)
    public void exportAcceptedXML() {
        new XMLExporter(this, true).execute();
    }

    @OnClick(R.id.exportAllCSV)
    public void exportAllCSV() {
        new CSVExportHelper(this, "all").execute();
    }

    @OnClick(R.id.exportAllJSON)
    public void exportAllJSON() {
        new JSONExporter(this, false).execute();
    }

    @OnClick(R.id.exportAllXML)
    public void exportAllXML() {
        new XMLExporter(this, false).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setSettingsTheme(this);
        setContentView(R.layout.activity_export);
        ButterKnife.bind(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(R.string.psexportactivity_title);
        }
        ThemeHelper.initActionBar(supportActionBar);
        if (!PermissionsHelper.requestWritePermission(this))
            disableButtons();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Provides the results of permission requests
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL) {
            enableButtons();
        }
    }
}
