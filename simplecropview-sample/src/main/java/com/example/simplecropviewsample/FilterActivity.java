package com.example.simplecropviewsample;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Display;

public class FilterActivity extends AppCompatActivity {
    private static final String TAG = FilterActivity.class.getSimpleName();

    public static Intent createIntent(Activity activity, Uri uri) {
        Intent intent = new Intent(activity, FilterActivity.class);
        intent.setData(uri);
        return intent;
    }

    // Lifecycle Method ////////////////////////////////////////////////////////////////////////////

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Uri uri = getIntent().getData();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.filter_container, FilterFragment.newInstance(uri)).commit();
        }

        // apply custom font
        FontUtils.setFont(findViewById(R.id.filter_root_layout));

        initToolbar();
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.filter_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        FontUtils.setTitle(actionBar, "Filter Sample");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    public void startResultActivity(Uri uri) {
        if (isFinishing()) return;
        // Start ResultActivity
        startActivity(ResultActivity.createIntent(this, uri));
//      startActivity(FilterActivity.createIntent(this, uri));
    }

}