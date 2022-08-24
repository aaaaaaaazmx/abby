package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomePlantEightPopBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 *
 * plant8
 *
 * @author 李志军 2022-08-06 17:35
 */
class HomePlantEightPop(
    context: Context,
    private val onNextAction: (() -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_eight_pop
    }

    private var binding: HomePlantEightPopBinding? = null
    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantEightPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                onNextAction?.invoke()
                dismiss()
            }
        }
    }
}