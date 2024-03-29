package com.cl.common_base.widget.waterview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.test.services.storage.internal.TestStorageUtil;

import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cl.common_base.R;
import com.cl.common_base.report.Reporter;
import com.cl.common_base.widget.waterview.animation.CenterEvaluator;
import com.cl.common_base.widget.waterview.bean.Water;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import kotlin.Pair;

/**
 * @author wodx521
 * @date on 2018/10/19
 */
public class WaterView extends FrameLayout {
    private LayoutInflater mInflater;
    private int mChildViewRes = R.layout.water_item;
    public static int viewWidth;
    public static int viewHeight;
    private List<View> mViews = new ArrayList<>();
    private List<Integer> randomList = new ArrayList<>();
    private List<Point> points = new LinkedList<>();
    //矩形随机布置
    public static final int RECTANGLE_RANDOM = 100;
    //居中圆形随机布置
    public static final int CIRCLE_RANDOM = 101;
    // 扇形居中排列布置
    public static final int FAN_RANDOM = 102;
    //view缩放动画
    public static final int SCALE = 102;
    //view上下平移动画
    public static final int TRANSLATE = 103;
    //显示动画时间
    public static final int ANIMATION_SHOW_VIEW_DURATION = 500;
    //移除动画时间
    public static final int REMOVE_DELAY_MILLIS = 2000;
    public static final int VIEW_RIGHT_TOP = 200;
    //结束点右下角
    public static final int VIEW_RIGHT_BOTTOM = 201;
    //结束点左上角
    public static final int VIEW_LEFT_TOP = 202;
    //结束点左下角
    public static final int VIEW_LEFT_BOTTOM = 203;
    //结束view上方
    public static final int VIEW_TOP = 204;
    //结束view下方
    public static final int VIEW_BOTTOM = 205;
    //结束view中心位置
    public static final int VIEW_CENTER = 206;
    //自缩放
    public static final int VIEW_SELF = 207;
    public static final int BACKGROUND = 208;
    public static final int DRAWABLE_TOP = 209;
    private int right;
    private int bottom;
    private int left;
    private int top;
    private Point mDestroyPoint;
    private float mMaxSpace;
    //布局样式
    private int mLayoutStyle;
    //水滴视图的布局
    private int angle;
    //消失点
    private int destroyPoint;
    //视图动画
    private int viewAnimation;
    private int mImageRes;
    private TimeInterpolator mInterpolator = new LinearInterpolator();
    private int drawablePosition;
    private int textColor;
    private int textSize;

    public WaterView(@NonNull Context context) {
        this(context, null);
    }

    public WaterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(context);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.WaterView);
        mLayoutStyle = attributes.getInt(R.styleable.WaterView_layout_style, RECTANGLE_RANDOM);
        angle = attributes.getInt(R.styleable.WaterView_angle, 1);
        destroyPoint = attributes.getInt(R.styleable.WaterView_destroy_point, VIEW_LEFT_BOTTOM);
        viewAnimation = attributes.getInt(R.styleable.WaterView_view_animation, SCALE);
        mImageRes = attributes.getResourceId(R.styleable.WaterView_imageRes, R.drawable.shape_view);
        drawablePosition = attributes.getInt(R.styleable.WaterView_drawable_position, BACKGROUND);
        textColor = attributes.getColor(R.styleable.WaterView_text_color, Color.WHITE);
        textSize = attributes.getInt(R.styleable.WaterView_text_size, 18);
        attributes.recycle();
    }

    //初始化时调用一次,获取控件宽高
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        mMaxSpace = (float) Math.sqrt(w * w + h * h);
        right = getRight();
        bottom = getBottom();
        left = getLeft();
        top = getTop();
    }

    /**
     * 设置水滴view的消失点
     *
     * @param destroyPoint 消失点位置
     */
    public void setDestroyPoint(int destroyPoint) {
        this.destroyPoint = destroyPoint;
    }

    /**
     * 设置view显示的动画
     *
     * @param viewAnimation view动画
     */
    public void setViewAnimation(int viewAnimation) {
        this.viewAnimation = viewAnimation;
    }

    /**
     * 设置view的布局样式
     *
     * @param layoutStyle view布局样式
     */
    public void setLayoutStyle(int layoutStyle) {
        this.mLayoutStyle = layoutStyle;
    }

    public void setDrawablePosition(int drawablePosition) {
        this.drawablePosition = drawablePosition;
    }

    /**
     * 设置view上的图片
     *
     * @param imageRes view上图片
     */
    public void setImageRes(int imageRes) {
        this.mImageRes = imageRes;
    }

    public void setViewInterpolator(TimeInterpolator value) {
        if (value != null) {
            mInterpolator = value;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mInterpolator = new LinearInterpolator();
            }
        }
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    //页面销毁时,移除动画
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearView();
    }

    /**
     * 设置添加水滴
     *
     * @param waterList 水滴数据集合
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setWaters(List<Water> waterList) {
        clearView();
        //确保初始化完成
        post(() -> addWaterView(waterList));
    }

    /**
     * 水滴view的数据集合
     *
     * @param waters 水滴数据集合
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void addWaterView(List<Water> waters) {
        try {
            if (null == waters || waters.isEmpty()) return;
            for (int i = 0; i < waters.size(); i++) {
                Water water = waters.get(i);
                View waterView = mInflater.inflate(mChildViewRes, this, false);
                TextView tvWater = waterView.findViewById(R.id.tv_water);

                TextView tvNumber = waterView.findViewById(R.id.tv_numbeasd);
                TextView tvTip = waterView.findViewById(R.id.tv_tip);
                LinearLayout llTop = waterView.findViewById(R.id.ll_top);

                waterView.setTag(water);
                switch (drawablePosition) {
                    case BACKGROUND:
                        llTop.setVisibility(GONE);
                        tvWater.setVisibility(VISIBLE);
                        tvWater.setBackgroundResource(mImageRes);
                        tvWater.setText(water.getNumber());
                        tvWater.setTextColor(textColor);
                        tvWater.setTextSize(textSize);
                        break;
                    case DRAWABLE_TOP:
                        /*tvWater.setCompoundDrawablesRelativeWithIntrinsicBounds(0, mImageRes, 0, 0);*/
                        tvWater.setVisibility(GONE);
                        llTop.setVisibility(VISIBLE);
                        tvNumber.setBackgroundResource(mImageRes);
                        tvNumber.setText(water.getNumber());
                        String name = water.getName();
                        if (name.contains(" ")) {
                            String[] s = name.split(" ");
                            String ss;
                            for (String s1 : s) {
                                ss = s1 + "\n";
                                tvTip.append(ss);
                            }
                        } else {
                            if (!TextUtils.isEmpty(name)) {
                                tvTip.append(name);
                            }
                        }
                        break;
                }
                int finalI = i;

                waterView.setOnClickListener(view -> {
                    if (mClickListener != null) {
                        view.setClickable(false);
                        mClickListener.clickListener(view, finalI, water, mViews);
                    }
                });
                mViews.add(waterView);
            }
            if (mViews.isEmpty()) return;
            //生成区域内所有的点集合
            switch (mLayoutStyle) {
                /*case RECTANGLE_RANDOM:
                    initPointsRec(left, right, top, bottom);
                    setChildViewLocation(mViews);
                    addShowViewAnimation();
                    break;
                case CIRCLE_RANDOM:
                    initPointsCircular(angle);
                    setChildViewCircleLocation(mViews);
                    addShowViewAnimation();
                    break;*/
                case FAN_RANDOM:
                    double angle = (double) 180 / mViews.size();
                    Rect rect = new Rect(-(left * 3), 0, getWidth(), getHeight());
                    double startAngle = Math.asin((double) getHeight() / (2 * ((double) getWidth() / 2) + getHeight())) * (180 + angle) / Math.PI;
                    double sweepAngle = 180.0 - angle / 2 - 2 * startAngle;
                    // 防止ANR
                    setViewLocation(rect, startAngle, sweepAngle, mViews.size(), angle);
                    break;
            }
        } catch (Exception e) {
            Pair<String, String> pair1 = new Pair<>("addWaterView", e.toString());
            Pair[] options = new Pair[]{pair1};
            Reporter.reportError(Reporter.ErrorType.ApiError, e.toString(), options);
        }
    }

    private void setViewLocation(Rect rect, double startAngle, double sweepAngle, int size, double angle) {
        new Thread(() -> {
            generateClockwiseArcPoints(rect, startAngle, sweepAngle, size, angle);
            if (null == points || points.isEmpty()) return;
            if (null == mViews || mViews.isEmpty()) return;
            for (int i = 0; i < mViews.size(); i++) {
                View view = mViews.get(i);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    final int index = i; // 将i的值复制到最终的局部变量
                    view.post(() -> {
                        view.setX(points.get(index).x);
                        view.setY(points.get(index).y);
                    });
                }
            }

            // 使用Handler在主线程中执行UI更新操作
            Handler mainHandler = new Handler(getContext().getMainLooper());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mainHandler.post(() -> addShowViewAnimation(mViews));
            }
        }).start();
    }


    /**
     * 全局随机生成
     *
     * @param left   此视图相对于其父级的左部位置。
     * @param right  此视图相对于其父级的右部位置。
     * @param top    此视图相对于其父级的上部位置。
     * @param bottom 此视图相对于其父级的底部位置。
     */
    private void initPointsRec(int left, int right, int top, int bottom) {
        points.clear();
        List<Integer> widthList = new ArrayList<>();
        List<Integer> heightList = new ArrayList<>();
        for (View mView : mViews) {
            mView.measure(0, 0);
            int measuredHeight = mView.getMeasuredHeight();
            int measuredWidth = mView.getMeasuredWidth();
            widthList.add(measuredWidth);
            heightList.add(measuredHeight);
        }
        Collections.sort(widthList);
        Collections.sort(heightList);
        int maxWidth = widthList.get(widthList.size() - 1);
        int maxHeight = heightList.get(heightList.size() - 1);
        for (int i = 0; i < right - left - maxWidth; i += (maxWidth / 2)) {
            for (int j = 0; j < bottom - top - maxHeight; j += (maxHeight / 3)) {
                points.add(new Point(i, j));
            }
        }
    }

    /**
     * 以中心随机生成
     *
     * @param interval 水滴视图间隔角度
     */
    private void initPointsCircular(int interval) {
        points.clear();
        List<Integer> widthList = new ArrayList<>();
        List<Integer> heightList = new ArrayList<>();
        for (View mView : mViews) {
            mView.measure(0, 0);
            int measuredHeight = mView.getMeasuredHeight();
            int measuredWidth = mView.getMeasuredWidth();
            widthList.add(measuredWidth);
            heightList.add(measuredHeight);
        }
        Collections.sort(widthList);
        Collections.sort(heightList);
        int maxWidth = widthList.get(widthList.size() - 1);
        int maxHeight = heightList.get(heightList.size() - 1);

        if (viewWidth > viewHeight) {
            for (int i = 0; i < 360; i += interval) {
                int x = (int) ((right - maxWidth) / 2 + (viewHeight / 2 - maxHeight / 2) * Math.cos(Math.PI * (i - 90) / 180));
                int y = (int) ((bottom - maxHeight) / 2 - (viewHeight / 2 - maxHeight / 2) * Math.sin(Math.PI * (i - 90) / 180));
                points.add(new Point(x, y));
            }
        } else {
            for (int i = 0; i < 360; i += interval) {
                int x = (int) ((right - maxWidth) / 2 + (viewWidth / 2 - maxWidth / 2) * Math.cos(Math.PI * (i - 90) / 180));
                int y = (int) ((bottom - maxHeight) / 2 - (viewWidth / 2 - maxWidth / 2) * Math.sin(Math.PI * (i - 90) / 180));
                points.add(new Point(x, y));
            }
        }

    }

    /**
     * 设置view在父控件中的位置
     *
     * @param viewList view集合
     */
    private void setChildViewLocation(List<View> viewList) {
        while (randomList.size() < viewList.size()) {
            int newPoint = (int) (Math.random() * points.size());
            if (randomList.size() > 0) {
                for (int i = 0; i < randomList.size(); i++) {
                    Integer integer = randomList.get(i);
                    if (Math.abs(integer - newPoint) > 2) {
                        if (i == randomList.size() - 1) {
                            randomList.add(newPoint);
                        }
                    } else {
                        break;
                    }
                }
            } else {
                randomList.add(newPoint);
            }
        }

        for (int i = 0; i < viewList.size(); i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                viewList.get(i).setY(points.get(randomList.get(i)).y);
                viewList.get(i).setX(points.get(randomList.get(i)).x);
            }
        }
    }

    /**
     * 设置view在父控件中的位置
     *
     * @param viewList view集合
     */
    private void setChildViewCircleLocation(List<View> viewList) {
        while (randomList.size() < viewList.size()) {
            int v = (int) (Math.random() * points.size());
            if (!randomList.contains(v)) {
                randomList.add(v);
            }
        }
        for (int i = 0; i < viewList.size(); i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                viewList.get(i).setY(points.get(i).y);
                viewList.get(i).setX(points.get(i).x);
            }
        }
    }

    /**
     * 添加显示动画
     *
     * @param viewList 视图中view集合
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void addShowViewAnimation(List<View> viewList) {
        try {
            for (View view : viewList) {
                // 检查View是否已经有父控件
                if (view.getParent() != null) {
                    ((ViewGroup) view.getParent()).removeView(view);  // 从旧的父控件中移除
                }

                addView(view);

                //view出现的动画
                view.setAlpha(0);
                view.setScaleX(0);
                view.setScaleY(0);
                view.animate().alpha(0.8f).scaleX(0.8f).scaleY(0.8f).setDuration(ANIMATION_SHOW_VIEW_DURATION).start();

                //view显示后的动画
                float x = view.getX();
                float y = view.getY();
                view.measure(0, 0);
                int measuredHeight = view.getMeasuredHeight();
                int measuredWidth = view.getMeasuredWidth();
                switch (viewAnimation) {
                    case SCALE:
                        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f, x + measuredWidth / 2, y + measuredHeight / 2);
                        scaleAnimation.setRepeatMode(Animation.REVERSE);
                        scaleAnimation.setDuration(1000);
                        scaleAnimation.setRepeatCount(-1);
                        view.startAnimation(scaleAnimation);
                        break;
                    case TRANSLATE:
                        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, 10);
                        translateAnimation.setRepeatMode(Animation.REVERSE);
                        translateAnimation.setDuration(1000);
                        translateAnimation.setRepeatCount(-1);
                        view.startAnimation(translateAnimation);
                        break;
                }
            }
        } catch (Exception e) {
        }
    }


    /**
     * 移除动画view
     *
     * @param view 需要移除的view
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void animRemoveView(View view, int position) {
        try {
            float x = view.getX();
            float y = view.getY();
            Point point = new Point((int) x, (int) y);
            view.measure(0, 0);
            int measuredHeight = view.getMeasuredHeight();
            int measuredWidth = view.getMeasuredWidth();
            ValueAnimator animator;
            float distance;
            switch (destroyPoint) {
                case VIEW_RIGHT_TOP:
                    mDestroyPoint = new Point(viewWidth, 0);
                    break;
                case VIEW_LEFT_TOP:
                    mDestroyPoint = new Point(0, 0);
                    break;
                case VIEW_RIGHT_BOTTOM:
                    mDestroyPoint = new Point(viewWidth, viewHeight);
                    break;
                case VIEW_LEFT_BOTTOM:
                    mDestroyPoint = new Point(0, viewHeight);
                    break;
                case VIEW_TOP:
                    mDestroyPoint = new Point((int) x, 0);
                    break;
                case VIEW_BOTTOM:
                    mDestroyPoint = new Point((int) x, viewHeight);
                    break;
                case VIEW_CENTER:
                    mDestroyPoint = new Point((viewWidth - measuredWidth) / 2, (viewHeight - measuredHeight) / 2);
                    break;
                case VIEW_SELF:
                    mDestroyPoint = new Point((int) x, (int) y);
                    break;
            }
            distance = getDistance(point, mDestroyPoint);
            int i = measuredHeight / 2;
            if (distance >= measuredHeight) {
                animator = ValueAnimator.ofObject(new CenterEvaluator(), point, mDestroyPoint);
                animator.setDuration((long) (REMOVE_DELAY_MILLIS / mMaxSpace * distance));
                animator.addUpdateListener(valueAnimator -> {
                    Point currentPoint = (Point) valueAnimator.getAnimatedValue();
                    float alpha = getDistance(currentPoint, mDestroyPoint) / distance;
                    view.setX(currentPoint.x);
                    view.setY(currentPoint.y);
                    // 设置相对于屏幕的原点
                    if (alpha <= 0.9) {
                        view.setAlpha(alpha);
                        view.setScaleX(alpha);
                        view.setScaleY(alpha);
                    }
                });
            } else {
                animator = ValueAnimator.ofFloat(0.8f, 0);
                animator.setDuration((long) (REMOVE_DELAY_MILLIS / mMaxSpace * measuredHeight));
                animator.addUpdateListener(valueAnimator -> {
                    float currentValue = (float) valueAnimator.getAnimatedValue();
                    // 设置相对于屏幕的原点
                    view.setAlpha(currentValue);
                    view.setScaleX(currentValue);
                    view.setScaleY(currentValue);
                });
            }
            animator.setInterpolator(mInterpolator);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //结束时从容器移除水滴
                    view.clearAnimation();
                    removeView(view);
                }
            });
            animator.start();
        } catch (Exception e) {
        }
    }

    /**
     * 两点间距离
     *
     * @param p1 第一个点
     * @param p2 第二个点
     * @return 返回两点间的距离
     */
    private float getDistance(Point p1, Point p2) {
        float xDifference = Math.abs(p2.x - p1.x);
        float yDifference = Math.abs(p2.y - p1.y);
        return (float) Math.sqrt(xDifference * xDifference + yDifference * yDifference);
    }

    /**
     * 刷新视图时清空视图
     */
    private void clearView() {
        if (mViews != null && mViews.size() > 0) {
            for (View mView : mViews) {
                mView.clearAnimation();
            }
            mViews.clear();
        }
        randomList.clear();
        removeAllViews();
    }

    public interface ClickListener {
        void clickListener(View view, int finalI, Water water, List<View> mViews);
    }

    public ClickListener mClickListener;

    public void setClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }


    public List<Point> generateClockwiseSectorPoints(Rect rect, float startAngle, float sweepAngle, int interval) {
        points.clear();
        int centerX = rect.centerX();
        int centerY = rect.centerY();
        int radius = Math.min(rect.width(), rect.height()) / 2;
        int endAngle = (int) (startAngle + sweepAngle);
        for (int i = (int) startAngle; i <= endAngle; i += interval) {
            double radians = Math.toRadians(i);
            int x = (int) (centerX + radius * Math.cos(radians));
            int y = (int) (centerY + radius * Math.sin(radians));
            points.add(new Point(x, y));
        }
        points.remove(0);
        return points;
    }

    public List<Point> generateClockwiseArcPoints(Rect rect, double startAngle, double sweepAngle, int numPoints, double angle) {
        points.clear();
        int centerX = rect.left + rect.width() / 2;
        int centerY = rect.top + rect.height() / 2;
        int radius = Math.min(rect.width(), rect.height()) / 2;

        float angleInterval = (float) (sweepAngle / (numPoints - 1));

        for (int i = 0; i < numPoints; i++) {
            double radians = Math.toRadians((180 + angle / 3) + startAngle + angleInterval * i);
            int x = (int) (centerX + radius * Math.cos(radians));
            int y = (int) (centerY + radius * Math.sin(radians));
            Point point = new Point(x, y);
            if (rect.contains(point.x, point.y)) {
                points.add(point);
            }
        }
        return points;
    }

}
