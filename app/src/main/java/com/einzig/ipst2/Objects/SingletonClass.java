package com.einzig.ipst2.Objects;

import java.util.ArrayList;
import java.util.List;



import android.content.SharedPreferences;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SingletonClass {

	public PortalSubmission detailsSubmission;
	MySQLiteHelper db;
	SharedPreferences sharedPref;
	private static final SingletonClass _theInstance = new SingletonClass();

	public static SingletonClass getInstance() {
		return _theInstance;
	}


	public PortalSubmission getDetailsSubmission() {
		return detailsSubmission;
	}

	public void setDetailsSubmission(PortalSubmission detailsSubmission) {
		this.detailsSubmission = detailsSubmission;
	}

	public MySQLiteHelper getDb() {
		return db;
	}

	public void setDb(MySQLiteHelper db) {
		this.db = db;
	}

	public SharedPreferences getSharedPref() {
		return sharedPref;
	}

	public void setSharedPref(SharedPreferences sharedPref) {
		this.sharedPref = sharedPref;
	}
}
