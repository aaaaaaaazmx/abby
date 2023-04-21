package com.cl.modules_contact.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactReportPopBinding
import com.lxj.xpopup.core.CenterPopupView

class ContactReportPop(
    context: Context,
    private val onConfirmAction: ((txt: String) -> Unit)? = null,
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.contact_report_pop
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<ContactReportPopBinding>(popupImplView)?.apply {
            tvCancel.setOnClickListener {
                dismiss()
            }

            tvConfirm.setOnClickListener {
                val txt = etEmail.text.toString()
                if (txt.isEmpty()) {
                    ToastUtil.shortShow("Please enter the report content")
                    return@setOnClickListener
                }
                onConfirmAction?.invoke(txt)
                dismiss()
            }

        }
    }
}