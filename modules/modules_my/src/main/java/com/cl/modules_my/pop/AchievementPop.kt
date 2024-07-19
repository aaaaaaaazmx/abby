package com.cl.modules_my.pop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.cl.common_base.ext.logI
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyPopAchievementBinding
import com.lxj.xpopup.core.BottomPopupView
import kotlin.math.max


class AchievementPop(
    context: Context,
    private val url: String?,
    private val backUrl: String?,
    private val title: String?,
    private val selectStatus: Boolean?,
    private val isGain: Boolean,
    private val addAction: (() -> Unit)? = null
) : BottomPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.my_pop_achievement
    }

    private lateinit var bind: MyPopAchievementBinding
    @SuppressLint("CheckResult", "ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        bind =  DataBindingUtil.bind<MyPopAchievementBinding>(popupImplView)?.apply {
            lifecycleOwner = this@AchievementPop
            data = backUrl
            title = this@AchievementPop.title
            desc = url
            isSelect = selectStatus ?: false
            isGain = this@AchievementPop.isGain

            executePendingBindings()

            tvAdd.setOnClickListener {
                addAction?.invoke()
                dismiss()
            }

            val requestOptions = RequestOptions()
            // 设置 placeholder 和 error
            requestOptions.placeholder(com.cl.common_base.R.mipmap.placeholder)
            requestOptions.error(com.cl.common_base.R.mipmap.errorholder)

            Glide.with(context)
                .load(backUrl)
                .apply(requestOptions)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        // 图片加载失败的处理
                        return false
                    }
                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        if (!this@AchievementPop.isGain) return false
                        ivAddAccessory.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                // 确保只调用一次
                                ivAddAccessory.viewTreeObserver.removeOnGlobalLayoutListener(this)

                                // 现在可以安全地使用宽度和高度
                                val animationSet = AnimationSet(true)
                                animationSet.duration = 800
                                animationSet.repeatCount = 0

                                val rotateAnimation = Rotate3dAnimation(-180f, 0f, ivAddAccessory.width / 2.0f, ivAddAccessory.height / 2.0f, 50f, false)
                                //rotateAnimation.duration = 500 // 初始1秒
                                rotateAnimation.repeatCount = 0

                                val scaleAnimation = ScaleAnimation(
                                    0.5f, 1f, // 开始和结束时缩放大小
                                    0.5f, 1f,
                                    Animation.RELATIVE_TO_SELF, 0.5f,
                                    Animation.RELATIVE_TO_SELF, 0.5f
                                )
                                //scaleAnimation.duration = 500 // 1秒
                                rotateAnimation.repeatCount = 0

                                animationSet.addAnimation(rotateAnimation)
                                animationSet.addAnimation(scaleAnimation)

                                ivAddAccessory.startAnimation(animationSet)
                            }
                        })
                        return false
                    }
                })
                .into(ivAddAccessory)


            ivClose.setOnClickListener { dismiss() }
        }!!
    }

    override fun onDismiss() {
        super.onDismiss()
        bind.ivAddAccessory.clearAnimation()
    }
}