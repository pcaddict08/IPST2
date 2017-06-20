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

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.einzig.ipst2.R;
import com.einzig.ipst2.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Folder;
import javax.mail.MessagingException;

import static com.einzig.ipst2.activities.MainActivity.TAG;

/**
 * @author Ryan Porterfield
 * @since 2017-05-25
 */

public class FolderGetter {
    public static final String DEFAULT_FOLDER = "[Gmail]/All Mail";
    static final private String TEST_FOLDER = "";
    final private Activity activity;
    final private List<Folder> folders;
    final private SharedPreferences preferences;
    private Folder folder;

    FolderGetter(Activity activity, Folder[] folders, SharedPreferences preferences) {
        this.activity = activity;
        this.folders = flattenFolders(folders);
        this.folder = null;
        this.preferences = preferences;
    }

    private List<Folder> flattenFolders(Folder[] folders) {
        List<Folder> folderList = new ArrayList<>();
        for (Folder folder : folders) {
            folderList.add(folder);
            try {
                Folder[] subfolders = folder.list();
                if (subfolders.length > 0)
                    folderList.addAll(flattenFolders(subfolders));
            } catch (MessagingException e) {
                Log.e(TAG, e.toString());
            }
        }
        return folderList;
    }

    /**
     * Set a mail folder to look for Ingress emails
     *
     * @param folderList List of folders that can be selected.
     */
    private void getCustomFolder(final List<Folder> folderList) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                String[] items = getItems(folderList);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Set Custom Folder To Parse");
                builder.setItems(items, getListener());
                builder.show();
            }
        });
    }

    private Folder getDefaultFolder() {
        for (Folder folder : folders) {
            if (folder.getFullName().equals(DEFAULT_FOLDER)) {
                //if (folder.getFullName().equals(TEST_FOLDER)) {
                return folder;
            }
        }
        return null;
    }

    Folder getFolder() throws MessagingException {
        String folderPref = preferences.getString(MainActivity.FOLDER_KEY, MainActivity.NULL_KEY);
        Log.d(TAG, "Folder: " + folderPref);
        Folder folder = null;
        if (folderPref.equals(MainActivity.NULL_KEY))
            folder = getDefaultFolder();
        else {
            for (Folder f : folders) {
                if (f.getFullName().equalsIgnoreCase(folderPref))
                    folder = f;
            }
        }
        Log.d(TAG, "Number of folders: " + folders.size());
        Log.d(TAG, "Folders: ");
        for (Folder f : folders)
            Log.d(TAG, "\t" + f.getFullName());

        if (folder == null)
            folder = noAllMailFolder(folderPref);
        return folder;
    }

    private String[] getItems(List<Folder> folders) {
        String[] items = new String[folders.size()];
        for (int i = 0; i < items.length; ++i)
            items[i] = folders.get(i).getFullName();
        return items;
    }

    private DialogInterface.OnClickListener getListener() {
        return new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "FOLDER: " + folders.get(which));
                folder = folders.get(which);
                Log.d(TAG, MainActivity.FOLDER_KEY + " -> " + folder.getFullName());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(MainActivity.FOLDER_KEY, folder.getFullName());
                editor.apply();
            }
        };
    }

    /**
     * Called when DEFAULT_FOLDER doesn't exist.
     *
     * @param folderPref The folder that was previously being used for emails
     * @return a new folder to parse.
     * @see FolderGetter#DEFAULT_FOLDER
     */
    private Folder noAllMailFolder(final String folderPref) {
        Log.d(TAG, "NO ALL MAIL FOLDER");
        activity.runOnUiThread(new AllMailError(folderPref));
        return folder;
    }

    /**
     * Reset the mail folder preference to GMail's All Mail folder
     */
    private void resetAllMail() {
        folder = getDefaultFolder();
    }

    /**
     * Helper class to display an AlertDialog on the UI.
     * Used when the folder we were checking for emails doesn't exist.
     */
    private class AllMailError implements Runnable {
        /** Dialog message */
        private String message;
        /** Neutral button text */
        private String neutral;
        /** Positive button text */
        private String positive;
        /** Dialog title */
        private String title;

        /**
         * Create a new runnable to handle a missing mail folder error
         *
         * @param previousFolder Previous folder we checked for mail which doesn't exist.
         */
        AllMailError(String previousFolder) {
            Log.d(TAG, "Got to all mail error");
            title = activity.getResources().getString(R.string.error) + previousFolder;
            title += activity.getResources().getString(R.string.foldermissing);
            positive = activity.getResources().getString(R.string.setcustomfolder);
            message = activity.getResources().getString(R.string.cantfindfolder);
            message = String.format(message, previousFolder);
            neutral = "";
            if (previousFolder.equalsIgnoreCase(DEFAULT_FOLDER)) {
                message += activity.getResources().getString(R.string.allmailmissing);
            } else {
                message += activity.getResources().getString(R.string.custommissing);
                neutral = activity.getResources().getString(R.string.allmail);
            }
        }

        /*
         * Override for Runnable.
         * Set some display settings then build and display the AlertDialog.
         */
        @Override
        public void run() {
            activity.findViewById(R.id.progress_view_mainactivity).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.gmail_login_button).setVisibility(View.VISIBLE);
            ((TextView) activity.findViewById(R.id.loading_text_mainactivity))
                    .setText(R.string.loading);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(title).setMessage(message);
            builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    getCustomFolder(folders);
                }
            });
            builder.setCancelable(true);
            if (!neutral.equals(""))
                builder.setNeutralButton(neutral, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        resetAllMail();
                    }
                });
            builder.show();
            Log.d(TAG, "SHOWING THING");

        }
    }
}
