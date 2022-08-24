package com.cl.modules_pairing_connection.widget

import android.content.Context
import com.cl.modules_pairing_connection.R
import com.cl.modules_pairing_connection.databinding.PairPopGuideOpenBleBinding
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