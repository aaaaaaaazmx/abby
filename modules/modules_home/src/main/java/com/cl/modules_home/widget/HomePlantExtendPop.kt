package com.cl.modules_home.widget

import android.content.Context
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomePlantExtendPopBinding
import com.cl.modules_home.ui.HomeFragment.Companion.KEY_NEW_PLANT
import com.lxj.xpopup.core.BottomPopupView

/**
 * 继承弹窗
 *
 *
 * @author 李志军 2022-08-06 17:35
 */
class HomePlantExtendPop(
    context: Context,
    private val onNextAction: ((status: String) -> Unit)? = null
) : BottomPopupView(context){
    override fun getImplLayoutId(): Int {
        return R.layout.home_plant_extend_pop
    }

    private var binding: HomePlantExtendPopBinding? = null
    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<HomePlantExtendPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                dismiss()
                val one = binding?.cbOne?.isChecked
                onNextAction?.invoke(
                    if (one == true) KEY_NEW_PLANT else KEY_EXTEND
                )
            }

            cbOne.setOnCheckedChangeListener { _, b ->
                if (b) {
                    cbTwo.isChecked = false
                    btnSuccess.isEnabled = (cbOne.isChecked || cbTwo.isChecked) && b
                }
            }

            cbTwo.setOnCheckedChangeListener { _, b ->
                if (b) {
                    cbOne.isChecked = false
                    btnSuccess.isEnabled = (cbOne.isChecked || cbTwo.isChecked) && b
                }
            }

            cbThree.setOnCheckedChangeListener { _, b ->
                btnSuccess.isEnabled = (cbOne.isChecked || cbTwo.isChecked) && b
            }
        }
    }

    companion object {
        const val KEY_EXTEND = "key_extend"
        const val KEY_NEW_PLANT = "key_new_plant"
    }
}