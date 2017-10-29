/******************************************************************************
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
 ******************************************************************************/

package com.einzig.ipst2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.einzig.ipst2.portal.PortalSubmission;

import java.util.ArrayList;
import java.util.List;

import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_DATE_RESPONDED;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_NAME;
import static com.einzig.ipst2.database.PendingPortalContract.PendingPortalEntry.COLUMN_PICTURE_URL;

/**
 * @author Ryan Porterfield
 * @since 2017-10-29
 */

class DatabaseInterface extends SQLiteOpenHelper {

    /**
     * WHERE clause for getting a date between a range
     */
    static final String DATE_BETWEEN_CLAUSE = String.format("%s BETWEEN ? AND ?",
                                                            COLUMN_DATE_RESPONDED);
    /**
     * WHERE clause for primary key values
     */
    static final String PRIMARY_KEY_CLAUSE = String.format("%s = ? AND %s = ?",
                                                           COLUMN_PICTURE_URL,
                                                           COLUMN_NAME);
    /**
     * Database name
     */
    static final private String DATABASE_NAME = "IPSTSubmissionDB";
    /**
     * Database version
     */
    static final private int DATABASE_VERSION = 2;

    /**
     * Create a new DatabaseInterface to interact with the SQLite database for the application
     *
     * @param context Context used by super class
     */
    public DatabaseInterface(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Add some values to a table in the database.
     *
     * @param table         Table key in the database
     * @param contentValues Values to be inserted
     */
    void addPortal(@NonNull final String table, @NonNull final ContentValues contentValues) {
        final SQLiteDatabase db = getWritableDatabase();
        db.insert(table, null, contentValues);
        db.close();
    }

    /**
     * Clear the database
     */
    void deleteAll() {
        final SQLiteDatabase db = this.getWritableDatabase();
        onUpgrade(db, 0, DATABASE_VERSION);
        db.close();
    }

    /**
     * Helper function to remove a portal from the database
     *
     * @param table  Table the portal exists in
     * @param portal The accepted submission being removed from the database
     */
    void deletePortal(@NonNull final String table, @NonNull final PortalSubmission portal) {
        final SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table, PRIMARY_KEY_CLAUSE,
                  new String[]{portal.getPictureURL(), portal.getName()});
        db.close();
    }

    /**
     * Get the number of entries in a table
     *
     * @param table Table to count rows on
     * @return number of entries in a table
     */
    long getEntryCount(@NonNull final String table, @Nullable final String selection,
                       @Nullable final String[] selectionArgs) {
        final SQLiteDatabase db = getReadableDatabase();
        final long count = DatabaseUtils.queryNumEntries(db, table, selection, selectionArgs);
        db.close();
        return count;
    }

    /**
     * Helper function to get a list of portals from a table matching selection
     *
     * @param table         Database table to search
     * @param selection     WHERE claus
     * @param selectionArgs Arguments for wildcards in selection
     * @param builder       PortalBuilder to build the portal from the query
     * @param <P>           Type of PortalSubmission being returned
     * @return all portals in table which match the selection
     */
    <P extends PortalSubmission> List<P> getPortals(@NonNull final String table,
                                                    @Nullable final String selection,
                                                    @Nullable final String[] selectionArgs,
                                                    @NonNull final PortalBuilder<P> builder) {
        final List<P> portals = new ArrayList<>();
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(table,
                                       null,
                                       selection,
                                       selectionArgs,
                                       null,
                                       null,
                                       null,
                                       null);

        while (cursor.moveToNext()) {
            portals.add(builder.build(cursor));
        }

        cursor.close();
        db.close();
        return portals;
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
        // Drop tables
        db.execSQL(AcceptedPortalContract.SQL_DELETE_ENTRIES);
        db.execSQL(PendingPortalContract.SQL_DELETE_ENTRIES);
        db.execSQL(RejectedPortalContract.SQL_DELETE_ENTRIES);
        // Create tables
        db.execSQL(PendingPortalContract.SQL_CREATE_ENTRIES);
        db.execSQL(AcceptedPortalContract.SQL_CREATE_ENTRIES);
        db.execSQL(RejectedPortalContract.SQL_CREATE_ENTRIES);
    }

    /**
     * @param table
     * @param pictureURL
     * @param portalName
     * @param contentValues
     */
    void updatePortal(@NonNull final String table,
                      @Nullable final String pictureURL,
                      @NonNull final String portalName,
                      @NonNull final ContentValues contentValues) {
        final SQLiteDatabase db = getWritableDatabase();
        db.update(table, contentValues, PRIMARY_KEY_CLAUSE, new String[]{pictureURL, portalName});
        db.close();
    }
}
