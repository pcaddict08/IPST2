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

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Steven Foskett on 6/14/2017.
 */
public class PreferencesHelper {
    public static void printAllPrefs(SharedPreferences prefs) {
        Map<String, ?> keys = prefs.getAll();

        if (keys.size() == 0)
            Logger.d("NO keys found in prefs");
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Logger.d("map values", entry.getKey() + ": " +
                    entry.getValue().toString());
        }
    }

    public static SimpleDateFormat getSDF(Context context) {
        SimpleDateFormat sdf = null;
        String formatString =
                PreferenceManager.getDefaultSharedPreferences(context).getString("date-type", "");
        switch (formatString) {
        case "":
            sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
            break;
        case "monthdayyear":
            sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());

            break;
        case "daymonthyear":
            sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            break;
        case "yearmonthday":
            sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            break;
        case "yeardaymonth":
            sdf = new SimpleDateFormat("yyyy-dd-MM", Locale.getDefault());

            break;
        }
        return sdf;
    }
}
