package com.einzig.ipst2.portal;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * @author Ryan Porterfield
 * @since 2015-07-24
 */
public class PortalRejected extends PortalResponded {
    /**
     * Inflates a PortalSubmission from a Parcel
     */
    public static final Parcelable.Creator<PortalRejected> CREATOR = new Parcelable.Creator<PortalRejected>() {
        @Override
        public PortalRejected createFromParcel(Parcel in) {
            return new PortalRejected(in);
        }

        @Override
        public PortalRejected[] newArray(int size) {
            return new PortalRejected[size];
        }
    };

    /**
     *
     */
    private static final long serialVersionUID = 622210508012331815L;

    /**
     * The reason Niantic gave for the portal being rejected.
     */
    private String rejectionReason;

    /**
     * Create a new PortalRejected.
     *
     * @param name The name of the portal.
     * @param dateSubmitted The date the portal was submitted.
     * @param pictureURL The URL of the portal submission picture.
     * @param dateResponded The date that Niantic denied the portal.
     * @param rejectionReason The reason Niantic gave for rejecting the portal submission.
     */
    public PortalRejected(String name, Date dateSubmitted, String pictureURL, Date dateResponded, String rejectionReason) {
        super(name, dateSubmitted, pictureURL, dateResponded);
        this.rejectionReason = rejectionReason;
    }

    protected PortalRejected(Parcel in) {
        super(in);
        rejectionReason = in.readString();
    }

    /**
     * Get the reason the submission was rejected.
     * @return the reason the submission was rejected.
     */
    public String getRejectionReason() {
        return this.rejectionReason;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(rejectionReason);
    }
}
