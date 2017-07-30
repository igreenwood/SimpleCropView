package com.isseiaoki.simplecropview.util;

@SuppressWarnings("unused") public class Logger {
  private static final String TAG = "SimpleCropView";
  public static boolean enabled = false;

  public static void e(String msg) {
    if (!enabled) return;
    android.util.Log.e(TAG, msg);
  }

  public static void e(String msg, Throwable e) {
    if (!enabled) return;
    android.util.Log.e(TAG, msg, e);
  }

  public static void i(String msg) {
    if (!enabled) return;
    android.util.Log.i(TAG, msg);
  }

  public static void i(String msg, Throwable e) {
    if (!enabled) return;
    android.util.Log.i(TAG, msg, e);
  }
}
