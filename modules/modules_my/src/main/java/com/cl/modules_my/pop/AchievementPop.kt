package com.cl.modules_my.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyPopAchievementBinding
import com.lxj.xpopup.core.BottomPopupView

class AchievementPop(context: Context, private val url: String, private val addAction: (()->Unit)? = null ): BottomPopupView(context) {

        override fun getImplLayoutId(): Int {
            return R.layout.my_pop_achievement
        }

        override fun onCreate() {
            super.onCreate()
            DataBindingUtil.bind<MyPopAchievementBinding>(popupImplView)?.apply {
                lifecycleOwner = this@AchievementPop
                data = url
                executePendingBindings()

                tvAdd.setOnClickListener {
                    addAction?.invoke()
                    dismiss()
                }
            }
        }
}