package com.cl.modules_home.widget

import android.content.Context
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomePlantTwoPopBinding
import com.cl.common_base.constants.Constants
import com.cl.common_base.util.Prefs
import com.lxj.xpopup.core.BottomPopupView

/**
 * 种植开始需要准备的底部弹窗
 *
 * @author 李志军 2022-08-04 17:35
 */
class HomePlantTwoPop(
    context: Context,
    private val onNextAction: (() -> Unit)? = null
) : BottomPopupView(context),
    CompoundButton.OnCheckedChangeListener {
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_two_pop
    }

    override fun beforeShow() {
        super.beforeShow()
        val isF = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        binding?.tvAddWater?.text = if (isF) "At least 3 gallons of pure water"  else "At least 12L of pure water"
    }

    private var binding: HomePlantTwoPopBinding? = null
    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantTwoPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                onNextAction?.invoke()
                dismiss()
            }
            cbOne.setOnCheckedChangeListener(this@HomePlantTwoPop)
            cbTwo.setOnCheckedChangeListener(this@HomePlantTwoPop)
            cbThree.setOnCheckedChangeListener(this@HomePlantTwoPop)

            clOne.setOnClickListener {
                val check = cbOne.isChecked
                cbOne.isChecked = !check
            }
            clTwo.setOnClickListener {
                val check = cbTwo.isChecked
                cbTwo.isChecked = !check
            }
            clThree.setOnClickListener {
                val check = cbThree.isChecked
                cbThree.isChecked = !check
            }
        }
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
        val checked1 = binding?.cbOne?.isChecked
        val checked2 = binding?.cbTwo?.isChecked
        val checked3 = binding?.cbThree?.isChecked
        binding?.btnSuccess?.isEnabled = (checked1 == true && checked2 == true && checked3 == true)
    }
}