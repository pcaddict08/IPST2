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

import com.einzig.ipst2.portal.PortalRejected;

import org.joda.time.LocalDate;

import static com.einzig.ipst2.database.DatabaseInterface.DATE_FORMATTER;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.*;
import static com.einzig.ipst2.database.RejectedPortalContract.RejectedPortalEntry.COLUMN_REJECTION_REASON;

/**
 * @author Ryan Porterfield
 * @since 2017-05-19
 */
public final class PortalRejectedBuilder extends PortalBuilder<PortalRejected> {
    /**
     *
     */
    public PortalRejectedBuilder() {
    }

    /**
     * Create an instance of PortalRejected from a database entry.
     *
     * @param c Cursor containing the database fields of the portal.
     * @return a PortalRejected representation of a portal in the database.
     */
    @Override
    PortalRejected build(Cursor c) {
        String name, pictureURL, reason;
        LocalDate submitted, responded;
        name = c.getString(c.getColumnIndex(COLUMN_NAME));
        submitted = DATE_FORMATTER.parseLocalDate(c.getString(
                c.getColumnIndex(COLUMN_DATE_SUBMITTED)));
        pictureURL = c.getString(c.getColumnIndex(COLUMN_PICTURE_URL));
        responded = DATE_FORMATTER.parseLocalDate(c.getString(c.getColumnIndex(COLUMN_DATE_RESPONDED)));
        reason = c.getString(c.getColumnIndex(COLUMN_REJECTION_REASON));
        return new PortalRejected(name, submitted, pictureURL, responded, reason);
    }

    @Override
    public PortalRejected build(String name, LocalDate dateResponded, String message) {
        String pictureURL = parsePictureURL(message, name);
        String rejectionReason = parseRejectionReason(message);
        return new PortalRejected(name, null, pictureURL, dateResponded, rejectionReason);
    }

    /**
     * Parse the reason a portal was rejected.
     *
     * @param messageString The body of the email as a String for parsing.
     * @return the reason the portal was rejected.
     */
    private String parseRejectionReason(String messageString) {
        String rejectionReason = "N/A";
        if (messageString != null) {
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
        }
        return rejectionReason;
    }
}
