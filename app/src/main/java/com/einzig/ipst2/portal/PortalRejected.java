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
import android.os.Parcelable;

import org.joda.time.LocalDate;

/**
 * @author Ryan Porterfield
 * @since 2015-07-24
 */
public class PortalRejected extends PortalResponded {
    /**
     * Inflates a PortalSubmission from a Parcel
     */
    public static final Parcelable.Creator<PortalRejected> CREATOR =
            new Parcelable.Creator<PortalRejected>() {
                @Override
                public PortalRejected createFromParcel(Parcel in) {
                    return new PortalRejected(in);
                }

                @Override
                public PortalRejected[] newArray(int size) {
                    return new PortalRejected[size];
                }
            };
    private static final long serialVersionUID = 622210508012331815L;
    /**
     * The reason Niantic gave for the portal being rejected.
     */
    private final String rejectionReason;

    /**
     * Create a new PortalRejected.
     *
     * @param name            The name of the portal.
     * @param dateSubmitted   The date the portal was submitted.
     * @param pictureURL      The URL of the portal submission picture.
     * @param dateResponded   The date that Niantic denied the portal.
     * @param rejectionReason The reason Niantic gave for rejecting the portal submission.
     */
    public PortalRejected(String name, LocalDate dateSubmitted, String pictureURL,
            LocalDate dateResponded, String rejectionReason) {
        super(name, dateSubmitted, pictureURL, dateResponded);
        this.rejectionReason = rejectionReason;
    }

    /**
     * Create a new PortalRejected from a Parcel.
     *
     * @param in Parcel that contains the PortalRejected.
     */
    private PortalRejected(Parcel in) {
        super(in);
        rejectionReason = in.readString();
    }

    /**
     * Get the reason the submission was rejected.
     *
     * @return the reason the submission was rejected.
     */
    public String getRejectionReason() {
        return this.rejectionReason;
    }

    /*
     * Convert the portal to a Parcel.
     * Uses the C paradigm of passing the Parcel as an argument and modifying it instead of
     * returning a Parcel object.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(rejectionReason);
    }
}
