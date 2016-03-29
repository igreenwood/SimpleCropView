package com.isseiaoki.simplecropview.callback;


import android.net.Uri;

public interface SaveCallback {
    void onSuccess(Uri outputUri);
    void onError();
}
