package com.cl.modules_login.adapter

import android.app.Activity
import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.bean.AccessoryListBean
import com.cl.common_base.bean.AllDpBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.setVisible
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.R
import com.cl.modules_login.databinding.LoginAccessItemBinding
import com.cl.modules_login.response.OffLineDeviceBean

class OffLineAccessAdapter(
    data: MutableList<AccessoryListBean>?,
    val type: String? = null,
    private val switchListener: ((accessoryId: String, isCheck: Boolean, usbPort: String?) -> Unit)? = null,
) :
    BaseQuickAdapter<AccessoryListBean, BaseDataBindingHolder<LoginAccessItemBinding>>(R.layout.login_access_item, data) {
    val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)

    override fun convert(holder: BaseDataBindingHolder<LoginAccessItemBinding>, item: AccessoryListBean) {
        holder.dataBinding?.apply {
            deviceType = deviceType
            datas = item
            executePendingBindings()

            ivDelete.setVisible(item.accessoryType == "Hum" || item.accessoryType == "Light" || item.accessoryType == "Fan")
            ivLuosi.setVisible(item.accessoryType == "Camera")

            ivLuosi.setSafeOnClickListener {
                if (item.accessoryType == "Camera") {
                    //  跳转到摄像头预览界面
                    com.cl.common_base.util.ipc.CameraUtils.ipcProcess(
                        it.context,
                        item.cameraId,
                        item.deviceId,
                    )
                }
            }

            ivDelete.setSafeOnClickListener {
                xpopup(context) {
                    asCustom(
                        BaseCenterPop(context, content = "Are you sure you want to delete this add-on?",
                            confirmText = "Yes",
                            cancelText = "No",
                            onConfirmAction = {
                                // 删除当前
                                if (item.accessoryType == "Hum" || item.accessoryType == "Light" || item.accessoryType == "Fan") {
                                    // 直接删除当前缓存即可。
                                    Prefs.getObjects()?.firstOrNull { it.id == item.deviceId }?.let { a ->
                                        Prefs.removeObjectAccessory(a, item)
                                        this@OffLineAccessAdapter.remove(item)
                                    }
                                }
                            })
                    ).show()
                }
            }

            ftCheck.setSwitchClickListener {
                val b = ftCheck.isItemChecked
                val usbPort = when (type) {
                    // 一个usb口的
                    OffLineDeviceBean.DEVICE_VERSION_OG_BLACK, OffLineDeviceBean.DEVICE_VERSION_OG_PRO, OffLineDeviceBean.DEVICE_VERSION_O1_PRO -> {
                        3
                    }

                    else -> {
                        1
                    }
                }
                var dpBean: AllDpBean? = null
                if (usbPort == 1) {
                    dpBean = AllDpBean(cmd = "6", usb = if (!b) 1 else 0)
                } else {
                    when (item.usbPort) {
                        1 -> {
                            dpBean = AllDpBean(cmd = "6", usb = if (!b) 1 else 0)
                        }

                        2 -> {
                            dpBean = AllDpBean(cmd = "6", usb2 = if (!b) 1 else 0)
                        }

                        3 -> {
                            dpBean = AllDpBean(cmd = "6", usb3 = if (!b) 1 else 0)
                        }
                    }
                }
                GSON.toJsonInBackground(dpBean) { it1 ->
                    DeviceControl.get().success {
                        logI("dp to success")
                        item.isCheck = !b
                    }.error { code, error -> ToastUtil.shortShow(error) }.sendDps(it1)
                }
            }
        }
    }
}