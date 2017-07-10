/*
 ****************************************************************************
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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.einzig.ipst2.R;
import com.einzig.ipst2.database.DatabaseInterface;
import com.einzig.ipst2.parse.AuthenticatorTask;
import com.einzig.ipst2.parse.EmailParseTask;
import com.einzig.ipst2.parse.GetMailTask;
import com.einzig.ipst2.parse.MailBundle;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.DialogHelper;
import com.einzig.ipst2.util.Logger;
import com.einzig.ipst2.util.PreferencesHelper;
import com.einzig.ipst2.util.ThemeHelper;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.singh.daman.proprogressviews.DoubleArcProgress;

import org.joda.time.LocalDate;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Main activity class which launches the app.
 * Contains all startup and initialization code.
 *
 * @author Steven Foskett
 * @since 2017-05-15
 */
public class MainActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener {
    /** Preferences key for sending a portal through a bundle */
    static public final String PORTAL_KEY = "portal";
    /** Preferences key for sending a portal list through Bundle */
    static public final String PORTAL_LIST_KEY_RANGE = "portalList";
    static public final String PORTAL_LIST_KEY_TYPE = "portalListType";
    /** Activity-for-result code to request write-to-external-storage permissions */
    static final public int REQUEST_CODE_WRITE_EXTERNAL = 1 << 1;
    /** The key for saving version num */
    static final public String VERSION_KEY = "version";
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    /** Used to get the result of LoginActivity */
    static private final int LOGIN_ACTIVITY_CODE = 0;
    /** Seems to be redundant with PreferencesHelper#REFRESH_KEY */
    static final private String REFRESH_KEY = "refresh";
    /** Activity-for-result code to request email permissions */
    static final private int REQUEST_CODE_EMAIL = 1;
    /**  */
    static final private int REQUEST_CODE_ALL = REQUEST_CODE_EMAIL & REQUEST_CODE_WRITE_EXTERNAL;
    static private final int SETTINGS_ACTIVITY_CODE = 101;

    /* Butterknife UI code */
    @BindView(R.id.baseui_mainactivity)
    RelativeLayout baseui_mainactivity;
    @BindView(R.id.acceptedgraph_mainactivity)
    TextView acceptedgraph;
    @BindView(R.id.acceptedtext_mainactivity)
    TextView acceptedtext;
    @BindView(R.id.alltab_mainactivity)
    RadioButton alltab;
    @BindView(R.id.gmail_login_button)
    Button gmail_login_button;
    @BindView(R.id.mainui_mainactivity)
    LinearLayout mainui_mainactivity;
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
    @BindView(R.id.tabs_mainactivity)
    RadioGroup tabs_mainactivity;
    @BindView(R.id.todaytab_mainactivity)
    RadioButton todaytab;
    /*Butterknife Binds for Views*/
    @BindView(R.id.viewlist_mainactivity)
    Button viewButton;
    @BindView(R.id.weektab_mainactivity)
    RadioButton weektab;
    @BindView(R.id.arcprogress_mainactivity)
    DoubleArcProgress arcprogress_mainactivity;

    /** Database Handle for getting portals and such */
    private DatabaseInterface db;
    /**  */
    private LocalDate viewDate;

    /**
     * MainActivity constructor, initialize variables.
     */
    public MainActivity() {
        db = new DatabaseInterface(this);
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
        ThemeHelper.styleRadioButton(alltab, this, alltab.isSelected());
        ThemeHelper.styleRadioButton(monthtab, this, monthtab.isSelected());
        ThemeHelper.styleRadioButton(weektab, this, weektab.isSelected());
        ThemeHelper.styleRadioButton(todaytab, this, todaytab.isSelected());
        if(ThemeHelper.isDarkTheme(this)) {
            baseui_mainactivity.setBackgroundColor(ContextCompat.getColor(this, R.color
                    .colorLighterPrimary_dark));
            arcprogress_mainactivity.setBackgroundColor(Color.WHITE);
        }
        selectRadioItem();
    }

    /** TODO Save a user preference if they deny us WRITE_EXTERNAL_STORAGE so we don't ask for it
     * Request permissions if we don't have them
     * @return true if we have all permissions, otherwise false
     */
    private boolean checkPermissions() {
        boolean hasPermissions = hasAccountsPermission() && hasWritePermission();
        String[] permissions = {GET_ACCOUNTS, WRITE_EXTERNAL_STORAGE};
        if (!hasPermissions)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ALL);
        return hasPermissions;
    }

    /**
     * Display an error message dialog
     *
     * @param title       Title for error dialog
     * @param messageText Message for error dialog
     */
    private void errorFoundMessage(final int title, final int messageText) {
        Logger.d("Displaying error message");
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
        acceptedgraph.setText(
                String.format(Locale.getDefault(), "%.1f%%", ((accepted * 100) / (totalnum))));
        rejectedgraph.setText(
                String.format(Locale.getDefault(), "%.1f%%", ((rejected * 100) / (totalnum))));
        pendinggraph.setText(
                String.format(Locale.getDefault(), "%.1f%%", ((pending * 100) / (totalnum))));
    }

    public void formatUIFromRadio(int viewID) {
        Button viewList = (Button) findViewById(R.id.viewlist_mainactivity);
        switch (viewID) {
        case R.id.todaytab_mainactivity:
            viewDate = new LocalDate().minusDays(1);
            viewList.setText(R.string.viewlisttoday);
            break;
        case R.id.weektab_mainactivity:
            viewDate = new LocalDate().minusDays(7);
            viewList.setText(R.string.viewlistweek);
            break;
        case R.id.monthtab_mainactivity:
            viewDate = new LocalDate().minusMonths(1);
            viewList.setText(R.string.viewlistmonth);
            break;
        case R.id.alltab_mainactivity:
            viewDate = null;
            formatUI(db.getAcceptedCount(), db.getRejectedCount(), db.getPendingCount());
            viewList.setText(R.string.viewlistall);
            break;
        }
        Logger.d("viewDate -> " + viewDate);
        if (viewDate == null)
            formatUI(db.getAcceptedCount(),
                    db.getRejectedCount(),
                    db.getPendingCount());
        else
            formatUI(db.getAcceptedCountByResponseDate(viewDate),
                    db.getRejectedCountByResponseDate(viewDate),
                    db.getPendingCountByDate(viewDate));
    }

    /**
     * Get user account if the user has already logged in.
     *
     * @return Account user logged in on.
     */
    private Account getAccount() {
        PreferencesHelper helper = new PreferencesHelper(getApplicationContext());
        String email = helper.get(helper.emailKey());
        Logger.i("Getting account " + email);
        AccountManager manager = AccountManager.get(this);
        for (Account account : manager.getAccounts()) {
            if (account.name.equalsIgnoreCase(email) && account.type.equalsIgnoreCase("com.google"))
                return account;
        }
        Logger.d("returning null account");
        return null;
    }

    public boolean getAccountsPermission() {
        boolean hasPermission = hasAccountsPermission();
        if (!hasPermission)
            ActivityCompat.requestPermissions(this, new String[]{GET_ACCOUNTS}, REQUEST_CODE_EMAIL);
        return hasPermission;
    }

    /**
     * Search through accounts on the user's device now that we have permission to do so.
     */
    public void gotAccountsPermission() {
        AccountManager manager = AccountManager.get(MainActivity.this);
        int numGoogAcct = 0;
        Account[] accountList = manager.getAccounts();
        for (Account a : accountList) {
            if (a.type.equals("com.google")) {
                numGoogAcct++;
            }
        }

        if (numGoogAcct == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, ThemeHelper
                    .getDialogTheme(this));
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.progress_view_mainactivity).setVisibility(View.VISIBLE);
                    findViewById(R.id.gmail_login_button).setVisibility(View.INVISIBLE);
                }
            });
            Intent intent = AccountManager.newChooseAccountIntent(null, null,
                    new String[]{"com.google"}, false, null, null, null, null);
            startActivityForResult(intent, LOGIN_ACTIVITY_CODE);
        }
    }

    /**
     * @return true if we have permission to access user accounts, otherwise false
     */
    public boolean hasAccountsPermission() {
        return ContextCompat.checkSelfPermission(this, GET_ACCOUNTS) == PERMISSION_GRANTED;
    }

    /**
     * @return true if we have permission to write to external storage, otherwise false
     */
    public boolean hasWritePermission() {
        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) ==
                PERMISSION_GRANTED;
    }

    private void initActionBar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setLogo(R.mipmap.ic_launcher);
            ab.setDisplayUseLogoEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }
    }

    private void initLoginButton() {
        gmail_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getAccountsPermission())
                    gotAccountsPermission();
            }
        });
    }

    /*
     * Callback method if an activity is started for result via startActivityForResult().
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d("onActivityResult(" + requestCode + ") -> " + resultCode);

        switch (requestCode) {
        case LOGIN_ACTIVITY_CODE:
            onLoginResult(resultCode, data);
            break;
        case SETTINGS_ACTIVITY_CODE:
            PreferencesHelper helper = new PreferencesHelper(getApplicationContext());
            if (!helper.isInitialized(helper.emailKey()))
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
        Logger.d("Check Changed : " + buttonView.getText().toString() + " - " + isChecked);
        ThemeHelper.styleRadioButton(buttonView, this, isChecked);
    }

    /*
     * View list of accepted portals
     */
    @OnClick({R.id.acceptedbutton_mainactivity})
    public void onClickAccepted(View view) {
        openList(viewDate, "accepted");
    }

    /*
     * View list of pending portals
     */
    @OnClick({R.id.pendingbutton_mainactivity})
    public void onClickPending(View view) {
        openList(viewDate, "pending");
    }

    /*
     * View list of rejected portals
     */
    @OnClick({R.id.rejectedbutton_mainactivity})
    public void onClickRejected(View view) {
        openList(viewDate, "rejected");
    }

    /*
     * View list of all portals
     */
    @OnClick(R.id.viewlist_mainactivity)
    public void onClickViewList(View view) {
        Logger.d("View all button clicked");
        if (((Button) view).getText().toString().equals(getString(R.string.viewlistall))) {
            Logger.d("Going to All List");
            openList(null, "all");
        } else if (((Button) view).getText().toString().equals(getString(R.string.viewlistmonth))) {
            Logger.d("Going to Month List");
            openList(new LocalDate().minusDays(30), "all");
        } else if (((Button) view).getText().toString().equals(getString(R.string.viewlistweek))) {
            Logger.d("Going to Week List");
            openList(new LocalDate().minusDays(7), "all");
        } else if (((Button) view).getText().toString().equals(getString(R.string.viewlisttoday))) {
            Logger.d("Going to Today List");
            openList(new LocalDate().minusDays(1), "all");
        }
    }

    private void initUI() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
        Fabric.with(this, new Crashlytics());
        initActionBar();
        styleButtons();
    }

    /* TODO Refactor into smaller functions
     * Called on startup by Android after the app starts and resources are available.
     * Used for more advanced initializations.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        initUI();
        initLoginButton();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        boolean shouldRefresh = getIntent().getBooleanExtra(REFRESH_KEY, false);
        if (checkPermissions())
            onHavePermissions(shouldRefresh);
    }

    private void onHavePermissions(boolean shouldRefresh) {
        PreferencesHelper helper = new PreferencesHelper(getApplicationContext());
        if (!helper.isInitialized(helper.resetKey())) {
            db.deleteAll();
            helper.clearAll();
        }
        helper.initPreferences();

        helper.printAllPreferences();
        if (!helper.getManualRefresh() || shouldRefresh) {
            if (helper.isInitialized(helper.emailKey())) {
                parseEmail();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress_view_mainactivity.setVisibility(View.INVISIBLE);
                        gmail_login_button.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else {
            buildUIAfterParsing();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mainactivity, menu);
        return true;
    }

    private void onLoginResult(int resultCode, Intent data) {
        PreferencesHelper helper = new PreferencesHelper(getApplicationContext());
        if (resultCode != RESULT_OK) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.progress_view_mainactivity).setVisibility(View.INVISIBLE);
                    findViewById(R.id.gmail_login_button).setVisibility(View.VISIBLE);
                }
            });
            return;
        }
        helper.set(helper.emailKey(), data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
        Account me = getAccount();
        if (me != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.progress_view_mainactivity).setVisibility(View.INVISIBLE);
                    findViewById(R.id.gmail_login_button).setVisibility(View.VISIBLE);
                }
            });
            errorFoundMessage(R.string.accountnotfoundtitle, R.string.accountnotfoundmessage);
        }
        Logger.d("Got account name " + data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
        parseEmail();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

        case R.id.settings_mainactivity:
            startActivityForResult(new Intent(this, SettingsActivity.class),
                    SETTINGS_ACTIVITY_CODE);
            break;
        case R.id.refresh_mainactivity:
            finish();
            Intent nextIntent = getIntent();
            nextIntent.putExtra(REFRESH_KEY, true);
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
        formatUIFromRadio(view.getId());
    }

    /*
     * Provides the results of permission requests
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ALL) {
            onHavePermissions(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
     * Method to open listview once list has been created
     */
    public void openList(LocalDate dateForList, String listType) {
        Intent intent = new Intent(MainActivity.this, PSListActivity.class);
        String stringForLog = "";
        if(dateForList != null)
            stringForLog = dateForList.toString();
        Logger.i("MainActivity", "Starting list activity with " + listType + " type " +
                stringForLog + "");
        intent.putExtra(PORTAL_LIST_KEY_TYPE, listType);
        intent.putExtra(PORTAL_LIST_KEY_RANGE, stringForLog);
        startActivity(intent);
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
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Logger.d("IS MAIN THREAD?!");
        }
        final Account account = getAccount();
        if (account != null) {
            final ProgressDialog dialog = new ProgressDialog(this, ThemeHelper
                    .getDialogTheme(this));
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.setTitle(getString(R.string.searching_email));
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            new Thread() {
                public void run() {
                    parseEmailWork(account, dialog);
                }
            }.start();
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
    private void parseEmailWork(Account account, final ProgressDialog dialog) {
        try {
            Logger.d("Showing DIALOG");
            String token = new AuthenticatorTask(this, account).execute().get();
            final MailBundle bundle = new GetMailTask(this, account, token).execute().get();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        dialog.dismiss();
                        if (bundle != null)
                            new EmailParseTask(MainActivity.this, bundle).execute();
                        else {
                            gmail_login_button.setVisibility(View.VISIBLE);
                            progress_view_mainactivity.setVisibility(View.INVISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            Logger.e(e.toString());
        }
    }

    /* RESET ui to show gmail button*/
    public void resetUI() {
        gmail_login_button.setVisibility(View.VISIBLE);
        progress_view_mainactivity.setVisibility(View.INVISIBLE);
        mainui_mainactivity.setVisibility(View.INVISIBLE);
        tabs_mainactivity.setVisibility(View.INVISIBLE);
        viewButton.setVisibility(View.INVISIBLE);
    }

    /* Style buttons */
    public void styleButtons()
    {
        ThemeHelper.styleButton(gmail_login_button, this);
        ThemeHelper.styleButton(viewButton, this);
    }

    /**
     * Set radio item active and the rest not
     */
    public void selectRadioItem() {
        PreferencesHelper helper = new PreferencesHelper(getApplicationContext());
        String defaultTab = helper.get(helper.defaultTabKey());
        int position;
        if (defaultTab.equals(getString(R.string.month)))
            position = R.id.monthtab_mainactivity;
        else if (defaultTab.equals(getString(R.string.today)))
            position = R.id.todaytab_mainactivity;
        else if (defaultTab.equals(getString(R.string.week)))
            position = R.id.weektab_mainactivity;
        else
            position = R.id.alltab_mainactivity;

        formatUIFromRadio(position);
        Logger.d("MainActivity#selectRadioItem", "Default Tab: " + position);
        tabs_mainactivity.check(position);
    }

    /*
    * Method to set layout params for graph bars
     */
    public void setLayoutParamsGraphBars(int height, TextView layout) {
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height + 35,
                getResources().getDisplayMetrics());
        Logger.d("HEIGHT: " + params.height);
        layout.setLayoutParams(params);
    }
}
