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
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.einzig.ipst2.database.DatabaseInterface;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalResponded;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.sort.SortHelper;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVWriter;

/*
 * Created by Steven Foskett on 7/12/2017.
 */

public class CSVExportHelper extends AsyncTask<String, String, String> {
    private Activity activity;
    private PreferencesHelper helper;
    private DatabaseInterface db;
    private String errorThatHappened = "";
    private boolean errorHappened;
    private String exportType;
    private String pathTofile;

    public CSVExportHelper(Activity activity, String exportType) {
        this.activity = activity;
        this.exportType = exportType;
        this.helper = new PreferencesHelper(this.activity);
        this.db = new DatabaseInterface(this.activity);
    }

    protected String doInBackground(String... urls) {
        DateTimeFormatter uiFormatter = helper.getUIFormatter();
        DateTimeFormatter fileFormatter = ISODateTimeFormat.basicDate();
        String date = fileFormatter.print(LocalDateTime.now());
        String fileName = "/IPST2-backup-" + date + ".csv";
        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/Download");
        File file = new File(dir, fileName);
        if (Environment.getExternalStorageDirectory() == null) {
            errorHappened = true;
            errorThatHappened = "No SD Card Found";
        } else {
            try {
                Logger.d(file.getAbsolutePath());
                    CSVWriter mWriter = new CSVWriter(new FileWriter(file));
                    Vector<? extends PortalSubmission> subList =
                            db.getAllPortals(helper.isSeerOnly());
                    SortHelper.sortList(subList, activity);

                    String[] mExportChartHeaders = {
                            "Portal Name",
                            "Date Submitted",
                            "Date Accepted",
                            "Date Rejected",
                            "Status",
                            "Live Address",
                            "Intel Link URL",
                            "Picture URL",
                            "Rejection Reason",
                            "Lat/Lon",
                            "Date Pattern"
                    };

                    mWriter.writeNext(mExportChartHeaders);

                    for (PortalSubmission submission : subList) {
                        Logger.d("PORTAL SUBMISSION ADDED TO CSV: " + submission.getName());
                        String name = submission.getName();
                        String dateSubmitted = "N/A";
                        if (submission.getDateSubmitted() != null)
                            dateSubmitted = uiFormatter.print(submission.getDateSubmitted());

                        String dateAccepted = "N/A";
                        String dateRejected = "N/A";
                        if (submission instanceof PortalAccepted ||
                                submission instanceof PortalRejected) {
                            if (((PortalResponded) submission).getDateResponded() != null) {
                                if (submission instanceof PortalAccepted)
                                    dateAccepted = uiFormatter.print(((PortalResponded) submission)
                                            .getDateResponded());
                                else
                                    dateRejected = uiFormatter.print(((PortalResponded) submission)
                                            .getDateResponded());
                            }
                        }

                        String status = "Pending";
                        if (submission instanceof PortalAccepted)
                            status = "Accepted";
                        else if (submission instanceof PortalRejected)
                            status = "Rejected";

                        String liveAddress = "N/A";
                        String intelLink = "N/A";
                        String pictureURL = "N/A";
                        String rejectionReason = "N/A";
                        String latLonString = "N/A";

                        if (submission.getPictureURL() != null)
                            pictureURL = submission.getPictureURL();

                        if (submission instanceof PortalResponded) {
                            if (submission instanceof PortalAccepted) {
                                liveAddress = ((PortalAccepted) submission).getLiveAddress();
                                intelLink = ((PortalAccepted) submission).getIntelLinkURL();
                                try {
                                    latLonString = intelLink.substring(intelLink.indexOf("=") + 1,
                                            intelLink.indexOf("&"));
                                } catch (java.lang.StringIndexOutOfBoundsException e) {
                                    latLonString = "String Index was Out of Bounds";
                                }
                            } else if (submission instanceof PortalRejected) {
                                rejectionReason = ((PortalRejected) submission)
                                        .getRejectionReason();
                            }
                        }

                        if (name != null)
                            name = name.replaceAll(",", "");

                        status = status.replaceAll(",", "");

                        if (liveAddress != null) {
                            liveAddress = liveAddress.replaceAll(",", "");
                        }

                        if (intelLink != null)
                            intelLink = intelLink.replaceAll(",", ",");

                        if (rejectionReason != null)
                            rejectionReason = rejectionReason.replaceAll(",", "");


                        String[] lineOfCSV =
                                {name, dateSubmitted, dateAccepted, dateRejected, status,
                                        liveAddress, intelLink, pictureURL, rejectionReason,
                                        latLonString, helper.getUIFormatterPattern()};

                        if (exportType.equalsIgnoreCase("all"))
                            mWriter.writeNext(lineOfCSV);
                        else if (exportType.equalsIgnoreCase("accepted"))
                            if (status.equalsIgnoreCase("Accepted"))
                                mWriter.writeNext(lineOfCSV);
                    }
                    mWriter.close();

                    pathTofile = file.getAbsolutePath();

                    //Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    //sendIntent.setType("application/csv");
                    //sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
                    //startActivity(sendIntent);
            } catch (Exception e) {
                e.printStackTrace();
                errorThatHappened = e.toString();
                errorHappened = true;
            }
            db.close();
        }

        return "";
    }

    protected void onPostExecute(String result) {
        if (!errorHappened) {
            Toast.makeText(activity, "The CSV file has been successfully exported to this location on your external storage: \n\n" +
                    pathTofile, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "CSV Export FAILED: " + errorThatHappened, Toast.LENGTH_SHORT)
                    .show();
        }
    }
}