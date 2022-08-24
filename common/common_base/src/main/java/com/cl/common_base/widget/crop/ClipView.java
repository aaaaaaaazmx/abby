package com.cl.common_base.widget.crop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * @date on 2019-07-16.
 */
public class ClipView extends View {
    private Paint paint = new Paint();
    //画裁剪区域边框的画笔
    private Paint borderPaint = new Paint();
    //裁剪框水平方向间距
    private float mHorizontalPadding;
    //裁剪框边框宽度
    private int clipBorderWidth;
    //裁剪圆框的半径
    private int clipRadiusWidth;
    //裁剪框矩形宽度
    private int clipWidth;
    //裁剪框类别，（圆形、矩形），默认为圆形
    private ClipType clipType = ClipType.CIRCLE;
    private Xfermode xfermode;
    //指定裁剪框的宽高
    private int aspectX;
    private int aspectY;

    public ClipView(Context context) {
        this(context, null);
    }

    public ClipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //去锯齿
        paint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(clipBorderWidth);
        borderPaint.setAntiAlias(true);
        xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
//                | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
//                | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
        //通过Xfermode的DST_OUT来产生中间的透明裁剪区域，一定要另起一个Layer（层）
        canvas.saveLayer(0, 0, this.getWidth(), this.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        //设置背景
        canvas.drawColor(Color.parseColor("#a8000000"));
        paint.setXfermode(xfermode);
        //绘制圆形裁剪框
        if (clipType == ClipType.CIRCLE) {
            //中间的透明的圆
            canvas.drawCircle(this.getWidth() / 2f, this.getHeight() / 2f, clipRadiusWidth, paint);
            //白色的圆边框
            canvas.drawCircle(this.getWidth() / 2f, this.getHeight() / 2f, clipRadiusWidth, borderPaint);
        } else if (clipType == ClipType.RECTANGLE) { //绘制矩形裁剪框
            //绘制中间的矩形
//            canvas.drawRect(mHorizontalPadding + clipWidth / 4, this.getHeight() / 2 - clipWidth / 2,
//                    this.getWidth() - mHorizontalPadding - clipWidth / 4, this.getHeight() / 2 + clipWidth / 2, paint);
            canvas.drawRect(mHorizontalPadding + clipWidth / 2f - aspectX / 2f, this.getHeight() / 2f - aspectY / 2f,
                    mHorizontalPadding + clipWidth / 2f + aspectX / 2f, this.getHeight() / 2f + aspectY / 2f, paint);
            //绘制白色的矩形边框
            canvas.drawRect(mHorizontalPadding + clipWidth / 2f - aspectX / 2f, this.getHeight() / 2f - aspectY / 2f,
                    mHorizontalPadding + clipWidth / 2f + aspectX / 2f, this.getHeight() / 2f + aspectY / 2f, borderPaint);
        }
        //出栈，恢复到之前的图层，意味着新建的图层会被删除，新建图层上的内容会被绘制到canvas (or the previous layer)
        canvas.restore();
    }

    /**
     * 获取裁剪区域的Rect
     *
     * @return
     */
    public Rect getClipRect1() {
        Rect rect = new Rect();
        //宽度的一半 - 圆的半径
        rect.left = (this.getWidth() / 2 - clipRadiusWidth);
        //宽度的一半 + 圆的半径
        rect.right = (this.getWidth() / 2 + clipRadiusWidth);
        //高度的一半 - 圆的半径
        rect.top = (this.getHeight() / 2 - clipRadiusWidth);
        //高度的一半 + 圆的半径
        rect.bottom = (this.getHeight() / 2 + clipRadiusWidth);
        return rect;
    }

    public Rect getClipRect2() {
        Rect rect = new Rect();
        //宽度的一半 - 矩形宽度的1/4
//        rect.left = (int) (this.getWidth() / 2f - aspectX / 2f);
        rect.left = (this.getWidth() - aspectX) / 2;
        //宽度的一半 + 矩形宽度的1/4
//        rect.right = (int) (this.getWidth() / 2f + aspectX / 2f);
        rect.right = this.getWidth() - rect.left;
        //高度的一半 - 矩形宽度的一半
        rect.top = (this.getHeight() - aspectY) / 2;
        //高度的一半 + 矩形宽度的一半
        rect.bottom = this.getHeight() - rect.top;
        return rect;
    }

    /**
     * 设置裁剪框边框宽度
     *
     * @param clipBorderWidth
     */
    public void setClipBorderWidth(int clipBorderWidth) {
        this.clipBorderWidth = clipBorderWidth;
        borderPaint.setStrokeWidth(clipBorderWidth);
        invalidate();
    }

    /**
     * 设置裁剪框的宽高
     *
     * @param
     */
    public void setClipWidthAndHeight(int width, int height) {
        this.aspectX = width;
        this.aspectY = height;
        invalidate();
    }

    /**
     * 设置裁剪框水平间距
     *
     * @param mHorizontalPadding
     */
    public void setmHorizontalPadding(float mHorizontalPadding) {
        this.mHorizontalPadding = mHorizontalPadding;
        this.clipRadiusWidth = (int) (getScreenWidth(getContext()) - 2 * mHorizontalPadding) / 2;
        this.clipWidth = clipRadiusWidth * 2;
//        this.mHorizontalPadding = (int) (getScreenWidth(getContext()) - 2 * mHorizontalPadding) / 4 + mHorizontalPadding;
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }


    /**
     * 设置裁剪框类别
     *
     * @param clipType
     */
    public void setClipType(ClipType clipType) {
        this.clipType = clipType;
    }

    /**
     * 裁剪框类别，圆形、矩形
     */
    public enum ClipType {
        CIRCLE, RECTANGLE
    }
}
