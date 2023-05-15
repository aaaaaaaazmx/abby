package com.cl.common_base.refresh

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.scwang.smart.drawable.ProgressDrawable
import com.scwang.smart.refresh.classics.ArrowDrawable
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.scwang.smart.refresh.layout.util.SmartUtil

@SuppressLint("RestrictedApi")
@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
class ClassicsHeader : LinearLayout, RefreshHeader {

    private var mHeaderText //标题文本
            : TextView? = null
    private var mArrowView //下拉箭头
            : ImageView? = null
    private var mProgressView //刷新动画视图
            : ImageView? = null
    private var mProgressDrawable //刷新动画
            : ProgressDrawable? = null
    
    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    constructor(context: Context) : this(context, null)
    
    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {
        gravity = Gravity.CENTER
        mHeaderText = TextView(context)
        mProgressDrawable = ProgressDrawable()
        mArrowView = ImageView(context)
        mProgressView = ImageView(context)
        mProgressView?.setImageDrawable(mProgressDrawable)
        mArrowView?.setImageDrawable(ArrowDrawable())
        addView(mProgressView, SmartUtil.dp2px(20f), SmartUtil.dp2px(20f))
        addView(mArrowView, SmartUtil.dp2px(20f), SmartUtil.dp2px(20f))
        addView(Space(context), SmartUtil.dp2px(20f), SmartUtil.dp2px(20f))
        addView(mHeaderText, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        minimumHeight = SmartUtil.dp2px(60f)
    }
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)


    @RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {
        when (newState) {
            RefreshState.None, RefreshState.PullDownToRefresh -> {
                // mHeaderText?.text = "下拉开始刷新"
                mArrowView?.visibility = VISIBLE //显示下拉箭头
                mProgressView?.visibility = GONE //隐藏动画
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    mArrowView?.animate()?.rotation(0f)
                } //还原箭头方向
            }

            RefreshState.Refreshing -> {
                // mHeaderText?.text = "正在刷新"
                mProgressView?.visibility = VISIBLE //显示加载动画
                mArrowView?.visibility = GONE //隐藏箭头
            }

            RefreshState.ReleaseToRefresh -> {
                // mHeaderText?.text = "释放立即刷新"
                mArrowView?.animate()?.rotation(180f) //显示箭头改为朝上
            }

            else -> {}
        }
    }

    override fun getView(): View {
        return this
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun setPrimaryColors(vararg colors: Int) {
    }

    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) {
    }

    override fun onMoving(isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) {
    }

    override fun onReleased(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
    }

    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
        mProgressDrawable?.start()
    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        mProgressDrawable?.stop() //停止动画

        mProgressView?.visibility = GONE //隐藏动画

        if (success) {
            // mHeaderText?.text = "刷新完成"
        } else {
            // mHeaderText?.text = "刷新失败"
        }
        return 0 //延迟0毫秒之后再弹回、立即返回
    }

    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {
    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }
}