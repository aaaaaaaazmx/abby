package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseChooseTimerBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 单个时间选择弹窗
 */
class ChooseTimerPop(
    context: Context,
    private val onConfirmAction: ((value: Int) -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null,
    private val time: Int = 7, // 传进来的时间，24小时制
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_choose_timer
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseChooseTimerBinding>(popupImplView)?.apply {
            tvConfirm.setOnClickListener {
                // 拿到的值需要自己判断当前是华氏度还是摄氏度，需要自己去转换
                onConfirmAction?.invoke(tpTime.hour)
                dismiss()
            }
            tvCancel.setOnClickListener {
                onCancelAction?.invoke()
                dismiss()
            }
            // 默认选中
            tpTime.set24Hour()
            tpTime.setSelectedScope(time, false)
        }
    }

}