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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Toast;

import com.einzig.ipst2.R;
import com.einzig.ipst2.database.DatabaseInterface;
import com.einzig.ipst2.util.PreferencesHelper;
import com.einzig.ipst2.util.SendMessageHelper;
import com.einzig.ipst2.util.SendPortalData;

import java.util.List;

/**
 * @author Steven Foskett
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    // TODO Remove all string literal preference strings

    /**
     *
     * @param context
     * @return
     */
    public static String getVersionNum(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public void clearedData() {
        setResult(Activity.RESULT_OK, getIntent());
        finish();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SettingsActivity.DBPreferenceFragment.class.getName().equals(fragmentName)
                || SettingsActivity.ListSettingsFragment.class.getName().equals(fragmentName)
                || SettingsActivity.MiscSettingsFragment.class.getName().equals(fragmentName)
                || SettingsActivity.AboutSettingsFragment.class.getName().equals(fragmentName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setLogo(R.mipmap.ic_launcher);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)

    public static class AboutSettingsFragment extends PreferenceFragment {

        public void goToURL(String urlString) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(urlString));
            startActivity(i);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_aboutsheet);
            setHasOptionsMenu(true);

            Preference versionnum = findPreference("version-num");
            if (versionnum != null)
                versionnum.setSummary(getVersionNum(getActivity()));

            Preference contactdev = findPreference("contact-dev");
            if (contactdev != null)
                contactdev.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SendMessageHelper.sendMessage(getActivity(), "Contact IPST2 Dev - In App",
                                "App Version - " + getVersionNum(getActivity()) + "\n",
                                "portalsubmissiontracker2@gmail.com",
                                "Contacting Dev...");
                        return false;
                    }
                });

            Preference uploadportals = findPreference("upload-portals");
            if (uploadportals != null)
                uploadportals.setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                SharedPreferences preferences = PreferenceManager
                                        .getDefaultSharedPreferences(getActivity());
                                long timeNow = System.currentTimeMillis();
                                long timeSent = preferences.getLong("last-portal-data", 0);
                                if (timeSent == 0 || (timeNow - timeSent) >= (1000 * 60 * 15)) {
                                    Toast.makeText(getActivity(), "Building portal data...", Toast
                                            .LENGTH_SHORT).show();
                                    preferences.edit()
                                            .putLong("last-portal-data", System.currentTimeMillis())
                                            .apply();
                                    new SendPortalData(getActivity()).execute();
                                } else {
                                    Toast.makeText(getActivity(), "Recently Sent Portal Data, " +
                                            "Please wait and try again.", Toast.LENGTH_LONG).show();
                                }
                                return false;
                            }
                        });
            Preference viewipststats = findPreference("view-ipststats");
            if (viewipststats != null)
                viewipststats.setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                goToURL("https://demaerschalck.eu/ingress/");
                                return false;
                            }
                        });
            Preference gototg = findPreference("goto-tg");
            if (gototg != null)
                gototg.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        goToURL("https://t.me/ipst2");
                        return false;
                    }
                });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DBPreferenceFragment extends PreferenceFragment {
        public void clearAllData() {
            DatabaseInterface db = new DatabaseInterface(getActivity());
            db.nukeAll();
            //db.deleteAll();
            PreferencesHelper helper = new PreferencesHelper(getActivity());
            helper.clearAll();
            helper.initPreferences();
            helper.printAllPreferences();
            ((SettingsActivity) getActivity()).clearedData();
        }

        public void confirmClearDialog() {
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(getActivity(), R.style.dialogtheme)
                                .setTitle(R.string.confirmcleardb_preftitle)
                                .setMessage(R.string.confirmcleardbpref_message)
                                .setPositiveButton(R.string.confirm,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                clearAllData();
                                            }
                                        })
                                .setNegativeButton(R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface,
                                                    int i) {
                                            }
                                        })
                                .setIcon(R.drawable.ic_warning)
                                .show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_dbsheet);
            setHasOptionsMenu(true);
            PreferencesHelper helper = new PreferencesHelper(getActivity());
            if (!helper.isInitialized(helper.dateFormatKey()))
                helper.set(helper.dateFormatKey(), helper.mdyFormat());
            if (!helper.isInitialized(helper.sortKey()))
                helper.set(helper.sortKey(), helper.responseDateSort());
            DBPreferenceFragment.this.findPreference(getResources()
                    .getString(R.string.hardResetKey))
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            confirmClearDialog();
                            return false;
                        }
                    });
            // TODO remove hardcoded pref string
            Preference exportDBPref = DBPreferenceFragment.this.findPreference("exportdb_pref");
            if (exportDBPref != null)
                exportDBPref.setOnPreferenceClickListener(new Preference
                        .OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //TODO add confirm export dialog and go to export db method
                        //TODO add export db method, show toast when start and finish
                        return false;
                    }
                });

            Preference importDBPref = DBPreferenceFragment.this.findPreference("importdb_pref");
            if (importDBPref != null)
                importDBPref.setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                //TODO add confirm import dialog and go to export db method
                                //TODO add import db method, show toast when start and finish
                                return false;
                            }
                        });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ListSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_listsheet);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MiscSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_miscsheet);
            setHasOptionsMenu(true);

            Preference darktheme = findPreference("dark-theme");
            if (darktheme != null)
                darktheme.setOnPreferenceClickListener(new Preference
                        .OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //TODO add theme stuff
                        return false;
                    }
                });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}


