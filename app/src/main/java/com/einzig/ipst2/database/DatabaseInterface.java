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

package com.einzig.ipst2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import static com.einzig.ipst2.activities.MainActivity.TAG;

/**
 * @author Ryan Porterfield
 * @since 2017-05-18
 */
public class DatabaseInterface extends SQLiteOpenHelper {
    /** The date format that MySQL stores DATETIME objects in */
    static public final SimpleDateFormat dateFormatter;
    /** Database name */
    static final private String DATABASE_NAME = "IPSTSubmissionDB";
    /** Database version */
    static final private int DATABASE_VERSION = 1;
    /** Table key for the date Niantic approved or denied the portal */
    static final private String KEY_DATE_RESPONDED = "dateResponded";
    /** Table key for the date the portal was submitted */
    static final private String KEY_DATE_SUBMITTED = "dateSubmitted";
    /** Table key for the link to the portal on the intel map */
    static final private String KEY_INTEL_LINK_URL = "intelLinkURL";
    /** Table key for address of the portal */
    static final private String KEY_LIVE_ADDRESS = "liveAddress";
    /** Table key for portal name */
    static final private String KEY_NAME = "name";
    /** Table key for the URL to the submission picture */
    static final private String KEY_PICTURE_URL = "pictureURL";
    /** Table key for the reason the portal was rejected */
    static final private String KEY_REJECTION_REASON = "rejectionReason";
    /** The name of the table in containing accepted portal submissions */
    static final private String TABLE_ACCEPTED = "acceptedSubmissions";
    /** The name of the table in containing pending portal submissions */
    static final private String TABLE_PENDING = "pendingSubmissions";
    /** The name of the table in containing rejected portal submissions */
    static final private String TABLE_REJECTED = "rejectedSubmissions";

    static {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    /**
     * Create a new DatabaseInterface to interact with the SQLite database for the application
     *
     * @param context Context used by super class
     */
    public DatabaseInterface(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Add some values to a table in the database.
     *
     * @param table  Table key in the database
     * @param values Values to be inserted
     */
    private void addPortal(String table, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long result = db.insert(table, null, values);
        if (result == -1)
            Log.e(TAG, values.get(KEY_PICTURE_URL) + " NOT UNIQUE");
        db.close();
    }

    /**
     * Insert a PortalAccepted into the database
     *
     * @param portal The portal getting stored in the database
     */
    public void addPortalAccepted(PortalAccepted portal) {
        Log.d(TAG, "Add accepted portal: " + portal.getName());
        if(portal.getDateSubmitted() == null)
            portal.setDateSubmitted(portal.getDateResponded());
        String dateSubmitted = getDateStringSafe(portal.getDateSubmitted());
        String dateResponded = getDateStringSafe(portal.getDateResponded());
        ContentValues values = new ContentValues();
        // Values put!
        values.put(KEY_NAME, portal.getName());
        values.put(KEY_DATE_SUBMITTED, dateSubmitted);
        values.put(KEY_PICTURE_URL, portal.getPictureURL());
        values.put(KEY_DATE_RESPONDED, dateResponded);
        values.put(KEY_LIVE_ADDRESS, portal.getLiveAddress());
        values.put(KEY_INTEL_LINK_URL, portal.getIntelLinkURL());
        addPortal(TABLE_ACCEPTED, values);
    }

    /**
     * Insert a PortalRejected into the database
     *
     * @param portal The portal getting stored in the database
     */
    public void addPortalRejected(PortalRejected portal) {
        Log.d(TAG, "Add rejected portal: " + portal.getName());
        String dateSubmitted = getDateStringSafe(portal.getDateSubmitted());
        String dateResponded = getDateStringSafe(portal.getDateResponded());
        ContentValues values = new ContentValues();
        // Values put!
        values.put(KEY_NAME, portal.getName());
        values.put(KEY_DATE_SUBMITTED, dateSubmitted);
        values.put(KEY_PICTURE_URL, portal.getPictureURL());
        values.put(KEY_DATE_RESPONDED, dateResponded);
        values.put(KEY_REJECTION_REASON, portal.getRejectionReason());
        addPortal(TABLE_REJECTED, values);
    }

    /**
     * Insert a PortalSubmission into the database
     *
     * @param portal The portal getting stored in the database
     */
    public void addPortalSubmission(PortalSubmission portal) {
        Log.d(TAG, "Add portal submission: " + portal.getName());
        String dateSubmitted = dateFormatter.format(portal.getDateSubmitted());
        ContentValues values = new ContentValues();
        // Values put!
        values.put(KEY_NAME, portal.getName());
        values.put(KEY_DATE_SUBMITTED, dateSubmitted);
        values.put(KEY_PICTURE_URL, portal.getPictureURL());
        addPortal(TABLE_PENDING, values);
    }

    /**
     * Check if the database contains an accepted portal submission
     *
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return true if the database contains the portal, otherwise false
     */
    public boolean containsAccepted(String portalPictureURL) {
        return getAcceptedPortal(portalPictureURL) != null;
    }

    /**
     * Check if the database contains a pending portal submission
     *
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return true if the database contains the portal, otherwise false
     */
    public boolean containsPending(String portalPictureURL) {
        return getPendingPortal(portalPictureURL) != null;
    }

    /**
     * Check if the database contains a rejected portal submission
     *
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return true if the database contains the portal, otherwise false
     */
    public boolean containsRejected(String portalPictureURL) {
        return getRejectedPortal(portalPictureURL) != null;
    }

    /**
     * Create a table in the database
     *
     * @param tableName    The name of the table
     * @param optionalKeys Any keys in addition to the 3 default keys in the table
     * @return A MySQL command string that creates a table for storing PortalSubmissions
     */
    private String createTable(String tableName, String optionalKeys) {
        String createCommand = "CREATE TABLE " + tableName + " ( " + KEY_NAME + " TEXT NOT NULL, " +
                KEY_DATE_SUBMITTED + " DATETIME NOT NULL, " + KEY_PICTURE_URL +
                " TEXT NOT NULL UNIQUE";
        if (!optionalKeys.equals(""))
            createCommand = createCommand + ", " + optionalKeys;
        return createCommand + ", PRIMARY KEY (" + KEY_PICTURE_URL + ") );";
    }

    /**
     * Creates a string containing the additional keys for the table of accepted portals
     *
     * @return the additional keys for the table of accepted portals
     */
    private String createTableAccepted() {
        return KEY_DATE_RESPONDED + " DATETIME, " + KEY_LIVE_ADDRESS + " TEXT, " +
                KEY_INTEL_LINK_URL + " TEXT ";
    }

    /**
     * Creates a string containing the additional keys for the table of rejected portals
     *
     * @return the additional keys for the table of rejected portals
     */
    private String createTableRejected() {
        return KEY_DATE_RESPONDED + " DATETIME, " + KEY_REJECTION_REASON + " TEXT ";
    }

    /**
     * Remove an accepted portal submission
     *
     * @param portal The accepted submission being removed from the database
     */
    public void deleteAccepted(PortalAccepted portal) {
        Log.d(TAG, "Remove accepted portal: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACCEPTED, KEY_PICTURE_URL + " = ?", new String[]{portal.getPictureURL()});
        db.close();
    }

    /**
     * Clear the database
     */
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PENDING, null, null);
        db.delete(TABLE_ACCEPTED, null, null);
        db.delete(TABLE_REJECTED, null, null);
        db.close();
    }

    /**
     * Remove a pending portal submission
     *
     * @param portal The pending submission being removed from the database
     */
    public void deletePending(PortalSubmission portal) {
        Log.d(TAG, "Remove portal submission: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PENDING, KEY_PICTURE_URL + " = ?", new String[]{portal.getPictureURL()});
        db.close();
    }

    /**
     * Remove a rejected portal submission
     *
     * @param portal The rejected submission being removed from the database
     */
    public void deleteRejected(PortalRejected portal) {
        Log.d(TAG, "Remove rejected portal: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REJECTED, KEY_PICTURE_URL + " = ?", new String[]{portal.getPictureURL()});
        db.close();
    }

    /**
     * Get a Vector of all accepted portals which went live after a certain date
     *
     * @param fromDate Date to start searching from
     * @return Vector of all accepted portals which went live after a certain date
     */
    public Vector<PortalAccepted> getAcceptedByResponseDate(Date fromDate) {
        return getAcceptedByResponseDate(fromDate, Calendar.getInstance().getTime());
    }

    /**
     * Get a Vector of all accepted portals which went live in between a range of days
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of all accepted portals which went live in between a range of days
     */
    public Vector<PortalAccepted> getAcceptedByResponseDate(Date fromDate, Date toDate) {
        return getAllAcceptedByDate(KEY_DATE_RESPONDED, fromDate, toDate);
    }

    /**
     * Get a Vector of all accepted portals which were submitted after a certain date
     *
     * @param fromDate Date to start searching from
     * @return Vector of all accepted portals which were submitted after a certain date
     */
    public Vector<PortalAccepted> getAcceptedBySubmissionDate(Date fromDate) {
        return getAcceptedBySubmissionDate(fromDate, Calendar.getInstance().getTime());
    }

    /**
     * Get a Vector of all accepted portals which were submitted in between a range of days
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of all accepted portals which were submitted in between a range of days
     */
    public Vector<PortalAccepted> getAcceptedBySubmissionDate(Date fromDate, Date toDate) {
        return getAllAcceptedByDate(KEY_DATE_SUBMITTED, fromDate, toDate);
    }

    /**
     * Get the number of approved portals
     *
     * @return number of approved portals
     */
    public long getAcceptedCount() {
        return getEntryCount(TABLE_ACCEPTED, null, null);
    }

    /**
     * Get the number of portals accepted since fromDate
     *
     * @param fromDate Date to start searching from
     * @return number of portals accepted since fromDate
     */
    public long getAcceptedCountByResponseDate(Date fromDate) {
        return getCountByDate(TABLE_ACCEPTED, KEY_DATE_RESPONDED, fromDate);
    }

    /**
     * Get the number of portals accepted between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of portals accepted between fromDate and toDate
     */
    public long getAcceptedCountByResponseDate(Date fromDate, Date toDate) {
        return getCountByDate(TABLE_ACCEPTED, KEY_DATE_RESPONDED, fromDate, toDate);
    }

    /**
     * Get the number of accepted portals submitted since fromDate
     *
     * @param fromDate Date to start searching from
     * @return number of accepted portals submitted since fromDate
     */
    public long getAcceptedCountBySubmissionDate(Date fromDate) {
        return getCountByDate(TABLE_ACCEPTED, KEY_DATE_SUBMITTED, fromDate);
    }

    /**
     * Get the number of accepted portals submitted between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of accepted portals submitted between fromDate and toDate
     */
    public long getAcceptedCountBySubmissionDate(Date fromDate, Date toDate) {
        return getCountByDate(TABLE_ACCEPTED, KEY_DATE_SUBMITTED, fromDate, toDate);
    }

    /**
     * Get an accepted portal submission from the database
     *
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalAccepted representation of an accepted portal in the database
     */
    public PortalAccepted getAcceptedPortal(String portalPictureURL) {
        return getPortal(TABLE_ACCEPTED, portalPictureURL, new PortalAcceptedBuilder());
    }

    /**
     * @param table   Database table to search
     * @param builder PortalBuilder to build the portal from the query
     * @param <P>     Type of PortalSubmission being returned
     * @return all portals in table
     */
    private <P extends PortalSubmission> Vector<P> getAll(String table, PortalBuilder<P> builder) {
        return getAll(table, null, null, builder);
    }

    /**
     * @param table         Database table to search
     * @param selection     WHERE claus
     * @param selectionArgs Arguments for wildcards in selection
     * @param builder       PortalBuilder to build the portal from the query
     * @param <P>           Type of PortalSubmission being returned
     * @return all portals in table which match the selection
     */
    private <P extends PortalSubmission> Vector<P> getAll(String table, String
            selection, String[] selectionArgs, PortalBuilder<P> builder) {
        Vector<P> portals = new Vector<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table, null, selection, selectionArgs, null, null, null, null);
        while (cursor.moveToNext()) {
            portals.add(builder.build(cursor));
        }
        return portals;
    }

    /**
     * Get a Vector of all accepted portal submissions
     *
     * @return Vector of all accepted portal submissions
     */
    public Vector<PortalAccepted> getAllAccepted() {
        Log.d(TAG, "Get all accepted portals");
        return getAll(TABLE_ACCEPTED, new PortalAcceptedBuilder());
    }

    /**
     * Helper function getAcceptedBySubmissionDate and getAllAcceptedBy
     *
     * @param dateKey  Database key used for searching. Can be either KEY_DATE_SUBMITTED or
     *                 KEY_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of accepted portals which were either submitted or approved from fromDate to
     * toDate
     */
    private Vector<PortalAccepted> getAllAcceptedByDate(String dateKey, Date fromDate,
            Date toDate) {
        Log.d(TAG, "Getting all accepted portals within date range");
        Vector<PortalAccepted> portals = getPortalsByDate(TABLE_ACCEPTED, dateKey, fromDate,
                toDate, new PortalAcceptedBuilder());
        return portals;
    }

    /**
     * Gets a Vector of all pending portal submissions
     *
     * @return Vector of all pending portal submissions
     */
    public Vector<PortalSubmission> getAllPending() {
        Log.d(TAG, "Get all pending portals");
        return getAll(TABLE_PENDING, new PortalSubmissionBuilder());
    }

    /**
     * Get all portals from the database
     *
     * @return all portals from the database
     */
    public Vector<PortalSubmission> getAllPortals() {
        Vector<PortalSubmission> portals = getAllPending();
        portals.addAll(getAllAccepted());
        portals.addAll(getAllRejected());
        return portals;
    }

    /**
     * Get all portals after date from the database
     *
     * @return all portals after date from the database
     */
    public Vector<PortalSubmission> getAllPortalsFromDate(Date fromDate) {
        Vector<PortalSubmission> portals = new Vector<>();
        portals.addAll(getPendingByDate(fromDate));
        portals.addAll(getAcceptedByResponseDate(fromDate));
        portals.addAll(getRejectedByResponseDate(fromDate));
        return portals;
    }

    /**
     * Gets a Vector of all rejected portal submissions
     *
     * @return Vector of all rejected portal submissions
     */
    public Vector<PortalRejected> getAllRejected() {
        Log.d(TAG, "Get all rejected portals");
        return getAll(TABLE_REJECTED, new PortalRejectedBuilder());
    }

    /**
     * Get the number of portals in a table since fromDate
     *
     * @param table    Table in the database
     * @param dateKey  Either KEY_DATE_SUBMITTED or KEY_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @return number of portals in a table since date
     */
    private long getCountByDate(String table, String dateKey, Date fromDate) {
        return getCountByDate(table, dateKey, fromDate, Calendar.getInstance().getTime());
    }

    /**
     * Get the number of portals in a table between fromDate and toDate
     *
     * @param table    Table in the database
     * @param dateKey  Either KEY_DATE_SUBMITTED or KEY_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of portals in a table between fromDate and toDate
     */
    private long getCountByDate(String table, String dateKey, Date fromDate, Date toDate) {
        String fromDateStr = dateFormatter.format(fromDate);
        String toDateStr = dateFormatter.format(toDate);

        return getEntryCount(table, dateKey + " BETWEEN ? AND ?",
                new String[]{fromDateStr, toDateStr});
    }

    /**
     * Get the total number of portals in the database
     *
     * @return the total number of portals in the database
     */
    public long getDatabaseSize() {
        return getAcceptedCount() + getPendingCount() + getRejectedCount();
    }

    /**
     * Safely get a String representation of date
     *
     * @param date A point in time
     * @return String representation of date
     */
    private String getDateStringSafe(Date date) {
        return (date != null) ? dateFormatter.format(date) : "";
    }

    /**
     * Get the number of entries in a table
     *
     * @param table Table to count rows on
     * @return number of entries in a table
     */
    private long getEntryCount(String table, String selection, String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, table, selection, selectionArgs);
        db.close();
        return count;
    }

    /**
     * Get all pending portal submissions which were submitted between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of pending portals which were submitted between fromDate and toDate
     */
    public Vector<PortalSubmission> getPendingByDate(Date fromDate, Date toDate) {
        Log.d(TAG, "Getting all pending portals in a date range");
        return getPortalsByDate(TABLE_PENDING, KEY_DATE_SUBMITTED, fromDate, toDate,
                new PortalSubmissionBuilder());
    }

    /**
     * Get all pending portal submissions which were submitted since fromDate
     *
     * @param fromDate Date to start searching from
     * @return Vector of all pending portals which were submitted after a fromDate
     */
    public Vector<PortalSubmission> getPendingByDate(Date fromDate) {
        return getPendingByDate(fromDate, Calendar.getInstance().getTime());
    }

    /**
     * Get the number of pending portals
     *
     * @return number of pending portals
     */
    public long getPendingCount() {
        return getEntryCount(TABLE_PENDING, null, null);
    }

    /**
     * Get the number of portals that were submitted since fromDate
     *
     * @param fromDate Date to start searching from
     * @return number of portals that were submitted since fromDate
     */
    public long getPendingCountByDate(Date fromDate) {
        return getCountByDate(TABLE_PENDING, KEY_DATE_SUBMITTED, fromDate);
    }

    /**
     * Get the number of portals that were submitted between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of portals that were submitted between fromDate and toDate
     */
    public long getPendingCountByDate(Date fromDate, Date toDate) {
        return getCountByDate(TABLE_PENDING, KEY_DATE_SUBMITTED, fromDate, toDate);
    }

    /**
     * Get a pending portal submission from the database
     *
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalSubmission representation of a pending portal in the database
     */
    public PortalSubmission getPendingPortal(String portalPictureURL) {
        Log.d(TAG, "Getting pending portal");
        return getPortal(TABLE_PENDING, portalPictureURL, new PortalSubmissionBuilder());
    }

    /**
     * @param table      Database table to search
     * @param pictureURL Unique key for a portal in the database
     * @param builder    PortalBuilder to build the portal from the query
     * @param <P>        Type of PortalSubmission being returned
     * @return a portal from the database
     */
    private <P extends PortalSubmission> P getPortal(String table, String pictureURL,
            PortalBuilder<P> builder) {
        Vector<P> portals = getAll(table, KEY_PICTURE_URL + " = ?", new String[]{pictureURL},
                builder);
        return (portals.size() > 0) ? portals.firstElement() : null;
    }

    /**
     * Get all portals in a range of dates.
     *
     * @param dateKey  Database key used for searching. Can be either KEY_DATE_SUBMITTED or
     *                 KEY_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of portals which were either submitted or approved from fromDate to
     * toDate.
     * @see DatabaseInterface#KEY_DATE_RESPONDED
     * @see DatabaseInterface#KEY_DATE_SUBMITTED
     */
    private <P extends PortalSubmission> Vector<P> getPortalsByDate(String table, String dateKey,
            Date fromDate, Date toDate,
            PortalBuilder<P> builder) {
        String fromDateStr = dateFormatter.format(fromDate);
        String toDateStr = dateFormatter.format(toDate);
        return getAll(table, dateKey + " BETWEEN ? AND ?", new String[]{fromDateStr, toDateStr},
                builder);
    }

    /**
     * Helper function getAcceptedBySubmissionDate and getAllAcceptedBy
     *
     * @param dateKey  Database key used for searching. Can be either KEY_DATE_SUBMITTED or
     *                 KEY_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of rejected portals which were either submitted or approved from fromDate to
     * toDate.
     */
    private Vector<PortalRejected> getRejectedByDate(String dateKey, Date fromDate, Date toDate) {
        Log.d(TAG, "Getting all rejected portals within date range");
        return getPortalsByDate(TABLE_REJECTED, dateKey, fromDate, toDate, new
                PortalRejectedBuilder());
    }

    /**
     * Get a Vector of all rejected portals which went live after a certain date
     *
     * @param fromDate Date to start searching from
     * @return Vector of all rejected portals which went live after a certain date
     */
    public Vector<PortalRejected> getRejectedByResponseDate(Date fromDate) {
        return getRejectedByResponseDate(fromDate, Calendar.getInstance().getTime());
    }

    /**
     * Get a Vector of all rejected portals which went live in between a range of days
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of all rejected portals which went live in between a range of days
     */
    public Vector<PortalRejected> getRejectedByResponseDate(Date fromDate, Date toDate) {
        return getRejectedByDate(KEY_DATE_RESPONDED, fromDate, toDate);
    }

    /**
     * Get a Vector of all rejected portals which were submitted after a certain date
     *
     * @param fromDate Date to start searching from
     * @return Vector of all rejected portals which were submitted after a certain date
     */
    public Vector<PortalRejected> getRejectedBySubmissionDate(Date fromDate) {
        return getRejectedBySubmissionDate(fromDate, Calendar.getInstance().getTime());
    }

    /**
     * Get a Vector of all rejected portals which were submitted in between a range of days
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of all rejected portals which were submitted in between a range of days
     */
    public Vector<PortalRejected> getRejectedBySubmissionDate(Date fromDate, Date toDate) {
        return getRejectedByDate(KEY_DATE_SUBMITTED, fromDate, toDate);
    }

    /**
     * Get the number of rejected portals
     *
     * @return number of rejected portals
     */
    public long getRejectedCount() {
        return getEntryCount(TABLE_REJECTED, null, null);
    }

    /**
     * Get the number of portals that were rejected since fromDate
     *
     * @param fromDate Date to start searching from
     * @return number of portals that were rejected since fromDate
     */
    public long getRejectedCountByResponseDate(Date fromDate) {
        return getCountByDate(TABLE_REJECTED, KEY_DATE_RESPONDED, fromDate);
    }

    /**
     * Get the number of portals that were rejected between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of portals that were rejected between fromDate and toDate
     */
    public long getRejectedCountByResponseDate(Date fromDate, Date toDate) {
        return getCountByDate(TABLE_REJECTED, KEY_DATE_RESPONDED, fromDate, toDate);
    }

    /**
     * Get the number of rejected portals that were submitted since fromDate
     *
     * @param fromDate Date to start searching from
     * @return number of rejected portals that were submitted since fromDate
     */
    public long getRejectedCountBySubmissionDate(Date fromDate) {
        return getCountByDate(TABLE_REJECTED, KEY_DATE_SUBMITTED, fromDate);
    }

    /**
     * Get the number of rejected portals that were submitted between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of rejected portals that were submitted between fromDate and toDate
     */
    public long getRejectedCountBySubmissionDate(Date fromDate, Date toDate) {
        return getCountByDate(TABLE_REJECTED, KEY_DATE_SUBMITTED, fromDate, toDate);
    }

    /**
     * Get a rejected portal submission from the database
     *
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalRejected representation of a rejected portal in the database
     */
    public PortalRejected getRejectedPortal(String portalPictureURL) {
        Log.d(TAG, "Getting rejected portal");
        return getPortal(TABLE_REJECTED, portalPictureURL, new PortalRejectedBuilder());
    }

    /**
     * Create tables in the database
     *
     * @param db A reference to the SQLiteDatabase object used by the app
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable(TABLE_PENDING, ""));
        db.execSQL(createTable(TABLE_ACCEPTED, createTableAccepted()));
        db.execSQL(createTable(TABLE_REJECTED, createTableRejected()));
    }

    /**
     * Delete the old tables from the database and create the new tables
     *
     * @param db         A reference to the SQLiteDatabase object used by the app
     * @param oldVersion The current version of the database
     * @param newVersion The new version of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Don't need this yet
    }

    /**
     * Update a database entry for an accepted portal
     *
     * @param portal    PortalAccepted containing the new information
     * @param oldPortal PortalAccepted containing the old information to be updated
     */
    public void updateAccepted(PortalAccepted portal, PortalAccepted oldPortal) {
        Log.d(TAG, "Update portal: " + oldPortal.getName());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        String dateSubmitted = dateFormatter.format(portal.getDateSubmitted());
        String dateResponded = dateFormatter.format(portal.getDateResponded());

        values.put(KEY_NAME, portal.getName());
        values.put(KEY_DATE_SUBMITTED, dateSubmitted);
        values.put(KEY_PICTURE_URL, portal.getPictureURL());
        values.put(KEY_DATE_RESPONDED, dateResponded);
        values.put(KEY_LIVE_ADDRESS, portal.getLiveAddress());
        values.put(KEY_INTEL_LINK_URL, portal.getIntelLinkURL());

        db.update(TABLE_ACCEPTED, values, KEY_PICTURE_URL + " = ?",
                new String[]{String.valueOf(oldPortal.getPictureURL())});

        db.close();
    }

    /**
     * Update a database entry for a pending portal
     *
     * @param portal    PortalSubmission containing the new information
     * @param oldPortal PortalSubmission containing the old information to be updated
     */
    public void updatePending(PortalSubmission portal, PortalSubmission oldPortal) {
        Log.d(TAG, "Update portal: " + oldPortal.getName());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, portal.getName());
        values.put(KEY_DATE_SUBMITTED, dateFormatter.format(portal.getDateSubmitted()));
        values.put(KEY_PICTURE_URL, portal.getPictureURL());

        db.update(TABLE_PENDING, values, KEY_PICTURE_URL + " = ?",
                new String[]{String.valueOf(oldPortal.getPictureURL())});
        db.close();
    }

    /**
     * Update a database entry for a rejected portal
     *
     * @param portal    PortalRejected containing the new information
     * @param oldPortal PortalRejected containing the old information to be updated
     */
    public void updateRejected(PortalRejected portal, PortalRejected oldPortal) {
        Log.d(TAG, "Update portal: " + oldPortal.getName());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        String dateSubmitted = dateFormatter.format(portal.getDateSubmitted());
        String dateResponded = dateFormatter.format(portal.getDateResponded());

        values.put(KEY_NAME, portal.getName());
        values.put(KEY_DATE_SUBMITTED, dateSubmitted);
        values.put(KEY_PICTURE_URL, portal.getPictureURL());
        values.put(KEY_DATE_RESPONDED, dateResponded);
        values.put(KEY_REJECTION_REASON, portal.getRejectionReason());

        db.update(TABLE_REJECTED, values, KEY_PICTURE_URL + " = ?",
                new String[]{String.valueOf(oldPortal.getPictureURL())});
        db.close();
    }
}
