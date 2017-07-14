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

import com.einzig.ipst2.portal.PortalAccepted;

import org.joda.time.LocalDate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.einzig.ipst2.database.AcceptedPortalContract.AcceptedPortalEntry.COLUMN_INTEL_LINK_URL;
import static com.einzig.ipst2.database.AcceptedPortalContract.AcceptedPortalEntry.COLUMN_LIVE_ADDRESS;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_DATE_RESPONDED;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_DATE_SUBMITTED;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_NAME;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_PICTURE_URL;

/**
 * @author Ryan Porterfield
 * @since 2017-05-19
 */
public final class PortalAcceptedBuilder extends PortalBuilder<PortalAccepted> {
    /**
     *
     */
    public PortalAcceptedBuilder() {
    }

    @Override
    public PortalAccepted build(String name, LocalDate dateResponded, String message) {
        String pictureURL = parsePictureURL(message, name);
        String address = parseLiveAddress(message);
        String intelLink = parseIntelLink(message);
        return new PortalAccepted(name, null, pictureURL, dateResponded, address,
                intelLink);
    }

    /**
     * Create an instance of PortalAccepted from a database entry.
     *
     * @param c Cursor containing the database fields of the portal.
     * @return a PortalAccepted representation of a portal in the database.
     */
    @Override
    PortalAccepted build(Cursor c) {
        String name, pictureURL, location, intelLink;
        LocalDate submitted, responded;
        name = c.getString(c.getColumnIndex(COLUMN_NAME));
        submitted = parseDate(c.getString(c.getColumnIndex(COLUMN_DATE_SUBMITTED)));
        pictureURL = c.getString(c.getColumnIndex(COLUMN_PICTURE_URL));
        responded = parseDate(c.getString(c.getColumnIndex(COLUMN_DATE_RESPONDED)));
        location = c.getString(c.getColumnIndex(COLUMN_LIVE_ADDRESS));
        intelLink = c.getString(c.getColumnIndex(COLUMN_INTEL_LINK_URL));
        return new PortalAccepted(name, submitted, pictureURL, responded, location, intelLink);
    }

    /**
     * @inheritDoc
     */
    @Override
    PortalAccepted build(String[] csvLine) {
        /*
         * 0: name
         * 1: dateSubmitted
         * 2: dateAccepted
         * 3: dateRejected
         * 5: liveAddress
         * 6: intelLink
         * 7: pictureURL
         * 8: rejectionReason
         */
        String name = csvLine[0];
        String subDateStr = csvLine[1];
        String accDateStr = csvLine[2];
        String address = csvLine[5];
        String intelURL = csvLine[6];
        String pictureURL = csvLine[7];
        return new PortalAccepted(name, parseDate(subDateStr), pictureURL, parseDate(accDateStr),
                address, intelURL);
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
        try {
            Matcher m = p.matcher(messageString);
            if (m.find()) {
                intelLinkURL = m.group(1);
            } else
                intelLinkURL = "N/A";
        } catch (Exception e) {
            intelLinkURL = "N/A";
            e.printStackTrace();
        }
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
        liveAddress = "N/A";
        try {
            Matcher regexMatcher = titleFinder.matcher(messageString);
            if (regexMatcher.find()) {
                liveAddress = regexMatcher.group(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return liveAddress;
    }
}
