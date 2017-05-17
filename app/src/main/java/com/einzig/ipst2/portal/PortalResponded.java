package com.einzig.ipst2.portal;

import android.os.Parcel;

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
    private Date dateResponded;

    /**
     * Create a new PortalResponded.
     *
     * @param name The name of the portal.
     * @param dateSubmitted The date the portal was submitted.
     * @param pictureURL The URL of the portal submission picture.
     * @param dateResponded The date that Niantic approved or denied the portal.
     */
    public PortalResponded(String name, Date dateSubmitted, String pictureURL, Date dateResponded) {
        super(name, dateSubmitted, pictureURL);
        this.dateResponded = dateResponded;
    }

    protected PortalResponded(Parcel in) {
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeSerializable(dateResponded);
    }
}
