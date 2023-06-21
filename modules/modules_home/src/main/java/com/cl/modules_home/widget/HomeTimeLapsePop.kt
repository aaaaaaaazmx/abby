package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeTimeLapserPopBinding
import com.cl.common_base.pop.BaseCenterPop
import com.lxj.xpopup.core.CenterPopupView

class HomeTimeLapsePop(
    context: Context,
    private val onConfirmAction: (() -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_time_lapser_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeTimeLapserPopBinding>(popupImplView)?.apply {
            btnNext.setOnClickListener {
                onConfirmAction?.invoke()
                dismiss()
            }
            ivClose.setOnClickListener {
                onCancelAction?.invoke()
                dismiss()
            }
        }
    }
}