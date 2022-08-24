package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.MyFirmwareReplantPopBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 固件强制升级之后，需要解除绑定，
 *
 * @author 李志军 2022-08-22 11:29
 */
class FirmwareReplantPop(
    context: Context,
    private val onConfirmAction: (()->Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_firmware_replant_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyFirmwareReplantPopBinding>(popupImplView)?.apply {
            tvConfirm.setOnClickListener {
                dismiss()
                onConfirmAction?.invoke()
            }
        }
    }
}