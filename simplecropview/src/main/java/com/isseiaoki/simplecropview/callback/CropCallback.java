package com.isseiaoki.simplecropview.callback;

import android.graphics.Bitmap;

public interface CropCallback {
    void onSuccess(Bitmap cropped);
    void onError();
}
