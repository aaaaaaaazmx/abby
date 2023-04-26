package com.cl.common_base.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.MyChooserOptionBinding
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