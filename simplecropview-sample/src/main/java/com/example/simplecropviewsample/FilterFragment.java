package com.example.simplecropviewsample;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.isseiaoki.simplecropview.FilterImageView;
import com.isseiaoki.simplecropview.util.Utils;

import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.disposables.CompositeDisposable;


public class FilterFragment extends Fragment implements SwitchCompat.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG = FilterFragment.class.getSimpleName();

//    private static final int REQUEST_PICK_IMAGE = 10011;
//    private static final int REQUEST_SAF_PICK_IMAGE = 10012;
//    private static final String PROGRESS_DIALOG = "ProgressDialog";
//    private static final String KEY_FRAME_RECT = "FrameRect";
//    private static final String KEY_SOURCE_URI = "SourceUri";

    // Views ///////////////////////////////////////////////////////////////////////////////////////
    private FilterImageView mImageView;
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.PNG;
    private Uri mUri = null;

    private ExecutorService mExecutor;


    // Note: only the system can call this constructor by reflection.
    public FilterFragment() {
    }

    public static FilterFragment newInstance(Uri uri) {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putString("Uri", uri.toString());
        fragment.setArguments(args);
        Log.i("Filter fragment", "newInstance");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.i("Filter fragment", "on create");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("Filter fragment", "on create view");
        mUri = Uri.parse(getArguments().getString("Uri"));
        return inflater.inflate(R.layout.fragment_filter, null, false);

//        mImageView = (ImageView) findViewById(R.id.result_image);
//        mExecutor = Executors.newSingleThreadExecutor();
//
//        final Uri uri = getIntent().getData();
//        mExecutor.submit(new ResultActivity.LoadScaledImageTask(this, uri, mImageView, calcImageSize()));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // bind Views
        bindViews(view);

        Log.i("Filter fragment", "on view created");

        // load image
//        mDisposable.add(loadImage(mSourceUri));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDisposable.dispose();
    }

    // Bind views //////////////////////////////////////////////////////////////////////////////////

    private Uri saveAndSendImageToResultActivity() {
        OutputStream outputStream = null;
        try {
            outputStream = getContext().getContentResolver().openOutputStream(mUri);
            mImageView.getBitmap().compress(mCompressFormat, 100, outputStream);
            return mUri;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.closeQuietly(outputStream);
        }
        return mUri;
    }

    private void bindViews(View view) {
        mImageView = (FilterImageView) view.findViewById(R.id.filterImageView);
        view.findViewById(R.id.filterButtonDone).setOnClickListener(this);
        view.findViewById(R.id.NoFilterButton).setOnClickListener(this);
        view.findViewById(R.id.Filter1Button).setOnClickListener(this);
        view.findViewById(R.id.Filter2Button).setOnClickListener(this);
        view.findViewById(R.id.Filter3Button).setOnClickListener(this);
        view.findViewById(R.id.Filter4Button).setOnClickListener(this);
        view.findViewById(R.id.Filter5Button).setOnClickListener(this);
        view.findViewById(R.id.Filter6Button).setOnClickListener(this);
        view.findViewById(R.id.Filter7Button).setOnClickListener(this);
        Switch diagonalSwitch = (Switch) view.findViewById(R.id.diagonal_switch_button);
        diagonalSwitch.setOnCheckedChangeListener(this);

        mExecutor = Executors.newSingleThreadExecutor();
        mExecutor.submit(new ResultActivity.LoadScaledImageTask(getActivity(), mUri, mImageView, calcImageSize()));

        Log.i("Filter fragment", "bindViews");
    }

    private int calcImageSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        return Math.min(Math.max(metrics.widthPixels, metrics.heightPixels), 2048);
    }

    // Handle button event /////////////////////////////////////////////////////////////////////////

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.diagonal_switch_button) {
            if (buttonView.isChecked()) {
                mImageView.setIsDiagonal(true);
            } else {
                mImageView.setIsDiagonal(false);
            }
            filterButtonClicked();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filterButtonDone:
                ((FilterActivity) getContext()).startResultActivity(saveAndSendImageToResultActivity());
                break;
            case R.id.NoFilterButton:
                mImageView.setFilterMode(FilterImageView.FilterMode.NO_FILTER);
                filterButtonClicked();
                break;
            case R.id.Filter1Button:
                mImageView.setFilterMode(FilterImageView.FilterMode.INVERT_COLORS);
                filterButtonClicked();
                break;
            case R.id.Filter2Button:
                mImageView.setFilterMode(FilterImageView.FilterMode.SEPIA);
                filterButtonClicked();
                break;
            case R.id.Filter3Button:
                mImageView.setFilterMode(FilterImageView.FilterMode.GREY_SCALE);
                filterButtonClicked();
                break;
            case R.id.Filter4Button:
                mImageView.setFilterMode(FilterImageView.FilterMode.FILTER_4);
                filterButtonClicked();
                break;
            case R.id.Filter5Button:
                mImageView.setFilterMode(FilterImageView.FilterMode.FILTER_5);
                filterButtonClicked();
                break;
            case R.id.Filter6Button:
                mImageView.setFilterMode(FilterImageView.FilterMode.FILTER_6);
                filterButtonClicked();
                break;
            case R.id.Filter7Button:
                mImageView.setFilterMode(FilterImageView.FilterMode.FILTER_7);
                filterButtonClicked();
                break;
        }
    }

    //    Show progress //////////////////////////////////////////////////////////
    private static final String PROGRESS_DIALOG = "ProgressDialog";

    public void showProgress() {
        ProgressDialogFragment f = ProgressDialogFragment.getInstance();
        getFragmentManager().beginTransaction().add(f, PROGRESS_DIALOG).commitAllowingStateLoss();
    }

    public void dismissProgress() {
        if (!isResumed()) return;
        android.support.v4.app.FragmentManager manager = getFragmentManager();
        if (manager == null) return;
        ProgressDialogFragment f = (ProgressDialogFragment) manager.findFragmentByTag(PROGRESS_DIALOG);
        if (f != null) {
            getFragmentManager().beginTransaction().remove(f).commitAllowingStateLoss();
        }
    }

    private void filterButtonClicked() {
        showProgress();
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mImageView.getIsStarted() && mImageView.getIsFinished()){
                    dismissProgress();
                    mImageView.setIsStarted(false);
                    mImageView.setIsFinished(false);
                }
                else if (!mImageView.getIsStarted()){
                    dismissProgress();
                    timer.cancel();
                    this.cancel();
                }
            }
        }, 200, 300);
    }

}