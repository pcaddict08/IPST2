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

package com.einzig.ipst2.portal;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * @author Ryan Porterfield
 * @since 2017-05-17
 */
public class PortalAccepted extends PortalResponded {
    /**
     * Inflates a PortalSubmission from a Parcel
     */
    public static final Parcelable.Creator<PortalAccepted> CREATOR = new Parcelable.Creator<PortalAccepted>() {
        @Override
        public PortalAccepted createFromParcel(Parcel in) {
            return new PortalAccepted(in);
        }

        @Override
        public PortalAccepted[] newArray(int size) {
            return new PortalAccepted[size];
        }
    };

    /** URL that points to the portal on the Ingress Intel site. */
    private final String intelLinkURL;
    /** Real-world address of the portal */
    private final String liveAddress;

    /**
     * Create a new PortalAccepted.
     * @param name The name of the portal.
     * @param dateSubmitted The date the portal was submitted.
     * @param pictureURL The URL of the portal submission picture.
     * @param dateResponded The date that Niantic approved the portal.
     */
    public PortalAccepted(String name, Date dateSubmitted, String pictureURL, Date dateResponded,
                          String liveAddress, String intelLinkURL) {
        super(name, dateSubmitted, pictureURL, dateResponded);
        this.intelLinkURL = intelLinkURL;
        this.liveAddress = liveAddress;
    }

    /**
     * Create a new PortalAccepted from a Parcel.
     * @param in Parcel that contains the PortalAccepted.
     */
    private PortalAccepted(Parcel in) {
        super(in);
        intelLinkURL = in.readString();
        liveAddress = in.readString();
    }

    /**
     * Get the URL to the portal on the Ingress Intel site.
     * @return URL to the portal on the Ingress Intel site.
     */
    public String getIntelLinkURL() {
        return intelLinkURL;
    }

    /**
     * Get the real-world address of the portal.
     * @return real-world address of the portal.
     */
    public String getLiveAddress() {
        return liveAddress;
    }

    /*
     * Convert the portal to a Parcel.
     * Uses the C paradigm of passing the Parcel as an argument and modifying it instead of
     * returning a Parcel object.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(intelLinkURL);
        dest.writeString(liveAddress);
    }
}
