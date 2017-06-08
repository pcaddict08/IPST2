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

import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalSubmission;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ryan Porterfield
 * @since 2017-05-19
 */

public final class PortalAcceptedBuilder extends PortalBuilder<PortalAccepted> {
    /**
     * @param db reference to a SQLite database to run queries on
     */
    public PortalAcceptedBuilder(SQLiteDatabase db) {
        super(db, DatabaseInterface.TABLE_ACCEPTED);
    }

    @Override
    public PortalAccepted build(String name, Date dateResponded, String message) {
        String pictureURL = parsePictureURL(message);
        Date dateSubmitted = dateResponded;
        PortalSubmission submission = findPortal(pictureURL);
        if (submission != null)
            dateSubmitted = submission.getDateSubmitted();
        String address = parseLiveAddress(message);
        String intelLink = parseIntelLink(message);
        return new PortalAccepted(name, dateSubmitted, pictureURL, dateResponded, address, intelLink);
    }

    /**
     * Create an instance of PortalAccepted from a database entry.
     * @param cursor Cursor containing the database fields of the portal.
     * @return a PortalAccepted representation of a portal in the database.
     */
    @Override
    PortalAccepted build(Cursor cursor) {
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

    /**
     * Parse the intel link for an accepted portal from the email.
     *
     * @param messageString The body of the email as a String for parsing.
     * @return Ingress Intel map link for the portal.
     */
    private String parseIntelLink(String messageString) {
        String intelLinkURL;
        Pattern p = Pattern.compile("href=\"(.*?)\"");
        Matcher m = p.matcher(messageString);
        if (m.find()) {
            intelLinkURL = m.group(1);
        }
        else
            intelLinkURL = "N/A";
        return intelLinkURL;
    }

    /**
     * Parse the address of an accepted portal from the email.
     *
     * @param messageString The body of the email as a String for parsing.
     * @return address of the portal.
     */
    private String parseLiveAddress(String messageString) {
        String liveAddress;
        Pattern titleFinder = Pattern.compile("<a[^>]*>(.*?)</a>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher = titleFinder.matcher(messageString);
        if (regexMatcher.find()) {
            liveAddress = regexMatcher.group(1);
        }
        else
            liveAddress = "N/A";
        return liveAddress;
    }
}
