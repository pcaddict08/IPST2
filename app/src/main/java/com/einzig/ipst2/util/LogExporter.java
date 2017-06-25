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
import android.os.AsyncTask;
import android.os.Environment;

import com.einzig.ipst2.database.DatabaseInterface;

import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static com.einzig.ipst2.database.DatabaseInterface.DATE_FORMATTER;

/**
 * @author Ryan Porterfield
 * @since 2017-06-24
 */
public class LogExporter extends AsyncTask<Void, Void, Void> {
    /** Application context */
    final private Context context;

    /**
     * Create a task to export logs from the database
     * @param context Application context
     */
    public LogExporter(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        File logFile = getLogFile();
        try {
            if (!logFile.createNewFile()) {
                Logger.e("LogExporter", "Unable to create log file");
                return null;
            }
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(logFile));
            for (LogEntry entry : getLogs())
                writer.write(entry.toString() + "\n\n");
            writer.close();
        } catch (IOException e) {
            Logger.e("LogExporter", e.toString());
        }
        return null;
    }

    /**
     * Get the name of the file to write logs to
     * @return name of the file to write logs to
     */
    private String getFilename() {
        LocalDate now = LocalDate.now();
        return "IPST Logs " + DATE_FORMATTER.print(now) + ".txt";
    }

    /**
     * Get the File object to write logs to
     * @return File object to write logs to
     */
    private File getLogFile() {
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                getFilename());
    }

    /**
     * Get the list of logs to export
     * @return list of logs to export
     */
    private List<LogEntry> getLogs() {
        return new DatabaseInterface(context).getLogs();
    }
}
