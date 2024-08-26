package com.cl.modules_my.viewmodel

import android.bluetooth.BluetoothGattCharacteristic
import android.util.SparseArray
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
import com.bhm.ble.utils.BleUtil
import com.chad.library.adapter.base.entity.node.BaseNode
import com.cl.common_base.BaseApplication
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AccessoryAddData
import com.cl.common_base.bean.CharacteristicNode
import com.cl.common_base.bean.LogEntity
import com.cl.common_base.ext.logI
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.repository.MyRepository
import com.cl.common_base.bean.RefreshBleDevice
import com.cl.common_base.bean.ServiceNode
import com.cl.common_base.bean.SystemConfigBeanItem
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.letMultiple
import com.cl.common_base.ext.logD
import com.cl.common_base.help.BleConnectHandler
import com.cl.common_base.help.ConnectEvent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.logging.Level
import javax.inject.Inject
import kotlin.experimental.xor

@ActivityRetainedScoped
class BlePairViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {

    // 当前连接中的BleDevice
    private val _currentBleDevice = MutableLiveData<BleDevice?>()
    val currentBleDevice: LiveData<BleDevice?> = _currentBleDevice

    // 设置当前连接中的BleDevice
    fun setCurrentBleDevice(bleDevice: BleDevice?) {
        _currentBleDevice.value = bleDevice
    }

    // 当前的服务ID、特征ID
    private val _currentServiceId = MutableLiveData<String?>()
    val currentServiceId: LiveData<String?> = _currentServiceId
    private val _currentCharacteristicId = MutableLiveData<String?>()
    val currentCharacteristicId: LiveData<String?> = _currentCharacteristicId

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

    private val refreshMutableStateFlow = MutableStateFlow(
        RefreshBleDevice(null, null)
    )

    val refreshStateFlow: StateFlow<RefreshBleDevice?> = refreshMutableStateFlow

    init {
        viewModelScope.launch {
            BleConnectHandler.get().connectEvents.collect {
                when (it) {
                    is ConnectEvent.ConnectStart -> {

                    }

                    is ConnectEvent.ConnectFail -> {
                        refreshMutableStateFlow.value = RefreshBleDevice(it.bleDevice, System.currentTimeMillis())
                    }

                    is ConnectEvent.ConnectDisConnecting -> {
                    }

                    is ConnectEvent.ConnectDisConnected -> {
                        refreshMutableStateFlow.value = RefreshBleDevice(it.bleDevice, System.currentTimeMillis())
                    }

                    is ConnectEvent.ConnectSuccess -> {
                        refreshMutableStateFlow.value = RefreshBleDevice(it.bleDevice, System.currentTimeMillis())
                    }

                    else -> {}
                }
            }
        }
    }

    // livedata 扫描
    private val _scanLiveData = MutableLiveData<Boolean>()
    val scanLiveData: LiveData<Boolean> = _scanLiveData

    /**
     * 扫描之后的回调
     */
    fun getScanCallback(isAdd: Boolean): BleScanCallback.() -> Unit {
        return {
            onScanStart {
                BleLogger.d("onScanStart")
            }
            onLeScan { bleDevice, _ ->
                //可以根据currentScanCount是否已有清空列表数据
                bleDevice.deviceName?.let { _ ->
                }
            }
            onLeScanDuplicateRemoval { bleDevice, _ ->
                bleDevice.deviceName?.let { _ ->
                    logI("onLeScanDuplicateRemoval: bleDevice.deviceName = ${bleDevice.deviceName}")
                    if (bleDevice.deviceName == Constants.Ble.KEY_PH_DEVICE_NAME) {
                        listDRData.add(bleDevice)
                        if (isAdd) {
                            connect(bleDevice)
                        }
                    }
                    listDRMutableStateFlow.value = bleDevice
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
                if (listDRData.isEmpty()) {
                    logI("BLe -> msg: 没有扫描到设备")
                    // ToastUtil.shortShow("The device was not detected.")
                    _scanLiveData.value = true
                }
            }
            onScanFail {
                val msg: String = when (it) {
                    is BleScanFailType.UnSupportBle -> BaseApplication.getContext().getString(com.cl.common_base.R.string.string_1160)
                    is BleScanFailType.NoBlePermission -> BaseApplication.getContext().getString(com.cl.common_base.R.string.string_1161)
                    is BleScanFailType.GPSDisable -> BaseApplication.getContext().getString(com.cl.common_base.R.string.string_1483)
                    is BleScanFailType.BleDisable -> BaseApplication.getContext().getString(com.cl.common_base.R.string.string_1163)
                    is BleScanFailType.AlReadyScanning -> BaseApplication.getContext().getString(com.cl.common_base.R.string.string_1485)
                    is BleScanFailType.ScanError -> {
                        "${it.throwable?.message}"
                    }
                }
                BleLogger.e(msg)
                logI("BLe -> msg: $msg")
                ToastUtil.shortShow(msg)
                _scanLiveData.value = true
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
     * 断开Ph设备
     */
    fun disConnectPhDevice() {
        BleManager.get().getAllConnectedDevice()?.firstOrNull { it.deviceName == Constants.Ble.KEY_PH_DEVICE_NAME }?.let {
            BleManager.get().disConnect(it)
        }
    }

    /**
     * 查找当前Ph笔
     *
     * true 表示没找到
     * false 表示找到了
     */
    fun notFindConnectPhDevice(): Boolean {
        return BleManager.get().getAllConnectedDevice()?.firstOrNull { it.deviceName == Constants.Ble.KEY_PH_DEVICE_NAME } == null
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
            BleManager.get().connect(device)
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
    fun readData(
        bleDevice: BleDevice,
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

    /**
     * 写数据
     * 注意：因为分包后每一个包，可能是包含完整的协议，所以分包由业务层处理，组件只会根据包的长度和mtu值对比后是否拦截
     */
    fun writeData(
        bleDevice: BleDevice,
        node: CharacteristicNode,
        text: String
    ) {
        // 使用示例
        val bytesToWrite = ByteArray(19)
        // bytesToWrite[0] = 0x01
        // bytesToWrite[1] = 0x00
        // 计算并设置校验码到bytesToWrite[2]
        bytesToWrite[2] = checksum(bytesToWrite, 1)  // 只计算前两个字节的校验码
        val data = text.toByteArray()
        BleLogger.i("data is: ${BleUtil.bytesToHex(data)}")
        val mtu = BleManager.get().getOptions()?.mtu ?: com.bhm.ble.data.Constants.DEFAULT_MTU
        //mtu长度包含了ATT的opcode一个字节以及ATT的handle2个字节
        val maxLength = mtu - 3
        val listData: SparseArray<ByteArray> = BleUtil.subpackage(bytesToWrite, maxLength)
        BleManager.get().writeData(bleDevice, node.serviceUUID, node.characteristicUUID, listData) {
            onWriteFail { currentPackage, _, t ->
                addLogMsg(LogEntity(Level.OFF, "第${currentPackage}包数据写失败：${t.message}"))
            }
            onWriteSuccess { currentPackage, _, justWrite ->
                addLogMsg(
                    LogEntity(
                        Level.FINE, "${node.characteristicUUID} -> 第${currentPackage}包数据写成功：" +
                                BleUtil.bytesToHex(justWrite)
                    )
                )
            }
            onWriteComplete { allSuccess ->
                //代表所有数据写成功，可以在这个方法中处理成功的逻辑
                addLogMsg(LogEntity(Level.FINE, "${node.characteristicUUID} -> 写数据完成，是否成功：$allSuccess"))
            }
        }
    }


    fun writeDataForPh(
        bleDevice: BleDevice,
        open: Boolean
    ) {
        // 使用示例
        val bytesToWrite = ByteArray(19)
        bytesToWrite[0] = 0x01
        // 计算并设置校验码到bytesToWrite[2]
        bytesToWrite[2] = checksum(bytesToWrite, 1)  // 只计算前两个字节的校验码
        // val data = text.toByteArray()
        // BleLogger.i("data is: ${BleUtil.bytesToHex(data)}")
        val mtu = BleManager.get().getOptions()?.mtu ?: com.bhm.ble.data.Constants.DEFAULT_MTU
        //mtu长度包含了ATT的opcode一个字节以及ATT的handle2个字节
        val maxLength = mtu - 3
        val listData: SparseArray<ByteArray> = BleUtil.subpackage(bytesToWrite, maxLength)
        letMultiple(currentServiceId.value, currentCharacteristicId.value) { serviceId, characteristicId ->
            BleManager.get().writeData(bleDevice, serviceId, characteristicId, listData) {
                onWriteFail { currentPackage, _, t ->
                    addLogMsg(LogEntity(Level.OFF, "第${currentPackage}包数据写失败：${t.message}"))
                }
                onWriteSuccess { currentPackage, _, justWrite ->
                    addLogMsg(
                        LogEntity(
                            Level.FINE, "$characteristicId -> 第${currentPackage}包数据写成功：" +
                                    BleUtil.bytesToHex(justWrite)
                        )
                    )
                }
                onWriteComplete { allSuccess ->
                    //代表所有数据写成功，可以在这个方法中处理成功的逻辑
                    addLogMsg(LogEntity(Level.FINE, "$characteristicId -> 写数据完成，是否成功：$allSuccess"))
                }
            }
        }
    }

    private fun checksum(data: ByteArray, len: Int): Byte {
        var chksum: Byte = 0
        for (i in 0 until len) {
            chksum = (chksum xor data[i])
        }
        return chksum
    }

    /**
     * 获取特征值的属性
     */
    private fun getOperateType(characteristic: BluetoothGattCharacteristic): String {
        val property = StringBuilder()
        val charaProp: Int = characteristic.properties
        if (charaProp and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
            property.append("Read")
            property.append(" , ")
        }
        if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
            property.append("Write")
            property.append(" , ")
        }
        if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
            property.append("Write No Response")
            property.append(" , ")
        }
        if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
            property.append("Notify")
            property.append(" , ")
        }
        if (charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
            property.append("Indicate")
            property.append(" , ")
        }
        if (property.length > 1) {
            property.delete(property.length - 2, property.length - 1)
        }
        return if (property.isNotEmpty()) {
            property.toString()
        } else {
            ""
        }
    }
}