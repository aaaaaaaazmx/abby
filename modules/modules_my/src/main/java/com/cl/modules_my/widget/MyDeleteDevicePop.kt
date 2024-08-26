package com.cl.modules_my.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.slidetoconfirmlib.ISlideListener
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyDeleteDevicePopBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 *
 * 删除设备弹窗
 * @author 李志军 2022-08-12 12:33
 */
class MyDeleteDevicePop(
    context: Context,
    private val isShowUnlockButton: Boolean? = false,
    private val unLockText: String? = null,
    private val titleText: String? = null,
    private val checkText: String? = "I understand",
    private val contentText: String? = context.getString(com.cl.common_base.R.string.setting_delete_device),
    private val onNextAction: (() -> Unit)? = null,
) : BottomPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.my_delete_device_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyDeleteDevicePopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            tvDec.text = contentText
            // 勾选狂的文案
            tvThree.text = checkText
            tvTitle.text = titleText
            ViewUtils.setVisible(!titleText.isNullOrEmpty(), tvTitle)
            ViewUtils.setVisible(isShowUnlockButton ?: false, slideToConfirm)
            ViewUtils.setVisible(!(isShowUnlockButton ?: false), btnSuccess)
            slideToConfirm.setEngageText(unLockText ?: "Slide to unlock")
            slideToConfirm.slideListener = object : ISlideListener {
                override fun onSlideStart() {
                }

                override fun onSlideMove(percent: Float) {
                }

                override fun onSlideCancel() {
                }

                override fun onSlideDone() {
                    if (!cbBox.isChecked) {
                        ToastUtil.shortShow(context?.getString(com.cl.common_base.R.string.string_279))
                        return
                    }
                    dismiss()
                    onNextAction?.invoke()
                }
            }

            btnSuccess.setOnClickListener {
                dismiss()
                onNextAction?.invoke()
            }

            rlCheck.setOnClickListener {
                val isCheck = cbBox.isChecked
                cbBox.isChecked = !isCheck
                btnSuccess.isEnabled = !isCheck
            }

            cbBox.setOnCheckedChangeListener { _, isChecked ->
                btnSuccess.isEnabled = isChecked
                cbBox.isChecked = isChecked
            }

        }
    }
}