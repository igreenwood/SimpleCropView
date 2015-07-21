package com.example.simplecropviewsample;

import android.app.Application;
import android.graphics.Bitmap;

public class AppController extends Application{
    private static final String TAG = AppController.class.getSimpleName();
    public Bitmap cropped = null;
}