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

package com.einzig.ipst2;

/*
 * Created by Steven Foskett on 7/25/2017.
 */

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.PlayStoreListener;

import android.app.Activity;
import android.app.Application;
import android.widget.Toast;

import com.einzig.ipst2.billing.Encryption;

import javax.annotation.Nonnull;

public class CheckoutApplication extends Application {
    final public static String Billing_KEY = "INSERT KEY HERE";
    @Nonnull
    private final Billing mBilling = new Billing(this, new Billing.DefaultConfiguration() {
        @Nonnull
        @Override
        public String getPublicKey() {
            // encrypted public key of the app. Plain version can be found in Google Play's Developer
            // Console in Service & APIs section under "YOUR LICENSE KEY FOR THIS APPLICATION" title.
            // A naive encryption algorithm is used to "protect" the key. See more about key protection
            // here: https://developer.android.com/google/play/billing/billing_best_practices.html#key
            //return Encryption.decrypt(s, "se.solovyev@gmail.com");
            return Billing_KEY;
        }
    });

    /**
     * Returns an instance of {@link CheckoutApplication} attached to the passed activity.
     */
    public static CheckoutApplication get(Activity activity) {
        return (CheckoutApplication) activity.getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBilling.addPlayStoreListener(new PlayStoreListener() {
            @Override
            public void onPurchasesChanged() {
                Toast.makeText(CheckoutApplication.this, R.string.purchases_changed, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Nonnull
    public Billing getBilling() {
        return mBilling;
    }
}