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

package com.einzig.ipst2.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map;

/**
 * @author Steven Foskett
 * @since 2017-06-14
 */
public class PreferencesHelper {
    /**  */
    static public final String DATE_FORMAT_KEY;
    /** Day-month-year format */
    static public final String DD_MM_YYYY_FORMAT;
    /** The key for saving default category preference */
    static public final String DEFAULT_CAT_KEY;
    /** Preferences key used for saving and retrieving the user's email address */
    static public final String EMAIL_KEY;
    /** Preferences key for email folder containing portal emails */
    static final public String FOLDER_KEY;
    /** Preferences key for the most recent parse date */
    static public final String LAST_PARSE_DATE_KEY;
    /** The key for saving manual refresh preference */
    static public final String MANUAL_REFRESH_KEY;
    /** Standard American date format */
    static public final String MM_DD_YYYY_FORMAT;
    /** Used for the default key when something is uninitialized */
    static public final String NULL_KEY;
    /** The key for saving portal submission sort preference */
    static public final String SORT_KEY;
    /** Year-day-month format */
    static public final String YYYY_DD_MM_FORMAT;
    /** ISO/International year-month-day format */
    static public final String YYYY_MM_DD_FORMAT;

    static {
        DATE_FORMAT_KEY = "dateFormat";
        DD_MM_YYYY_FORMAT = "dd-MM-yyyy";
        DEFAULT_CAT_KEY = "default-category";
        EMAIL_KEY = "email";
        FOLDER_KEY = "mailFolder";
        LAST_PARSE_DATE_KEY = "parseDate";
        MANUAL_REFRESH_KEY = "manualRefresh";
        MM_DD_YYYY_FORMAT = "MM-dd-yyyy";
        NULL_KEY = "uninitialized";
        SORT_KEY = "sort";
        YYYY_DD_MM_FORMAT = "yyyy-dd-MM";
        YYYY_MM_DD_FORMAT = "yyyy-MM-dd";
    }

    /**
     * @param context
     * @return
     */
    public static DateTimeFormatter getUIFormatter(Context context) {
        String formatString =
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getString(DATE_FORMAT_KEY, NULL_KEY);
        return DateTimeFormat.forPattern(formatString);
    }

    /**
     * Log all preferences
     *
     * @param prefs App preferences
     */
    public static void printAllPrefs(SharedPreferences prefs) {
        Map<String, ?> keys = prefs.getAll();

        if (keys.size() == 0)
            Logger.d("PreferencesHelper#printAllprefs", "NO keys found in prefs");
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Logger.d("PreferencesHelper#printAllprefs", entry.getKey() + ": " +
                    entry.getValue().toString());
        }
    }
}
