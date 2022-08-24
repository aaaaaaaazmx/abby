package com.cl.modules_my.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MySettingDeleteBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 删除设备弹窗
 *
 * @author 李志军 2022-08-03 14:37
 */
class SettingDeleteAbbyOutPop(
    context: Context,
    private val onConFirmAction: (() -> Unit)? = null,
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_setting_delete
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MySettingDeleteBinding>(popupImplView)?.apply {
            executePendingBindings()
            tvConfirm.setOnClickListener {
                dismiss()
                onConFirmAction?.invoke()
            }
            tvCancel.setOnClickListener { dismiss() }
        }
    }
}