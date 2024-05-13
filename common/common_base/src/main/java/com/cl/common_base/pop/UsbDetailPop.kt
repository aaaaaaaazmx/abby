package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.PopUsbDetailBinding
import com.cl.common_base.ext.setSafeOnClickListener
import com.lxj.xpopup.core.CenterPopupView

class UsbDetailPop(context: Context): CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_usb_detail
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<PopUsbDetailBinding>(popupImplView)?.apply {
            ivClose.setSafeOnClickListener { dismiss() }
        }
    }
}
