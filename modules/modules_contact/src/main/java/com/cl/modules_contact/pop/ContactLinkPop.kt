package com.cl.modules_contact.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactLinkPopBinding
import com.cl.modules_contact.databinding.ContactReportPopBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 输入超链接弹窗
 */
class ContactLinkPop(
    context: Context,
    private val txt: String? = null,
    private val onConfirmAction: ((txt: String) -> Unit)? = null,
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.contact_link_pop
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<ContactLinkPopBinding>(popupImplView)?.apply {
            tvCancel.setOnClickListener {
                dismiss()
            }
            etEmail.setText(txt)

            tvConfirm.setOnClickListener {
                val txt = etEmail.text.toString()
                /*  if (txt.isEmpty()) {
                      ToastUtil.shortShow("Please enter the link")
                      return@setOnClickListener
                  }*/
                onConfirmAction?.invoke(txt)
                dismiss()
            }

        }
    }
}