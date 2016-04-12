package com.isseiaoki.simplecropview.callback;


import android.net.Uri;

public interface SaveCallback extends Callback{
    void onSuccess(Uri outputUri);
    void onError();
}
