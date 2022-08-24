package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeRerepairBinding
import com.google.gson.annotations.Until
import com.lxj.xpopup.core.CenterPopupView

/**
 * 修改SN弹窗
 *
 * @author 李志军 2022-08-19 16:22
 */
class HomeRepairSnPop(
    context: Context,
    private val onConfirmAction: (() -> Unit)? = null
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_rerepair
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeRerepairBinding>(popupImplView)?.apply {
            tvConfirm.setOnClickListener {
                onConfirmAction?.invoke()
            }
        }
    }
}