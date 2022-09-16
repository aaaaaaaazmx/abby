package com.cl.common_base.pop

import android.content.Context
import android.graphics.Typeface
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.ChooseSeedPopBinding
import com.google.gson.annotations.Until
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * 选择种子界面，弹窗第一步，填写strain
 */
class ChooserSeedPop(
    context: Context,
    private val onConfirmAction: ((type: String) -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.choose_seed_pop
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<ChooseSeedPopBinding>(popupImplView)?.apply {
            tvHow.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)

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

            clTwoBox.setOnCheckedChangeListener { p0, p1 ->
                if (autoBox.isChecked) {
                    autoBox.isChecked = !p1
                }
                btnSuccess.isEnabled = autoBox.isChecked || p1
            }

            autoBox.setOnCheckedChangeListener { p0, p1 ->
                if (clTwoBox.isChecked) {
                    clTwoBox.isChecked = !p1
                }
                btnSuccess.isEnabled = clTwoBox.isChecked || p1
            }

            // 点击确定
            btnSuccess.setOnClickListener {
                onConfirmAction?.invoke(
                    if (autoBox.isChecked) KEY_AUTO else KEY_PHOTO
                )
            }

            // 跳转图文链接Pop
            tvHow.setOnClickListener {
                XPopup.Builder(context)
                    .isDestroyOnDismiss(false)
                    .isDestroyOnDismiss(false)
                    .asCustom(BaseCenterPop(context, content = context.getString(R.string.seed_attribute), isShowCancelButton = false))
                    .show()
            }

            ivClose.setOnClickListener {
                onCancelAction?.invoke()
                dismiss()
            }

        }
    }

    override fun onDismiss() {
        onCancelAction?.invoke()
        super.onDismiss()
    }

    companion object {
        const val KEY_PHOTO = "Photo"
        const val KEY_AUTO = "Auto"
    }
}