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

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;

import com.einzig.ipst2.util.Logger;

import java.io.IOException;

/**
 * @author Ryan Porterfield
 * @since 2017-05-17
 */
class AuthToken implements AccountManagerCallback<Bundle> {
    /**
     * Create an AuthToken container class to get an AuthToken for accessing GMail.
     */
    AuthToken() {
        Logger.d("Creating a new AuthToken");
    }

    /*
     * Get the AuthToken from the AccountManager.
     * After the token has been acquired count down the latch so that the token can be returned from
     * getToken().
     * @sa getToken
     */
    @Override
    public void run(AccountManagerFuture<Bundle> result) {
        Logger.d("Running AuthToken");
        try {
            Bundle bundle = result.getResult();
            Logger.i("authToken -> " + bundle.getString(AccountManager.KEY_AUTHTOKEN));
        } catch (AuthenticatorException e) {
            Logger.e("Could not authenticate:\n" + e);
        } catch (IOException e) {
            Logger.e(e.toString());
        } catch (OperationCanceledException e) {
            Logger.e("Operation cancelled:\n" + e);
        }
    }
}
