package com.isseiaoki.simplecropview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class FilterImageView extends ImageView {
    private static final String TAG = "FilterImageView";

    private Bitmap NonFilterBitmap;
    private Bitmap ImgBitmap;

    private FilterMode mFilterMode = FilterMode.NO_FILTER;

    public FilterImageView(Context context) {
        super(context);
//        NonFilterBitmap = getBitmap().copy(getBitmap().getConfig(), false);
//        ImgBitmap = getBitmap().copy(getBitmap().getConfig(), true);
    }

    public FilterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        NonFilterBitmap = getBitmap().copy(getBitmap().getConfig(), false);
//        ImgBitmap = getBitmap().copy(getBitmap().getConfig(), true);
    }

    public FilterImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        NonFilterBitmap = getBitmap().copy(getBitmap().getConfig(), false);
//        ImgBitmap = getBitmap().copy(getBitmap().getConfig(), true);
    }

    public Bitmap getBitmap() {
        Bitmap bm = null;
        Drawable drawable = getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Log.d(TAG, "getBitmap: BITMAP OK");
            bm = ((BitmapDrawable) drawable).getBitmap();
        }
        return bm;
    }

    public Bitmap getNonFilterBitmap() {
        return NonFilterBitmap;
    }

    public Bitmap getImgBitmap() {
        return ImgBitmap;
    }


    public enum FilterMode {
        NO_FILTER(0), mFilter(1), GREY_SCALE(2), SEPIA(3), DIAGONAL_SEPIA(4);

        private final int id;

        FilterMode(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public void setFilterMode(FilterMode filterMode) {
        this.mFilterMode = filterMode;
        ApplyFilter applyFilter = new ApplyFilter(this, filterMode);
        applyFilter.execute();
        invalidate();
    }


    private static class ApplyFilter extends AsyncTask<Void, Void, Bitmap> {

        private WeakReference<FilterImageView> imageViewWeakReference;
        private Bitmap bitmap;
        private int height;
        private int width;
        private FilterMode filterMode;


        ApplyFilter(FilterImageView filterImageView, FilterMode filterMode) {
            imageViewWeakReference = new WeakReference<>(filterImageView);
            bitmap = imageViewWeakReference.get().getBitmap();
            width = bitmap.getWidth();
            height = bitmap.getHeight();
            this.filterMode = imageViewWeakReference.get().mFilterMode;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap imgOut = Bitmap.createBitmap(width, height, bitmap.getConfig());
            switch (filterMode) {
                case NO_FILTER:
//                    returnTheOrginalImg(imgOut); //todo fix this
                    break;
                case mFilter:
                    applyMFilter(imgOut);
                    break;
                case GREY_SCALE:
                    applyGreyScale(imgOut);
                    break;
                case SEPIA:
                    applySepia(imgOut);
                    break;
                case DIAGONAL_SEPIA:
                    applyDiagonalSepia(imgOut);
                    break;
            }
            return imgOut;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewWeakReference != null && bitmap != null) {
                Log.d(TAG, "onPostExecute: bitmap not null");
                FilterImageView imageView = imageViewWeakReference.get();
                if (imageView != null) {
                    Log.d(TAG, "onPostExecute: imageView not null");
                    imageView.setImageBitmap(bitmap);

                }
            }
        }


        private void applyMFilter(Bitmap bitmap) {
            int A, R, G, B;
            int pixel;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pixel = this.bitmap.getPixel(x, y);

                    A = Color.alpha(pixel);
                    R = Color.red(pixel);
                    G = Color.green(pixel);
                    B = Color.blue(pixel);

                    bitmap.setPixel(x, y, Color.argb(A, 255 - R, 255 - G, 255 - B));
                }
            }
        }

        private void returnTheOrginalImg(Bitmap bitmap) {
            if (imageViewWeakReference != null & bitmap != null) {
//                bitmap = imageViewWeakReference.get().getNonFilterBitmap();
            }
        }

        private void applyGreyScale(Bitmap bitmap) {
            final double GS_RED = 0.299;
            final double GS_GREEN = 0.587;
            final double GS_BLUE = 0.114;

            int A, R, G, B;
            int pixel;

            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    pixel = this.bitmap.getPixel(x, y);

                    A = Color.alpha(pixel);
                    R = Color.red(pixel);
                    G = Color.green(pixel);
                    B = Color.blue(pixel);

                    R = G = B = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);

                    bitmap.setPixel(x, y, Color.argb(A, R, G, B));
                }
            }
        }

        private void applySepia(Bitmap bitmap) {

            int A, R, G, B;
            int pixel;

            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    pixel = this.bitmap.getPixel(x, y);

                    A = Color.alpha(pixel);
                    R = Color.red(pixel);
                    G = Color.green(pixel);
                    B = Color.blue(pixel);

                    int newR = (int) (0.393 * R + 0.769 * G + 0.189 * B);
                    int newG = (int) (0.349 * R + 0.686 * G + 0.168 * B);
                    int newB = (int) (0.272 * R + 0.534 * G + 0.131 * B);

                    newR = Math.min(newR, 255);
                    newG = Math.min(newG, 255);
                    newB = Math.min(newB, 255);

                    bitmap.setPixel(x, y, Color.argb(A, newR, newG, newB));
                }
            }
        }

        private void applyDiagonalSepia(Bitmap bitmap) {
            int A, R, G, B;
            int pixel;

            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    pixel = this.bitmap.getPixel(x, y);

                    A = Color.alpha(pixel);
                    R = Color.red(pixel);
                    G = Color.green(pixel);
                    B = Color.blue(pixel);

                    int newR = (int) (0.393 * R + 0.769 * G + 0.189 * B);
                    int newG = (int) (0.349 * R + 0.686 * G + 0.168 * B);
                    int newB = (int) (0.272 * R + 0.534 * G + 0.131 * B);

                    newR = Math.min(newR, 255);
                    newG = Math.min(newG, 255);
                    newB = Math.min(newB, 255);

                    bitmap.setPixel(x, y, Color.argb(A, newR, newG, newB));

                    int newPixel = 0;
                    if (x < y - height / 2) {
                        // apply sepia at lower
                        newPixel = Color.argb(A, newR, newG, newB);

                    } else if ((x - (height / 2)) > y) {
                        // apply sepia upper
                        newPixel = Color.argb(A, newR, newG, newB);

                    } else {
                        //  don't apply sepia
                        newPixel = pixel;
                    }
                    bitmap.setPixel(x, y, newPixel);
                }
            }
        }
    }

}

