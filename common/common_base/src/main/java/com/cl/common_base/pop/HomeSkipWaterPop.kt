package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.HomeSkipWaterPopBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 确认跳过加水弹窗
 *
 * @author 李志军 2022-08-18 18:04
 */
class HomeSkipWaterPop(
    context: Context,
    val onConfirmAction: (()->Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_skip_water_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeSkipWaterPopBinding>(popupImplView)?.apply {
            tvConfirm.setOnClickListener {
                dismiss()
                onConfirmAction?.invoke()
            }

            tvCancel.setOnClickListener {
                dismiss()
            }
        }
    }
}