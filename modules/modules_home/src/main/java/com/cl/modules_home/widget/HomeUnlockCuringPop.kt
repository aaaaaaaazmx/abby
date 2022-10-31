package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeCuringUnlockBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 最后一个周期解锁弹窗
 */
class HomeUnlockCuringPop(
    context: Context,
    private val onConfirmAction: ((status: String) -> Unit)? = null,
    var periodStatus: String? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_curing_unlock
    }


    /**
     * 周期状态
     */
    fun setPopPeriodStatus(stats: String) {
        this.periodStatus = stats
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeCuringUnlockBinding>(popupImplView)?.apply {
            tvConfirm.setOnClickListener {
                dismiss()
                periodStatus?.let { it1 -> onConfirmAction?.invoke(it1) }
            }

            tvCancel.setOnClickListener{
                dismiss()
            }
        }
    }
}