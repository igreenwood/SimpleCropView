package com.example.simplecropviewsample;

import android.app.Application;
import android.graphics.Bitmap;

public class AppController extends Application{
    private static final String TAG = AppController.class.getSimpleName();
    public Bitmap cropped = null;

    @Override
    public void onCreate(){
        super.onCreate();
        // load custom font
        FontUtils.loadFont(getApplicationContext(), "Roboto-Light.ttf");
    }
}