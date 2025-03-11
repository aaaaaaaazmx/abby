package com.cl.modules_login.ui

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.util.device.TuyaCameraUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.R
import com.cl.modules_login.databinding.LoginEditProfilePopBinding
import com.cl.modules_login.response.OffLineDeviceBean
import com.lxj.xpopup.core.BottomPopupView
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback

class SettingNamePop(
    context: Context,
    val device: OffLineDeviceBean? = null,
    private val doneAction: ((String) -> Unit)? = null,
    private val deleteAction: ((String) -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.login_edit_profile_pop
    }

    /**
     * 涂鸦摄像头帮助类
     */
    private val tuyaUtils by lazy {
        TuyaCameraUtils()
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<LoginEditProfilePopBinding>(popupImplView)?.apply {
            executePendingBindings()
            // btn_success 点击之后根据对应设备id 保存 strainName
            // 然后跳转到主页
            btnSuccess.setOnClickListener {
                // 保存 strainName
                doneAction?.invoke(etEmail.text.toString())
                dismiss()
            }

            ivClose.setSafeOnClickListener { dismiss() }

            btnDelete.setSafeOnClickListener {
                if (device?.spaceType == ListDeviceBean.KEY_SPACE_TYPE_BOX) {
                    val deviceInstance = ThingHomeSdk.newDeviceInstance(device.devId.toString())

                    deviceInstance.removeDevice(object : IResultCallback {
                        override fun onError(code: String?, error: String?) {
                            ToastUtil.shortShow(error)
                        }

                        override fun onSuccess() {
                            //   如果当前设备绑定了摄像头，那么就需要把摄像头一并解绑。
                            val accessoryList = device.accessoryList?.firstOrNull { it.accessoryType == "Camera" }
                            if (null != accessoryList) {
                                // 移除摄像头
                                tuyaUtils.unBindCamera(accessoryList.cameraId.toString(), onSuccessAction = {
                                    deleteAction?.invoke("")
                                    dismiss()
                                }, onErrorAction = {
                                    ToastUtil.shortShow(it)
                                    deleteAction?.invoke("")
                                    dismiss()
                                })
                                return
                            }
                            deleteAction?.invoke("")
                            dismiss()
                        }
                    })
                    return@setSafeOnClickListener
                }
            }
        }
    }
}