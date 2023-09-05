package com.cl.abby.ui

import android.animation.ValueAnimator
import android.graphics.drawable.AnimationDrawable
import com.cl.abby.R
import com.cl.abby.databinding.CustomSplashActivityBinding
import com.cl.common_base.base.BaseActivity

class CustomSplashActivity : BaseActivity<CustomSplashActivityBinding>() {

    private val images = intArrayOf(
        com.cl.common_base.R.mipmap.home_week_one,
        com.cl.common_base.R.mipmap.home_week_two,
        com.cl.common_base.R.mipmap.home_week_three,
        com.cl.common_base.R.mipmap.home_week_four,
        com.cl.common_base.R.mipmap.home_week_five,
        com.cl.common_base.R.mipmap.home_week_six,
        com.cl.common_base.R.mipmap.home_week_seven,
        com.cl.common_base.R.mipmap.home_week_eight,
        com.cl.common_base.R.mipmap.home_week_nine,
        com.cl.common_base.R.mipmap.home_week_ten,
        com.cl.common_base.R.mipmap.home_week_eleven,
        com.cl.common_base.R.mipmap.home_week_twelve
    )

    override fun initView() {
        binding.ivAnimation.apply {
            val animator = ValueAnimator.ofInt(0, images.size - 1)
            animator.duration = (images.size * 1000).toLong()
            animator.repeatCount = ValueAnimator.INFINITE

            animator.addUpdateListener { animation ->
                val index = (animation.animatedValue as Int)
                setImageResource(images[index])
            }
            animator.start()
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }
}