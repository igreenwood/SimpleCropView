package com.example.simplecropviewsample;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.isseiaoki.simplecropview.FilterImageView;
import com.isseiaoki.simplecropview.util.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.disposables.CompositeDisposable;


public class FilterFragment extends Fragment {
    private static final String TAG = FilterFragment.class.getSimpleName();

    private static final int REQUEST_PICK_IMAGE = 10011;
    private static final int REQUEST_SAF_PICK_IMAGE = 10012;
    private static final String PROGRESS_DIALOG = "ProgressDialog";
    private static final String KEY_FRAME_RECT = "FrameRect";
    private static final String KEY_SOURCE_URI = "SourceUri";

    // Views ///////////////////////////////////////////////////////////////////////////////////////
//    private ImageView mImageView;
    private FilterImageView mImageView;
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
//    private RectF mFrameRect = null;
    private Uri uri = null;

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

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.i("Filter fragment", "on create");
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        Log.i("Filter fragment", "on create view");
        uri = Uri.parse(getArguments().getString("Uri"));
        return inflater.inflate(R.layout.fragment_filter, null, false);

//        mImageView = (ImageView) findViewById(R.id.result_image);
//        mExecutor = Executors.newSingleThreadExecutor();
//
//        final Uri uri = getIntent().getData();
//        mExecutor.submit(new ResultActivity.LoadScaledImageTask(this, uri, mImageView, calcImageSize()));
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // bind Views
        bindViews(view);

        Log.i("Filter fragment", "on view created");

        // load image
//        mDisposable.add(loadImage(mSourceUri));
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save data
//        outState.putParcelable(KEY_FRAME_RECT, mImageView.getActualCropRect());
//        outState.putParcelable(KEY_SOURCE_URI, mImageView.getSourceUri());
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mDisposable.dispose();
    }

//    @Override public void onActivityResult(int requestCode, int resultCode, Intent result) {
//        super.onActivityResult(requestCode, resultCode, result);
//        if (resultCode == Activity.RESULT_OK) {
//            // reset frame rect
//            mFrameRect = null;
//            switch (requestCode) {
//                case REQUEST_PICK_IMAGE:
//                    mDisposable.add(loadImage(result.getData()));
//                    break;
//                case REQUEST_SAF_PICK_IMAGE:
//                    mDisposable.add(loadImage(Utils.ensureUriPermission(getContext(), result)));
//                    break;
//            }
//        }
//    }

//    private Disposable loadImage(final Uri uri) {
//        mSourceUri = uri;
//        return new RxPermissions(getActivity()).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                .filter(new Predicate<Boolean>() {
//                    @Override public boolean test(@io.reactivex.annotations.NonNull Boolean granted)
//                            throws Exception {
//                        return granted;
//                    }
//                })
//                .flatMapCompletable(new Function<Boolean, CompletableSource>() {
//                    @Override
//                    public CompletableSource apply(@io.reactivex.annotations.NonNull Boolean aBoolean)
//                            throws Exception {
//                        return mImageView.load(uri)
//                                .useThumbnail(true)
//                                .initialFrameRect(mFrameRect)
//                                .executeAsCompletable();
//                    }
//                })
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action() {
//                    @Override public void run() throws Exception {
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override public void accept(@NonNull Throwable throwable) throws Exception {
//                    }
//                });
//    }

//    private Disposable cropImage() {
//        return mImageView.crop(mSourceUri)
//                .executeAsSingle()
//                .flatMap(new Function<Bitmap, SingleSource<Uri>>() {
//                    @Override public SingleSource<Uri> apply(@io.reactivex.annotations.NonNull Bitmap bitmap)
//                            throws Exception {
//                        return mImageView.save(bitmap)
//                                .compressFormat(mCompressFormat)
//                                .executeAsSingle(createSaveUri());
//                    }
//                })
//                .doOnSubscribe(new Consumer<Disposable>() {
//                    @Override public void accept(@io.reactivex.annotations.NonNull Disposable disposable)
//                            throws Exception {
//                        showProgress();
//                    }
//                })
//                .doFinally(new Action() {
//                    @Override public void run() throws Exception {
//                        dismissProgress();
//                    }
//                })
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<Uri>() {
//                    @Override public void accept(@io.reactivex.annotations.NonNull Uri uri) throws Exception {
//                        ((FilterActivity) getActivity()).startResultActivity(uri);
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override public void accept(@io.reactivex.annotations.NonNull Throwable throwable)
//                            throws Exception {
//                    }
//                });
//    }

    // Bind views //////////////////////////////////////////////////////////////////////////////////


    private void sendImageToResult(){
        // TODO: 6/8/20 must be completed with create uri for image and send to result activity


    }

    private void bindViews(View view) {


        mImageView = (FilterImageView) view.findViewById(R.id.filterImageView);
        view.findViewById(R.id.filterButtonDone).setOnClickListener(btnListener);
        view.findViewById(R.id.NoFilterButton).setOnClickListener(btnListener);
        view.findViewById(R.id.Filter1Button).setOnClickListener(btnListener);
        view.findViewById(R.id.Filter2Button).setOnClickListener(btnListener);
        view.findViewById(R.id.Filter3Button).setOnClickListener(btnListener);
        view.findViewById(R.id.Filter4Button).setOnClickListener(btnListener);


        mExecutor = Executors.newSingleThreadExecutor();
        mExecutor.submit(new ResultActivity.LoadScaledImageTask(getActivity(), uri, mImageView, calcImageSize()));

        Log.i("Filter fragment", "bindViews");

    }

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

    public Uri createSaveUri() {
        return createNewUri(getContext(), mCompressFormat);
    }

    public static String getDirPath() {
        String dirPath = "";
        File imageDir = null;
        File extStorageDir = Environment.getExternalStorageDirectory();
        if (extStorageDir.canWrite()) {
            imageDir = new File(extStorageDir.getPath() + "/simplecropview");
        }
        if (imageDir != null) {
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }
            if (imageDir.canWrite()) {
                dirPath = imageDir.getPath();
            }
        }
        return dirPath;
    }

    public static Uri getUriFromDrawableResId(Context context, int drawableResId) {
        StringBuilder builder = new StringBuilder().append(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .append("://")
                .append(context.getResources().getResourcePackageName(drawableResId))
                .append("/")
                .append(context.getResources().getResourceTypeName(drawableResId))
                .append("/")
                .append(context.getResources().getResourceEntryName(drawableResId));
        return Uri.parse(builder.toString());
    }

    public static Uri createNewUri(Context context, Bitmap.CompressFormat format) {
        long currentTimeMillis = System.currentTimeMillis();
        Date today = new Date(currentTimeMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String title = dateFormat.format(today);
        String dirPath = getDirPath();
        String fileName = "scv" + title + "." + getMimeType(format);
        String path = dirPath + "/" + fileName;
        File file = new File(path);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + getMimeType(format));
        values.put(MediaStore.Images.Media.DATA, path);
        long time = currentTimeMillis / 1000;
        values.put(MediaStore.MediaColumns.DATE_ADDED, time);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time);
        if (file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length());
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Logger.i("SaveUri = " + uri);
        return uri;
    }

    public static String getMimeType(Bitmap.CompressFormat format) {
        switch (format) {
            case JPEG:
                return "jpeg";
            case PNG:
                return "png";
        }
        return "png";
    }

    // Handle button event /////////////////////////////////////////////////////////////////////////

    private final View.OnClickListener btnListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            switch (v.getId()) {
                case R.id.filterButtonDone:
                    sendImageToResult();
                    break;
                case R.id.NoFilterButton:
                    // TODO: 6/7/20 no filter
                    mImageView.setFilterMode(FilterImageView.FilterMode.NO_FILTER);
                    break;
                case R.id.Filter1Button:
                    // TODO: 6/7/20 filter 1
                    mImageView.setFilterMode(FilterImageView.FilterMode.INVERT_COLORS);
                    break;
                case R.id.Filter2Button:
                    // TODO: 6/7/20 filter 2
                    mImageView.setFilterMode(FilterImageView.FilterMode.SEPIA);
                    break;
                case R.id.Filter3Button:
                    // TODO: 6/7/20 filter 3
                    mImageView.setFilterMode(FilterImageView.FilterMode.GREY_SCALE);
                    break;
                case R.id.Filter4Button:
                    // TODO: 6/7/20 filter 4
                    mImageView.setFilterMode(FilterImageView.FilterMode.DIAGONAL_SEPIA);
                    break;
            }
        }
    };


    private int calcImageSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        return Math.min(Math.max(metrics.widthPixels, metrics.heightPixels), 2048);
    }
}