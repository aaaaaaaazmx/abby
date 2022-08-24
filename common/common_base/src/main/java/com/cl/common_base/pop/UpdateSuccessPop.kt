package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseUpdateSuccessPopBinding
import com.google.gson.annotations.Until
import com.lxj.xpopup.core.CenterPopupView

/**
 * 固件升级成功
 *
 * @author 李志军 2022-08-17 16:51
 */
class UpdateSuccessPop(
    context: Context,
    val onConfirmAction: (() -> Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_update_success_pop
    }

    override fun onCreate() {
        DataBindingUtil.bind<BaseUpdateSuccessPopBinding>(popupImplView)?.apply {
            tvConfirm.setOnClickListener {
                dismiss()
                onConfirmAction?.invoke()
            }
        }
    }
}