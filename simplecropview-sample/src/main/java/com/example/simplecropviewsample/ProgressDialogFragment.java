package com.example.simplecropviewsample;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {
  public static final String TAG = ProgressDialogFragment.class.getSimpleName();

  // Note: only the system can call this constructor by reflection.
  public ProgressDialogFragment() {
  }

  public static ProgressDialogFragment getInstance() {
    ProgressDialogFragment fragment = new ProgressDialogFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable
  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_progress_dialog, null, false);
    ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress);
    progressBar.getIndeterminateDrawable()
        .setColorFilter(requireContext().getResources().getColor(R.color.colorAccent),
            PorterDuff.Mode.SRC_IN);
    return view;
  }

  @NonNull
  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    // タッチしても消えないように設定
    dialog.setCancelable(false);
    // ビュー全体のリスナ
    dialog.setCanceledOnTouchOutside(false);
    dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
      @Override public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        // Disable Back key and Search key
        switch (keyCode) {
          case KeyEvent.KEYCODE_BACK:
          case KeyEvent.KEYCODE_SEARCH:
            return true;
          default:
            return false;
        }
      }
    });
    return dialog;
  }
}