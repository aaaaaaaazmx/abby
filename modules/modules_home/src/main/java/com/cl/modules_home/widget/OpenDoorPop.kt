package com.cl.modules_home.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomePopOpenDoorBinding
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.widget.slidetoconfirmlib.ISlideListener
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.core.CenterPopupView

class OpenDoorPop(
    context: Context,
    private val onSuccessAction: (() -> Unit)? = null,
) : CenterPopupView(context) {
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
                    // 旋钮开门
                    DeviceControl.get()
                        .success {
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
                        .doorLock(true)
                }

            }
        }
    }
}