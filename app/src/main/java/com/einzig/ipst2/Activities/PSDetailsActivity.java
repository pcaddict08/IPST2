package com.einzig.ipst2.Activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.einzig.ipst2.R;

public class PSDetailsActivity extends AppCompatActivity {
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
        if(supportActionBar!=null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);
    }
}
