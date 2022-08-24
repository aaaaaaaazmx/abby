package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.databinding.BasePumpWaterFinishedPopBinding
import com.lxj.xpopup.core.BottomPopupView


/**
 * 排水结束弹窗
 *
 * @author 李志军 2022-08-10 15:06
 */
class BasePumpWaterFinishedPop(
    context: Context,
    private val onSuccessAction: (() -> Unit)? = null,
    private var data: MutableList<AdvertisingData>? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_pump_water_finished_pop
    }

    var binding: BasePumpWaterFinishedPopBinding? = null
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<BasePumpWaterFinishedPopBinding>(popupImplView)?.apply {
            btnSuccess.setOnClickListener {
                onSuccessAction?.invoke()
                dismiss()
            }
        }
    }
}