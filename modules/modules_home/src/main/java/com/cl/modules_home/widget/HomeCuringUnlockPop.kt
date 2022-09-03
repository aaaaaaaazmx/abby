package com.cl.modules_home.widget

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeCuringUnlockPopBinding
import com.cl.common_base.databinding.BaseUpdateSuccessPopBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 解锁Curing周期的弹窗
 */
class HomeCuringUnlockPop(
    context: Context,
    private val onConfirmAction: (() -> Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_curing_unlock_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeCuringUnlockPopBinding>(popupImplView)?.apply {
            tvConfirm.setOnClickListener {
                dismiss()
                onConfirmAction?.invoke()
            }
        }
    }
}