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

package com.einzig.ipst2.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
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
import android.widget.RadioGroup;
import android.widget.TextView;

import com.einzig.ipst2.DialogHelper;
import com.einzig.ipst2.R;
import com.einzig.ipst2.database.DatabaseInterface;
import com.einzig.ipst2.parse.AuthenticatorTask;
import com.einzig.ipst2.parse.EmailParseTask;
import com.einzig.ipst2.parse.GetMailTask;
import com.einzig.ipst2.parse.MailBundle;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GoogleApiAvailability;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Main activity class which launches the app.
 * Contains all startup and initialization code.
 *
 * @author Steven Foskett
 * @since 2017-05-15
 */
public class MainActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener {
    /**
     * Preferences key used for saving and retrieving the user's email address
     */
    static public final String EMAIL_KEY = "email";
    /**
     * Preferences key for email folder containing portal emails
     */
    static final public String FOLDER_KEY = "mailFolder";
    /**
     * Preferences key for sending date through Bundle
     */
    static public final String MOST_RECENT_DATE_KEY = "recentDate";
    /**
     * Used for the default key when something is uninitialized
     */
    static public final String NULL_KEY = "uninitialized";
    /**
     * Preferences key for sending a portal through a bundle
     */
    static public final String PORTAL_KEY = "portal";
    /**
     * Preferences key for sending a portal list through Bundle
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
    static final int REQUEST_CODE_EMAIL = 1;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    /**
     * Used to get the result of LoginActivity
     */
    static private final int LOGIN_ACTIVITY_CODE = 0;

    @BindView(R.id.acceptedgraph_mainactivity)
    TextView acceptedgraph;
    @BindView(R.id.acceptedtext_mainactivity)
    TextView acceptedtext;
    @BindView(R.id.alltab_mainactivity)
    RadioButton alltab;
    @BindView(R.id.gmail_login_button)
    Button gmail_login_button;
    @BindView(R.id.monthtab_mainactivity)
    RadioButton monthtab;
    @BindView(R.id.pendinggraph_mainactivity)
    TextView pendinggraph;
    @BindView(R.id.pendingtext_mainactivity)
    TextView pendingtext;
    @BindView(R.id.progress_view_mainactivity)
    LinearLayout progress_view_mainactivity;
    @BindView(R.id.rejectedgraph_mainactivity)
    TextView rejectedgraph;
    @BindView(R.id.rejectedtext_mainactivity)
    TextView rejectedtext;
    @BindView(R.id.todaytab_mainactivity)
    RadioButton todaytab;
    /*Butterknife Binds for Views*/
    @BindView(R.id.viewlist_mainactivity)
    Button viewButton;
    @BindView(R.id.weektab_mainactivity)
    RadioButton weektab;
    /** Database Handle for getting portals and such */
    private DatabaseInterface db;
    /** Preferences for saving app settings */
    private SharedPreferences preferences;
    /**  */
    private Date viewDate;


    /**
     * MainActivity constructor, initialize variables.
     */
    public MainActivity() {
        db = new DatabaseInterface(this);
        preferences = null;
        viewDate = null;
    }

    /**
     * Build and showing the UI once emails are parsed.
     */
    public void buildUIAfterParsing() {
        DatabaseInterface db = new DatabaseInterface(this);
        long accepted = db.getAcceptedCount();
        long pending = db.getPendingCount();
        long rejected = db.getRejectedCount();
        progress_view_mainactivity.setVisibility(View.INVISIBLE);
        mainui_mainactivity.setVisibility(View.VISIBLE);
        tabs_mainactivity.setVisibility(View.VISIBLE);
        viewButton.setVisibility(View.VISIBLE);
        formatUI(accepted, rejected, pending);
        todaytab.setOnCheckedChangeListener(this);
        weektab.setOnCheckedChangeListener(this);
        monthtab.setOnCheckedChangeListener(this);
        alltab.setOnCheckedChangeListener(this);
    }

    /* RESET ui to show gmail button*/
    public void resetUI()
    {
        gmail_login_button.setVisibility(View.VISIBLE);
        progress_view_mainactivity.setVisibility(View.INVISIBLE);
        mainui_mainactivity.setVisibility(View.INVISIBLE);
        tabs_mainactivity.setVisibility(View.INVISIBLE);
        viewButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Display an error message dialog
     *
     * @param title       Title for error dialog
     * @param messageText Message for error dialog
     */
    private void errorFoundMessage(final int title, final int messageText) {
        Log.d(TAG, "Displaying error message");
        runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(MainActivity.this).setTitle(title)
                        .setMessage(messageText)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).show();
            }
        });
    }

    /**
     * Format UI when changing radio buttons
     */
    public void formatUI(long accepted, long rejected, long pending) {
        pendingtext.setText(String.format(Locale.getDefault(), "%d", pending));
        acceptedtext.setText(String.format(Locale.getDefault(), "%d", accepted));
        rejectedtext.setText(String.format(Locale.getDefault(), "%d", rejected));
        double totalnum = accepted + rejected + pending;
        if (totalnum == 0)
            totalnum += 1;
        setLayoutParamsGraphBars((int) ((pending * 100) / (totalnum)), pendinggraph);
        setLayoutParamsGraphBars((int) ((rejected * 100) / (totalnum)), rejectedgraph);
        setLayoutParamsGraphBars((int) ((accepted * 100) / (totalnum)), acceptedgraph);
        acceptedgraph.setText(String.format(Locale.getDefault(), "%.1f%%", ((accepted * 100) / (totalnum))));
        rejectedgraph.setText(String.format(Locale.getDefault(), "%.1f%%", ((rejected * 100) / (totalnum))));
        pendinggraph.setText(String.format(Locale.getDefault(), "%.1f%%", ((pending * 100) / (totalnum))));
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
        preferences = PreferenceManager.getDefaultSharedPreferences(this);//getPreferences(MODE_PRIVATE);
        Log.i(TAG, EMAIL_KEY + " -> " + preferences.getString(EMAIL_KEY, NULL_KEY));
        Log.i(TAG, MOST_RECENT_DATE_KEY + " -> " +
                preferences.getString(MOST_RECENT_DATE_KEY, NULL_KEY));
        Log.i(TAG, SORT_KEY + " -> " + preferences.getString(SORT_KEY, NULL_KEY));
    }

    /**
     * Search through accounts on the user's device now that we have permission to do so.
     */
    public void gotPermission_accounts() {
        AccountManager manager = AccountManager.get(MainActivity.this);
        int numGoogAcct = 0;
        Account[] accountList = manager.getAccounts();
        for (Account a : accountList) {
            if (a.type.equals("com.google")) {
                numGoogAcct++;
            }
        }
        if (numGoogAcct == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.noaccountstitle);
            builder.setMessage(R.string.noaccountsmessage);//.  Would you like to log in manually?")
            builder.setCancelable(true);
            builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else {
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
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.GET_ACCOUNTS}, REQUEST_CODE_EMAIL);
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
        Log.d(TAG, "onActivityResult(" + requestCode + ") -> " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case LOGIN_ACTIVITY_CODE:
                if(resultCode != RESULT_OK)
                    return;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(EMAIL_KEY, data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                editor.apply();
                Log.d(TAG, "Got account name " + data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                parseEmail();
                Account me = getAccount();
                if (me != null) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    findViewById(R.id.progress_view_mainactivity).setVisibility(View.VISIBLE);
                    findViewById(R.id.gmail_login_button).setVisibility(View.INVISIBLE);
                } else {
                    errorFoundMessage(R.string.accountnotfoundtitle, R.string.accountnotfoundmessage);
                }
                break;
            case SETTINGS_ACTIVITY_CODE:
                if(preferences != null && preferences.getString(EMAIL_KEY, NULL_KEY).equalsIgnoreCase(NULL_KEY))
                    resetUI();
                break;
        }
    }

    /**
     * This method fixes formatting when radio boxes are changed
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        buttonView.setTypeface(isChecked ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        buttonView.setTextColor(isChecked ? ActivityCompat.getColor(this, R.color.white)
                : ActivityCompat.getColor(this, R.color.colorPrimaryDark));
        buttonView.setBackground(isChecked ? ActivityCompat.getDrawable(this, R.drawable.cell_shape_radio)
                : ActivityCompat.getDrawable(this, R.drawable.cell_shape_radio_clear));
    }

    /*
     * View list of accepted portals
     */
    @OnClick({R.id.acceptedbutton_mainactivity})
    public void onClickAccepted(View view) {
        Log.d(TAG, "Status list button clicked");
        Vector<PortalAccepted> portals;
        if (viewDate == null)
            portals = db.getAllAccepted();
        else
            portals = db.getAcceptedByResponseDate(viewDate);
        openList(portals);
    }

    /*
     * View list of pending portals
     */
    @OnClick({R.id.pendingbutton_mainactivity})
    public void onClickPending(View view) {
        Vector<PortalSubmission> portals;
        if (viewDate == null)
            portals = db.getAllPending();
        else
            portals = db.getPendingByDate(viewDate);
        openList(portals);
    }

    /*
     * View list of rejected portals
     */
    @OnClick({R.id.rejectedbutton_mainactivity})
    public void onClickRejected(View view) {
        Vector<PortalRejected> portals;
        if (viewDate == null)
            portals = db.getAllRejected();
        else
            portals = db.getRejectedByResponseDate(viewDate);
        openList(portals);
    }

    /*
     * View list of all portals
     */
    @OnClick(R.id.viewlist_mainactivity)
    public void onClickViewList(View view) {
        Log.d(TAG, "View all button clicked");
        Vector<PortalSubmission> mainList = new Vector<>();
        if (((Button) view).getText().toString().equals(getString(R.string.viewlistall))) {
            Log.d(TAG, "Going to All List");
            mainList = db.getAllPortals();
        } else if (((Button) view).getText().toString().equals(getString(R.string.viewlistmonth))) {
            Log.d(TAG, "Going to Month List");
            mainList = db.getAllPortalsFromDate(new DateTime().minusDays(30).toDate());
        } else if (((Button) view).getText().toString().equals(getString(R.string.viewlistweek))) {
            Log.d(TAG, "Going to Week List");
            mainList = db.getAllPortalsFromDate(new DateTime().minusDays(7).toDate());
        } else if (((Button) view).getText().toString().equals(getString(R.string.viewlisttoday))) {
            Log.d(TAG, "Going to Today List");
            mainList = db.getAllPortalsFromDate(new DateTime().minusDays(1).toDate());
        }

        openList(mainList);
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
        ButterKnife.bind(this);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setLogo(R.mipmap.ic_launcher);
            ab.setDisplayUseLogoEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }
        getPreferences();
        gmail_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginHitMethod();
            }
        });
        Log.d(MainActivity.TAG, "PREF FOR EMAIL: " + preferences.getString(EMAIL_KEY, NULL_KEY));
        if (!preferences.getString(EMAIL_KEY, NULL_KEY).equalsIgnoreCase(NULL_KEY)) {
            progress_view_mainactivity.setVisibility(View.VISIBLE);
            gmail_login_button.setVisibility(View.INVISIBLE);
            parseEmail();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_ACTIVITY_CODE);
                break;
            case R.id.refresh_mainactivity:
                finish();
                startActivity(getIntent());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method for when radiobuttons are clicked
     *
     * @param view RadioButton
     */
    public void onRadioButtonClicked(View view) {
        if (!((RadioButton) view).isChecked())
            return;
        RadioButton tempButton = (RadioButton) view;
        tempButton.setTypeface(null, Typeface.BOLD);
        // Check which radio button was clicked
        Button viewList = (Button) findViewById(R.id.viewlist_mainactivity);
        switch (view.getId()) {
        case R.id.todaytab_mainactivity:
            viewDate = new DateTime().minusDays(1).toDate();
            viewList.setText(R.string.viewlisttoday);
            break;
        case R.id.weektab_mainactivity:
            viewDate = new DateTime().minusDays(7).toDate();
            viewList.setText(R.string.viewlistweek);
            break;
        case R.id.monthtab_mainactivity:
            viewDate = new DateTime().minusMonths(1).toDate();
            viewList.setText(R.string.viewlistmonth);
            break;
        case R.id.alltab_mainactivity:
            viewDate = null;
            formatUI(db.getAcceptedCount(), db.getRejectedCount(), db.getPendingCount());
            viewList.setText(R.string.viewlistall);
            break;
        }
        Log.d(TAG, "viewDate -> " + viewDate);
        if (viewDate == null)
            formatUI(db.getAcceptedCount(),
                    db.getRejectedCount(),
                    db.getPendingCount());
        else
            formatUI(db.getAcceptedCountByResponseDate(viewDate),
                    db.getRejectedCountByResponseDate(viewDate),
                    db.getPendingCountByDate(viewDate));
    }

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

    /*
     * Method to open listview once list has been created
     */
    public void openList(Vector<? extends PortalSubmission> list) {
        if (list.size() > 0) {
            Intent intent = new Intent(MainActivity.this, PSListActivity.class);
            intent.putExtra("psList", list);
            startActivity(intent);
        } else {
            DialogHelper.showSimpleDialog(R.string.noportalwarning, R.string.noportalmessage,
                    MainActivity.this);
        }
    }

    /**
     * Wrapper function for parseEmailWork.
     * <p>
     * Builds the progress dialog for, then calls parseEmailWork
     * </p>
     *
     * @see MainActivity#parseEmailWork(Account, ProgressDialog)
     */
    private void parseEmail() {
        Account account = getAccount();
        if (account != null) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.setTitle("Searching Email");
            dialog.setCanceledOnTouchOutside(false);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            parseEmailWork(account, dialog);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * Parses emails for portals
     * <p>
     * Runs 3 tasks to:
     * <ol>
     * <li>Get OAuth token</li>
     * <li>Search for relevant emails</li>
     * <li>Parse portals from the relevant emails</li>
     * </ol>
     * </p>
     *
     * @param account GMail account
     * @param dialog  Progress dialog to display while authenticating and searching for mail
     */
    private void parseEmailWork(Account account, ProgressDialog dialog) {
        try {
            dialog.show();
            String token = new AuthenticatorTask(this, account).execute().get();
            MailBundle bundle = new GetMailTask(this, account, token).execute().get();
            dialog.dismiss();
            if (bundle != null)
                new EmailParseTask(this, bundle).execute();
            else {
                gmail_login_button.setVisibility(View.VISIBLE);
                progress_view_mainactivity.setVisibility(View.INVISIBLE);
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.toString());
        }
    }

    /*
    * Method to set layout params for graph bars
     */
    public void setLayoutParamsGraphBars(int height, TextView layout) {
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height + 35,
                getResources().getDisplayMetrics());
        Log.d(TAG, "HEIGHT: " + params.height);
        layout.setLayoutParams(params);
    }
}
