package com.cl.common_base.pop

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.MyFirmwareUpdateBinding
import com.cl.common_base.ext.dp2px
import com.google.firebase.messaging.CommonNotificationBuilder
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.tuya.smart.android.device.bean.UpgradeInfoBean

/**
 * 固件升级弹窗
 *
 * @author 李志军 2022-08-17 10:28
 */
class FirmwareUpdatePop(
    context: Context,
    var update: UpgradeInfoBean? = null,
    val onConfirmAction: (()->Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_firmware_update
    }

    fun setData(update: UpgradeInfoBean) {
        this.update = update
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyFirmwareUpdateBinding>(popupImplView)?.apply {
            update?.let { update ->
                //0：App 提醒升级
                //2：App 强制升级
                //3：检测升级
                when (update.upgradeType) {
                    0 -> {
                        ivImg.background =
                            ContextCompat.getDrawable(context, R.mipmap.my_remind_update)
                        tvContent.text =
                            "To ensure a good user experience, please upgrade your device firmware in time."
                    }
                    2 -> {
                        ivImg.background =
                            ContextCompat.getDrawable(context, R.mipmap.my_force_update)
                        tvContent.text =
                            "Ensure a better user experience, please upgrade the firmware immediately."
                    }
                    3 -> {
                        ivImg.background =
                            ContextCompat.getDrawable(context, R.mipmap.my_remind_update)
                        tvContent.text =
                            "To ensure a good user experience, please upgrade your device firmware in time."
                    }
                }
            }

            tvConfirm.setOnClickListener {
                onConfirmAction?.invoke()
                dismiss()
            }
            tvCancel.setOnClickListener { dismiss() }
        }
    }
}