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

import android.provider.BaseColumns;

/**
 * @author Ryan Porterfield
 * @since 2017-06-24
 */
class LoggingContract {
    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + LoggingEntry.TABLE_LOGGING + " (" +
                    LoggingEntry._ID + " INTEGER PRIMARY KEY," +
                    LoggingEntry.COLUMN_LOG_LEVEL + " INTEGER NOT NULL," +
                    LoggingEntry.COLUMN_LOG_TIME + " DATETEIME NOT NULL," +
                    LoggingEntry.COLUMN_LOG_SCOPE + " TEXT," +
                    LoggingEntry.COLUMN_LOG_MESSAGE + " TEXT NOT NULL)";

    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LoggingEntry.TABLE_LOGGING;

    private LoggingContract() {
    }

    static private class LoggingEntry implements BaseColumns {
        static final String COLUMN_LOG_LEVEL = "logLevel";
        static final String COLUMN_LOG_MESSAGE = "logMessage";
        static final String COLUMN_LOG_SCOPE = "logScope";
        static final String COLUMN_LOG_TIME = "logTime";
        static final String TABLE_LOGGING = "logging";
    }
}
