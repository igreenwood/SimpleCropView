package com.isseiaoki.simplecropview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;


public class CropImageView extends ImageView {
    private static final String TAG = CropImageView.class.getSimpleName();

    private static final int HANDLE_SIZE_IN_DP = 24;
    private static final int MIN_FRAME_SIZE_IN_DP = 100;
    private static final int FRAME_STROKE_WEIGHT_IN_DP = 1;
    private static final int GUIDE_STROKE_WEIGHT_IN_DP = 3;

    private final int TRANSPARENT;
    private final int TRANSLUCENT_WHITE = 0x66FFFFFF;
    private boolean mIsLayoutInitialized = false;
    private float mMinFrameSize;
    private int mHandleSize;
    private int mTouchPadding = 0;

    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private Bitmap mBitmap = null;
    private TouchArea mTouchArea = TouchArea.OUT_OF_BOUNDS;
    private CropMode mCropMode = CropMode.RATIO_1_1;
    private ShowMode mGuideShowMode = ShowMode.SHOW_ALWAYS;
    private ShowMode mHandleShowMode = ShowMode.SHOW_ALWAYS;
    private boolean mShowGuide = true;
    private boolean mShowHandle = true;
    private boolean mIsCropEnabled = true;
    private PointF mCustomRatio = new PointF(1.0f, 1.0f);

    // 拡縮倍率
    private float mScale = 1.0f;
    // 角度
    private float mAngle = 0.0f;

    // 画像の横幅
    private float mImgWidth = 0.0f;
    // 画像の縦幅
    private float mImgHeight = 0.0f;
    // 初期化済みか
    private boolean mIsInitialized = false;

    private float mFrameStrokeWeight = 6.0f;
    private float mGuideStrokeWeight = 3.0f;

    // 背景色
    private int mBackgroundColor;
    // フレーム選択範囲外の色
    private int mOverlayColor;
    // フレームの色
    private int mFrameColor;
    private float mLastX, mLastY;
    // 描画用の変数
    private Matrix mMatrix = null;
    private Paint mPaintTransparent;
    private Paint mPaintFrame;
    private Paint mPaintBitmap;
    private Paint mPaintText;
    private Paint mPaintCache;
    private RectF mFrameRect;
    private RectF mImageRect;
    private PointF mCenter = new PointF();

    /**
     * Constructor
     */

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
        mPaintText = new Paint();
        mPaintBitmap = new Paint();
        mPaintBitmap.setFilterBitmap(true);
        mPaintCache = new Paint();
        mPaintCache.setFilterBitmap(true);

        mMatrix = new Matrix();
        mScale = 1.0f;
        mBackgroundColor = getResources().getColor(android.R.color.transparent);
        mFrameColor = TRANSLUCENT_WHITE;
        mOverlayColor = TRANSLUCENT_WHITE;

        // handle Styleable
        handleStyleable(context, attrs, defStyle, mDensity);
    }

    /**
     * Lifecycle Methods
     */

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.image = this.mBitmap;
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
        if (mBitmap != null) initLayout(mViewWidth, mViewHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(mBackgroundColor);

        if (mIsInitialized){
            setMatrix();
            Matrix localMatrix1 = new Matrix();
            localMatrix1.postConcat(this.mMatrix);

            canvas.drawBitmap(mBitmap, localMatrix1, mPaintBitmap);

            // draw edit frame
            drawEditFrame(canvas);
        }
    }

    /**
     * Handle Styleable
     */

    private void handleStyleable(Context context, AttributeSet attrs, int defStyle, float mDensity) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, defStyle, 0);
        Drawable drawable;
        mCropMode = CropMode.RATIO_1_1;
        try {
            drawable = ta.getDrawable(R.styleable.CropImageView_imgSrc);
            if (drawable != null) setImageBitmap(((BitmapDrawable) drawable).getBitmap());
            for (CropMode mode : CropMode.values()) {
                if (ta.getInt(R.styleable.CropImageView_cropMode, 3) == mode.getId()) {
                    mCropMode = mode;
                    break;
                }
            }
            mBackgroundColor = ta.getColor(R.styleable.CropImageView_backgroundColor, mBackgroundColor);
            mOverlayColor = ta.getColor(R.styleable.CropImageView_overlayColor, TRANSLUCENT_WHITE);
            mFrameColor = ta.getColor(R.styleable.CropImageView_frameColor, TRANSLUCENT_WHITE);
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ta.recycle();
        }
    }

    /**
     * Drawing Method
     */

    public void drawEditFrame(Canvas canvas) {
        if(!mIsCropEnabled)return;
        mPaintTransparent.setFilterBitmap(true);
        mPaintTransparent.setColor(mOverlayColor);
        mPaintTransparent.setStyle(Paint.Style.FILL);

        canvas.drawRect(mImageRect.left, mImageRect.top, mImageRect.right, mFrameRect.top, mPaintTransparent);
        canvas.drawRect(mImageRect.left, mFrameRect.bottom, mImageRect.right, mImageRect.bottom, mPaintTransparent);
        canvas.drawRect(mImageRect.left, mFrameRect.top, mFrameRect.left, mFrameRect.bottom, mPaintTransparent);
        canvas.drawRect(mFrameRect.right, mFrameRect.top, mImageRect.right, mFrameRect.bottom, mPaintTransparent);

        mPaintFrame.setAntiAlias(true);
        mPaintFrame.setFilterBitmap(true);
        mPaintFrame.setStyle(Paint.Style.STROKE);
        mPaintFrame.setColor(mFrameColor);
        mPaintFrame.setStrokeWidth(mFrameStrokeWeight);

        canvas.drawLine(mFrameRect.left, mFrameRect.top, mFrameRect.left, mFrameRect.bottom, mPaintFrame);
        canvas.drawLine(mFrameRect.left, mFrameRect.top, mFrameRect.right, mFrameRect.top, mPaintFrame);
        canvas.drawLine(mFrameRect.right, mFrameRect.top, mFrameRect.right, mFrameRect.bottom, mPaintFrame);
        canvas.drawLine(mFrameRect.left, mFrameRect.bottom, mFrameRect.right, mFrameRect.bottom, mPaintFrame);


        if (mShowGuide) {
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

            canvas.drawCircle(mFrameRect.left, mFrameRect.top, mHandleSize, mPaintFrame);
            canvas.drawCircle(mFrameRect.right, mFrameRect.top, mHandleSize, mPaintFrame);
            canvas.drawCircle(mFrameRect.left, mFrameRect.bottom, mHandleSize, mPaintFrame);
            canvas.drawCircle(mFrameRect.right, mFrameRect.bottom, mHandleSize, mPaintFrame);
        }
    }

    public void setMatrix() {
        mMatrix.reset();
        mMatrix.setTranslate(mCenter.x - mImgWidth * 0.5f, mCenter.y - mImgHeight * 0.5f);
        mMatrix.postScale(mScale, mScale, mCenter.x, mCenter.y);
        mMatrix.postRotate(mAngle, mCenter.x, mCenter.y);
    }

    /**
     * Initializer
     */

    public void initLayout(int viewW, int viewH) {
        if (mIsLayoutInitialized) return;
        float imgW = mBitmap.getWidth();
        float imgH = mBitmap.getHeight();
        mImgWidth = imgW;
        mImgHeight = imgH;
        float w = (float) viewW;
        float h = (float) viewH;
        float viewRatio = w / h;
        float imgRatio = imgW / imgH;
        float scale = 1.0f;
        if (imgRatio >= viewRatio) {
            scale = w / imgW;
        } else if (imgRatio < viewRatio) {
            scale = h / imgH;
        }
        setCenter(new PointF(getPaddingLeft() + w* 0.5f, getPaddingTop() + h * 0.5f));
        setScale(scale);
        initCropFrame();
        adjustRatio();
        mIsInitialized = true;
    }

    public void initCropFrame() {
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

    /**
     * Handle Touch Events
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!mIsInitialized)return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onDown(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                return true;
            case MotionEvent.ACTION_CANCEL:
                onCancel();
                return false;
            case MotionEvent.ACTION_UP:
                onUp(event);
                return false;
        }
        return true;
    }

    public void onDown(MotionEvent e) {
        invalidate();
        mLastX = e.getX();
        mLastY = e.getY();
        checkTouchArea(e.getX(), e.getY());
    }

    public void onMove(MotionEvent e) {
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

    public void onUp(MotionEvent e) {
        if (mGuideShowMode == ShowMode.SHOW_ON_TOUCH) mShowGuide = false;
        if (mHandleShowMode == ShowMode.SHOW_ON_TOUCH) mShowHandle = false;
        mTouchArea = TouchArea.OUT_OF_BOUNDS;
        invalidate();
    }

    public void onCancel() {
        mTouchArea = TouchArea.OUT_OF_BOUNDS;
        invalidate();
    }

    /**
     * Hit Test Methods
     */

    public void checkTouchArea(float x, float y) {
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

    public boolean isInsideFrame(float x, float y) {
        if (mFrameRect.left <= x && mFrameRect.right >= x) {
            if (mFrameRect.top <= y && mFrameRect.bottom >= y) {
                mTouchArea = TouchArea.CENTER;
                return true;
            }
        }
        return false;
    }

    public boolean isInsideCornerLeftTop(float x, float y) {
        float dx = x - mFrameRect.left;
        float dy = y - mFrameRect.top;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    public boolean isInsideCornerRightTop(float x, float y) {
        float dx = x - mFrameRect.right;
        float dy = y - mFrameRect.top;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    public boolean isInsideCornerLeftBottom(float x, float y) {
        float dx = x - mFrameRect.left;
        float dy = y - mFrameRect.bottom;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    public boolean isInsideCornerRightBottom(float x, float y) {
        float dx = x - mFrameRect.right;
        float dy = y - mFrameRect.bottom;
        float d = dx * dx + dy * dy;
        return sq(mHandleSize + mTouchPadding) >= d;
    }

    /**
     * Adjust Frame
     */

    public void moveFrame(float x, float y) {
        mFrameRect.left += x;
        mFrameRect.right += x;
        mFrameRect.top += y;
        mFrameRect.bottom += y;
        checkMoveBounds();
    }

    public void moveHandleLT(float diffX, float diffY) {
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

    public void moveHandleRT(float diffX, float diffY) {
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

    public void moveHandleLB(float diffX, float diffY) {
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

    public void moveHandleRB(float diffX, float diffY) {
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

    /**
     * Frame Position Correction
     */

    public void checkScaleBounds() {
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

    public void checkMoveBounds() {
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

    public boolean isInsideHorizontal(float x) {
        return mImageRect.left <= x && mImageRect.right >= x;
    }

    public boolean isInsideVertical(float y) {
        return mImageRect.top <= y && mImageRect.bottom >= y;
    }

    public boolean isWidthTooSmall() {
        return getFrameW() < mMinFrameSize;
    }

    public boolean isHeightTooSmall() {
        return getFrameH() < mMinFrameSize;
    }

    /**
     * Frame Ratio Correction
     */

    public void adjustRatio() {
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
        mFrameRect = new RectF(l, t, r, b);
        invalidate();
    }

    public float getRatioX(float w) {
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
                return 1.0f;
            case RATIO_CUSTOM:
                return mCustomRatio.x;
            default:
                return w;
        }
    }

    public float getRatioY(float h) {
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
                return 1.0f;
            case RATIO_CUSTOM:
                return mCustomRatio.y;
            default:
                return h;
        }
    }

    public float getRatioX() {
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
                return 1.0f;
            case RATIO_CUSTOM:
                return mCustomRatio.x;
            default:
                return 1.0f;
        }
    }

    public float getRatioY() {
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
                return 1.0f;
            case RATIO_CUSTOM:
                return mCustomRatio.y;
            default:
                return 1.0f;
        }
    }


    /**
     * Get cropped image
     *
     * @return Cropped Bitmap
     */
    public Bitmap getCroppedBitmap() {
        int x = 0, y = 0, w = 0, h = 0;
        if (mBitmap != null) {
            int l = (int) (mFrameRect.left / mScale);
            int t = (int) (mFrameRect.top / mScale);
            int r = (int) (mFrameRect.right / mScale);
            int b = (int) (mFrameRect.bottom / mScale);
            x = l - (int) (mImageRect.left / mScale);
            y = t - (int) (mImageRect.top / mScale);
            w = r - l;
            h = b - t;
        }
        return Bitmap.createBitmap(mBitmap, x, y, w, h, null, false);
    }

    /**
     * Utility Methods
     */

    public float getDensity() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    public float sq(float value) {
        return value * value;
    }

    /**
     * Getters and Setters
     */

    public Bitmap getImageBitmap() {
        return mBitmap;
    }

    @Override
    public void setImageBitmap(Bitmap bitmap){
        if (this.mBitmap != null && this.mBitmap != bitmap) {
            this.mBitmap.recycle();
            this.mBitmap = null;
        }
        this.mBitmap = bitmap;
        if (mBitmap != null) {
            mImgWidth = mBitmap.getWidth();
            mImgHeight = mBitmap.getHeight();
            initLayout(mViewWidth, mViewHeight);
        } else {
            mIsInitialized = false;
        }
    }

    @Override
    public void setImageResource(int resId){
        if (resId != 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
            setImageBitmap(bitmap);
        }
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float mScale) {
        this.mScale = mScale;
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float mAngle) {
        this.mAngle = mAngle;
    }

    public PointF getCenter() {
        return mCenter;
    }

    public void setCenter(PointF mCenter) {
        this.mCenter = mCenter;
    }

    public float getImgWidth() {
        return mImgWidth;
    }

    public void setImgWidth(float mImgWidth) {
        this.mImgWidth = mImgWidth;
    }

    public float getFrameW() {
        return (mFrameRect.right - mFrameRect.left);
    }

    public float getFrameH() {
        return (mFrameRect.bottom - mFrameRect.top);
    }

    public float getFrameCenterX() {
        return (mFrameRect.left + mFrameRect.right) * 0.5f;
    }

    public float getFrameCenterY() {
        return (mFrameRect.top + mFrameRect.bottom) * 0.5f;
    }

    public float getScaledImgW() {
        return mImageRect.right - mImageRect.left;
    }

    public float getScaledImgH() {
        return mImageRect.bottom - mImageRect.top;
    }

    public void setCropMode(CropMode mode) {
        if(mode == CropMode.RATIO_CUSTOM){
            setCustomRatio(1, 1);
        }else{
            mCropMode = mode;
            adjustRatio();
        }
    }

    public void setCustomRatio(int ratioX, int ratioY) {
        if (ratioX == 0 || ratioY == 0) return;
        mCropMode = CropMode.RATIO_CUSTOM;
        mCustomRatio = new PointF(ratioX, ratioY);
        adjustRatio();
    }

    public void setOverlayColor(int overlayColor) {
        this.mOverlayColor = overlayColor;
        invalidate();
    }

    public void setFrameColor(int frameColor) {
        this.mFrameColor = frameColor;
        invalidate();
    }

    public void setBackgroundColor(int bgColor) {
        this.mBackgroundColor = bgColor;
        invalidate();
    }

    public void setMinFrameSizeInDp(int minDp) {
        mMinFrameSize = minDp * getDensity();
    }

    public void setTouchPaddingInDp(int paddingDp) {
        mTouchPadding = (int) (paddingDp * getDensity());
    }

    public void setHandleSizeInDp(int handleDp) {
        mHandleSize = (int) (handleDp * getDensity());
    }

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

    public void setFrameStrokeWeightInDp(int weightDp) {
        mFrameStrokeWeight = weightDp * getDensity();
        invalidate();
    }

    public void setGuideStrokeWeightInDp(int weightDp) {
        mGuideStrokeWeight = weightDp * getDensity();
        invalidate();
    }

    public void setCropEnabled(boolean enabled){
        mIsCropEnabled = enabled;
        invalidate();
    }

    /**
     * Enum
     */

    private enum TouchArea {
        OUT_OF_BOUNDS, CENTER, LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM;
    }

    public enum CropMode {
        RATIO_FIT_IMAGE(0), RATIO_4_3(1), RATIO_3_4(2), RATIO_1_1(3), RATIO_16_9(4), RATIO_9_16(5), RATIO_FREE(6), RATIO_CUSTOM(7);
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

    /**
     * Save/Restore State Support
     */

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
        }
    }
}
