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
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.einzig.ipst2.database.DatabaseInterface;

import org.joda.time.LocalDateTime;

/**
 * @author Ryan Porterfield
 * @since 2017-06-24
 */

public class Logger {
    /** Tag used for logging for this class */
    static private final String TAG = "IPST";
    /** Save logs to the database */
    static private DatabaseInterface db;
    /** Do we have a database handle yet? */
    static private boolean initialized;

    static {
        db = null;
        initialized = false;
    }

    /**
     * Nicely concatenate the scope and message together for writing to console
     * @param scope Scope the message was written from
     * @param message Message to be logged
     * @return scope :: message
     */
    static private String concatMessage(String scope, String message) {
        return (scope == null || scope.equals("")) ? message : scope + " :: " + message;
    }

    /**
     * Log a debug message
     * @param message Message to be logged
     */
    static public void d(String message) {
        d("", message);
    }

    /**
     * Log a debug message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void d(String scope, String message) {
        Log.d(TAG, concatMessage(scope, message));
        log("D", scope, message);
    }

    /**
     * Log an error message
     * @param message Message to be logged
     */
    static public void e(String message) {
        e("", message);
    }

    /**
     * Log an error message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void e(String scope, String message) {
        Log.e(TAG, concatMessage(scope, message));
        log("E", scope, message);
    }

    /**
     * Log an info message
     * @param message Message to be logged
     */
    static public void i(String message) {
        i("", message);
    }

    /**
     * Log an info message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void i(String scope, String message) {
        Log.i(TAG, concatMessage(scope, message));
        log("I", scope, message);
    }

    /**
     * Give the logger a context to create the DatabaseInterface with
     * @param context Context for DatabaseInterface
     */
    static public void initialize(Context context) {
        if (initialized)
            return;
        db = new DatabaseInterface(context);
        initialized = true;
    }

    /**
     * If Logger has been initialized write a log to the database, otherwise do nothing.
     * @param level Severity of the log
     * @param scope Scope the message was written from
     * @param message Message to be logged
     * @see Logger#initialized
     */
    static private void log(String level, String scope, String message) {
        if (!initialized)
            return;
        Crashlytics.log(level + "-" + scope + "-" + message);
       // LocalDateTime now = LocalDateTime.now();
       // LogEntry entry = new LogEntry(level, now, scope, message);
       // db.addLog(entry);
    }

    /**
     * Log a verbose message
     * @param message Message to be logged
     */
    static public void v(String message) {
        v("", message);
    }

    /**
     * Log a verbose message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void v(String scope, String message) {
        Log.v(TAG, concatMessage(scope, message));
        log("V", scope, message);
    }

    /**
     * Log a warning message
     * @param message Message to be logged
     */
    static public void w(String message) {
        w("", message);
    }

    /**
     * Log a warning message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void w(String scope, String message) {
        Log.w(TAG, concatMessage(scope, message));
        log("W", scope, message);
    }

    /**
     * Log a WTF message
     * @param message Message to be logged
     */
    static public void wtf(String message) {
        wtf("", message);
    }

    /**
     * Log a WTF message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void wtf(String scope, String message) {
        Log.wtf(TAG, concatMessage(scope, message));
        log("WTF", scope, message);
    }
}
