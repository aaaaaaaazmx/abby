package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeTimeLapserDestroyPopBinding
import com.lxj.xpopup.core.BubbleAttachPopupView

class HomeTimeLapseDestroyPop(context: Context) : BubbleAttachPopupView(context){
    override fun getImplLayoutId(): Int {
        return R.layout.home_time_lapser_destroy_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeTimeLapserDestroyPopBinding>(popupImplView)?.apply {
        }
    }
}