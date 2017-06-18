package com.example.simplecropviewsample;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();

  // Lifecycle Method ////////////////////////////////////////////////////////////////////////////

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.container, MainFragment.getInstance())
          .commit();
    }
  }

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  @Override public void onBackPressed() {
    super.onBackPressed();
    finish();
  }

  public void startResultActivity(Uri uri) {
    if (isFinishing()) return;
    // Start ResultActivity
    startActivity(ResultActivity.createIntent(this, uri));
  }
}
