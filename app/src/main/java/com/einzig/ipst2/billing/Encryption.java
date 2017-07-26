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

package com.einzig.ipst2.billing;

/*
 * Created by Steven Foskett on 7/25/2017.
 */

import android.util.Base64;

import javax.annotation.Nonnull;

/**
 * Simple (and stupid) encryption algorithm. Should not be used in real apps.
 */
public final class Encryption {

    @Nonnull
    public static String decrypt(@Nonnull String message, @Nonnull String salt) {
        return xor(new String(Base64.decode(message, 0)), salt);
    }

    @Nonnull
    static String encrypt(@Nonnull String message, @Nonnull String salt) {
        return new String(Base64.encode(xor(message, salt).getBytes(), 0));
    }

    /**
     * Encrypts or decrypts a base-64 string using a XOR cipher.
     */
    @Nonnull
    private static String xor(@Nonnull String message, @Nonnull String salt) {
        final char[] m = message.toCharArray();
        final int ml = m.length;

        final char[] s = salt.toCharArray();
        final int sl = s.length;

        final char[] res = new char[ml];
        for (int i = 0; i < ml; i++) {
            res[i] = (char) (m[i] ^ s[i % sl]);
        }
        return new String(res);
    }

}