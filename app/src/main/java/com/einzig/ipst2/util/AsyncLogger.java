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

import android.os.Environment;
import android.util.Log;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Ryan Porterfield
 * @since 2017-06-24
 */
class AsyncLogger extends Thread {
    /** Tag used for logging for this class */
    static final private String TAG = "IPST";

    final private BlockingQueue<LogCallback> consoleQueue;
    final private BlockingQueue<LogEntry> fileQueue;
    final private OutputStreamWriter writer;

    private boolean running;

    {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(getLogFile());
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to create log file");
        }

        consoleQueue = new LinkedBlockingDeque<>();
        fileQueue = new LinkedBlockingDeque<>();
        running = true;
        if (stream != null)
            writer = new OutputStreamWriter(stream);
        else
            writer = null;
    }

    /**
     *
     */
    AsyncLogger() {
    }

    /**
     * Nicely concatenate the scope and message together for writing to console
     *
     * @param scope   Scope the message was written from
     * @param message Message to be logged
     * @return scope :: message
     */
    private String concatMessage(String scope, String message) {
        return (scope == null || scope.equals("")) ? message : scope + " :: " + message;
    }

    /**
     * Log a debug message
     *
     * @param scope   Scope the message was written from
     * @param message Message to be logged
     */
    void d(String scope, String message) {
        Log.d(TAG, concatMessage(scope, message));
        log("D", scope, message);
    }
    
    void dAsync(String scope, String message) {
        logAsync("D", scope, message, new DebugCallback(message));
    }

    /**
     * Log an error message
     *
     * @param scope   Scope the message was written from
     * @param message Message to be logged
     */
    void e(String scope, String message) {
        Log.e(TAG, concatMessage(scope, message));
        log("E", scope, message);
    }

    private boolean externalMediaWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Get the name of the file to write logs to
     *
     * @return name of the file to write logs to
     */
    private String getFilename() {
        DateTimeFormatter formatter = ISODateTimeFormat.basicDate();
        return "IPST_Logs_" + formatter.print(LocalDateTime.now()) + ".txt";
    }

    /**
     * Get the File object to write logs to
     *
     * @return File object to write logs to
     */
    private File getLogFile() throws FileNotFoundException {
        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/Download");
        File logFile = new File(dir, getFilename());
        Log.i(TAG, "Writing to " + logFile.getAbsolutePath());
        return logFile;
    }

    /**
     * Log an info message
     *
     * @param scope   Scope the message was written from
     * @param message Message to be logged
     */
    void i(String scope, String message) {
        Log.i(TAG, concatMessage(scope, message));
        log("I", scope, message);
    }

    /**
     * Write log to log file
     *
     * @param level   Severity of the log
     * @param scope   Scope the message was written from
     * @param message Message to be logged
     */
    private void log(String level, String scope, String message) {
        fileQueue.add(new LogEntry(level, LocalDateTime.now(), scope, message));
    }

    /**
     * Asynchronously calls android.util.Log as well as writes log to file
     *
     * @param level   Severity of the log
     * @param scope   Scope the message was written from
     * @param message Message to be logged
     * @param callback Callback method that can be called to asynchronously log to console
     */
    private void logAsync(String level, String scope, String message, LogCallback callback) {
        consoleQueue.add(callback);
        fileQueue.add(new LogEntry(level, LocalDateTime.now(), scope, message));
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (consoleQueue.size() > 0) {
                    LogCallback callback = consoleQueue.take();
                    callback.run();
                }
                if (writer != null) {
                    if (fileQueue.size() == 0)
                        writer.flush();
                    LogEntry log = fileQueue.take();
                    writer.write(log.toString());
                }
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    /**
     * Log a verbose message
     *
     * @param scope   Scope the message was written from
     * @param message Message to be logged
     */
    void v(String scope, String message) {
        Log.v(TAG, concatMessage(scope, message));
        log("V", scope, message);
    }

    void vAsync(String scope, String message) {
        logAsync("V", scope, message, new VerboseCallback(message));
    }
    
    /**
     * Log a warning message
     *
     * @param scope   Scope the message was written from
     * @param message Message to be logged
     */
    void w(String scope, String message) {
        Log.w(TAG, concatMessage(scope, message));
        log("W", scope, message);
    }

    /**
     * Log a WTF message
     *
     * @param scope   Scope the message was written from
     * @param message Message to be logged
     */
    void wtf(String scope, String message) {
        Log.wtf(TAG, concatMessage(scope, message));
        log("WTF", scope, message);
    }

    /**
     *
     */
    private abstract class LogCallback implements Runnable {
        final String message;

        private LogCallback(String message) {
            this.message = message;
        }
    }

    private class DebugCallback extends LogCallback {
        private DebugCallback(String message) {
            super(message);
        }

        public void run() {
            Log.d(TAG, message);
        }
    }

    private class ErrorCallback extends LogCallback {
        private ErrorCallback(String message) {
            super(message);
        }

        public void run() {
            Log.e(TAG, message);
        }
    }

    private class InfoCallback extends LogCallback {
        private InfoCallback(String message) {
            super(message);
        }

        public void run() {
            Log.i(TAG, message);
        }
    }

    private class VerboseCallback extends LogCallback {
        private VerboseCallback(String message) {
            super(message);
        }

        public void run() {
            Log.v(TAG, message);
        }
    }

    private class WarnCallback extends LogCallback {
        private WarnCallback(String message) {
            super(message);
        }

        public void run() {
            Log.w(TAG, message);
        }
    }

    private class WTFCallback extends LogCallback {
        private WTFCallback(String message) {
            super(message);
        }

        public void run() {
            Log.wtf(TAG, message);
        }
    }
}
