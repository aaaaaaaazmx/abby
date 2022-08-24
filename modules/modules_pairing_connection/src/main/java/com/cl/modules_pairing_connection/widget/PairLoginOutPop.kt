package com.cl.modules_pairing_connection.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_pairing_connection.R
import com.cl.modules_pairing_connection.databinding.PairLoginOutBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 退出登录弹窗
 *
 * @author 李志军 2022-08-03 14:37
 */
class PairLoginOutPop(
    context: Context,
    private val onConFirmAction: (() -> Unit)? = null,
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pair_login_out
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<PairLoginOutBinding>(popupImplView)?.apply {
            executePendingBindings()
            tvConfirm.setOnClickListener {
                dismiss()
                onConFirmAction?.invoke()
            }
            tvCancel.setOnClickListener { dismiss() }
        }
    }
}