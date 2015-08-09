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

    // Views ///////////////////////////////////////////////////////////////////////////////////////
    private CropImageView mCropView;
    private RelativeLayout mRootLayout;
    private HorizontalScrollView mTabLayout;

    // Image file index(1 ~ 5)
    private int mImageIndex = 1;

    // Variables for color setting /////////////////////////////////////////////////////////////////
    private boolean mApplySkin = true;
    private final int DEFAULT_COLOR_BG = 0xffffffff, DEFAULT_COLOR_TAB = 0xffadb5b7, DEFAULT_COLOR_FRAME = 0xfffff300, DEFAULT_COLOR_OVERLAY = 0xbbffffff;
    private int mBackgroundColor, mTabColor, mFrameColor, mOverlayColor;

    // Bundle key for Save/Restore state ///////////////////////////////////////////////////////////
    private static final String KEY_COLOR1="color1", KEY_COLOR2="color2", KEY_COLOR3="color3", KEY_COLOR4="color4", KEY_SKIN_FLAG="skin_flg", KEY_IMG_INDEX = "img_index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        FontUtils.setFont(mRootLayout);
        mCropView.setImageBitmap(getImageForIndex(mImageIndex));

        initColor();
    }

    // Save/Restore State //////////////////////////////////////////////////////////////////////////

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_COLOR1, mBackgroundColor);
        outState.putInt(KEY_COLOR2, mTabColor);
        outState.putInt(KEY_COLOR3, mFrameColor);
        outState.putInt(KEY_COLOR4, mOverlayColor);
        outState.putBoolean(KEY_SKIN_FLAG, mApplySkin);
        outState.putInt(KEY_IMG_INDEX, mImageIndex);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        mBackgroundColor = savedInstanceState.getInt(KEY_COLOR1);
        mTabColor = savedInstanceState.getInt(KEY_COLOR2);
        mFrameColor = savedInstanceState.getInt(KEY_COLOR3);
        mOverlayColor = savedInstanceState.getInt(KEY_COLOR4);
        mApplySkin = savedInstanceState.getBoolean(KEY_SKIN_FLAG);
        mImageIndex = savedInstanceState.getInt(KEY_IMG_INDEX);
        setColorSkin();
    }

    // Handle button event /////////////////////////////////////////////////////////////////////////

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
                    incrementImageIndex();
                    mCropView.setImageBitmap(getImageForIndex(mImageIndex));
                    setDefaultSkin();
                    mApplySkin = true;
                    break;
                case R.id.buttonChangeSkin:
                    changeSkin();
                    break;
            }
        }
    };

    // Bind views //////////////////////////////////////////////////////////////////////////////////

    private void findViews() {
        mCropView = (CropImageView) findViewById(R.id.cropImageView);
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
    }

    // Switch image files //////////////////////////////////////////////////////////////////////////

    private void incrementImageIndex() {
        mImageIndex++;
        if(mImageIndex >5) mImageIndex -= 5;
    }

    public Bitmap getImageForIndex(int index){
        String fileName = "sample"+ index;
        int resId = getResources().getIdentifier(fileName, "mipmap", getPackageName());
        return BitmapFactory.decodeResource(getResources(), resId);
    }

    // Handle UI colors ////////////////////////////////////////////////////////////////////////////

    private void initColor() {
        mBackgroundColor = DEFAULT_COLOR_BG;
        mTabColor = DEFAULT_COLOR_TAB;
        mFrameColor = DEFAULT_COLOR_FRAME;
        mOverlayColor = DEFAULT_COLOR_OVERLAY;
        setColorSkin();
    }

    public void changeSkin() {
        if (mApplySkin) {
            mApplySkin = false;
            setVividSkin();
        } else {
            mApplySkin = true;
            setDefaultSkin();
        }
    }

    public void setDefaultSkin() {
        mBackgroundColor = DEFAULT_COLOR_BG;
        mTabColor = DEFAULT_COLOR_TAB;
        mFrameColor = DEFAULT_COLOR_FRAME;
        mOverlayColor = DEFAULT_COLOR_OVERLAY;
        setColorSkin();
    }

    public void setVividSkin() {
        Palette palette = Palette.generate(mCropView.getImageBitmap(), 16);
        if (palette != null) {
            mBackgroundColor = palette.getDarkMutedColor(DEFAULT_COLOR_BG);
            mTabColor = palette.getMutedColor(DEFAULT_COLOR_TAB);
            mFrameColor = palette.getVibrantColor(DEFAULT_COLOR_FRAME);
            mOverlayColor = setAlpha(palette.getDarkMutedColor(DEFAULT_COLOR_OVERLAY), 0x66);
            setColorSkin();
        }
    }

    public void setColorSkin(){
        mRootLayout.setBackgroundColor(mBackgroundColor);
        mTabLayout.setBackgroundColor(mTabColor);
        mCropView.setBackgroundColor(mBackgroundColor);
        mCropView.setFrameColor(mFrameColor);
        mCropView.setHandleColor(mFrameColor);
        mCropView.setGuideColor(setAlpha(mFrameColor, 0x66));
        mCropView.setOverlayColor(mOverlayColor);
    }

    public int setAlpha(int color, int alpha){
        int r = (color >> 16)&0xFF;
        int g = (color >> 8)&0xFF;
        int b = (color)&0xFF;
        return (alpha << 24)|(r << 16)|(g << 8)|(b);
    }

}
