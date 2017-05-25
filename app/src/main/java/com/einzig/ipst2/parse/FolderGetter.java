/* ********************************************************************************************** *
 * ********************************************************************************************** *
 *                                                                                                *
 * Copyright 2017 Steven Foskett, Jimmy Ho, Ryan Porterfield                                      *
 *                                                                                                *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software  *
 * and associated documentation files (the "Software"), to deal in the Software without           *
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,     *
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the  *
 * Software is furnished to do so, subject to the following conditions:                           *
 *                                                                                                *
 * The above copyright notice and this permission notice shall be included in all copies or       *
 * substantial portions of the Software.                                                          *
 *                                                                                                *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING  *
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND     *
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,   *
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.        *
 *                                                                                                *
 * ********************************************************************************************** *
 * ********************************************************************************************** */

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

/**
 * @author Ryan Porterfield
 * @since 2017-05-25
 */

class FolderGetter {
    static final private String DEFAULT_FOLDER = "[Gmail]/All Mail";
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
            //if (!(folder.getName().contains("[") && folder.getName().contains("]")))
            folderList.add(folder);
            try {
                Folder[] subfolders = folder.list();
                if (subfolders.length > 0)
                    folderList.addAll(flattenFolders(subfolders));
            } catch (MessagingException e) {
                Log.e(MainActivity.TAG, e.toString());
            }
        }
        return folderList;
    }

    /**
     * Set a mail folder to look for Ingress emails
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
                return folder;
            }
        }
        return null;
    }

    Folder getFolder() throws MessagingException {
        String folderPref = preferences.getString(MainActivity.FOLDER_KEY, MainActivity.NULL_KEY);
        Log.d(MainActivity.TAG, "Folder: " + folderPref);
        Folder folder = null;
        if (folderPref.equals(MainActivity.NULL_KEY))
            folder = getDefaultFolder();
        else {
            for (Folder f : folders) {
                if (f.getFullName().equalsIgnoreCase(folderPref))
                    folder = f;
            }
        }
        Log.d(MainActivity.TAG, "Number of folders: " + folders.size());
        Log.d(MainActivity.TAG, "Folders: ");
        for (Folder f : folders)
            Log.d(MainActivity.TAG, "\t" + f.getFullName());

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
                Log.d(MainActivity.TAG, "FOLDER: " + folders.get(which));
                folder = folders.get(which);
            }
        };
    }

    /**
     * Handles the error when GMail's All Mail folder is missing and the custom folder set by the
     * user is unset, or also missing.
     * @param customFolder
     * @return
     */
    private Folder noAllMailFolder(final String customFolder) {
        activity.runOnUiThread(new AllMailError(customFolder));
        return folder;
    }

    /**
     * Reset the mail folder preference to GMail's All Mail folder
     */
    private void resetAllMail() {
        folder = getDefaultFolder();
    }

    /** TODO (Anyone): Move strings to strings resource
     *
     */
    private class AllMailError implements Runnable {
        final private String title;
        final private String message;
        final private String positive;
        final private String neutral;

        AllMailError(String customFolder) {
            title = "Error: " + customFolder + " Folder Missing";
            positive = "Set Custom Folder";
            if (customFolder.equalsIgnoreCase(DEFAULT_FOLDER)) {
                message = "IPST couldn't find the 'All Mail' folder. Either it's been deleted,"
                        + " or your GMail is not in English. Currently the 'All Mail' folder"
                        + " is how IPST parses for portal submissions.\n\n";
                neutral = "";
            } else {
                message = "IPST couldn't find the '" + customFolder + "' folder."
                        + " Either it's been deleted, or is missing for some other reason.\n\n"
                        + "Would you like to set a new folder to parse, or reset to 'All Mail'?";
                neutral = "All Mail";
            }
        }

        @Override
        public void run() {
            activity.findViewById(R.id.progress_view_mainactivity).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.gmail_login_button).setVisibility(View.VISIBLE);
            ((TextView) activity.findViewById(R.id.loading_text_mainactivity)).setText("Loading...");
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
        }
    }
}
