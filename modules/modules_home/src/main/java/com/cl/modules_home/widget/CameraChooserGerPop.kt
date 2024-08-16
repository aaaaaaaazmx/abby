package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeCameraTipPopBinding
import com.lxj.xpopup.core.BubbleAttachPopupView

/**
 * 生辰gif还是video的选项
 */
class CameraChooserGerPop(context: Context, private val gifAction: (() -> Unit)? = null, private val videoAction: (() -> Unit)? = null) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_camera_tip_pop
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<HomeCameraTipPopBinding>(popupImplView)?.apply {
            tvPhotoPost.setOnClickListener {
                gifAction?.invoke()
                dismiss()
            }

            tvReelPost.setOnClickListener {
                videoAction?.invoke()
                dismiss()
            }
        }
    }
}