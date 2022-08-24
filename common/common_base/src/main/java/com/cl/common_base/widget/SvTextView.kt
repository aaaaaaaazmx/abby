package com.cl.common_base.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import com.cl.common_base.R


/**
 * 自定义背景View
 * 具体使用参看attrs的注释
 *
 * @version 1.0
 * @since 2021-02-20 11:58r
 */
class SvTextView : AppCompatTextView {

    // 自定义背景
    private var mCustomizeBackground: GradientDrawable? = null

    // 背景填充类型
    private var mSolidType = SolidTypeEnum.SOLIDEnum.type

    // 背景填充色
    private var mSolidColor: ColorStateList? = null

    // 背景填充渐变开始颜色
    private var mGradientStartColor = 0

    // 背景填充渐变结束颜色
    private var mGradientEndColor = 0

    // 渐变方向
    private var mGradientOrientation = GradientOrientationEnum.TOP_BOTTOM.type

    // 背景渐变填充色类型
    private var mGradientType = 0

    // 边框类型
    private var mStrokeType = StrokeTypeEnum.LINE.type

    // 边框色
    private var mStrokeColor: Int = 0

    // 边框粗细
    private var mStrokeWidth = 0

    // 边框类型为虚线时: 虚线宽
    private var mStrokeDashWidth = 0F

    // 边框类型为虚线时: 虚线间距
    private var mStrokeDashGap = 0F

    // 全部弧度
    private var mRadius = 0F

    // 左上角度
    private var mTopLeftRadius = 0F

    // 左下角度
    private var mBottomLeftRadius = 0F

    // 右上角度
    private var mTopRightRadius = 0F

    // 右下角度
    private var mBottomRightRadius = 0F

    constructor(context: Context) : super(context, null)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
        init(context, attrs)
        setCustomizeBackground()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
        setCustomizeBackground()
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SvTextView, 0, 0).apply {
            mSolidType = getInt(R.styleable.SvTextView_svSolidType, SolidTypeEnum.SOLIDEnum.type)
            mSolidColor = getColorStateList(R.styleable.SvTextView_svSolidColor)
            mGradientStartColor = getInt(R.styleable.SvTextView_svGradientStartColor, DEFAULT_COLOR)
            mGradientEndColor = getInt(R.styleable.SvTextView_svGradientEndColor, DEFAULT_COLOR)
            mGradientOrientation = getInt(R.styleable.SvTextView_svGradientOrientation, GradientOrientationEnum.TOP_BOTTOM.type)
            mGradientType = getInt(R.styleable.SvTextView_svGradientType, GradientDrawable.LINEAR_GRADIENT)
            mStrokeType = getInt(R.styleable.SvTextView_svStrokeType, StrokeTypeEnum.LINE.type)
            mStrokeColor = getInt(R.styleable.SvTextView_svStrokeColor, DEFAULT_COLOR)
            mStrokeWidth = getDimension(R.styleable.SvTextView_svStrokeWidth, 0F).toInt()
            mStrokeDashWidth = getDimension(R.styleable.SvTextView_svStrokeDashWidth, 0F)
            mStrokeDashGap = getDimension(R.styleable.SvTextView_svStrokeDashGap, 0F)
            mRadius = getDimension(R.styleable.SvTextView_svRadius, 0F)
            mTopLeftRadius = getDimension(R.styleable.SvTextView_svTopLeftRadius, mRadius)
            mTopRightRadius = getDimension(R.styleable.SvTextView_svTopRightRadius, mRadius)
            mBottomLeftRadius = getDimension(R.styleable.SvTextView_svBottomLeftRadius, mRadius)
            mBottomRightRadius = getDimension(R.styleable.SvTextView_svBottomRightRadius, mRadius)

            recycle()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setCustomizeBackground() {
        getCustomizeBackground().let {
            mCustomizeBackground = it
            background = StateListDrawable().apply { addState(intArrayOf(), it) }
        }
    }

    /**
     * 获取自定义背景
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getCustomizeBackground(): GradientDrawable {
        val radius = floatArrayOf(
            mTopLeftRadius, mTopLeftRadius,
            mTopRightRadius, mTopRightRadius,
            mBottomRightRadius, mBottomRightRadius,
            mBottomLeftRadius, mBottomLeftRadius
        )

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = radius
        }

        setBackgroundColor(drawable)
        setBackgroundStroke(drawable)

        return drawable
    }

    /**
     * 设置背景颜色
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setBackgroundColor(drawable: GradientDrawable) {
        when (mSolidType) {
            // 纯色背景
            SolidTypeEnum.SOLIDEnum.type -> {
                drawable.color = mSolidColor
            }
            // 渐变
            SolidTypeEnum.GRADIENT.type -> {
                drawable.orientation = getGradientOrientation()
                drawable.color = mSolidColor
                drawable.gradientType = mGradientType
                drawable.colors = intArrayOf(mGradientStartColor, mGradientEndColor)
            }
        }
    }

    /**
     * 设置背景描边
     */
    private fun setBackgroundStroke(drawable: GradientDrawable) {
        when (mStrokeType) {
            // 实线
            StrokeTypeEnum.LINE.type -> {
                drawable.setStroke(mStrokeWidth, mStrokeColor)
            }
            // 虚线
            StrokeTypeEnum.DASH.type -> {
                drawable.setStroke(mStrokeWidth, mStrokeColor, mStrokeDashWidth, mStrokeDashGap)
            }
        }
    }

    /**
     * 获取渐变方向.
     */
    private fun getGradientOrientation(): GradientDrawable.Orientation {
        return when (mGradientOrientation) {
            GradientOrientationEnum.TOP_BOTTOM.type -> GradientDrawable.Orientation.TOP_BOTTOM
            GradientOrientationEnum.TR_BL.type -> GradientDrawable.Orientation.TR_BL
            GradientOrientationEnum.RIGHT_LEFT.type -> GradientDrawable.Orientation.RIGHT_LEFT
            GradientOrientationEnum.BR_TL.type -> GradientDrawable.Orientation.BR_TL
            GradientOrientationEnum.BOTTOM_TOP.type -> GradientDrawable.Orientation.BOTTOM_TOP
            GradientOrientationEnum.BL_TR.type -> GradientDrawable.Orientation.BL_TR
            GradientOrientationEnum.LEFT_RIGHT.type -> GradientDrawable.Orientation.LEFT_RIGHT
            GradientOrientationEnum.TL_BR.type -> GradientDrawable.Orientation.TL_BR
            else -> GradientDrawable.Orientation.TOP_BOTTOM
        }
    }

    /**
     * 设置填充色类型
     */

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Suppress("unused")
    fun setSolidType(solidTypeEnum: SolidTypeEnum) {
        mSolidType = solidTypeEnum.type

        setCustomizeBackground()
    }

    /**
     * 设置纯色填充色
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Suppress("unused")
    fun setSolidColor(@ColorInt resId: Int) {
        mSolidColor = ColorStateList.valueOf(resId)

        setCustomizeBackground()
    }

    enum class StrokeTypeEnum(val type: Int) {
        /** 没有 */
        NONE(0),

        /** 实线 */
        LINE(1),

        /** 虚线 */
        DASH(2),
    }

    enum class SolidTypeEnum(val type: Int) {
        /** 背景类型 : 默认 */
        DEFAULT(0),

        /** 背景类型 : 纯色 */
        SOLIDEnum(1),

        /** 背景类型 : 渐变 */
        GRADIENT(2),
    }

    /**
     * 渐变绘制的方向.
     */
    enum class GradientOrientationEnum(val type: Int) {
        /** 从顶部到底部绘制渐变 */
        TOP_BOTTOM(0),

        /** 从右上到左下绘制渐变 */
        TR_BL(1),

        /** 从右向左绘制渐变 */
        RIGHT_LEFT(2),

        /** 从右下到左上绘制渐变 */
        BR_TL(3),

        /** 从底部到顶部绘制渐变 */
        BOTTOM_TOP(4),

        /** 从左下角到右上角绘制渐变 */
        BL_TR(5),

        /** 从左到右绘制渐变 */
        LEFT_RIGHT(6),

        /** 从左上角到右下角绘制渐变 */
        TL_BR(7),
    }

    companion object {
        const val DEFAULT_COLOR = 0x00000000
    }
}