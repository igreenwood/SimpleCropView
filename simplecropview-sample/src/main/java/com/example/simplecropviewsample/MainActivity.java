package com.example.simplecropviewsample;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.basic_sample_button).setOnClickListener(this);
    findViewById(R.id.rx_sample_button).setOnClickListener(this);

    // apply custom font
    FontUtils.setFont(findViewById(R.id.root_layout));

    initToolbar();
  }

  @Override public void onClick(View view) {
    switch (view.getId()) {
      case R.id.basic_sample_button:
        startActivity(BasicActivity.createIntent(this));
        break;
      case R.id.rx_sample_button:
        startActivity(RxActivity.createIntent(this));
        break;
    }
  }

  private void initToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    FontUtils.setTitle(actionBar, "SimpleCropView");
  }
}
