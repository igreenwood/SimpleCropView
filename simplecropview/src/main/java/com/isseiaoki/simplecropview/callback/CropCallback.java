package com.isseiaoki.simplecropview.callback;

import android.graphics.Bitmap;

public interface CropCallback extends Callback {
    void onSuccess(Bitmap cropped);
    void onError();
}
