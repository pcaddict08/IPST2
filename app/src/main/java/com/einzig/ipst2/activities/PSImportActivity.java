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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.einzig.ipst2.R;
import com.einzig.ipst2.util.CSVImportHelper;
import com.einzig.ipst2.util.Logger;
import com.einzig.ipst2.util.ThemeHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PSImportActivity extends AppCompatActivity {
    static final private int FILE_SELECT_CODE = 3;
    private ProgressDialog dialog;
    @BindView(R.id.importfromcsv_psimportactivity)
    Button importfromcsv_psimportactivity;

    @OnClick(R.id.importfromcsv_psimportactivity)
    void getFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/csv");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.chooseImport)), 3);
    }

    private String getPath(Uri uri) {
        String path = "";
        if (uri.getScheme().equalsIgnoreCase("content")) {
            String[] projection = { "_data" };
            Cursor cursor;
            try {
                cursor = getContentResolver().query(uri, projection, null, null, null);
                int column = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    path = cursor.getString(column);
                }
                cursor.close();
            } catch (Exception e) {
                Logger.e("ImportActivity", e.toString());
            }
        } else if (uri.getScheme().equalsIgnoreCase("file")) {
            path = uri.getPath();
        }
        return path;
    }

    private int getPortalCount(File importFile) {
        int lineCount = 0;
        try {
            InputStreamReader stream = new InputStreamReader(new FileInputStream(importFile));
            BufferedReader reader = new BufferedReader(stream);
            for (String line = reader.readLine(); line != null; line = reader.readLine())
                ++lineCount;
            reader.close();
        } catch (IOException e) {
            Logger.e("getPortalCount", e.toString());
        }
        return lineCount;
    }

    /**
     * Initialize the progress dialog
     */
    private void initProgressDialog(int maxProgress) {
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle(getString(R.string.importingCSV));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMax(maxProgress);
    }

    public void finishedParsing()
    {
        dialog.dismiss();
        Toast.makeText(this, "Finished Importing. Refreshing now.", Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_OK, getIntent());
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int result, Intent data) {
        if (requestCode != FILE_SELECT_CODE || result != RESULT_OK)
            return;
        Uri uri = data.getData();
        String path = getPath(uri);
        File importFile = new File(path);
        initProgressDialog(getPortalCount(importFile));
        dialog.show();
        new CSVImportHelper(this, importFile, dialog).execute();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        dialog = new ProgressDialog(this, ThemeHelper.getDialogTheme(this));
        setContentView(R.layout.activity_psimport);
        ButterKnife.bind(this);
        ThemeHelper.styleButton(importfromcsv_psimportactivity, this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(R.string.psimportactivity_title);
        }
        ThemeHelper.initActionBar(getSupportActionBar());
    }
}
