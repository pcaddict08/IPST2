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

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.einzig.ipst2.R;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.ThemeHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        if (portal != null) {
            name_pseditactivity.setText(portal.getName());
            buildUI();
        } else {
            finish();
        }
    }

    public void buildUI() {
        ThemeHelper.styleView(namebg_pseditactivity, this);
        ThemeHelper.styleView(datesubbg_pseditactivity, this);
        ThemeHelper.styleView(saveportalbuttonbg_pseditactivity, this);
        ThemeHelper.styleButton(datesubbutton_pseditactivity, this);
        ThemeHelper.styleButton(saveportalbutton_pseditactivity, this);
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
