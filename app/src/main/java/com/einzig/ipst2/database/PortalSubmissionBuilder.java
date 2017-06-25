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

import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.Logger;

import org.joda.time.LocalDate;

import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_DATE_SUBMITTED;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_NAME;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_PICTURE_URL;

/**
 * @author Ryan Porterfield
 * @since 2017-05-19
 */
public final class PortalSubmissionBuilder extends PortalBuilder<PortalSubmission> {
    /**
     *
     */
    public PortalSubmissionBuilder() {
    }

    /**
     * Create an instance of PortalSubmission from a database entry.
     *
     * @param c Cursor containing the database fields of the portal.
     * @return a PortalSubmission representation of a portal in the database.
     */
    @Override
    PortalSubmission build(Cursor c) {
        String name, pictureURL;
        LocalDate dateSubmitted;
        name = c.getString(c.getColumnIndex(COLUMN_NAME));
        dateSubmitted = parseDate(c.getString(c.getColumnIndex
                (COLUMN_DATE_SUBMITTED)));
        pictureURL = c.getString(c.getColumnIndex(COLUMN_PICTURE_URL));
        Logger.d("PSBuilder", "Name: " + name + "\tSubmitted: " + dateSubmitted + "\tP-URL: " +
                pictureURL);
        return new PortalSubmission(name, dateSubmitted, pictureURL);
    }

    @Override
    public PortalSubmission build(String name, LocalDate dateResponded, String message) {
        String pictureURL = parsePictureURL(message, name);
        return new PortalSubmission(name, dateResponded, pictureURL);
    }
}
