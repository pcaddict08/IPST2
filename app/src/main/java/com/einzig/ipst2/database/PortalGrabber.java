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

package com.einzig.ipst2.database;

import android.app.Activity;
import android.os.AsyncTask;

import com.einzig.ipst2.activities.PSListActivity;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.Logger;
import com.einzig.ipst2.util.PreferencesHelper;

import org.joda.time.LocalDate;

import java.util.Vector;

/*
 * Created by Steven Foskett on 6/29/2017.
 */

public class PortalGrabber extends AsyncTask<Void, Void, Vector<? extends PortalSubmission>> {
    private Activity fromAct;
    private String RANGE;
    private String TYPE;
    private DatabaseInterface db;

    public PortalGrabber(Activity fromAct, String RANGE, String TYPE, DatabaseInterface db) {
        this.fromAct = fromAct;
        this.RANGE = RANGE;
        this.TYPE = TYPE;
        this.db = db;
    }

    @Override
    protected void onPostExecute(Vector<? extends PortalSubmission> result) {
        if (this.fromAct instanceof PSListActivity) {
            ((PSListActivity) this.fromAct).AfterParse(result);
        }
    }

    @Override
    protected Vector<? extends PortalSubmission> doInBackground(Void... voids) {
        Vector<? extends PortalSubmission> psList = null;
        Logger.d("RANGE: " + RANGE);
        Logger.d("TYPE: " + TYPE);
        LocalDate viewDate = null;
        PreferencesHelper helper = new PreferencesHelper(fromAct);
        try {
            if (RANGE != null)
                if (!RANGE.equalsIgnoreCase(""))
                    viewDate = new LocalDate(RANGE);
            if (TYPE != null)
                switch (TYPE) {
                case "all":
                    if (viewDate == null)
                        psList = db.getAllPortals(helper.isSeerOnly());
                    else
                        psList = db.getAllPortalsFromDate(viewDate, helper.isSeerOnly());
                    break;
                case "accepted":
                    if (viewDate == null)
                        psList = db.getAllAccepted(helper.isSeerOnly());
                    else
                        psList = db.getAcceptedByResponseDate(viewDate, helper.isSeerOnly());
                    break;
                case "pending":
                    if (viewDate == null)
                        psList = db.getAllPending(helper.isSeerOnly());
                    else
                        psList = db.getPendingByDate(viewDate, helper.isSeerOnly());
                    break;
                case "rejected":
                    if (viewDate == null)
                        psList = db.getAllRejected(helper.isSeerOnly());
                    else
                        psList = db.getRejectedByResponseDate(viewDate, helper.isSeerOnly());
                    break;
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return psList;
    }
}
