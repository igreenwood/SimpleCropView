package com.isseiaoki.simplecropview;

import com.isseiaoki.simplecropview.animation.SimpleValueAnimator;
import com.isseiaoki.simplecropview.animation.SimpleValueAnimatorListener;
import com.isseiaoki.simplecropview.animation.ValueAnimatorV14;
import com.isseiaoki.simplecropview.animation.ValueAnimatorV8;
import com.isseiaoki.simplecropview.callback.Callback;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.isseiaoki.simplecropview.util.Logger;
import com.isseiaoki.simplecropview.util.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class CropImageView extends ImageView {
    private static final String TAG = CropImageView.class.getSimpleName();

    // Constants ///////////////////////////////////////////////////////////////////////////////////

    private static final int HANDLE_SIZE_IN_DP = 14;
    private static final int MIN_FRAME_SIZE_IN_DP = 50;
    private static final int FRAME_STROKE_WEIGHT_IN_DP = 1;
    private static final int GUIDE_STROKE_WEIGHT_IN_DP = 1;
    private static final float DEFAULT_INITIAL_FRAME_SCALE = 1f;
    private static final int DEFAULT_ANIMATION_DURATION_MILLIS = 100;
    private static final int DEBUG_TEXT_SIZE_IN_DP = 15;

    private static final int TRANSPARENT = 0x00000000;
    private static final int TRANSLUCENT_WHITE = 0xBBFFFFFF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int TRANSLUCENT_BLACK = 0xBB000000;

    // Member variables ////////////////////////////////////////////////////////////////////////////

    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private float mScale = 1.0f;
    private float mAngle = 0.0f;
    private float mImgWidth = 0.0f;
    private float mImgHeight = 0.0f;

    private boolean mIsInitialized = false;
    private Matrix mMatrix = null;
    private Paint mPaintTranslucent;
    private Paint mPaintFrame;
    private Paint mPaintBitmap;
    private Paint mPaintDebug;
    private RectF mFrameRect;
    private RectF mImageRect;
    private PointF mCenter = new PointF();
    private float mLastX, mLastY;
    private boolean mIsRotating = false;
    private boolean mIsAnimating = false;
    private SimpleValueAnimator mAnimator = null;
    private final Interpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator();
    private Interpolator mInterpolator = DEFAULT_INTERPOLATOR;
    private LoadCallback mLoadCallback = null;
    private CropCallback mCropCallback = null;
    private SaveCallback mSaveCallback = null;
    private ExecutorService mExecutor;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Uri mSourceUri = null;
    private Uri mSaveUri = null;
    private int mExifRotation = 0;
    private int mOutputMaxWidth;
    private int mOutputMaxHeight;
    private int mOutputWidth = 0;
    private int mOutputHeight = 0;
    private boolean mIsDebug = false;
    private boolean mIsCropping = false;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.PNG;
    private int mCompressQuality = 100;
    private int mInputImageWidth = 0;
    private int mInputImageHeight = 0;
    private int mOutputImageWidth = 0;
    private int mOutputImageHeight = 0;
    private boolean mIsLoading = false;
    // Instance variables for customizable attributes //////////////////////////////////////////////

    private TouchArea mTouchArea = TouchArea.OUT_OF_BOUNDS;

    private CropMode mCropMode = CropMode.SQUARE;
    private ShowMode mGuideShowMode = ShowMode.SHOW_ALWAYS;
    private ShowMode mHandleShowMode = ShowMode.SHOW_ALWAYS;
    private float mMinFrameSize;
    private int mHandleSize;
    private int mTouchPadding = 0;
    private boolean mShowGuide = true;
    private boolean mShowHandle = true;
    private boolean mIsCropEnabled = true;
    private boolean mIsEnabled = true;
    private PointF mCustomRatio = new PointF(1.0f, 1.0f);
    private float mFrameStrokeWeight = 2.0f;
    private float mGuideStrokeWeight = 2.0f;
    private int mBackgroundColor;
    private int mOverlayColor;
    private int mFrameColor;
    private int mHandleColor;
    private int mGuideColor;
    private float mInitialFrameScale; // 0.01 ~ 1.0, 0.75 is default value
    private boolean mIsAnimationEnabled = true;
    private int mAnimationDurationMillis = DEFAULT_ANIMATION_DURATION_MILLIS;
    private boolean mIsHandleShadowEnabled = true;

    // Constructor /////////////////////////////////////////////////////////////////////////////////

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mExecutor = Executors.newSingleThreadExecutor();
        float density = getDensity();
        mHandleSize = (int) (density * HANDLE_SIZE_IN_DP);
        mMinFrameSize = density * MIN_FRAME_SIZE_IN_DP;
        mFrameStrokeWeight = density * FRAME_STROKE_WEIGHT_IN_DP;
        mGuideStrokeWeight = density * GUIDE_STROKE_WEIGHT_IN_DP;

        mPaintFrame = new Paint();
        mPaintTranslucent = new Paint();
        mPaintBitmap = new Paint();
        mPaintBitmap.setFilterBitmap(true);
        mPaintDebug = new Paint();
        mPaintDebug.setAntiAlias(true);
        mPaintDebug.setStyle(Paint.Style.STROKE);
        mPaintDebug.setColor(WHITE);
        mPaintDebug.setTextSize((float) DEBUG_TEXT_SIZE_IN_DP * density);

        mMatrix = new Matrix();
        mScale = 1.0f;
        mBackgroundColor = TRANSPARENT;
        mFrameColor = WHITE;
        mOverlayColor = TRANSLUCENT_BLACK;
        mHandleColor = WHITE;
        mGuideColor = TRANSLUCENT_WHITE;

        // handle Styleable
        handleStyleable(context, attrs, defStyle, density);
    }

    // Lifecycle methods ///////////////////////////////////////////////////////////////////////////

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.image = getBitmap();
        ss.mode = this.mCropMode;
        ss.backgroundColor = this.mBackgroundColor;
        ss.overlayColor = this.mOverlayColor;
        ss.frameColor = this.mFrameColor;
        ss.guideShowMode = this.mGuideShowMode;
        ss.handleShowMode = this.mHandleShowMode;
        ss.showGuide = this.mShowGuide;
        ss.showHandle = this.mShowHandle;
        ss.handleSize = this.mHandleSize;
        ss.touchPadding = this.mTouchPadding;
        ss.minFrameSize = this.mMinFrameSize;
        ss.customRatioX = this.mCustomRatio.x;
        ss.customRatioY = this.mCustomRatio.y;
        ss.frameStrokeWeight = this.mFrameStrokeWeight;
        ss.guideStrokeWeight = this.mGuideStrokeWeight;
        ss.isCropEnabled = this.mIsCropEnabled;
        ss.handleColor = this.mHandleColor;
        ss.guideColor = this.mGuideColor;
        ss.initialFrameScale = this.mInitialFrameScale;
        ss.angle = this.mAngle;
        ss.isAnimationEnabled = this.mIsAnimationEnabled;
        ss.animationDuration = this.mAnimationDurationMillis;
        ss.exifRotation = this.mExifRotation;
        ss.sourceUri = this.mSourceUri;
        ss.saveUri = this.mSaveUri;
        ss.compressFormat = this.mCompressFormat;
        ss.compressQuality = this.mCompressQuality;
        ss.isDebug = this.mIsDebug;
        ss.outputMaxWidth = this.mOutputMaxWidth;
        ss.outputMaxHeight = this.mOutputMaxHeight;
        ss.outputWidth = this.mOutputWidth;
        ss.outputHeight = this.mOutputHeight;
        ss.isHandleShadowEnabled = this.mIsHandleShadowEnabled;
        ss.inputImageWidth = this.mInputImageWidth;
        ss.inputImageHeight = this.mInputImageHeight;
        ss.outputImageWidth = this.mOutputImageWidth;
        ss.outputImageHeight = this.mOutputImageHeight;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mCropMode = ss.mode;
        this.mBackgroundColor = ss.backgroundColor;
        this.mOverlayColor = ss.overlayColor;
        this.mFrameColor = ss.frameColor;
        this.mGuideShowMode = ss.guideShowMode;
        this.mHandleShowMode = ss.handleShowMode;
        this.mShowGuide = ss.showGuide;
        this.mShowHandle = ss.showHandle;
        this.mHandleSize = ss.handleSize;
        this.mTouchPadding = ss.touchPadding;
        this.mMinFrameSize = ss.minFrameSize;
        this.mCustomRatio = new PointF(ss.customRatioX, ss.customRatioY);
        this.mFrameStrokeWeight = ss.frameStrokeWeight;
        this.mGuideStrokeWeight = ss.guideStrokeWeight;
        this.mIsCropEnabled = ss.isCropEnabled;
        this.mHandleColor = ss.handleColor;
        this.mGuideColor = ss.guideColor;
        this.mInitialFrameScale = ss.initialFrameScale;
        this.mAngle = ss.angle;
        this.mIsAnimationEnabled = ss.isAnimationEnabled;
        this.mAnimationDurationMillis = ss.animationDuration;
        this.mExifRotation = ss.exifRotation;
        this.mSourceUri = ss.sourceUri;
        this.mSaveUri = ss.saveUri;
        this.mCompressFormat = ss.compressFormat;
        this.mCompressQuality = ss.compressQuality;
        this.mIsDebug = ss.isDebug;
        this.mOutputMaxWidth = ss.outputMaxWidth;
        this.mOutputMaxHeight = ss.outputMaxHeight;
        this.mOutputWidth = ss.outputWidth;
        this.mOutputHeight = ss.outputHeight;
        this.mIsHandleShadowEnabled = ss.isHandleShadowEnabled;
        this.mInputImageWidth = ss.inputImageWidth;
        this.mInputImageHeight = ss.inputImageHeight;
        this.mOutputImageWidth = ss.outputImageWidth;
        this.mOutputImageHeight = ss.outputImageHeight;
        setImageBitmap(ss.image);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(viewWidth, viewHeight);

        mViewWidth = viewWidth - getPaddingLeft() - getPaddingRight();
        mViewHeight = viewHeight - getPaddingTop() - getPaddingBottom();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getDrawable() != null) setupLayout(mViewWidth, mViewHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(mBackgroundColor);

        if (mIsInitialized) {
            setMatrix();
            Bitmap bm = getBitmap();
            if (bm != null) {
                canvas.drawBitmap(bm, mMatrix, mPaintBitmap);
                // draw edit frame
                drawCropFrame(canvas);
            }
            if (mIsDebug) {
                drawDebugInfo(canvas);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mExecutor.shutdown();
        super.onDetachedFromWindow();
    }

    // Handle styleable ////////////////////////////////////////////////////////////////////////////

    private void handleStyleable(Context context, AttributeSet attrs, int defStyle,
                                 float mDensity) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.scv_CropImageView,
                defStyle, 0);
        Drawable drawable;
        mCropMode = CropMode.SQUARE;
        try {
            drawable = ta.getDrawable(R.styleable.scv_CropImageView_scv_img_src);
            if (drawable != null) setImageDrawable(drawable);
            for (CropMode mode : CropMode.values()) {
                if (ta.getInt(R.styleable.scv_CropImageView_scv_crop_mode, 3) == mode.getId()) {
                    mCropMode = mode;
                    break;
                }
            }
            mBackgroundColor = ta.getColor(R.styleable.scv_CropImageView_scv_background_color,
                    TRANSPARENT);
            mOverlayColor = ta.getColor(R.styleable.scv_CropImageView_scv_overlay_color,
                    TRANSLUCENT_BLACK);
            mFrameColor = ta.getColor(R.styleable.scv_CropImageView_scv_frame_color, WHITE);
            mHandleColor = ta.getColor(R.styleable.scv_CropImageView_scv_handle_color, WHITE);
            mGuideColor = ta.getColor(R.styleable.scv_CropImageView_scv_guide_color,
                    TRANSLUCENT_WHITE);
            for (ShowMode mode : ShowMode.values()) {
                if (ta.getInt(R.styleable.scv_CropImageView_scv_guide_show_mode,
                        1) == mode.getId()) {
                    mGuideShowMode = mode;
                    break;
                }
            }

            for (ShowMode mode : ShowMode.values()) {
                if (ta.getInt(R.styleable.scv_CropImageView_scv_handle_show_mode,
                        1) == mode.getId()) {
                    mHandleShowMode = mode;
                    break;
                }
            }
            setGuideShowMode(mGuideShowMode);
            setHandleShowMode(mHandleShowMode);
            mHandleSize = ta.getDimensionPixelSize(R.styleable.scv_CropImageView_scv_handle_size,
                    (int) (HANDLE_SIZE_IN_DP * mDensity));
            mTouchPadding = ta.getDimensionPixelSize(
                    R.styleable.scv_CropImageView_scv_touch_padding, 0);
            mMinFrameSize = ta.getDimensionPixelSize(
                    R.styleable.scv_CropImageView_scv_min_frame_size,
                    (int) (MIN_FRAME_SIZE_IN_DP * mDensity));
            mFrameStrokeWeight = ta.getDimensionPixelSize(
                    R.styleable.scv_CropImageView_scv_frame_stroke_weight,
                    (int) (FRAME_STROKE_WEIGHT_IN_DP * mDensity));
            mGuideStrokeWeight = ta.getDimensionPixelSize(
                    R.styleable.scv_CropImageView_scv_guide_stroke_weight,
                    (int) (GUIDE_STROKE_WEIGHT_IN_DP * mDensity));
            mIsCropEnabled = ta.getBoolean(R.styleable.scv_CropImageView_scv_crop_enabled, true);
            mInitialFrameScale = constrain(
                    ta.getFloat(R.styleable.scv_CropImageView_scv_initial_frame_scale,
                            DEFAULT_INITIAL_FRAME_SCALE), 0.01f, 1.0f,
                    DEFAULT_INITIAL_FRAME_SCALE);
            mIsAnimationEnabled = ta.getBoolean(R.styleable.scv_CropImageView_scv_animation_enabled,
                    true);
            mAnimationDurationMillis = ta.getInt(
                    R.styleable.scv_CropImageView_scv_animation_duration,
                    DEFAULT_ANIMATION_DURATION_MILLIS);
            mIsHandleShadowEnabled = ta.getBoolean(R.styleable.scv_CropImageView_scv_handle_shadow_enabled, true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ta.recycle();
        }
    }

    // Drawing method //////////////////////////////////////////////////////////////////////////////

    private void drawDebugInfo(Canvas canvas) {
        Paint.FontMetrics fontMetrics = mPaintDebug.getFontMetrics();
        mPaintDebug.measureText("W");
        int textHeight = (int) (fontMetrics.descent - fontMetrics.ascent);
        int x = (int) (mImageRect.left + (float) mHandleSize * 0.5f * getDensity());
        int y = (int) (mImageRect.top + textHeight + (float) mHandleSize * 0.5f * getDensity());
        StringBuilder builder = new StringBuilder();
        builder.append("LOADED FROM: ")
                .append(mSourceUri != null ? "Uri" : "Bitmap");
        canvas.drawText(builder.toString(), x, y, mPaintDebug);
        builder = new StringBuilder();

        if (mSourceUri == null) {
            builder.append("INPUT_IMAGE_SIZE: ")
                    .append((int) mImgWidth)
                    .append("x")
                    .append((int) mImgHeight);
            y += textHeight;
            canvas.drawText(builder.toString(), x, y, mPaintDebug);
            builder = new StringBuilder();
        } else {
            builder = new StringBuilder()
                    .append("INPUT_IMAGE_SIZE: ")
                    .append(mInputImageWidth)
                    .append("x")
                    .append(mInputImageHeight);
            y += textHeight;
            canvas.drawText(builder.toString(), x, y, mPaintDebug);
            builder = new StringBuilder();
        }
        builder.append("LOADED_IMAGE_SIZE: ")
                .append(getBitmap().getWidth())
                .append("x")
                .append(getBitmap().getHeight());
        y += textHeight;
        canvas.drawText(builder.toString(), x, y, mPaintDebug);
        builder = new StringBuilder();
        if (mOutputImageWidth > 0 && mOutputImageHeight > 0) {
            builder.append("OUTPUT_IMAGE_SIZE: ")
                    .append(mOutputImageWidth)
                    .append("x")
                    .append(mOutputImageHeight);
            y += textHeight;
            canvas.drawText(builder.toString(), x, y, mPaintDebug);
            builder = new StringBuilder()
                    .append("EXIF ROTATION: ")
                    .append(mExifRotation);
            y += textHeight;
            canvas.drawText(builder.toString(), x, y, mPaintDebug);
            builder = new StringBuilder()
                    .append("CURRENT_ROTATION: ")
                    .append((int) mAngle);
            y += textHeight;
            canvas.drawText(builder.toString(), x, y, mPaintDebug);
        }
    }

    private void drawCropFrame(Canvas canvas) {
        if (!mIsCropEnabled) return;
        if (mIsRotating) return;
        drawOverlay(canvas);
        drawFrame(canvas);
        if (mShowGuide) drawGuidelines(canvas);
        if (mShowHandle) drawHandles(canvas);
    }

    private void drawOverlay(Canvas canvas) {
        mPaintTranslucent.setAntiAlias(true);
        mPaintTranslucent.setFilterBitmap(true);
        mPaintTranslucent.setColor(mOverlayColor);
        mPaintTranslucent.setStyle(Paint.Style.FILL);
        Path path = new Path();
        RectF overlayRect = new RectF((float) Math.floor(mImageRect.left),
                (float) Math.floor(mImageRect.top),
                (float) Math.ceil(mImageRect.right),
                (float) Math.ceil(mImageRect.bottom));
        if (!mIsAnimating
                && (mCropMode == CropMode.CIRCLE || mCropMode == CropMode.CIRCLE_SQUARE)) {
            path.addRect(
                    overlayRect,
                    Path.Direction.CW
            );
            PointF circleCenter = new PointF((mFrameRect.left + mFrameRect.right) / 2,
                    (mFrameRect.top + mFrameRect.bottom) / 2);
            float circleRadius = (mFrameRect.right - mFrameRect.left) / 2;
            path.addCircle(circleCenter.x, circleCenter.y, circleRadius, Path.Direction.CCW);
            canvas.drawPath(path, mPaintTranslucent);
        } else {
            path.addRect(
                    overlayRect,
                    Path.Direction.CW
            );
            path.addRect(mFrameRect, Path.Direction.CCW);
            canvas.drawPath(path, mPaintTranslucent);
        }
    }

    private void drawFrame(Canvas canvas) {
        mPaintFrame.setAntiAlias(true);
        mPaintFrame.setFilterBitmap(true);
        mPaintFrame.setStyle(Paint.Style.STROKE);
        mPaintFrame.setColor(mFrameColor);
        mPaintFrame.setStrokeWidth(mFrameStrokeWeight);
        canvas.drawRect(mFrameRect, mPaintFrame);
    }

    private void drawGuidelines(Canvas canvas) {
        mPaintFrame.setColor(mGuideColor);
        mPaintFrame.setStrokeWidth(mGuideStrokeWeight);
        float h1 = mFrameRect.left + (mFrameRect.right - mFrameRect.left) / 3.0f;
        float h2 = mFrameRect.right - (mFrameRect.right - mFrameRect.left) / 3.0f;
        float v1 = mFrameRect.top + (mFrameRect.bottom - mFrameRect.top) / 3.0f;
        float v2 = mFrameRect.bottom - (mFrameRect.bottom - mFrameRect.top) / 3.0f;
        canvas.drawLine(h1, mFrameRect.top, h1, mFrameRect.bottom, mPaintFrame);
        canvas.drawLine(h2, mFrameRect.top, h2, mFrameRect.bottom, mPaintFrame);
        canvas.drawLine(mFrameRect.left, v1, mFrameRect.right, v1, mPaintFrame);
        canvas.drawLine(mFrameRect.left, v2, mFrameRect.right, v2, mPaintFrame);
    }

    private void drawHandles(Canvas canvas) {
        if (mIsHandleShadowEnabled) drawHandleShadows(canvas);
        mPaintFrame.setStyle(Paint.Style.FILL);
        mPaintFrame.setColor(mHandleColor);
        canvas.drawCircle(mFrameRect.left, mFrameRect.top, mHandleSize, mPaintFrame);
        canvas.drawCircle(mFrameRect.right, mFrameRect.top, mHandleSize, mPaintFrame);
        canvas.drawCircle(mFrameRect.left, mFrameRect.bottom, mHandleSize, mPaintFrame);
        canvas.drawCircle(mFrameRect.right, mFrameRect.bottom, mHandleSize, mPaintFrame);
    }

    private void drawHandleShadows(Canvas canvas) {
        mPaintFrame.setStyle(Paint.Style.FILL);
        mPaintFrame.setColor(TRANSLUCENT_BLACK);
        RectF rect = new RectF(mFrameRect);
        rect.offset(0, 1);
        canvas.drawCircle(rect.left, rect.top, mHandleSize, mPaintFrame);
        canvas.drawCircle(rect.right, rect.top, mHandleSize, mPaintFrame);
        canvas.drawCircle(rect.left, rect.bottom, mHandleSize, mPaintFrame);
        canvas.drawCircle(rect.right, rect.bottom, mHandleSize, mPaintFrame);
    }

    private void setMatrix() {
        mMatrix.reset();
        mMatrix.setTranslate(mCenter.x - mImgWidth * 0.5f, mCenter.y - mImgHeight * 0.5f);
        mMatrix.postScale(mScale, mScale, mCenter.x, mCenter.y);
        mMatrix.postRotate(mAngle, mCenter.x, mCenter.y);
    }

    // Layout calculation //////////////////////////////////////////////////////////////////////////

    private void setupLayout(int viewW, int viewH) {
        if (viewW == 0 || viewH == 0) return;
        setCenter(new PointF(getPaddingLeft() + viewW * 0.5f, getPaddingTop() + viewH * 0.5f));
        setScale(calcScale(viewW, viewH, mAngle));
        setMatrix();
        mImageRect = calcImageRect(new RectF(0f, 0f, mImgWidth, mImgHeight), mMatrix);
        mFrameRect = calcFrameRect(mImageRect);
        mIsInitialized = true;
        invalidate();
    }

    private float calcScale(int viewW, int viewH, float angle) {
        mImgWidth = getDrawable().getIntrinsicWidth();
        mImgHeight = getDrawable().getIntrinsicHeight();
        if (mImgWidth <= 0) mImgWidth = viewW;
        if (mImgHeight <= 0) mImgHeight = viewH;
        float viewRatio = (float) viewW / (float) viewH;
        float imgRatio = getRotatedWidth(angle) / getRotatedHeight(angle);
        float scale = 1.0f;
        if (imgRatio >= viewRatio) {
            scale = viewW / getRotatedWidth(angle);
        } else if (imgRatio < viewRatio) {
            scale = viewH / getRotatedHeight(angle);
        }
        return scale;
    }

    private RectF calcImageRect(RectF rect, Matrix matrix) {
        RectF applied = new RectF();
        matrix.mapRect(applied, rect);
        return applied;
    }

    private RectF calcFrameRect(RectF imageRect) {
        float frameW = getRatioX(imageRect.width());
        float frameH = getRatioY(imageRect.height());
        float imgRatio = imageRect.width() / imageRect.height();
        float frameRatio = frameW / frameH;
        float l = imageRect.left, t = imageRect.top, r = imageRect.right, b = imageRect.bottom;
        if (frameRatio >= imgRatio) {
            l = imageRect.left;
            r = imageRect.right;
            float hy = (imageRect.top + imageRect.bottom) * 0.5f;
            float hh = (imageRect.width() / frameRatio) * 0.5f;
            t = hy - hh;
            b = hy + hh;
        } else if (frameRatio < imgRatio) {
            t = imageRect.top;
            b = imageRect.bottom;
            float hx = (imageRect.left + imageRect.right) * 0.5f;
            float hw = imageRect.height() * frameRatio * 0.5f;
            l = hx - hw;
            r = hx + hw;
        }
        float w = r - l;
        float h = b - t;
        float cx = l + w / 2;
        float cy = t + h / 2;
        float sw = w * mInitialFrameScale;
        float sh = h * mInitialFrameScale;
        return new RectF(cx - sw / 2, cy - sh / 2, cx + sw / 2, cy + sh / 2);
    }

    // Touch Event /////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsInitialized) return false;
        if (!mIsCropEnabled) return false;
        if (!mIsEnabled) return false;
        if (mIsRotating) return false;
        if (mIsAnimating) return false;
        if (mIsLoading) return false;
        if (mIsCropping) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onDown(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                if (mTouchArea != TouchArea.OUT_OF_BOUNDS) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                onCancel();
                return true;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                onUp(event);
                return true;
        }
        return false;
    }


    private void onDown(MotionEvent e) {
        invalidate();
        mLastX = e.getX();
        mLastY = e.getY();
        checkTouchArea(e.getX(), e.getY());
    }

    private void onMove(MotionEvent e) {
        float diffX = e.getX() - mLastX;
        float diffY = e.getY() - mLastY;
        switch (mTouchArea) {
            case CENTER:
                moveFrame(diffX, diffY);
                break;
            case LEFT_TOP:
                moveHandleLT(diffX, diffY);
                break;
            case RIGHT_TOP:
                moveHandleRT(diffX, diffY);
                break;
            case LEFT_BOTTOM:
                moveHandleLB(diffX, diffY);
                break;
            case RIGHT_BOTTOM:
                moveHandleRB(diffX, diffY);
                break;
            case OUT_OF_BOUNDS:
                break;
        }
        invalidate();
        mLastX = e.getX();
        mLastY = e.getY();
    }

    private void onUp(MotionEvent e) {
        if (mGuideShowMode == ShowMode.SHOW_ON_TOUCH) mShowGuide = false;
        if (mHandleShowMode == ShowMode.SHOW_ON_TOUCH) mShowHandle = false;
        mTouchArea = TouchArea.OUT_OF_BOUNDS;
        invalidate();
    }

    private void onCancel() {
        mTouchArea = TouchArea.OUT_OF_BOUNDS;
        invalidate();
    }

    // Hit test ////////////////////////////////////////////////////////////////////////////////////

    private void checkTouchArea(float x, float y) {
        if (isInsideCornerLeftTop(x, y)) {
            mTouchArea = TouchArea.LEFT_TOP;
            if (mHandleShowMode == ShowMode.SHOW_ON_TOUCH) mShowHandle = true;
            if (mGuideShowMode == ShowMode.SHOW_ON_TOUCH) mShowGuide = true;
            return;
        }
        if (isInsideCornerRightTop(x, y)) {
            mTouchArea = TouchArea.RIGHT_TOP;
            if (mHandleShowMode == ShowMode.SHOW_ON_TOUCH) mShowHandle = true;
            if (mGuideShowMode == ShowMode.SHOW_ON_TOUCH) mShowGuide = true;
            return;
        }
        if (isInsideCornerLeftBottom(x, y)) {
            mTouchArea = TouchArea.LEFT_BOTTOM;
            if (mHandleShowMode == ShowMode.SHOW_ON_TOUCH) mShowHandle = true;
            if (mGuideShowMode == ShowMode.SHOW_ON_TOUCH) mShowGuide = true;
            return;
        }
        if (isInsideCornerRightBottom(x, y)) {
            mTouchArea = TouchArea.RIGHT_BOTTOM;
            if (mHandleShowMode == ShowMode.SHOW_ON_TOUCH) mShowHandle = true;
            if (mGuideShowMode == ShowMode.SHOW_ON_TOUCH) mShowGuide = true;
            return;
        }
        if (isInsideFrame(x, y)) {
            if (mGuideShowMode == ShowMode.SHOW_ON_TOUCH) mShowGuide = true;
            mTouchArea = TouchArea.CENTER;
            return;
        }
        mTouchArea = TouchArea.OUT_OF_BOUNDS;
    }

    private boolean isInsideFrame(float x, float y) {
        if (mFrameRect.left <= x && mFrameRect.right >= x) {
            if (mFrameRect.top <= y && mFrameRect.bottom >= y) {
                mTouchArea = TouchArea.CENTER;
                return true;
            }
        }
        return false;
    }

    private boolean isInsideCornerLeftTop(float x, float y) {
        float dx = x - mFrameRect.left;
        float dy = y - mFrameRect.top;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideCornerRightTop(float x, float y) {
        float dx = x - mFrameRect.right;
        float dy = y - mFrameRect.top;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideCornerLeftBottom(float x, float y) {
        float dx = x - mFrameRect.left;
        float dy = y - mFrameRect.bottom;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    private boolean isInsideCornerRightBottom(float x, float y) {
        float dx = x - mFrameRect.right;
        float dy = y - mFrameRect.bottom;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    // Adjust frame ////////////////////////////////////////////////////////////////////////////////

    private void moveFrame(float x, float y) {
        mFrameRect.left += x;
        mFrameRect.right += x;
        mFrameRect.top += y;
        mFrameRect.bottom += y;
        checkMoveBounds();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private void moveHandleLT(float diffX, float diffY) {
        if (mCropMode == CropMode.FREE) {
            mFrameRect.left += diffX;
            mFrameRect.top += diffY;
            if (isWidthTooSmall()) {
                float offsetX = mMinFrameSize - getFrameW();
                mFrameRect.left -= offsetX;
            }
            if (isHeightTooSmall()) {
                float offsetY = mMinFrameSize - getFrameH();
                mFrameRect.top -= offsetY;
            }
            checkScaleBounds();
        } else {
            float dx = diffX;
            float dy = diffX * getRatioY() / getRatioX();
            mFrameRect.left += dx;
            mFrameRect.top += dy;
            if (isWidthTooSmall()) {
                float offsetX = mMinFrameSize - getFrameW();
                mFrameRect.left -= offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                mFrameRect.top -= offsetY;
            }
            if (isHeightTooSmall()) {
                float offsetY = mMinFrameSize - getFrameH();
                mFrameRect.top -= offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                mFrameRect.left -= offsetX;
            }
            float ox, oy;
            if (!isInsideHorizontal(mFrameRect.left)) {
                ox = mImageRect.left - mFrameRect.left;
                mFrameRect.left += ox;
                oy = ox * getRatioY() / getRatioX();
                mFrameRect.top += oy;
            }
            if (!isInsideVertical(mFrameRect.top)) {
                oy = mImageRect.top - mFrameRect.top;
                mFrameRect.top += oy;
                ox = oy * getRatioX() / getRatioY();
                mFrameRect.left += ox;
            }
        }
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private void moveHandleRT(float diffX, float diffY) {
        if (mCropMode == CropMode.FREE) {
            mFrameRect.right += diffX;
            mFrameRect.top += diffY;
            if (isWidthTooSmall()) {
                float offsetX = mMinFrameSize - getFrameW();
                mFrameRect.right += offsetX;
            }
            if (isHeightTooSmall()) {
                float offsetY = mMinFrameSize - getFrameH();
                mFrameRect.top -= offsetY;
            }
            checkScaleBounds();
        } else {
            float dx = diffX;
            float dy = diffX * getRatioY() / getRatioX();
            mFrameRect.right += dx;
            mFrameRect.top -= dy;
            if (isWidthTooSmall()) {
                float offsetX = mMinFrameSize - getFrameW();
                mFrameRect.right += offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                mFrameRect.top -= offsetY;
            }
            if (isHeightTooSmall()) {
                float offsetY = mMinFrameSize - getFrameH();
                mFrameRect.top -= offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                mFrameRect.right += offsetX;
            }
            float ox, oy;
            if (!isInsideHorizontal(mFrameRect.right)) {
                ox = mFrameRect.right - mImageRect.right;
                mFrameRect.right -= ox;
                oy = ox * getRatioY() / getRatioX();
                mFrameRect.top += oy;
            }
            if (!isInsideVertical(mFrameRect.top)) {
                oy = mImageRect.top - mFrameRect.top;
                mFrameRect.top += oy;
                ox = oy * getRatioX() / getRatioY();
                mFrameRect.right -= ox;
            }
        }
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private void moveHandleLB(float diffX, float diffY) {
        if (mCropMode == CropMode.FREE) {
            mFrameRect.left += diffX;
            mFrameRect.bottom += diffY;
            if (isWidthTooSmall()) {
                float offsetX = mMinFrameSize - getFrameW();
                mFrameRect.left -= offsetX;
            }
            if (isHeightTooSmall()) {
                float offsetY = mMinFrameSize - getFrameH();
                mFrameRect.bottom += offsetY;
            }
            checkScaleBounds();
        } else {
            float dx = diffX;
            float dy = diffX * getRatioY() / getRatioX();
            mFrameRect.left += dx;
            mFrameRect.bottom -= dy;
            if (isWidthTooSmall()) {
                float offsetX = mMinFrameSize - getFrameW();
                mFrameRect.left -= offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                mFrameRect.bottom += offsetY;
            }
            if (isHeightTooSmall()) {
                float offsetY = mMinFrameSize - getFrameH();
                mFrameRect.bottom += offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                mFrameRect.left -= offsetX;
            }
            float ox, oy;
            if (!isInsideHorizontal(mFrameRect.left)) {
                ox = mImageRect.left - mFrameRect.left;
                mFrameRect.left += ox;
                oy = ox * getRatioY() / getRatioX();
                mFrameRect.bottom -= oy;
            }
            if (!isInsideVertical(mFrameRect.bottom)) {
                oy = mFrameRect.bottom - mImageRect.bottom;
                mFrameRect.bottom -= oy;
                ox = oy * getRatioX() / getRatioY();
                mFrameRect.left += ox;
            }
        }
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private void moveHandleRB(float diffX, float diffY) {
        if (mCropMode == CropMode.FREE) {
            mFrameRect.right += diffX;
            mFrameRect.bottom += diffY;
            if (isWidthTooSmall()) {
                float offsetX = mMinFrameSize - getFrameW();
                mFrameRect.right += offsetX;
            }
            if (isHeightTooSmall()) {
                float offsetY = mMinFrameSize - getFrameH();
                mFrameRect.bottom += offsetY;
            }
            checkScaleBounds();
        } else {
            float dx = diffX;
            float dy = diffX * getRatioY() / getRatioX();
            mFrameRect.right += dx;
            mFrameRect.bottom += dy;
            if (isWidthTooSmall()) {
                float offsetX = mMinFrameSize - getFrameW();
                mFrameRect.right += offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                mFrameRect.bottom += offsetY;
            }
            if (isHeightTooSmall()) {
                float offsetY = mMinFrameSize - getFrameH();
                mFrameRect.bottom += offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                mFrameRect.right += offsetX;
            }
            float ox, oy;
            if (!isInsideHorizontal(mFrameRect.right)) {
                ox = mFrameRect.right - mImageRect.right;
                mFrameRect.right -= ox;
                oy = ox * getRatioY() / getRatioX();
                mFrameRect.bottom -= oy;
            }
            if (!isInsideVertical(mFrameRect.bottom)) {
                oy = mFrameRect.bottom - mImageRect.bottom;
                mFrameRect.bottom -= oy;
                ox = oy * getRatioX() / getRatioY();
                mFrameRect.right -= ox;
            }
        }
    }

    // Frame position correction ///////////////////////////////////////////////////////////////////

    private void checkScaleBounds() {
        float lDiff = mFrameRect.left - mImageRect.left;
        float rDiff = mFrameRect.right - mImageRect.right;
        float tDiff = mFrameRect.top - mImageRect.top;
        float bDiff = mFrameRect.bottom - mImageRect.bottom;

        if (lDiff < 0) {
            mFrameRect.left -= lDiff;
        }
        if (rDiff > 0) {
            mFrameRect.right -= rDiff;
        }
        if (tDiff < 0) {
            mFrameRect.top -= tDiff;
        }
        if (bDiff > 0) {
            mFrameRect.bottom -= bDiff;
        }
    }

    private void checkMoveBounds() {
        float diff = mFrameRect.left - mImageRect.left;
        if (diff < 0) {
            mFrameRect.left -= diff;
            mFrameRect.right -= diff;
        }
        diff = mFrameRect.right - mImageRect.right;
        if (diff > 0) {
            mFrameRect.left -= diff;
            mFrameRect.right -= diff;
        }
        diff = mFrameRect.top - mImageRect.top;
        if (diff < 0) {
            mFrameRect.top -= diff;
            mFrameRect.bottom -= diff;
        }
        diff = mFrameRect.bottom - mImageRect.bottom;
        if (diff > 0) {
            mFrameRect.top -= diff;
            mFrameRect.bottom -= diff;
        }
    }

    private boolean isInsideHorizontal(float x) {
        return mImageRect.left <= x && mImageRect.right >= x;
    }

    private boolean isInsideVertical(float y) {
        return mImageRect.top <= y && mImageRect.bottom >= y;
    }

    private boolean isWidthTooSmall() {
        return getFrameW() < mMinFrameSize;
    }

    private boolean isHeightTooSmall() {
        return getFrameH() < mMinFrameSize;
    }

    // Frame aspect ratio correction ///////////////////////////////////////////////////////////////

    private void recalculateFrameRect(int durationMillis) {
        if (mImageRect == null) return;
        if (mIsAnimating) {
            getAnimator().cancelAnimation();
        }
        final RectF currentRect = new RectF(mFrameRect);
        final RectF newRect = calcFrameRect(mImageRect);
        final float diffL = newRect.left - currentRect.left;
        final float diffT = newRect.top - currentRect.top;
        final float diffR = newRect.right - currentRect.right;
        final float diffB = newRect.bottom - currentRect.bottom;
        if (mIsAnimationEnabled) {
            SimpleValueAnimator animator = getAnimator();
            animator.addAnimatorListener(new SimpleValueAnimatorListener() {
                @Override
                public void onAnimationStarted() {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationUpdated(float scale) {
                    mFrameRect = new RectF(currentRect.left + diffL * scale,
                            currentRect.top + diffT * scale,
                            currentRect.right + diffR * scale,
                            currentRect.bottom + diffB * scale);
                    invalidate();
                }

                @Override
                public void onAnimationFinished() {
                    mFrameRect = newRect;
                    invalidate();
                    mIsAnimating = false;
                }
            });
            animator.startAnimation(durationMillis);
        } else {
            mFrameRect = calcFrameRect(mImageRect);
            invalidate();
        }
    }

    private float getRatioX(float w) {
        switch (mCropMode) {
            case FIT_IMAGE:
                return mImageRect.width();
            case FREE:
                return w;
            case RATIO_4_3:
                return 4;
            case RATIO_3_4:
                return 3;
            case RATIO_16_9:
                return 16;
            case RATIO_9_16:
                return 9;
            case SQUARE:
            case CIRCLE:
            case CIRCLE_SQUARE:
                return 1;
            case CUSTOM:
                return mCustomRatio.x;
            default:
                return w;
        }
    }

    private float getRatioY(float h) {
        switch (mCropMode) {
            case FIT_IMAGE:
                return mImageRect.height();
            case FREE:
                return h;
            case RATIO_4_3:
                return 3;
            case RATIO_3_4:
                return 4;
            case RATIO_16_9:
                return 9;
            case RATIO_9_16:
                return 16;
            case SQUARE:
            case CIRCLE:
            case CIRCLE_SQUARE:
                return 1;
            case CUSTOM:
                return mCustomRatio.y;
            default:
                return h;
        }
    }

    private float getRatioX() {
        switch (mCropMode) {
            case FIT_IMAGE:
                return mImageRect.width();
            case RATIO_4_3:
                return 4;
            case RATIO_3_4:
                return 3;
            case RATIO_16_9:
                return 16;
            case RATIO_9_16:
                return 9;
            case SQUARE:
            case CIRCLE:
            case CIRCLE_SQUARE:
                return 1;
            case CUSTOM:
                return mCustomRatio.x;
            default:
                return 1;
        }
    }

    private float getRatioY() {
        switch (mCropMode) {
            case FIT_IMAGE:
                return mImageRect.height();
            case RATIO_4_3:
                return 3;
            case RATIO_3_4:
                return 4;
            case RATIO_16_9:
                return 9;
            case RATIO_9_16:
                return 16;
            case SQUARE:
            case CIRCLE:
            case CIRCLE_SQUARE:
                return 1;
            case CUSTOM:
                return mCustomRatio.y;
            default:
                return 1;
        }
    }

    // Utility /////////////////////////////////////////////////////////////////////////////////////

    private float getDensity() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    private float sq(float value) {
        return value * value;
    }

    private float constrain(float val, float min, float max, float defaultVal) {
        if (val < min || val > max) return defaultVal;
        return val;
    }

    private void postErrorOnMainThread(final Callback callback) {
        if (callback == null) return;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback.onError();
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onError();
                }
            });
        }
    }

    private Bitmap getBitmap() {
        Bitmap bm = null;
        Drawable d = getDrawable();
        if (d != null && d instanceof BitmapDrawable) bm = ((BitmapDrawable) d).getBitmap();
        return bm;
    }

    private float getRotatedWidth(float angle) {
        return getRotatedWidth(angle, mImgWidth, mImgHeight);
    }

    private float getRotatedWidth(float angle, float width, float height) {
        return angle % 180 == 0 ? width : height;
    }

    private float getRotatedHeight(float angle) {
        return getRotatedHeight(angle, mImgWidth, mImgHeight);
    }

    private float getRotatedHeight(float angle, float width, float height) {
        return angle % 180 == 0 ? height : width;
    }

    private Bitmap getRotatedBitmap(Bitmap bitmap) {
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.setRotate(mAngle, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                rotateMatrix, true);
    }

    // Animation ///////////////////////////////////////////////////////////////////////////////////

    private SimpleValueAnimator getAnimator() {
        setupAnimatorIfNeeded();
        return mAnimator;
    }

    private void setupAnimatorIfNeeded() {
        if (mAnimator == null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mAnimator = new ValueAnimatorV8(mInterpolator);
            } else {
                mAnimator = new ValueAnimatorV14(mInterpolator);
            }
        }
    }

    // Cropping ////////////////////////////////////////////////////////////////////////////////////

    private Bitmap decodeRegion() {
        Bitmap cropped = null;
        InputStream is = null;
        try {
            is = getContext().getContentResolver()
                    .openInputStream(mSourceUri);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
            final int originalImageWidth = decoder.getWidth();
            final int originalImageHeight = decoder.getHeight();
            Rect cropRect = calcCropRect(originalImageWidth, originalImageHeight);
            if (mAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(-mAngle);
                RectF rotated = new RectF();
                matrix.mapRect(rotated, new RectF(cropRect));
                rotated.offset(rotated.left < 0 ? originalImageWidth : 0,
                        rotated.top < 0 ? originalImageHeight : 0);
                cropRect = new Rect((int) rotated.left, (int) rotated.top, (int) rotated.right,
                        (int) rotated.bottom);
            }
            cropped = decoder.decodeRegion(cropRect, new BitmapFactory.Options());
            if (mAngle != 0) {
                Bitmap rotated = getRotatedBitmap(cropped);
                if (cropped != getBitmap() && cropped != rotated) {
                    cropped.recycle();
                }
                cropped = rotated;
            }
        } catch (IOException e) {
            Logger.e("An error occurred while cropping the image: " + e.getMessage(), e);
        } catch (OutOfMemoryError e) {
            Logger.e("OOM Error: " + e.getMessage(), e);
        } catch (Exception e) {
            Logger.e("An unexpected error has occurred: " + e.getMessage(), e);
        } finally {
            Utils.closeQuietly(is);
        }
        return cropped;
    }

    private Rect calcCropRect(int originalImageWidth, int originalImageHeight) {
        float scaleToOriginal = getRotatedWidth(mAngle, originalImageWidth,
                originalImageHeight) / mImageRect.width();
        float offsetX = mImageRect.left * scaleToOriginal;
        float offsetY = mImageRect.top * scaleToOriginal;
        int left = Math.round(mFrameRect.left * scaleToOriginal - offsetX);
        int top = Math.round(mFrameRect.top * scaleToOriginal - offsetY);
        int right = Math.round(mFrameRect.right * scaleToOriginal - offsetX);
        int bottom = Math.round(mFrameRect.bottom * scaleToOriginal - offsetY);
        int imageW = Math.round(getRotatedWidth(mAngle, originalImageWidth, originalImageHeight));
        int imageH = Math.round(getRotatedHeight(mAngle, originalImageWidth, originalImageHeight));
        return new Rect(Math.max(left, 0), Math.max(top, 0), Math.min(right, imageW),
                Math.min(bottom, imageH));
    }

    private Bitmap scaleBitmapIfNeeded(Bitmap cropped) {
        int width = cropped.getWidth();
        int height = cropped.getHeight();
        int outWidth = 0;
        int outHeight = 0;
        float imageRatio = getRatioX(mFrameRect.width()) / getRatioY(mFrameRect.height());

        if (mOutputWidth > 0) {
            outWidth = mOutputWidth;
            outHeight = Math.round(mOutputWidth / imageRatio);
        } else if (mOutputHeight > 0) {
            outHeight = mOutputHeight;
            outWidth = Math.round(mOutputHeight * imageRatio);
        } else {
            if (mOutputMaxWidth > 0 && mOutputMaxHeight > 0
                    && (width > mOutputMaxWidth || height > mOutputMaxHeight)) {
                float maxRatio = (float) mOutputMaxWidth / (float) mOutputMaxHeight;
                if (maxRatio >= imageRatio) {
                    outHeight = mOutputMaxHeight;
                    outWidth = Math.round((float) mOutputMaxHeight * imageRatio);
                } else {
                    outWidth = mOutputMaxWidth;
                    outHeight = Math.round((float) mOutputMaxWidth / imageRatio);
                }
            }
        }

        if (outWidth > 0 && outHeight > 0) {
            Bitmap scaled = Utils.getScaledBitmap(cropped, outWidth, outHeight);
            if (cropped != getBitmap() && cropped != scaled) {
                cropped.recycle();
            }
            cropped = scaled;
        }
        return cropped;
    }

    // File save ///////////////////////////////////////////////////////////////////////////////////

    private void saveToFile(Bitmap bitmap, final Uri uri) {
        OutputStream outputStream = null;
        try {
            outputStream = getContext().getContentResolver()
                    .openOutputStream(uri);
            if (outputStream != null) {
                bitmap.compress(mCompressFormat, mCompressQuality, outputStream);
            }
        } catch (IOException e) {
            Logger.e("An error occurred while saving the image: " + uri, e);
            postErrorOnMainThread(mSaveCallback);
        } finally {
            Utils.closeQuietly(outputStream);
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSaveCallback != null) mSaveCallback.onSuccess(uri);
            }
        });
    }

    // Public methods //////////////////////////////////////////////////////////////////////////////

    /**
     * Get source image bitmap
     *
     * @return src bitmap
     */
    public Bitmap getImageBitmap() {
        return getBitmap();
    }

    /**
     * Set source image bitmap
     *
     * @param bitmap src image bitmap
     */
    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap); // calles setImageDrawable internally
    }

    /**
     * Set source image resource id
     *
     * @param resId source image resource id
     */
    @Override
    public void setImageResource(int resId) {
        mIsInitialized = false;
        super.setImageResource(resId);
        updateLayout();
    }

    /**
     * Set image drawable.
     *
     * @param drawable source image drawable
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        mIsInitialized = false;
        super.setImageDrawable(drawable);
        updateLayout();
    }

    /**
     * Set image uri
     *
     * @param uri source image local uri
     */
    @Override
    public void setImageURI(Uri uri) {
        mIsInitialized = false;
        super.setImageURI(uri);
        updateLayout();
    }

    private void updateLayout() {
        resetImageInfo();
        Drawable d = getDrawable();
        if (d != null) {
            setupLayout(mViewWidth, mViewHeight);
        }
    }

    private void resetImageInfo() {
        if (mIsLoading) return;
        mSourceUri = null;
        mSaveUri = null;
        mInputImageWidth = 0;
        mInputImageHeight = 0;
        mOutputImageWidth = 0;
        mOutputImageHeight = 0;
        mAngle = mExifRotation;
    }

    /**
     * Load image from Uri.
     *
     * @param sourceUri Image Uri
     * @param callback  Callback
     */
    public void startLoad(Uri sourceUri, LoadCallback callback) {
        mLoadCallback = callback;
        mSourceUri = sourceUri;
        if (sourceUri == null) {
            postErrorOnMainThread(mLoadCallback);
            throw new IllegalStateException("Source Uri must not be null.");
        }
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                mIsLoading = true;
                mExifRotation = Utils.getExifOrientation(getContext(), mSourceUri);
                int maxSize = Utils.getMaxSize();
                int requestSize = Math.max(mViewWidth, mViewHeight);
                if (requestSize == 0) requestSize = maxSize;
                try {
                    final Bitmap sampledBitmap = Utils.decodeSampledBitmapFromUri(getContext(),
                            mSourceUri,
                            requestSize);
                    mInputImageWidth = Utils.sInputImageWidth;
                    mInputImageHeight = Utils.sInputImageHeight;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAngle = mExifRotation;
                            setImageBitmap(sampledBitmap);
                            if (mLoadCallback != null) mLoadCallback.onSuccess();
                            mIsLoading = false;
                        }
                    });
                } catch (OutOfMemoryError e) {
                    Logger.e("OOM Error: " + e.getMessage(), e);
                    postErrorOnMainThread(mLoadCallback);
                    mIsLoading = false;
                } catch (Exception e) {
                    Logger.e("An unexpected error has occurred: " + e.getMessage(), e);
                    postErrorOnMainThread(mLoadCallback);
                    mIsLoading = false;
                }
            }
        });
    }

    /**
     * Rotate image
     *
     * @param degrees        rotation angle
     * @param durationMillis animation duration in milliseconds
     */
    public void rotateImage(RotateDegrees degrees, int durationMillis) {
        if (mIsRotating) {
            getAnimator().cancelAnimation();
        }
        final float currentAngle = mAngle;
        final float newAngle = (mAngle + degrees.getValue());
        final float angleDiff = newAngle - currentAngle;
        final float currentScale = mScale;
        final float newScale = calcScale(mViewWidth, mViewHeight, newAngle);
        if (mIsAnimationEnabled) {
            final float scaleDiff = newScale - currentScale;
            SimpleValueAnimator animator = getAnimator();
            animator.addAnimatorListener(new SimpleValueAnimatorListener() {
                @Override
                public void onAnimationStarted() {
                    mIsRotating = true;
                }

                @Override
                public void onAnimationUpdated(float scale) {
                    mAngle = currentAngle + angleDiff * scale;
                    mScale = currentScale + scaleDiff * scale;
                    setMatrix();
                    invalidate();
                }

                @Override
                public void onAnimationFinished() {
                    mAngle = newAngle % 360;
                    mScale = newScale;
                    setupLayout(mViewWidth, mViewHeight);
                    mIsRotating = false;
                }
            });
            animator.startAnimation(durationMillis);
        } else {
            mAngle = newAngle % 360;
            mScale = newScale;
            setupLayout(mViewWidth, mViewHeight);
        }
    }

    /**
     * Rotate image
     *
     * @param degrees rotation angle
     */
    public void rotateImage(RotateDegrees degrees) {
        rotateImage(degrees, mAnimationDurationMillis);
    }

    /**
     * Get cropped image bitmap
     *
     * @return cropped image bitmap
     */
    public Bitmap getCroppedBitmap() {
        Bitmap source = getBitmap();
        if (source == null) return null;

        Bitmap rotated = getRotatedBitmap(source);
        Rect cropRect = calcCropRect(source.getWidth(), source.getHeight());
        Bitmap cropped = Bitmap.createBitmap(
                rotated,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height(),
                null,
                false
        );
        if (rotated != cropped && rotated != source) {
            rotated.recycle();
        }

        if (mCropMode == CropMode.CIRCLE) {
            Bitmap circle = getCircularBitmap(cropped);
            if (cropped != getBitmap()) {
                cropped.recycle();
            }
            cropped = circle;
        }
        return cropped;
    }

    /**
     * Crop the square image in a circular
     *
     * @param square image bitmap
     * @return circular image bitmap
     */
    public Bitmap getCircularBitmap(Bitmap square) {
        if (square == null) return null;
        Bitmap output = Bitmap.createBitmap(square.getWidth(), square.getHeight(),
                Bitmap.Config.ARGB_8888);

        final Rect rect = new Rect(0, 0, square.getWidth(), square.getHeight());
        Canvas canvas = new Canvas(output);

        int halfWidth = square.getWidth() / 2;
        int halfHeight = square.getHeight() / 2;

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        canvas.drawCircle(halfWidth, halfHeight, Math.min(halfWidth, halfHeight), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(square, rect, rect, paint);
        return output;
    }

    /**
     * Crop image from Uri
     *
     * @param saveUri      Uri for saving the cropped image
     * @param cropCallback Callback for cropping the image
     * @param saveCallback Callback for saving the image
     */
    public void startCrop(Uri saveUri, CropCallback cropCallback, SaveCallback saveCallback) {
        mSaveUri = saveUri;
        mCropCallback = cropCallback;
        mSaveCallback = saveCallback;
        if (mIsCropping) {
            postErrorOnMainThread(mCropCallback);
            postErrorOnMainThread(mSaveCallback);
            return;
        }
        mIsCropping = true;
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Bitmap cropped;

                // Use thumbnail for crop
                if (mSourceUri == null) {
                    cropped = getCroppedBitmap();
                }
                // Use file for crop
                else {
                    cropped = decodeRegion();
                    if (mCropMode == CropMode.CIRCLE) {
                        Bitmap circle = getCircularBitmap(cropped);
                        if (cropped != getBitmap()) {
                            cropped.recycle();
                        }
                        cropped = circle;
                    }
                }

                // Success
                if (cropped != null) {
                    cropped = scaleBitmapIfNeeded(cropped);
                    final Bitmap tmp = cropped;
                    mOutputImageWidth = tmp.getWidth();
                    mOutputImageHeight = tmp.getHeight();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCropCallback != null) mCropCallback.onSuccess(tmp);
                            if (mIsDebug) invalidate();
                        }
                    });
                }
                // Error
                else {
                    postErrorOnMainThread(mCropCallback);
                }

                if (mSaveUri == null) {
                    postErrorOnMainThread(mSaveCallback);
                    return;
                }
                saveToFile(cropped, mSaveUri);
                mIsCropping = false;
            }
        });
    }

    /**
     * Get frame position relative to the source bitmap.
     *
     * @return crop area boundaries.
     */
    public RectF getActualCropRect() {
        float offsetX = (mImageRect.left / mScale);
        float offsetY = (mImageRect.top / mScale);
        float l = (mFrameRect.left / mScale) - offsetX;
        float t = (mFrameRect.top / mScale) - offsetY;
        float r = (mFrameRect.right / mScale) - offsetX;
        float b = (mFrameRect.bottom / mScale) - offsetY;
        return new RectF(l, t, r, b);
    }

    /**
     * Set crop mode
     *
     * @param mode           crop mode
     * @param durationMillis animation duration in milliseconds
     */
    public void setCropMode(CropMode mode, int durationMillis) {
        if (mode == CropMode.CUSTOM) {
            setCustomRatio(1, 1);
        } else {
            mCropMode = mode;
            recalculateFrameRect(durationMillis);
        }
    }

    /**
     * Set crop mode
     *
     * @param mode crop mode
     */
    public void setCropMode(CropMode mode) {
        setCropMode(mode, mAnimationDurationMillis);
    }

    /**
     * Set custom aspect ratio to crop frame
     *
     * @param ratioX         ratio x
     * @param ratioY         ratio y
     * @param durationMillis animation duration in milliseconds
     */
    public void setCustomRatio(int ratioX, int ratioY, int durationMillis) {
        if (ratioX == 0 || ratioY == 0) return;
        mCropMode = CropMode.CUSTOM;
        mCustomRatio = new PointF(ratioX, ratioY);
        recalculateFrameRect(durationMillis);
    }

    /**
     * Set custom aspect ratio to crop frame
     *
     * @param ratioX ratio x
     * @param ratioY ratio y
     */
    public void setCustomRatio(int ratioX, int ratioY) {
        setCustomRatio(ratioX, ratioY, mAnimationDurationMillis);
    }

    /**
     * Set image overlay color
     *
     * @param overlayColor color resId or color int(ex. 0xFFFFFFFF)
     */
    public void setOverlayColor(int overlayColor) {
        this.mOverlayColor = overlayColor;
        invalidate();
    }

    /**
     * Set crop frame color
     *
     * @param frameColor color resId or color int(ex. 0xFFFFFFFF)
     */
    public void setFrameColor(int frameColor) {
        this.mFrameColor = frameColor;
        invalidate();
    }

    /**
     * Set handle color
     *
     * @param handleColor color resId or color int(ex. 0xFFFFFFFF)
     */
    public void setHandleColor(int handleColor) {
        this.mHandleColor = handleColor;
        invalidate();
    }

    /**
     * Set guide color
     *
     * @param guideColor color resId or color int(ex. 0xFFFFFFFF)
     */
    public void setGuideColor(int guideColor) {
        this.mGuideColor = guideColor;
        invalidate();
    }

    /**
     * Set view background color
     *
     * @param bgColor color resId or color int(ex. 0xFFFFFFFF)
     */
    public void setBackgroundColor(int bgColor) {
        this.mBackgroundColor = bgColor;
        invalidate();
    }

    /**
     * Set crop frame minimum size in density-independent pixels.
     *
     * @param minDp crop frame minimum size in density-independent pixels
     */
    public void setMinFrameSizeInDp(int minDp) {
        mMinFrameSize = minDp * getDensity();
    }

    /**
     * Set crop frame minimum size in pixels.
     *
     * @param minPx crop frame minimum size in pixels
     */
    public void setMinFrameSizeInPx(int minPx) {
        mMinFrameSize = minPx;
    }

    /**
     * Set handle radius in density-independent pixels.
     *
     * @param handleDp handle radius in density-independent pixels
     */
    public void setHandleSizeInDp(int handleDp) {
        mHandleSize = (int) (handleDp * getDensity());
    }

    /**
     * Set crop frame handle touch padding(touch area) in density-independent pixels.
     *
     * handle touch area : a circle of radius R.(R = handle size + touch padding)
     *
     * @param paddingDp crop frame handle touch padding(touch area) in density-independent pixels
     */
    public void setTouchPaddingInDp(int paddingDp) {
        mTouchPadding = (int) (paddingDp * getDensity());
    }

    /**
     * Set guideline show mode.
     * (SHOW_ALWAYS/NOT_SHOW/SHOW_ON_TOUCH)
     *
     * @param mode guideline show mode
     */
    public void setGuideShowMode(ShowMode mode) {
        mGuideShowMode = mode;
        switch (mode) {
            case SHOW_ALWAYS:
                mShowGuide = true;
                break;
            case NOT_SHOW:
            case SHOW_ON_TOUCH:
                mShowGuide = false;
                break;
        }
        invalidate();
    }

    /**
     * Set handle show mode.
     * (SHOW_ALWAYS/NOT_SHOW/SHOW_ON_TOUCH)
     *
     * @param mode handle show mode
     */
    public void setHandleShowMode(ShowMode mode) {
        mHandleShowMode = mode;
        switch (mode) {
            case SHOW_ALWAYS:
                mShowHandle = true;
                break;
            case NOT_SHOW:
            case SHOW_ON_TOUCH:
                mShowHandle = false;
                break;
        }
        invalidate();
    }

    /**
     * Set frame stroke weight in density-independent pixels.
     *
     * @param weightDp frame stroke weight in density-independent pixels.
     */
    public void setFrameStrokeWeightInDp(int weightDp) {
        mFrameStrokeWeight = weightDp * getDensity();
        invalidate();
    }

    /**
     * Set guideline stroke weight in density-independent pixels.
     *
     * @param weightDp guideline stroke weight in density-independent pixels.
     */
    public void setGuideStrokeWeightInDp(int weightDp) {
        mGuideStrokeWeight = weightDp * getDensity();
        invalidate();
    }

    /**
     * Set whether to show crop frame.
     *
     * @param enabled should show crop frame?
     */
    public void setCropEnabled(boolean enabled) {
        mIsCropEnabled = enabled;
        invalidate();
    }

    /**
     * Set locking the crop frame.
     *
     * @param enabled should lock crop frame?
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mIsEnabled = enabled;
    }

    /**
     * Set initial scale of the frame.(0.01 ~ 1.0)
     *
     * @param initialScale initial scale
     */
    public void setInitialFrameScale(float initialScale) {
        mInitialFrameScale = constrain(initialScale, 0.01f, 1.0f, DEFAULT_INITIAL_FRAME_SCALE);
    }

    /**
     * Set whether to animate
     *
     * @param enabled is animation enabled?
     */
    public void setAnimationEnabled(boolean enabled) {
        mIsAnimationEnabled = enabled;
    }

    /**
     * Set duration of animation
     *
     * @param durationMillis animation duration in milliseconds
     */
    public void setAnimationDuration(int durationMillis) {
        mAnimationDurationMillis = durationMillis;
    }

    /**
     * Set interpolator of animation
     * (Default interpolator is DecelerateInterpolator)
     *
     * @param interpolator interpolator used for animation
     */
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
        mAnimator = null;
        setupAnimatorIfNeeded();
    }

    /**
     * Set whether to show debug display
     *
     * @param debug is logging enabled
     */
    public void setDebug(boolean debug) {
        mIsDebug = debug;
        invalidate();
    }

    /**
     * Set whether to log exception
     *
     * @param enabled is logging enabled
     */
    public void setLoggingEnabled(boolean enabled) {
        Logger.enabled = enabled;
    }

    /**
     * Set Image Load callback
     *
     * @param callback callback
     */
    public void setLoadCallback(LoadCallback callback) {
        mLoadCallback = callback;
    }

    /**
     * Set Image Crop callback
     *
     * @param callback callback
     */
    public void setCropCallback(CropCallback callback) {
        mCropCallback = callback;
    }

    /**
     * Set Image Save callback
     *
     * @param callback callback
     */
    public void setSaveCallback(SaveCallback callback) {
        mSaveCallback = callback;
    }

    /**
     * Set fixed width for output
     * (After cropping, the image is scaled to the specified size.)
     *
     * @param outputWidth output width
     */
    public void setOutputWidth(int outputWidth) {
        mOutputWidth = outputWidth;
        mOutputHeight = 0;
    }

    /**
     * Set fixed height for output
     * (After cropping, the image is scaled to the specified size.)
     *
     * @param outputHeight output height
     */
    public void setOutputHeight(int outputHeight) {
        mOutputHeight = outputHeight;
        mOutputWidth = 0;
    }

    /**
     * Set maximum size for output
     * (If cropped image size is larger than max size, the image is scaled to the smaller size.
     * If fixed output width/height has already set, these parameters are ignored.)
     *
     * @param maxWidth  max output width
     * @param maxHeight max output height
     */
    public void setOutputMaxSize(int maxWidth, int maxHeight) {
        mOutputMaxWidth = maxWidth;
        mOutputMaxHeight = maxHeight;
    }

    /**
     * Set compress format for output
     *
     * @param format compress format{@link android.graphics.Bitmap.CompressFormat}
     */
    public void setCompressFormat(Bitmap.CompressFormat format) {
        mCompressFormat = format;
    }

    /**
     * Set compress quality for output
     *
     * @param quality compress quality(0-100: 100 is default.)
     */
    public void setCompressQuality(int quality) {
        mCompressQuality = quality;
    }

    /**
     * Set whether to show handle shadows
     *
     * @param handleShadowEnabled should show handle shadows?
     */
    public void setHandleShadowEnabled(boolean handleShadowEnabled) {
        mIsHandleShadowEnabled = handleShadowEnabled;
    }

    private void setScale(float mScale) {
        this.mScale = mScale;
    }

    private void setCenter(PointF mCenter) {
        this.mCenter = mCenter;
    }

    private float getFrameW() {
        return (mFrameRect.right - mFrameRect.left);
    }

    private float getFrameH() {
        return (mFrameRect.bottom - mFrameRect.top);
    }

    // Enum ////////////////////////////////////////////////////////////////////////////////////////

    private enum TouchArea {
        OUT_OF_BOUNDS, CENTER, LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM
    }

    public enum CropMode {
        FIT_IMAGE(0), RATIO_4_3(1), RATIO_3_4(2), SQUARE(3), RATIO_16_9(4), RATIO_9_16(5), FREE(
                6), CUSTOM(7), CIRCLE(8), CIRCLE_SQUARE(9);
        private final int ID;

        CropMode(final int id) {
            this.ID = id;
        }

        public int getId() {
            return ID;
        }
    }

    public enum ShowMode {
        SHOW_ALWAYS(1), SHOW_ON_TOUCH(2), NOT_SHOW(3);
        private final int ID;

        ShowMode(final int id) {
            this.ID = id;
        }

        public int getId() {
            return ID;
        }
    }

    public enum RotateDegrees {
        ROTATE_90D(90), ROTATE_180D(180), ROTATE_270D(270), ROTATE_M90D(-90), ROTATE_M180D(-180),
        ROTATE_M270D(-270);

        private final int VALUE;

        RotateDegrees(final int value) {
            this.VALUE = value;
        }

        public int getValue() {
            return VALUE;
        }
    }

    // Save/Restore support ////////////////////////////////////////////////////////////////////////

    public static class SavedState extends BaseSavedState {
        Bitmap image;
        CropMode mode;
        int backgroundColor;
        int overlayColor;
        int frameColor;
        ShowMode guideShowMode;
        ShowMode handleShowMode;
        boolean showGuide;
        boolean showHandle;
        int handleSize;
        int touchPadding;
        float minFrameSize;
        float customRatioX;
        float customRatioY;
        float frameStrokeWeight;
        float guideStrokeWeight;
        boolean isCropEnabled;
        int handleColor;
        int guideColor;
        float initialFrameScale;
        float angle;
        boolean isAnimationEnabled;
        int animationDuration;
        int exifRotation;
        Uri sourceUri;
        Uri saveUri;
        Bitmap.CompressFormat compressFormat;
        int compressQuality;
        boolean isDebug;
        int outputMaxWidth;
        int outputMaxHeight;
        int outputWidth;
        int outputHeight;
        boolean isHandleShadowEnabled;
        int inputImageWidth;
        int inputImageHeight;
        int outputImageWidth;
        int outputImageHeight;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            image = in.readParcelable(Bitmap.class.getClassLoader());
            mode = (CropMode) in.readSerializable();
            backgroundColor = in.readInt();
            overlayColor = in.readInt();
            frameColor = in.readInt();
            guideShowMode = (ShowMode) in.readSerializable();
            handleShowMode = (ShowMode) in.readSerializable();
            showGuide = (in.readInt() != 0);
            showHandle = (in.readInt() != 0);
            handleSize = in.readInt();
            touchPadding = in.readInt();
            minFrameSize = in.readFloat();
            customRatioX = in.readFloat();
            customRatioY = in.readFloat();
            frameStrokeWeight = in.readFloat();
            guideStrokeWeight = in.readFloat();
            isCropEnabled = (in.readInt() != 0);
            handleColor = in.readInt();
            guideColor = in.readInt();
            initialFrameScale = in.readFloat();
            angle = in.readFloat();
            isAnimationEnabled = (in.readInt() != 0);
            animationDuration = in.readInt();
            exifRotation = in.readInt();
            sourceUri = in.readParcelable(Uri.class.getClassLoader());
            saveUri = in.readParcelable(Uri.class.getClassLoader());
            compressFormat = (Bitmap.CompressFormat) in.readSerializable();
            compressQuality = in.readInt();
            isDebug = (in.readInt() != 0);
            outputMaxWidth = in.readInt();
            outputMaxHeight = in.readInt();
            outputWidth = in.readInt();
            outputHeight = in.readInt();
            isHandleShadowEnabled = (in.readInt() != 0);
            inputImageWidth = in.readInt();
            inputImageHeight = in.readInt();
            outputImageWidth = in.readInt();
            outputImageHeight = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flag) {
            super.writeToParcel(out, flag);
            out.writeParcelable(image, flag);
            out.writeSerializable(mode);
            out.writeInt(backgroundColor);
            out.writeInt(overlayColor);
            out.writeInt(frameColor);
            out.writeSerializable(guideShowMode);
            out.writeSerializable(handleShowMode);
            out.writeInt(showGuide ? 1 : 0);
            out.writeInt(showHandle ? 1 : 0);
            out.writeInt(handleSize);
            out.writeInt(touchPadding);
            out.writeFloat(minFrameSize);
            out.writeFloat(customRatioX);
            out.writeFloat(customRatioY);
            out.writeFloat(frameStrokeWeight);
            out.writeFloat(guideStrokeWeight);
            out.writeInt(isCropEnabled ? 1 : 0);
            out.writeInt(handleColor);
            out.writeInt(guideColor);
            out.writeFloat(initialFrameScale);
            out.writeFloat(angle);
            out.writeInt(isAnimationEnabled ? 1 : 0);
            out.writeInt(animationDuration);
            out.writeInt(exifRotation);
            out.writeParcelable(sourceUri, flag);
            out.writeParcelable(saveUri, flag);
            out.writeSerializable(compressFormat);
            out.writeInt(compressQuality);
            out.writeInt(isDebug ? 1 : 0);
            out.writeInt(outputMaxWidth);
            out.writeInt(outputMaxHeight);
            out.writeInt(outputWidth);
            out.writeInt(outputHeight);
            out.writeInt(isHandleShadowEnabled ? 1 : 0);
            out.writeInt(inputImageWidth);
            out.writeInt(inputImageHeight);
            out.writeInt(outputImageWidth);
            out.writeInt(outputImageHeight);
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public SavedState createFromParcel(final Parcel inParcel) {
                return new SavedState(inParcel);
            }

            public SavedState[] newArray(final int inSize) {
                return new SavedState[inSize];
            }
        };
    }
}
