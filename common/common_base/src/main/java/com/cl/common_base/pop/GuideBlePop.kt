package com.cl.common_base.pop

import android.content.Context
import com.cl.common_base.R
import com.cl.common_base.databinding.PairPopGuideOpenBleBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 * 引导开启Ble弹窗
 *
 * @author 李志军 2022-08-03 15:14
 */
class GuideBlePop(context: Context): BottomPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.pair_pop_guide_open_ble
    }

    override fun onCreate() {
        super.onCreate()
        PairPopGuideOpenBleBinding.bind(popupImplView).apply {
            executePendingBindings()
            tvOk.setOnClickListener { dismiss() }
        }
    }
}