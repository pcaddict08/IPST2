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

package com.einzig.ipst2.Activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.einzig.ipst2.R;
import com.einzig.ipst2.Utilities.EmailParseTask;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GoogleApiAvailability;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Main activity class which launches the app.
 * Contains all startup and initialization code.
 *
 * @author Steven Foskett
 * @since 2017-05-15
 */
public class MainActivity extends AppCompatActivity {
    /** ??? */
    static public final String EXTRA_ACCOUNTNAME = "extra_accountname";
    /** String key used for saving and retrieving the user's email address */
    static public final String EMAIL_KEY = "email";
    /** String key for sending date through Bundle */
    static public final String MOST_RECENT_DATE_KEY = "recentDate";
    /** Used for the default key when something is uninitialized */
    static public final String NULL_KEY = "uninitialized";
    /** String key for sending a portal through a bundle */
    static public final String PORTAL_KEY = "portal";
    /** String key for sending a portal list through Bundle */
    static public final String PORTAL_LIST_KEY = "portalList";
    /** The PortalSubmission being combined with a PortalResponded */
    static public final String PORTAL_SUBMISSION_KEY = "portalSubmission";
    /** The key for saving portal submission sort preference */
    static public final String SORT_KEY = "sort";
    /** Used to get the result of LoginActivity */
    static private final int LOGIN_ACTIVITY_CODE = 0;
    /** Tag used for logging for this class */
    static private final String TAG = "IPST:MainActivity";

    static final int REQUEST_CODE_EMAIL = 1;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;

    /** Last date that email was parsed */
    private Date mostRecentDate;
    /** Format for parsing and printing dates */
    private DateFormat dateFormat;
    /** Preferences for saving app settings */
    private SharedPreferences preferences;

    /**
     * MainActivity constructor, initialize variables.
     */
    public MainActivity() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        mostRecentDate = null;
        preferences = null;
    }

    /**
     * Set a mail folder to look for Ingress emails
     *
     * @param folderList List of folders that can be selected.
     */
    public void addCustomFolder(final ArrayList<String> folderList) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                String[] stockArr = new String[folderList.size()];
                stockArr = folderList.toArray(stockArr);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Set Custom Folder To Parse")
                        .setItems(stockArr, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = preferences.edit();
                                Log.d(TAG, "FOLDER: " + folderList.get(which));
                                editor.putString("mail_pref", folderList.get(which));
                                editor.apply();
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(i);
                                finish();
                                overridePendingTransition(0, 0);
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    /**
     * Display an error message dialog
     *
     * @param title         Title for error dialog
     * @param messageText   Message for error dialog
     */
    private void errorFoundMessage(final String title, final String messageText) {
        Log.d(TAG, "Displaying error message");
        this.runOnUiThread(new Runnable() {
            public void run() {
                new android.app.AlertDialog.Builder(MainActivity.this).setTitle(title)
                        .setMessage(messageText)
                        // TODO (Anyone): Move "Ok" string to strings resource
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).show();
            }
        });
    }

    /**
     * Get user account if the user has already logged in.
     *
     * @return Account user logged in on.
     */
    private Account getAccount() {
        String email = preferences.getString(EMAIL_KEY, NULL_KEY);
        Log.i(TAG, "Getting account " + email);
        AccountManager manager = AccountManager.get(this);
        for(Account account: manager.getAccounts()) {
            Log.d(TAG, "Has account " + account.name);
            if(account.name.equals(email) && account.type.equals("com.google"))
                return account;
        }
        return null;
    }

    /**
     * Get the last date that email was parsed.
     *
     * @return last date that email was parsed.
     */
    public Date getMostRecentDate() {
        return mostRecentDate;
    }

    /**
     * Get saved user preferences.
     */
    private void getPreferences() {
        preferences = getPreferences(MODE_PRIVATE);
        Log.i(TAG, EMAIL_KEY + " -> " + preferences.getString(EMAIL_KEY, NULL_KEY));
        Log.i(TAG, MOST_RECENT_DATE_KEY + " -> " + preferences.getString(MOST_RECENT_DATE_KEY, NULL_KEY));
        Log.i(TAG, SORT_KEY + " -> " + preferences.getString(SORT_KEY, NULL_KEY));
        try {
            mostRecentDate = dateFormat.parse(preferences.getString(MOST_RECENT_DATE_KEY, ""));
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * Search through accounts on the user's device now that we have permission to do so.
     */
    public void gotPermission_accounts() {
        try {
            AccountManager tempAM = AccountManager.get(MainActivity.this);
            int numGoogAcct = 0;
            Account[] accountList = tempAM.getAccounts();
            for (Account a : accountList) {
                if (a.type.equals("com.google")) {
                    numGoogAcct++;
                }
            }
            if (numGoogAcct == 0) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("No Accounts");
                alertDialogBuilder
                        .setMessage("You have no Google accounts on your device")//.  Would you like to log in manually?")
                        .setCancelable(true)
                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                Log.d(TAG, "Found Some Accounts");
                // TODO (anyone): Find a way to do this that isn't deprecated
                Intent intent = AccountManager.newChooseAccountIntent(null, null,
                        new String[]{"com.google"}, false, null, null, null, null);
                startActivityForResult(intent, LOGIN_ACTIVITY_CODE);

            }
        } catch (Exception e) { // TODO (Anyone): Catch specific exceptions, not Exception
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("Error Retrieving Accounts");
            alertDialogBuilder
                    .setMessage("There was an error retrieving your Google accounts.")
                    .setCancelable(true)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //manualLogin("Log In Below");
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            Log.d(TAG, "CAUGHT NOT FOUND");
        }
    }

    /**
     * This method is a hook for background threads and async tasks that need to provide the
     * user a response UI when an exception occurs.
     */
    public void handleException(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
                    int statusCode = ((GooglePlayServicesAvailabilityException) e)
                            .getConnectionStatusCode();
                    Dialog dialog = googleAPI.getErrorDialog(MainActivity.this,
                            statusCode,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException) e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    /**
     * Login if we have permission or get permission if we don't.
     *
     * @see MainActivity#gotPermission_accounts()
     */
    public void loginHitMethod() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.GET_ACCOUNTS)
                    == PackageManager.PERMISSION_GRANTED) {
                gotPermission_accounts();
            } else {
                /*  TODO (Steven): The user may have to close and reopen the app here
                 *  since we're not doing anything after getting permission */
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.GET_ACCOUNTS}, REQUEST_CODE_EMAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Callback method if an activity is started for result via startActivityForResult().
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Ryan's code
        Log.d(TAG, "onActivityResult(" + requestCode + ") -> " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != LOGIN_ACTIVITY_CODE || resultCode != RESULT_OK) {
            return;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(EMAIL_KEY, data.getStringExtra(EMAIL_KEY));
        editor.apply();
        Log.d(TAG, "Got account name " + data.getStringExtra(EMAIL_KEY));
        parseEmail();
        // Steven's code
        Account me = getAccount();
        if (me != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // SHOW LOADING THINGIES
            findViewById(R.id.progress_view_mainactivity).setVisibility(View.VISIBLE);
            findViewById(R.id.gmail_login_button).setVisibility(View.INVISIBLE);
        } else {
            errorFoundMessage("Account Not Found",
                              "The account you selected wasn't found on this device.");
        }
    }

    /*
     * Called on startup by Android after the app starts and resources are available.
     * Used for more advanced initializations.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        getPreferences();

        Button gmail_login_button = (Button) findViewById(R.id.gmail_login_button);
        if (gmail_login_button != null) {
            gmail_login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loginHitMethod();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mainactivity, menu);
        return true;
    }

    /**
     * Update the mostRecentDate preference after email has been parsed.
     */
    public void onEmailParse() {
        String dateString = dateFormat.format(mostRecentDate.getTime());
        Log.d(TAG, MOST_RECENT_DATE_KEY + " -> " + dateString);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MOST_RECENT_DATE_KEY, dateString);
        editor.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings_mainactivity:
                startActivity(new Intent(this, SettingsActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // TODO (Ryan): Look into how this works
    /*
     * Provides the results of permission requests
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_EMAIL && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gotPermission_accounts();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // TODO (Ryan): Check master branch to see if this is used
    // TODO (Anyone): Move strings to strings resource
    /*
     * Handles the error when GMail's All Mail folder is missing and the custom folder set by the
     * user is unset, or also missing.
     */
    public void noAllMailFolder(final String customFolder, final ArrayList<String> folderList) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                findViewById(R.id.progress_view_mainactivity).setVisibility(View.INVISIBLE);
                findViewById(R.id.gmail_login_button).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.loading_text_mainactivity)).setText("Loading...");

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if (customFolder.equalsIgnoreCase("")) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error: All Mail Folder Missing")
                            .setMessage("IPST couldn't find the 'All Mail' folder.  Either it's been deleted, or your Gmail is not in English."
                                    + " Currently the 'All Mail' folder is how IPST parses for portal submissions.  \n\n")
                            .setPositiveButton("Set Custom Folder", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    addCustomFolder(folderList);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Do nothing.
                        }
                    }).show();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error: Custom Mail Folder '" + customFolder + "' Missing")
                            .setMessage("IPST couldn't find the '" + customFolder + "' folder.  Either it's been deleted, or is missing for some other reason. \n\n"
                                    + "Would you like to set a new folder to parse, or reset to 'All Mail'?")
                            .setPositiveButton("Set Custom Folder", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    addCustomFolder(folderList);
                                }
                            })
                            .setNeutralButton("All Mail", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    resetAllMail();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }).show();
                }
            }
        });
    }

    /**
     * Parse emails from Niantic
     */
    private void parseEmail() {
        Account account = getAccount();
        if (account != null) {
            new EmailParseTask(this, account).execute();
        } else {
            // TODO (Anyone): Move both strings to strings resource
            errorFoundMessage("Error: Account not found",
                    "Account not found on device, if you would like to log in manually, do so using the 'Manual Log In' button.");
        }
    }

    /**
     * Reset the mail folder preference to GMail's All Mail folder
     */
    public void resetAllMail() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("mail_pref", "All Mail");
        editor.apply();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        finish();
        overridePendingTransition(0, 0);
    }

}
