/* ********************************************************************************************** *
 * ********************************************************************************************** *
 *                                                                                                *
 * Copyright 2017 Steven Foskett, Jimmy Ho, Ryan Porterfield                                      *
 *                                                                                                *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software  *
 * and associated documentation files (the "Software"), to deal in the Software without           *
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,     *
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the  *
 * Software is furnished to do so, subject to the following conditions:                           *
 *                                                                                                *
 * The above copyright notice and this permission notice shall be included in all copies or       *
 * substantial portions of the Software.                                                          *
 *                                                                                                *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING  *
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND     *
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,   *
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.        *
 *                                                                                                *
 * ********************************************************************************************** *
 * ********************************************************************************************** */

package com.einzig.ipst2.Utilities;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.einzig.ipst2.R;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;

import java.util.List;

/**
 * @author Ryan Porterfield
 * @since 2017-05-18
 */
public class UpdateDatabaseTask extends AsyncTask<Long, Long, Long> {
    /**
     * The tag used when logging for the class
     */
    private static String TAG = "IPST:UpdateDatabase";

    /**
     * All accepted portals since the last time the database was updated.
     */
    private final List<PortalAccepted> acceptedPortals;
    /**
     * All rejected portals since the last time the database was updated.
     */
    private final List<PortalRejected> rejectedPortals;
    /**
     * All pending portal submissions since the last time the database was updated.
     */
    private final List<PortalSubmission> pendingPortals;

    /**
     * The number of portals we've updated so far.
     */
    private long currentPortal;
    /**
     * Interface that communicates with the database.
     */
    private DatabaseInterface db;
    /**
     * Total number of portals we have to update in the database
     */
    private long totalPortals;

    /* Context of the task for UI functions*/
    private Context context;

    /* Boolean to determine whether you should update main activity UI*/
    private boolean fromMainActivity;

    /**
     * Update the database with new portal information.
     *
     * @param context         The context that this task is being started from.
     * @param acceptedPortals A list of all portals which have been accepted since the last update.
     * @param rejectedPortals A list of all portals which have been rejected since the last update.
     * @param pendingPortals  A list of all portals which have been submitted since the last update.
     * @param fromMainActivity  Boolean to determine if UI needs to be updated on finish.
     */
    public UpdateDatabaseTask(Context context,
                              List<PortalAccepted> acceptedPortals,
                              List<PortalSubmission> pendingPortals,
                              List<PortalRejected> rejectedPortals,
                              boolean fromMainActivity) {
        this.fromMainActivity = fromMainActivity;
        this.context = context;
        this.acceptedPortals = acceptedPortals;
        currentPortal = 0;
        db = new DatabaseInterface(context);
        this.pendingPortals = pendingPortals;
        this.rejectedPortals = rejectedPortals;
        totalPortals = db.getDatabaseSize();
    }

    @Override
    protected Long doInBackground(Long... params) {
        updateAcceptedDatabase();
        updatePendingDatabase();
        updateRejectedDatabase();
        return totalPortals;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        Log.d(TAG, "Updating " + progress[0] + " / " + progress[1]);
    }

    @Override
    protected void onPostExecute(Long result) {
        try {
            if (fromMainActivity == true && context != null) {
                Activity mainActivity = (Activity) context;
                mainActivity.findViewById(R.id.progress_view_mainactivity).setVisibility(View.INVISIBLE);
                mainActivity.findViewById(R.id.mainui_mainactivity).setVisibility(View.VISIBLE);
                //show base UI
                //set ui numbers
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Finished Updating Database");
    }

    private void publishProgress() {
        super.publishProgress(++currentPortal, totalPortals);
    }

    /**
     * Update the acceptedSubmissions table in the database from recently parsed emails.
     */
    private void updateAcceptedDatabase() {
        Log.i(TAG, "Updating acceptedPortals database");
        synchronized (acceptedPortals) {
            for (PortalAccepted accepted : acceptedPortals) {
                if (db.containsPending(accepted.getPictureURL())) {
                    PortalSubmission pending = db.getPendingPortal(accepted.getPictureURL());
                    accepted.setDateSubmitted(pending.getDateSubmitted());
                    db.deletePending(pending);
                }
                if (!db.containsAccepted(accepted.getPictureURL())) {
                    db.addPortalAccepted(accepted);
                }
                publishProgress();
            }
        }
        db.close();
    }

    /**
     * Update the pendingSubmissions table in the database from recently parsed emails.
     */
    private void updatePendingDatabase() {
        Log.i(TAG, "Updating pending database");
        synchronized (pendingPortals) {
            for (PortalSubmission pending : pendingPortals) {
                if (!db.containsPending(pending.getPictureURL()))
                    db.addPortalSubmission(pending);
                publishProgress();
            }
        }
        db.close();
    }

    /**
     * Update the rejectedSubmissions table in the database from recently parsed emails.
     */
    private void updateRejectedDatabase() {
        Log.i(TAG, "Updating rejectedPortals database");
        synchronized (rejectedPortals) {
            for (PortalRejected rejected : rejectedPortals) {
                if (db.containsPending(rejected.getPictureURL())) {
                    PortalSubmission pending = db.getPendingPortal(rejected.getPictureURL());
                    rejected.setDateSubmitted(pending.getDateSubmitted());
                    db.deletePending(pending);
                }
                if (!db.containsRejected(rejected.getPictureURL())) {
                    db.addPortalRejected(rejected);
                }
                publishProgress();
            }
        }
        db.close();
    }
}
