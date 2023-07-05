package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.*
import com.lxj.xpopup.core.BottomPopupView

/**
 * 添加肥料
 *
 * @author 李志军
 */
class HomePlantFeedPop(
    context: Context,
    private val onNextAction: (() -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_feed_pop
    }

    private var binding: HomePlantFeedPopBinding? = null

    override fun onDismiss() {
        super.onDismiss()
        binding?.cbThree?.isChecked = false
    }

    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantFeedPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                onNextAction?.invoke()
                dismiss()
            }
            cbThree.setOnCheckedChangeListener { compoundButton, b ->
                btnSuccess.isEnabled = b
            }
            clThree.setOnClickListener {
                btnSuccess.isEnabled = cbThree.isChecked
            }
        }
    }
}
