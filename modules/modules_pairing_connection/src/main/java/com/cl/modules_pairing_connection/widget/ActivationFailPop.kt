package com.cl.modules_pairing_connection.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_pairing_connection.R
import com.cl.modules_pairing_connection.databinding.PairActivationFailBinding
import com.cl.modules_pairing_connection.databinding.PairActivationSuccessBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 激活失败.
 *
 * @author 李志军 2022-08-19 17:49
 */
class ActivationFailPop(
    context: Context,
    private val onTryAction: (() -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pair_activation_fail
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<PairActivationFailBinding>(popupImplView)?.apply {
            tvTry.setOnClickListener {
                onTryAction?.invoke()
                dismiss()
            }
            tvCancel.setOnClickListener {
                dismiss()
                onCancelAction?.invoke()
            }
        }
    }
}