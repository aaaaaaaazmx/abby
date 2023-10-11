package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhm.ble.BleManager
import com.bhm.ble.callback.BleConnectCallback
import com.bhm.ble.callback.BleScanCallback
import com.bhm.ble.data.BleConnectFailType
import com.bhm.ble.data.BleScanFailType
import com.bhm.ble.device.BleDevice
import com.bhm.ble.utils.BleLogger
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AccessoryAddData
import com.cl.common_base.bean.CharacteristicNode
import com.cl.common_base.bean.LogEntity
import com.cl.common_base.ext.logI
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.repository.MyRepository
import com.cl.common_base.bean.RefreshBleDevice
import com.cl.common_base.bean.SystemConfigBeanItem
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.logging.Level
import javax.inject.Inject

@ActivityRetainedScoped
class BlePairViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {

    // 当前连接中的BleDevice
    private val _currentBleDevice = MutableLiveData<BleDevice?>()
    val currentBleDevice:LiveData<BleDevice?> = _currentBleDevice
    // 设置当前连接中的BleDevice
    fun setCurrentBleDevice(bleDevice: BleDevice?) {
        _currentBleDevice.value = bleDevice
    }

    // 当前的服务ID、特征ID
    private val _currentServiceId = MutableLiveData<String?>()
    val currentServiceId:LiveData<String?> = _currentServiceId
    private val _currentCharacteristicId = MutableLiveData<String?>()
    val currentCharacteristicId:LiveData<String?> = _currentCharacteristicId
    // 设置当前的服务ID、特征ID
    fun setCurrentServiceId(serviceId: String?) {
        _currentServiceId.value = serviceId
    }
    fun setCurrentCharacteristicId(characteristicId: String?) {
        _currentCharacteristicId.value = characteristicId
    }

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
                    logI("BLe -> msg: 没有扫描到设备")
                    ToastUtil.shortShow("The device was not detected.")
                }
            }
            onScanFail {
                val msg: String = when (it) {
                    is BleScanFailType.UnSupportBle -> "The device does not support Bluetooth."
                    is BleScanFailType.NoBlePermission -> "Insufficient permissions, please check."
                    is BleScanFailType.GPSDisable -> "The device has not enabled GPS positioning."
                    is BleScanFailType.BleDisable -> "Bluetooth is not turned on."
                    is BleScanFailType.AlReadyScanning -> "Scanning in progress."
                    is BleScanFailType.ScanError -> {
                        "${it.throwable?.message}"
                    }
                }
                BleLogger.e(msg)
                logI("BLe -> msg: $msg")
                ToastUtil.shortShow(msg)
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
            refreshMutableStateFlow.value = RefreshBleDevice(bleDevice, System.currentTimeMillis())
        }
        onDisConnecting { isActiveDisConnected, bleDevice, _, _ ->
            BleLogger.e("-----${bleDevice.deviceAddress} -> onDisConnecting: $isActiveDisConnected")
        }
        onDisConnected { isActiveDisConnected, bleDevice, _, _ ->
            logI(
                "BLe -> msg: Disconnect(${bleDevice.deviceAddress}，isActiveDisConnected: " +
                        "$isActiveDisConnected)"
            )
            BleLogger.e("-----${bleDevice.deviceAddress} -> onDisConnected: $isActiveDisConnected")
            ToastUtil.shortShow(
                "Disconnect:${bleDevice.deviceName}"
            )
            refreshMutableStateFlow.value = RefreshBleDevice(bleDevice, System.currentTimeMillis())
            //发送断开的通知
            /* val message = MessageEvent()
             message.data = bleDevice
             EventBus.getDefault().post(message)*/
        }
        onConnectSuccess { bleDevice, _ ->
            logI("BLe -> msg: Connection successful: (${bleDevice.deviceAddress})")
            ToastUtil.shortShow("Connection successful:${bleDevice.deviceName}")
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


    /**
     * 读特征值数据
     */
    private val listLogMutableStateFlow = MutableStateFlow(LogEntity(Level.INFO, "数据适配完毕"))
    val listLogStateFlow: StateFlow<LogEntity> = listLogMutableStateFlow
    fun readData(bleDevice: BleDevice,
                 node: CharacteristicNode
    ) {
        BleManager.get().readData(bleDevice, node.serviceUUID, node.characteristicUUID) {
            onReadFail {
                addLogMsg(LogEntity(Level.OFF, "Failed to read feature value data.：${it.message}"))
            }
            onReadSuccess {
                // addLogMsg(LogEntity(Level.FINE, "${node.characteristicUUID} -> 读特征值数据成功：${BleUtil.bytesToHex(it)}"))
                addLogMsg(LogEntity(Level.FINE, msg = "$it", it))
            }
        }
    }

    /**
     * 添加日志显示
     */
    @Synchronized
    fun addLogMsg(logEntity: LogEntity) {
        listLogMutableStateFlow.value = logEntity
    }


    /**
     * 添加蓝牙配件接口
     */
    private val _accessoryAdd = MutableLiveData<Resource<AccessoryAddData>>()
    val accessoryAdd: LiveData<Resource<AccessoryAddData>> = _accessoryAdd
    fun accessoryAdd(automationId: String, deviceId: String, accessoryDeviceId: String? = null) {
        viewModelScope.launch {
            repository.accessoryAdd(automationId, deviceId, accessoryDeviceId)
                .map {
                    if (it.code != Constants.APP_SUCCESS) {
                        Resource.DataError(
                            it.code,
                            it.msg
                        )
                    } else {
                        Resource.Success(it.data)
                    }
                }
                .flowOn(Dispatchers.IO)
                .onStart {
                    emit(Resource.Loading())
                }
                .catch {
                    logD("catch $it")
                    emit(
                        Resource.DataError(
                            -1,
                            "$it"
                        )
                    )
                }.collectLatest {
                    _accessoryAdd.value = it
                }
        }
    }


    /**
     * 删除设备接口、用于删除通用配件
     */
    private val _deleteDevice = MutableLiveData<Resource<BaseBean>>()
    val deleteDevice: LiveData<Resource<BaseBean>> = _deleteDevice
    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            repository.deleteDevice(deviceId)
                .map {
                    if (it.code != Constants.APP_SUCCESS) {
                        Resource.DataError(
                            it.code,
                            it.msg
                        )
                    } else {
                        Resource.Success(it.data)
                    }
                }
                .flowOn(Dispatchers.IO)
                .onStart {
                    emit(Resource.Loading())
                }
                .catch {
                    logD("catch $it")
                    emit(
                        Resource.DataError(
                            -1,
                            "$it"
                        )
                    )
                }.collectLatest {
                    _deleteDevice.value = it
                }
        }
    }

    /**
     * 获取系统配置
     */
    private val _systemConfig = MutableLiveData<Resource<MutableList<SystemConfigBeanItem>>>()
    val systemConfig: LiveData<Resource<MutableList<SystemConfigBeanItem>>> = _systemConfig
    fun systemConfig() {
        viewModelScope.launch {
            repository.getSystemConfig("PH_METER_VIDEO")
                .map {
                    if (it.code != Constants.APP_SUCCESS) {
                        Resource.DataError(
                            it.code,
                            it.msg
                        )
                    } else {
                        Resource.Success(it.data)
                    }
                }
                .flowOn(Dispatchers.IO)
                .onStart {
                    emit(Resource.Loading())
                }
                .catch {
                    logD("catch $it")
                    emit(
                        Resource.DataError(
                            -1,
                            "$it"
                        )
                    )
                }.collectLatest {
                    _systemConfig.value = it
                }
        }
    }



}