package com.cl.modules_login.ui

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.cl.common_base.ext.containsIgnoreCase
import com.cl.common_base.ext.equalsIgnoreCase
import com.cl.common_base.ext.xpopup
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

@AndroidEntryPoint
class OffLineDeviceActivity : BaseActivity<LoginOfflineDeviceBinding>() {
    val homeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }

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

    override fun initData() {
        binding.rvList.layoutManager = LinearLayoutManager(this@OffLineDeviceActivity)
        binding.rvList.adapter = this@OffLineDeviceActivity.adapter

        // 获取设备列表
        getDeviceList()

        adapter.addChildClickViewIds(R.id.btn_jump_to_device, R.id.iv_pair_luosi, R.id.btn_chang)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val itemData = adapter.data[position] as OffLineDeviceBean
            when (view.id) {
                // 設置植物名
                R.id.btn_chang -> {
                    xpopup(this@OffLineDeviceActivity){
                        asCustom(SettingNamePop(this@OffLineDeviceActivity, doneAction = {
                            itemData.strainName = it
                            Prefs.addObject(itemData.devId.toString(), PresetData(strainName = it))
                            itemData.strainName = it
                            adapter.notifyItemChanged(position)
                        })).show()
                    }
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
                            BaseCenterPop(this@OffLineDeviceActivity,
                                content = "Are you sure you want to delete this add-on?",
                                confirmText = "Yes",
                                cancelText = "No",
                                onConfirmAction = {
                                    //  删除当前配件
                                    ThingHomeSdk.newDeviceInstance(itemData.devId)
                                        ?.resetFactory(object : IResultCallback {
                                            override fun onError(code: String?, error: String?) {
                                                Log.i("12312313", "resetFactory  error")

                                            }

                                            override fun onSuccess() {
                                                ThingHomeSdk.newDeviceInstance(itemData.devId)
                                                    ?.removeDevice(object : IResultCallback {
                                                        override fun onSuccess() {
                                                            adapter.removeAt(position)
                                                        }

                                                        override fun onError(
                                                            code: String?, error: String?
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

    }

    private fun getDeviceList() {
        ThingHomeSdk.newHomeInstance(homeId).getHomeDetail(object : IThingHomeResultCallback {
            override fun onSuccess(bean: HomeBean?) {
                bean?.let { it ->
                    val deviceInfoBean = it.deviceList as ArrayList<DeviceBean>
                    val offLineDeviceBean = mutableListOf(OffLineDeviceBean())
                    val saveList = Prefs.getObjects()

                    // 赋值
                    deviceInfoBean.forEach { a ->
                        if (saveList?.isEmpty() == true) {
                            offLineDeviceBean.add(OffLineDeviceBean(name = a.name, devId = a.devId))
                        } else {

                        }
                    }
                    // 为所有配件命名
                    // TODO 其实这边还需要一个操作,就是每台设备下的子配件 需要添加进去 ,比如风扇,摄摄像头,补光灯.啥的.
                    offLineDeviceBean.forEach {
                        if (it.name?.equalsIgnoreCase("WiFi Temperature & Humidity") == true) {
                            it.spaceType = KEY_MONITOR_VIEW_OUT
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1_TH
                        } else if (it.name?.equalsIgnoreCase("WIFI温湿度传感器") == true) {
                            it.spaceType = KEY_MONITOR_OUT
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1_TH_CN
                        } else if (it.name?.containsIgnoreCase("O1_PRO") == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1_PRO
                        } else if (it.name?.containsIgnoreCase("heyabby-O1") == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1
                        } else if (it.name?.containsIgnoreCase("OG") == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_OG
                        } else if (it.name?.containsIgnoreCase("O1_SE") == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1_SE
                        } else if (it.name?.containsIgnoreCase("O1_SOIL") == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1_SOIL
                        } else if (it.name?.containsIgnoreCase("OG_PRO") == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_OG_PRO
                        } else if (it.name?.containsIgnoreCase("OG_BLACK") == true) {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_OG_BLACK
                        } else {
                            it.spaceType = KEY_SPACE_TYPE_BOX
                            it.type = OffLineDeviceBean.DEVICE_VERSION_O1
                        }
                    }
                    // 然后进行排序,spaceType =  box的为一组 ,  spaceType =  KEY_MONITOR_VIEW_OUT|| KEY_MONITOR_VIEW_OUT 的为一组,其他的为一组
                    val sharedList =
                        offLineDeviceBean.filter { it.spaceType == KEY_MONITOR_VIEW_OUT || it.spaceType == KEY_MONITOR_OUT }
                            .toMutableList()
                    val unSharedList =
                        offLineDeviceBean.filter { it.spaceType == KEY_SPACE_TYPE_BOX }
                            .toMutableList()
                    // 为adapter赋值
                    if (sharedList.isNotEmpty()) {
                        sharedList.add(
                            0, OffLineDeviceBean(
                                spaceType = ListDeviceBean.KEY_SPACE_TYPE_TEXT,
                                textDesc = getString(com.cl.common_base.R.string.string_1789)
                            )
                        )
                    }
                    if (unSharedList.isNotEmpty()) {
                        unSharedList.add(
                            0, OffLineDeviceBean(
                                spaceType = ListDeviceBean.KEY_SPACE_TYPE_TEXT,
                                textDesc = getString(com.cl.common_base.R.string.string_1790)
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