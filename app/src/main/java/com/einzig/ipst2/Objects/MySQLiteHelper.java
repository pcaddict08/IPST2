package com.einzig.ipst2.Objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.einzig.ipst2.Utilities.Utilities;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "SubmissionDB4";
    private static final String TABLE_PS = "portalSubmissions";
    private static final String KEY_NAME = "name";
    private static final String KEY_DATESUBMITTED = "dateSubmitted";
    private static final String KEY_DATEACCEPTED = "dateAccepted";
    private static final String KEY_DATEREJECTED = "dateRejected";
    private static final String KEY_STATUS = "status";
    private static final String KEY_ACCOUNTACCEPTEDEMAIL = "accountAcceptedEmail";
    private static final String KEY_LIVEADDRESS = "liveAddress";
    private static final String KEY_INTELLINKURL = "intelLinkURL";
    private static final String KEY_REJECTIONREASON = "rejectionReason";
    private static final String KEY_PICTUREURL = "pictureURL";
    private static final String[] COLUMNS = {
            KEY_NAME,
            KEY_DATESUBMITTED,
            KEY_DATEACCEPTED,
            KEY_DATEREJECTED,
            KEY_STATUS,
            KEY_ACCOUNTACCEPTEDEMAIL,
            KEY_LIVEADDRESS,
            KEY_INTELLINKURL,
            KEY_PICTUREURL,
            KEY_REJECTIONREASON
    };

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM dd HH:mm:ss yyyy");

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PORTAL_TABLE = "CREATE TABLE "+TABLE_PS+" ( " +
                COLUMNS[0] + " TEXT, " +
                COLUMNS[1] + " TEXT, " +
                COLUMNS[2] + " TEXT, " +
                COLUMNS[3] + " TEXT, " +
                COLUMNS[4] + " TEXT, " +
                COLUMNS[5] + " TEXT, " +
                COLUMNS[6] + " TEXT, " +
                COLUMNS[7] + " TEXT, " +
                COLUMNS[8] + " TEXT, " +
                COLUMNS[9] + " TEXT )";
        db.execSQL(CREATE_PORTAL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PS);
        this.onCreate(db);
    }

    public void addPortalSubmission(PortalSubmission portalSubmission) {
        Log.d("addSubmission", portalSubmission.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, portalSubmission.getName());
        if (portalSubmission.getDateSubmitted() == null)
            values.put(KEY_DATESUBMITTED, "");
        else {
            values.put(KEY_DATESUBMITTED, dateFormat.format(portalSubmission.getDateSubmitted()));
        }

        if (portalSubmission.getDateAccepted() == null)
            values.put(KEY_DATEACCEPTED, "");
        else {
            values.put(KEY_DATEACCEPTED, dateFormat.format(portalSubmission.getDateAccepted()));
        }

        if (portalSubmission.getDateRejected() == null)
            values.put(KEY_DATEREJECTED, "");
        else {
            values.put(KEY_DATEREJECTED, dateFormat.format(portalSubmission.getDateRejected()));
        }

        values.put(KEY_STATUS, portalSubmission.getStatus());
        values.put(KEY_ACCOUNTACCEPTEDEMAIL, portalSubmission.getAccountAcceptedEmail());
        values.put(KEY_LIVEADDRESS, portalSubmission.getLiveAddress());
        values.put(KEY_INTELLINKURL, portalSubmission.getIntelLinkURL());
        values.put(KEY_PICTUREURL, portalSubmission.getPictureURL());
        values.put(KEY_REJECTIONREASON, portalSubmission.getRejectionReason());

        db.insert(TABLE_PS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        db.close();
    }

    public PortalSubmission getPortalSubmission(String imageURL) throws ParseException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (imageURL != null) {
            cursor = db.query(TABLE_PS, // a. table
                    COLUMNS, // b. column names
                    " pictureURL = ?", // c. selections
                    new String[]{imageURL}, // d. selections args
                    null, // e. group by
                    null, // f. having
                    null, // g. order by
                    null); // h. limit
        }
        if (cursor != null) {
            cursor.moveToFirst();
            PortalSubmission submission = new PortalSubmission();
            String dateSubmitted = "";
            String dateRejected = "";
            String dateAccepted = "";
            String dateSort = "";
            submission.setName("foundPortal");

            Log.d("getSubmission(" + imageURL + ")", submission.getName());
            return submission;
        } else
            return null;
    }

    // Get All Books
    public List<PortalSubmission> getAllSubmissions() throws ParseException {
        List<PortalSubmission> submissions = new LinkedList<PortalSubmission>();
        String query = "SELECT  * FROM " + TABLE_PS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        PortalSubmission submission = null;
        if (cursor.moveToFirst()) {
            String dateSubmitted = "";
            String dateRejected = "";
            String dateAccepted = "";
            String dateSort = "";

            do {
                submission = new PortalSubmission();
                submission.setName(cursor.getString(0));
                dateSubmitted = cursor.getString(1);
                dateAccepted = cursor.getString(2);
                dateRejected = cursor.getString(3);
                dateSort = cursor.getString(4);

                if (!dateSubmitted.equalsIgnoreCase("")) {
                    submission.setDateSubmitted(dateFormat.parse(cursor.getString(1)));
                }
                if (!dateAccepted.equalsIgnoreCase("")) {
                    submission.setDateAccepted(dateFormat.parse(cursor.getString(2)));
                }
                if (!dateRejected.equalsIgnoreCase("")) {
                    submission.setDateRejected(dateFormat.parse(cursor.getString(3)));
                }
                submission.setStatus(cursor.getString(5));
                submission.setAccountAcceptedEmail(cursor.getString(6));
                submission.setLiveAddress(cursor.getString(7));
                submission.setIntelLinkURL(cursor.getString(8));
                submission.setPictureURL(cursor.getString(9));
                submission.setRejectionReason(cursor.getString(10));
                submissions.add(submission);
            } while (cursor.moveToNext());
        }
        return submissions;
    }

    public int updateSubmission(PortalSubmission submission, PortalSubmission oldSubmission) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, submission.getName());

        if (submission.getDateSubmitted() == null)
            values.put(KEY_DATESUBMITTED, "");
        else {
            values.put(KEY_DATESUBMITTED, dateFormat.format(submission.getDateSubmitted()));
        }

        if (submission.getDateAccepted() == null)
            values.put(KEY_DATEACCEPTED, "");
        else {
            values.put(KEY_DATEACCEPTED, dateFormat.format(submission.getDateAccepted()));
        }

        if (submission.getDateRejected() == null)
            values.put(KEY_DATEREJECTED, "");
        else {
            values.put(KEY_DATEREJECTED, dateFormat.format(submission.getDateRejected()));
        }

        values.put(KEY_STATUS, submission.getStatus());
        values.put(KEY_ACCOUNTACCEPTEDEMAIL, submission.getAccountAcceptedEmail());
        values.put(KEY_LIVEADDRESS, submission.getLiveAddress());
        values.put(KEY_INTELLINKURL, submission.getIntelLinkURL());
        values.put(KEY_PICTUREURL, submission.getPictureURL());
        values.put(KEY_REJECTIONREASON, submission.getRejectionReason());

        // 3. updating row
        int i = db.update(TABLE_PS, //table
                values, // column/value
                KEY_PICTUREURL + " = ?", // selections
                new String[]{String.valueOf(oldSubmission.getPictureURL())}); //selection args
        db.close();
        Log.d("updateSubmission", submission.getPictureURL());

        return i;

    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PS, null, null);
        db.close();
    }

    public void deleteSubmission(PortalSubmission submission) {
        Log.d("deleteSubmission", "TRYING: " + submission.getPictureURL());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PS,
                KEY_PICTUREURL + " = ?",
                new String[]{submission.getPictureURL()});
        db.close();
        Log.d("deleteSubmission", submission.getPictureURL());
    }

    static class CustomNameComparator implements Comparator<PortalSubmission> {
        @Override
        public int compare(PortalSubmission o1, PortalSubmission o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    static class CustomTypeComparator implements Comparator<PortalSubmission> {
        @Override
        public int compare(PortalSubmission o1, PortalSubmission o2) {
            return o1.getStatus().compareTo(o2.getStatus());
        }
    }

    static class CustomSubmissionDateComparator implements Comparator<PortalSubmission> {
        @Override
        public int compare(PortalSubmission o1, PortalSubmission o2) {
            return o1.getDateSubmitted().compareTo(o2.getDateSubmitted());
        }
    }

}
