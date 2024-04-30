//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.warkiz.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Build.VERSION;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.warkiz.widget.R.styleable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;

public class IndicatorSeekBar extends View {
    private static final int THUMB_MAX_WIDTH = 30;
    private static final String FORMAT_PROGRESS = "${PROGRESS}";
    private static final String FORMAT_TICK_TEXT = "${TICK_TEXT}";
    private Context mContext;
    private Paint mStockPaint;
    private TextPaint mTextPaint;
    private OnSeekChangeListener mSeekChangeListener;
    private Rect mRect;
    private float mCustomDrawableMaxHeight;
    private float lastProgress;
    private float mFaultTolerance;
    private float mScreenWidth;
    private boolean mClearPadding;
    private SeekParams mSeekParams;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mMeasuredWidth;
    private int mPaddingTop;
    private float mSeekLength;
    private float mSeekBlockLength;
    private boolean mIsTouching;
    private float mMax;
    private float mMin;
    private float mProgress;
    private boolean mIsFloatProgress;
    private int mScale;
    private boolean mUserSeekable;
    private boolean mOnlyThumbDraggable;
    private boolean mSeekSmoothly;
    private float[] mProgressArr;
    private boolean mR2L;
    private boolean mShowTickText;
    private boolean mShowBothTickTextsOnly;
    private int mTickTextsHeight;
    private String[] mTickTextsArr;
    private float[] mTickTextsWidth;
    private float[] mTextCenterX;
    private float mTickTextY;
    private int mTickTextsSize;
    private Typeface mTextsTypeface;
    private int mSelectedTextsColor;
    private int mUnselectedTextsColor;
    private int mHoveredTextColor;
    private CharSequence[] mTickTextsCustomArray;
    private Indicator mIndicator;
    private int mIndicatorColor;
    private int mIndicatorTextColor;
    private boolean mIndicatorStayAlways;
    private int mIndicatorTextSize;
    private View mIndicatorContentView;
    private View mIndicatorTopContentView;
    private int mShowIndicatorType;
    private String mIndicatorTextFormat;
    private float[] mTickMarksX;
    private int mTicksCount;
    private int mUnSelectedTickMarksColor;
    private int mSelectedTickMarksColor;
    private float mTickRadius;
    private Bitmap mUnselectTickMarksBitmap;
    private Bitmap mSelectTickMarksBitmap;
    private Drawable mTickMarksDrawable;
    private int mShowTickMarksType;
    private boolean mTickMarksEndsHide;
    private boolean mTickMarksSweptHide;
    private int mTickMarksSize;
    private boolean mTrackRoundedCorners;
    private RectF mProgressTrack;
    private RectF mBackgroundTrack;
    private int mBackgroundTrackSize;
    private int mProgressTrackSize;
    private int mBackgroundTrackColor;
    private int mProgressTrackColor;
    private int[] mSectionTrackColorArray;
    private boolean mCustomTrackSectionColorResult;
    private float mThumbRadius;
    private float mThumbTouchRadius;
    private Bitmap mThumbBitmap;
    private int mThumbColor;
    private int mThumbSize;
    private Drawable mThumbDrawable;
    private Bitmap mPressedThumbBitmap;
    private int mPressedThumbColor;
    private boolean mShowThumbText;
    private float mThumbTextY;
    private int mThumbTextColor;
    private boolean mHideThumb;
    private boolean mAdjustAuto;
    private int[] mColorSeeds;

    public IndicatorSeekBar(Context context) {
        this(context, (AttributeSet)null);
    }

    public IndicatorSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFaultTolerance = -1.0F;
        this.mScreenWidth = -1.0F;
        this.mScale = 1;
        this.mColorSeeds = new int[0];
        this.mContext = context;
        this.initAttrs(this.mContext, attrs);
        this.initParams();
    }

    @RequiresApi(
            api = 21
    )
    public IndicatorSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mFaultTolerance = -1.0F;
        this.mScreenWidth = -1.0F;
        this.mScale = 1;
        this.mColorSeeds = new int[0];
        this.mContext = context;
        this.initAttrs(this.mContext, attrs);
        this.initParams();
    }

    IndicatorSeekBar(Builder builder) {
        super(builder.context);
        this.mFaultTolerance = -1.0F;
        this.mScreenWidth = -1.0F;
        this.mScale = 1;
        this.mColorSeeds = new int[0];
        this.mContext = builder.context;
        int defaultPadding = SizeUtils.dp2px(this.mContext, 16.0F);
        this.setPadding(defaultPadding, this.getPaddingTop(), defaultPadding, this.getPaddingBottom());
        this.apply(builder);
        this.initParams();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        Builder builder = new Builder(context);
        if (attrs == null) {
            this.apply(builder);
        } else {
            TypedArray ta = context.obtainStyledAttributes(attrs, styleable.IndicatorSeekBar);
            int colorsId = ta.getResourceId(styleable.IndicatorSeekBar_colorSeekBarColorSeeds, 0);
            this.mMax = ta.getFloat(styleable.IndicatorSeekBar_isb_max, builder.max);
            this.mMin = ta.getFloat(styleable.IndicatorSeekBar_isb_min, builder.min);
            this.mProgress = ta.getFloat(styleable.IndicatorSeekBar_isb_progress, builder.progress);
            this.mIsFloatProgress = ta.getBoolean(styleable.IndicatorSeekBar_isb_progress_value_float, builder.progressValueFloat);
            this.mUserSeekable = ta.getBoolean(styleable.IndicatorSeekBar_isb_user_seekable, builder.userSeekable);
            this.mClearPadding = ta.getBoolean(styleable.IndicatorSeekBar_isb_clear_default_padding, builder.clearPadding);
            this.mOnlyThumbDraggable = ta.getBoolean(styleable.IndicatorSeekBar_isb_only_thumb_draggable, builder.onlyThumbDraggable);
            this.mSeekSmoothly = ta.getBoolean(styleable.IndicatorSeekBar_isb_seek_smoothly, builder.seekSmoothly);
            this.mR2L = ta.getBoolean(styleable.IndicatorSeekBar_isb_r2l, builder.r2l);
            this.mBackgroundTrackSize = ta.getDimensionPixelSize(styleable.IndicatorSeekBar_isb_track_background_size, builder.trackBackgroundSize);
            this.mProgressTrackSize = ta.getDimensionPixelSize(styleable.IndicatorSeekBar_isb_track_progress_size, builder.trackProgressSize);
            this.mBackgroundTrackColor = ta.getColor(styleable.IndicatorSeekBar_isb_track_background_color, builder.trackBackgroundColor);
            this.mProgressTrackColor = ta.getColor(styleable.IndicatorSeekBar_isb_track_progress_color, builder.trackProgressColor);
            this.mTrackRoundedCorners = ta.getBoolean(styleable.IndicatorSeekBar_isb_track_rounded_corners, builder.trackRoundedCorners);
            this.mThumbSize = ta.getDimensionPixelSize(styleable.IndicatorSeekBar_isb_thumb_size, builder.thumbSize);
            this.mThumbDrawable = ta.getDrawable(styleable.IndicatorSeekBar_isb_thumb_drawable);
            this.mAdjustAuto = ta.getBoolean(styleable.IndicatorSeekBar_isb_thumb_adjust_auto, true);
            this.initThumbColor(ta.getColorStateList(styleable.IndicatorSeekBar_isb_thumb_color), builder.thumbColor);
            this.mShowThumbText = ta.getBoolean(styleable.IndicatorSeekBar_isb_show_thumb_text, builder.showThumbText);
            this.mThumbTextColor = ta.getColor(styleable.IndicatorSeekBar_isb_thumb_text_color, builder.thumbTextColor);
            this.mTicksCount = ta.getInt(styleable.IndicatorSeekBar_isb_ticks_count, builder.tickCount);
            this.mShowTickMarksType = ta.getInt(styleable.IndicatorSeekBar_isb_show_tick_marks_type, builder.showTickMarksType);
            this.mTickMarksSize = ta.getDimensionPixelSize(styleable.IndicatorSeekBar_isb_tick_marks_size, builder.tickMarksSize);
            this.initTickMarksColor(ta.getColorStateList(styleable.IndicatorSeekBar_isb_tick_marks_color), builder.tickMarksColor);
            this.mTickMarksDrawable = ta.getDrawable(styleable.IndicatorSeekBar_isb_tick_marks_drawable);
            this.mTickMarksSweptHide = ta.getBoolean(styleable.IndicatorSeekBar_isb_tick_marks_swept_hide, builder.tickMarksSweptHide);
            this.mTickMarksEndsHide = ta.getBoolean(styleable.IndicatorSeekBar_isb_tick_marks_ends_hide, builder.tickMarksEndsHide);
            this.mShowTickText = ta.getBoolean(styleable.IndicatorSeekBar_isb_show_tick_texts, builder.showTickText);
            this.mTickTextsSize = ta.getDimensionPixelSize(styleable.IndicatorSeekBar_isb_tick_texts_size, builder.tickTextsSize);
            this.initTickTextsColor(ta.getColorStateList(styleable.IndicatorSeekBar_isb_tick_texts_color), builder.tickTextsColor);
            this.mTickTextsCustomArray = ta.getTextArray(styleable.IndicatorSeekBar_isb_tick_texts_array);
            this.initTextsTypeface(ta.getInt(styleable.IndicatorSeekBar_isb_tick_texts_typeface, -1), builder.tickTextsTypeFace);
            this.mShowIndicatorType = ta.getInt(styleable.IndicatorSeekBar_isb_show_indicator, builder.showIndicatorType);
            this.mIndicatorColor = ta.getColor(styleable.IndicatorSeekBar_isb_indicator_color, builder.indicatorColor);
            this.mIndicatorTextSize = ta.getDimensionPixelSize(styleable.IndicatorSeekBar_isb_indicator_text_size, builder.indicatorTextSize);
            this.mIndicatorTextColor = ta.getColor(styleable.IndicatorSeekBar_isb_indicator_text_color, builder.indicatorTextColor);
            int indicatorContentViewId = ta.getResourceId(styleable.IndicatorSeekBar_isb_indicator_content_layout, 0);
            if (indicatorContentViewId > 0) {
                this.mIndicatorContentView = View.inflate(this.mContext, indicatorContentViewId, (ViewGroup)null);
            }

            int indicatorTopContentLayoutId = ta.getResourceId(styleable.IndicatorSeekBar_isb_indicator_top_content_layout, 0);
            if (indicatorTopContentLayoutId > 0) {
                this.mIndicatorTopContentView = View.inflate(this.mContext, indicatorTopContentLayoutId, (ViewGroup)null);
            }

            if (colorsId != 0) {
                this.mColorSeeds = this.getColorsById(colorsId);
            }

            ta.recycle();
        }
    }

    private int[] getColorsById(@ArrayRes int id) {
        int[] colors;
        int j;
        if (this.isInEditMode()) {
            String[] s = this.mContext.getResources().getStringArray(id);
            colors = new int[s.length];

            for(j = 0; j < s.length; ++j) {
                colors[j] = Color.parseColor(s[j]);
            }

            return colors;
        } else {
            TypedArray typedArray = this.mContext.getResources().obtainTypedArray(id);
            colors = new int[typedArray.length()];

            for(j = 0; j < typedArray.length(); ++j) {
                colors[j] = typedArray.getColor(j, -16777216);
            }

            typedArray.recycle();
            return colors;
        }
    }

    private void initParams() {
        this.initProgressRangeValue();
        if (this.mBackgroundTrackSize > this.mProgressTrackSize) {
            this.mBackgroundTrackSize = this.mProgressTrackSize;
        }

        if (this.mThumbDrawable == null) {
            this.mThumbRadius = (float)this.mThumbSize / 2.0F;
            this.mThumbTouchRadius = this.mThumbRadius * 1.2F;
        } else {
            this.mThumbRadius = (float)Math.min(SizeUtils.dp2px(this.mContext, 30.0F), this.mThumbSize) / 2.0F;
            this.mThumbTouchRadius = this.mThumbRadius;
        }

        if (this.mTickMarksDrawable == null) {
            this.mTickRadius = (float)this.mTickMarksSize / 2.0F;
        } else {
            this.mTickRadius = (float)Math.min(SizeUtils.dp2px(this.mContext, 30.0F), this.mTickMarksSize) / 2.0F;
        }

        this.mCustomDrawableMaxHeight = Math.max(this.mThumbTouchRadius, this.mTickRadius) * 2.0F;
        this.initStrokePaint();
        this.measureTickTextsBonds();
        this.lastProgress = this.mProgress;
        this.collectTicksInfo();
        this.mProgressTrack = new RectF();
        this.mBackgroundTrack = new RectF();
        this.initDefaultPadding();
        this.initIndicatorContentView();
    }

    private void collectTicksInfo() {
        if (this.mTicksCount >= 0 && this.mTicksCount <= 50) {
            if (this.mTicksCount != 0) {
                this.mTickMarksX = new float[this.mTicksCount];
                if (this.mShowTickText) {
                    this.mTextCenterX = new float[this.mTicksCount];
                    this.mTickTextsWidth = new float[this.mTicksCount];
                }

                this.mProgressArr = new float[this.mTicksCount];

                for(int i = 0; i < this.mProgressArr.length; ++i) {
                    this.mProgressArr[i] = this.mMin + (float)i * (this.mMax - this.mMin) / (float)(this.mTicksCount - 1 > 0 ? this.mTicksCount - 1 : 1);
                }
            }

        } else {
            throw new IllegalArgumentException("the Argument: TICK COUNT must be limited between (0-50), Now is " + this.mTicksCount);
        }
    }

    private void initDefaultPadding() {
        if (!this.mClearPadding) {
            int normalPadding = SizeUtils.dp2px(this.mContext, 16.0F);
            if (this.getPaddingLeft() == 0) {
                this.setPadding(normalPadding, this.getPaddingTop(), this.getPaddingRight(), this.getPaddingBottom());
            }

            if (this.getPaddingRight() == 0) {
                this.setPadding(this.getPaddingLeft(), this.getPaddingTop(), normalPadding, this.getPaddingBottom());
            }
        }

    }

    private void initProgressRangeValue() {
        if (this.mMax < this.mMin) {
            throw new IllegalArgumentException("the Argument: MAX's value must be larger than MIN's.");
        } else {
            if (this.mProgress < this.mMin) {
                this.mProgress = this.mMin;
            }

            if (this.mProgress > this.mMax) {
                this.mProgress = this.mMax;
            }

        }
    }

    private void initStrokePaint() {
        if (this.mStockPaint == null) {
            this.mStockPaint = new Paint();
        }

        if (this.mTrackRoundedCorners) {
            this.mStockPaint.setStrokeCap(Cap.ROUND);
        }

        this.mStockPaint.setAntiAlias(true);
        if (this.mBackgroundTrackSize > this.mProgressTrackSize) {
            this.mProgressTrackSize = this.mBackgroundTrackSize;
        }

    }

    private void measureTickTextsBonds() {
        if (this.needDrawText()) {
            this.initTextPaint();
            this.mTextPaint.setTypeface(this.mTextsTypeface);
            this.mTextPaint.getTextBounds("j", 0, 1, this.mRect);
            this.mTickTextsHeight = this.mRect.height() + SizeUtils.dp2px(this.mContext, 3.0F);
        }

    }

    private boolean needDrawText() {
        return this.mShowThumbText || this.mTicksCount != 0 && this.mShowTickText;
    }

    private void initTextPaint() {
        if (this.mTextPaint == null) {
            this.mTextPaint = new TextPaint();
            this.mTextPaint.setAntiAlias(true);
            this.mTextPaint.setTextAlign(Align.CENTER);
            this.mTextPaint.setTextSize((float)this.mTickTextsSize);
        }

        if (this.mRect == null) {
            this.mRect = new Rect();
        }

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = Math.round(this.mCustomDrawableMaxHeight + (float)this.getPaddingTop() + (float)this.getPaddingBottom());
        this.setMeasuredDimension(resolveSize(SizeUtils.dp2px(this.mContext, 170.0F), widthMeasureSpec), height + this.mTickTextsHeight);
        this.initSeekBarInfo();
        this.refreshSeekBarLocation();
    }

    private void initSeekBarInfo() {
        this.mMeasuredWidth = this.getMeasuredWidth();
        if (VERSION.SDK_INT < 17) {
            this.mPaddingLeft = this.getPaddingLeft();
            this.mPaddingRight = this.getPaddingRight();
        } else {
            this.mPaddingLeft = this.getPaddingStart();
            this.mPaddingRight = this.getPaddingEnd();
        }

        this.mPaddingTop = this.getPaddingTop();
        this.mSeekLength = (float)(this.mMeasuredWidth - this.mPaddingLeft - this.mPaddingRight);
        this.mSeekBlockLength = this.mSeekLength / (float)(this.mTicksCount - 1 > 0 ? this.mTicksCount - 1 : 1);
    }

    private void refreshSeekBarLocation() {
        this.initTrackLocation();
        if (this.needDrawText()) {
            this.mTextPaint.getTextBounds("j", 0, 1, this.mRect);
            this.mTickTextY = (float)this.mPaddingTop + this.mCustomDrawableMaxHeight + (float)Math.round((float)this.mRect.height() - this.mTextPaint.descent()) + (float)SizeUtils.dp2px(this.mContext, 3.0F);
            this.mThumbTextY = this.mTickTextY;
        }

        if (this.mTickMarksX != null) {
            this.initTextsArray();
            if (this.mTicksCount > 2) {
                this.mProgress = this.mProgressArr[this.getClosestIndex()];
                this.lastProgress = this.mProgress;
            }

            this.refreshThumbCenterXByProgress(this.mProgress);
        }
    }

    private void initTextsArray() {
        if (this.mTicksCount != 0) {
            if (this.mShowTickText) {
                this.mTickTextsArr = new String[this.mTicksCount];
            }

            for(int i = 0; i < this.mTickMarksX.length; ++i) {
                if (this.mShowTickText) {
                    this.mTickTextsArr[i] = this.getTickTextByPosition(i);
                    this.mTextPaint.getTextBounds(this.mTickTextsArr[i], 0, this.mTickTextsArr[i].length(), this.mRect);
                    this.mTickTextsWidth[i] = (float)this.mRect.width();
                    this.mTextCenterX[i] = (float)this.mPaddingLeft + this.mSeekBlockLength * (float)i;
                }

                this.mTickMarksX[i] = (float)this.mPaddingLeft + this.mSeekBlockLength * (float)i;
            }

        }
    }

    private void initTrackLocation() {
        if (this.mR2L) {
            this.mBackgroundTrack.left = (float)this.mPaddingLeft;
            this.mBackgroundTrack.top = (float)this.mPaddingTop + this.mThumbTouchRadius;
            this.mBackgroundTrack.right = (float)this.mPaddingLeft + this.mSeekLength * (1.0F - (this.mProgress - this.mMin) / this.getAmplitude());
            this.mBackgroundTrack.bottom = this.mBackgroundTrack.top;
            this.mProgressTrack.left = this.mBackgroundTrack.right;
            this.mProgressTrack.top = this.mBackgroundTrack.top;
            this.mProgressTrack.right = (float)(this.mMeasuredWidth - this.mPaddingRight);
            this.mProgressTrack.bottom = this.mBackgroundTrack.bottom;
        } else {
            this.mProgressTrack.left = (float)this.mPaddingLeft;
            this.mProgressTrack.top = (float)this.mPaddingTop + this.mThumbTouchRadius;
            this.mProgressTrack.right = (this.mProgress - this.mMin) * this.mSeekLength / this.getAmplitude() + (float)this.mPaddingLeft;
            this.mProgressTrack.bottom = this.mProgressTrack.top;
            this.mBackgroundTrack.left = this.mProgressTrack.right;
            this.mBackgroundTrack.top = this.mProgressTrack.bottom;
            this.mBackgroundTrack.right = (float)(this.mMeasuredWidth - this.mPaddingRight);
            this.mBackgroundTrack.bottom = this.mProgressTrack.bottom;
        }

    }

    private String getTickTextByPosition(int index) {
        if (this.mTickTextsCustomArray == null) {
            return this.getProgressString(this.mProgressArr[index]);
        } else {
            return index < this.mTickTextsCustomArray.length ? String.valueOf(this.mTickTextsCustomArray[index]) : "";
        }
    }

    private void refreshThumbCenterXByProgress(float progress) {
        if (this.mR2L) {
            this.mBackgroundTrack.right = (float)this.mPaddingLeft + this.mSeekLength * (1.0F - (progress - this.mMin) / this.getAmplitude());
            this.mProgressTrack.left = this.mBackgroundTrack.right;
        } else {
            this.mProgressTrack.right = (progress - this.mMin) * this.mSeekLength / this.getAmplitude() + (float)this.mPaddingLeft;
            this.mBackgroundTrack.left = this.mProgressTrack.right;
        }

    }

    protected synchronized void onDraw(Canvas canvas) {
        this.drawTrack(canvas);
        this.drawTickMarks(canvas);
        this.drawTickTexts(canvas);
        this.drawThumb(canvas);
        this.drawThumbText(canvas);
    }

    private void drawTrack(Canvas canvas) {
        if (this.mCustomTrackSectionColorResult) {
            int sectionSize = this.mTicksCount - 1 > 0 ? this.mTicksCount - 1 : 1;
            this.mStockPaint.setStrokeCap(Cap.ROUND);

            for(int i = 0; i < sectionSize; ++i) {
                this.mStockPaint.setColor(this.mSectionTrackColorArray[i]);
                int thumbPosFloat = Math.round(Float.parseFloat(this.getThumbPosOnTickFloat() + ""));
                if (i < thumbPosFloat && thumbPosFloat < i + 1) {
                    Log.e("1231231233", "onDraw: " + this.mSectionTrackColorArray[i] + "  " + i);
                    this.mStockPaint.setColor(mProgressTrackColor);
                    float thumbCenterX = this.getThumbCenterX();
                    this.mStockPaint.setStrokeWidth((float)this.getLeftSideTrackSize());
                    canvas.drawLine(this.mTickMarksX[i], this.mProgressTrack.top, thumbCenterX, this.mProgressTrack.bottom, this.mStockPaint);
                    this.mStockPaint.setStrokeWidth((float)this.getRightSideTrackSize());
                    canvas.drawLine(thumbCenterX, this.mProgressTrack.top, this.mTickMarksX[i + 1], this.mProgressTrack.bottom, this.mStockPaint);
                } else {
                    if (i < thumbPosFloat) {
                        this.mStockPaint.setStrokeCap(Cap.ROUND);
                        Log.e("1231231211", "onDraw: " + this.mSectionTrackColorArray[i] + "  " + i);
                        this.mStockPaint.setStrokeWidth((float)this.getLeftSideTrackSize());
                        this.mStockPaint.setColor(mProgressTrackColor);
                    } else {
                        Log.e("1231231222", "onDraw: " + this.mSectionTrackColorArray[i] + "  " + i);
                        this.mStockPaint.setStrokeWidth((float)this.getRightSideTrackSize());
                        this.mStockPaint.setStrokeCap(Cap.SQUARE);
                    }

                    if (i == sectionSize - 1) {
                        this.mStockPaint.setStrokeWidth((float)this.getRightSideTrackSize());
                        this.mStockPaint.setStrokeCap(Cap.ROUND);
                    }

                    canvas.drawLine(this.mTickMarksX[i], this.mProgressTrack.top, this.mTickMarksX[i + 1], this.mProgressTrack.bottom, this.mStockPaint);
                }
            }
        } else {
            if (this.mColorSeeds.length != 0) {
                LinearGradient mColorGradient = new LinearGradient(0.0F, 0.0F, (float)this.getWidth(), 0.0F, this.mColorSeeds, (float[])null, TileMode.CLAMP);
                this.mStockPaint.setShader(mColorGradient);
                this.mStockPaint.setAntiAlias(true);
            }

            this.mStockPaint.setColor(this.mProgressTrackColor);
            this.mStockPaint.setStrokeWidth((float)this.mProgressTrackSize);
            canvas.drawLine(this.mProgressTrack.left, this.mProgressTrack.top, this.mProgressTrack.right, this.mProgressTrack.bottom, this.mStockPaint);
            this.mStockPaint.setColor(this.mBackgroundTrackColor);
            this.mStockPaint.setStrokeWidth((float)this.mBackgroundTrackSize);
            canvas.drawLine(this.mBackgroundTrack.left, this.mBackgroundTrack.top, this.mBackgroundTrack.right, this.mBackgroundTrack.bottom, this.mStockPaint);
        }

    }

    private void drawTickMarks(Canvas canvas) {
        if (this.mTicksCount != 0 && (this.mShowTickMarksType != 0 || this.mTickMarksDrawable != null)) {
            float thumbCenterX = this.getThumbCenterX();

            for(int i = 0; i < this.mTickMarksX.length; ++i) {
                float thumbPosFloat = this.getThumbPosOnTickFloat();
                if ((!this.mTickMarksSweptHide || !(thumbCenterX >= this.mTickMarksX[i])) && (!this.mTickMarksEndsHide || i != 0 && i != this.mTickMarksX.length - 1) && (i != this.getThumbPosOnTick() || this.mTicksCount <= 2 || this.mSeekSmoothly)) {
                    if ((float)i <= thumbPosFloat) {
                        this.mStockPaint.setColor(this.getLeftSideTickColor());
                    } else {
                        this.mStockPaint.setColor(this.getRightSideTickColor());
                    }

                    if (this.mTickMarksDrawable != null) {
                        if (this.mSelectTickMarksBitmap == null || this.mUnselectTickMarksBitmap == null) {
                            this.initTickMarksBitmap();
                        }

                        if (this.mSelectTickMarksBitmap == null || this.mUnselectTickMarksBitmap == null) {
                            throw new IllegalArgumentException("the format of the selector TickMarks drawable is wrong!");
                        }

                        if ((float)i <= thumbPosFloat) {
                            canvas.drawBitmap(this.mSelectTickMarksBitmap, this.mTickMarksX[i] - (float)this.mUnselectTickMarksBitmap.getWidth() / 2.0F, this.mProgressTrack.top - (float)this.mUnselectTickMarksBitmap.getHeight() / 2.0F, this.mStockPaint);
                        } else {
                            canvas.drawBitmap(this.mUnselectTickMarksBitmap, this.mTickMarksX[i] - (float)this.mUnselectTickMarksBitmap.getWidth() / 2.0F, this.mProgressTrack.top - (float)this.mUnselectTickMarksBitmap.getHeight() / 2.0F, this.mStockPaint);
                        }
                    } else if (this.mShowTickMarksType == 1) {
                        canvas.drawCircle(this.mTickMarksX[i], this.mProgressTrack.top, this.mTickRadius, this.mStockPaint);
                    } else if (this.mShowTickMarksType == 3) {
                        int rectWidth = SizeUtils.dp2px(this.mContext, 1.0F);
                        float dividerTickHeight;
                        if (thumbCenterX >= this.mTickMarksX[i]) {
                            dividerTickHeight = (float)this.getLeftSideTrackSize();
                        } else {
                            dividerTickHeight = (float)this.getRightSideTrackSize();
                        }

                        canvas.drawRect(this.mTickMarksX[i] - (float)rectWidth, this.mProgressTrack.top - dividerTickHeight / 2.0F, this.mTickMarksX[i] + (float)rectWidth, this.mProgressTrack.top + dividerTickHeight / 2.0F, this.mStockPaint);
                    } else if (this.mShowTickMarksType == 2) {
                        canvas.drawRect(this.mTickMarksX[i] - (float)this.mTickMarksSize / 2.0F, this.mProgressTrack.top - (float)this.mTickMarksSize / 2.0F, this.mTickMarksX[i] + (float)this.mTickMarksSize / 2.0F, this.mProgressTrack.top + (float)this.mTickMarksSize / 2.0F, this.mStockPaint);
                    }
                }
            }

        }
    }

    private void drawTickTexts(Canvas canvas) {
        if (this.mTickTextsArr != null) {
            float thumbPosFloat = this.getThumbPosOnTickFloat();

            for(int i = 0; i < this.mTickTextsArr.length; ++i) {
                if (!this.mShowBothTickTextsOnly || i == 0 || i == this.mTickTextsArr.length - 1) {
                    if (i == this.getThumbPosOnTick() && (float)i == thumbPosFloat) {
                        this.mTextPaint.setColor(this.mHoveredTextColor);
                    } else if ((float)i < thumbPosFloat) {
                        this.mTextPaint.setColor(this.getLeftSideTickTextsColor());
                    } else {
                        this.mTextPaint.setColor(this.getRightSideTickTextsColor());
                    }

                    int index = i;
                    if (this.mR2L) {
                        index = this.mTickTextsArr.length - i - 1;
                    }

                    if (i == 0) {
                        canvas.drawText(this.mTickTextsArr[index], this.mTextCenterX[i] + this.mTickTextsWidth[index] / 2.0F, this.mTickTextY, this.mTextPaint);
                    } else if (i == this.mTickTextsArr.length - 1) {
                        canvas.drawText(this.mTickTextsArr[index], this.mTextCenterX[i] - this.mTickTextsWidth[index] / 2.0F, this.mTickTextY, this.mTextPaint);
                    } else {
                        canvas.drawText(this.mTickTextsArr[index], this.mTextCenterX[i], this.mTickTextY, this.mTextPaint);
                    }
                }
            }

        }
    }

    private void drawThumb(Canvas canvas) {
        if (!this.mHideThumb) {
            float thumbCenterX = this.getThumbCenterX();
            if (this.mThumbDrawable == null) {
                if (this.mIsTouching) {
                    this.mStockPaint.setColor(this.mPressedThumbColor);
                } else {
                    this.mStockPaint.setColor(this.mThumbColor);
                }

                canvas.drawCircle(thumbCenterX, this.mProgressTrack.top, this.mIsTouching ? this.mThumbTouchRadius : this.mThumbRadius, this.mStockPaint);
            } else {
                if (this.mThumbBitmap == null || this.mPressedThumbBitmap == null) {
                    this.initThumbBitmap();
                }

                if (this.mThumbBitmap == null || this.mPressedThumbBitmap == null) {
                    throw new IllegalArgumentException("the format of the selector thumb drawable is wrong!");
                }

                this.mStockPaint.setAlpha(255);
                if (this.mIsTouching) {
                    canvas.drawBitmap(this.mPressedThumbBitmap, thumbCenterX - (float)this.mPressedThumbBitmap.getWidth() / 2.0F, this.mProgressTrack.top - (float)this.mPressedThumbBitmap.getHeight() / 2.0F, this.mStockPaint);
                } else {
                    canvas.drawBitmap(this.mThumbBitmap, thumbCenterX - (float)this.mThumbBitmap.getWidth() / 2.0F, this.mProgressTrack.top - (float)this.mThumbBitmap.getHeight() / 2.0F, this.mStockPaint);
                }
            }

        }
    }

    private void drawThumbText(Canvas canvas) {
        if (this.mShowThumbText && (!this.mShowTickText || this.mTicksCount <= 2)) {
            this.mTextPaint.setColor(this.mThumbTextColor);
            canvas.drawText(this.getProgressString(this.mProgress), this.getThumbCenterX(), this.mThumbTextY, this.mTextPaint);
        }
    }

    private float getThumbCenterX() {
        return this.mR2L ? this.mBackgroundTrack.right : this.mProgressTrack.right;
    }

    private int getLeftSideTickColor() {
        return this.mR2L ? this.mUnSelectedTickMarksColor : this.mSelectedTickMarksColor;
    }

    private int getRightSideTickColor() {
        return this.mR2L ? this.mSelectedTickMarksColor : this.mUnSelectedTickMarksColor;
    }

    private int getLeftSideTickTextsColor() {
        return this.mR2L ? this.mUnselectedTextsColor : this.mSelectedTextsColor;
    }

    private int getRightSideTickTextsColor() {
        return this.mR2L ? this.mSelectedTextsColor : this.mUnselectedTextsColor;
    }

    private int getLeftSideTrackSize() {
        return this.mR2L ? this.mBackgroundTrackSize : this.mProgressTrackSize;
    }

    private int getRightSideTrackSize() {
        return this.mR2L ? this.mProgressTrackSize : this.mBackgroundTrackSize;
    }

    private int getThumbPosOnTick() {
        return this.mTicksCount != 0 ? Math.round((this.getThumbCenterX() - (float)this.mPaddingLeft) / this.mSeekBlockLength) : 0;
    }

    private float getThumbPosOnTickFloat() {
        return this.mTicksCount != 0 ? (this.getThumbCenterX() - (float)this.mPaddingLeft) / this.mSeekBlockLength : 0.0F;
    }

    private int getHeightByRatio(Drawable drawable, int width) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        return Math.round(1.0F * (float)width * (float)intrinsicHeight / (float)intrinsicWidth);
    }

    private Bitmap getDrawBitmap(Drawable drawable, boolean isThumb) {
        if (drawable == null) {
            return null;
        } else {
            int maxRange = SizeUtils.dp2px(this.mContext, 30.0F);
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int width;
            int height;
            if (intrinsicWidth > maxRange) {
                if (isThumb) {
                    width = this.mThumbSize;
                } else {
                    width = this.mTickMarksSize;
                }

                height = this.getHeightByRatio(drawable, width);
                if (width > maxRange) {
                    width = maxRange;
                    height = this.getHeightByRatio(drawable, maxRange);
                }
            } else {
                width = drawable.getIntrinsicWidth();
                height = drawable.getIntrinsicHeight();
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    private void initThumbColor(ColorStateList colorStateList, int defaultColor) {
        if (colorStateList == null) {
            this.mThumbColor = defaultColor;
            this.mPressedThumbColor = this.mThumbColor;
        } else {
            int[][] states = null;
            int[] colors = null;
            Class<? extends ColorStateList> aClass = colorStateList.getClass();

            try {
                Field[] f = aClass.getDeclaredFields();
                Field[] var7 = f;
                int var8 = f.length;

                for(int var9 = 0; var9 < var8; ++var9) {
                    Field field = var7[var9];
                    field.setAccessible(true);
                    if ("mStateSpecs".equals(field.getName())) {
                        states = (int[][])field.get(colorStateList);
                    }

                    if ("mColors".equals(field.getName())) {
                        colors = (int[])field.get(colorStateList);
                    }
                }

                if (states == null || colors == null) {
                    return;
                }
            } catch (Exception var11) {
                throw new RuntimeException("Something wrong happened when parseing thumb selector color.");
            }

            if (states.length == 1) {
                this.mThumbColor = colors[0];
                this.mPressedThumbColor = this.mThumbColor;
            } else {
                if (states.length != 2) {
                    throw new IllegalArgumentException("the selector color file you set for the argument: isb_thumb_color is in wrong format.");
                }

                for(int i = 0; i < states.length; ++i) {
                    int[] attr = states[i];
                    if (attr.length == 0) {
                        this.mPressedThumbColor = colors[i];
                    } else {
                        switch (attr[0]) {
                            case 16842919:
                                this.mThumbColor = colors[i];
                                break;
                            default:
                                throw new IllegalArgumentException("the selector color file you set for the argument: isb_thumb_color is in wrong format.");
                        }
                    }
                }
            }

        }
    }

    private void initTickMarksColor(ColorStateList colorStateList, int defaultColor) {
        if (colorStateList == null) {
            this.mSelectedTickMarksColor = defaultColor;
            this.mUnSelectedTickMarksColor = this.mSelectedTickMarksColor;
        } else {
            int[][] states = null;
            int[] colors = null;
            Class<? extends ColorStateList> aClass = colorStateList.getClass();

            try {
                Field[] f = aClass.getDeclaredFields();
                Field[] var7 = f;
                int var8 = f.length;

                for(int var9 = 0; var9 < var8; ++var9) {
                    Field field = var7[var9];
                    field.setAccessible(true);
                    if ("mStateSpecs".equals(field.getName())) {
                        states = (int[][])field.get(colorStateList);
                    }

                    if ("mColors".equals(field.getName())) {
                        colors = (int[])field.get(colorStateList);
                    }
                }

                if (states == null || colors == null) {
                    return;
                }
            } catch (Exception var11) {
                throw new RuntimeException("Something wrong happened when parsing thumb selector color." + var11.getMessage());
            }

            if (states.length == 1) {
                this.mSelectedTickMarksColor = colors[0];
                this.mUnSelectedTickMarksColor = this.mSelectedTickMarksColor;
            } else {
                if (states.length != 2) {
                    throw new IllegalArgumentException("the selector color file you set for the argument: isb_tick_marks_color is in wrong format.");
                }

                for(int i = 0; i < states.length; ++i) {
                    int[] attr = states[i];
                    if (attr.length == 0) {
                        this.mUnSelectedTickMarksColor = colors[i];
                    } else {
                        switch (attr[0]) {
                            case 16842913:
                                this.mSelectedTickMarksColor = colors[i];
                                break;
                            default:
                                throw new IllegalArgumentException("the selector color file you set for the argument: isb_tick_marks_color is in wrong format.");
                        }
                    }
                }
            }

        }
    }

    private void initTickTextsColor(ColorStateList colorStateList, int defaultColor) {
        if (colorStateList == null) {
            this.mUnselectedTextsColor = defaultColor;
            this.mSelectedTextsColor = this.mUnselectedTextsColor;
            this.mHoveredTextColor = this.mUnselectedTextsColor;
        } else {
            int[][] states = null;
            int[] colors = null;
            Class<? extends ColorStateList> aClass = colorStateList.getClass();

            try {
                Field[] f = aClass.getDeclaredFields();
                Field[] var7 = f;
                int var8 = f.length;

                for(int var9 = 0; var9 < var8; ++var9) {
                    Field field = var7[var9];
                    field.setAccessible(true);
                    if ("mStateSpecs".equals(field.getName())) {
                        states = (int[][])field.get(colorStateList);
                    }

                    if ("mColors".equals(field.getName())) {
                        colors = (int[])field.get(colorStateList);
                    }
                }

                if (states == null || colors == null) {
                    return;
                }
            } catch (Exception var11) {
                throw new RuntimeException("Something wrong happened when parseing thumb selector color.");
            }

            if (states.length == 1) {
                this.mUnselectedTextsColor = colors[0];
                this.mSelectedTextsColor = this.mUnselectedTextsColor;
                this.mHoveredTextColor = this.mUnselectedTextsColor;
            } else {
                if (states.length != 3) {
                    throw new IllegalArgumentException("the selector color file you set for the argument: isb_tick_texts_color is in wrong format.");
                }

                for(int i = 0; i < states.length; ++i) {
                    int[] attr = states[i];
                    if (attr.length == 0) {
                        this.mUnselectedTextsColor = colors[i];
                    } else {
                        switch (attr[0]) {
                            case 16842913:
                                this.mSelectedTextsColor = colors[i];
                                break;
                            case 16843623:
                                this.mHoveredTextColor = colors[i];
                                break;
                            default:
                                throw new IllegalArgumentException("the selector color file you set for the argument: isb_tick_texts_color is in wrong format.");
                        }
                    }
                }
            }

        }
    }

    private void initTextsTypeface(int typeface, Typeface defaultTypeface) {
        switch (typeface) {
            case 0:
                this.mTextsTypeface = Typeface.DEFAULT;
                break;
            case 1:
                this.mTextsTypeface = Typeface.MONOSPACE;
                break;
            case 2:
                this.mTextsTypeface = Typeface.SANS_SERIF;
                break;
            case 3:
                this.mTextsTypeface = Typeface.SERIF;
                break;
            default:
                if (defaultTypeface == null) {
                    this.mTextsTypeface = Typeface.DEFAULT;
                } else {
                    this.mTextsTypeface = defaultTypeface;
                }
        }

    }

    private void initThumbBitmap() {
        if (this.mThumbDrawable != null) {
            if (this.mThumbDrawable instanceof StateListDrawable) {
                try {
                    StateListDrawable listDrawable = (StateListDrawable)this.mThumbDrawable;
                    Class<? extends StateListDrawable> aClass = listDrawable.getClass();
                    int stateCount = (Integer)aClass.getMethod("getStateCount").invoke(listDrawable);
                    if (stateCount != 2) {
                        throw new IllegalArgumentException("the format of the selector thumb drawable is wrong!");
                    }

                    Method getStateSet = aClass.getMethod("getStateSet", Integer.TYPE);
                    Method getStateDrawable = aClass.getMethod("getStateDrawable", Integer.TYPE);

                    for(int i = 0; i < stateCount; ++i) {
                        int[] stateSet = (int[])getStateSet.invoke(listDrawable, i);
                        Drawable stateDrawable;
                        if (stateSet.length > 0) {
                            if (stateSet[0] != 16842919) {
                                throw new IllegalArgumentException("the state of the selector thumb drawable is wrong!");
                            }

                            stateDrawable = (Drawable)getStateDrawable.invoke(listDrawable, i);
                            this.mPressedThumbBitmap = this.getDrawBitmap(stateDrawable, true);
                        } else {
                            stateDrawable = (Drawable)getStateDrawable.invoke(listDrawable, i);
                            this.mThumbBitmap = this.getDrawBitmap(stateDrawable, true);
                        }
                    }
                } catch (Exception var9) {
                    this.mThumbBitmap = this.getDrawBitmap(this.mThumbDrawable, true);
                    this.mPressedThumbBitmap = this.mThumbBitmap;
                }
            } else {
                this.mThumbBitmap = this.getDrawBitmap(this.mThumbDrawable, true);
                this.mPressedThumbBitmap = this.mThumbBitmap;
            }

        }
    }

    private void initTickMarksBitmap() {
        if (this.mTickMarksDrawable instanceof StateListDrawable) {
            StateListDrawable listDrawable = (StateListDrawable)this.mTickMarksDrawable;

            try {
                Class<? extends StateListDrawable> aClass = listDrawable.getClass();
                Method getStateCount = aClass.getMethod("getStateCount");
                int stateCount = (Integer)getStateCount.invoke(listDrawable);
                if (stateCount != 2) {
                    throw new IllegalArgumentException("the format of the selector TickMarks drawable is wrong!");
                }

                Method getStateSet = aClass.getMethod("getStateSet", Integer.TYPE);
                Method getStateDrawable = aClass.getMethod("getStateDrawable", Integer.TYPE);

                for(int i = 0; i < stateCount; ++i) {
                    int[] stateSet = (int[])getStateSet.invoke(listDrawable, i);
                    Drawable stateDrawable;
                    if (stateSet.length > 0) {
                        if (stateSet[0] != 16842913) {
                            throw new IllegalArgumentException("the state of the selector TickMarks drawable is wrong!");
                        }

                        stateDrawable = (Drawable)getStateDrawable.invoke(listDrawable, i);
                        this.mSelectTickMarksBitmap = this.getDrawBitmap(stateDrawable, false);
                    } else {
                        stateDrawable = (Drawable)getStateDrawable.invoke(listDrawable, i);
                        this.mUnselectTickMarksBitmap = this.getDrawBitmap(stateDrawable, false);
                    }
                }
            } catch (Exception var10) {
                this.mUnselectTickMarksBitmap = this.getDrawBitmap(this.mTickMarksDrawable, false);
                this.mSelectTickMarksBitmap = this.mUnselectTickMarksBitmap;
            }
        } else {
            this.mUnselectTickMarksBitmap = this.getDrawBitmap(this.mTickMarksDrawable, false);
            this.mSelectTickMarksBitmap = this.mUnselectTickMarksBitmap;
        }

    }

    public void setEnabled(boolean enabled) {
        if (enabled != this.isEnabled()) {
            super.setEnabled(enabled);
            if (this.isEnabled()) {
                this.setAlpha(1.0F);
                if (this.mIndicatorStayAlways) {
                    this.mIndicatorContentView.setAlpha(1.0F);
                }
            } else {
                this.setAlpha(0.3F);
                if (this.mIndicatorStayAlways) {
                    this.mIndicatorContentView.setAlpha(0.3F);
                }
            }

        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.post(new Runnable() {
            public void run() {
                IndicatorSeekBar.this.requestLayout();
            }
        });
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        ViewParent parent = this.getParent();
        if (parent == null) {
            return super.dispatchTouchEvent(event);
        } else {
            switch (event.getAction()) {
                case 0:
                    parent.requestDisallowInterceptTouchEvent(true);
                    break;
                case 1:
                case 3:
                    parent.requestDisallowInterceptTouchEvent(false);
                case 2:
            }

            return super.dispatchTouchEvent(event);
        }
    }

    public boolean performClick() {
        return super.performClick();
    }

    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("isb_instance_state", super.onSaveInstanceState());
        bundle.putFloat("isb_progress", this.mProgress);
        return bundle;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle)state;
            this.setProgress(bundle.getFloat("isb_progress"));
            super.onRestoreInstanceState(bundle.getParcelable("isb_instance_state"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mUserSeekable && this.isEnabled()) {
            switch (event.getAction()) {
                case 0:
                    this.performClick();
                    float mX = event.getX();
                    if (this.isTouchSeekBar(mX, event.getY())) {
                        if (this.mOnlyThumbDraggable && !this.isTouchThumb(mX)) {
                            return false;
                        }

                        this.mIsTouching = true;
                        if (this.mSeekChangeListener != null) {
                            this.mSeekChangeListener.onStartTrackingTouch(this);
                        }

                        this.refreshSeekBar(event);
                        return true;
                    }
                    break;
                case 1:
                case 3:
                    this.mIsTouching = false;
                    if (this.mSeekChangeListener != null) {
                        this.mSeekChangeListener.onStopTrackingTouch(this);
                    }

                    if (!this.autoAdjustThumb()) {
                        this.invalidate();
                    }

                    if (this.mIndicator != null) {
                        this.mIndicator.hide();
                    }
                    break;
                case 2:
                    this.refreshSeekBar(event);
            }

            return super.onTouchEvent(event);
        } else {
            return false;
        }
    }

    private void refreshSeekBar(MotionEvent event) {
        this.refreshThumbCenterXByProgress(this.calculateProgress(this.calculateTouchX(this.adjustTouchX(event))));
        this.setSeekListener(true);
        this.invalidate();
        this.updateIndicator();
    }

    private boolean progressChange() {
        if (this.mIsFloatProgress) {
            return this.lastProgress != this.mProgress;
        } else {
            return Math.round(this.lastProgress) != Math.round(this.mProgress);
        }
    }

    private float adjustTouchX(MotionEvent event) {
        float mTouchXCache;
        if (event.getX() < (float)this.mPaddingLeft) {
            mTouchXCache = (float)this.mPaddingLeft;
        } else if (event.getX() > (float)(this.mMeasuredWidth - this.mPaddingRight)) {
            mTouchXCache = (float)(this.mMeasuredWidth - this.mPaddingRight);
        } else {
            mTouchXCache = event.getX();
        }

        return mTouchXCache;
    }

    private float calculateProgress(float touchX) {
        this.lastProgress = this.mProgress;
        this.mProgress = this.mMin + this.getAmplitude() * (touchX - (float)this.mPaddingLeft) / this.mSeekLength;
        return this.mProgress;
    }

    private float calculateTouchX(float touchX) {
        float touchXTemp = touchX;
        if (this.mTicksCount > 2 && !this.mSeekSmoothly) {
            int touchBlockSize = Math.round((touchX - (float)this.mPaddingLeft) / this.mSeekBlockLength);
            touchXTemp = this.mSeekBlockLength * (float)touchBlockSize + (float)this.mPaddingLeft;
        }

        return this.mR2L ? this.mSeekLength - touchXTemp + (float)(2 * this.mPaddingLeft) : touchXTemp;
    }

    private boolean isTouchSeekBar(float mX, float mY) {
        if (this.mFaultTolerance == -1.0F) {
            this.mFaultTolerance = (float)SizeUtils.dp2px(this.mContext, 5.0F);
        }

        boolean inWidthRange = mX >= (float)this.mPaddingLeft - 2.0F * this.mFaultTolerance && mX <= (float)(this.mMeasuredWidth - this.mPaddingRight) + 2.0F * this.mFaultTolerance;
        boolean inHeightRange = mY >= this.mProgressTrack.top - this.mThumbTouchRadius - this.mFaultTolerance && mY <= this.mProgressTrack.top + this.mThumbTouchRadius + this.mFaultTolerance;
        return inWidthRange && inHeightRange;
    }

    private boolean isTouchThumb(float mX) {
        this.refreshThumbCenterXByProgress(this.mProgress);
        float rawTouchX;
        if (this.mR2L) {
            rawTouchX = this.mBackgroundTrack.right;
        } else {
            rawTouchX = this.mProgressTrack.right;
        }

        return rawTouchX - (float)this.mThumbSize / 2.0F <= mX && mX <= rawTouchX + (float)this.mThumbSize / 2.0F;
    }

    private void updateIndicator() {
        if (this.mIndicatorStayAlways) {
            this.updateStayIndicator();
        } else {
            if (this.mIndicator == null) {
                return;
            }

            this.mIndicator.iniPop();
            if (this.mIndicator.isShowing()) {
                this.mIndicator.update(this.getThumbCenterX());
            } else {
                this.mIndicator.show(this.getThumbCenterX());
            }
        }

    }

    private void initIndicatorContentView() {
        if (this.mShowIndicatorType != 0) {
            if (this.mIndicator == null) {
                this.mIndicator = new Indicator(this.mContext, this, this.mIndicatorColor, this.mShowIndicatorType, this.mIndicatorTextSize, this.mIndicatorTextColor, this.mIndicatorContentView, this.mIndicatorTopContentView);
                this.mIndicatorContentView = this.mIndicator.getInsideContentView();
            }

        }
    }

    private void updateStayIndicator() {
        if (this.mIndicatorStayAlways && this.mIndicator != null) {
            this.mIndicator.setProgressTextView(this.getIndicatorTextString());
            this.mIndicatorContentView.measure(0, 0);
            int measuredWidth = this.mIndicatorContentView.getMeasuredWidth();
            float thumbCenterX = this.getThumbCenterX();
            if (this.mScreenWidth == -1.0F) {
                DisplayMetrics metric = new DisplayMetrics();
                WindowManager systemService = (WindowManager)this.mContext.getSystemService("window");
                if (systemService != null) {
                    systemService.getDefaultDisplay().getMetrics(metric);
                    this.mScreenWidth = (float)metric.widthPixels;
                }
            }

            int indicatorOffset;
            int arrowOffset;
            if ((float)(measuredWidth / 2) + thumbCenterX > (float)this.mMeasuredWidth) {
                indicatorOffset = this.mMeasuredWidth - measuredWidth;
                arrowOffset = (int)(thumbCenterX - (float)indicatorOffset - (float)(measuredWidth / 2));
            } else if (thumbCenterX - (float)(measuredWidth / 2) < 0.0F) {
                indicatorOffset = 0;
                arrowOffset = -((int)((float)(measuredWidth / 2) - thumbCenterX));
            } else {
                indicatorOffset = (int)(this.getThumbCenterX() - (float)(measuredWidth / 2));
                arrowOffset = 0;
            }

            this.mIndicator.updateIndicatorLocation(indicatorOffset);
            this.mIndicator.updateArrowViewLocation(arrowOffset);
        }
    }

    private boolean autoAdjustThumb() {
        if (this.mTicksCount >= 3 && this.mSeekSmoothly) {
            if (!this.mAdjustAuto) {
                return false;
            } else {
                final int closestIndex = this.getClosestIndex();
                final float touchUpProgress = this.mProgress;
                ValueAnimator animator = ValueAnimator.ofFloat(new float[]{0.0F, Math.abs(touchUpProgress - this.mProgressArr[closestIndex])});
                animator.start();
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        IndicatorSeekBar.this.lastProgress = IndicatorSeekBar.this.mProgress;
                        if (touchUpProgress - IndicatorSeekBar.this.mProgressArr[closestIndex] > 0.0F) {
                            IndicatorSeekBar.this.mProgress = touchUpProgress - (Float)animation.getAnimatedValue();
                        } else {
                            IndicatorSeekBar.this.mProgress = touchUpProgress + (Float)animation.getAnimatedValue();
                        }

                        IndicatorSeekBar.this.refreshThumbCenterXByProgress(IndicatorSeekBar.this.mProgress);
                        IndicatorSeekBar.this.setSeekListener(false);
                        if (IndicatorSeekBar.this.mIndicator != null && IndicatorSeekBar.this.mIndicatorStayAlways) {
                            IndicatorSeekBar.this.mIndicator.refreshProgressText();
                            IndicatorSeekBar.this.updateStayIndicator();
                        }

                        IndicatorSeekBar.this.invalidate();
                    }
                });
                return true;
            }
        } else {
            return false;
        }
    }

    private String getProgressString(float progress) {
        return this.mIsFloatProgress ? FormatUtils.fastFormat((double)progress, this.mScale) : String.valueOf(Math.round(progress));
    }

    private int getClosestIndex() {
        int closestIndex = 0;
        float amplitude = Math.abs(this.mMax - this.mMin);

        for(int i = 0; i < this.mProgressArr.length; ++i) {
            float amplitudeTemp = Math.abs(this.mProgressArr[i] - this.mProgress);
            if (amplitudeTemp <= amplitude) {
                amplitude = amplitudeTemp;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    private float getAmplitude() {
        return this.mMax - this.mMin > 0.0F ? this.mMax - this.mMin : 1.0F;
    }

    private void setSeekListener(boolean formUser) {
        if (this.mSeekChangeListener != null) {
            if (this.progressChange()) {
                this.mSeekChangeListener.onSeeking(this.collectParams(formUser));
            }

        }
    }

    private SeekParams collectParams(boolean formUser) {
        if (this.mSeekParams == null) {
            this.mSeekParams = new SeekParams(this);
        }

        this.mSeekParams.progress = this.getProgress();
        this.mSeekParams.progressFloat = this.getProgressFloat();
        this.mSeekParams.fromUser = formUser;
        if (this.mTicksCount > 2) {
            int rawThumbPos = this.getThumbPosOnTick();
            if (this.mShowTickText && this.mTickTextsArr != null) {
                this.mSeekParams.tickText = this.mTickTextsArr[rawThumbPos];
            }

            if (this.mR2L) {
                this.mSeekParams.thumbPosition = this.mTicksCount - rawThumbPos - 1;
            } else {
                this.mSeekParams.thumbPosition = rawThumbPos;
            }
        }

        return this.mSeekParams;
    }

    private void apply(Builder builder) {
        this.mMax = builder.max;
        this.mMin = builder.min;
        this.mProgress = builder.progress;
        this.mIsFloatProgress = builder.progressValueFloat;
        this.mTicksCount = builder.tickCount;
        this.mSeekSmoothly = builder.seekSmoothly;
        this.mR2L = builder.r2l;
        this.mUserSeekable = builder.userSeekable;
        this.mClearPadding = builder.clearPadding;
        this.mOnlyThumbDraggable = builder.onlyThumbDraggable;
        this.mShowIndicatorType = builder.showIndicatorType;
        this.mIndicatorColor = builder.indicatorColor;
        this.mIndicatorTextColor = builder.indicatorTextColor;
        this.mIndicatorTextSize = builder.indicatorTextSize;
        this.mIndicatorContentView = builder.indicatorContentView;
        this.mIndicatorTopContentView = builder.indicatorTopContentView;
        this.mBackgroundTrackSize = builder.trackBackgroundSize;
        this.mBackgroundTrackColor = builder.trackBackgroundColor;
        this.mProgressTrackSize = builder.trackProgressSize;
        this.mProgressTrackColor = builder.trackProgressColor;
        this.mTrackRoundedCorners = builder.trackRoundedCorners;
        this.mThumbSize = builder.thumbSize;
        this.mThumbDrawable = builder.thumbDrawable;
        this.mThumbTextColor = builder.thumbTextColor;
        this.initThumbColor(builder.thumbColorStateList, builder.thumbColor);
        this.mShowThumbText = builder.showThumbText;
        this.mShowTickMarksType = builder.showTickMarksType;
        this.mTickMarksSize = builder.tickMarksSize;
        this.mTickMarksDrawable = builder.tickMarksDrawable;
        this.mTickMarksEndsHide = builder.tickMarksEndsHide;
        this.mTickMarksSweptHide = builder.tickMarksSweptHide;
        this.initTickMarksColor(builder.tickMarksColorStateList, builder.tickMarksColor);
        this.mShowTickText = builder.showTickText;
        this.mTickTextsSize = builder.tickTextsSize;
        this.mTickTextsCustomArray = builder.tickTextsCustomArray;
        this.mTextsTypeface = builder.tickTextsTypeFace;
        this.initTickTextsColor(builder.tickTextsColorStateList, builder.tickTextsColor);
    }

    void showStayIndicator() {
        this.mIndicatorContentView.setVisibility(4);
        this.postDelayed(new Runnable() {
            public void run() {
                Animation animation = new AlphaAnimation(0.1F, 1.0F);
                animation.setDuration(180L);
                IndicatorSeekBar.this.mIndicatorContentView.setAnimation(animation);
                IndicatorSeekBar.this.updateStayIndicator();
                IndicatorSeekBar.this.mIndicatorContentView.setVisibility(0);
            }
        }, 300L);
    }

    void setIndicatorStayAlways(boolean indicatorStayAlways) {
        this.mIndicatorStayAlways = indicatorStayAlways;
    }

    View getIndicatorContentView() {
        return this.mIndicatorContentView;
    }

    String getIndicatorTextString() {
        if (this.mIndicatorTextFormat != null && this.mIndicatorTextFormat.contains("${TICK_TEXT}")) {
            if (this.mTicksCount > 2 && this.mTickTextsArr != null) {
                return this.mIndicatorTextFormat.replace("${TICK_TEXT}", this.mTickTextsArr[this.getThumbPosOnTick()]);
            }
        } else if (this.mIndicatorTextFormat != null && this.mIndicatorTextFormat.contains("${PROGRESS}")) {
            return this.mIndicatorTextFormat.replace("${PROGRESS}", this.getProgressString(this.mProgress));
        }

        return this.getProgressString(this.mProgress);
    }

    public static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    public Indicator getIndicator() {
        return this.mIndicator;
    }

    public int getTickCount() {
        return this.mTicksCount;
    }

    public synchronized float getProgressFloat() {
        BigDecimal bigDecimal = BigDecimal.valueOf((double)this.mProgress);
        return bigDecimal.setScale(this.mScale, 4).floatValue();
    }

    public int getProgress() {
        return Math.round(this.mProgress);
    }

    public float getMax() {
        return this.mMax;
    }

    public float getMin() {
        return this.mMin;
    }

    public OnSeekChangeListener getOnSeekChangeListener() {
        return this.mSeekChangeListener;
    }

    public synchronized void setProgress(float progress) {
        this.lastProgress = this.mProgress;
        this.mProgress = progress < this.mMin ? this.mMin : (progress > this.mMax ? this.mMax : progress);
        if (!this.mSeekSmoothly && this.mTicksCount > 2) {
            this.mProgress = this.mProgressArr[this.getClosestIndex()];
        }

        this.setSeekListener(false);
        this.refreshThumbCenterXByProgress(this.mProgress);
        this.postInvalidate();
        this.updateStayIndicator();
    }

    public synchronized void setMax(float max) {
        this.mMax = Math.max(this.mMin, max);
        this.initProgressRangeValue();
        this.collectTicksInfo();
        this.refreshSeekBarLocation();
        this.invalidate();
        this.updateStayIndicator();
    }

    public synchronized void setMin(float min) {
        this.mMin = Math.min(this.mMax, min);
        this.initProgressRangeValue();
        this.collectTicksInfo();
        this.refreshSeekBarLocation();
        this.invalidate();
        this.updateStayIndicator();
    }

    public void setR2L(boolean isR2L) {
        this.mR2L = isR2L;
        this.requestLayout();
        this.invalidate();
        this.updateStayIndicator();
    }

    public void setThumbDrawable(Drawable drawable) {
        if (drawable == null) {
            this.mThumbDrawable = null;
            this.mThumbBitmap = null;
            this.mPressedThumbBitmap = null;
        } else {
            this.mThumbDrawable = drawable;
            this.mThumbRadius = (float)Math.min(SizeUtils.dp2px(this.mContext, 30.0F), this.mThumbSize) / 2.0F;
            this.mThumbTouchRadius = this.mThumbRadius;
            this.mCustomDrawableMaxHeight = Math.max(this.mThumbTouchRadius, this.mTickRadius) * 2.0F;
            this.initThumbBitmap();
        }

        this.requestLayout();
        this.invalidate();
    }

    public void hideThumb(boolean hide) {
        this.mHideThumb = hide;
        this.invalidate();
    }

    public void hideThumbText(boolean hide) {
        this.mShowThumbText = !hide;
        this.invalidate();
    }

    public void thumbColor(@ColorInt int thumbColor) {
        this.mThumbColor = thumbColor;
        this.mPressedThumbColor = thumbColor;
        this.invalidate();
    }

    public void thumbColorStateList(@NonNull ColorStateList thumbColorStateList) {
        this.initThumbColor(thumbColorStateList, this.mThumbColor);
        this.invalidate();
    }

    public void setTickMarksDrawable(Drawable drawable) {
        if (drawable == null) {
            this.mTickMarksDrawable = null;
            this.mUnselectTickMarksBitmap = null;
            this.mSelectTickMarksBitmap = null;
        } else {
            this.mTickMarksDrawable = drawable;
            this.mTickRadius = (float)Math.min(SizeUtils.dp2px(this.mContext, 30.0F), this.mTickMarksSize) / 2.0F;
            this.mCustomDrawableMaxHeight = Math.max(this.mThumbTouchRadius, this.mTickRadius) * 2.0F;
            this.initTickMarksBitmap();
        }

        this.invalidate();
    }

    public void tickMarksColor(@ColorInt int tickMarksColor) {
        this.mSelectedTickMarksColor = tickMarksColor;
        this.mUnSelectedTickMarksColor = tickMarksColor;
        this.invalidate();
    }

    public void tickMarksColor(@NonNull ColorStateList tickMarksColorStateList) {
        this.initTickMarksColor(tickMarksColorStateList, this.mSelectedTickMarksColor);
        this.invalidate();
    }

    public void tickTextsColor(@ColorInt int tickTextsColor) {
        this.mUnselectedTextsColor = tickTextsColor;
        this.mSelectedTextsColor = tickTextsColor;
        this.mHoveredTextColor = tickTextsColor;
        this.invalidate();
    }

    public void tickTextsColorStateList(@NonNull ColorStateList tickTextsColorStateList) {
        this.initTickTextsColor(tickTextsColorStateList, this.mSelectedTextsColor);
        this.invalidate();
    }

    public void setDecimalScale(int scale) {
        this.mScale = scale;
    }

    public void setIndicatorTextFormat(String format) {
        this.mIndicatorTextFormat = format;
        this.initTextsArray();
        this.updateStayIndicator();
    }

    public void customSectionTrackColor(@NonNull ColorCollector collector) {
        int[] colorArray = new int[this.mTicksCount - 1 > 0 ? this.mTicksCount - 1 : 1];

        for(int i = 0; i < colorArray.length; ++i) {
            colorArray[i] = this.mBackgroundTrackColor;
        }

        this.mCustomTrackSectionColorResult = collector.collectSectionTrackColor(colorArray);
        this.mSectionTrackColorArray = colorArray;
        this.invalidate();
    }

    public void customTickTexts(@NonNull String[] tickTextsArr) {
        this.mTickTextsCustomArray = tickTextsArr;
        if (this.mTickTextsArr != null) {
            for(int i = 0; i < this.mTickTextsArr.length; ++i) {
                String tickText;
                if (i < tickTextsArr.length) {
                    tickText = String.valueOf(tickTextsArr[i]);
                } else {
                    tickText = "";
                }

                int index = i;
                if (this.mR2L) {
                    index = this.mTicksCount - 1 - i;
                }

                this.mTickTextsArr[index] = tickText;
                if (this.mTextPaint != null && this.mRect != null) {
                    this.mTextPaint.getTextBounds(tickText, 0, tickText.length(), this.mRect);
                    this.mTickTextsWidth[index] = (float)this.mRect.width();
                }
            }

            this.invalidate();
        }

    }

    public void customTickTextsTypeface(@NonNull Typeface typeface) {
        this.mTextsTypeface = typeface;
        this.measureTickTextsBonds();
        this.requestLayout();
        this.invalidate();
    }

    public void setOnSeekChangeListener(@NonNull OnSeekChangeListener listener) {
        this.mSeekChangeListener = listener;
    }

    public void showBothEndsTickTextsOnly(boolean onlyShow) {
        this.mShowBothTickTextsOnly = onlyShow;
    }

    public void setUserSeekAble(boolean seekAble) {
        this.mUserSeekable = seekAble;
    }

    public synchronized void setTickCount(int tickCount) {
        if (this.mTicksCount >= 0 && this.mTicksCount <= 50) {
            this.mTicksCount = tickCount;
            this.collectTicksInfo();
            this.initTextsArray();
            this.initSeekBarInfo();
            this.refreshSeekBarLocation();
            this.invalidate();
            this.updateStayIndicator();
        } else {
            throw new IllegalArgumentException("the Argument: TICK COUNT must be limited between (0-50), Now is " + this.mTicksCount);
        }
    }

    public void setThumbAdjustAuto(boolean adjustAuto) {
        this.mAdjustAuto = adjustAuto;
    }
}
