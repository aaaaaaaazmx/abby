package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeFanFailBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 风扇故障
 *
 * @author 李志军 2022-08-16 15:04
 */
class FanFailPop(
    context: Context,
    private val onConFirmAction: (() -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_fan_fail
    }

    var binding: HomeFanFailBinding? = null
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<HomeFanFailBinding>(popupImplView)?.apply {
            executePendingBindings()
            tvConfirm.setOnClickListener {
                dismiss()
                onConFirmAction?.invoke()
            }
            tvCancel.setOnClickListener {
                dismiss()
                onCancelAction?.invoke()
            }
        }
    }

    fun setData(title: String?) {
        binding?.tvContent?.text = title
    }
}