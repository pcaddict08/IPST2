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

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.io.Serializable;

public class PortalSubmission implements Parcelable, Serializable {
    /**
     * Inflates a PortalSubmission from a Parcel
     */
    public static final Creator<PortalSubmission> CREATOR;
    /**
     * Version descriptor for serialized PortalSubmissions
     */
    private static final long serialVersionUID;

    static {
        CREATOR = new Creator<PortalSubmission>() {
            @Override
            public PortalSubmission createFromParcel(Parcel in) {
                return new PortalSubmission(in);
            }

            @Override
            public PortalSubmission[] newArray(int size) {
                return new PortalSubmission[size];
            }
        };

        serialVersionUID = -223108874747293680L;
    }

    /**
     * The name of the portal.
     *
     * @serial
     */
    private String name;
    /**
     * The URL that links to the submission picture.
     *
     * @serial
     */
    private final String pictureURL;
    /**
     * The date the portal was submitted.
     *
     * @serial
     */
    private LocalDate dateSubmitted;

    /**
     * Create a new PortalSubmission.
     *
     * @param name          The name of the portal.
     * @param dateSubmitted The date the portal was submitted.
     * @param pictureURL    The URL of the portal submission picture.
     */
    public PortalSubmission(String name, LocalDate dateSubmitted, String pictureURL) {
        this.name = name;
        this.dateSubmitted = dateSubmitted;
        this.pictureURL = pictureURL;
    }

    /**
     * Create a new PortalSubmission from a Parcel.
     *
     * @param in Parcel that contains the PortalSubmission.
     */
    PortalSubmission(Parcel in) {
        name = in.readString();
        dateSubmitted = (LocalDate) in.readSerializable();
        pictureURL = in.readString();
    }

    /*
     * No idea what this does
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Check if two PortalSubmissions are the same.
     *
     * @param submission The submission being compared.
     * @return @c true if the two portal submissions are the same, otherwise false.
     */
    public boolean equals(PortalSubmission submission) {
        return pictureURL.equalsIgnoreCase(submission.pictureURL) && name.equals(submission.name);
    }

    /**
     * Get the date that the portal was submitted.
     *
     * @return the date that the portal was submitted.
     */
    public LocalDate getDateSubmitted() {
        return dateSubmitted;
    }

    /*
    * Return days since Niantic has responded
    * */
    public int getDaysSinceResponse() {
        return Days.daysBetween(dateSubmitted, LocalDate.now()).getDays();
    }


    /*
    * Return Sharable Portal Details
    * */
    public String getShareDetails() {
        String returnString = "";
        try {
            returnString = getName() + "\n"
                    + "Submitted: " + getDateSubmitted().toString() + "\n"
                    + "Days In Queue: " + getDaysSinceResponse() + "\n"
                    + "Status: Pending\n"
                    + "Picture URL: " + getPictureURL();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnString;
    }

    /**
     * Get the portal's name
     *
     * @return the portal's name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the URL of the portal submission picture
     *
     * @return the portal submission picture URL.
     */
    public String getPictureURL() {
        return pictureURL;
    }

    /**
     * Change the date that the portal was submitted.
     *
     * @param dateSubmitted the modified date the portal was submitted.
     */
    public void setDateSubmitted(LocalDate dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    /*
     * Convert the portal to a Parcel.
     * Uses the C paradigm of passing the Parcel as an argument and modifying it instead of
     * returning a Parcel object.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeSerializable(dateSubmitted);
        dest.writeString(pictureURL);
    }
}
