package com.cl.modules_my.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyChooserTipBinding
import com.lxj.xpopup.core.BubbleAttachPopupView

/**
 * This is a short description.
 *
 * @author 李志军 2023-09-17 21:00
 */
class MyChooerTipPop(
    context: Context,
    private val onPhotoPostAction: (() -> Unit)? = null,
    private val onReelPostAction: (() -> Unit)? = null,
): BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_chooser_tip
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyChooserTipBinding>(popupImplView)?.apply {
            tvPhotoPost.setOnClickListener {
                onPhotoPostAction?.invoke()
                dismiss()
            }

            tvReelPost.setOnClickListener {
                onReelPostAction?.invoke()
                dismiss()
            }
        }
    }
}