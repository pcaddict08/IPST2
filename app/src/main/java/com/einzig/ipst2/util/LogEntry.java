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

import org.joda.time.LocalDate;

import static com.einzig.ipst2.database.DatabaseInterface.DATE_FORMATTER;

/**
 * @author Ryan Porterfield
 * @since 2017-06-24
 */

public class LogEntry {
    final private int level;
    final private String message;
    final private LocalDate time;
    final private String scope;

    public LogEntry(int level, LocalDate time, String scope, String message) {
        this.level = level;
        this.message = message;
        this.time = time;
        this.scope = scope;
    }

    public int getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public LocalDate getTime() {
        return time;
    }

    public String getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return message +
                "\n\tLevel: " + level +
                "\n\tTime: " + DATE_FORMATTER.print(time) +
                "\n\tScope: " + scope;
    }
}
