package com.cl.modules_login.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_login.R
import com.cl.modules_login.databinding.PopRetransmissionBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 * 重发邮件弹窗
 */
class RetransmissionPop(
    context: Context,
    private val onAgainAction: (() -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_retransmission
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<PopRetransmissionBinding>(popupImplView)?.apply {
            executePendingBindings()

            tvSend.setOnClickListener {
                onAgainAction?.invoke()
                dismiss()
            }

            tvCancel.setOnClickListener {
                dismiss()
            }
        }
    }
}