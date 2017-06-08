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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;

import java.util.Date;

/**
 * @author Ryan Porterfield
 * @since 2017-05-19
 */
public final class PortalRejectedBuilder extends PortalBuilder<PortalRejected> {
    /**
     * @param db reference to a SQLite database to run queries on
     */
    public PortalRejectedBuilder(SQLiteDatabase db) {
        super(db, DatabaseInterface.TABLE_REJECTED);
    }

    /**
     * Create an instance of PortalRejected from a database entry.
     *
     * @param cursor Cursor containing the database fields of the portal.
     * @return a PortalRejected representation of a portal in the database.
     */
    @Override
    PortalRejected build(Cursor cursor) {
        String name, pictureURL, reason;
        Date submitted, responded;
        name = cursor.getString(0);
        submitted = parseDate(cursor.getString(1));
        pictureURL = cursor.getString(2);
        responded = parseDate(cursor.getString(3));
        reason = cursor.getString(4);
        return new PortalRejected(name, submitted, pictureURL, responded, reason);
    }

    @Override
    public PortalRejected build(String name, Date dateResponded, String message) {
        String pictureURL = parsePictureURL(message);
        Date dateSubmitted = dateResponded;
        PortalSubmission submission = findPortal(pictureURL);
        if (submission != null)
            dateSubmitted = submission.getDateSubmitted();
        String rejectionReason = parseRejectionReason(message);
        return new PortalRejected(name, dateSubmitted, pictureURL, dateResponded, rejectionReason);
    }

    /**
     * Parse the reason a portal was rejected.
     *
     * @param messageString The body of the email as a String for parsing.
     * @return the reason the portal was rejected.
     */
    private String parseRejectionReason(String messageString) {
        String rejectionReason = "N/A";
        if (messageString.contains("does not meet the criteria"))
            rejectionReason = "Does not meet portal criteria";
        if (messageString.contains("duplicate"))
            rejectionReason = "Duplicate of another portal";
        if (messageString.contains("too close")) {
            if (rejectionReason.equalsIgnoreCase("N/A"))
                rejectionReason = "Too Close to another portal";
            else
                rejectionReason = rejectionReason + " or too close to another portal";
        }
        return rejectionReason;
    }
}
