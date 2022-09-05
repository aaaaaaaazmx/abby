package com.cl.common_base.pop

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseUpdateSuccessPopBinding
import com.cl.common_base.ext.setVisible
import com.lxj.xpopup.core.CenterPopupView

class SendEmailTipsPop(
    context: Context,
    val onConfirmAction: (() -> Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_update_success_pop
    }

    override fun onCreate() {
        DataBindingUtil.bind<BaseUpdateSuccessPopBinding>(popupImplView)?.apply {
            ivPic.visibility = View.GONE
            tvContent.setTextColor(Color.parseColor("#000000"))
            tvContent.text = "Please email us at growsupport@heyabby.com. The email is already copied to your clipboard."
            tvConfirm.setOnClickListener {
                dismiss()
                onConfirmAction?.invoke()
            }
        }
    }
}