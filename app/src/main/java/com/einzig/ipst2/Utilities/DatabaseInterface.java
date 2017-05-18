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

package com.einzig.ipst2.Utilities;

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
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

/**
 * @author Ryan Porterfield
 * @since 2017-05-18
 */
public class DatabaseInterface extends SQLiteOpenHelper {
    /** Database version */
    private static final int DATABASE_VERSION = 0;
    //TODO (Ryan): Replace hardcoded keys with string resources
    /** Database name */
    private static final String DATABASE_NAME = "IPSTSubmissionDB";
    /** Table key for portal name */
    private static final String KEY_NAME = "name";
    /** Table key for the date the portal was submitted */
    private static final String KEY_DATE_SUBMITTED = "dateSubmitted";
    /** Table key for the date Niantic approved or denied the portal */
    private static final String KEY_DATE_RESPONDED = "dateResponded";
    /** Table key for address of the portal */
    private static final String KEY_LIVE_ADDRESS = "liveAddress";
    /** Table key for the link to the portal on the intel map */
    private static final String KEY_INTEL_LINK_URL = "intelLinkURL";
    /** Table key for the reason the portal was rejected */
    private static final String KEY_REJECTION_REASON = "rejectionReason";
    /** Table key for the URL to the submission picture */
    private static final String KEY_PICTURE_URL = "pictureURL";
    /** The name of the table in the first version of the database */
    private static final String LEGACY_TABLE_SUBMISSIONS = "submissions";
    /** The name of the table in the second version of the database */
    private static final String LEGACY_TABLE_PORTAL_SUBMISSIONS = "portalSubmissions";
    /** The name of the table in containing accepted portal submissions */
    private static final String TABLE_ACCEPTED = "acceptedSubmissions";
    /** The name of the table in containing pending portal submissions */
    private static final String TABLE_PENDING = "pendingSubmissions";
    /** The name of the table in containing rejected portal submissions */
    private static final String TABLE_REJECTED = "RejectedSubmissions";
    /** Tag for logging */
    private static final String TAG = "IPST:Database";

    /** The date format that MySQL stores DATETIME objects in */
    private SimpleDateFormat mySQLDateFormat;

    public DatabaseInterface(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mySQLDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    }

    /**
     * Insert a PortalAccepted into the database.
     * @param portal The portal getting stored in the database.
     */
    public void addPortalAccepted(PortalAccepted portal) {
        Log.d(TAG, "Add accepted portal: " + portal.getName());
        String dateSubmitted = dateToString(portal.getDateSubmitted());
        String dateResponded = dateToString(portal.getDateResponded());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        // Values put!
        values.put(KEY_NAME, portal.getName());
        values.put(KEY_DATE_SUBMITTED, dateSubmitted);
        values.put(KEY_PICTURE_URL, portal.getPictureURL());
        values.put(KEY_DATE_RESPONDED, dateResponded);
        values.put(KEY_LIVE_ADDRESS, portal.getLiveAddress());
        values.put(KEY_INTEL_LINK_URL, portal.getIntelLinkURL());
        db.insert(TABLE_ACCEPTED, null, values);
        db.close();
    }

    /**
     * Insert a PortalRejected into the database.
     * @param portal The portal getting stored in the database.
     */
    public void addPortalRejected(PortalRejected portal) {
        Log.d(TAG, "Add rejected portal: " + portal.getName());
        String dateSubmitted = dateToString(portal.getDateSubmitted());
        String dateResponded = dateToString(portal.getDateResponded());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        // Values put!
        values.put(KEY_NAME, portal.getName());
        values.put(KEY_DATE_SUBMITTED, dateSubmitted);
        values.put(KEY_PICTURE_URL, portal.getPictureURL());
        values.put(KEY_DATE_RESPONDED, dateResponded);
        values.put(KEY_REJECTION_REASON, portal.getRejectionReason());
        db.insert(TABLE_REJECTED, null, values);
        db.close();
    }

    /**
     * Insert a PortalSubmission into the database.
     * @param portal The portal getting stored in the database.
     */
    public void addPortalSubmission(PortalSubmission portal){
        Log.d(TAG, "Add portal submission: " + portal.getName());
        String dateSubmitted = dateToString(portal.getDateSubmitted());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        // Values put!
        values.put(KEY_NAME, portal.getName());
        values.put(KEY_DATE_SUBMITTED, dateSubmitted);
        values.put(KEY_PICTURE_URL, portal.getPictureURL());
        db.insert(TABLE_PENDING, null, values);
        db.close(); 
    }

    public boolean containsAccepted(String portalPictureURL) {
        return getAcceptedPortal(portalPictureURL) != null;
    }

    public boolean containsPending(String portalPictureURL) {
        return getPendingPortal(portalPictureURL) != null;
    }

    public boolean containsRejected(String portalPictureURL) {
        return getRejectedPortal(portalPictureURL) != null;
    }

    private PortalAccepted createPortalAccepted(Cursor cursor) {
        String name, pictureURL, location, intelLink;
        Date submitted, responded;
        name = cursor.getString(0);
        submitted = parseDate(cursor.getString(1));
        pictureURL = cursor.getString(2);
        responded = parseDate(cursor.getString(3));
        location = cursor.getString(4);
        intelLink = cursor.getString(5);
        return new PortalAccepted(name, submitted, pictureURL, responded, location, intelLink);
    }

    private PortalSubmission createPortalPending(Cursor cursor) {
        String name, pictureURL;
        Date dateSubmitted;
        name = cursor.getString(0);
        dateSubmitted = parseDate(cursor.getString(1));
        pictureURL = cursor.getString(2);
        return new PortalSubmission(name, dateSubmitted, pictureURL);
    }

    private PortalRejected createPortalRejected(Cursor cursor) {
        String name, pictureURL, reason;
        Date submitted, responded;
        name = cursor.getString(0);
        submitted = parseDate(cursor.getString(1));
        pictureURL = cursor.getString(2);
        responded = parseDate(cursor.getString(3));
        reason = cursor.getString(4);
        return new PortalRejected(name, submitted, pictureURL, responded, reason);
    }

    /**
     * Create a table in the database
     * @param tableName The name of the table.
     * @param optionalKeys Any keys in addition to the 3 default keys in the table.
     * @return A MySQL command string that creates a table for storing PortalSubmissions.
     */
    private String createTable(String tableName, String optionalKeys) {
        String createCommand = "CREATE TABLE " + tableName + " ( " + KEY_NAME + " TEXT NOT NULL, " +
                KEY_DATE_SUBMITTED + " DATETIME NOT NULL, " + KEY_PICTURE_URL + " TEXT NOT NULL UNIQUE";
        if (!optionalKeys.equals(""))
            createCommand = createCommand + ", " + optionalKeys;
        return createCommand + ", PRIMARY KEY (" + KEY_PICTURE_URL + ") );";
    }

    /**
     * Creates a string containing the additional keys for the table of accepted portals.
     * @return the additional keys for the table of accepted portals.
     */
    private String createTableAccepted() {
        return KEY_DATE_RESPONDED + " DATETIME, " + KEY_LIVE_ADDRESS + " TEXT, " +
                KEY_INTEL_LINK_URL + " TEXT ";
    }

    /**
     * Creates a string containing the additional keys for the table of rejected portals.
     * @return the additional keys for the table of rejected portals.
     */
    private String createTableRejected() {
        return KEY_DATE_RESPONDED + " DATETIME, " + KEY_REJECTION_REASON + " TEXT ";
    }

    /**
     * Parse a Date object to a formatted String.
     * @param date The Date object being written to a string.
     * @return A formatted string representation of the date.
     */
    private String dateToString(Date date) {
        return mySQLDateFormat.format(date);
    }

    public void delete(PortalSubmission portal) {
        if (portal instanceof PortalAccepted)
            deleteAccepted((PortalAccepted) portal);
        else if (portal instanceof  PortalRejected)
            deleteRejected((PortalRejected) portal);
        else
            deletePending(portal);
    }

    /**
     * Clear the database.
     */
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PENDING, null, null);
        db.delete(TABLE_ACCEPTED, null, null);
        db.delete(TABLE_REJECTED, null, null);
        db.close();
    }

    /**
     * Remove an accepted portal submission.
     * @param portal The accepted submission being removed from the database.
     */
    public void deleteAccepted(PortalAccepted portal) {
        Log.d(TAG, "Remove accepted portal: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACCEPTED, KEY_PICTURE_URL + " = ?", new String[]{portal.getPictureURL()});
        db.close();
    }

    /**
     * Remove a pending portal submission.
     * @param portal The pending submission being removed from the database.
     */
    public void deletePending(PortalSubmission portal) {
        Log.d(TAG, "Remove portal submission: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PENDING, KEY_PICTURE_URL + " = ?", new String[]{portal.getPictureURL()});
        db.close();
    }

    /**
     * Remove a rejected portal submission.
     * @param portal The rejected submission being removed from the database.
     */
    public void deleteRejected(PortalRejected portal) {
        Log.d(TAG, "Remove rejected portal: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REJECTED, KEY_PICTURE_URL + " = ?", new String[]{portal.getPictureURL()});
        db.close();
    }

    public PortalAccepted getAcceptedPortal(String portalPictureURL) {
        PortalAccepted portal;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ACCEPTED, null,
                KEY_PICTURE_URL + " = ?", new String[]{portalPictureURL}, null, null, null, null);
        if (cursor.getCount() < 1)    // Portal doesn't exist
            return null;
        cursor.moveToFirst();
        portal = createPortalAccepted(cursor);
        cursor.close();
        db.close();
        return portal;
    }

    /**
     * Gets an ArrayList of all accepted portal submissions.
     * @return a list of all accepted portal submissions.
     */
    public Vector<PortalAccepted> getAllAccepted() {
        Log.d(TAG, "Get all accepted portals");
        Vector<PortalAccepted> portals = new Vector<>();
        String query = "SELECT * FROM " + TABLE_ACCEPTED;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() < 1)    // return empty list
            return portals;
        cursor.moveToFirst();
        do {
            portals.add(createPortalAccepted(cursor));
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return portals;
    }

    public Vector<PortalSubmission> getAllPortals() {
        Vector<PortalSubmission> portals = getAllPending();
        portals.addAll(getAllAccepted());
        portals.addAll(getAllRejected());
        return portals;
    }

    /**
     * Gets an ArrayList of all pending portal submissions.
     * @return a list of all pending portal submissions.
     */
    public Vector<PortalSubmission> getAllPending() {
        Log.d(TAG, "Get all pending portals");
        Vector<PortalSubmission> submissions = new Vector<>();
        String query = "SELECT  * FROM " + TABLE_PENDING;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() < 1)    // return empty list
            return submissions;
        cursor.moveToFirst();
        do {
            submissions.add(createPortalPending(cursor));
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return submissions;
    }

    /**
     * Gets an ArrayList of all rejected portal submissions.
     * @return a list of all rejected portal submissions.
     */
    public Vector<PortalRejected> getAllRejected() {
        Log.d(TAG, "Get all rejected portals");
        Vector<PortalRejected> portals = new Vector<>();
        String query = "SELECT * FROM " + TABLE_REJECTED;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() < 1)    // return empty list
            return portals;
        cursor.moveToFirst();
        do {
            portals.add(createPortalRejected(cursor));
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return portals;
    }

    public long getDatabaseSize() {
        SQLiteDatabase db = getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_ACCEPTED);
        count += DatabaseUtils.queryNumEntries(db, TABLE_PENDING);
        count += DatabaseUtils.queryNumEntries(db, TABLE_REJECTED);
        return count;
    }

    public PortalSubmission getPendingPortal(String portalPictureURL) {
        Log.d(TAG, "Getting pending portal");
        PortalSubmission portal;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PENDING, null,
                KEY_PICTURE_URL + " = ?", new String[]{portalPictureURL}, null, null, null, null);
        if (cursor.getCount() < 1)    // Portal doesn't exist
            return null;
        cursor.moveToFirst();
        portal = createPortalPending(cursor);
        cursor.close();
        db.close();
        return portal;
    }

    public PortalRejected getRejectedPortal(String portalPictureURL) {
        PortalRejected portal;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ACCEPTED, null,
                KEY_PICTURE_URL + " = ?", new String[]{portalPictureURL}, null, null, null, null);
        if (cursor.getCount() < 1)    // Portal doesn't exist
            return null;
        cursor.moveToFirst();
        portal = createPortalRejected(cursor);
        cursor.close();
        db.close();
        return portal;
    }

    /**
     * Create tables in the database.
     * @param db A reference to the SQLiteDatabase object used by the app.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable(TABLE_PENDING, ""));
        db.execSQL(createTable(TABLE_ACCEPTED, createTableAccepted()));
        db.execSQL(createTable(TABLE_REJECTED, createTableRejected()));
    }

    /**
     * Delete the old tables from the database and create the new tables.
     *
     * @param db A reference to the SQLiteDatabase object used by the app.
     * @param oldVersion The current version of the database.
     * @param newVersion The new version of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS " + LEGACY_TABLE_SUBMISSIONS + ";");
        db.execSQL("DROP TABLE IF EXISTS " + LEGACY_TABLE_PORTAL_SUBMISSIONS + ";");

        // create fresh books table
        this.onCreate(db);
    }

    /**
     * Parse a String to a Date.
     * @param dateString The date in ISO format yyyy-MM-dd HH:mm:ss
     * @return a Date object representing the date that dateString contained.
     */
    private Date parseDate(String dateString) {
        Date parse;
        try {
            parse = mySQLDateFormat.parse(dateString);
        } catch (ParseException e) {
            parse = new Date();
        }
        return parse;
    }

    public void updateAccepted(PortalAccepted portal, PortalAccepted oldPortal) {
        Log.d(TAG, "Update portal: " + oldPortal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String dateSubmitted = dateToString(portal.getDateSubmitted());
        String dateResponded = dateToString(portal.getDateResponded());

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

    public void updatePending(PortalSubmission portal, PortalSubmission oldPortal) {
        Log.d(TAG, "Update portal: " + oldPortal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, portal.getName());
        values.put(KEY_DATE_SUBMITTED, mySQLDateFormat.format(portal.getDateSubmitted()));
        values.put(KEY_PICTURE_URL, portal.getPictureURL());

        db.update(TABLE_PENDING, values, KEY_PICTURE_URL + " = ?",
                new String[]{String.valueOf(oldPortal.getPictureURL())});
        db.close();
    }

    public void updateRejected(PortalRejected portal, PortalRejected oldPortal) {
        Log.d(TAG, "Update portal: " + oldPortal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String dateSubmitted = dateToString(portal.getDateSubmitted());
        String dateResponded = dateToString(portal.getDateResponded());

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
