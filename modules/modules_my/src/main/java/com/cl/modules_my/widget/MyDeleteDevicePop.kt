package com.cl.modules_my.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyDeleteDevicePopBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 *
 * 删除设备弹窗
 * @author 李志军 2022-08-12 12:33
 */
class MyDeleteDevicePop(
    context: Context,
    private val onNextAction: (() -> Unit)? = null
) : BottomPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.my_delete_device_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyDeleteDevicePopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                dismiss()
                onNextAction?.invoke()
            }

            rlCheck.setOnClickListener {
                val isCheck = cbBox.isChecked
                cbBox.isChecked = !isCheck
                btnSuccess.isEnabled = !isCheck
            }

            cbBox.setOnCheckedChangeListener { _, isChecked ->
                btnSuccess.isEnabled = isChecked
                cbBox.isChecked = isChecked
            }

        }
    }
}