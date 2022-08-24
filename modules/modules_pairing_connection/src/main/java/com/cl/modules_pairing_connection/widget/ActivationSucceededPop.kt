package com.cl.modules_pairing_connection.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_pairing_connection.R
import com.cl.modules_pairing_connection.databinding.PairActivationSuccessBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 激活成功.
 *
 * @author 李志军 2022-08-19 17:49
 */
class ActivationSucceededPop(
    context: Context,
    private val onConfirmAction: (() -> Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pair_activation_success
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<PairActivationSuccessBinding>(popupImplView)?.apply {
            tvConfirm.setOnClickListener {
                onConfirmAction?.invoke()
                dismiss()
            }
        }
    }
}