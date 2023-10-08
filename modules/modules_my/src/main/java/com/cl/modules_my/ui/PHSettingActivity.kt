package com.cl.modules_my.ui

import com.bhm.ble.BleManager
import com.bhm.ble.attribute.BleOptions
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyPhSettingActivityBinding
import com.cl.modules_my.viewmodel.BlePairViewModel
import javax.inject.Inject

/**
 * Ph笔数据设置界面
 */
class PHSettingActivity: BaseActivity<MyPhSettingActivityBinding>() {

    @Inject
    lateinit var mViewMode: BlePairViewModel

    override fun initView() {
        // 初始化
        BleManager.get().init(
            application,
            BleOptions.Builder()
                .setScanMillisTimeOut(5000)
                .setConnectMillisTimeOut(5000)
                //一般不推荐autoSetMtu，因为如果设置的等待时间会影响其他操作
                .setMtu(100, true)
                .setScanDeviceName(PhPairActivity.DEVICE_NAME)
                .setAutoConnect(true)
                .setMaxConnectNum(2)
                .setConnectRetryCountAndInterval(2, 1000)
                .build()
        )

        // 查看当前是否已经有连接的设备
        /*if (mViewMode.connectedDevice?.size == 0) {
            checkPermissionAndStartScan()
        }*/
    }

    override fun onBleChange(status: String) {
        super.onBleChange(status)
        when (status) {
            Constants.Ble.KEY_BLE_ON -> {
                // 这个界面是不需要是否有连接过设备的， 其他界面在蓝牙开关的时候，都需要判断之前是否连接过设备，连接过那么就直接连接了。
                checkPermissionAndStartScan()
                logI("KEY_BLE_ON")
            }

            Constants.Ble.KEY_BLE_OFF -> {
                ToastUtil.shortShow("Bluetooth is turned off")
                logI("KEY_BLE_OFF")
            }
        }
    }

    private fun checkPermissionAndStartScan() {
        PermissionHelp().checkConnect(
            this@PHSettingActivity,
            supportFragmentManager,
            true,
            object : PermissionHelp.OnCheckResultListener {
                override fun onResult(result: Boolean) {
                    if (!result) return
                    BleManager.get().startScan(mViewMode.getScanCallback(true))
                }
            })
    }

    override fun observe() {
    }

    override fun initData() {
    }
}