package com.cl.common_base.pop

import android.content.Context
import android.view.View
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseCustomBubbleAttachPopupBinding
import com.lxj.xpopup.core.BubbleAttachPopupView

class CustomBubbleAttachPopup(
    context: Context,
    private val easeNumber: Int? = null, // 婚讯啊未读数量
    private val bubbleClickAction: (() -> Unit)? = null
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_custom_bubble_attach_popup
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseCustomBubbleAttachPopupBinding>(popupImplView)?.apply {
            tvSupportNumber.visibility = if ("$easeNumber".isEmpty()) View.GONE else View.VISIBLE
            tvSupportNumber.text = "$easeNumber"

            clRoot.setOnClickListener {
                bubbleClickAction?.invoke()
                dismiss()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return true
    }
}