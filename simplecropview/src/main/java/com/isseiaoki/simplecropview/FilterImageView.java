package com.isseiaoki.simplecropview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class FilterImageView extends ImageView {
    private static final String TAG = "FilterImageView";

    private Bitmap imgBitmap;
    private boolean imgFlag = true;
    private boolean isDiagonal = false;

    private FilterMode mFilterMode = FilterMode.NO_FILTER;

    public FilterImageView(Context context) {
        super(context);
    }

    public FilterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    public enum FilterMode {
        NO_FILTER(0), INVERT_COLORS(1), GREY_SCALE(2), SEPIA(3), FILTER_4(4), FILTER_5(5), FILTER_6(6);

        private final int id;

        FilterMode(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public void setFilterMode(FilterMode filterMode) {
        if (imgFlag) {
            imgBitmap = getBitmap();
            imgFlag = false;
        }
        if (filterMode.getId() != mFilterMode.getId()) {
            this.mFilterMode = filterMode;
            ApplyFilter applyFilter = new ApplyFilter(this);
            applyFilter.execute();
            invalidate();
        }
    }

    public void setIsDiagonal(boolean isDiagonal) {
        this.isDiagonal = isDiagonal;;
        if (mFilterMode == FilterMode.NO_FILTER)
            return;
        ApplyFilter applyFilter = new ApplyFilter(this);
        applyFilter.execute();
        invalidate();
    }


    private static class ApplyFilter extends AsyncTask<Void, Void, Bitmap> {

        private WeakReference<FilterImageView> imageViewWeakReference;
        private Bitmap bitmap;
        private int height;
        private int width;
        private FilterMode filterMode;
        private boolean isDiagonal;


        ApplyFilter(FilterImageView filterImageView) {
            imageViewWeakReference = new WeakReference<>(filterImageView);
            bitmap = imageViewWeakReference.get().getImgBitmap();
            width = bitmap.getWidth();
            height = bitmap.getHeight();
            this.filterMode = imageViewWeakReference.get().mFilterMode;
            this.isDiagonal = imageViewWeakReference.get().isDiagonal;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap imgOut = Bitmap.createBitmap(width, height, bitmap.getConfig());
            switch (filterMode) {
                case NO_FILTER:
                    imgOut = Bitmap.createBitmap(this.bitmap);
                    break;
                case INVERT_COLORS:
                    applyMFilter(imgOut);
                    break;
                case GREY_SCALE:
                    applyGreyScale(imgOut);
                    break;
                case SEPIA:
                    applySepia(imgOut);
                    break;
                case FILTER_4:
                    applyFilter4(imgOut);
                    break;
                case FILTER_5:
                    applyFilter5(imgOut);
                    break;
                case FILTER_6:
                    break;
            }
            return imgOut;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewWeakReference != null && bitmap != null) {
                FilterImageView imageView = imageViewWeakReference.get();
                if (imageView != null) {
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                    imageView.setImageDrawable(bitmapDrawable);
//                    imageView.setImageBitmap(bitmap);
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
                    R = Math.min( (int) (2.13 * Color.red(pixel)), 255);
                    G = Math.min((int) (  Color.green(pixel)), 255);
                    B = Math.min( (int) ( 2.32 * Color.blue(pixel)), 255);

                    if (isDiagonal) {
                        int newPixel = 0;
                        if (x < y - height / 2) {
                            // apply inverse at lower
                            newPixel = Color.argb(A, R, G, B);

                        } else if ((x - (height / 2)) > y) {
                            // apply inverse upper
                            newPixel = Color.argb(A, R, G, B);

                        } else {
                            //  don't apply inverse
                            newPixel = pixel;
                        }
                        bitmap.setPixel(x, y, newPixel);
                    } else
                        bitmap.setPixel(x, y, Color.argb(A, R, G, B));
                }
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

                    if (isDiagonal) {
                        int newPixel = 0;
                        if (x < y - height / 2) {
                            // apply GS at lower
                            newPixel = Color.argb(A, R, G, B);

                        } else if ((x - (height / 2)) > y) {
                            // apply GS upper
                            newPixel = Color.argb(A, R, G, B);

                        } else {
                            //  don't apply GS
                            newPixel = pixel;
                        }
                        bitmap.setPixel(x, y, newPixel);
                    } else
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

                    if (isDiagonal) {
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
                    else
                        bitmap.setPixel(x, y, Color.argb(A, newR, newG, newB));
                }
            }
        }

        private void applyFilter4(Bitmap bitmap) {
            int A, R, G, B;
            int pixel;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pixel = this.bitmap.getPixel(x, y);

                    A = Color.alpha(pixel);
                    R = Math.min( (int) (2.45 * Color.red(pixel)), 255);
                    G = Math.min((int) (1.65 *  Color.green(pixel)), 255);
                    B = Math.min( (int) (2.32 * Color.blue(pixel)), 255);

                    if (isDiagonal) {
                        int newPixel = 0;
                        if (x < y - height / 2) {
                            // apply inverse at lower
                            newPixel = Color.argb(A, R, G, B);

                        } else if ((x - (height / 2)) > y) {
                            // apply inverse upper
                            newPixel = Color.argb(A, R, G, B);

                        } else {
                            //  don't apply inverse
                            newPixel = pixel;
                        }
                        bitmap.setPixel(x, y, newPixel);
                    } else
                        bitmap.setPixel(x, y, Color.argb(A, R, G, B));
                }
            }
        }

        private void applyFilter5(Bitmap bitmap) {
            int A, R, G, B;
            int pixel;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pixel = this.bitmap.getPixel(x, y);

                    A = Color.alpha(pixel);
                    R = Math.min( (int) (2.13 * Color.red(pixel)), 255);
                    G = Math.min((int) (  Color.green(pixel)), 255);
                    B = Math.min( (int) ( Color.blue(pixel)), 255);

                    if (isDiagonal) {
                        int newPixel = 0;
                        if (x < y - height / 2) {
                            // apply inverse at lower
                            newPixel = Color.argb(A, R, G, B);

                        } else if ((x - (height / 2)) > y) {
                            // apply inverse upper
                            newPixel = Color.argb(A, R, G, B);

                        } else {
                            //  don't apply inverse
                            newPixel = pixel;
                        }
                        bitmap.setPixel(x, y, newPixel);
                    } else
                        bitmap.setPixel(x, y, Color.argb(A, R, G, B));
                }
            }
        }
//        private void applyDiagonalSepia(Bitmap bitmap) {
//            int A, R, G, B;
//            int pixel;
//
//            for (int x = 0; x < width; ++x) {
//                for (int y = 0; y < height; ++y) {
//                    pixel = this.bitmap.getPixel(x, y);
//
//                    A = Color.alpha(pixel);
//                    R = Color.red(pixel);
//                    G = Color.green(pixel);
//                    B = Color.blue(pixel);
//
//                    int newR = (int) (0.393 * R + 0.769 * G + 0.189 * B);
//                    int newG = (int) (0.349 * R + 0.686 * G + 0.168 * B);
//                    int newB = (int) (0.272 * R + 0.534 * G + 0.131 * B);
//
//                    newR = Math.min(newR, 255);
//                    newG = Math.min(newG, 255);
//                    newB = Math.min(newB, 255);
//
////                    bitmap.setPixel(x, y, Color.argb(A, newR, newG, newB));
//
//                    int newPixel = 0;
//                    if (x < y - height / 2) {
//                        // apply sepia at lower
//                        newPixel = Color.argb(A, newR, newG, newB);
//
//                    } else if ((x - (height / 2)) > y) {
//                        // apply sepia upper
//                        newPixel = Color.argb(A, newR, newG, newB);
//
//                    } else {
//                        //  don't apply sepia
//                        newPixel = pixel;
//                    }
//                    bitmap.setPixel(x, y, newPixel);
//                }
//            }
//        }
    }

}

