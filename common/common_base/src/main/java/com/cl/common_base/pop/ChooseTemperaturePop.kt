package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseChooseTemperatureBinding
import com.cl.common_base.widget.wheel.temperature.TemperaturePick
import com.lxj.xpopup.core.CenterPopupView

/**
 * 选择温度弹窗
 */
class ChooseTemperaturePop(
    context: Context,
    private val scope: Int = 0, // 大于小于、默认大于
    private val value: String = "70", // 模式是华氏度
    private val onConfirmAction: ((scope: Int, value: String) -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null,
) : CenterPopupView(context),
    TemperaturePick.OnTempSelectedListener {
    override fun getImplLayoutId(): Int {
        return R.layout.base_choose_temperature
    }

    private var mBinding: BaseChooseTemperatureBinding? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = DataBindingUtil.bind<BaseChooseTemperatureBinding>(popupImplView)?.apply {
            tpTime.setOnTempSelectedListener(this@ChooseTemperaturePop)
            tvConfirm.setOnClickListener {
                // 拿到的值需要自己判断当前是华氏度还是摄氏度，需要自己去转换
                onConfirmAction?.invoke(tpTime.scope, tpTime.value())
                dismiss()
            }
            tvCancel.setOnClickListener {
                onCancelAction?.invoke()
                dismiss()
            }
            // 默认选中
            tpTime.setChooseData(scope, value)
        }
    }

    /**
     * 滑动监听
     */
    override fun onTempSelected(scope: String?, value: String?, unit: String?) {

    }
}