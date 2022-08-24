package com.cl.common_base.listener

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.util.livedatabus.LiveEventBus

/**
 * 蓝牙监听
 *
 * @author 李志军 2022-08-05 10:45
 */
class BluetoothMonitorReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, intent: Intent?) {
        when (intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
            BluetoothAdapter.STATE_TURNING_ON -> {
                logI("蓝牙正在关闭")
            }
            BluetoothAdapter.STATE_ON -> {
                logI("蓝牙已经打开")
                LiveEventBus.get().with(Constants.Ble.KEY_BLE_STATE, String::class.java)
                    .postEvent(Constants.Ble.KEY_BLE_ON)
            }
            BluetoothAdapter.STATE_TURNING_OFF -> {
                logI("蓝牙正在关闭")
                LiveEventBus.get().with(Constants.Ble.KEY_BLE_STATE, String::class.java)
                    .postEvent(Constants.Ble.KEY_BLE_OFF)
            }
            BluetoothAdapter.STATE_OFF -> {
                logI("蓝牙已经关闭")
            }
        }
    }
}