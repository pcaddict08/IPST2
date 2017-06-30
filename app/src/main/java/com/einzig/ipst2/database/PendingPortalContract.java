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

import android.provider.BaseColumns;

/**
 * @author Ryan Porterfield
 * @since 2017-06-24
 */

public class PendingPortalContract {
    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PendingPortalEntry.TABLE_PENDING + " (" +
                    PendingPortalEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    PendingPortalEntry.COLUMN_DATE_SUBMITTED + " DATETIME NOT NULL, " +
                    PendingPortalEntry.COLUMN_PICTURE_URL + " TEXT)";

    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PendingPortalEntry.TABLE_PENDING;

    private PendingPortalContract() {
    }

    public static class PendingPortalEntry implements BaseColumns {
        /** Table key for the date Niantic approved or denied the portal */
        static final String COLUMN_DATE_RESPONDED = "dateResponded";
        /** Table key for the date the portal was submitted */
        static final String COLUMN_DATE_SUBMITTED = "dateSubmitted";
        /** Table key for portal name */
        static final String COLUMN_NAME = "name";
        /** Table key for the URL to the submission picture */
        public static final String COLUMN_PICTURE_URL = "pictureURL";
        /** The name of the table in containing pending portal submissions */
        static final String TABLE_PENDING = "pendingSubmissions";
    }
}
