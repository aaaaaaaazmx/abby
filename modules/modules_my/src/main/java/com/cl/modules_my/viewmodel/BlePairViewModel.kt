package com.cl.modules_my.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.bhm.ble.BleManager
import com.bhm.ble.callback.BleConnectCallback
import com.bhm.ble.callback.BleScanCallback
import com.bhm.ble.data.BleConnectFailType
import com.bhm.ble.data.BleScanFailType
import com.bhm.ble.device.BleDevice
import com.bhm.ble.utils.BleLogger
import com.cl.common_base.ext.logI
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.repository.MyRepository
import com.cl.modules_my.repository.RefreshBleDevice
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ActivityRetainedScoped
class BlePairViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {

    /**
     * 蓝牙数据类
     */
    private val listDRMutableStateFlow = MutableStateFlow(
        BleDevice(null, null, null, null, null, null, null)
    )
    val listDRStateFlow: StateFlow<BleDevice> = listDRMutableStateFlow

    val listDRData = mutableListOf<BleDevice>()

    private val scanStopMutableStateFlow = MutableStateFlow(true)

    val scanStopStateFlow: StateFlow<Boolean> = scanStopMutableStateFlow

    private val refreshMutableStateFlow = MutableStateFlow(
        RefreshBleDevice(null, null)
    )

    val refreshStateFlow: StateFlow<RefreshBleDevice?> = refreshMutableStateFlow

    // 当前已经连接的设备
    val connectedDevice = BleManager.get().getAllConnectedDevice()

    /**
     * 扫描之后的回调
     */
    fun getScanCallback(showData: Boolean): BleScanCallback.() -> Unit {
        return {
            onScanStart {
                BleLogger.d("onScanStart")
                scanStopMutableStateFlow.value = false
            }
            onLeScan { bleDevice, _ ->
                //可以根据currentScanCount是否已有清空列表数据
                bleDevice.deviceName?.let { _ ->

                }
            }
            onLeScanDuplicateRemoval { bleDevice, _ ->
                bleDevice.deviceName?.let { _ ->
                    if (showData) {
                        listDRData.add(bleDevice)
                        listDRMutableStateFlow.value = bleDevice
                    }
                }
            }
            onScanComplete { bleDeviceList, bleDeviceDuplicateRemovalList ->
                //扫描到的数据是所有扫描次数的总和
                bleDeviceList.forEach {
                    it.deviceName?.let { deviceName ->
                        BleLogger.i("bleDeviceList-> $deviceName, ${it.deviceAddress}")
                    }
                }
                bleDeviceDuplicateRemovalList.forEach {
                    it.deviceName?.let { deviceName ->
                        BleLogger.e("bleDeviceDuplicateRemovalList-> $deviceName, ${it.deviceAddress}")
                    }
                }
                scanStopMutableStateFlow.value = true
                if (listDRData.isEmpty() && showData) {
                    logI("BLe -> msg: 没有扫描到数据")
                }
            }
            onScanFail {
                val msg: String = when (it) {
                    is BleScanFailType.UnSupportBle -> "设备不支持蓝牙"
                    is BleScanFailType.NoBlePermission -> "权限不足，请检查"
                    is BleScanFailType.GPSDisable -> "设备未打开GPS定位"
                    is BleScanFailType.BleDisable -> "蓝牙未打开"
                    is BleScanFailType.AlReadyScanning -> "正在扫描"
                    is BleScanFailType.ScanError -> {
                        "${it.throwable?.message}"
                    }
                }
                BleLogger.e(msg)
                logI("BLe -> msg: $msg")
                scanStopMutableStateFlow.value = true
            }
        }
    }



    /**
     * 停止扫描
     */
    fun stopScan() {
        BleManager.get().stopScan()
    }

    /**
     * 是否已连接
     */
    fun isConnected(bleDevice: BleDevice?) = BleManager.get().isConnected(bleDevice)

    /**
     * 开始连接
     */
    fun connect(address: String) {
        connect(BleManager.get().buildBleDeviceByDeviceAddress(address))
    }
    /**
     * 开始连接
     */
    fun connect(bleDevice: BleDevice?) {
        bleDevice?.let { device ->
            BleManager.get().connect(device, connectCallback)
        }
    }

    // 监听回调。
    private val connectCallback: BleConnectCallback.() -> Unit = {
        onConnectStart {
            BleLogger.e("-----onConnectStart")
        }
        onConnectFail { bleDevice, connectFailType ->
            val msg: String = when (connectFailType) {
                is BleConnectFailType.UnSupportBle -> "设备不支持蓝牙"
                is BleConnectFailType.NoBlePermission -> "权限不足，请检查"
                is BleConnectFailType.NullableBluetoothDevice -> "设备为空"
                is BleConnectFailType.BleDisable -> "蓝牙未打开"
                is BleConnectFailType.ConnectException -> "连接异常(${connectFailType.throwable.message})"
                is BleConnectFailType.ConnectTimeOut -> "连接超时"
                is BleConnectFailType.AlreadyConnecting -> "连接中"
                is BleConnectFailType.ScanNullableBluetoothDevice -> "连接失败，扫描数据为空"
            }
            BleLogger.e(msg)
            logI("BLe -> msg: $msg")
            ToastUtil.shortShow(msg)
            refreshMutableStateFlow.value = RefreshBleDevice(bleDevice, System.currentTimeMillis())
        }
        onDisConnecting { isActiveDisConnected, bleDevice, _, _ ->
            BleLogger.e("-----${bleDevice.deviceAddress} -> onDisConnecting: $isActiveDisConnected")
        }
        onDisConnected { isActiveDisConnected, bleDevice, _, _ ->
            logI("BLe -> msg: 断开连接(${bleDevice.deviceAddress}，isActiveDisConnected: " +
                    "$isActiveDisConnected)")
            BleLogger.e("-----${bleDevice.deviceAddress} -> onDisConnected: $isActiveDisConnected")
            ToastUtil.shortShow("断开连接:${bleDevice.deviceAddress}，isActiveDisConnected: " +
                    "$isActiveDisConnected")
            refreshMutableStateFlow.value = RefreshBleDevice(bleDevice, System.currentTimeMillis())
            //发送断开的通知
           /* val message = MessageEvent()
            message.data = bleDevice
            EventBus.getDefault().post(message)*/
        }
        onConnectSuccess { bleDevice, _ ->
            logI("BLe -> msg: 连接成功(${bleDevice.deviceAddress})")
            ToastUtil.shortShow("连接成功:${bleDevice.deviceAddress}")
            refreshMutableStateFlow.value = RefreshBleDevice(bleDevice, System.currentTimeMillis())
        }
    }

    /**
     * 断开连接
     */
    fun disConnect(bleDevice: BleDevice?) {
        bleDevice?.let { device ->
            BleManager.get().disConnect(device)
        }
    }

    /**
     * 断开所有连接 释放资源
     */
    fun close() {
        BleManager.get().closeAll()
    }
}