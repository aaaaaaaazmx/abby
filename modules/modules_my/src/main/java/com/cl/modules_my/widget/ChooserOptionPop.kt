package com.cl.modules_my.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyChooserOptionBinding
import com.lxj.xpopup.core.BottomPopupView

/**
 * 拍照还是从相册选择弹窗
 */
class ChooserOptionPop(
    context: Context,
    val onPhotoAction: (() -> Unit)? = null,
    val onLibraryAction: (() -> Unit)? = null,

    ) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_chooser_option
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyChooserOptionBinding>(popupImplView)?.apply {
            executePendingBindings()

            photo.setOnClickListener {
                dismiss()
                onPhotoAction?.invoke()
            }
            library.setOnClickListener {
                dismiss()
                onLibraryAction?.invoke()
            }
            cancel.setOnClickListener { dismiss() }
        }
    }
}