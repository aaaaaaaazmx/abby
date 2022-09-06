package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseLocationEnableBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 开启定位弹窗
 *
 * @author 李志军 2022-08-03 14:37
 */
class PairLocationPop(
    context: Context,
    private val onConFirmAction: (() -> Unit)? = null,
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_location_enable
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseLocationEnableBinding>(popupImplView)?.apply {
            executePendingBindings()
            tvContent.text = context.getString(R.string.permission_location_tips)
            tvConfirm.setOnClickListener {
                dismiss()
                onConFirmAction?.invoke()
            }
            tvCancel.setOnClickListener { dismiss() }
        }
    }
}