package com.cl.modules_my.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyLoginOutBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 退出确认框
 */
class LoginOutPop(
    context: Context,
    private val onConFirmAction: (() -> Unit)? = null,
    ) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_login_out
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyLoginOutBinding>(popupImplView)?.apply {
            executePendingBindings()
            tvConfirm.setOnClickListener {
                dismiss()
                onConFirmAction?.invoke()
            }
            tvCancel.setOnClickListener { dismiss() }
        }
    }
}