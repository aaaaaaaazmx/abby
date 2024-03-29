package com.cl.modules_my.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.slidetoconfirmlib.ISlideListener
import com.cl.common_base.widget.slidetoconfirmlib.SlideToConfirm
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyAttentionPopBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 * 删除植物确认弹窗
 */
class AttentionPop(
    context: Context,
    private val contentText: String? = null,
    private val isShowTalkButton: Boolean? = false,
    private val rePlantAction: (() -> Unit)? = null,
    private val talkButtonAction: (()->Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_attention_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyAttentionPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            btnSuccess.setOnClickListener {
                talkButtonAction?.invoke()
                dismiss()
            }

            ViewUtils.setVisible(isShowTalkButton == true, btnSuccess)
            tvTitleDesc.text = if (isShowTalkButton == true) "You are about to replant. The current session will be lost, and this operation is irreversible. Our growing expert may help you save the plant." else "You are about to replant. The current session will be lost, and this operation is irreversible."

            clCheck.setOnClickListener {
                curingBox.isChecked = !curingBox.isChecked
            }


            slideToConfirm.slideListener = object : ISlideListener {
                override fun onSlideStart() {

                }

                override fun onSlideMove(percent: Float) {
                }

                override fun onSlideCancel() {
                }

                override fun onSlideDone() {
                    if (!curingBox.isChecked) {
                        ToastUtil.shortShow("Please select all item")
                        slideToConfirm.reset()
                        return
                    }
                    rePlantAction?.invoke()
                    dismiss()
                }
            }
        }
    }
}