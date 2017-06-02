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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.einzig.ipst2.R;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
            Log.d(MainActivity.TAG, "PS Type: " + portalSubmission.getClass().getName());
            ((TextView) findViewById(R.id.name_psdetailsactivity)).setText(portalSubmission.getName());
            ((TextView) findViewById(R.id.submitted_psdetailsactivity)).setText(portalSubmission.getSubmittedDateString());
            ((TextView) findViewById(R.id.daysinqueue_psdetailsactivity)).setText(String.valueOf(portalSubmission.getDaysInQueue()));
            if (portalSubmission instanceof PortalRejected) {
                findViewById(R.id.psstatusimage_psdetailsactivity).setBackgroundColor(getResources().getColor(R.color.rejected));
                ((ImageView) findViewById(R.id.psstatusimage_psdetailsactivity)).setImageDrawable(getResources().getDrawable(R.drawable.ic_rejected));
            } else if (portalSubmission instanceof PortalAccepted) {
                findViewById(R.id.psstatusimage_psdetailsactivity).setBackgroundColor(getResources().getColor(R.color.accepted));
                ((ImageView) findViewById(R.id.psstatusimage_psdetailsactivity)).setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
            }
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
            Picasso.with(this)
                    .load(portalSubmission.getPictureURL())
                    .error(R.drawable.ic_warning_white)
                    .into(((ImageView) findViewById(R.id.psimage_psdetailsactivity)));
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

    public void imageDownload(final Context context, final String url) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, portalSubmission.getName(), "Saved from IPST2");
                            PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Image Saved", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (Exception e) {
                            PSDetailsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Image Save Failed", Toast.LENGTH_LONG).show();
                                }
                            });
                            e.printStackTrace();
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
}
