package com.isseiaoki.simplecropview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;


public class CropImageView extends ImageView {
    private static final String TAG = CropImageView.class.getSimpleName();

    // Constants ///////////////////////////////////////////////////////////////////////////////////

    private static final int HANDLE_SIZE_IN_DP = 16;
    private static final int MIN_FRAME_SIZE_IN_DP = 50;
    private static final int FRAME_STROKE_WEIGHT_IN_DP = 1;
    private static final int GUIDE_STROKE_WEIGHT_IN_DP = 1;
    private static final float DEFAULT_INITIAL_FRAME_SCALE = 0.75f;

    private final int TRANSPARENT;
    private final int TRANSLUCENT_WHITE = 0xBBFFFFFF;
    private final int WHITE = 0xFFFFFFFF;
    private final int TRANSLUCENT_BLACK = 0xBB000000;

    // Member variables ////////////////////////////////////////////////////////////////////////////

    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private float mScale = 1.0f;
    private float mAngle = 0.0f;
    private float mImgWidth = 0.0f;
    private float mImgHeight = 0.0f;
    private boolean mIsInitialized = false;
    private Matrix mMatrix = null;
    private Paint mPaintTransparent;
    private Paint mPaintFrame;
    private Paint mPaintBitmap;
    private RectF mFrameRect;
    private RectF mImageRect;
    private PointF mCenter = new PointF();
    private float mLastX, mLastY;

    // Instance variables for customizable attributes //////////////////////////////////////////////

    private TouchArea mTouchArea = TouchArea.OUT_OF_BOUNDS;
    private CropMode mCropMode = CropMode.RATIO_1_1;
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
    private float mFrameStrokeWeight = 3.0f;
    private float mGuideStrokeWeight = 3.0f;
    private int mBackgroundColor;
    private int mOverlayColor;
    private int mFrameColor;
    private int mHandleColor;
    private int mGuideColor;
    private float mInitialFrameScale; // 0.01 ~ 1.0, 0.75 is default value

    // Constructor /////////////////////////////////////////////////////////////////////////////////

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TRANSPARENT = getResources().getColor(android.R.color.transparent);

        float mDensity = getDensity();
        mHandleSize = (int) (mDensity * HANDLE_SIZE_IN_DP);
        mMinFrameSize = mDensity * MIN_FRAME_SIZE_IN_DP;
        mFrameStrokeWeight = mDensity * FRAME_STROKE_WEIGHT_IN_DP;
        mGuideStrokeWeight = mDensity * GUIDE_STROKE_WEIGHT_IN_DP;

        mPaintFrame = new Paint();
        mPaintTransparent = new Paint();
        mPaintBitmap = new Paint();
        mPaintBitmap.setFilterBitmap(true);

        mMatrix = new Matrix();
        mScale = 1.0f;
        mBackgroundColor = TRANSPARENT;
        mFrameColor = WHITE;
        mOverlayColor = TRANSLUCENT_BLACK;
        mHandleColor = WHITE;
        mGuideColor = TRANSLUCENT_WHITE;

        // handle Styleable
        handleStyleable(context, attrs, defStyle, mDensity);
    }

    // Lifecycle methods ///////////////////////////////////////////////////////////////////////////

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        Bitmap bm = getBitmap();
        ss.image = bm;
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
        setImageBitmap(ss.image);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(viewWidth, viewHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mViewWidth = r - l - getPaddingLeft() - getPaddingRight();
        mViewHeight = b - t - getPaddingTop() - getPaddingBottom();
        if (getDrawable() != null) initLayout(mViewWidth, mViewHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIsInitialized) {
            setMatrix();
            Matrix localMatrix1 = new Matrix();
            localMatrix1.postConcat(this.mMatrix);
            Bitmap bm = getBitmap();
            if (bm != null) {
                canvas.drawBitmap(bm, localMatrix1, mPaintBitmap);
                // draw edit frame
                drawEditFrame(canvas);
            }
        }
    }

    // Handle styleable ////////////////////////////////////////////////////////////////////////////

    private void handleStyleable(Context context, AttributeSet attrs, int defStyle, float mDensity) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, defStyle, 0);
        Drawable drawable;
        mCropMode = CropMode.RATIO_1_1;
        try {
            drawable = ta.getDrawable(R.styleable.CropImageView_imgSrc);
            if (drawable != null) setImageDrawable(drawable);
            for (CropMode mode : CropMode.values()) {
                if (ta.getInt(R.styleable.CropImageView_cropMode, 3) == mode.getId()) {
                    mCropMode = mode;
                    break;
                }
            }
            mBackgroundColor = ta.getColor(R.styleable.CropImageView_backgroundColor, TRANSPARENT);
            super.setBackgroundColor(mBackgroundColor);
            mOverlayColor = ta.getColor(R.styleable.CropImageView_overlayColor, TRANSLUCENT_BLACK);
            mFrameColor = ta.getColor(R.styleable.CropImageView_frameColor, WHITE);
            mHandleColor = ta.getColor(R.styleable.CropImageView_handleColor, WHITE);
            mGuideColor = ta.getColor(R.styleable.CropImageView_guideColor, TRANSLUCENT_WHITE);
            for (ShowMode mode : ShowMode.values()) {
                if (ta.getInt(R.styleable.CropImageView_guideShowMode, 1) == mode.getId()) {
                    mGuideShowMode = mode;
                    break;
                }
            }

            for (ShowMode mode : ShowMode.values()) {
                if (ta.getInt(R.styleable.CropImageView_handleShowMode, 1) == mode.getId()) {
                    mHandleShowMode = mode;
                    break;
                }
            }
            setGuideShowMode(mGuideShowMode);
            setHandleShowMode(mHandleShowMode);
            mHandleSize = ta.getDimensionPixelSize(R.styleable.CropImageView_handleSize, (int) (HANDLE_SIZE_IN_DP * mDensity));
            mTouchPadding = ta.getDimensionPixelSize(R.styleable.CropImageView_touchPadding, 0);
            mMinFrameSize = ta.getDimensionPixelSize(R.styleable.CropImageView_minFrameSize, (int) (MIN_FRAME_SIZE_IN_DP * mDensity));
            mFrameStrokeWeight = ta.getDimensionPixelSize(R.styleable.CropImageView_frameStrokeWeight, (int) (FRAME_STROKE_WEIGHT_IN_DP * mDensity));
            mGuideStrokeWeight = ta.getDimensionPixelSize(R.styleable.CropImageView_guideStrokeWeight, (int) (GUIDE_STROKE_WEIGHT_IN_DP * mDensity));
            mIsCropEnabled = ta.getBoolean(R.styleable.CropImageView_cropEnabled, true);
            mInitialFrameScale = constrain(ta.getFloat(R.styleable.CropImageView_initialFrameScale, DEFAULT_INITIAL_FRAME_SCALE), 0.01f, 1.0f, DEFAULT_INITIAL_FRAME_SCALE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ta.recycle();
        }
    }

    // Drawing method //////////////////////////////////////////////////////////////////////////////

    private void drawEditFrame(Canvas canvas) {
        if (!mIsCropEnabled) return;

        if (mCropMode == CropMode.CIRCLE) {
            mPaintTransparent.setFilterBitmap(true);
            mPaintTransparent.setColor(mOverlayColor);
            mPaintTransparent.setStyle(Paint.Style.FILL);

            Path path = new Path();
            path.addRect(mImageRect.left, mImageRect.top, mImageRect.right, mImageRect.bottom, Path.Direction.CW);
            path.addCircle((mFrameRect.left + mFrameRect.right) / 2, (mFrameRect.top + mFrameRect.bottom) / 2, (mFrameRect.right - mFrameRect.left) / 2, Path.Direction.CCW);
            canvas.drawPath(path, mPaintTransparent);

        } else {
            mPaintTransparent.setFilterBitmap(true);
            mPaintTransparent.setColor(mOverlayColor);
            mPaintTransparent.setStyle(Paint.Style.FILL);

            canvas.drawRect(mImageRect.left, mImageRect.top, mImageRect.right, mFrameRect.top, mPaintTransparent);
            canvas.drawRect(mImageRect.left, mFrameRect.bottom, mImageRect.right, mImageRect.bottom, mPaintTransparent);
            canvas.drawRect(mImageRect.left, mFrameRect.top, mFrameRect.left, mFrameRect.bottom, mPaintTransparent);
            canvas.drawRect(mFrameRect.right, mFrameRect.top, mImageRect.right, mFrameRect.bottom, mPaintTransparent);
        }

        mPaintFrame.setAntiAlias(true);
        mPaintFrame.setFilterBitmap(true);
        mPaintFrame.setStyle(Paint.Style.STROKE);
        mPaintFrame.setColor(mFrameColor);
        mPaintFrame.setStrokeWidth(mFrameStrokeWeight);

        canvas.drawRect(mFrameRect.left, mFrameRect.top, mFrameRect.right, mFrameRect.bottom, mPaintFrame);

        if (mShowGuide) {
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

        if (mShowHandle) {
            mPaintFrame.setStyle(Paint.Style.FILL);
            mPaintFrame.setColor(mHandleColor);
            canvas.drawCircle(mFrameRect.left, mFrameRect.top, mHandleSize, mPaintFrame);
            canvas.drawCircle(mFrameRect.right, mFrameRect.top, mHandleSize, mPaintFrame);
            canvas.drawCircle(mFrameRect.left, mFrameRect.bottom, mHandleSize, mPaintFrame);
            canvas.drawCircle(mFrameRect.right, mFrameRect.bottom, mHandleSize, mPaintFrame);
        }
    }

    private void setMatrix() {
        mMatrix.reset();
        mMatrix.setTranslate(mCenter.x - mImgWidth * 0.5f, mCenter.y - mImgHeight * 0.5f);
        mMatrix.postScale(mScale, mScale, mCenter.x, mCenter.y);
        mMatrix.postRotate(mAngle, mCenter.x, mCenter.y);
    }

    // Initializer /////////////////////////////////////////////////////////////////////////////////

    private void initLayout(int viewW, int viewH) {
        mImgWidth = getDrawable().getIntrinsicWidth();
        mImgHeight = getDrawable().getIntrinsicHeight();
        if (mImgWidth <= 0) mImgWidth = viewW;
        if (mImgHeight <= 0) mImgHeight = viewH;
        float w = (float) viewW;
        float h = (float) viewH;
        float viewRatio = w / h;
        float imgRatio = mImgWidth / mImgHeight;
        float scale = 1.0f;
        if (imgRatio >= viewRatio) {
            scale = w / mImgWidth;
        } else if (imgRatio < viewRatio) {
            scale = h / mImgHeight;
        }
        setCenter(new PointF(getPaddingLeft() + w * 0.5f, getPaddingTop() + h * 0.5f));
        setScale(scale);
        initCropFrame();
        adjustRatio();
        mIsInitialized = true;
    }

    private void initCropFrame() {
        setMatrix();
        float[] arrayOfFloat = new float[8];
        arrayOfFloat[0] = 0.0f;
        arrayOfFloat[1] = 0.0f;
        arrayOfFloat[2] = 0.0f;
        arrayOfFloat[3] = mImgHeight;
        arrayOfFloat[4] = mImgWidth;
        arrayOfFloat[5] = 0.0f;
        arrayOfFloat[6] = mImgWidth;
        arrayOfFloat[7] = mImgHeight;

        mMatrix.mapPoints(arrayOfFloat);

        float l = arrayOfFloat[0];
        float t = arrayOfFloat[1];
        float r = arrayOfFloat[6];
        float b = arrayOfFloat[7];

        mFrameRect = new RectF(l, t, r, b);
        mImageRect = new RectF(l, t, r, b);
    }

    // Touch Event /////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsInitialized) return false;
        if (!mIsCropEnabled) return false;
        if (!mIsEnabled) return false;
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

    private void moveHandleLT(float diffX, float diffY) {
        if (mCropMode == CropMode.RATIO_FREE) {
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

    private void moveHandleRT(float diffX, float diffY) {
        if (mCropMode == CropMode.RATIO_FREE) {
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

    private void moveHandleLB(float diffX, float diffY) {
        if (mCropMode == CropMode.RATIO_FREE) {
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

    private void moveHandleRB(float diffX, float diffY) {
        if (mCropMode == CropMode.RATIO_FREE) {
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

    private void adjustRatio() {
        if (mImageRect == null) return;
        float imgW = mImageRect.right - mImageRect.left;
        float imgH = mImageRect.bottom - mImageRect.top;
        float frameW = getRatioX(imgW);
        float frameH = getRatioY(imgH);
        float imgRatio = imgW / imgH;
        float frameRatio = frameW / frameH;
        float l = mImageRect.left, t = mImageRect.top, r = mImageRect.right, b = mImageRect.bottom;
        if (frameRatio >= imgRatio) {
            l = mImageRect.left;
            r = mImageRect.right;
            float hy = (mImageRect.top + mImageRect.bottom) * 0.5f;
            float hh = (imgW / frameRatio) * 0.5f;
            t = hy - hh;
            b = hy + hh;
        } else if (frameRatio < imgRatio) {
            t = mImageRect.top;
            b = mImageRect.bottom;
            float hx = (mImageRect.left + mImageRect.right) * 0.5f;
            float hw = imgH * frameRatio * 0.5f;
            l = hx - hw;
            r = hx + hw;
        }
        float w = r - l;
        float h = b - t;
        float cx = l + w / 2;
        float cy = t + h / 2;
        float sw = w * mInitialFrameScale;
        float sh = h * mInitialFrameScale;
        mFrameRect = new RectF(cx - sw / 2, cy - sh / 2, cx + sw / 2, cy + sh / 2);
        invalidate();
    }

    private float getRatioX(float w) {
        switch (mCropMode) {
            case RATIO_FIT_IMAGE:
                return mImgWidth;
            case RATIO_FREE:
                return w;
            case RATIO_4_3:
                return 4.0f;
            case RATIO_3_4:
                return 3.0f;
            case RATIO_16_9:
                return 16.0f;
            case RATIO_9_16:
                return 9.0f;
            case RATIO_1_1:
            case CIRCLE:
                return 1.0f;
            case RATIO_CUSTOM:
                return mCustomRatio.x;
            default:
                return w;
        }
    }

    private float getRatioY(float h) {
        switch (mCropMode) {
            case RATIO_FIT_IMAGE:
                return mImgHeight;
            case RATIO_FREE:
                return h;
            case RATIO_4_3:
                return 3.0f;
            case RATIO_3_4:
                return 4.0f;
            case RATIO_16_9:
                return 9.0f;
            case RATIO_9_16:
                return 16.0f;
            case RATIO_1_1:
            case CIRCLE:
                return 1.0f;
            case RATIO_CUSTOM:
                return mCustomRatio.y;
            default:
                return h;
        }
    }

    private float getRatioX() {
        switch (mCropMode) {
            case RATIO_FIT_IMAGE:
                return mImgWidth;
            case RATIO_4_3:
                return 4.0f;
            case RATIO_3_4:
                return 3.0f;
            case RATIO_16_9:
                return 16.0f;
            case RATIO_9_16:
                return 9.0f;
            case RATIO_1_1:
            case CIRCLE:
                return 1.0f;
            case RATIO_CUSTOM:
                return mCustomRatio.x;
            default:
                return 1.0f;
        }
    }

    private float getRatioY() {
        switch (mCropMode) {
            case RATIO_FIT_IMAGE:
                return mImgHeight;
            case RATIO_4_3:
                return 3.0f;
            case RATIO_3_4:
                return 4.0f;
            case RATIO_16_9:
                return 9.0f;
            case RATIO_9_16:
                return 16.0f;
            case RATIO_1_1:
            case CIRCLE:
                return 1.0f;
            case RATIO_CUSTOM:
                return mCustomRatio.y;
            default:
                return 1.0f;
        }
    }
    // Utility methods /////////////////////////////////////////////////////////////////////////////

    private float getDensity() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    private float sq(float value) {
        return value * value;
    }

    private float constrain(float val, float min, float max, float defaultVal) {
        if (val < min || val > max) return defaultVal;
        return val;
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
        mIsInitialized = false;
        super.setImageBitmap(bitmap);
        updateDrawableInfo();
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
        updateDrawableInfo();
    }

    /**
     * Set image drawable.
     *
     * @param drawable
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        mIsInitialized = false;
        super.setImageDrawable(drawable);
        updateDrawableInfo();
    }

    /**
     * Set image uri
     *
     * @param uri
     */
    @Override
    public void setImageURI(Uri uri) {
        mIsInitialized = false;
        super.setImageURI(uri);
        updateDrawableInfo();
    }

    private void updateDrawableInfo() {
        Drawable d = getDrawable();
        if (d != null) {
            initLayout(mViewWidth, mViewHeight);
        }
    }

    /**
     * Rotate image.
     *
     * @param degrees angle of ration in degrees.
     */
    public void rotateImage(RotateDegrees degrees) {
        Bitmap source = getBitmap();
        if (source == null) return;

        int angle = degrees.getValue();
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        setImageBitmap(rotated);
    }

    /**
     * Get cropped image bitmap
     *
     * @return cropped image bitmap
     */
    public Bitmap getCroppedBitmap() {
        Bitmap source = getBitmap();
        if (source == null) return null;

        int x, y, w, h;
        float l = (mFrameRect.left / mScale);
        float t = (mFrameRect.top / mScale);
        float r = (mFrameRect.right / mScale);
        float b = (mFrameRect.bottom / mScale);
        x = Math.round(l - (mImageRect.left / mScale));
        y = Math.round(t - (mImageRect.top / mScale));
        w = Math.round(r - l);
        h = Math.round(b - t);

        Bitmap cropped = Bitmap.createBitmap(source, x, y, w, h, null, false);
        if (mCropMode != CropMode.CIRCLE) return cropped;
        return getCircularBitmap(cropped);
    }

    /**
     * Get cropped rect image bitmap
     * <p/>
     * This method always returns rect image.
     * (If you need a square image with CropMode.CIRCLE, you can use this method.)
     *
     * @return cropped image bitmap
     */
    public Bitmap getRectBitmap() {
        Bitmap source = getBitmap();
        if (source == null) return null;

        int x, y, w, h;
        float l = (mFrameRect.left / mScale);
        float t = (mFrameRect.top / mScale);
        float r = (mFrameRect.right / mScale);
        float b = (mFrameRect.bottom / mScale);
        x = Math.round(l - (mImageRect.left / mScale));
        y = Math.round(t - (mImageRect.top / mScale));
        w = Math.round(r - l);
        h = Math.round(b - t);

        return Bitmap.createBitmap(source, x, y, w, h, null, false);
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

    private Bitmap getBitmap() {
        Bitmap bm = null;
        Drawable d = getDrawable();
        if (d != null && d instanceof BitmapDrawable) bm = ((BitmapDrawable) d).getBitmap();
        return bm;
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
     * @param mode crop mode
     */
    public void setCropMode(CropMode mode) {
        if (mode == CropMode.RATIO_CUSTOM) {
            setCustomRatio(1, 1);
        } else {
            mCropMode = mode;
            adjustRatio();
        }
    }

    /**
     * Set custom aspect ratio to crop frame
     *
     * @param ratioX aspect ratio X
     * @param ratioY aspect ratio Y
     */
    public void setCustomRatio(int ratioX, int ratioY) {
        if (ratioX == 0 || ratioY == 0) return;
        mCropMode = CropMode.RATIO_CUSTOM;
        mCustomRatio = new PointF(ratioX, ratioY);
        adjustRatio();
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
        super.setBackgroundColor(this.mBackgroundColor);
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
     * <p/>
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
        OUT_OF_BOUNDS, CENTER, LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM;
    }

    public enum CropMode {
        RATIO_FIT_IMAGE(0), RATIO_4_3(1), RATIO_3_4(2), RATIO_1_1(3), RATIO_16_9(4), RATIO_9_16(5), RATIO_FREE(6), RATIO_CUSTOM(7), CIRCLE(8);
        private final int ID;

        private CropMode(final int id) {
            this.ID = id;
        }

        public int getId() {
            return ID;
        }
    }

    public enum ShowMode {
        SHOW_ALWAYS(1), SHOW_ON_TOUCH(2), NOT_SHOW(3);
        private final int ID;

        private ShowMode(final int id) {
            this.ID = id;
        }

        public int getId() {
            return ID;
        }
    }

    public enum RotateDegrees {
        ROTATE_90D(90), ROTATE_180D(180), ROTATE_270D(270);

        private final int VALUE;

        private RotateDegrees(final int value) {
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
