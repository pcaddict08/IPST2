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

import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry;

/**
 * @author Ryan Porterfield
 * @since 2017-06-24
 */
public class AcceptedPortalContract {
    static final String SQL_CREATE_ENTRIES = String.format(
            "CREATE TABLE %s (%s TEXT NOT NULL, %s TEXT, %s DATETIME NOT NULL, %s TEXT, %s TEXT, " +
                    "FOREIGN KEY (%s, %s) REFERENCES %s(%s, %s), PRIMARY KEY (%s, %s))",
            AcceptedPortalEntry.TABLE_ACCEPTED,
            PendingPortalEntry.COLUMN_NAME,
            PendingPortalEntry.COLUMN_PICTURE_URL,
            PendingPortalEntry.COLUMN_DATE_RESPONDED,
            AcceptedPortalEntry.COLUMN_LIVE_ADDRESS,
            AcceptedPortalEntry.COLUMN_INTEL_LINK_URL,
            // Foreign Key
            PendingPortalEntry.COLUMN_PICTURE_URL,
            PendingPortalEntry.COLUMN_NAME,
            PendingPortalEntry.TABLE_PENDING,
            PendingPortalEntry.COLUMN_PICTURE_URL,
            PendingPortalEntry.COLUMN_NAME,
            // Primary Key
            PendingPortalEntry.COLUMN_PICTURE_URL,
            PendingPortalEntry.COLUMN_NAME);

    static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + AcceptedPortalEntry.TABLE_ACCEPTED;

    private AcceptedPortalContract() {
    }

    static class AcceptedPortalEntry implements BaseColumns {
        /**
         * Table key for the link to the portal on the intel map
         */
        static final String COLUMN_INTEL_LINK_URL = "intelLinkURL";
        /**
         * Table key for address of the portal
         */
        static final String COLUMN_LIVE_ADDRESS = "liveAddress";
        /**
         * The name of the table in containing accepted portal submissions
         */
        static final String TABLE_ACCEPTED = "acceptedSubmissions";
    }
}
