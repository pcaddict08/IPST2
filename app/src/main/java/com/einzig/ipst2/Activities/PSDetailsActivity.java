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

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.einzig.ipst2.R;
import com.einzig.ipst2.portal.PortalSubmission;
import com.squareup.picasso.Picasso;

public class PSDetailsActivity extends AppCompatActivity {

    PortalSubmission portalSubmission;

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
                                .into((ImageView)itemLayout.findViewById(R.id.bigimage_imagedetails));
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
}
