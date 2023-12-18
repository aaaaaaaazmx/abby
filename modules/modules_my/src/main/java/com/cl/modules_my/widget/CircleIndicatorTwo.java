package com.cl.modules_my.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.youth.banner.indicator.BaseIndicator;
import com.youth.banner.util.BannerUtils;

public class CircleIndicatorTwo extends BaseIndicator {
    private int mNormalRadius;
    private int mSelectedRadius;
    private int maxRadius;

    public static final int INDICATOR_NORMAL_WIDTH = (int) BannerUtils.dp2px(12);
    public static final int INDICATOR_SELECTED_WIDTH = (int) BannerUtils.dp2px(14);
    
    public CircleIndicatorTwo(Context context) {
        this(context, null);
    }

    public CircleIndicatorTwo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleIndicatorTwo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mNormalRadius = INDICATOR_NORMAL_WIDTH / 2;
        mSelectedRadius = INDICATOR_SELECTED_WIDTH / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = config.getIndicatorSize();
        if (count <= 1) {
            return;
        }

        mNormalRadius = INDICATOR_NORMAL_WIDTH / 2;
        mSelectedRadius = INDICATOR_SELECTED_WIDTH / 2;
        //考虑当 选中和默认 的大小不一样的情况
        maxRadius = Math.max(mSelectedRadius, mNormalRadius);
        //间距*（总数-1）+选中宽度+默认宽度*（总数-1）
        int width = (count - 1) * config.getIndicatorSpace() + INDICATOR_SELECTED_WIDTH + INDICATOR_NORMAL_WIDTH * (count - 1);
        setMeasuredDimension(width, Math.max(INDICATOR_NORMAL_WIDTH, INDICATOR_SELECTED_WIDTH));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int count = config.getIndicatorSize();
        if (count <= 1) {
            return;
        }
        float left = 0;
        for (int i = 0; i < count; i++) {
            mPaint.setColor(config.getCurrentPosition() == i ? config.getSelectedColor() : config.getNormalColor());
            int indicatorWidth = config.getCurrentPosition() == i ? INDICATOR_SELECTED_WIDTH : INDICATOR_NORMAL_WIDTH;
            int radius = config.getCurrentPosition() == i ? mSelectedRadius : mNormalRadius;
            canvas.drawCircle(left + radius, maxRadius, radius, mPaint);
            left += indicatorWidth + config.getIndicatorSpace();
        }
//        mPaint.setColor(config.getNormalColor());
//        for (int i = 0; i < count; i++) {
//            canvas.drawCircle(left + maxRadius, maxRadius, mNormalRadius, mPaint);
//            left += INDICATOR_NORMAL_WIDTH + config.getIndicatorSpace();
//        }
//        mPaint.setColor(config.getSelectedColor());
//        left = maxRadius + (INDICATOR_NORMAL_WIDTH + config.getIndicatorSpace()) * config.getCurrentPosition();
//        canvas.drawCircle(left, maxRadius, mSelectedRadius, mPaint);
    }

}