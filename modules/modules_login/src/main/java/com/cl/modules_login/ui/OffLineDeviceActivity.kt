package com.cl.modules_login.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.AccessoryListBean.Companion.KEY_MONITOR_OUT
import com.cl.common_base.bean.AccessoryListBean.Companion.KEY_MONITOR_VIEW_OUT
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.ListDeviceBean.Companion.KEY_SPACE_TYPE_BOX
import com.cl.common_base.bean.PresetData
import com.cl.common_base.bean.UpPlantInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.containsIgnoreCase
import com.cl.common_base.ext.equalsIgnoreCase
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.temperatureConversionThree
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.Prefs
import com.cl.modules_login.R
import com.cl.modules_login.databinding.LoginOfflineDeviceBinding
import com.cl.modules_login.response.OffLineDeviceBean
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.AndroidEntryPoint
import com.cl.modules_login.adapter.OffLineDeviceListAdapter
import com.lxj.xpopup.XPopup
import com.thingclips.smart.sdk.api.IResultCallback
import java.io.Serializable

@Route(path = RouterPath.My.PAGE_MY_DEVICE_LIST)
@AndroidEntryPoint
class OffLineDeviceActivity : BaseActivity<LoginOfflineDeviceBinding>() {
    val homeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }

    private val isMetric = { Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false) }

    private val devId by lazy {
        intent.getStringExtra(Constants.Tuya.KEY_DEVICE_ID)
    }

    private val adapter by lazy {
        OffLineDeviceListAdapter(mutableListOf())
    }

    override fun initView() {
    }

    override fun observe() {
    }

    override fun onResume() {
        super.onResume()
        // 获取设备列表
        getDeviceList()
    }

    override fun initData() {
        binding.rvList.layoutManager = LinearLayoutManager(this@OffLineDeviceActivity)
        binding.rvList.adapter = this@OffLineDeviceActivity.adapter

        adapter.addChildClickViewIds(R.id.btn_jump_to_device, R.id.iv_pair_luosi, R.id.btn_chang, R.id.btn_add_accessory)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val itemData = adapter.data[position] as OffLineDeviceBean
            when (view.id) {
                R.id.btn_add_accessory -> {
                    // 添加配件
                    val intent = Intent(this@OffLineDeviceActivity, OffLineAddAccessoryActivity::class.java)
                    intent.putExtra("deviceList", adapter.data as? Serializable)
                    intent.putExtra("currentDevice", itemData as? Serializable)
                    startActivity(intent)
                }
                // 設置植物名
                R.id.btn_chang -> {
                    startActivity(Intent(this@OffLineDeviceActivity, OffLineSpaceSetting::class.java).apply {
                        putExtra(Constants.Tuya.KEY_DEVICE_ID, itemData.devId)
                        putExtra("bean", itemData)
                    })
                }

                R.id.btn_jump_to_device -> {
                    //  保存设备ID
                    Prefs.putStringAsync(Constants.Tuya.KEY_DEVICE_ID, itemData.devId.toString())
                    startActivity(Intent(
                        this@OffLineDeviceActivity, OffLineMainActivity::class.java
                    ).apply {
                        putExtra(Constants.Tuya.KEY_DEVICE_ID, itemData.devId.toString())
                    })
                    finish()
                }

                R.id.iv_pair_luosi -> {
                    xpopup(this@OffLineDeviceActivity) {
                        asCustom(
                            BaseCenterPop(this@OffLineDeviceActivity, content = "Are you sure you want to delete this add-on?", confirmText = "Yes", cancelText = "No", onConfirmAction = {
                                //  删除当前配件
                                ThingHomeSdk.newDeviceInstance(itemData.devId)?.resetFactory(object : IResultCallback {
                                    override fun onError(code: String?, error: String?) {
                                        Log.i("12312313", "resetFactory  error")

                                    }

                                    override fun onSuccess() {
                                        ThingHomeSdk.newDeviceInstance(itemData.devId)?.removeDevice(object : IResultCallback {
                                            override fun onSuccess() {
                                                adapter.removeAt(position)
                                            }

                                            override fun onError(
                                                code: String?, error: String?,
                                            ) {
                                                Log.i("12312313", "resetFactory  error")
                                            }
                                        })
                                    }
                                })
                            })
                        ).show()
                    }
                }
            }

        }


        binding.ivAddDevice.setSafeOnClickListener {
            PermissionHelp().checkConnectForTuYaBle(this@OffLineDeviceActivity, object : PermissionHelp.OnCheckResultListener {
                override fun onResult(result: Boolean) {
                    if (!result) return
                    // 如果权限都已经同意了
                    ARouter.getInstance().build(RouterPath.PairConnect.PAGE_PLANT_SCAN).navigation()
                }
            })
        }
    }

    private fun getDeviceList() {
        ThingHomeSdk.newHomeInstance(homeId).getHomeDetail(object : IThingHomeResultCallback {
            override fun onSuccess(bean: HomeBean?) {
                bean?.let { it ->
                    val deviceInfoBean = it.deviceList as ArrayList<DeviceBean>
                    val offLineDeviceBean = mutableListOf<OffLineDeviceBean>()
                    val saveList = Prefs.getObjects()

                    // 赋值新的，或者对老的赋值strainName
                    // 赋值就是每台设备下的子配件 需要添加进去 ,比如风扇,摄摄像头,补光灯.啥的.
                    deviceInfoBean.forEach { a ->
                        if (a.productId == OffLineDeviceBean.DEVICE_VERSION_CAMERA) return@forEach
                        if (saveList?.isEmpty() == true) {
                            offLineDeviceBean.add(OffLineDeviceBean(name = a.name, devId = a.devId, productId = a.productId, dps = a.dps))
                        } else {
                            // 从旧的里面找到devId想通的，然后找出来。
                            val list = saveList?.firstOrNull { it.id == a.devId }
                            if (null == list) {
                                offLineDeviceBean.add(OffLineDeviceBean(name = a.name, devId = a.devId, productId = a.productId, dps = a.dps))
                            } else {
                                offLineDeviceBean.add(
                                    OffLineDeviceBean(
                                        name = a.name,
                                        devId = a.devId,
                                        strainName = list.strainName,
                                        productId = a.productId,
                                        accessoryList = list.accessoryList,
                                        dps = a.dps
                                    )
                                )
                            }
                        }
                    }
                    // 为所有配件命名
                    offLineDeviceBean.forEach {
                        if (it.productId?.equalsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1_TH) == true) {
                            it.spaceType = KEY_MONITOR_VIEW_OUT
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1_TH
                            it.background = R.mipmap.ic_smart_sensor
                            // 解析dp点
                            it.dps?.forEach { (key, value) ->
                                when (key) {
                                    "1" -> {
                                        //  温度 / 10
                                        it.wendu = temperatureConversionThree(value.safeToInt().div(10).safeToFloat(), isMetric())
                                    }

                                    "2" -> {
                                        // 湿度
                                        it.shidu = value.safeToInt().toString()
                                    }
                                }
                            }
                            if (isMetric()) {
                                it.unit = "°C"
                            } else {
                                it.unit = "F"
                            }
                        } else if (it.productId?.equalsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1_TH_CN) == true) {
                            it.spaceType = KEY_MONITOR_OUT
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1_TH_CN
                            it.background = R.mipmap.ic_smart_monitor
                            // 解析dp点
                            it.dps?.forEach { (key, value) ->
                                when (key) {
                                    "1" -> {
                                        //  温度 / 10
                                        it.wendu = temperatureConversionThree(value.safeToInt().div(10).safeToFloat(), isMetric())
                                    }

                                    "2" -> {
                                        // 湿度
                                        it.shidu = value.safeToInt().toString()
                                    }
                                    "9" -> {
                                        // 华氏度还是摄氏度
                                        if (value.toString().equalsIgnoreCase("c") || value.toString().equalsIgnoreCase("°C")) {
                                            it.unit = "°C"
                                        } else {
                                            it.unit = "F"
                                        }
                                    }
                                }
                            }
                            if (isMetric()) {
                                it.unit = "°C"
                            } else {
                                it.unit = "F"
                            }
                        } else if (it.productId?.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1_PRO) == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1_PRO
                        } else if (it.productId?.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1) == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1
                        } else if (it.productId?.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_OG) == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_OG
                        } else if (it.productId?.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1_SE) == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1_SE
                        } else if (it.productId?.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1_SOIL) == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1_SOIL
                        } else if (it.productId?.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_OG_PRO) == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_OG_PRO
                        } else if (it.productId?.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_OG_BLACK) == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_OG_BLACK
                        } else {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1
                        }
                    }

                    offLineDeviceBean.forEach {
                        if (it.spaceType == KEY_SPACE_TYPE_BOX) {
                            it.background = com.cl.common_base.R.mipmap.my_jiqi_oi
                        }
                    }

                    // 然后进行排序,spaceType =  box的为一组 ,  spaceType =  KEY_MONITOR_VIEW_OUT|| KEY_MONITOR_VIEW_OUT 的为一组,其他的为一组
                    val sharedList = offLineDeviceBean.filter { it.spaceType == KEY_MONITOR_VIEW_OUT || it.spaceType == KEY_MONITOR_OUT }.toMutableList()
                    val unSharedList = offLineDeviceBean.filter { it.spaceType == KEY_SPACE_TYPE_BOX }.toMutableList()
                    // 为adapter赋值
                    if (sharedList.isNotEmpty()) {
                        sharedList.add(
                            0, OffLineDeviceBean(
                                spaceType = ListDeviceBean.KEY_SPACE_TYPE_TEXT, textDesc = getString(com.cl.common_base.R.string.string_1789)
                            )
                        )
                    }
                    if (unSharedList.isNotEmpty()) {
                        unSharedList.add(
                            0, OffLineDeviceBean(
                                spaceType = ListDeviceBean.KEY_SPACE_TYPE_TEXT, textDesc = getString(com.cl.common_base.R.string.string_1790)
                            )
                        )
                    }

                    // 新数据
                    val newList = sharedList + unSharedList

                    // 根据传递过来的devid表示是当前选中的
                    newList.indexOfFirst { it.devId == devId }.apply {
                        if (this != -1) {
                            newList[this].isChoose = true
                        }
                    }
                    adapter.setList(newList)
                }

            }

            override fun onError(errorCode: String?, errorMsg: String?) {

            }

        })
    }
}