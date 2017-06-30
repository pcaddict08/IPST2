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

import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.LogEntry;
import com.einzig.ipst2.util.Logger;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static com.einzig.ipst2.database.AcceptedPortalContract.AcceptedPortalEntry.COLUMN_INTEL_LINK_URL;
import static com.einzig.ipst2.database.AcceptedPortalContract.AcceptedPortalEntry.COLUMN_LIVE_ADDRESS;
import static com.einzig.ipst2.database.AcceptedPortalContract.AcceptedPortalEntry.TABLE_ACCEPTED;
import static com.einzig.ipst2.database.LoggingContract.LoggingEntry.COLUMN_LOG_LEVEL;
import static com.einzig.ipst2.database.LoggingContract.LoggingEntry.COLUMN_LOG_MESSAGE;
import static com.einzig.ipst2.database.LoggingContract.LoggingEntry.COLUMN_LOG_SCOPE;
import static com.einzig.ipst2.database.LoggingContract.LoggingEntry.COLUMN_LOG_TIME;
import static com.einzig.ipst2.database.LoggingContract.LoggingEntry.TABLE_LOGGING;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_DATE_RESPONDED;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_DATE_SUBMITTED;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_NAME;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_PICTURE_URL;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.TABLE_PENDING;
import static com.einzig.ipst2.database.RejectedPortalContract.RejectedPortalEntry.COLUMN_REJECTION_REASON;
import static com.einzig.ipst2.database.RejectedPortalContract.RejectedPortalEntry.TABLE_REJECTED;

/**
 * @author Ryan Porterfield
 * @since 2017-05-18
 */
public class DatabaseInterface extends SQLiteOpenHelper {
    /** The date format that MySQL stores DATETIME objects in */
    static final public DateTimeFormatter DATE_FORMATTER;
    /** Database name */
    static final private String DATABASE_NAME;
    /** Database version */
    static final private int DATABASE_VERSION;

    static {
        DATE_FORMATTER = ISODateTimeFormat.date();
        DATABASE_NAME = "IPSTSubmissionDB";
        DATABASE_VERSION = 1;
    }

    /**
     * Create a new DatabaseInterface to interact with the SQLite database for the application
     *
     * @param context Context used by super class
     */
    public DatabaseInterface(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void addLog(LogEntry log) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOG_LEVEL, log.getLevel());
        values.put(COLUMN_LOG_TIME, DATE_FORMATTER.print(log.getTime()));
        values.put(COLUMN_LOG_SCOPE, log.getScope());
        values.put(COLUMN_LOG_MESSAGE, log.getMessage());
        if (db.isOpen()) {
            db.insert(TABLE_LOGGING, null, values);
            db.close();
        }
    }

    /**
     * Add some values to a table in the database.
     *
     * @param table  Table key in the database
     * @param values Values to be inserted
     */
    private void addPortal(String table, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        db.insert(table, null, values);
        db.close();
    }

    /**
     * Insert a PortalAccepted into the database
     *
     * @param portal The portal getting stored in the database
     */
    public void addPortalAccepted(PortalAccepted portal) {
        Logger.d("Add accepted portal: " + portal.getName());
        if (portal.getDateSubmitted() == null)
            portal.setDateSubmitted(portal.getDateResponded());
        String dateSubmitted = getDateStringSafe(portal.getDateSubmitted());
        String dateResponded = getDateStringSafe(portal.getDateResponded());
        ContentValues values = new ContentValues();
        // Values put!
        Logger.v("DBI#addPortalAccepted", "Name: " + portal.getName() + "\tSubmitted: " +
                dateSubmitted + "\tResponded: " + dateResponded);
        values.put(COLUMN_NAME, portal.getName());
        values.put(COLUMN_DATE_SUBMITTED, dateSubmitted);
        values.put(COLUMN_PICTURE_URL, portal.getPictureURL());
        values.put(COLUMN_DATE_RESPONDED, dateResponded);
        values.put(COLUMN_LIVE_ADDRESS, portal.getLiveAddress());
        values.put(COLUMN_INTEL_LINK_URL, portal.getIntelLinkURL());
        addPortal(TABLE_ACCEPTED, values);
    }

    /**
     * Insert a PortalRejected into the database
     *
     * @param portal The portal getting stored in the database
     */
    public void addPortalRejected(PortalRejected portal) {
        Logger.d("Add rejected portal: " + portal.getName());
        String dateSubmitted = getDateStringSafe(portal.getDateSubmitted());
        String dateResponded = getDateStringSafe(portal.getDateResponded());
        ContentValues values = new ContentValues();
        // Values put!
        Logger.v("DBI#addPortalRejected", "Name: " + portal.getName() + "\tSubmitted: " +
                dateSubmitted + "\tResponded: " + dateResponded);
        values.put(COLUMN_NAME, portal.getName());
        values.put(COLUMN_DATE_SUBMITTED, dateSubmitted);
        values.put(COLUMN_PICTURE_URL, portal.getPictureURL());
        values.put(COLUMN_DATE_RESPONDED, dateResponded);
        values.put(COLUMN_REJECTION_REASON, portal.getRejectionReason());
        addPortal(TABLE_REJECTED, values);
    }

    /**
     * Insert a PortalSubmission into the database
     *
     * @param portal The portal getting stored in the database
     */
    public void addPortalSubmission(PortalSubmission portal) {
        Logger.d("Add portal submission: " + portal.getName());
        String dateSubmitted = DATE_FORMATTER.print(portal.getDateSubmitted());
        ContentValues values = new ContentValues();
        // Values put!
        Logger.v("DBI#addPortalSubmission", "Name: " + portal.getName());
        values.put(COLUMN_NAME, portal.getName());
        values.put(COLUMN_DATE_SUBMITTED, dateSubmitted);
        Logger.v("DBI#addPortalSubmission", "Submitted: " + dateSubmitted);
        values.put(COLUMN_PICTURE_URL, portal.getPictureURL());
        addPortal(TABLE_PENDING, values);
    }

    /**
     * Remove an accepted portal submission
     *
     * @param portal The accepted submission being removed from the database
     */
    public void deleteAccepted(PortalAccepted portal) {
        Logger.d("Remove accepted portal: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACCEPTED, COLUMN_PICTURE_URL + " = ?",
                new String[]{portal.getPictureURL()});
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

    /*
    * NUKE the database
    * */
    public void nukeAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()) {

            while (!c.isAfterLast()) {
                db.execSQL("DROP TABLE '" + c.getString(c.getColumnIndex("name")) + "'");
                c.moveToNext();
            }
        }
        c.close();
        onCreate(db);
    }

    /**
     * Remove a pending portal submission
     *
     * @param portal The pending submission being removed from the database
     */
    public void deletePending(PortalSubmission portal) {
        Logger.d("Remove portal submission: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PENDING, COLUMN_PICTURE_URL + " = ?", new String[]{portal.getPictureURL()});
        db.close();
    }

    /**
     * Remove a rejected portal submission
     *
     * @param portal The rejected submission being removed from the database
     */
    public void deleteRejected(PortalRejected portal) {
        Logger.d("Remove rejected portal: " + portal.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REJECTED, COLUMN_PICTURE_URL + " = ?",
                new String[]{portal.getPictureURL()});
        db.close();
    }

    /**
     * Get a Vector of all accepted portals which went live after a certain date
     *
     * @param fromDate Date to start searching from
     * @return Vector of all accepted portals which went live after a certain date
     */
    public Vector<PortalAccepted> getAcceptedByResponseDate(LocalDate fromDate) {
        return getAcceptedByResponseDate(fromDate, LocalDate.now());
    }

    /**
     * Get a Vector of all accepted portals which went live in between a range of days
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of all accepted portals which went live in between a range of days
     */
    public Vector<PortalAccepted> getAcceptedByResponseDate(LocalDate fromDate,
            LocalDate toDate) {
        return getAllAcceptedByDate(COLUMN_DATE_RESPONDED, fromDate, toDate);
    }

    /**
     * Get a Vector of all accepted portals which were submitted after a certain date
     *
     * @param fromDate Date to start searching from
     * @return Vector of all accepted portals which were submitted after a certain date
     */
    public Vector<PortalAccepted> getAcceptedBySubmissionDate(LocalDate fromDate) {
        return getAcceptedBySubmissionDate(fromDate, LocalDate.now());
    }

    /**
     * Get a Vector of all accepted portals which were submitted in between a range of days
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of all accepted portals which were submitted in between a range of days
     */
    public Vector<PortalAccepted> getAcceptedBySubmissionDate(LocalDate fromDate,
            LocalDate toDate) {
        return getAllAcceptedByDate(COLUMN_DATE_SUBMITTED, fromDate, toDate);
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
    public long getAcceptedCountByResponseDate(LocalDate fromDate) {
        return getCountByDate(TABLE_ACCEPTED, COLUMN_DATE_RESPONDED, fromDate);
    }

    /**
     * Get the number of portals accepted between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of portals accepted between fromDate and toDate
     */
    public long getAcceptedCountByResponseDate(LocalDate fromDate, LocalDate toDate) {
        return getCountByDate(TABLE_ACCEPTED, COLUMN_DATE_RESPONDED, fromDate, toDate);
    }

    /**
     * Get the number of accepted portals submitted since fromDate
     *
     * @param fromDate Date to start searching from
     * @return number of accepted portals submitted since fromDate
     */
    public long getAcceptedCountBySubmissionDate(LocalDate fromDate) {
        return getCountByDate(TABLE_ACCEPTED, COLUMN_DATE_SUBMITTED, fromDate);
    }

    /**
     * Get the number of accepted portals submitted between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of accepted portals submitted between fromDate and toDate
     */
    public long getAcceptedCountBySubmissionDate(LocalDate fromDate, LocalDate toDate) {
        return getCountByDate(TABLE_ACCEPTED, COLUMN_DATE_SUBMITTED, fromDate, toDate);
    }

    /**
     * Get an accepted portal submission from the database
     *
     * @param pictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalAccepted representation of an accepted portal in the database
     */
    public PortalAccepted getAcceptedPortal(String pictureURL, String portalName) {
        return getPortal(TABLE_ACCEPTED, pictureURL, portalName, new PortalAcceptedBuilder());
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
        cursor.close();
        db.close();
        return portals;
    }

    /**
     * Get a Vector of all accepted portal submissions
     *
     * @return Vector of all accepted portal submissions
     */
    public Vector<PortalAccepted> getAllAccepted() {
        Logger.d("Get all accepted portals");
        return getAll(TABLE_ACCEPTED, new PortalAcceptedBuilder());
    }

    /**
     * Helper function getAcceptedBySubmissionDate and getAllAcceptedBy
     *
     * @param dateKey  Database key used for searching. Can be either COLUMN_DATE_SUBMITTED or
     *                 COLUMN_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of accepted portals which were either submitted or approved from fromDate to
     * toDate
     */
    private Vector<PortalAccepted> getAllAcceptedByDate(String dateKey, LocalDate fromDate,
            LocalDate toDate) {
        Logger.d("Getting all accepted portals within date range");
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
        Logger.d("Get all pending portals");
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
    public Vector<PortalSubmission> getAllPortalsFromDate(LocalDate fromDate) {
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
        Logger.d("Get all rejected portals");
        return getAll(TABLE_REJECTED, new PortalRejectedBuilder());
    }

    /**
     * Get the number of portals in a table since fromDate
     *
     * @param table    Table in the database
     * @param dateKey  Either COLUMN_DATE_SUBMITTED or COLUMN_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @return number of portals in a table since date
     */
    private long getCountByDate(String table, String dateKey, LocalDate fromDate) {
        return getCountByDate(table, dateKey, fromDate, LocalDate.now());
    }

    /**
     * Get the number of portals in a table between fromDate and toDate
     *
     * @param table    Table in the database
     * @param dateKey  Either COLUMN_DATE_SUBMITTED or COLUMN_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of portals in a table between fromDate and toDate
     */
    private long getCountByDate(String table, String dateKey, LocalDate fromDate,
            LocalDate toDate) {
        String fromDateStr = DATE_FORMATTER.print(fromDate);
        String toDateStr = DATE_FORMATTER.print(toDate);

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
    private String getDateStringSafe(LocalDate date) {
        return (date != null) ? DATE_FORMATTER.print(date) : "";
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

    public List<LogEntry> getLogs() {
        SQLiteDatabase db = getReadableDatabase();
        List<LogEntry> logs = new ArrayList<>();
        Cursor c = db.query(TABLE_LOGGING, null, null, null, null, null, null);
        while (c.moveToNext()) {
            String level = c.getString(c.getColumnIndex(COLUMN_LOG_LEVEL));
            LocalDateTime time = DATE_FORMATTER.parseLocalDateTime(
                    c.getString(c.getColumnIndex(COLUMN_LOG_TIME)));
            String scope = c.getString(c.getColumnIndex(COLUMN_LOG_SCOPE));
            String message = c.getString((c.getColumnIndex(COLUMN_LOG_MESSAGE)));
            logs.add(new LogEntry(level, time, scope, message));
        }
        c.close();
        db.close();
        return logs;
    }

    /**
     * Get all pending portal submissions which were submitted between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of pending portals which were submitted between fromDate and toDate
     */
    public Vector<PortalSubmission> getPendingByDate(LocalDate fromDate, LocalDate toDate) {
        Logger.d("Getting all pending portals in a date range");
        return getPortalsByDate(TABLE_PENDING, COLUMN_DATE_SUBMITTED, fromDate, toDate,
                new PortalSubmissionBuilder());
    }

    /**
     * Get all pending portal submissions which were submitted since fromDate
     *
     * @param fromDate Date to start searching from
     * @return Vector of all pending portals which were submitted after a fromDate
     */
    public Vector<PortalSubmission> getPendingByDate(LocalDate fromDate) {
        return getPendingByDate(fromDate, LocalDate.now());
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
    public long getPendingCountByDate(LocalDate fromDate) {
        return getCountByDate(TABLE_PENDING, COLUMN_DATE_SUBMITTED, fromDate);
    }

    /**
     * Get the number of portals that were submitted between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of portals that were submitted between fromDate and toDate
     */
    public long getPendingCountByDate(LocalDate fromDate, LocalDate toDate) {
        return getCountByDate(TABLE_PENDING, COLUMN_DATE_SUBMITTED, fromDate, toDate);
    }

    /**
     * Get a pending portal submission from the database
     *
     * @param pictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalSubmission representation of a pending portal in the database
     */
    public PortalSubmission getPendingPortal(String pictureURL, String portalName) {
        Logger.d("Getting pending portal");
        return getPortal(TABLE_PENDING, pictureURL, portalName, new PortalSubmissionBuilder());
    }

    /**
     * @param table      Database table to search
     * @param pictureURL Unique key for a portal in the database
     * @param builder    PortalBuilder to build the portal from the query
     * @param <P>        Type of PortalSubmission being returned
     * @return a portal from the database
     */
    private <P extends PortalSubmission> P getPortal(String table, String pictureURL,
            String portalName, PortalBuilder<P> builder) {
        Vector<P> portals = getAll(table, COLUMN_PICTURE_URL + " = ? AND " + COLUMN_NAME + " = ?",
                new
                        String[]{pictureURL, portalName},
                builder);
        for (P portal : portals) {
            if (portal.getName().equals(portalName))
                return portal;
            else
                return portal;
        }
        return null;
    }

    /**
     * Get all portals in a range of dates.
     *
     * @param dateKey  Database key used for searching. Can be either COLUMN_DATE_SUBMITTED or
     *                 COLUMN_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of portals which were either submitted or approved from fromDate to
     * toDate.
     * @see PendingPortalContract.PendingPortalEntry#COLUMN_DATE_RESPONDED
     * @see PendingPortalContract.PendingPortalEntry#COLUMN_DATE_SUBMITTED
     */
    private <P extends PortalSubmission> Vector<P> getPortalsByDate(String table, String dateKey,
            LocalDate fromDate, LocalDate toDate,
            PortalBuilder<P> builder) {
        String fromDateStr = DATE_FORMATTER.print(fromDate);
        String toDateStr = DATE_FORMATTER.print(toDate);
        return getAll(table, dateKey + " BETWEEN ? AND ?", new String[]{fromDateStr, toDateStr},
                builder);
    }

    /* Method to check if a portal url is in the db
    *
     * @param dateKey  Database key used for searching. Can be either COLUMN_DATE_SUBMITTED or
     *                 COLUMN_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of portals which were either submitted or approved from fromDate to
     * toDate.
    * */
    public <P extends PortalSubmission> Vector<P> getPortalByURL(String table, String urlKey,
            String url,
            PortalBuilder<P> builder) {
        return getAll(table, urlKey + "=?", new String[]{url},
                builder);
    }

    /**
     * Helper function getAcceptedBySubmissionDate and getAllAcceptedBy
     *
     * @param dateKey  Database key used for searching. Can be either COLUMN_DATE_SUBMITTED or
     *                 COLUMN_DATE_RESPONDED
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of rejected portals which were either submitted or approved from fromDate to
     * toDate.
     */
    private Vector<PortalRejected> getRejectedByDate(String dateKey, LocalDate fromDate,
            LocalDate toDate) {
        Logger.d("Getting all rejected portals within date range");
        return getPortalsByDate(TABLE_REJECTED, dateKey, fromDate, toDate, new
                PortalRejectedBuilder());
    }

    /**
     * Get a Vector of all rejected portals which went live after a certain date
     *
     * @param fromDate Date to start searching from
     * @return Vector of all rejected portals which went live after a certain date
     */
    public Vector<PortalRejected> getRejectedByResponseDate(LocalDate fromDate) {
        return getRejectedByResponseDate(fromDate, LocalDate.now());
    }

    /**
     * Get a Vector of all rejected portals which went live in between a range of days
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of all rejected portals which went live in between a range of days
     */
    public Vector<PortalRejected> getRejectedByResponseDate(LocalDate fromDate,
            LocalDate toDate) {
        return getRejectedByDate(COLUMN_DATE_RESPONDED, fromDate, toDate);
    }

    /**
     * Get a Vector of all rejected portals which were submitted after a certain date
     *
     * @param fromDate Date to start searching from
     * @return Vector of all rejected portals which were submitted after a certain date
     */
    public Vector<PortalRejected> getRejectedBySubmissionDate(LocalDate fromDate) {
        return getRejectedBySubmissionDate(fromDate, LocalDate.now());
    }

    /**
     * Get a Vector of all rejected portals which were submitted in between a range of days
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of all rejected portals which were submitted in between a range of days
     */
    public Vector<PortalRejected> getRejectedBySubmissionDate(LocalDate fromDate,
            LocalDate toDate) {
        return getRejectedByDate(COLUMN_DATE_SUBMITTED, fromDate, toDate);
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
    public long getRejectedCountByResponseDate(LocalDate fromDate) {
        return getCountByDate(TABLE_REJECTED, COLUMN_DATE_RESPONDED, fromDate);
    }

    /**
     * Get the number of portals that were rejected between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of portals that were rejected between fromDate and toDate
     */
    public long getRejectedCountByResponseDate(LocalDate fromDate, LocalDate toDate) {
        return getCountByDate(TABLE_REJECTED, COLUMN_DATE_RESPONDED, fromDate, toDate);
    }

    /**
     * Get the number of rejected portals that were submitted since fromDate
     *
     * @param fromDate Date to start searching from
     * @return number of rejected portals that were submitted since fromDate
     */
    public long getRejectedCountBySubmissionDate(LocalDate fromDate) {
        return getCountByDate(TABLE_REJECTED, COLUMN_DATE_SUBMITTED, fromDate);
    }

    /**
     * Get the number of rejected portals that were submitted between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of rejected portals that were submitted between fromDate and toDate
     */
    public long getRejectedCountBySubmissionDate(LocalDate fromDate, LocalDate toDate) {
        return getCountByDate(TABLE_REJECTED, COLUMN_DATE_SUBMITTED, fromDate, toDate);
    }

    /**
     * Get a rejected portal submission from the database
     *
     * @param pictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalRejected representation of a rejected portal in the database
     */
    public PortalRejected getRejectedPortal(String pictureURL, String portalName) {
        Logger.d("Getting rejected portal");
        return getPortal(TABLE_REJECTED, pictureURL, portalName, new PortalRejectedBuilder());
    }

    /**
     * Create tables in the database
     *
     * @param db A reference to the SQLiteDatabase object used by the app
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PendingPortalContract.SQL_CREATE_ENTRIES);
        db.execSQL(AcceptedPortalContract.SQL_CREATE_ENTRIES);
        db.execSQL(RejectedPortalContract.SQL_CREATE_ENTRIES);
        db.execSQL(LoggingContract.SQL_CREATE_ENTRIES);
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
        db.execSQL(LoggingContract.SQL_DELETE_ENTRIES);
    }

    public void resetLogTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(LoggingContract.SQL_DELETE_ENTRIES);
        db.execSQL(LoggingContract.SQL_CREATE_ENTRIES);
        db.close();
    }

    /**
     * Update a database entry for an accepted portal
     *
     * @param portal    PortalAccepted containing the new information
     * @param oldPortal PortalAccepted containing the old information to be updated
     */
    public void updateAccepted(PortalAccepted portal, PortalAccepted oldPortal) {
        Logger.d("Update portal: " + oldPortal.getName());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        String dateSubmitted = DATE_FORMATTER.print(portal.getDateSubmitted());
        String dateResponded = DATE_FORMATTER.print(portal.getDateResponded());

        values.put(COLUMN_NAME, portal.getName());
        values.put(COLUMN_DATE_SUBMITTED, dateSubmitted);
        values.put(COLUMN_PICTURE_URL, portal.getPictureURL());
        values.put(COLUMN_DATE_RESPONDED, dateResponded);
        values.put(COLUMN_LIVE_ADDRESS, portal.getLiveAddress());
        values.put(COLUMN_INTEL_LINK_URL, portal.getIntelLinkURL());

        db.update(TABLE_ACCEPTED, values, COLUMN_PICTURE_URL + " = ?",
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
        Logger.d("Update portal: " + oldPortal.getName());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, portal.getName());
        values.put(COLUMN_DATE_SUBMITTED, DATE_FORMATTER.print(portal.getDateSubmitted()));
        values.put(COLUMN_PICTURE_URL, portal.getPictureURL());

        db.update(TABLE_PENDING, values, COLUMN_PICTURE_URL + " = ?",
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
        Logger.d("Update portal: " + oldPortal.getName());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        String dateSubmitted = DATE_FORMATTER.print(portal.getDateSubmitted());
        String dateResponded = DATE_FORMATTER.print(portal.getDateResponded());

        values.put(COLUMN_NAME, portal.getName());
        values.put(COLUMN_DATE_SUBMITTED, dateSubmitted);
        values.put(COLUMN_PICTURE_URL, portal.getPictureURL());
        values.put(COLUMN_DATE_RESPONDED, dateResponded);
        values.put(COLUMN_REJECTION_REASON, portal.getRejectionReason());

        db.update(TABLE_REJECTED, values, COLUMN_PICTURE_URL + " = ?",
                new String[]{String.valueOf(oldPortal.getPictureURL())});
        db.close();
    }
}
