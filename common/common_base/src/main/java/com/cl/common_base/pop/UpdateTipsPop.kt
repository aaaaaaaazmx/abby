package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseUpdateTipsPopBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 *  固件升级提示框
 *
 * @author 李志军 2022-08-17 10:55
 */
class UpdateTipsPop(
    context: Context,
    val onConfirmAction: (()->Unit)? = null
) : CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.base_update_tips_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseUpdateTipsPopBinding>(popupImplView)?.apply {
            tvConfirm.setOnClickListener {
                onConfirmAction?.invoke()
                dismiss()
            }
            tvCancel.setOnClickListener { dismiss() }
        }
    }
}