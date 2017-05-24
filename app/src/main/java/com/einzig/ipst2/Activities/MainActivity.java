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

package com.einzig.ipst2.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.einzig.ipst2.R;
import com.einzig.ipst2.parse.EmailParseTask;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Main activity class which launches the app.
 * Contains all startup and initialization code.
 *
 * @author Steven Foskett
 * @since 2017-05-15
 */
public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    /**
     * ???
     */
    static public final String EXTRA_ACCOUNTNAME = "extra_accountname";
    /**
     * String key used for saving and retrieving the user's email address
     */
    static public final String EMAIL_KEY = "email";
    /**
     * String key for sending date through Bundle
     */
    static public final String MOST_RECENT_DATE_KEY = "recentDate";
    /**
     * Used for the default key when something is uninitialized
     */
    static public final String NULL_KEY = "uninitialized";
    /**
     * String key for sending a portal through a bundle
     */
    static public final String PORTAL_KEY = "portal";
    /**
     * String key for sending a portal list through Bundle
     */
    static public final String PORTAL_LIST_KEY = "portalList";
    /**
     * The PortalSubmission being combined with a PortalResponded
     */
    static public final String PORTAL_SUBMISSION_KEY = "portalSubmission";
    /**
     * The key for saving portal submission sort preference
     */
    static public final String SORT_KEY = "sort";
    /**
     * Tag used for logging for this class
     */
    static public final String TAG = "IPST";
    /**
     * Used to get the result of LoginActivity
     */
    static private final int LOGIN_ACTIVITY_CODE = 0;
    static final int REQUEST_CODE_EMAIL = 1;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    /**
     * Preferences for saving app settings
     */
    private SharedPreferences preferences;

    /**
     * MainActivity constructor, initialize variables.
     */
    public MainActivity() {
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
     * @param title       Title for error dialog
     * @param messageText Message for error dialog
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
        for (Account account : manager.getAccounts()) {
            Log.d(TAG, "Has account " + account.name);
            Log.d(TAG, "account name " + email);
            Log.d(TAG, "account type " + account.type);
            if (account.name.equalsIgnoreCase(email) && account.type.equalsIgnoreCase("com.google"))
                return account;
        }
        Log.d(TAG, "returning null account");
        return null;
    }

    /**
     * Get saved user preferences.
     */
    private void getPreferences() {
        preferences = getPreferences(MODE_PRIVATE);
        Log.i(TAG, EMAIL_KEY + " -> " + preferences.getString(EMAIL_KEY, NULL_KEY));
        Log.i(TAG, MOST_RECENT_DATE_KEY + " -> " + preferences.getString(MOST_RECENT_DATE_KEY, NULL_KEY));
        Log.i(TAG, SORT_KEY + " -> " + preferences.getString(SORT_KEY, NULL_KEY));
    }

    /**
     * Search through accounts on the user's device now that we have permission to do so.
     */
    public void gotPermission_accounts() {
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
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.GET_ACCOUNTS}, REQUEST_CODE_EMAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    *  Method for building/showing the ui once emails are parsed.
    */
    public void buildUIAfterParsing(long acceptedcount, long pendingcount, long rejectedcount) {
        long totalCount = acceptedcount + pendingcount + rejectedcount;
        //Do stuff with these numbers.  Set bars with heights based on percentages. set button listeners here
        findViewById(R.id.progress_view_mainactivity).setVisibility(View.INVISIBLE);
        findViewById(R.id.mainui_mainactivity).setVisibility(View.VISIBLE);
        formatUI(acceptedcount, rejectedcount, pendingcount);
        RadioButton todayButton = (RadioButton) findViewById(R.id.todaytab_mainactivity);
        if (todayButton != null)
            todayButton.setOnCheckedChangeListener(this);
        RadioButton weekButton = (RadioButton) findViewById(R.id.weektab_mainactivity);
        if (weekButton != null)
            weekButton.setOnCheckedChangeListener(this);
        RadioButton monthButton = (RadioButton) findViewById(R.id.monthtab_mainactivity);
        if (monthButton != null)
            monthButton.setOnCheckedChangeListener(this);
        RadioButton allButton = (RadioButton) findViewById(R.id.alltab_mainactivity);
        if (allButton != null)
            allButton.setOnCheckedChangeListener(this);
    }

    /*
    * Method to format UI when changing radio buttons
     */
    public void formatUI(long accepted, long rejected, long pending)
    {
        ((TextView) findViewById(R.id.pendingtext_mainactivity)).setText(String.format(Locale.getDefault(), "%d", pending));
        ((TextView) findViewById(R.id.acceptedtext_mainactivity)).setText(String.format(Locale.getDefault(), "%d", accepted));
        ((TextView) findViewById(R.id.rejectedtext_mainactivity)).setText(String.format(Locale.getDefault(), "%d", rejected));

        setLayoutParamsGraphBars((int) ((pending * 100) / (accepted + rejected + pending)), (LinearLayout) findViewById(R.id.pendinggraph_mainactivity));
        setLayoutParamsGraphBars((int) ((rejected * 100) / (accepted + rejected + pending)), (LinearLayout) findViewById(R.id.rejectedgraph_mainactivity));
        setLayoutParamsGraphBars((int) ((accepted * 100) / (accepted + rejected + pending)), (LinearLayout) findViewById(R.id.acceptedgraph_mainactivity));
    }

    /*
    * Method to set layout params for graph bars
     */
    public void setLayoutParamsGraphBars(int height, LinearLayout layout)
    {
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());
        layout.setLayoutParams(params);
    }

    /*
    *  Method for when radiobuttons are clicked
    */
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        RadioButton tempButton = (RadioButton) view;
        tempButton.setTypeface(null, Typeface.BOLD);
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.todaytab_mainactivity:
                if (checked) {
                    //TODO set text of labels what the today things are.

                    Button buttonTextSet = (Button) findViewById(R.id.viewlist_mainactivity);
                    buttonTextSet.setText(R.string.viewlisttoday);
                }
                break;
            case R.id.weektab_mainactivity:
                if (checked) {

                    Button buttonTextSet = (Button) findViewById(R.id.viewlist_mainactivity);
                    buttonTextSet.setText(R.string.viewlistweek);
                }
                break;
            case R.id.monthtab_mainactivity:
                if (checked) {

                    Button buttonTextSet = (Button) findViewById(R.id.viewlist_mainactivity);
                    buttonTextSet.setText(R.string.viewlistmonth);
                }
                break;
            case R.id.alltab_mainactivity:
                if (checked) {


                    Button buttonTextSet = (Button) findViewById(R.id.viewlist_mainactivity);
                    buttonTextSet.setText(R.string.viewlistall);
                }
                break;
        }
    }

    /*
    *  This method fixes formatting when radio boxes are changed
    * */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        buttonView.setTypeface(isChecked ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        buttonView.setBackground(isChecked ? getResources().getDrawable(R.drawable.cell_shape_radio)
                                            : getResources().getDrawable(R.drawable.cell_shape_radio_clear));
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
        // TODO
        editor.putString(EMAIL_KEY, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
        editor.apply();
        Log.d(TAG, "Got account name " + data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
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
        if (!preferences.getString(EMAIL_KEY, NULL_KEY).equalsIgnoreCase(NULL_KEY)) {
            findViewById(R.id.progress_view_mainactivity).setVisibility(View.VISIBLE);
            findViewById(R.id.gmail_login_button).setVisibility(View.INVISIBLE);
            parseEmail();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mainactivity, menu);
        return true;
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
