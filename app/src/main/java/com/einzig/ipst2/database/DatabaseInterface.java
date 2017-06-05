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

package com.einzig.ipst2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.einzig.ipst2.activities.MainActivity;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

/** TODO (Ryan): Get # of portals after date sorted by SORT_KEY
 * @author Ryan Porterfield
 * @since 2017-05-18
 */
public class DatabaseInterface extends SQLiteOpenHelper {
    /** The name of the table in containing accepted portal submissions */
    static final String TABLE_ACCEPTED = "acceptedSubmissions";
    /** The name of the table in containing pending portal submissions */
    static final String TABLE_PENDING = "pendingSubmissions";
    /** The name of the table in containing rejected portal submissions */
    static final String TABLE_REJECTED = "rejectedSubmissions";
    /** Database version */
    static private final int DATABASE_VERSION = 1;
    /** Database name */
    static private final String DATABASE_NAME = "IPSTSubmissionDB";
    /** Table key for portal name */
    static private final String KEY_NAME = "name";
    /** Table key for the date the portal was submitted */
    static private final String KEY_DATE_SUBMITTED = "dateSubmitted";
    /** Table key for the date Niantic approved or denied the portal */
    static private final String KEY_DATE_RESPONDED = "dateResponded";
    /** Table key for address of the portal */
    static private final String KEY_LIVE_ADDRESS = "liveAddress";
    /** Table key for the link to the portal on the intel map */
    static private final String KEY_INTEL_LINK_URL = "intelLinkURL";
    /** Table key for the reason the portal was rejected */
    static private final String KEY_REJECTION_REASON = "rejectionReason";
    /** Table key for the URL to the submission picture */
    static private final String KEY_PICTURE_URL = "pictureURL";
    /** The date format that MySQL stores DATETIME objects in */
    private final SimpleDateFormat dateFormatter;

    /**
     * Create a new DatabaseInterface to interact with the SQLite database for the application
     * @param context Context used by super class
     */
    public DatabaseInterface(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    }

    /**
     * Insert a PortalAccepted into the database
     * @param portal The portal getting stored in the database
     */
    public void addPortalAccepted(PortalAccepted portal) {
        Log.d(MainActivity.TAG, "Add accepted portal: " + portal.getName());
        String dateSubmitted = dateFormatter.format(portal.getDateSubmitted());
        String dateResponded = dateFormatter.format(portal.getDateResponded());
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
     * @param portal The portal getting stored in the database
     */
    public void addPortalRejected(PortalRejected portal) {
        Log.d(MainActivity.TAG, "Add rejected portal: " + portal.getName());
        String dateSubmitted = dateFormatter.format(portal.getDateSubmitted());
        String dateResponded = dateFormatter.format(portal.getDateResponded());
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
     * @param portal The portal getting stored in the database
     */
    public void addPortalSubmission(PortalSubmission portal) {
        Log.d(MainActivity.TAG, "Add portal submission: " + portal.getName());
        String dateSubmitted = dateFormatter.format(portal.getDateSubmitted());
        ContentValues values = new ContentValues();
        // Values put!
        values.put(KEY_NAME, portal.getName());
        values.put(KEY_DATE_SUBMITTED, dateSubmitted);
        values.put(KEY_PICTURE_URL, portal.getPictureURL());
        addPortal(TABLE_PENDING, values);
    }

    /**
     * Add some values to a table in the database.
     * @param table Table key in the database
     * @param values Values to be inserted
     */
    private void addPortal(String table, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long result = db.insert(table, null, values);
        if (result == -1)
            Log.e(MainActivity.TAG, values.get(KEY_PICTURE_URL) + " NOT UNIQUE");
        db.close();
    }

    /**
     * Check if the database contains an accepted portal submission
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return true if the database contains the portal, otherwise false
     */
    public boolean containsAccepted(String portalPictureURL) {
        return getAcceptedPortal(portalPictureURL) != null;
    }

    /**
     * Check if the database contains a pending portal submission
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return true if the database contains the portal, otherwise false
     */
    public boolean containsPending(String portalPictureURL) {
        return getPendingPortal(portalPictureURL) != null;
    }

    /**
     * Check if the database contains a rejected portal submission
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return true if the database contains the portal, otherwise false
     */
    public boolean containsRejected(String portalPictureURL) {
        return getRejectedPortal(portalPictureURL) != null;
    }

    /**
     * Create a table in the database
     * @param tableName The name of the table
     * @param optionalKeys Any keys in addition to the 3 default keys in the table
     * @return A MySQL command string that creates a table for storing PortalSubmissions
     */
    private String createTable(String tableName, String optionalKeys) {
        String createCommand = "CREATE TABLE " + tableName + " ( " + KEY_NAME + " TEXT NOT NULL, " +
                KEY_DATE_SUBMITTED + " DATETIME NOT NULL, " + KEY_PICTURE_URL + " TEXT NOT NULL UNIQUE";
        if (!optionalKeys.equals(""))
            createCommand = createCommand + ", " + optionalKeys;
        return createCommand + ", PRIMARY KEY (" + KEY_PICTURE_URL + ") );";
    }

    /**
     * Creates a string containing the additional keys for the table of accepted portals
     * @return the additional keys for the table of accepted portals
     */
    private String createTableAccepted() {
        return KEY_DATE_RESPONDED + " DATETIME, " + KEY_LIVE_ADDRESS + " TEXT, " +
                KEY_INTEL_LINK_URL + " TEXT ";
    }

    /**
     * Creates a string containing the additional keys for the table of rejected portals
     * @return the additional keys for the table of rejected portals
     */
    private String createTableRejected() {
        return KEY_DATE_RESPONDED + " DATETIME, " + KEY_REJECTION_REASON + " TEXT ";
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
     * Remove an accepted portal submission
     * @param portal The accepted submission being removed from the database
     */
    public void deleteAccepted(PortalAccepted portal) {
        Log.d(MainActivity.TAG, "Remove accepted portal: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACCEPTED, KEY_PICTURE_URL + " = ?", new String[]{portal.getPictureURL()});
        db.close();
    }

    /**
     * Remove a pending portal submission
     * @param portal The pending submission being removed from the database
     */
    public void deletePending(PortalSubmission portal) {
        Log.d(MainActivity.TAG, "Remove portal submission: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PENDING, KEY_PICTURE_URL + " = ?", new String[]{portal.getPictureURL()});
        db.close();
    }

    /**
     * Remove a rejected portal submission
     * @param portal The rejected submission being removed from the database
     */
    public void deleteRejected(PortalRejected portal) {
        Log.d(MainActivity.TAG, "Remove rejected portal: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REJECTED, KEY_PICTURE_URL + " = ?", new String[]{portal.getPictureURL()});
        db.close();
    }

    /**
     * Get an accepted portal submission from the database
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalAccepted representation of an accepted portal in the database
     */
    public PortalAccepted getAcceptedPortal(String portalPictureURL) {
        SQLiteDatabase db = getReadableDatabase();
        PortalAcceptedBuilder b = new PortalAcceptedBuilder(dateFormatter, db);
        PortalAccepted p = b.getPortal(KEY_PICTURE_URL + " = ?", new String[]{portalPictureURL});
        db.close();
        return p;
    }

    /**
     * Get a Vector of all accepted portal submissions
     * @return Vector of all accepted portal submissions
     */
    public Vector<PortalAccepted> getAllAccepted() {
        Log.d(MainActivity.TAG, "Get all accepted portals");
        SQLiteDatabase db = getReadableDatabase();
        PortalAcceptedBuilder b = new PortalAcceptedBuilder(dateFormatter, db);
        Vector<PortalAccepted> portals = b.getPortals(null, null);
        db.close();
        return portals;
    }

    /**
     * Get a Vector of all accepted portals which went live after a certain date
     * @param fromDate Date to start searching from
     * @return Vector of all accepted portals which went live after a certain date
     */
    public Vector<PortalAccepted> getAllAcceptedByResponseDate(Date fromDate) {
        return getAllAcceptedByResponseDate(fromDate, Calendar.getInstance().getTime());
    }

    /**
     * Get a Vector of all accepted portals which went live in between a range of days
     * @param fromDate Date to start searching from
     * @param toDate Date to stop searching at
     * @return Vector of all accepted portals which went live in between a range of days
     */
    public Vector<PortalAccepted> getAllAcceptedByResponseDate(Date fromDate, Date toDate) {
        return getAllAcceptedByDate(KEY_DATE_RESPONDED, fromDate, toDate);
    }

    /**
     * Get a Vector of all accepted portals which were submitted after a certain date
     * @param fromDate Date to start searching from
     * @return Vector of all accepted portals which were submitted after a certain date
     */
    public Vector<PortalAccepted> getAllAcceptedBySubmissionDate(Date fromDate) {
        return getAllAcceptedBySubmissionDate(fromDate, Calendar.getInstance().getTime());
    }

    /**
     * Get a Vector of all accepted portals which were submitted in between a range of days
     * @param fromDate Date to start searching from
     * @param toDate Date to stop searching at
     * @return Vector of all accepted portals which were submitted in between a range of days
     */
    public Vector<PortalAccepted> getAllAcceptedBySubmissionDate(Date fromDate, Date toDate) {
        return getAllAcceptedByDate(KEY_DATE_SUBMITTED, fromDate, toDate);
    }

    /**
     * Helper function getAllAcceptedBySubmissionDate and getAllAcceptedBy
     * @param dateKey Database key used for searching. Can be either KEY_DATE_SUBMITTED or
     *                KEY_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate Date to stop searching at
     * @return Vector of accepted portals which were either submitted or approved from fromDate to
     *         toDate
     */
    private Vector<PortalAccepted> getAllAcceptedByDate(String dateKey, Date fromDate, Date toDate) {
        Log.d(MainActivity.TAG, "Getting all accepted portals within date range");
        SQLiteDatabase db = getReadableDatabase();
        PortalAcceptedBuilder b = new PortalAcceptedBuilder(dateFormatter, db);
        Vector<PortalAccepted> portals = b.getPortalsByDate(dateKey, fromDate, toDate);
        db.close();
        return portals;
    }

    /**
     * Get all portals from the database
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
     * @return all portals after date from the database
     */
    public Vector<PortalSubmission> getAllPortalsFromDate(Date fromDate) {
        Vector<PortalSubmission> portals = new Vector<>();
        portals.addAll(getAllPendingByDate(fromDate));
        portals.addAll(getAllAcceptedByResponseDate(fromDate));
        portals.addAll(getAllRejectedByResponseDate(fromDate));
        return portals;
    }


    /**
     * Just to prove I could. For the love of Cthulhu don't call this ever. Please.
     *
     * Theoretically this would replace getAllAcceptedByDate, getAllPendingByDate and
     * getAllRejectedByDate. This follows the DRY (Don't Repeat Yourself) programming paradigm and
     * makes testing and debugging easier. If you have to make a change to one of the methods this
     * replaces, you have to be sure to change all of them. With this method you only have to change
     * it here, once. However, it breaks the KISS (Keep It Simple Stupid) programming paradigm, and
     * it's basically impossible for anyone else to figure out what the hell is going on here.
     *
     * For example, getAllAcceptedBySubmissionDate would instead call:
     *      getAllPortalsByDate(PortalAcceptedBuilder.class, KEY_DATE_SUBMITTED, fromDate, toDate);
     * getAllPendingByDate would call:
     *      getAllPortalsByDate(PortalSubmissionBuilder.class, KEY_DATE_SUBMITTED, fromDate, toDate);
     * and getAllRejectedByResponseDate would call:
     *      getAllPortalsByDate(PortalRejectedBuilder.class, KEY_DATE_RESPONDED, fromDate, toDate);
     *
     * @param c Class of PortalBuilder to instantiate
     * @param dateKey Database key used for searching. Can be either KEY_DATE_SUBMITTED or
     *                KEY_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate Date to stop searching at
     * @param <P> Subclass of PortalSubmission being returned
     * @return Vector of portals (either accepted, pending, or rejected) which were either submitted
     * or approved/rejected in between fromDate and toDate
     */
    private <P extends PortalSubmission> Vector<P> getAllPortalsByDate(Class<PortalBuilder<P>> c, String dateKey, Date fromDate, Date toDate) {
        SQLiteDatabase db = getReadableDatabase();
        Vector<P> portals = null;
        try {
            Constructor<PortalBuilder<P>> ctor = c.getDeclaredConstructor(SimpleDateFormat.class, SQLiteDatabase.class);
            PortalBuilder<P> b = ctor.newInstance(dateFormatter, db);
            portals = b.getPortalsByDate(dateKey, fromDate, toDate);
        } catch (NoSuchMethodException e) {
            Log.e(MainActivity.TAG, "Couldn't get constructor\n" + e.toString());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e(MainActivity.TAG, "Couldn't instantiate PortalBuilder\n" + e.toString());
        }
        db.close();
        return portals;
    }

    /**
     * Gets a Vector of all pending portal submissions
     * @return Vector of all pending portal submissions
     */
    public Vector<PortalSubmission> getAllPending() {
        Log.d(MainActivity.TAG, "Get all pending portals");
        SQLiteDatabase db = getReadableDatabase();
        PortalSubmissionBuilder b = new PortalSubmissionBuilder(dateFormatter, db);
        Vector<PortalSubmission> portals = b.getPortals(null, null);
        db.close();
        return portals;
    }

    /**
     * Get all pending portal submissions which were submitted between fromDate and toDate
     * @param fromDate Date to start searching from
     * @param toDate Date to stop searching at
     * @return Vector of pending portals which were submitted between fromDate and toDate
     */
    public Vector<PortalSubmission> getAllPendingByDate(Date fromDate, Date toDate) {
        Log.d(MainActivity.TAG, "Getting all pending portals in a date range");
        SQLiteDatabase db = getReadableDatabase();
        PortalSubmissionBuilder b = new PortalSubmissionBuilder(dateFormatter, db);
        Vector<PortalSubmission> portals = b.getPortalsByDate(KEY_DATE_SUBMITTED, fromDate, toDate);
        db.close();
        return portals;
    }

    /**
     * Get all pending portal submissions which were submitted since fromDate
     * @param fromDate Date to start searching from
     * @return Vector of all pending portals which were submitted after a fromDate
     */
    public Vector<PortalSubmission> getAllPendingByDate(Date fromDate) {
        return getAllPendingByDate(fromDate, Calendar.getInstance().getTime());
    }

    /**
     * Gets a Vector of all rejected portal submissions
     * @return Vector of all rejected portal submissions
     */
    public Vector<PortalRejected> getAllRejected() {
        Log.d(MainActivity.TAG, "Get all rejected portals");
        SQLiteDatabase db = getReadableDatabase();
        PortalRejectedBuilder b = new PortalRejectedBuilder(dateFormatter, db);
        Vector<PortalRejected> portals = b.getPortals(null, null);
        db.close();
        return portals;
    }

    /**
     * Get a Vector of all rejected portals which went live after a certain date
     * @param fromDate Date to start searching from
     * @return Vector of all rejected portals which went live after a certain date
     */
    public Vector<PortalRejected> getAllRejectedByResponseDate(Date fromDate) {
        return getAllRejectedByResponseDate(fromDate, Calendar.getInstance().getTime());
    }

    /**
     * Get a Vector of all rejected portals which went live in between a range of days
     * @param fromDate Date to start searching from
     * @param toDate Date to stop searching at
     * @return Vector of all rejected portals which went live in between a range of days
     */
    public Vector<PortalRejected> getAllRejectedByResponseDate(Date fromDate, Date toDate) {
        return getAllRejectedByDate(KEY_DATE_RESPONDED, fromDate, toDate);
    }

    /**
     * Get a Vector of all rejected portals which were submitted after a certain date
     * @param fromDate Date to start searching from
     * @return Vector of all rejected portals which were submitted after a certain date
     */
    public Vector<PortalRejected> getAllRejectedBySubmissionDate(Date fromDate) {
        return getAllRejectedBySubmissionDate(fromDate, Calendar.getInstance().getTime());
    }

    /**
     * Get a Vector of all rejected portals which were submitted in between a range of days
     * @param fromDate Date to start searching from
     * @param toDate Date to stop searching at
     * @return Vector of all rejected portals which were submitted in between a range of days
     */
    public Vector<PortalRejected> getAllRejectedBySubmissionDate(Date fromDate, Date toDate) {
        return getAllRejectedByDate(KEY_DATE_SUBMITTED, fromDate, toDate);
    }

    /**
     * Helper function getAllAcceptedBySubmissionDate and getAllAcceptedBy
     * @param dateKey Database key used for searching. Can be either KEY_DATE_SUBMITTED or
     *                KEY_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate Date to stop searching at
     * @return Vector of rejected portals which were either submitted or approved from fromDate to
     *         toDate.
     */
    private Vector<PortalRejected> getAllRejectedByDate(String dateKey, Date fromDate, Date toDate) {
        Log.d(MainActivity.TAG, "Getting all rejected portals within date range");
        SQLiteDatabase db = getReadableDatabase();
        PortalRejectedBuilder b = new PortalRejectedBuilder(dateFormatter, db);
        Vector<PortalRejected> portals = b.getPortalsByDate(dateKey, fromDate, toDate);
        db.close();
        return portals;
    }

    /**
     * Get the number of approved portals
     * @return number of approved portals
     */
    public long getAcceptedCount() {
        return getTableSize(TABLE_ACCEPTED);
    }

    /**
     * Get the number of pending portals
     * @return number of pending portals
     */
    public long getPendingCount() {
        return getTableSize(TABLE_PENDING);
    }

    /**
     * Get the number of rejected portals
     * @return number of rejected portals
     */
    public long getRejectedCount() {
        return getTableSize(TABLE_REJECTED);
    }

    /**
     * Get the number of entries in a table
     * @param table Table to count rows on
     * @return number of entries in a table
     */
    private long getTableSize(String table) {
        SQLiteDatabase db = getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, table);
        db.close();
        return count;
    }

    /**
     * Get the total number of portals in the database
     * @return the total number of portals in the database
     */
    public long getDatabaseSize() {
        return getAcceptedCount() + getPendingCount() + getRejectedCount();
    }

    /**
     * Get a pending portal submission from the database
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalSubmission representation of a pending portal in the database
     */
    public PortalSubmission getPendingPortal(String portalPictureURL) {
        Log.d(MainActivity.TAG, "Getting pending portal");
        SQLiteDatabase db = getReadableDatabase();
        PortalSubmissionBuilder b = new PortalSubmissionBuilder(dateFormatter, db);
        PortalSubmission p = b.getPortal(KEY_PICTURE_URL + " = ?", new String[]{portalPictureURL});
        db.close();
        return p;
    }

    /**
     * Get a rejected portal submission from the database
     * @param portalPictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalRejected representation of a rejected portal in the database
     */
    public PortalRejected getRejectedPortal(String portalPictureURL) {
        Log.d(MainActivity.TAG, "Getting rejected portal");
        SQLiteDatabase db = getReadableDatabase();
        PortalRejectedBuilder b = new PortalRejectedBuilder(dateFormatter, db);
        PortalRejected p = b.getPortal(KEY_PICTURE_URL + " = ?", new String[]{portalPictureURL});
        db.close();
        return p;
    }

    /**
     * Create tables in the database
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
     * @param db A reference to the SQLiteDatabase object used by the app
     * @param oldVersion The current version of the database
     * @param newVersion The new version of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Don't need this yet
    }

    /**
     * Update a database entry for an accepted portal
     * @param portal PortalAccepted containing the new information
     * @param oldPortal PortalAccepted containing the old information to be updated
     */
    public void updateAccepted(PortalAccepted portal, PortalAccepted oldPortal) {
        Log.d(MainActivity.TAG, "Update portal: " + oldPortal.getName());
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
                  new String[] { String.valueOf(oldPortal.getPictureURL()) });

        db.close();
    }

    /**
     * Update a database entry for a pending portal
     * @param portal PortalSubmission containing the new information
     * @param oldPortal PortalSubmission containing the old information to be updated
     */
    public void updatePending(PortalSubmission portal, PortalSubmission oldPortal) {
        Log.d(MainActivity.TAG, "Update portal: " + oldPortal.getName());
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
     * @param portal PortalRejected containing the new information
     * @param oldPortal PortalRejected containing the old information to be updated
     */
    public void updateRejected(PortalRejected portal, PortalRejected oldPortal) {
        Log.d(MainActivity.TAG, "Update portal: " + oldPortal.getName());
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
