package com.cl.common_base.pop

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.lxj.xpopup.core.BottomPopupView

/**
 * 通用引导界面地步弹窗
 * 一个文字、一个图片、一个按钮
 *
 * @param backGround 背景图片
 * @param text 文案描述
 * @param buttonText 按钮文案描述
 */
class BaseBottomPop(
    context: Context,
    private val backGround: Drawable? = null,
    private val text: String? = null,
    private val buttonText: String? = null,
    private val onNextAction: (() -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_botoom_pop
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<com.cl.common_base.databinding.BaseBotoomPopBinding>(popupImplView)?.apply {
            backGround?.let {
                ivAdd.background = it
            }
            buttonText?.let {
                btnSuccess.text = it
            }
            text?.let {
                tvDec.text = it
            }
            btnSuccess.setOnClickListener {
                dismiss()
                onNextAction?.invoke()
            }
            ivClose.setOnClickListener { dismiss() }
        }
    }
}