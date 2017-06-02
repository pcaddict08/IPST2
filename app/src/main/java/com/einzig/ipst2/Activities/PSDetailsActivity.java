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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.UUID;

public class PSDetailsActivity extends AppCompatActivity {
    PortalSubmission portalSubmission;
    static final int WRITE_EXTERNAL_STORAGE = 2;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psdetails);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        portalSubmission = getIntent().getExtras().getParcelable("ps");
        if (portalSubmission != null) {
            buildUI();
        }
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

    public void buildUI()
    {
        Log.d(MainActivity.TAG, "PS Type: " + portalSubmission.getClass().getName());
        ((TextView) findViewById(R.id.name_psdetailsactivity)).setText(portalSubmission.getName());
        ((TextView) findViewById(R.id.submitted_psdetailsactivity)).setText(portalSubmission.getSubmittedDateString());
        if (portalSubmission instanceof PortalResponded) {
            ((TextView) findViewById(R.id.daysinqueue_psdetailsactivity)).setText(String.valueOf(((PortalResponded) portalSubmission).getResponseTime()));
            LinearLayout respondedLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.row_psdetails_responded, (LinearLayout) findViewById(R.id.extralayout_psdetailsactivity), false);
            ((TextView)respondedLayout.findViewById(R.id.dateresponded_respondedrow)).setText(((PortalResponded) portalSubmission).getDateRespondedString());
            ((LinearLayout) findViewById(R.id.extralayout_psdetailsactivity)).addView(respondedLayout);
            if (portalSubmission instanceof PortalRejected) {
                findViewById(R.id.psstatusimage_psdetailsactivity).setBackgroundColor(getResources().getColor(R.color.rejected));
                ((ImageView) findViewById(R.id.psstatusimage_psdetailsactivity)).setImageDrawable(getResources().getDrawable(R.drawable.ic_rejected));
            } else if (portalSubmission instanceof PortalAccepted) {
                findViewById(R.id.psstatusimage_psdetailsactivity).setBackgroundColor(getResources().getColor(R.color.accepted));
                ((ImageView) findViewById(R.id.psstatusimage_psdetailsactivity)).setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
            }
        }
        else
            ((TextView) findViewById(R.id.daysinqueue_psdetailsactivity)).setText(String.valueOf(portalSubmission.getDaysSinceResponse()));

        setUpImageDownloadButton();

        //Loading image into imageview
        Picasso.with(this)
                .load(portalSubmission.getPictureURL())
                .error(R.drawable.ic_warning_white)
                .into(((ImageView) findViewById(R.id.psimage_psdetailsactivity)));

        setUpImageDetailsView();
    }

    public void setUpImageDownloadButton()
    {
        findViewById(R.id.saveportalimage_psdetailsactivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(PSDetailsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    imageDownload(PSDetailsActivity.this, portalSubmission.getPictureURL());
                } else {
                    ActivityCompat.requestPermissions(PSDetailsActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
                }
            }
        });
    }

    public void setUpImageDetailsView()
    {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        findViewById(R.id.psimage_psdetailsactivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PSDetailsActivity.this);
                    RelativeLayout itemLayout = (RelativeLayout) LayoutInflater.from(PSDetailsActivity.this)
                            .inflate(R.layout.image_details, null);
                    builder.setView(itemLayout);
                    final AlertDialog d = builder.show();
                    Picasso.with(PSDetailsActivity.this)
                            .load(portalSubmission.getPictureURL())
                            .error(R.drawable.ic_warning_white)
                            .resize(displayMetrics.widthPixels, displayMetrics.heightPixels)
                            .centerInside()
                            .into((ImageView) itemLayout.findViewById(R.id.bigimage_imagedetails));
                    itemLayout.findViewById(R.id.close_imagedetails).setOnClickListener(new View.OnClickListener() {
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

    public void imageDownload(final Context context, final String url) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final File pictureFile = getOutputMediaFile();
                        if (pictureFile == null) {
                            Log.d(MainActivity.TAG, "Error creating media file, check storage permissions: ");
                            PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Image Save Failed", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                            fos.close();
                            PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Image Saved - " + pictureFile.getPath(), Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (FileNotFoundException e) {
                            PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Image Save Failed", Toast.LENGTH_LONG).show();
                                }
                            });
                            Log.d(MainActivity.TAG, "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Image Save Failed", Toast.LENGTH_LONG).show();
                                }
                            });
                            Log.d(MainActivity.TAG, "Error accessing file: " + e.getMessage());
                        }
                    }
                }).start();
            }

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
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        Picasso.with(context)
                .load(url)
                .into(target);
    }

    /** Create a File for saving an image */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm", Locale.getDefault()).format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }
}
