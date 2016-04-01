package com.example.simplecropviewsample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;

import java.io.File;

public class MainFragment extends Fragment {
    public static final String TAG = MainFragment.class.getSimpleName();
    private static final int REQUEST_PICK_IMAGE = 10011;
    private static final String PROGRESS_DIALOG = "ProgressDialog";

    // Views ///////////////////////////////////////////////////////////////////////////////////////
    private CropImageView mCropView;
    private RelativeLayout mRootLayout;

    // Image file index(1 ~ 5)
    private int mImageIndex = 5;

    // Bundle key for Save/Restore state ///////////////////////////////////////////////////////////
    private static final String KEY_IMG_INDEX = "img_index";

    // Note: only the system can call this constructor by reflection. 
    public MainFragment() {
    }

    public static MainFragment getInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, null, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // bind Views
        bindViews(view);
        // apply custom font
        FontUtils.setFont(mRootLayout);
        // set bitmap to CropImageView
        if (mCropView.getImageBitmap() == null) {
            mCropView.setImageBitmap(getImageForIndex(mImageIndex));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mImageIndex = savedInstanceState.getInt(KEY_IMG_INDEX);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_IMG_INDEX, mImageIndex);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            showProgress();
            mCropView.startLoad(result.getData(), new LoadCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "onSuccess");
                    dismissProgress();
                }

                @Override
                public void onError() {
                    Log.d(TAG, "onError");
                    dismissProgress();
                }
            });
        }
    }

    // Handle button event /////////////////////////////////////////////////////////////////////////

    private final View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.buttonDone:
                    // Get cropped bitmap and pass it to Application
                    showProgress();
                    mCropView.startCrop(
                            createSaveUri(),
                            new CropCallback() {
                                @Override
                                public void onSuccess(Bitmap cropped, int rotationAngle) {
                                    Log.d(TAG, "crop success");
                                }

                                @Override
                                public void onError() {
                                    Log.d(TAG, "crop error");
                                }
                            },
                            new SaveCallback() {
                                @Override
                                public void onSuccess(Uri outputUri) {
                                    Log.d(TAG, "save success");
                                    dismissProgress();
                                    ((MainActivity) getActivity()).startResultActivity(outputUri);
                                }

                                @Override
                                public void onError() {
                                    Log.d(TAG, "save error");
                                    dismissProgress();
                                }
                            });
                    break;
                case R.id.buttonFitImage:
                    mCropView.setCropMode(CropImageView.CropMode.FIT_IMAGE);
                    break;
                case R.id.button1_1:
                    mCropView.setCropMode(CropImageView.CropMode.SQUARE);
                    break;
                case R.id.button3_4:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_3_4);
                    break;
                case R.id.button4_3:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_4_3);
                    break;
                case R.id.button9_16:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_9_16);
                    break;
                case R.id.button16_9:
                    mCropView.setCropMode(CropImageView.CropMode.RATIO_16_9);
                    break;
                case R.id.buttonCustom:
                    mCropView.setCustomRatio(7, 5);
                    break;
                case R.id.buttonFree:
                    mCropView.setCropMode(CropImageView.CropMode.FREE);
                    break;
                case R.id.buttonCircle:
                    mCropView.setCropMode(CropImageView.CropMode.CIRCLE);
                    break;
                case R.id.buttonShowCircleButCropAsSquare:
                    mCropView.setCropMode(CropImageView.CropMode.CIRCLE_SQUARE);
                    break;
//                case R.id.buttonChangeImage:
//                    incrementImageIndex();
//                    mCropView.setImageBitmap(getImageForIndex(mImageIndex));
//                    break;
                case R.id.buttonRotateImage:
                    mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                    break;
                case R.id.buttonPickImage:
                    pickImage();
                    break;
            }
        }
    };

    // Bind views //////////////////////////////////////////////////////////////////////////////////

    private void bindViews(View view) {
        mCropView = (CropImageView) view.findViewById(R.id.cropImageView);
        view.findViewById(R.id.buttonDone).setOnClickListener(btnListener);
        view.findViewById(R.id.buttonFitImage).setOnClickListener(btnListener);
        view.findViewById(R.id.button1_1).setOnClickListener(btnListener);
        view.findViewById(R.id.button3_4).setOnClickListener(btnListener);
        view.findViewById(R.id.button4_3).setOnClickListener(btnListener);
        view.findViewById(R.id.button9_16).setOnClickListener(btnListener);
        view.findViewById(R.id.button16_9).setOnClickListener(btnListener);
        view.findViewById(R.id.buttonFree).setOnClickListener(btnListener);
//        view.findViewById(R.id.buttonChangeImage).setOnClickListener(btnListener);
        view.findViewById(R.id.buttonPickImage).setOnClickListener(btnListener);
        view.findViewById(R.id.buttonRotateImage).setOnClickListener(btnListener);
        view.findViewById(R.id.buttonCustom).setOnClickListener(btnListener);
        view.findViewById(R.id.buttonCircle).setOnClickListener(btnListener);
        view.findViewById(R.id.buttonShowCircleButCropAsSquare).setOnClickListener(btnListener);
        mRootLayout = (RelativeLayout) view.findViewById(R.id.layout_root);
    }

    // Switch image files //////////////////////////////////////////////////////////////////////////

    private void incrementImageIndex() {
        mImageIndex++;
        if (mImageIndex > 5) mImageIndex -= 5;
    }

    public Bitmap getImageForIndex(int index) {
        String fileName = "sample" + index;
        int resId = getResources().getIdentifier(fileName, "mipmap", getActivity().getPackageName());
        return BitmapFactory.decodeResource(getResources(), resId);
    }

    public void pickImage() {
        startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), REQUEST_PICK_IMAGE);
    }

    public void showProgress() {
        ProgressDialogFragment f = ProgressDialogFragment.getInstance();
        getFragmentManager()
                .beginTransaction()
                .add(f, PROGRESS_DIALOG)
                .commit();
    }

    public void dismissProgress() {
        ProgressDialogFragment f = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(PROGRESS_DIALOG);
        if (f != null) {
            getFragmentManager().beginTransaction().remove(f).commit();
        }
    }

    public Uri createSaveUri() {
        return Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
    }
}