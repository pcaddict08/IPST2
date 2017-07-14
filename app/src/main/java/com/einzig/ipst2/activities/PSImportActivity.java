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

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

import com.einzig.ipst2.R;
import com.einzig.ipst2.database.DatabaseInterface;
import com.einzig.ipst2.database.PortalBuilder;
import com.einzig.ipst2.util.Logger;
import com.einzig.ipst2.util.ThemeHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import butterknife.ButterKnife;

public class PSImportActivity extends AppCompatActivity {
    static final private int FILE_SELECT_CODE = 3;
    final private ProgressDialog dialog;

    public PSImportActivity() {
        dialog = new ProgressDialog(this, ThemeHelper.getDialogTheme(this));
    }

    private void getFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
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

    private void importFromCSV(File importFile) {
        try {
            InputStreamReader stream = new InputStreamReader(new FileInputStream(importFile));
            BufferedReader reader = new BufferedReader(stream);
            DatabaseInterface db = new DatabaseInterface(this);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] tokens = line.split(",");
                db.add(PortalBuilder.buildFromCSV(tokens));
            }
            reader.close();
        } catch (IOException e) {
            Logger.e("importFromCSV", e.toString());
        }
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

    @Override
    protected void onActivityResult(int requestCode, int result, Intent data) {
        if (requestCode != FILE_SELECT_CODE || result != RESULT_OK)
            return;
        Uri uri = data.getData();
        String path = getPath(uri);
        File importFile = new File(path);
        initProgressDialog(getPortalCount(importFile));
        importFromCSV(importFile);
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
        setContentView(R.layout.activity_psimport);
        ButterKnife.bind(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(R.string.psimportactivity_title);
        }
        ThemeHelper.initActionBar(getSupportActionBar());
    }
}
