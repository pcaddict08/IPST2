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

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.einzig.ipst2.activities.MainActivity.REQUEST_CODE_ALL;
import static com.einzig.ipst2.activities.MainActivity.REQUEST_CODE_EMAIL;

/**
 * Created by Steven Foskett on 7/13/2017.
 */

public class PermissionsHelper {
    /**
     * TODO Save a user preference if they deny us WRITE_EXTERNAL_STORAGE so we don't ask for it
     * Request permissions if we don't have them
     *
     * @return true if we have all permissions, otherwise false
     */
    public static boolean checkPermissions(Context context) {
        boolean hasPermissions = hasAccountsPermission(context) && hasWritePermission(context);
        String[] permissions = {GET_ACCOUNTS, WRITE_EXTERNAL_STORAGE};
        if (!hasPermissions)
            ActivityCompat.requestPermissions((Activity)context, permissions, REQUEST_CODE_ALL);
        return hasPermissions;
    }

    /**
     * @return true if we have permission to access user accounts, otherwise false
     */
    public static boolean hasAccountsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, GET_ACCOUNTS) == PERMISSION_GRANTED;
    }

    /**
     * @return true if we have permission to write to external storage, otherwise false
     */
    public static boolean hasWritePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) ==
                PERMISSION_GRANTED;
    }


    public static boolean getAccountsPermission(Context context) {
        boolean hasPermission = hasAccountsPermission(context);
        if (!hasPermission)
            ActivityCompat.requestPermissions((Activity)context, new String[]{GET_ACCOUNTS},
                    REQUEST_CODE_EMAIL);
        return hasPermission;
    }
}
