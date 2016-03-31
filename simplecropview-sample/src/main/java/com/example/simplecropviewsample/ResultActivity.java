package com.example.simplecropviewsample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.isseiaoki.simplecropview.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ResultActivity extends FragmentActivity {
    private ImageView mImageView;
    private ExecutorService mExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // apply custom font
        FontUtils.setFont((ViewGroup) findViewById(R.id.layout_root));
        mImageView = (ImageView)findViewById(R.id.result_image);
        mExecutor = Executors.newSingleThreadExecutor();

        // get cropped bitmap from Application
        final Bitmap cropped = AppController.getInstance().cropped;

        if(isLargeImage(cropped)){
            mExecutor.submit(new LoadScaledImageTask(cropped, mImageView, calcImageSize()));
        }else{
            mImageView.setImageBitmap(cropped);
        }
    }

    @Override
    protected void onDestroy() {
        mImageView.setImageBitmap(null);
        mExecutor.shutdown();
        super.onDestroy();
    }

    public int calcImageSize(){
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        return Math.min(Math.max(metrics.widthPixels, metrics.heightPixels), 2048);
    }

    public boolean isLargeImage(Bitmap bm){
        return bm.getWidth() > 2048 || bm.getHeight() > 2048;
    }

    public static class LoadScaledImageTask implements Runnable{
        private Handler mHandler = new Handler(Looper.getMainLooper());
        Bitmap bitmap;
        ImageView imageView;
        int width;

        public LoadScaledImageTask(Bitmap bitmap, ImageView imageView, int width){
            this.bitmap = bitmap;
            this.imageView = imageView;
            this.width = width;
        }

        @Override
        public void run() {
            final Bitmap scaled = Utils.getScaledBitmapForWidth(bitmap, width);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(scaled);
                }
            });
        }
    }
}
