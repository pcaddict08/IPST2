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

import com.einzig.ipst2.activities.MainActivity;
import com.einzig.ipst2.database.DatabaseInterface;

import static com.einzig.ipst2.activities.MainActivity.TAG;

/**
 * @author Ryan Porterfield
 * @since 2017-06-24
 */

public class Logger {
    static private DatabaseInterface db;
    static private boolean initialized;

    static {
        db = null;
        initialized = false;
    }

    static public void d(String log) {
        Log.d(TAG, log);
    }

    static public void initialize(Context context) {
        db = new DatabaseInterface(context);
        initialized = true;
    }

    static private void log(String log, LogCallback callback) {
        callback.log(log);
        if (!initialized)
            return;
    }

    /**
     * @author Ryan Porterfield
     * @since 2017-06-24
     */
    private interface LogCallback {
        void log(String log);
    }

    /**
     *
     */
    static private class DebugCallback implements LogCallback {
        public void log(String log) {
            Log.d(TAG, log);
        }
    }
}
