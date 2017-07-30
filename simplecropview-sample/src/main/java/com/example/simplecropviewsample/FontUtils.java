package com.example.simplecropviewsample;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("unused") public class FontUtils {
  public static Typeface sTypeface = null;

  /**
   * Load font from filePath
   *
   * @param context context
   * @param fileName font file name
   * @return typeface
   */
  public static Typeface loadFont(Context context, String fileName) {
    sTypeface = Typeface.createFromAsset(context.getAssets(), fileName);
    return sTypeface;
  }

  /**
   * Sets the font on all TextViews in the ViewGroup. Searches recursively for
   * all inner ViewGroups as well. Just add a check for any other views you
   * want to set as well (EditText, etc.)
   */
  public static void setFont(ViewGroup group) {
    int count = group.getChildCount();
    View v;
    for (int i = 0; i < count; i++) {
      v = group.getChildAt(i);
      if (v instanceof TextView) {
        ((TextView) v).setTypeface(sTypeface);
      } else if (v instanceof ViewGroup) setFont((ViewGroup) v);
    }
  }

  /**
   * Sets the font on TextView
   */
  public static void setFont(View v) {
    if (v instanceof TextView) {
      ((TextView) v).setTypeface(sTypeface);
    }
  }

  public static void setTitle(ActionBar actionBar, String title){
    SpannableString s = new SpannableString(title);
    s.setSpan(new TypefaceSpan("Roboto-Light.ttf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    actionBar.setTitle(s);
  }

  /**
   * Load font from res/raw
   * <p/>
   * Font in Android Library - Stack Overflow
   * http://stackoverflow.com/questions/7610355/font-in-android-library
   *
   * @param context Context
   * @param resourceId resourceId
   * @return Typeface or null
   */
  @SuppressWarnings("ResultOfMethodCallIgnored") public static Typeface getTypefaceFromRaw(
      Context context, int resourceId) {
    InputStream inputStream = null;
    BufferedOutputStream bos = null;
    OutputStream os = null;
    Typeface typeface = null;
    try {
      // Load font(in res/raw) to memory
      inputStream = context.getResources().openRawResource(resourceId);

      // Output font to temporary file
      String fontFilePath = context.getCacheDir() + "/tmp" + System.currentTimeMillis() + ".raw";

      os = new FileOutputStream(fontFilePath);
      bos = new BufferedOutputStream(os);

      byte[] buffer = new byte[inputStream.available()];
      int length;
      while ((length = inputStream.read(buffer)) > 0) {
        bos.write(buffer, 0, length);
      }

      // When loading completed, delete temporary files
      typeface = Typeface.createFromFile(fontFilePath);
      new File(fontFilePath).delete();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      tryClose(bos);
      tryClose(os);
      tryClose(inputStream);
    }

    return typeface;
  }

  /**
   * Release closeable object
   *
   * @param obj closeable object
   */
  private static void tryClose(Closeable obj) {
    if (obj != null) {
      try {
        obj.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}