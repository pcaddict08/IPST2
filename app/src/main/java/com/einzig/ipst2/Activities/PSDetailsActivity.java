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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.einzig.ipst2.DialogHelper;
import com.einzig.ipst2.R;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalResponded;
import com.einzig.ipst2.portal.PortalSubmission;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.einzig.ipst2.activities.MainActivity.TAG;

public class PSDetailsActivity extends AppCompatActivity {
    static final int WRITE_EXTERNAL_STORAGE = 2;
    @BindView(R.id.daysinqueue_psdetailsactivity)
    TextView daysinqueueLabel;
    @BindView(R.id.extralayout_psdetailsactivity)
    LinearLayout extralayout;
    @BindView(R.id.name_psdetailsactivity)
    TextView namelabel;
    PortalSubmission portalSubmission;
    @BindView(R.id.psimage_psdetailsactivity)
    ImageView psimage;
    @BindView(R.id.psstatusimage_psdetailsactivity)
    ImageView psstatusimage;
    @BindView(R.id.saveportalimage_psdetailsactivity)
    Button saveportalimage;
    @BindView(R.id.submitted_psdetailsactivity)
    TextView submittedLabel;


    public void buildUI() {
        Log.d(TAG, "PS Type: " + portalSubmission.getClass().getName());
        namelabel.setText(portalSubmission.getName());
        submittedLabel.setText(portalSubmission.getSubmittedDateString());
        Picasso.with(this)
                .load(portalSubmission.getPictureURL())
                .error(R.drawable.ic_warning_white)
                .into(psimage);
        setUpExtraUI();
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
                new SimpleDateFormat("ddMMyyyy_HHmm", Locale.getDefault()).format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public void goToIntel() {
        try {
            Uri uri = Uri.parse(((PortalAccepted) portalSubmission).getIntelLinkURL());
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
                            Log.d(TAG, "Error creating media file, check storage permissions: ");
                            PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Image Save Failed", Toast.LENGTH_LONG)
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
                                                "Image Saved - " + pictureFile.getPath(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            } catch (FileNotFoundException e) {
                                PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Image Save Failed",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                Log.d(TAG, "File not found: " + e.getMessage());
                            } catch (IOException e) {
                                PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Image Save Failed",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                Log.d(TAG, "Error accessing file: " + e.getMessage());
                            }
                        }
                    }
                }).start();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        Picasso.with(context)
                .load(url)
                .into(target);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psdetails);
        ButterKnife.bind(this);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        portalSubmission = getIntent().getExtras().getParcelable("ps");
        if (portalSubmission != null) {
            buildUI();
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imageDownload(PSDetailsActivity.this, portalSubmission.getPictureURL());
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void setUpExtraUI() {
        if (portalSubmission instanceof PortalResponded) {
            daysinqueueLabel.setText(
                    String.valueOf(((PortalResponded) portalSubmission).getResponseTime()));
            LinearLayout respondedLayout = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.row_psdetails_responded, extralayout, false);
            ((TextView) respondedLayout.findViewById(R.id.dateresponded_respondedrow)).setText(
                    ((PortalResponded) portalSubmission).getDateRespondedString());
            extralayout.addView(respondedLayout);
            if (portalSubmission instanceof PortalRejected) {
                psstatusimage.setBackgroundColor(getResources().getColor(R.color.rejected));
                psstatusimage.setImageDrawable(getResources().getDrawable(R.drawable.ic_rejected));
                LinearLayout rejectedLayout = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.row_psdetails_rejected, extralayout, false);
                ((TextView) rejectedLayout.findViewById(R.id.rejectionreason_rejectedrow)).setText(
                        ((PortalRejected) portalSubmission).getRejectionReason());
                extralayout.addView(rejectedLayout);
            } else if (portalSubmission instanceof PortalAccepted) {
                psstatusimage.setBackgroundColor(getResources().getColor(R.color.accepted));
                psstatusimage.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                LinearLayout acceptedLayout = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.row_psdetails_accepted, extralayout, false);
                ((TextView) acceptedLayout.findViewById(R.id.liveaddress_acceptedrow)).setText(
                        ((PortalAccepted) portalSubmission).getLiveAddress());
                acceptedLayout.findViewById(R.id.viewonintelmapbutton_acceptedrow)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                goToIntel();
                            }
                        });
                extralayout.addView(acceptedLayout);
            }
        } else
            daysinqueueLabel.setText(String.valueOf(portalSubmission.getDaysSinceResponse()));
    }

    public void setUpImageDetailsView() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        psimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PSDetailsActivity.this);
                    RelativeLayout itemLayout =
                            (RelativeLayout) LayoutInflater.from(PSDetailsActivity.this)
                                    .inflate(R.layout.image_details, null);
                    builder.setView(itemLayout);
                    final AlertDialog d = builder.show();
                    Picasso.with(PSDetailsActivity.this)
                            .load(portalSubmission.getPictureURL())
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
        saveportalimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(PSDetailsActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    imageDownload(PSDetailsActivity.this, portalSubmission.getPictureURL());
                } else {
                    ActivityCompat.requestPermissions(PSDetailsActivity.this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE);
                }
            }
        });
    }
}
