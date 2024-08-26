package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.HomePlantFourPopBinding
import com.cl.common_base.util.Prefs
import com.lxj.xpopup.core.BottomPopupView

/**
 * 开水
 * plant4
 *
 * @author 李志军 2022-08-06 17:35
 */
class HomePlantFourPop(
    context: Context,
    private val onNextAction: (() -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_four_pop
    }

    override fun beforeShow() {
        super.beforeShow()
        val isF = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        binding?.tvDec?.text = if (!isF)  context.getString(R.string.string_187) else context.getString(R.string.string_188)
    }

    private var binding: HomePlantFourPopBinding? = null
    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantFourPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                onNextAction?.invoke()
                dismiss()
            }
        }
    }
}