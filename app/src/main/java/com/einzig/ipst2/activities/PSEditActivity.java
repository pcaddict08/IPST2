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

package com.einzig.ipst2.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.einzig.ipst2.R;
import com.einzig.ipst2.database.DatabaseInterface;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.ThemeHelper;

import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.einzig.ipst2.activities.MainActivity.PORTAL_KEY;

public class PSEditActivity extends AppCompatActivity {
    @BindView(R.id.namebg_pseditactivity)
    LinearLayout namebg_pseditactivity;
    @BindView(R.id.namelabel_pseditactivity)
    TextView namelabel_pseditactivity;
    @BindView(R.id.name_pseditactivity)
    EditText name_pseditactivity;
    @BindView(R.id.datesubbg_pseditactivity)
    LinearLayout datesubbg_pseditactivity;
    @BindView(R.id.datesubbutton_pseditactivity)
    Button datesubbutton_pseditactivity;
    @BindView(R.id.saveportalbutton_pseditactivity)
    Button saveportalbutton_pseditactivity;
    @BindView(R.id.saveportalbuttonbg_pseditactivity)
    LinearLayout saveportalbuttonbg_pseditactivity;
    PortalSubmission portal;
    PortalSubmission oldPortal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psedit);
        ButterKnife.bind(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(R.string.pseditactivity_title);
        }
        ThemeHelper.initActionBar(getSupportActionBar());
        portal = getIntent().getExtras().getParcelable(PORTAL_KEY);
        oldPortal = getIntent().getExtras().getParcelable(PORTAL_KEY);
        if (portal != null) {
            name_pseditactivity.setText(portal.getName());
            buildUI();
        } else {
            finish();
        }
    }

    @OnClick(R.id.saveportalbutton_pseditactivity)
    public void tappedSave(){
        new AlertDialog.Builder(this, R.style.dialogtheme)
                .setTitle(R.string.save_portal_dialog_title)
                .setMessage(R.string.save_portal_dialog_message)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updatePortal();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setIcon(R.drawable.ic_warning)
                .show();
    }

    public void updatePortal() {
        DatabaseInterface db = new DatabaseInterface(this);
        portal.setName(name_pseditactivity.getText().toString());
        if (portal != null) {
            if (portal instanceof PortalAccepted && oldPortal instanceof PortalAccepted)
                db.updateAccepted((PortalAccepted) portal, (PortalAccepted) oldPortal);
            else if (portal instanceof PortalRejected && oldPortal instanceof PortalRejected)
                db.updateRejected((PortalRejected) portal, (PortalRejected) oldPortal);
            else if (portal instanceof PortalAccepted) {
                //Portal is accepted, but wasn't before
            } else if (portal instanceof PortalRejected) {
                //Portal is rejected, but wasn't before
            } else {
                db.updatePending(portal, oldPortal);
            }
        }
        finish();
    }

    @OnClick(R.id.datesubbutton_pseditactivity)
    public void editSubDate() {
        final Calendar c = Calendar.getInstance(Locale.getDefault());
        if (portal != null && portal.getDateSubmitted() != null) {
            c.setTime(portal.getDateSubmitted().toDate());
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog DPDSub_dialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        Calendar c = Calendar.getInstance(Locale.getDefault());
                        c.set(year, month, day);
                        if (portal != null)
                            portal.setDateSubmitted(new LocalDate(c.getTime()));
                        updateSubDateButton();
                    }
                }, year,
                month,
                day);
        DPDSub_dialog.setTitle("Set Submission Date");
        DPDSub_dialog.show();
    }

    public void buildUI() {
        ThemeHelper.styleView(namebg_pseditactivity, this);
        updateSubDateButton();
        ThemeHelper.styleView(datesubbg_pseditactivity, this);
        ThemeHelper.styleView(saveportalbuttonbg_pseditactivity, this);
        ThemeHelper.styleButton(datesubbutton_pseditactivity, this);
        ThemeHelper.styleButton(saveportalbutton_pseditactivity, this);
    }

    public void updateSubDateButton() {
        if (portal != null && portal.getDateSubmitted() != null)
            datesubbutton_pseditactivity.setText(portal.getDateSubmitted().toString());
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
}
