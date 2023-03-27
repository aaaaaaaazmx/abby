package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.MyTimePickerPopBinding
import com.lxj.xpopup.core.CenterPopupView

class TimePickerPop(
    context: Context,
    private val chooseTime: Int? = null,
    private val onConfirmAction: ((time: String, timeMis: Long) -> Unit)? = null,
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_time_picker_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyTimePickerPopBinding>(popupImplView)?.apply {
            if (chooseTime != null) {
                tpTime.setTime(chooseTime, 0, false)
            }

            tvConfirm.setOnClickListener {
                onConfirmAction?.invoke("${tpTime.hour}", 0)
                dismiss()
            }

            tvCancel.setOnClickListener {
                dismiss()
            }
        }
    }
}