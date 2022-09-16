package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.HomeCuringUnlockPopBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 解锁Curing周期的弹窗 ASK点击问号的弹窗
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