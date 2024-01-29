package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeFanBottomPopBinding
import com.cl.common_base.constants.Constants
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.google.gson.annotations.Until
import com.lxj.xpopup.core.BottomPopupView

class HomeFanBottonPop(context: Context, val remindMeAction: (()->Unit)? = null, val benOKAction: (()->Unit) ?= null): BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_fan_bottom_pop
    }

    private val isSHowRemindMe by lazy {
        // false表示展示，true表示不展示。
        Prefs.getBoolean(Constants.Global.KEY_IS_SHOW_FAN_CLOSE_TIP, false)
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<HomeFanBottomPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@HomeFanBottonPop
            executePendingBindings()

            // 是否展示不再提醒
            ViewUtils.setVisible(!isSHowRemindMe, tvNotRemind)

            btnOk.setOnClickListener {
                if (tvNotRemind.isChecked) {
                    // 不再提醒
                    Prefs.putBooleanAsync(Constants.Global.KEY_IS_SHOW_FAN_CLOSE_TIP, true)
                }
                benOKAction?.invoke()
                dismiss()
            }
        }
    }
}