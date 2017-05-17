package com.einzig.ipst2.portal;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

public class PortalSubmission implements Parcelable, Serializable {
    /**
     * Inflates a PortalSubmission from a Parcel
     */
    public static final Creator<PortalSubmission> CREATOR = new Creator<PortalSubmission>() {
        @Override
        public PortalSubmission createFromParcel(Parcel in) {
            return new PortalSubmission(in);
        }

        @Override
        public PortalSubmission[] newArray(int size) {
            return new PortalSubmission[size];
        }
    };

    /**
     * Serializable version string
     */
    private static final long serialVersionUID = -223108874747293680L;

    /**
     * The name of the portal.
     *
     * @serial
     */
    private String name;
    /**
     * The date the portal was submitted.
     *
     * @serial
     */
    private Date dateSubmitted;
    /**
     * The URL that links to the submission picture.
     *
     * @serial
     */
    private String pictureURL;

    /**
     * Create a new PortalSubmission.
     *
     * @param name The name of the portal.
     * @param dateSubmitted The date the portal was submitted.
     * @param pictureURL The URL of the portal submission picture.
     */
    public PortalSubmission(String name, Date dateSubmitted, String pictureURL) {
        this.name = name;
        this.dateSubmitted = dateSubmitted;
        this.pictureURL = pictureURL;
    }

    protected PortalSubmission(Parcel in) {
        name = in.readString();
        dateSubmitted = (Date) in.readSerializable();
        pictureURL = in.readString();
    }

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
        return pictureURL.equalsIgnoreCase(submission.pictureURL);
    }

    /**
     * Get the date that the portal was submitted.
     * @return the date that the portal was submitted.
     */
    public Date getDateSubmitted() {
        return dateSubmitted;
    }

    /**
     * Get the portal's name
     * @return the portal's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL of the portal submission picture
     * @return the portal submission picture URL.
     */
    public String getPictureURL() {
        return pictureURL;
    }

    /**
     * Change the date that the portal was submitted.
     * @param dateSubmitted the modified date the portal was submitted.
     */
    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    /**
     * Change the name of the portal.
     * @param name The new name of the portal.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeSerializable(dateSubmitted);
        dest.writeString(pictureURL);
    }
}
