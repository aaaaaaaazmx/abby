package com.cl.modules_my.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyLoginOutBinding
import com.cl.modules_my.databinding.MySubBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 退出确认框
 */
class SubPop(
    context: Context,
    private val onConFirmAction: (() -> Unit)? = null,
    ) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_sub
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MySubBinding>(popupImplView)?.apply {
            executePendingBindings()
            tvConfirm.setOnClickListener {
                dismiss()
                onConFirmAction?.invoke()
            }
        }
    }
}