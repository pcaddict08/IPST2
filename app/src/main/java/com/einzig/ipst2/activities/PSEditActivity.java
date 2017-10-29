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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.einzig.ipst2.R;
import com.einzig.ipst2.database.DatabaseHelper;
import com.einzig.ipst2.database.AcceptedPortalContract;
import com.einzig.ipst2.database.RejectedPortalContract;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalResponded;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.PreferencesHelper;
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
    @BindView(R.id.typespinner_pseditactivity)
    Spinner typespinner_pseditactivity;
    @BindView(R.id.typespinnerlayout_pseditactivity)
    LinearLayout typespinnerlayout_pseditactivity;
    @BindView(R.id.portalrespondedlayout_pseditactivity)
    LinearLayout portalrespondedlayout_pseditactivity;
    PortalSubmission portal;
    PortalSubmission oldPortal;
    DatabaseHelper db;
    PreferencesHelper helper;

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
        db = new DatabaseHelper(this);
        helper = new PreferencesHelper(this);
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
    public void tappedSave() {
        new AlertDialog.Builder(this, R.style.dialogtheme).setTitle(
                R.string.save_portal_dialog_title)
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

    /**
     * Add a portal to the database when updating portal entry
     *
     * @param portal Instance of PortalAccepted to add to the database
     */
    public void addPortalAccepted(PortalAccepted portal, PortalSubmission oldPortal) {
        PortalSubmission pending =
                db.getPendingPortal(oldPortal.getPictureURL(), oldPortal.getName());
        PortalRejected rejected =
                db.getRejectedPortal(oldPortal.getPictureURL(), oldPortal.getName());
        if (pending != null) {
            portal.setDateSubmitted(pending.getDateSubmitted());
            db.deletePending(pending);
        } else if (rejected != null) {
            db.deleteRejected(rejected);
        } else {
            portal.setDateSubmitted(portal.getDateResponded());
        }
        db.addPortalAccepted(portal);
    }

    /**
     * Add a portal to the database when updating portal entry
     *
     * @param portal Instance of PortalResponded to add to the database
     */
    public void addPortalPending(PortalSubmission portal, PortalResponded oldPortal) {
        if(oldPortal instanceof PortalAccepted)
            db.deleteAccepted((PortalAccepted)oldPortal);
        else if(oldPortal instanceof PortalRejected)
            db.deleteRejected((PortalRejected)oldPortal);
        db.addPortalSubmission(portal);
    }

    /**
     * Add a portal to the database when updating portal entry
     *
     * @param portal Instance of PortalRejected to add to the database
     */
    public void addPortalRejected(PortalRejected portal, PortalSubmission oldPortal) {
        PortalSubmission pending =
                db.getPendingPortal(oldPortal.getPictureURL(), oldPortal.getName());
        PortalAccepted accepted =
                db.getAcceptedPortal(oldPortal.getPictureURL(), oldPortal.getName());
        if (pending != null) {
            portal.setDateSubmitted(pending.getDateSubmitted());
            db.deletePending(pending);
        } else if (accepted != null) {
            db.deleteAccepted(accepted);
        } else {
            portal.setDateSubmitted(portal.getDateResponded());
        }
        db.addPortalRejected(portal);
    }

    public void updatePortal() {
        portal.setName(name_pseditactivity.getText().toString());
        if (portal != null) {
            if (portal instanceof PortalAccepted && oldPortal instanceof PortalAccepted)
                db.updateAccepted((PortalAccepted) portal, (PortalAccepted) oldPortal);
            else if (portal instanceof PortalRejected && oldPortal instanceof PortalRejected)
                db.updateRejected((PortalRejected) portal, (PortalRejected) oldPortal);
            else if (portal instanceof PortalAccepted && !(oldPortal instanceof PortalAccepted)) {
                addPortalAccepted((PortalAccepted) portal, oldPortal);
            } else if (portal instanceof PortalRejected && !(oldPortal instanceof PortalRejected)) {
                addPortalRejected((PortalRejected) portal, oldPortal);
            } else if ((!(portal instanceof PortalRejected) && !(oldPortal instanceof PortalRejected))
                    && (!(portal instanceof PortalAccepted) && !(oldPortal instanceof PortalAccepted))) {
                db.updatePending(portal, oldPortal);
            } else {
                if (oldPortal instanceof PortalResponded)
                    addPortalPending(portal, (PortalResponded) oldPortal);
            }
        }
        setResult(Activity.RESULT_OK, getIntent());
        Toast.makeText(this, "Portal Edit Complete", Toast.LENGTH_SHORT).show();
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
        DatePickerDialog DPDSub_dialog =
                new DatePickerDialog(this, ThemeHelper.getDateDialogTheme(PSEditActivity.this),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month,
                                                  int day) {
                                Calendar c = Calendar.getInstance(Locale.getDefault());
                                c.set(year, month, day);
                                if (portal != null)
                                    portal.setDateSubmitted(new LocalDate(c.getTime()));
                                updateDateButton(datesubbutton_pseditactivity,
                                        portal.getDateSubmitted().toString());
                            }
                        }, year, month, day);
        DPDSub_dialog.setTitle("Set Submission Date");
        DPDSub_dialog.show();
    }

    public void buildUI() {
        ThemeHelper.styleView(namebg_pseditactivity, this);
        updateDateButton(datesubbutton_pseditactivity, portal.getDateSubmitted().toString());
        ThemeHelper.styleView(datesubbg_pseditactivity, this);
        ThemeHelper.styleView(saveportalbuttonbg_pseditactivity, this);
        ThemeHelper.styleView(typespinnerlayout_pseditactivity, this);
        ThemeHelper.styleButton(datesubbutton_pseditactivity, this);
        ThemeHelper.styleButton(saveportalbutton_pseditactivity, this);

        String[] portalTypes = new String[]{"Pending", "Accepted", "Rejected"};
        typespinner_pseditactivity.setAdapter(ThemeHelper.styleSpinner(portalTypes, this));
        typespinner_pseditactivity.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i,
                                               long l) {
                        portalrespondedlayout_pseditactivity.removeAllViews();
                        switch (i) {
                            case 0:
                                portal = new PortalSubmission(portal.getName(),
                                        portal.getDateSubmitted(), portal.getPictureURL());
                                break;
                            case 1:
                                if (!(portal instanceof PortalAccepted)) {
                                    portal = new PortalAccepted(portal.getName(), portal.getDateSubmitted(),
                                            portal.getPictureURL(), new LocalDate(), "N/A", "N/A");
                                }
                                buildAcceptedUI();
                                break;
                            case 2:
                                if (!(portal instanceof PortalRejected)) {
                                    portal = new PortalRejected(portal.getName(), portal.getDateSubmitted(),
                                            portal.getPictureURL(), new LocalDate(), "N/A");
                                }
                                buildRejectedUI();
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
        if (portal instanceof PortalAccepted)
            typespinner_pseditactivity.setSelection(1);
        else if (portal instanceof PortalRejected)
            typespinner_pseditactivity.setSelection(2);
    }

    public void buildAcceptedUI() {
        LinearLayout acceptedLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.row_psedit_accepted, portalrespondedlayout_pseditactivity, false);
        ThemeHelper.styleView(acceptedLayout.findViewById(R.id.liveaddresslayout_pseditactivity), this);
        ThemeHelper.styleView(acceptedLayout.findViewById(R.id.intellinklayout_pseditactivity), this);
        ThemeHelper.styleView(acceptedLayout.findViewById(R.id.dateacceptedlayout_pseditactivity), this);
        ThemeHelper.styleButton((Button) acceptedLayout.findViewById(R.id.dateaccbutton_pseditactivity), this);
        if (portal instanceof PortalAccepted) {
            ((Button) acceptedLayout.findViewById(R.id.dateaccbutton_pseditactivity)).setText(((PortalAccepted) portal).getDateResponded().toString());
            ((EditText) acceptedLayout.findViewById(R.id.liveaddress_pseditactivity)).setText(((PortalAccepted) portal).getLiveAddress());
            ((EditText) acceptedLayout.findViewById(R.id.intellink_pseditactivity)).setText(((PortalAccepted) portal).getIntelLinkURL());
        }
        updateDateButton((Button) acceptedLayout.findViewById(R.id.dateaccbutton_pseditactivity), ((PortalResponded) portal).getDateResponded().toString());
        acceptedLayout.findViewById(R.id.dateaccbutton_pseditactivity)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        doPortalRespondedButton(view);
                    }
                });
        portalrespondedlayout_pseditactivity.addView(acceptedLayout);
    }

    public void doPortalRespondedButton(final View view) {
        final Calendar c = Calendar.getInstance(Locale.getDefault());
        if (portal != null && portal instanceof PortalResponded &&
                ((PortalResponded) portal).getDateResponded() != null) {
            c.setTime(((PortalResponded) portal).getDateResponded().toDate());
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog _dialog = new DatePickerDialog(PSEditActivity.this, ThemeHelper.getDateDialogTheme(PSEditActivity.this),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        Calendar c = Calendar.getInstance(Locale.getDefault());
                        c.set(year, month, day);
                        if (portal != null && portal instanceof PortalResponded)
                            ((PortalResponded) portal).setDateResponded(new LocalDate(c.getTime()));
                        updateDateButton((Button) view, ((PortalResponded) portal).getDateResponded().toString());
                    }
                }, year, month, day);
        _dialog.setTitle("Set Responded Date");
        _dialog.show();
    }

    public void buildRejectedUI() {
        LinearLayout rejectedLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.row_psedit_rejected, portalrespondedlayout_pseditactivity, false);
        ThemeHelper.styleView(rejectedLayout.findViewById(R.id.daterejectedlayout_pseditactivity), this);
        ThemeHelper.styleView(rejectedLayout.findViewById(R.id.rejectionreasonlayout_pseditactivity), this);
        ThemeHelper.styleButton((Button) rejectedLayout.findViewById(R.id.daterejbutton_pseditactivity), this);
        if (portal instanceof PortalRejected) {
            ((Button) rejectedLayout.findViewById(R.id.daterejbutton_pseditactivity)).setText(((PortalRejected) portal).getDateResponded().toString());
            ((EditText) rejectedLayout.findViewById(R.id.rejectionreason_pseditactivity)).setText(((PortalRejected) portal).getRejectionReason());
        }
        updateDateButton((Button) rejectedLayout.findViewById(R.id.daterejbutton_pseditactivity), ((PortalResponded) portal).getDateResponded().toString());
        rejectedLayout.findViewById(R.id.daterejbutton_pseditactivity)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        doPortalRespondedButton(view);
                    }
                });
        portalrespondedlayout_pseditactivity.addView(rejectedLayout);
    }

    public void updateDateButton(Button button, String buttonText) {
        if (button != null && buttonText != null)
            button.setText(buttonText);
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
