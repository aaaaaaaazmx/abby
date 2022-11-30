package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseCustomBubbleAttachPopupBinding
import com.lxj.xpopup.core.BubbleAttachPopupView

class CustomBubbleAttachPopup(context: Context) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_custom_bubble_attach_popup
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseCustomBubbleAttachPopupBinding>(popupImplView)?.apply {

        }
    }
}