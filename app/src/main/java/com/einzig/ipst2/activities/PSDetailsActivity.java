/*
 *****************************************************************************
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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.einzig.ipst2.R;
import com.einzig.ipst2.database.DatabaseHelper;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalResponded;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.DialogHelper;
import com.einzig.ipst2.util.Logger;
import com.einzig.ipst2.util.PreferencesHelper;
import com.einzig.ipst2.util.SendMessageHelper;
import com.einzig.ipst2.util.ThemeHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.einzig.ipst2.activities.MainActivity.PORTAL_KEY;
import static com.einzig.ipst2.activities.MainActivity.REQUEST_CODE_WRITE_EXTERNAL;

public class PSDetailsActivity extends AppCompatActivity {
    /* Butterknife UI code */
    @BindView(R.id.daysinqueue_psdetailsactivity)
    TextView daysInQueueLabel;
    @BindView(R.id.extralayout_psdetailsactivity)
    LinearLayout extraLayout;
    @BindView(R.id.name_psdetailsactivity)
    TextView namelabel;
    /**
     * Portal
     */
    PortalSubmission portal;
    @BindView(R.id.psimage_psdetailsactivity)
    ImageView portalImage;
    @BindView(R.id.psstatusimage_psdetailsactivity)
    ImageView portalStatusImage;
    @BindView(R.id.saveportalimage_psdetailsactivity)
    Button saveportalimage_psdetailsactivity;
    @BindView(R.id.submitted_psdetailsactivity)
    TextView submittedLabel;
    @BindView(R.id.toppanel_psdetailsactivity)
    LinearLayout toppanel_psdetailsactivity;
    @BindView(R.id.dayslayout_psdetailsactivity)
    LinearLayout dayslayout_psdetailsactivity;
    @BindView(R.id.psimageholder_psdetailsactivity)
    RelativeLayout psimageholder_psdetailsactivity;
    /**
     * Date Formatter for displaying dates on the UI
     */
    DateTimeFormatter uiFormatter;

    public final static int EDIT_ACTIVITY_CODE = 10101;
    DatabaseHelper db = new DatabaseHelper(this);
    /**
     *
     */
    public PSDetailsActivity() {
        uiFormatter = null;
    }

    /**
     * Add additional UI components for an accepted portal submission
     *
     * @param portal Portal being viewed cast to a PortalAccepted for convenience
     */
    private void buildAcceptedUI(PortalAccepted portal) {
        buildRespondedUI(portal);
        portalStatusImage.setBackgroundColor(ContextCompat.getColor(this, R.color.accepted));
        portalStatusImage.setImageDrawable(ContextCompat.getDrawable(this, R
                .drawable.ic_check));
        LinearLayout acceptedLayout = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.row_psdetails_accepted, extraLayout, false);
        ThemeHelper.styleView(acceptedLayout.findViewById(R.id.liveaddresslayout_acceptedrow),
                this);
        ThemeHelper.styleButton((Button) acceptedLayout.findViewById(R.id
                .viewonintelmapbutton_acceptedrow), this);

        ((TextView) acceptedLayout.findViewById(R.id.liveaddress_acceptedrow)).setText(
                portal.getLiveAddress());
        acceptedLayout.findViewById(R.id.viewonintelmapbutton_acceptedrow)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goToIntel();
                    }
                });
        extraLayout.addView(acceptedLayout);
    }

    /**
     * Add additional UI components for portal details
     */
    private void buildExtraUI() {
        if (portal instanceof PortalAccepted)
            buildAcceptedUI((PortalAccepted) portal);
        else if (portal instanceof PortalRejected)
            buildRejectedUI((PortalRejected) portal);
        else
            daysInQueueLabel.setText(String.valueOf(portal.getDaysSinceResponse()));
    }

    /**
     * Add additional UI components for a portal submission that is no longer pending
     *
     * @param portal Portal being viewed cast to a PortalResponded for convenience
     */
    private void buildRespondedUI(PortalResponded portal) {
        daysInQueueLabel.setText(String.valueOf(portal.getResponseTime()));
        LinearLayout respondedLayout = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.row_psdetails_responded, extraLayout, false);
        ThemeHelper.styleView(respondedLayout.findViewById(R.id.daterespondedlayout_respondedrow),
                this);
        ((TextView) respondedLayout.findViewById(R.id.dateresponded_respondedrow)).setText(
                uiFormatter.print(portal.getDateResponded()));
        extraLayout.addView(respondedLayout);
    }

    /**
     * Add additional UI components for a rejected portal submission
     *
     * @param portal Portal being viewed cast to a PortalRejected for convenience
     */
    private void buildRejectedUI(PortalRejected portal) {
        buildRespondedUI(portal);
        portalStatusImage.setBackgroundColor(ContextCompat.getColor(this, R.color.rejected));
        portalStatusImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable
                .ic_rejected));
        LinearLayout rejectedLayout = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.row_psdetails_rejected, extraLayout, false);
        ThemeHelper.styleView(rejectedLayout.findViewById(R.id.rejectionreasonlayout_rejectedrow),
                this);
        ((TextView) rejectedLayout.findViewById(R.id.rejectionreason_rejectedrow)).setText(
                portal.getRejectionReason());
        extraLayout.addView(rejectedLayout);
    }

    /**
     * Initialize default UI components
     */
    private void buildUI() {
        Logger.d("PSDetailsActivity", "Portal Type: " + portal.getClass().getName());
        ThemeHelper.styleView(toppanel_psdetailsactivity, this);
        ThemeHelper.styleView(dayslayout_psdetailsactivity, this);
        ThemeHelper.styleButton(saveportalimage_psdetailsactivity, this);
        namelabel.setText(portal.getName());
        submittedLabel.setText(uiFormatter.print(portal.getDateSubmitted()));
        if (portal.getPictureURL() != null && !portal.getPictureURL().equalsIgnoreCase(""))
            Picasso.with(this)
                    .load(portal.getPictureURL())
                    .error(R.drawable.ic_warning_white)
                    .into(portalImage);
        extraLayout.removeAllViews();
        buildExtraUI();
        setUpImageDownloadButton();
        setUpImageDetailsView();
    }

    /**
     * Create a File for saving an image
     */
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp =
                new SimpleDateFormat("ddMMyyyy_HHmm", Locale.getDefault()).format(
                        new LocalDate().toDate());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public void goToIntel() {
        try {
            Uri uri = Uri.parse(((PortalAccepted) portal).getIntelLinkURL());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (Exception e) {
            DialogHelper.showSimpleDialog(R.string.errortitle_viewintellink,
                    R.string.errormessage_viewintellink, this);
            e.printStackTrace();
        }
    }

    public void imageDownload(final Context context, final String url) {
        Target target = new Target() {
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                PSDetailsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Image Save Failed", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final File pictureFile = getOutputMediaFile();
                        if (pictureFile == null) {
                            Logger.d(getString(R.string.error_creating_media));
                            PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, R.string.image_save_failed,
                                            Toast.LENGTH_LONG)
                                            .show();
                                }
                            });
                        } else {
                            try {
                                FileOutputStream fos = new FileOutputStream(pictureFile);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                                fos.close();
                                PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context,
                                                getString(R.string.image_saved) +
                                                        pictureFile.getPath(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            } catch (FileNotFoundException e) {
                                PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context,
                                                getString(R.string.image_save_failed),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                Logger.d("File not found: " + e.getMessage());
                            } catch (IOException e) {
                                PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context,
                                                getString(R.string.image_save_failed),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                Logger.d("Error accessing file: " + e.getMessage());
                            }
                        }
                    }
                }).start();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        if (!url.equalsIgnoreCase(""))
            Picasso.with(context)
                    .load(url)
                    .into(target);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psdetails);
        ButterKnife.bind(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        ThemeHelper.initActionBar(getSupportActionBar());
        uiFormatter = new PreferencesHelper(getApplicationContext()).getUIFormatter();
        portal = getIntent().getExtras().getParcelable(PORTAL_KEY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d("onActivityResult(" + requestCode + ") -> " + resultCode);

        switch (requestCode) {
            case EDIT_ACTIVITY_CODE:
                onResultEdit(resultCode);
                break;
        }
    }

    private void onResultEdit(int resultCode) {
        if (resultCode == RESULT_OK) {
            setResult(Activity.RESULT_OK, getIntent());
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO Understand this
        if(portal != null)
            if(portal instanceof PortalAccepted)
                portal = db.getAcceptedPortal(portal.getPictureURL(), portal.getName());
            else if(portal instanceof PortalRejected)
                portal = db.getRejectedPortal(portal.getPictureURL(), portal.getName());
            else
                portal = db.getPendingPortal(portal.getPictureURL(), portal.getName());
        if (portal != null) {
            buildUI();
        } else
            finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.share_psdetailsactivity) {
            SendMessageHelper.sharePortal(portal, psimageholder_psdetailsactivity, this);
        } else if (id == R.id.edit_psdetailsactivity) {
            Intent intent = new Intent(this, PSEditActivity.class);
            intent.putExtra(PORTAL_KEY, (Parcelable) portal);
            startActivityForResult(intent, EDIT_ACTIVITY_CODE);
        } else if (id == R.id.delete_psdetailsactivity) {
            new android.app.AlertDialog.Builder(this, R.style.dialogtheme)
                    .setTitle(R.string.delete_dialog_title)
                    .setMessage(R.string.delete_dialog_message)
                    .setPositiveButton(R.string.emph_delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            deletePortal();
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
        return super.onOptionsItemSelected(item);
    }

    public void deletePortal() {
        DatabaseHelper db = new DatabaseHelper(this);
        if (portal != null)
            if (portal instanceof PortalAccepted)
                db.deleteAccepted((PortalAccepted) portal);
            else if (portal instanceof PortalRejected)
                db.deleteRejected((PortalRejected) portal);
            else
                db.deletePending(portal);
        onResultEdit(RESULT_OK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_psdetailsactivity, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imageDownload(PSDetailsActivity.this, portal.getPictureURL());
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void setUpImageDetailsView() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        portalImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(PSDetailsActivity.this);
                    RelativeLayout itemLayout =
                            (RelativeLayout) LayoutInflater.from(PSDetailsActivity.this)
                                    .inflate(R.layout.image_details, null);
                    builder.setView(itemLayout);
                    final AlertDialog d = builder.show();
                    Picasso.with(PSDetailsActivity.this)
                            .load(portal.getPictureURL())
                            .error(R.drawable.ic_warning_white)
                            .resize(displayMetrics.widthPixels, displayMetrics.heightPixels)
                            .centerInside()
                            .into((ImageView) itemLayout.findViewById(R.id.bigimage_imagedetails));
                    itemLayout.findViewById(R.id.close_imagedetails)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    d.dismiss();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setUpImageDownloadButton() {
        saveportalimage_psdetailsactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(PSDetailsActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    imageDownload(PSDetailsActivity.this, portal.getPictureURL());
                } else {
                    ActivityCompat.requestPermissions(PSDetailsActivity.this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_WRITE_EXTERNAL);
                }
            }
        });
    }
}
