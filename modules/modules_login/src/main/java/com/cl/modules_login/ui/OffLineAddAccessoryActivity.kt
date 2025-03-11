package com.cl.modules_login.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.AccessoryListBean
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.PresetData
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.util.Prefs
import com.cl.modules_login.databinding.LoginOfflineAccessoryBinding
import com.cl.modules_login.response.OffLineDeviceBean
import com.cl.modules_login.widget.OffLineUsbPop
import com.facebook.imagepipeline.producers.PriorityNetworkFetcher.PriorityFetchState
import dagger.hilt.android.AndroidEntryPoint
import kotlin.contracts.Returns
import kotlin.math.sign

@Route(path = RouterPath.LoginRegister.PAGE_ADD_ACCESSORY)
@AndroidEntryPoint
class OffLineAddAccessoryActivity : BaseActivity<LoginOfflineAccessoryBinding>() {

    // 设备列表
    private val deviceList by lazy {
        intent.getSerializableExtra("deviceList") as? MutableList<OffLineDeviceBean> ?: mutableListOf()
    }


    private val currentDevice by lazy {
        intent.getSerializableExtra("currentDevice") as? OffLineDeviceBean
    }

    // cameraUsb的端口号。
    private var cameraUsbPort: Int = -1
    override fun initView() {
        binding.tvAdd.setSafeOnClickListener {
            // 温湿度计
            // 跳转到添加tent内部温湿度传感器界面
            ARouter.getInstance().build(RouterPath.PairConnect.PAGE_WIFI_DEVICE_SCAN).withString(Constants.Pair.KEY_PAIR_WIFI_DEVICE, AccessoryListBean.KEY_MONITOR_IN)
                .withString("deviceId", currentDevice?.devId).navigation()
        }

        binding.tvAddMonitor.setSafeOnClickListener {
            // 带显示器的温湿度计
            ARouter.getInstance().build(RouterPath.PairConnect.PAGE_WIFI_DEVICE_SCAN).withString(Constants.Pair.KEY_PAIR_WIFI_DEVICE, AccessoryListBean.KEY_MONITOR_VIEW_IN)
                .withString("deviceId", currentDevice?.devId).navigation()
        }

        // 补光灯
        binding.tvAddLight.setSafeOnClickListener {
            handleAccessoryAdd("Light", "Smart Light")
        }

        // 风扇
        binding.tvAddFan.setSafeOnClickListener {
            handleAccessoryAdd("Fan", "Smart Fan")
        }

        // 摄像头
        binding.tvAddCamera.setSafeOnClickListener {
            // 获取当前保存的设备对象列表，并查找与当前设备对应的对象
            val objects = Prefs.getObjects()?.toMutableList()
            val currentDevId = currentDevice?.devId.toString()
            val objData = objects?.firstOrNull { it.id == currentDevId }

            // 根据设备 productId 判断当前设备支持的 usb 端口数量
            val usbPortCount = when (currentDevice?.productId) {
                OffLineDeviceBean.DEVICE_VERSION_OG_BLACK,
                OffLineDeviceBean.DEVICE_VERSION_OG_PRO,
                OffLineDeviceBean.DEVICE_VERSION_O1_PRO,
                -> 3

                else -> 1
            }

            fun directAdd(usbPort: Int) {
                cameraUsbPort = usbPort
                // 跳转到摄像头界面
                // 跳转到摄像头配对页面
                ARouter.getInstance().build(RouterPath.PairConnect.PAGE_WIFI_CONNECT).withString(
                    Constants.Global.KEY_WIFI_PAIRING_PARAMS, Constants.Global.KEY_GLOBAL_PAIR_DEVICE_CAMERA
                ).withString("deviceId", currentDevice?.devId).navigation(
                    this@OffLineAddAccessoryActivity, Constants.Global.KEY_WIFI_PAIRING_BACK
                )
            }

            // 通过弹窗让用户选择 usb 端口的逻辑
            fun popupAdd(existingUsbPortList: MutableList<Int?>?) {
                xpopup(this@OffLineAddAccessoryActivity) {
                    asCustom(OffLineUsbPop(this@OffLineAddAccessoryActivity, existingUsbPortList, usbChooseAction = {
                        // 跳转摄像头添加
                        cameraUsbPort = it
                        ARouter.getInstance().build(RouterPath.PairConnect.PAGE_WIFI_CONNECT).withString(
                            Constants.Global.KEY_WIFI_PAIRING_PARAMS, Constants.Global.KEY_GLOBAL_PAIR_DEVICE_CAMERA
                        ).withString("deviceId", currentDevice?.devId).navigation(
                            this@OffLineAddAccessoryActivity, Constants.Global.KEY_WIFI_PAIRING_BACK
                        )
                    })).show()
                }
            }

            // 根据当前对象是否存在及 usb 端口数选择不同的处理逻辑
            if (objData == null) {
                // 没有记录：若只有一个 usb 端口，则直接添加；否则弹窗让用户选择
                if (usbPortCount == 1) {
                    directAdd(1)
                } else {
                    // 对于多端口情况，新建 PresetData 时 usb 端口列表为空，弹窗内部可自行处理端口选取逻辑
                    popupAdd(existingUsbPortList = null)
                }
            } else {
                // 已存在记录：若只有一个 usb 端口，则移除原对象后直接添加；否则弹窗选择
                if (usbPortCount == 1) {
                    objects.remove(objData)
                    directAdd(1)
                } else {
                    // 收集当前已使用的 usb 端口列表，传给弹窗供用户排除已占用的端口
                    val usedUsbPorts = objData.accessoryList?.filter { it.accessoryType.toString().isNotEmpty() }?.map { it.usbPort }?.toMutableList()
                    popupAdd(existingUsbPortList = usedUsbPorts)
                }
            }


        }

        // 加湿器
        binding.tvAddHum.setSafeOnClickListener {
            handleAccessoryAdd("Hum", "Smart Humidifier")
        }
    }


    /**
     * 添加配件
     *
     * @param accessoryType 动态传入的配件类型
     * @param accessoryName 动态传入的配件名称
     */
    private fun handleAccessoryAdd(accessoryType: String, accessoryName: String, cameraId: String? = null) {
        // 获取当前保存的设备对象列表，并查找与当前设备对应的对象
        val objects = Prefs.getObjects()?.toMutableList()
        val currentDevId = currentDevice?.devId.toString()
        val objData = objects?.firstOrNull { it.id == currentDevId }

        // 根据设备 productId 判断当前设备支持的 usb 端口数量
        val usbPortCount = when (currentDevice?.productId) {
            OffLineDeviceBean.DEVICE_VERSION_OG_BLACK,
            OffLineDeviceBean.DEVICE_VERSION_OG_PRO,
            OffLineDeviceBean.DEVICE_VERSION_O1_PRO,
            -> 3

            else -> 1
        }

        // 直接添加配件的方法：创建一个新的 PresetData，并保存单个配件信息
        fun directAdd(usbPort: Int) {
            val bean = AccessoryListBean(
                accessoryType = accessoryType, accessoryName = accessoryName, usbPort = usbPort, deviceId = currentDevId, isCheck = true, cameraId = cameraId
            )
            val list = mutableListOf(bean)
            Prefs.addObject(devId = currentDevId, PresetData(id = currentDevId, accessoryList = list))
            finish()
        }

        // 通过弹窗让用户选择 usb 端口的逻辑
        fun popupAdd(existingUsbPortList: MutableList<Int?>?) {
            xpopup(this@OffLineAddAccessoryActivity) {
                asCustom(OffLineUsbPop(this@OffLineAddAccessoryActivity, existingUsbPortList, usbChooseAction = { selectedUsbPort ->
                    val bean = AccessoryListBean(
                        accessoryType = accessoryType, accessoryName = accessoryName, usbPort = selectedUsbPort, deviceId = currentDevId, isCheck = true, cameraId = cameraId
                    )
                    // 如果已存在对象则直接追加配件，否则创建新的 PresetData
                    if (objData != null) {
                        (objData.accessoryList ?: mutableListOf<AccessoryListBean>().also { objData.accessoryList = it }).add(bean)
                        Prefs.addObject(
                            devId = currentDevId, PresetData(
                                strainName = objData.strainName,
                                id = currentDevId,
                                accessoryList = objData.accessoryList,
                            )
                        )
                    } else {
                        val list = mutableListOf(bean)
                        Prefs.addObject(
                            devId = currentDevId, PresetData(id = currentDevId, accessoryList = list)
                        )
                    }
                    finish()
                })).show()
            }
        }

        // 根据当前对象是否存在及 usb 端口数选择不同的处理逻辑
        if (objData == null) {
            // 没有记录：若只有一个 usb 端口，则直接添加；否则弹窗让用户选择
            if (usbPortCount == 1) {
                directAdd(1)
            } else {
                // 对于多端口情况，新建 PresetData 时 usb 端口列表为空，弹窗内部可自行处理端口选取逻辑
                popupAdd(existingUsbPortList = null)
            }
        } else {
            // 已存在记录：若只有一个 usb 端口，则移除原对象后直接添加；否则弹窗选择
            if (usbPortCount == 1) {
                objects.remove(objData)
                directAdd(1)
            } else {
                // 收集当前已使用的 usb 端口列表，传给弹窗供用户排除已占用的端口
                val usedUsbPorts = objData.accessoryList?.filter { it.accessoryType.toString().isNotEmpty() }?.map { it.usbPort }?.toMutableList()
                popupAdd(existingUsbPortList = usedUsbPorts)
            }
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.Global.KEY_WIFI_PAIRING_BACK -> {
                    val url = data?.getStringExtra("qrcodeUrl")
                    val wifiName = data?.getStringExtra("wifiName")
                    val wifiPwd = data?.getStringExtra("wifiPsd")
                    val token = data?.getStringExtra("token")


                    // 说明绑定成功，跳转到二维码生成界面
                    ARouter.getInstance().build(RouterPath.My.PAGE_CAMERA_QR_CODE).withString(
                        "qrcodeUrl", url
                    ).withString("deviceId", currentDevice?.devId).withString(
                        "wifiName", wifiName
                    ).withString("wifiPsd", wifiPwd).withString("token", token).withString(
                        "accessoryId", currentDevice?.devId
                    ).navigation()
                }

                Constants.Global.KEY_WIFI_PAIRING_CAMERA -> {}
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val cameraId = intent?.getStringExtra(Constants.Global.KEY_IS_CAMERA_ID)

        // 获取当前保存的设备对象列表，并查找与当前设备对应的对象
        val objects = Prefs.getObjects()?.toMutableList()
        val currentDevId = currentDevice?.devId.toString()
        val objData = objects?.firstOrNull { it.id == currentDevId }

        // 根据设备 productId 判断当前设备支持的 usb 端口数量
        val usbPortCount = when (currentDevice?.productId) {
            OffLineDeviceBean.DEVICE_VERSION_OG_BLACK,
            OffLineDeviceBean.DEVICE_VERSION_OG_PRO,
            OffLineDeviceBean.DEVICE_VERSION_O1_PRO,
            -> 3

            else -> 1
        }

        fun popupAdd(existingUsbPortList: MutableList<Int?>?) {
            val bean = AccessoryListBean(
                accessoryType = "Camera", accessoryName = "Smart Camera", usbPort = cameraUsbPort, deviceId = currentDevId, isCheck = true, cameraId = cameraId
            )
            // 如果已存在对象则直接追加配件，否则创建新的 PresetData
            if (objData != null) {
                (objData.accessoryList ?: mutableListOf<AccessoryListBean>().also { objData.accessoryList = it }).add(bean)
                Prefs.addObject(
                    devId = currentDevId, PresetData(
                        strainName = objData.strainName,
                        id = currentDevId,
                        accessoryList = objData.accessoryList,
                    )
                )
            } else {
                val list = mutableListOf(bean)
                Prefs.addObject(
                    devId = currentDevId, PresetData(id = currentDevId, accessoryList = list)
                )
            }
            finish()
        }

        // 直接添加配件的方法：创建一个新的 PresetData，并保存单个配件信息
        fun directAdd(usbPort: Int) {
            val bean = AccessoryListBean(
                accessoryType = "Camera", accessoryName = "Smart Camera", usbPort = cameraUsbPort, deviceId = currentDevId, isCheck = true, cameraId = cameraId
            )
            val list = mutableListOf(bean)
            Prefs.addObject(devId = currentDevId, PresetData(id = currentDevId, accessoryList = list))
            finish()
        }

        // 根据当前对象是否存在及 usb 端口数选择不同的处理逻辑
        if (objData == null) {
            // 没有记录：若只有一个 usb 端口，则直接添加；否则弹窗让用户选择
            if (usbPortCount == 1) {
                directAdd(cameraUsbPort)
            } else {
                // 对于多端口情况，新建 PresetData 时 usb 端口列表为空，弹窗内部可自行处理端口选取逻辑
                popupAdd(existingUsbPortList = null)
            }
        } else {
            // 已存在记录：若只有一个 usb 端口，则移除原对象后直接添加；否则弹窗选择
            if (usbPortCount == 1) {
                objects.remove(objData)
                directAdd(cameraUsbPort)
            } else {
                // 收集当前已使用的 usb 端口列表，传给弹窗供用户排除已占用的端口
                val usedUsbPorts = objData.accessoryList?.filter { it.accessoryType.toString().isNotEmpty() }?.map { it.usbPort }?.toMutableList()
                popupAdd(existingUsbPortList = usedUsbPorts)
            }
        }

    }

}