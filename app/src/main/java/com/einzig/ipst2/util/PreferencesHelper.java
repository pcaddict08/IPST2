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

import com.einzig.ipst2.R;
import com.einzig.ipst2.parse.FolderGetter;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map;

/**
 * @author Steven Foskett
 * @since 2017-06-14
 */
public class PreferencesHelper {
    /** Application context for accessing resources */
    final private Context context;
    /** User settings */
    final private SharedPreferences preferences;

    /**
     * @param context Application context
     */
    public PreferencesHelper(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * @return Preferences value for descending alpha-numeric sort
     */
    public String alphaNumericDescSort() {
        return context.getString(R.string.alphaNumDescSort);
    }

    /**
     * @return Preferences value for alpha-numeric sort
     */
    public String alphaNumericSort() {
        return context.getString(R.string.alphaNumSort);
    }

    /**
     * Clear all preferences
     */
    public void clearAll() {
        preferences.edit().clear().apply();
    }

    /**
     * @return Preferences key for date formats
     */
    public String dateFormatKey() {
        return context.getString(R.string.dateFormatKey);
    }

    /**
     * @return The key for saving default category preference
     */
    public String defaultTabKey() {
        return context.getString(R.string.defaultTabKey);
    }

    /**
     * @return Day-month-year format
     */
    public String dmyFormat() {
        return context.getString(R.string.dmyFormat);
    }

    /**
     * @return Preferences key used for saving and retrieving the user's email address
     */
    public String emailKey() {
        return context.getString(R.string.emailKey);
    }

    /**
     * @return Preferences key for email folder containing portal emails
     */
    public String folderKey() {
        return context.getString(R.string.folderKey);
    }

    /*
    * @return Preferences key for reset thing
    * */
    public String resetKey() {
        return context.getString(R.string.resetkey_prefs);
    }

    /**
     * Get a preference value
     * @param key Preference key
     * @return value of key
     */
    public String get(String key) {
        return preferences.getString(key, nullKey());
    }

    /*
    *  Get a boolean preference value
    *  @param key Preference key
    *  @return value of key
    * */
    public boolean getBool(String key)
    {
        return preferences.getBoolean(key, false);
    }

    /**
     * Get the value of manual refresh preference
     * @return value of manual refresh preference
     */
    public boolean getManualRefresh() {
        return preferences.getBoolean(refreshKey(), false);
    }

    /**
     * @return Date formatter for displaying a date on the UI
     */
    public DateTimeFormatter getUIFormatter() {
        String formatString = preferences.getString(dateFormatKey(), nullKey());
        return DateTimeFormat.forPattern(formatString);
    }

    /**
     * Initialize preferences
     */
    public void initPreferences() {
        if (!isInitialized(dateFormatKey()))
            set(dateFormatKey(), mdyFormat());
        if (!isInitialized(sortKey()))
            set(sortKey(), responseDateSort());
        if (!isInitialized(folderKey()))
            set(folderKey(), FolderGetter.DEFAULT_FOLDER);
        if (!isInitialized(resetKey()))
            set(resetKey(), "done");
    }

    /**
     * Check if a preference exists
     * @param key Preference key
     * @return true if the preference exists, otherwise false
     */
    public boolean isInitialized(String key) {
        Logger.d("CHECKING IF " + key + " IS INITIALIZED: " + preferences.getString(key, nullKey()));
        return !preferences.getString(key, nullKey()).equals(nullKey());
    }

    /**
     * @return Standard American date format
     */
    public String mdyFormat() {
        return context.getString(R.string.mdyFormat);
    }

    /**
     * @return default key when something is uninitialized
     */
    public String nullKey() {
        return context.getString(R.string.nullKey);
    }

    /**
     * @return Preferences key for the most recent parse date
     */
    public String parseDateKey() {
        return context.getString(R.string.parseDateKey);
    }

    /**
     * @return Preferences key for the theme setting
     */
    public String themeKey() { return context.getString(R.string.theme_key); }

    /**
     * Log all preferences
     */
    public void printAllPreferences() {
        Map<String, ?> keys = preferences.getAll();
        if (keys.size() == 0)
            Logger.d("PreferencesHelper#printAllprefs", "NO keys found in prefs");
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Logger.d("PreferencesHelper#printAllprefs", entry.getKey() + ": " +
                    entry.getValue().toString());
        }
    }

    /**
     * @return The key for saving manual refresh preference
     */
    public String refreshKey() {
        return context.getString(R.string.refreshKey);
    }

    /**
     * @return Preferences value for descending response date sort
     */
    public String responseDateDescSort() {
        return context.getString(R.string.responseDateDescSort);
    }

    /**
     * @return Preferences value for response date sort
     */
    public String responseDateSort() {
        return context.getString(R.string.responseDateSort);
    }

    /**
     * Set a preference
     * @param key Preference key
     * @param value Preference value
     */
    public void set(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * @return The key for saving portal submission sort preference
     */
    public String sortKey() {
        return context.getString(R.string.sortKey);
    }

    /**
     * @return Preferences value for descending submission date sort
     */
    public String submissionDateDescSort() {
        return context.getString(R.string.submissionDateDescSort);
    }

    /**
     * @return Preferences value for submission date sort
     */
    public String submissionDateSort() {
        return context.getString(R.string.submissionDateSort);
    }

    /**
     * @return Preferences value for year-day-month date format
     */
    public String ydmFormat() {
        return context.getString(R.string.ydmFormat);
    }

    /**
     * @return Preferences value for ISO/International year-month-day date format
     */
    public String ymdFormat() {
        return context.getString(R.string.ymdFormat);
    }
}
