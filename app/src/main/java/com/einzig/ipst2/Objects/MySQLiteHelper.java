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

public class MySQLiteHelper extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 1;
	// Database Name
	private static final String DATABASE_NAME = "SubmissionDB4";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);	
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// SQL statement to create book table
		String CREATE_AGENT_TABLE = "CREATE TABLE portalSubmissions ( " +
				"name TEXT, " + 
				"dateSubmitted TEXT, "+
				"dateAccepted TEXT, "+
				"dateRejected TEXT, "+
				"dateSort TEXT," + 
				"status TEXT, "+  
				"accountAcceptedEmail TEXT, " + 
				"liveAddress TEXT, "+  
				"intelLinkURL TEXT, "+  
				"pictureURL TEXT, "+  
				"rejectionReason TEXT )";

		// create books table
		db.execSQL(CREATE_AGENT_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older books table if existed
		db.execSQL("DROP TABLE IF EXISTS submissions");

		// create fresh books table
		this.onCreate(db);
	}
	//---------------------------------------------------------------------

	/**
	 * CRUD operations (create "add", read "get", update, delete) agent + get all agents + delete all agent
	 */

	// Books table name
	private static final String TABLE_PS = "portalSubmissions";

	// Books Table Columns names
	private static final String KEY_NAME = "name";
	private static final String KEY_DATESUBMITTED = "dateSubmitted";
	private static final String KEY_DATEACCEPTED = "dateAccepted";
	private static final String KEY_DATEREJECTED = "dateRejected";
	private static final String KEY_DATESORT = "dateSort";
	private static final String KEY_STATUS = "status";
	private static final String KEY_ACCOUNTACCEPTEDEMAIL = "accountAcceptedEmail";
	private static final String KEY_LIVEADDRESS = "liveAddress";
	private static final String KEY_INTELLINKURL = "intelLinkURL";
	private static final String KEY_REJECTIONREASON = "rejectionReason";
	private static final String KEY_PICTUREURL = "pictureURL";
	private static final String[] COLUMNS = {KEY_NAME,KEY_DATESUBMITTED,KEY_DATEACCEPTED,KEY_DATEREJECTED,KEY_DATESORT,KEY_STATUS,KEY_ACCOUNTACCEPTEDEMAIL, KEY_LIVEADDRESS,KEY_INTELLINKURL, KEY_PICTUREURL,KEY_REJECTIONREASON};

	SimpleDateFormat dateFormat = new SimpleDateFormat("MM dd HH:mm:ss zzz yyyy");
	SimpleDateFormat dateFormatAZN = new SimpleDateFormat("MM dd HH:mm:ss yyyy");
	//EEE MMM dd HH:mm:ss zzz yyyy
	public void addPortalSubmission(PortalSubmission portalSubmission){
		Log.d("addSubmission", portalSubmission.getName());
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, portalSubmission.getName());

		if(portalSubmission.getDateSubmitted() == null)
			values.put(KEY_DATESUBMITTED, "");
		else
		{
			if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
				values.put(KEY_DATESUBMITTED, dateFormatAZN.format(portalSubmission.getDateSubmitted()));
			else
				values.put(KEY_DATESUBMITTED, dateFormat.format(portalSubmission.getDateSubmitted()));
		}
		
		if(portalSubmission.getDateAccepted() == null)
			values.put(KEY_DATEACCEPTED, "");
		else
		{
			if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
				values.put(KEY_DATEACCEPTED, dateFormatAZN.format(portalSubmission.getDateAccepted()));
			else
				values.put(KEY_DATEACCEPTED, dateFormat.format(portalSubmission.getDateAccepted()));
		}

		if(portalSubmission.getDateRejected() == null)
			values.put(KEY_DATEREJECTED, "");
		else
		{
			if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
				values.put(KEY_DATEREJECTED, dateFormatAZN.format(portalSubmission.getDateRejected()));
			else
				values.put(KEY_DATEREJECTED, dateFormat.format(portalSubmission.getDateRejected()));
		}

		if( portalSubmission.getDateSort() == null)
			values.put(KEY_DATESORT, "");
		else
		{
			if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
				values.put(KEY_DATESORT, dateFormatAZN.format(portalSubmission.getDateSort()));
			else
				values.put(KEY_DATESORT, dateFormat.format(portalSubmission.getDateSort()));
		}

		values.put(KEY_STATUS, portalSubmission.getStatus());
		values.put(KEY_ACCOUNTACCEPTEDEMAIL, portalSubmission.getAccountAcceptedEmail());
		values.put(KEY_LIVEADDRESS, portalSubmission.getLiveAddress());
		values.put(KEY_INTELLINKURL, portalSubmission.getIntelLinkURL());
		values.put(KEY_PICTUREURL, portalSubmission.getPictureURL());
		values.put(KEY_REJECTIONREASON, portalSubmission.getRejectionReason());

		// 3. insert
		db.insert(TABLE_PS, // table
				null, //nullColumnHack
				values); // key/value -> keys = column names/ values = column values

		// 4. close
		db.close(); 
	}

	public PortalSubmission getPortalSubmission(String imageURL) throws ParseException{

		// 1. get reference to readable DB
		SQLiteDatabase db = this.getReadableDatabase();

		// 2. build query
		Cursor cursor = null;
		
		if(imageURL != null)
		{
		cursor = db.query(TABLE_PS, // a. table
						COLUMNS, // b. column names
						" pictureURL = ?", // c. selections 
						new String[] { imageURL }, // d. selections args
						null, // e. group by
						null, // f. having
						null, // g. order by
						null); // h. limit
		}

		// 3. if we got results get the first one
		if (cursor != null)
		{
			cursor.moveToFirst();

			// 4. build book object
			PortalSubmission submission = new PortalSubmission();
			String dateSubmitted = "";
			String dateRejected = "";
			String dateAccepted = "";
			String dateSort = "";
			submission.setName("foundPortal");	
			//dateSubmitted = cursor.getString(1);
			//dateAccepted = cursor.getString(2);
			//dateRejected = cursor.getString(3);
			/*
			dateSort = cursor.getString(4);

			if(!dateSubmitted.equalsIgnoreCase(""))
				submission.setDateSubmitted(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(cursor.getString(1)));
			if(!dateAccepted.equalsIgnoreCase(""))
				submission.setDateAccepted(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(cursor.getString(2)));
			if(!dateRejected.equalsIgnoreCase(""))
				submission.setDateRejected(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(cursor.getString(3)));
			if(!dateSort.equalsIgnoreCase(""))
				submission.setDateSort(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(cursor.getString(4)));
			submission.setStatus(cursor.getString(5));
			submission.setAccountAcceptedEmail(cursor.getString(6));
			submission.setLiveAddress(cursor.getString(7));
			submission.setIntelLinkURL(cursor.getString(8));
			submission.setPictureURL(cursor.getString(9));
			submission.setRejectionReason(cursor.getString(10));
*/
			Log.d("getSubmission("+imageURL+")", submission.getName());

			return submission;
		}
		else
			return null;
	}

	// Get All Books
	public List<PortalSubmission> getAllSubmissions() throws ParseException {
		List<PortalSubmission> submissions = new LinkedList<PortalSubmission>();

		// 1. build the query
		String query = "SELECT  * FROM " + TABLE_PS;

		// 2. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		// 3. go over each row, build book and add it to list
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

				if(!dateSubmitted.equalsIgnoreCase(""))
				{
					if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
						submission.setDateSubmitted(dateFormatAZN.parse(cursor.getString(1)));
					else
						submission.setDateSubmitted(dateFormat.parse(cursor.getString(1)));
				}		
				if(!dateAccepted.equalsIgnoreCase(""))
				{
					if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
						submission.setDateAccepted(dateFormatAZN.parse(cursor.getString(2)));
					else
						submission.setDateAccepted(dateFormat.parse(cursor.getString(2)));
				}
				if(!dateRejected.equalsIgnoreCase(""))
				{
					if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
						submission.setDateRejected(dateFormatAZN.parse(cursor.getString(3)));
					else
						submission.setDateRejected(dateFormat.parse(cursor.getString(3)));
				}
				if(!dateSort.equalsIgnoreCase(""))
				{
					if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
						submission.setDateSort(dateFormatAZN.parse(cursor.getString(4)));
					else
						submission.setDateSort(dateFormat.parse(cursor.getString(4)));
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

		//Log.d("getAllSubmissions()", submissions.toString());

		// return books
		return submissions;
	}

	// Updating single book
	public int updateSubmission(PortalSubmission submission, PortalSubmission oldSubmission) {

		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, submission.getName());

		if(submission.getDateSubmitted() == null)
			values.put(KEY_DATESUBMITTED, "");
		else
		{
			if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
			{
				System.out.println("UPDATE SUBMISSION: AZN FORMAT");
				values.put(KEY_DATESUBMITTED, dateFormatAZN.format(submission.getDateSubmitted()));				
			}
			else
			{
				System.out.println("UPDATE SUBMISSION: NOT AZN FORMAT");
				values.put(KEY_DATESUBMITTED, dateFormat.format(submission.getDateSubmitted()));
			}
		}
		
		if(submission.getDateAccepted() == null)
			values.put(KEY_DATEACCEPTED, "");
		else
		{
			if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
				values.put(KEY_DATEACCEPTED, dateFormatAZN.format(submission.getDateAccepted()));
			else
				values.put(KEY_DATEACCEPTED, dateFormat.format(submission.getDateAccepted()));
		}

		if(submission.getDateRejected() == null)
			values.put(KEY_DATEREJECTED, "");
		else
		{
			if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
				values.put(KEY_DATEREJECTED, dateFormatAZN.format(submission.getDateRejected()));
			else
				values.put(KEY_DATEREJECTED, dateFormat.format(submission.getDateRejected()));
		}

		if( submission.getDateSort() == null)
			values.put(KEY_DATESORT, "");
		else
		{
			if(SingletonClass.getInstance().getSharedPref().getString("newDBFormatInit", "notInit").equalsIgnoreCase("yes"))
				values.put(KEY_DATESORT, dateFormatAZN.format(submission.getDateSort()));
			else
				values.put(KEY_DATESORT, dateFormat.format(submission.getDateSort()));
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
				KEY_PICTUREURL+" = ?", // selections
				new String[] { String.valueOf(oldSubmission.getPictureURL()) }); //selection args

		// 4. close
		db.close();
		Log.d("updateSubmission", submission.getPictureURL());

		return i;

	}


	public void deleteAll()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_PS, null, null);
		db.close();
	}

	public void deleteSubmission(PortalSubmission submission) {
		Log.d("deleteSubmission", "TRYING: " +  submission.getPictureURL());

		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. delete
		db.delete(TABLE_PS,
				KEY_PICTUREURL+" = ?",
				new String[] { submission.getPictureURL() });

		// 3. close
		db.close();

		Log.d("deleteSubmission", submission.getPictureURL());

	}
	static class CustomNameComparator implements Comparator<PortalSubmission> {
		@Override
		public int compare(PortalSubmission o1, PortalSubmission o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}

	static class CustomDateComparator implements Comparator<PortalSubmission> {
		@Override
		public int compare(PortalSubmission o1, PortalSubmission o2) {
			return o1.getDateSort().compareTo(o2.getDateSort());
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
