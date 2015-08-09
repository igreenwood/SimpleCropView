package com.example.simplecropviewsample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ViewGroup;

import com.isseiaoki.simplecropview.CropImageView;


public class ResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        FontUtils.setFont((ViewGroup)findViewById(R.id.layout_root));
        Bitmap cropped = ((AppController)getApplication()).cropped;
        ((CropImageView)findViewById(R.id.result_image)).setImageBitmap(cropped);
    }
}
