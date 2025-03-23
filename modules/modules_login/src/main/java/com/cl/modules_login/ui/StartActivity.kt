package com.cl.modules_login.ui

import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.PresetData
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.util.Prefs
import com.cl.modules_login.databinding.LoginItemPlantOneBinding
import com.cl.modules_login.response.OffLineDeviceBean
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.AndroidEntryPoint

@Route(path = RouterPath.LoginRegister.PAGE_PLANT_ONE)
@AndroidEntryPoint
class StartActivity: BaseActivity<LoginItemPlantOneBinding>() {
    private val deviceId by lazy {
        Prefs.getString(Constants.Tuya.KEY_DEVICE_ID)
    }

    override fun initView() {

        //  跳转到环境设置界面
        binding.ivStart.setSafeOnClickListener {
            // 弹出修改strainName的弹窗
            xpopup(this@StartActivity){
                asCustom(SettingNamePop(this@StartActivity, OffLineDeviceBean(devId = deviceId, spaceType = ListDeviceBean.KEY_SPACE_TYPE_BOX), doneAction = {
                    Prefs.addObject(deviceId, PresetData(strainName = it, id = deviceId))
                    // 跳转到首页
                    val intent = Intent(
                        this@StartActivity, OffLineMainActivity::class.java
                    )
                    intent.putExtra("strainName", it)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }, deleteAction = {
                    // 跳转到绑定机器界面
                    Prefs.getObjects()?.indexOfFirst { it.id == deviceId }?.let {
                        Prefs.removeObjectForDevice(it)
                    }
                    val intent = Intent(this@StartActivity, BindDeviceActivity::class.java)
                    startActivity(intent)
                    finish()
                })).show()
            }
        }

        binding.ivDeviceList.setSafeOnClickListener {
            startActivity(Intent(this@StartActivity, OffLineDeviceActivity::class.java).apply {
                putExtra("devId", deviceId)
            })
        }

    }

    override fun observe() {
    }

    override fun initData() {
    }
}