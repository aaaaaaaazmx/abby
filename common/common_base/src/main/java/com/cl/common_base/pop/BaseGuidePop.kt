package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseBaseGuidePopBinding
import com.lxj.xpopup.core.BubbleAttachPopupView

/**
 * 导航pop
 */
class BaseGuidePop(
    context: Context,
    private val confirmText: String? = null,
    private val onConfirmAction: (() -> Unit)? = null,
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_base_guide_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseBaseGuidePopBinding>(popupImplView)?.apply {
            tvCalendarDesc.text = confirmText
            btnSuccess.setOnClickListener {
                onConfirmAction?.invoke()
                dismiss()
            }
        }
    }
}