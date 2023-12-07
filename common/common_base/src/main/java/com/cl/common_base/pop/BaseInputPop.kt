package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseInputPopBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * 输入弹窗
 */
class BaseInputPop(context: Context, private val title: String? = null, private val hintText: String? = null, private val onConfirmAction: ((String) -> Unit) ? = null): CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.base_input_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseInputPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@BaseInputPop
            executePendingBindings()

            tvReport.text = title
            etEmail.setText(hintText)
            tvConfirm.setOnClickListener {
                onConfirmAction?.invoke(etEmail.text.toString())
                dismiss()
            }
            tvCancel.setOnClickListener { dismiss() }
        }
    }
}