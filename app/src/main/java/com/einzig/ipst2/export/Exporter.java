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

package com.einzig.ipst2.export;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;

import com.einzig.ipst2.R;
import com.einzig.ipst2.database.DatabaseInterface;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.sort.SortHelper;
import com.einzig.ipst2.util.PreferencesHelper;
import com.einzig.ipst2.util.ThemeHelper;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * @author Ryan Porterfield
 * @since 2017-07-27
 */
abstract class Exporter extends AsyncTask<Void, Integer, Void> {
    private final Activity activity;
    private final DatabaseInterface db;
    private final boolean acceptedOnly;
    private final PreferencesHelper helper;
    /** Display parsing progress */
    private final ProgressDialog dialog;

    Exporter(Activity activity) {
        this.acceptedOnly = false;
        this.activity = activity;
        this.db = new DatabaseInterface(activity);
        this.dialog = new ProgressDialog(activity, ThemeHelper.getDialogTheme(activity));
        this.helper = new PreferencesHelper(activity);
        initProgressDialog();
    }

    Exporter(Activity activity, boolean acceptedOnly) {
        this.acceptedOnly = acceptedOnly;
        this.activity = activity;
        this.db = new DatabaseInterface(activity);
        this.dialog = new ProgressDialog(activity, ThemeHelper.getDialogTheme(activity));
        this.helper = new PreferencesHelper(activity);
        initProgressDialog();
    }

    Activity getActivity() {
        return activity;
    }

    DatabaseInterface getDb() {
        return db;
    }

    boolean getAcceptedOnly() {
        return acceptedOnly;
    }

    abstract String getExportFileType();

    PreferencesHelper getHelper() {
        return helper;
    }

    Vector<? extends PortalSubmission> getSortedList() {
        Vector<? extends PortalSubmission> subList = db.getAllPortals(helper.isSeerOnly());
        SortHelper.sortList(subList, activity);
        return subList;
    }

    private File getOutputFile() {
        DateTimeFormatter fileFormatter = ISODateTimeFormat.basicDate();
        String date = fileFormatter.print(LocalDateTime.now());
        String fileName = "/IPST2-backup-" + (acceptedOnly ? "Accepted" : "All");
        fileName += "-" + date + "." + getExportFileType();
        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/Download");
        return new File(dir, fileName);
    }

    public FileWriter getOutputWriter() throws IOException {
        return new FileWriter(getOutputFile());
    }

    /**
     * Initialize the progress dialog
     */
    private void initProgressDialog() {
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle(activity.getString(R.string.parsing_email));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMax((int) db.getDatabaseSize(helper.isSeerOnly()));
    }

    public void onPostExecute() {
        dialog.dismiss();
    }

    public void onPreExecute() {
        dialog.show();
    }
}
