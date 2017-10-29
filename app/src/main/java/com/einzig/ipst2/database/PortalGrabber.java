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

import java.util.List;

/** TODO: Remove this
 * @author Steven Foskett
 * @since 2017-06-29
 */
public class PortalGrabber extends AsyncTask<Void, Void, List<? extends PortalSubmission>> {
    private Activity fromAct;
    private String RANGE;
    private String TYPE;
    private DatabaseHelper db;

    public PortalGrabber(Activity fromAct, String RANGE, String TYPE, DatabaseHelper db) {
        this.fromAct = fromAct;
        this.RANGE = RANGE;
        this.TYPE = TYPE;
        this.db = db;
    }

    @Override
    protected void onPostExecute(List<? extends PortalSubmission> result) {
        if (this.fromAct instanceof PSListActivity) {
            ((PSListActivity) this.fromAct).afterParse(result);
        }
    }

    @Override
    protected List<? extends PortalSubmission> doInBackground(Void... voids) {
        List<? extends PortalSubmission> psList = null;
        Logger.d("RANGE: " + RANGE);
        Logger.d("TYPE: " + TYPE);
        LocalDate viewDate = null;
        PreferencesHelper helper = new PreferencesHelper(fromAct);
        try {
            if (RANGE != null && !RANGE.equalsIgnoreCase(""))
                viewDate = new LocalDate(RANGE);
            if (TYPE != null)
                switch (TYPE) {
                case "all":
                    if (viewDate == null)
                        psList = db.getAllPortals();
                    else
                        psList = db.getAllPortals(viewDate, LocalDate.now());
                    break;
                case "accepted":
                    if (viewDate == null)
                        psList = db.getAllAccepted();
                    else
                        psList = db.getAcceptedPortals(viewDate, LocalDate.now());
                    break;
                case "pending":
                    if (viewDate == null)
                        psList = db.getAllPending();
                    else
                        psList = db.getPendingPortals(viewDate, LocalDate.now());
                    break;
                case "rejected":
                    if (viewDate == null)
                        psList = db.getAllRejected();
                    else
                        psList = db.getRejectedPortals(viewDate, LocalDate.now());
                    break;
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return psList;
    }
}
