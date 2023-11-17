package com.cl.modules_my.pop

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyPopAchievementBinding
import com.lxj.xpopup.core.BottomPopupView


class AchievementPop(context: Context, private val url: String?, private val backUrl: String?, private val title: String?, private val selectStatus: Boolean?, private val isGain: Boolean,private val addAction: (()->Unit)? = null ): BottomPopupView(context) {

        override fun getImplLayoutId(): Int {
            return R.layout.my_pop_achievement
        }

        @SuppressLint("CheckResult")
        override fun onCreate() {
            super.onCreate()
            DataBindingUtil.bind<MyPopAchievementBinding>(popupImplView)?.apply {
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

                // isGain
                val requestOptions = RequestOptions()
                // 判断是否需要应用灰度转换
                /*if (!this@AchievementPop.isGain) {
                    requestOptions.transform(GrayscaleTransformation())
                }*/
                // 设置 placeholder 和 error
                requestOptions.placeholder(com.cl.common_base.R.mipmap.placeholder)
                requestOptions.error(com.cl.common_base.R.mipmap.errorholder)
                Glide.with(context)
                    .load(backUrl)
                    .apply(requestOptions)
                    .into(ivAddAccessory)


                // 淡入动画
                // 淡入动画
                val fadeIn = ObjectAnimator.ofFloat(ivAddAccessory, "alpha", 0f, 1f)
                fadeIn.setDuration(500)


                // 缩放动画
                val scaleX = ObjectAnimator.ofFloat(ivAddAccessory, "scaleX", 0.5f, 1f)
                val scaleY = ObjectAnimator.ofFloat(ivAddAccessory, "scaleY", 0.5f, 1f)
                scaleX.setDuration(500)
                scaleY.setDuration(500)

                // 组合动画
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(fadeIn, scaleX, scaleY)
                animatorSet.start()

                ivClose.setOnClickListener { dismiss() }
            }
        }
}