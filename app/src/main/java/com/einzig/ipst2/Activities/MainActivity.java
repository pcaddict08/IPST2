package com.einzig.ipst2.Activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.einzig.ipst2.Objects.SingletonClass;
import com.einzig.ipst2.Utilities.Utilities;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sun.mail.imap.IMAPStore;
import com.einzig.ipst2.R;
import com.sun.mail.imap.Utility;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    public static final String EXTRA_ACCOUNTNAME = "extra_accountname";
    static final int REQUEST_CODE_EMAIL = 1;
    static final int REQUEST_CODE_CREDS = 2;
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    Bundle options;
    AccountManager am;

    private String token;
    private String mEmail;
    public Date mostRecentDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SingletonClass.getInstance().setSharedPref(PreferenceManager.getDefaultSharedPreferences(this));
        String password = SingletonClass.getInstance().getSharedPref().getString("password", "noPassword");
        String email = SingletonClass.getInstance().getSharedPref().getString("email", "noEmail");
        String IMAPServer = SingletonClass.getInstance().getSharedPref().getString("IMAPServer", "noServer");

        Button gmail_login_button = (Button) findViewById(R.id.gmail_login_button);
        if(gmail_login_button != null)
        {
            gmail_login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loginHitMethod();
                }
            });
        }
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

    public void gotPermission_creds(Account me)
    {
        am.getAuthToken(me, "oauth2:https://mail.google.com/", null, this, new OnTokenAcquired(), null);
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
                    System.out.println("Found Some Accounts");
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
            System.out.println("CAUGHT NOT FOUND");
        }
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result){
            try{
                Bundle bundle = result.getResult();
                token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                System.out.println("TOKEN - " + token);
                //new SendMailTask().execute();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                mEmail = data
                        .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                am = AccountManager.get(this);
                options = new Bundle();
                Account me = null;

                Account[] accounts = am.getAccounts();
                for(Account a: accounts){
                    if(a.name.equals(mEmail) && a.type.equals("com.google")){
                        me = a;
                    }
                }
                if (me != null){
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    try {
                        gotPermission_creds(me);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    /* SHOW LOADING THINGIES
                    ProgressBar mainBar = (ProgressBar) findViewById(R.id.mainProgressBar);
                    mainBar.setVisibility(View.VISIBLE);
                    findViewById(R.id.mainLoadingText).setVisibility(View.VISIBLE);
                    findViewById(R.id.totalLayouts).setVisibility(View.INVISIBLE);
                    */
                }
                else
                {
                    Utilities.print_debug("Account not found on device.");
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_EMAIL && grantResults.length > 0) {
            int grantResult = grantResults[0];
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                gotPermission_accounts();
            } else {

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
}
