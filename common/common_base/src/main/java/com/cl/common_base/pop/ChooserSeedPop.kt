package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.ChooseSeedPopBinding
import com.google.gson.annotations.Until
import com.lxj.xpopup.core.BottomPopupView

/**
 * 选择种子界面，弹窗第一步，填写strain
 */
class ChooserSeedPop(
    context: Context,
    private val onConfirmAction: ((type: String) -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.choose_seed_pop
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<ChooseSeedPopBinding>(popupImplView)?.apply {
            clTwo.setOnClickListener {
                clTwoBox.isChecked = !clTwoBox.isChecked
                if (autoBox.isChecked) {
                    autoBox.isChecked = !clTwoBox.isChecked
                }
                btnSuccess.isEnabled = clTwoBox.isChecked || autoBox.isChecked
            }
            clAuto.setOnClickListener {
                autoBox.isChecked = !autoBox.isChecked
                if (clTwoBox.isChecked) {
                    clTwoBox.isChecked = !autoBox.isChecked
                }
                btnSuccess.isEnabled = clTwoBox.isChecked || autoBox.isChecked
            }
            // 点击确定
            btnSuccess.setOnClickListener {
                onConfirmAction?.invoke(
                    if (autoBox.isChecked) KEY_AUTO else KEY_PHOTO
                )
            }

            // 跳转图文链接Pop
            tvHow.setOnClickListener {
                // todo 跳转到固定的图文链接模块
            }

            ivClose.setOnClickListener {
                dismiss()
            }

        }
    }

    companion object {
        const val KEY_PHOTO = "key_photo"
        const val KEY_AUTO = "key_photo"
    }
}