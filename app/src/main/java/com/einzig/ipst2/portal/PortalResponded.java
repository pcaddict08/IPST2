/******************************************************************************
 *                                                                            *
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
 *                                                                            *
 ******************************************************************************/

package com.einzig.ipst2.portal;

import android.os.Parcel;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Ryan Porterfield
 * @since 2015-07-24
 */
public abstract class PortalResponded extends PortalSubmission {
    private static final long serialVersionUID = 2609937877038303238L;
    /**
     * The date that Niantic approved or denied the portal.
     */
    private final Date dateResponded;

    /**
     * Create a new PortalResponded.
     *
     * @param name The name of the portal.
     * @param dateSubmitted The date the portal was submitted.
     * @param pictureURL The URL of the portal submission picture.
     * @param dateResponded The date that Niantic approved or denied the portal.
     */
    PortalResponded(String name, Date dateSubmitted, String pictureURL, Date dateResponded) {
        super(name, dateSubmitted, pictureURL);
        this.dateResponded = dateResponded;
    }

    /**
     * Create a new PortalResponded from a Parcel.
     * @param in Parcel that contains the PortalResponded.
     */
    PortalResponded(Parcel in) {
        super(in);
        dateResponded = (Date) in.readSerializable();
    }

    /**
     * Get the date the portal submission was processed.
     * @return The date the portal was approved or rejected.
     */
    public Date getDateResponded() {
        return this.dateResponded;
    }

    public long getResponseTime() {
        long diff = dateResponded.getTime() - getDateSubmitted().getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    /*
   *  Return a formatted responded date
   * */
    public String getDateRespondedString()
    {
        return dateFormat.format(this.dateResponded);
    }

    /*
    * Return days since Niantic has responded
    * */
    @Override
    public int getDaysSinceResponse()
    {
        return Days.daysBetween(new DateTime(this.dateResponded).toLocalDate(), new DateTime().toLocalDate()).getDays();
    }

    /*
     * Convert the portal to a Parcel.
     * Uses the C paradigm of passing the Parcel as an argument and modifying it instead of
     * returning a Parcel object.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeSerializable(dateResponded);
    }
}
