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

/**
 * @author Ryan Porterfield
 * @since 2017-06-24
 */

public class Logger {
    /**  */
    static final private AsyncLogger logger;

    static {
        logger = new AsyncLogger();
        logger.start();
    }

    /**
     * Log a d message
     * @param message Message to be logged
     */
    static public void d(String message) {
        logger.dAsync("", message);
    }

    /**
     * Log a d message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void d(String scope, String message) {
        logger.dAsync(scope, message);
    }

    /**
     * Log an e message
     * @param message Message to be logged
     */
    static public void e(String message) {
        logger.e("", message);
    }

    /**
     * Log an e message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void e(String scope, String message) {
        logger.e(scope, message);
    }

    /**
     * Log an i message
     * @param message Message to be logged
     */
    static public void i(String message) {
        logger.i("", message);
    }

    /**
     * Log an i message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void i(String scope, String message) {
        logger.i(scope, message);
    }

    /**
     * Log a v message
     * @param message Message to be logged
     */
    static public void v(String message) {
        logger.vAsync("", message);
    }

    /**
     * Log a v message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void v(String scope, String message) {
        logger.vAsync(scope, message);
    }

    /**
     * Log a warning message
     * @param message Message to be logged
     */
    static public void w(String message) {
        logger.w("", message);
    }

    /**
     * Log a warning message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void w(String scope, String message) {
        logger.w(scope, message);
    }

    /**
     * Log a WTF message
     * @param message Message to be logged
     */
    static public void wtf(String message) {
        logger.wtf("", message);
    }

    /**
     * Log a WTF message
     * @param scope Scope the message was written from
     * @param message Message to be logged
     */
    static public void wtf(String scope, String message) {
        logger.wtf(scope, message);
    }
}
