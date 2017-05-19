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

package com.einzig.ipst2.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.einzig.ipst2.portal.PortalAccepted;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Ryan Porterfield
 * @since 2017-05-19
 */

final class PortalAcceptedBuilder extends PortalBuilder<PortalAccepted> {

    /**
     * @param dateFormatter date format that MySQL uses to store DATETIME objects
     * @param db reference to a SQLite database to run queries on
     */
    PortalAcceptedBuilder(SimpleDateFormat dateFormatter, SQLiteDatabase db) {
        super(dateFormatter, db, DatabaseInterface.TABLE_ACCEPTED);
    }

    /**
     * Create an instance of PortalAccepted from a database entry.
     * @param cursor Cursor containing the database fields of the portal.
     * @return a PortalAccepted representation of a portal in the database.
     */
    @Override
    PortalAccepted createPortal(Cursor cursor) {
        String name, pictureURL, location, intelLink;
        Date submitted, responded;
        name = cursor.getString(0);
        submitted = parseDate(cursor.getString(1));
        pictureURL = cursor.getString(2);
        responded = parseDate(cursor.getString(3));
        location = cursor.getString(4);
        intelLink = cursor.getString(5);
        return new PortalAccepted(name, submitted, pictureURL, responded, location, intelLink);
    }
}
