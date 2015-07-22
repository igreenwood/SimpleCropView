package com.example.simplecropviewsample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;

import com.isseiaoki.simplecropview.CropImageView;


public class MainActivity extends Activity {

    private CropImageView mCropView;
    private RelativeLayout mRootLayout;
    private HorizontalScrollView mTabLayout;
    private boolean isPortrait = true;
    private boolean applySkin = true;
    private int dark, light, white, transparent = 0x66FFFFFF;
    private int mColor1, mColor2, mColor3, mColor4;

    private static final String KEY_COLOR1="color1", KEY_COLOR2="color2", KEY_COLOR3="color3", KEY_COLOR4="color4", KEY_SKIN_FLAG="skin_flg", KEY_IMG_FLAG = "img_flg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCropView = (CropImageView) findViewById(R.id.cropImageView);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.portrait);
        mCropView.setImageBitmap(bm);

        findViewById(R.id.buttonDone).setOnClickListener(btnListener);
        findViewById(R.id.buttonFitImage).setOnClickListener(btnListener);
        findViewById(R.id.button1_1).setOnClickListener(btnListener);
        findViewById(R.id.button3_4).setOnClickListener(btnListener);
        findViewById(R.id.button4_3).setOnClickListener(btnListener);
        findViewById(R.id.button9_16).setOnClickListener(btnListener);
        findViewById(R.id.button16_9).setOnClickListener(btnListener);
        findViewById(R.id.buttonFree).setOnClickListener(btnListener);
        findViewById(R.id.buttonChangeImage).setOnClickListener(btnListener);
        findViewById(R.id.buttonChangeSkin).setOnClickListener(btnListener);

        mRootLayout = (RelativeLayout) findViewById(R.id.layout_root);
        mTabLayout = (HorizontalScrollView) findViewById(R.id.tab_bar);
        dark = getResources().getColor(R.color.background_material_dark);
        light = getResources().getColor(R.color.background_floating_material_dark);
        white = getResources().getColor(android.R.color.white);
        mColor1 = dark;
        mColor2 = light;
        mColor3 = white;
        mColor4 = transparent;
        setColorSkin();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_COLOR1, mColor1);
        outState.putInt(KEY_COLOR2, mColor2);
        outState.putInt(KEY_COLOR3, mColor3);
        outState.putInt(KEY_COLOR4, mColor4);
        outState.putBoolean(KEY_SKIN_FLAG, applySkin);
        outState.putBoolean(KEY_IMG_FLAG, isPortrait);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        mColor1 = savedInstanceState.getInt(KEY_COLOR1);
        mColor2 = savedInstanceState.getInt(KEY_COLOR2);
        mColor3 = savedInstanceState.getInt(KEY_COLOR3);
        mColor4 = savedInstanceState.getInt(KEY_COLOR4);
        applySkin = savedInstanceState.getBoolean(KEY_SKIN_FLAG);
        isPortrait = savedInstanceState.getBoolean(KEY_IMG_FLAG);
        setColorSkin();
    }

    private final View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.buttonDone:
                    ((AppController) getApplication()).cropped = mCropView.getCroppedBitmap();
                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                    startActivity(intent);
                    break;
                case R.id.buttonFitImage:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_FIT_IMAGE);
                    break;
                case R.id.button1_1:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_1_1);
                    break;
                case R.id.button3_4:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_3_4);
                    break;
                case R.id.button4_3:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_4_3);
                    break;
                case R.id.button9_16:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_9_16);
                    break;
                case R.id.button16_9:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_16_9);
                    break;
                case R.id.buttonFree:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_FREE);
                    break;
                case R.id.buttonChangeImage:
                    if (isPortrait) {
                        mCropView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.landscape));
                        isPortrait = false;
                    } else {
                        mCropView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.portrait));
                        isPortrait = true;
                    }
                    setDefaultSkin();
                    applySkin = true;
                    break;
                case R.id.buttonChangeSkin:
                    changeSkin();
                    break;
            }
        }
    };

    public void changeSkin() {
        if (applySkin) {
            applySkin = false;
            setVividSkin();
        } else {
            applySkin = true;
            setDefaultSkin();
        }
    }

    public void setDefaultSkin() {
        mColor1 = dark;
        mColor2 = light;
        mColor3 = white;
        mColor4 = transparent;
        setColorSkin();
    }

    public void setVividSkin() {
        Palette palette = Palette.generate(mCropView.getImageBitmap(), 16);
        if (palette != null) {
            mColor1 = palette.getDarkMutedColor(dark);
            mColor2 = palette.getMutedColor(light);
            mColor3 = palette.getVibrantColor(white);
            mColor4 = setAlpha(palette.getVibrantColor(transparent), 0x66);
            setColorSkin();
        }
    }

    public void setColorSkin(){
        mRootLayout.setBackgroundColor(mColor1);
        mTabLayout.setBackgroundColor(mColor2);
        mCropView.setFrameColor(mColor3);
        mCropView.setBackgroundColor(mColor1);
        mCropView.setOverlayColor(mColor4);
    }

    public int setAlpha(int color, int alpha){
        int r = (color >> 16)&0xFF;
        int g = (color >> 8)&0xFF;
        int b = (color)&0xFF;
        return (alpha << 24)|(r << 16)|(g << 8)|(b);
    }
}
