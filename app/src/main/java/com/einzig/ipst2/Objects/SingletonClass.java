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

package com.einzig.ipst2.Objects;

import java.util.ArrayList;
import java.util.List;



import android.content.SharedPreferences;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.einzig.ipst2.portal.PortalSubmission;

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
