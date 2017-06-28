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

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.einzig.ipst2.database.DatabaseInterface;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static com.einzig.ipst2.activities.MainActivity.REQUEST_CODE_WRITE_EXTERNAL;

/**
 * @author Ryan Porterfield
 * @since 2017-06-24
 */
public class LogExporter extends AsyncTask<Void, Void, String> {
    /** Application activity */
    final private Activity activity;

    /* Filename to show */
    String filename;
    /**
     * Create a task to export logs from the database
     * @param activity Parent activity
     */
    public LogExporter(Activity activity) {
        this.activity = activity;
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL);
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(String filename) {
        if(filename != null)
            Toast.makeText(activity, "Log Exported: " + filename, Toast.LENGTH_LONG).show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (!externalMediaWritable() || !checkPermissions())
            return null;
        try {
            FileOutputStream stream = new FileOutputStream(getLogFile());
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            Logger.i("LogExporter", "Writing logs");
            for (LogEntry entry : getLogs())
                writer.write(entry.toString());
            writer.close();
            stream.close();
            Logger.i("LogExporter", "Write finished");
        } catch (IOException e) {
            Logger.e("LogExporter", e.toString());
        }
        return filename;
    }

    /**
     * Get the name of the file to write logs to
     * @return name of the file to write logs to
     */
    private String getFilename() {
        DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinuteSecond();
        return "IPST_Logs_" + formatter.print(LocalDateTime.now()) + ".txt";
    }

    /**
     * Get the File object to write logs to
     * @return File object to write logs to
     */
    private File getLogFile() throws FileNotFoundException {
        File root = Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/Download");
        this.filename = getFilename();
        File logFile = new File(dir, filename);
        Logger.i("LogExporter", "Writing to " + logFile.getAbsolutePath());
        return logFile;
    }

    /**
     * Get the list of logs to export
     * @return list of logs to export
     */
    private List<LogEntry> getLogs() {
        return new DatabaseInterface(activity).getLogs();
    }

    private boolean externalMediaWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}
