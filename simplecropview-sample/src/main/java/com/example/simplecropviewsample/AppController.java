package com.example.simplecropviewsample;

import android.app.Application;
import android.graphics.Bitmap;
@SuppressWarnings("unused")
public class AppController extends Application{
    private static final String TAG = AppController.class.getSimpleName();
    private static AppController instance;
    public Bitmap cropped = null;

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;
        // load custom font
        FontUtils.loadFont(getApplicationContext(), "Roboto-Light.ttf");
    }

    public static AppController getInstance(){
        return instance;
    }
}