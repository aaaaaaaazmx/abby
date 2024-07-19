package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomePopOpenDoorBinding
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.widget.slidetoconfirmlib.ISlideListener
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.core.CenterPopupView

class OpenDoorPop(
    context: Context,
    private val onSuccessAction: (() -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.home_pop_open_door
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<HomePopOpenDoorBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            slideToConfirm.slideListener = object : ISlideListener {
                override fun onSlideStart() {
                }

                override fun onSlideMove(percent: Float) {
                }

                override fun onSlideCancel() {
                }

                override fun onSlideDone() {
                    // 打开门传false 关闭门传true
                    // 旋钮开门
                    DeviceControl.get()
                        .success {
                            onSuccessAction?.invoke()
                            dismiss()
                        }
                        .error { code, error ->
                            ToastUtil.shortShow(
                                """
                                doorLock:
                                code-> $code
                                errorMsg-> $error
                          """.trimIndent()
                            )
                        }
                        .doorLock(false)
                }

            }
        }
    }
}