package com.cl.common_base.pop

import android.content.Context
import com.cl.common_base.R
import com.lxj.xpopup.core.CenterPopupView

/**
 * 时间选择弹窗
 */
class BaseTimeChoosePop(context: Context): CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.base_time_choose_pop
    }

    override fun onCreate() {
        super.onCreate()
    }

}