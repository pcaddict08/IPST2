/******************************************************************************
 *                                                                            *
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
 *                                                                            *
 ******************************************************************************/

package com.einzig.ipst2.parse;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import static com.einzig.ipst2.activities.MainActivity.TAG;

/**
 * @author Ryan Porterfield
 * @since 2017-05-28
 */

public class AuthenticatorTask extends AsyncTask<Void, Void, String> {
    /** The URL to get the OAuth token from */
    static final private String AUTH_URL = "oauth2:https://mail.google.com/";
    /** GMail account used for Ingress */
    private final Account account;
    /** Parent activity of this task */
    private final Activity activity;

    public AuthenticatorTask(Activity activity, Account account) {
        this.account = account;
        this.activity = activity;
    }

    /**
     * Authenticate with GMail.
     *
     * @return the OAuth token as a string.
     */
    private String authenticate() {
        String token = null;
        AccountManagerFuture<Bundle> future = AccountManager.get(activity).getAuthToken(account,
                AUTH_URL, null, activity, new AuthToken(), null);
        try {
            token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
            Log.i(TAG, future.getResult().toString());
        } catch (IOException | AuthenticatorException | OperationCanceledException e) {
            Log.e(TAG, e.toString());
        }
        return token;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return authenticate();
    }
}
