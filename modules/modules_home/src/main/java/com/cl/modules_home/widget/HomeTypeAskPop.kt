package com.cl.modules_home.widget

import android.content.Context
import com.bbgo.module_home.R
import com.lxj.xpopup.core.CenterPopupView

class HomeTypeAskPop(context: Context): CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_type_ask
    }

    override fun onCreate() {
        super.onCreate()
    }
}