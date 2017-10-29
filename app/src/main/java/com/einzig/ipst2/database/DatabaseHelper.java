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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.Logger;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;

import static com.einzig.ipst2.database.AcceptedPortalContract.AcceptedPortalEntry.COLUMN_INTEL_LINK_URL;
import static com.einzig.ipst2.database.AcceptedPortalContract.AcceptedPortalEntry.COLUMN_LIVE_ADDRESS;
import static com.einzig.ipst2.database.AcceptedPortalContract.AcceptedPortalEntry.TABLE_ACCEPTED;
import static com.einzig.ipst2.database.DatabaseInterface.DATE_BETWEEN_CLAUSE;
import static com.einzig.ipst2.database.DatabaseInterface.PRIMARY_KEY_CLAUSE;
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
public class DatabaseHelper {
    /**
     * The date format that MySQL stores DATETIME objects in
     */
    static final public DateTimeFormatter DATE_FORMATTER = ISODateTimeFormat.date();

    final private DatabaseInterface databaseInterface;

    /**
     * Create a new DatabaseHelper to interact with the SQLite database for the application
     *
     * @param context Context used by super class
     */
    public DatabaseHelper(@NonNull final Context context) {
        databaseInterface = new DatabaseInterface(context);
    }

    public <P extends PortalSubmission> void add(@NonNull final P portal) {
        if (portal instanceof PortalAccepted)
            addPortalAccepted((PortalAccepted) portal);
        else if (portal instanceof PortalRejected)
            addPortalRejected((PortalRejected) portal);
        else
            addPortalSubmission(portal);
    }

    /**
     * Insert a PortalAccepted into the database
     *
     * @param portal The portal getting stored in the database
     */
    public void addPortalAccepted(@NonNull final PortalAccepted portal) {
        final ContentValues contentValues = new ContentValues();
        final String dateResponded = DATE_FORMATTER.print(portal.getDateResponded());

        Logger.d("Add accepted portal: " + portal.getName());

        addPortalSubmission(portal);

        contentValues.put(COLUMN_NAME, portal.getName());
        contentValues.put(COLUMN_PICTURE_URL, portal.getPictureURL());
        contentValues.put(COLUMN_DATE_RESPONDED, dateResponded);
        contentValues.put(COLUMN_LIVE_ADDRESS, portal.getLiveAddress());
        contentValues.put(COLUMN_INTEL_LINK_URL, portal.getIntelLinkURL());

        databaseInterface.addPortal(TABLE_ACCEPTED, contentValues);
    }

    /**
     * Insert a PortalRejected into the database
     *
     * @param portal The portal getting stored in the database
     */
    public void addPortalRejected(@NonNull final PortalRejected portal) {
        final ContentValues contentValues = new ContentValues();
        final String dateResponded = DATE_FORMATTER.print(portal.getDateResponded());

        Logger.d("Add rejected portal: " + portal.getName());

        addPortalSubmission(portal);

        contentValues.put(COLUMN_NAME, portal.getName());
        contentValues.put(COLUMN_PICTURE_URL, portal.getPictureURL());
        contentValues.put(COLUMN_DATE_RESPONDED, dateResponded);
        contentValues.put(COLUMN_REJECTION_REASON, portal.getRejectionReason());

        databaseInterface.addPortal(TABLE_REJECTED, contentValues);
    }

    /**
     * Insert a PortalSubmission into the database
     *
     * @param portal The portal getting stored in the database
     */
    public void addPortalSubmission(@NonNull final PortalSubmission portal) {
        final ContentValues contentValues = new ContentValues();
        final String dateSubmitted = DATE_FORMATTER.print(portal.getDateSubmitted());

        Logger.d("Add portal submission: " + portal.getName());

        contentValues.put(COLUMN_NAME, portal.getName());
        contentValues.put(COLUMN_DATE_SUBMITTED, dateSubmitted);
        contentValues.put(COLUMN_PICTURE_URL, portal.getPictureURL());

        databaseInterface.addPortal(TABLE_PENDING, contentValues);
    }

    /**
     * Check if the database contains an accepted portal submission
     *
     * @param pictureURL URL of the portal picture used to uniquely identify the portal
     * @return true if the database contains the portal, otherwise false
     */
    public boolean containsAccepted(@NonNull final String pictureURL,
                                    @NonNull final String pictureName) {
        return getAcceptedPortal(pictureURL, pictureName) != null;
    }

    /**
     * Check if the database contains a pending portal submission
     *
     * @param pictureURL URL of the portal picture used to uniquely identify the portal
     * @return true if the database contains the portal, otherwise false
     */
    public boolean containsPending(@NonNull final String pictureURL,
                                   @NonNull final String pictureName) {
        return getPendingPortal(pictureURL, pictureName) != null;
    }

    /**
     * Check if the database contains a rejected portal submission
     *
     * @param pictureURL URL of the portal picture used to uniquely identify the portal
     * @return true if the database contains the portal, otherwise false
     */
    public boolean containsRejected(@NonNull final String pictureURL,
                                    @NonNull final String pictureName) {
        return getRejectedPortal(pictureURL, pictureName) != null;
    }

    /**
     * Remove an accepted portal submission
     *
     * @param portal The accepted submission being removed from the database
     */
    public void deleteAccepted(@NonNull final PortalAccepted portal) {
        Logger.d("Remove accepted portal: " + portal.getName());
        databaseInterface.deletePortal(TABLE_ACCEPTED, portal);
        databaseInterface.deletePortal(TABLE_PENDING, portal);
    }

    /**
     * Clear the database
     */
    public void deleteAll() {
        databaseInterface.deleteAll();
    }

    /**
     * Remove a pending portal submission
     *
     * @param portal The pending submission being removed from the database
     */
    public void deletePending(@NonNull final PortalSubmission portal) {
        Logger.d("Remove portal submission: " + portal.getName());
        databaseInterface.deletePortal(TABLE_PENDING, portal);
    }

    /**
     * Remove a rejected portal submission
     *
     * @param portal The rejected submission being removed from the database
     */
    public void deleteRejected(@NonNull final PortalRejected portal) {
        Logger.d("Remove rejected portal: " + portal.getName());
        databaseInterface.deletePortal(TABLE_REJECTED, portal);
        databaseInterface.deletePortal(TABLE_PENDING, portal);
    }

    /**
     * Get the number of approved portals
     *
     * @return number of approved portals
     */
    public long getAcceptedCount() {
        return databaseInterface.getEntryCount(TABLE_ACCEPTED, null, null);
    }

    /**
     * Get the number of portals accepted since fromDate
     *
     * @param fromDate Date to start searching from
     * @return number of portals accepted since fromDate
     */
    public long getAcceptedCount(@NonNull final LocalDate fromDate,
                                 @NonNull final LocalDate toDate) {
        return getCountByDate(TABLE_ACCEPTED, fromDate, toDate);
    }

    /**
     * Get an accepted portal submission from the database
     *
     * @param pictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalAccepted representation of an accepted portal in the database
     */
    public PortalAccepted getAcceptedPortal(@NonNull final String pictureURL,
                                            @NonNull final String portalName) {
        return getPortal(TABLE_ACCEPTED, pictureURL, portalName, new PortalAcceptedBuilder());
    }

    /**
     * Get a Vector of all accepted portals which went live in between a range of days
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of all accepted portals which went live in between a range of days
     */
    public List<PortalAccepted> getAcceptedPortals(@NonNull final LocalDate fromDate,
                                                   @NonNull final LocalDate toDate) {
        Logger.d("Getting all accepted portals within date range");
        return getPortalsByDate(TABLE_ACCEPTED, fromDate, toDate, new PortalAcceptedBuilder());
    }

    /**
     * Get a Vector of all accepted portal submissions
     *
     * @return Vector of all accepted portal submissions
     */
    public List<PortalAccepted> getAllAccepted() {
        Logger.d("Get all accepted portals");
        return databaseInterface.getPortals(TABLE_ACCEPTED,
                                            null,
                                            null,
                                            new PortalAcceptedBuilder());
    }

    /**
     * Helper function getAcceptedBySubmissionDate and getAllAcceptedBy
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of accepted portals which were either submitted or approved from fromDate to
     * toDate
     */
    public List<PortalAccepted> getAllAccepted(@NonNull final LocalDate fromDate,
                                               @NonNull final LocalDate toDate) {
        Logger.d("Getting all accepted portals within date range");
        return getPortalsByDate(TABLE_ACCEPTED, fromDate, toDate, new PortalAcceptedBuilder());
    }

    /**
     * Gets a Vector of all pending portal submissions
     *
     * @return Vector of all pending portal submissions
     */
    public List<PortalSubmission> getAllPending() {
        Logger.d("Get all pending portals");
        return databaseInterface.getPortals(TABLE_PENDING,
                                            null,
                                            null,
                                            new PortalSubmissionBuilder());
    }

    /**
     * Get all portals from the database
     *
     * @return all portals from the database
     */
    public List<PortalSubmission> getAllPortals() {
        final List<PortalSubmission> portals = new ArrayList<>();

        portals.addAll(getAllPending());
        portals.addAll(getAllAccepted());
        portals.addAll(getAllRejected());

        return portals;
    }

    /**
     * Get all portals after date from the database
     *
     * @param fromDate Date to start searching from
     * @return all portals after date from the database
     */
    public List<PortalSubmission> getAllPortals(@NonNull final LocalDate fromDate,
                                                @NonNull final LocalDate toDate) {
        final List<PortalSubmission> portals = new ArrayList<>();

        portals.addAll(getPendingPortals(fromDate, toDate));
        portals.addAll(getAcceptedPortals(fromDate, toDate));
        portals.addAll(getRejectedPortals(fromDate, toDate));

        return portals;
    }

    /**
     * Gets a Vector of all rejected portal submissions
     *
     * @return Vector of all rejected portal submissions
     */
    public List<PortalRejected> getAllRejected() {
        Logger.d("Get all rejected portals");
        return databaseInterface.getPortals(TABLE_REJECTED,
                                            null,
                                            null,
                                            new PortalRejectedBuilder());
    }

    /**
     * Get the number of portals in a table between fromDate and toDate
     *
     * @param table    Table in the database
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return number of portals in a table between fromDate and toDate
     */
    private long getCountByDate(@NonNull final String table,
                                @NonNull final LocalDate fromDate,
                                @NonNull final LocalDate toDate) {
        final String fromDateStr = DATE_FORMATTER.print(fromDate);
        final String toDateStr = DATE_FORMATTER.print(toDate);

        return databaseInterface.getEntryCount(table, COLUMN_DATE_RESPONDED + " BETWEEN ? AND ?",
                                               new String[]{fromDateStr, toDateStr});
    }

    /**
     * Get the number of pending portals
     *
     * @return number of pending portals
     */
    public long getPendingCount() {
        return databaseInterface.getEntryCount(TABLE_PENDING, null, null);
    }

    /**
     * Get the number of portals that were submitted since fromDate
     *
     * @param fromDate Date to start searching from
     * @return number of portals that were submitted since fromDate
     */
    public long getPendingCount(@NonNull final LocalDate fromDate,
                                @NonNull final LocalDate toDate) {
        return getCountByDate(TABLE_PENDING, fromDate, toDate);
    }

    /**
     * Get a pending portal submission from the database
     *
     * @param pictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalSubmission representation of a pending portal in the database
     */
    public PortalSubmission getPendingPortal(@NonNull final String pictureURL,
                                             @NonNull final String portalName) {
        Logger.d("Getting pending portal");
        return getPortal(TABLE_PENDING, pictureURL, portalName, new PortalSubmissionBuilder());
    }

    /**
     * Get all pending portal submissions which were submitted between fromDate and toDate
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of pending portals which were submitted between fromDate and toDate
     */
    public List<PortalSubmission> getPendingPortals(@NonNull final LocalDate fromDate,
                                                    @NonNull final LocalDate toDate) {
        Logger.d("Getting all pending portals in a date range");
        return getPortalsByDate(TABLE_PENDING, fromDate, toDate, new PortalSubmissionBuilder());
    }

    /**
     * @param table      Database table to search
     * @param pictureURL Unique key for a portal in the database
     * @param builder    PortalBuilder to build the portal from the query
     * @param <P>        Type of PortalSubmission being returned
     * @return a portal from the database
     */
    private <P extends PortalSubmission> P getPortal(@NonNull final String table,
                                                     @Nullable final String pictureURL,
                                                     @NonNull final String portalName,
                                                     @NonNull final PortalBuilder<P> builder) {
        final List<P> portals = databaseInterface.getPortals(table,
                                                             PRIMARY_KEY_CLAUSE,
                                                             new String[]{pictureURL, portalName},
                                                             builder);

        if (portals.size() > 0)
            return portals.get(0);
        Logger.d(String.format("Returning null for (%s, %s)", pictureURL, portalName));
        return null;
    }

    /**
     * Get all portals in a range of dates.
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of portals which were either submitted or approved from fromDate to
     * toDate.
     */
    private <P extends PortalSubmission> List<P> getPortalsByDate(@NonNull final String table,
                                                                  @NonNull final LocalDate fromDate,
                                                                  @NonNull final LocalDate toDate,
                                                                  @NonNull final PortalBuilder<P> builder) {
        final String fromDateStr = DATE_FORMATTER.print(fromDate);
        final String toDateStr = DATE_FORMATTER.print(toDate);
        return databaseInterface.getPortals(table,
                                            DATE_BETWEEN_CLAUSE,
                                            new String[]{fromDateStr, toDateStr},
                                            builder);
    }

    /**
     * Get the number of rejected portals
     *
     * @return number of rejected portals
     */
    public long getRejectedCount() {
        return databaseInterface.getEntryCount(TABLE_REJECTED, null, null);
    }

    /**
     * Get the number of portals that were rejected since fromDate
     *
     * @param fromDate Date to start searching from
     * @return number of portals that were rejected since fromDate
     */
    public long getRejectedCount(@NonNull final LocalDate fromDate,
                                 @NonNull final LocalDate toDate) {
        return getCountByDate(TABLE_REJECTED, fromDate, toDate);
    }

    /**
     * Get a rejected portal submission from the database
     *
     * @param pictureURL URL of the portal picture used to uniquely identify the portal
     * @return a PortalRejected representation of a rejected portal in the database
     */
    public PortalRejected getRejectedPortal(@NonNull final String pictureURL,
                                            @NonNull final String portalName) {
        Logger.d("Getting rejected portal");
        return getPortal(TABLE_REJECTED, pictureURL, portalName, new PortalRejectedBuilder());
    }

    /**
     * Helper function getAcceptedBySubmissionDate and getAllAcceptedBy
     *
     * @param fromDate Date to start searching from
     * @param toDate   Date to stop searching at
     * @return Vector of rejected portals which were either submitted or approved from fromDate to
     * toDate.
     */
    public List<PortalRejected> getRejectedPortals(@NonNull final LocalDate fromDate,
                                                    @NonNull final LocalDate toDate) {
        Logger.d("Getting all rejected portals within date range");
        return getPortalsByDate(TABLE_REJECTED, fromDate, toDate, new PortalRejectedBuilder());
    }

    /**
     * Get the total number of portals in the database
     *
     * @return the total number of portals in the database
     */
    public long getTotalPortalCount() {
        return getAcceptedCount() + getPendingCount() + getRejectedCount();
    }

    /**
     * Update a database entry for an accepted portal
     *
     * @param portal    PortalAccepted containing the new information
     * @param oldPortal PortalAccepted containing the old information to be updated
     */
    public void updateAccepted(@NonNull final PortalAccepted portal,
                               @NonNull final PortalAccepted oldPortal) {
        final ContentValues contentValues = new ContentValues();
        final String dateSubmitted = DATE_FORMATTER.print(portal.getDateSubmitted());
        final String dateResponded = DATE_FORMATTER.print(portal.getDateResponded());

        Logger.d(String.format("Update portal (%s, %s) ",
                               oldPortal.getName(),
                               oldPortal.getPictureURL()));

        contentValues.put(COLUMN_NAME, portal.getName());
        contentValues.put(COLUMN_DATE_SUBMITTED, dateSubmitted);
        contentValues.put(COLUMN_PICTURE_URL, portal.getPictureURL());
        contentValues.put(COLUMN_DATE_RESPONDED, dateResponded);
        contentValues.put(COLUMN_LIVE_ADDRESS, portal.getLiveAddress());
        contentValues.put(COLUMN_INTEL_LINK_URL, portal.getIntelLinkURL());

        databaseInterface.updatePortal(TABLE_PENDING,
                                       oldPortal.getPictureURL(),
                                       oldPortal.getName(),
                                       contentValues);
        databaseInterface.updatePortal(TABLE_ACCEPTED,
                                       oldPortal.getPictureURL(),
                                       oldPortal.getName(),
                                       contentValues);
    }

    /**
     * Update a database entry for a pending portal
     *
     * @param portal    PortalSubmission containing the new information
     * @param oldPortal PortalSubmission containing the old information to be updated
     */
    public void updatePending(@NonNull final PortalSubmission portal,
                              @NonNull final PortalSubmission oldPortal) {
        final ContentValues contentValues = new ContentValues();

        Logger.d(String.format("Update portal (%s, %s) ",
                               oldPortal.getName(),
                               oldPortal.getPictureURL()));

        contentValues.put(COLUMN_NAME, portal.getName());
        contentValues.put(COLUMN_DATE_SUBMITTED, DATE_FORMATTER.print(portal.getDateSubmitted()));
        contentValues.put(COLUMN_PICTURE_URL, portal.getPictureURL());

        databaseInterface.updatePortal(TABLE_PENDING,
                                       oldPortal.getPictureURL(),
                                       oldPortal.getName(),
                                       contentValues);
    }

    /**
     * Update a database entry for a rejected portal
     *
     * @param portal    PortalRejected containing the new information
     * @param oldPortal PortalRejected containing the old information to be updated
     */
    public void updateRejected(@NonNull final PortalRejected portal,
                               @NonNull final PortalRejected oldPortal) {
        final ContentValues contentValues = new ContentValues();
        final String dateSubmitted = DATE_FORMATTER.print(portal.getDateSubmitted());
        final String dateResponded = DATE_FORMATTER.print(portal.getDateResponded());

        Logger.d(String.format("Update portal (%s, %s) ",
                               oldPortal.getName(),
                               oldPortal.getPictureURL()));

        contentValues.put(COLUMN_NAME, portal.getName());
        contentValues.put(COLUMN_DATE_SUBMITTED, dateSubmitted);
        contentValues.put(COLUMN_PICTURE_URL, portal.getPictureURL());
        contentValues.put(COLUMN_DATE_RESPONDED, dateResponded);
        contentValues.put(COLUMN_REJECTION_REASON, portal.getRejectionReason());

        databaseInterface.updatePortal(TABLE_PENDING,
                                       oldPortal.getPictureURL(),
                                       oldPortal.getName(),
                                       contentValues);
        databaseInterface.updatePortal(TABLE_REJECTED,
                                       oldPortal.getPictureURL(),
                                       oldPortal.getName(),
                                       contentValues);
    }
}
