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

package com.einzig.ipst2.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.einzig.ipst2.activities.PSImportActivity;
import com.einzig.ipst2.database.DatabaseInterface;
import com.einzig.ipst2.database.PortalBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * Created by Steven Foskett on 7/14/2017.
 */

public class CSVImportHelper extends AsyncTask<Void, Integer, Void> {
    private Activity activity;
    private File importFile;
    private ProgressDialog dialog;
    public CSVImportHelper(Activity activity, File file, ProgressDialog dialog) {
        this.activity = activity;
        this.importFile = file;
        this.dialog = dialog;
    }

    protected Void doInBackground(Void... urls) {
        try {
            InputStreamReader stream = new InputStreamReader(new FileInputStream(importFile));
            BufferedReader reader = new BufferedReader(stream);
            DatabaseInterface db = new DatabaseInterface(activity);
            PreferencesHelper helper = new PreferencesHelper(activity);
            helper.set(helper.parseDateKey(), helper.nullKey());
            db.deleteAll();

            double lengthPerPercent = 100.0 / importFile.length();
            long readLength = 0;

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] tokens = line.split(",");
                readLength += line.length();
                db.add(PortalBuilder.buildFromCSV(tokens));
                Logger.d("Publishing Progress: " + lengthPerPercent + " - " + readLength + " -- "
                        + (int) Math.round(lengthPerPercent * readLength));
                publishProgress((int) Math.round(lengthPerPercent * readLength));
            }
            reader.close();
        } catch (IOException e) {
            Logger.e("importFromCSV", e.toString());
        }
        return null;
    }
    @Override
    protected void onProgressUpdate(Integer... progress) {
        dialog.setProgress(progress[0] + 1);
        Logger.v("Parsing CSV: " + dialog.getProgress() + " / " + dialog.getMax());
    }

    @Override
    protected void onPostExecute(Void result) {
        if (activity instanceof PSImportActivity)
            ((PSImportActivity) activity).finishedParsing();
    }
}