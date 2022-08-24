package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomePlantSixPopBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 * plant6
 *
 * @author 李志军 2022-08-06 17:35
 */
class HomePlantSixPop(
    context: Context,
    private val onNextAction: (() -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_six_pop
    }

    private var binding: HomePlantSixPopBinding? = null

    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantSixPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                onNextAction?.invoke()
                dismiss()
            }
        }
    }
}

