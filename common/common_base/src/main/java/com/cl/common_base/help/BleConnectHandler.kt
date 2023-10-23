package com.cl.common_base.help

import android.bluetooth.BluetoothGatt
import com.bhm.ble.callback.BleConnectCallback
import com.bhm.ble.data.BleConnectFailType
import com.bhm.ble.device.BleDevice
import com.bhm.ble.utils.BleLogger
import com.cl.common_base.ext.logI
import com.cl.common_base.widget.toast.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class BleConnectHandler(private val coroutineScope: CoroutineScope) {
    // 2. 创建一个 MutableSharedFlow 实例来发布连接事件
    private val _connectEvents = MutableSharedFlow<ConnectEvent>()
    val connectEvents = _connectEvents.asSharedFlow()

    private fun onConnectStart() {
        publishEvent(ConnectEvent.ConnectStart)
    }

    private fun onConnectFail(bleDevice: BleDevice, connectFailType: BleConnectFailType) {
        val msg: String = when (connectFailType) {
            is BleConnectFailType.UnSupportBle -> "The device does not support Bluetooth."
            is BleConnectFailType.NoBlePermission -> "Insufficient permissions, please check."
            is BleConnectFailType.NullableBluetoothDevice -> "The device is empty."
            is BleConnectFailType.BleDisable -> "Bluetooth not turned on."
            is BleConnectFailType.ConnectException -> "Connection abnormal.(${connectFailType.throwable.message})"
            is BleConnectFailType.ConnectTimeOut -> "Connection timed out."
            is BleConnectFailType.AlreadyConnecting -> "Connecting"
            is BleConnectFailType.ScanNullableBluetoothDevice -> "Connection failed, scan data is empty."
        }
        BleLogger.e(msg)
        logI("BLe -> msg: $msg")
        ToastUtil.shortShow(msg)
        publishEvent(ConnectEvent.ConnectFail(bleDevice, msg))
    }

    private fun onDisConnecting(isActiveDisConnected: Boolean, bleDevice: BleDevice,
                                gatt: BluetoothGatt?, status: Int) {
        publishEvent(ConnectEvent.ConnectDisConnecting(isActiveDisConnected, bleDevice = bleDevice, gatt, status = status))
    }

    private fun onDisConnected(isActiveDisConnected: Boolean, bleDevice: BleDevice,
                               gatt: BluetoothGatt?, status: Int) {
        ToastUtil.shortShow("Disconnect")
        publishEvent(ConnectEvent.ConnectDisConnected(isActiveDisConnected, bleDevice = bleDevice, gatt, status = status))
    }

    private fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt?) {
        publishEvent(ConnectEvent.ConnectSuccess(bleDevice, gatt))
    }

    // ... 其他事件处理函数

    private fun publishEvent(event: ConnectEvent) {
        coroutineScope.launch {
            _connectEvents.emit(event)
        }
    }

    companion object {
        private var bleConnectHandler: BleConnectHandler = BleConnectHandler(CoroutineScope(SupervisorJob() + Dispatchers.Main))

        @Synchronized
        fun get(): BleConnectHandler {
            return bleConnectHandler
        }

        // 在你的代码中使用 BleConnectHandler
        // 将 BleConnectHandler 传递给你的蓝牙连接库
        val connectCallBack: BleConnectCallback.() -> Unit = {
            onConnectStart {
                BleLogger.e("-----onConnectStart")
                bleConnectHandler.onConnectStart()
            }
            onConnectFail { bleDevice, connectFailType ->
                bleConnectHandler.onConnectFail(bleDevice, connectFailType)
            }
            onDisConnecting { isActiveDisConnected, bleDevice, gatt, status ->
                BleLogger.e("-----${bleDevice.deviceAddress} -> onDisConnecting: $isActiveDisConnected")
                bleConnectHandler.onDisConnecting(isActiveDisConnected, bleDevice,  gatt, status)
            }
            onDisConnected { isActiveDisConnected, bleDevice, _, _ ->
                BleLogger.e("-----${bleDevice.deviceAddress} -> onDisConnected: $isActiveDisConnected")
                bleConnectHandler.onDisConnected(isActiveDisConnected, bleDevice, null, 0)
                //发送断开的通知
                /* val message = MessageEvent()
                 message.data = bleDevice
                 EventBus.getDefault().post(message)*/
            }
            onConnectSuccess { bleDevice, _ ->
                logI("BLe -> msg: Connection successful: (${bleDevice.deviceAddress})")
                //ToastUtil.shortShow("Connection successful:${bleDevice.deviceName}")
                bleConnectHandler.onConnectSuccess(bleDevice, null)
            }
            // ... 其他事件处理
        }
    }
}


// 定义 ConnectEvent 类型来表示不同的连接事件
sealed class ConnectEvent {
    object ConnectStart : ConnectEvent()
    data class ConnectFail(val bleDevice: BleDevice, val message: String) : ConnectEvent()
    data class ConnectDisConnecting(
        val isActiveDisConnected: Boolean, val bleDevice: BleDevice, val gatt: BluetoothGatt?, val status: Int
    ) : ConnectEvent()

    data class ConnectDisConnected(
        val isActiveDisConnected: Boolean, val bleDevice: BleDevice, val gatt: BluetoothGatt?, val status: Int
    ) : ConnectEvent()

    data class ConnectSuccess(val bleDevice: BleDevice, val gatt: BluetoothGatt?) : ConnectEvent()
    // ... 其他事件类型
}


