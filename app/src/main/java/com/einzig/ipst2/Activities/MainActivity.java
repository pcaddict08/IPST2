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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.einzig.ipst2.Objects.SingletonClass;
import com.einzig.ipst2.R;
import com.einzig.ipst2.Utilities.Utilities;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    /** ??? */
    static public final String EXTRA_ACCOUNTNAME = "extra_accountname";
    /** Used to get the result of LoginActivity */
    static private final int LOGIN_ACTIVITY_CODE = 0;
    /** I don't know what this is for */
    static private final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    /** Tag used for logging for this class */
    static private final String TAG = "IPST:MainActivity";

    static final int REQUEST_CODE_EMAIL = 1;
    static final int REQUEST_CODE_CREDS = 2;
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;

    /**  */
    public final String EMAIL_KEY = getString(R.string.emailKey);
    /** String key for sending date through Bundle. */
    public final String MOST_RECENT_DATE_KEY = getString(R.string.recentDateKey);
    /** Used for the default key when something is uninitialized */
    public final String NULL_KEY = getString(R.string.nullKey);
    /** String key for sending a portal through a bundle */
    public final String PORTAL_KEY = getString(R.string.portalKey);
    /** String key for sending a portal list through Bundle. */
    public final String PORTAL_LIST_KEY = getString(R.string.portalListKey);
    /** The PortalSubmission being combined with a PortalResponded */
    public final String PORTAL_SUBMISSION_KEY = getString(R.string.portalSubmissionKey);
    /**  */
    public final String SORT_KEY = getString(R.string.sortKey);

    private Date mostRecentDate;
    private DateFormat dateFormat;
    private SharedPreferences preferences;

    public MainActivity() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        mostRecentDate = null;
        preferences = null;
    }

    public void addCustomFolder(final ArrayList<String> folderList) {
        this.runOnUiThread(new Runnable() {
            public void run() {

                String[] stockArr = new String[folderList.size()];
                stockArr = folderList.toArray(stockArr);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Set Custom Folder To Parse")
                        .setItems(stockArr, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ArrayList<String> some_array = folderList;
                                SharedPreferences.Editor editor = SingletonClass.getInstance().getSharedPref().edit();
                                Utilities.print_debug("FOLDER: " + some_array.get(which));
                                editor.putString("mail_pref", some_array.get(which));
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

    public Date getMostRecentDate() {
        return mostRecentDate;
    }

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

    public void gotPermission_accounts() {
        try {
            Account[] accountList = null;
            AccountManager tempAM = AccountManager.get(MainActivity.this);
            int numGoogAcct = 0;
            accountList = tempAM.getAccounts();
            if (accountList != null) {
                for (Account a : accountList) {
                    if (a.type.equals("com.google")) {
                        numGoogAcct++;
                    }
                }
                if (numGoogAcct == 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle("No Accounts");
                    alertDialogBuilder
                            .setMessage("You have no google accounts on your device")//.  Would you like to log in manually?")
                            .setCancelable(true)
                            .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    Utilities.print_debug("Found Some Accounts");
                    Intent intent = AccountManager.newChooseAccountIntent(null, null,
                            new String[]{"com.google"}, false, null, null, null, null);
                    startActivityForResult(intent, 1);

                }
            }
        } catch (Exception e) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("Error Retrieving Accounts");
            alertDialogBuilder
                    .setMessage("There was an error retreiving your google accounts.")
                    .setCancelable(true)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //manualLogin("Log In Below");
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            Utilities.print_debug("CAUGHT NOT FOUND");
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
                    int statusCode = ((GooglePlayServicesAvailabilityException) e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            MainActivity.this,
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

    public void loginHitMethod() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.GET_ACCOUNTS)
                    == PackageManager.PERMISSION_GRANTED) {
                gotPermission_accounts();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.GET_ACCOUNTS}, REQUEST_CODE_EMAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + ") -> " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != LOGIN_ACTIVITY_CODE || resultCode != RESULT_OK) {
            return;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(EMAIL_KEY, data.getStringExtra(EMAIL_KEY));
        editor.apply();
        Log.d(TAG, "Got account name " + data.getStringExtra(EMAIL_KEY));
        //parseEmail();

        Account me = getAccount();
        if (me != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            /* SHOW LOADING THINGIES
            */
            findViewById(R.id.progress_view_mainactivity).setVisibility(View.VISIBLE);
            findViewById(R.id.gmail_login_button).setVisibility(View.INVISIBLE);
        } else {
            Utilities.show_warning("Account Not Found", "The account you selected wasn't found on this device.", MainActivity.this);
        }
    }

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

    /**
     *
     */
    public void onEmailParse() {
        String dateString = dateFormat.format(mostRecentDate.getTime());
        Log.d(TAG, MOST_RECENT_DATE_KEY + " -> " + dateString);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MOST_RECENT_DATE_KEY, dateString);
        editor.apply();
    }

    // TODO (Ryan): Look into how this works
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

    public void resetAllMail() {
        SharedPreferences.Editor editor = SingletonClass.getInstance().getSharedPref().edit();
        editor.putString("mail_pref", "All Mail");
        editor.apply();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        finish();
        overridePendingTransition(0, 0);
    }

}
