package com.einzig.ipst2.portal;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * @author Ryan Porterfield
 * @since 2015-07-24
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

    private String intelLinkURL;
    private String liveAddress;

    /**
     * Create a new PortalAccepted.
     *
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

    protected PortalAccepted(Parcel in) {
        super(in);
        intelLinkURL = in.readString();
        liveAddress = in.readString();
    }

    public String getIntelLinkURL() {
        return intelLinkURL;
    }

    public String getLiveAddress() {
        return liveAddress;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(intelLinkURL);
        dest.writeString(liveAddress);
    }
}
