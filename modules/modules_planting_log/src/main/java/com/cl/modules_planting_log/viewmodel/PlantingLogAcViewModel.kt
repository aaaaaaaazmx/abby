package com.cl.modules_planting_log.viewmodel

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
import com.cl.common_base.bean.CharacteristicNode
import com.cl.common_base.bean.ImageUrl
import com.cl.common_base.bean.LogEntity
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.bean.RefreshBleDevice
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_planting_log.repository.PlantRepository
import com.cl.modules_planting_log.request.LogByIdData
import com.cl.modules_planting_log.request.LogListDataItem
import com.cl.modules_planting_log.request.LogListReq
import com.cl.modules_planting_log.request.LogSaveOrUpdateReq
import com.cl.modules_planting_log.request.LogTypeListDataItem
import com.cl.modules_planting_log.request.PlantIdByDeviceIdData
import com.cl.modules_planting_log.request.PlantInfoByPlantIdData
import com.thingclips.smart.sdk.bean.DeviceBean
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.logging.Level
import javax.inject.Inject

@ActivityRetainedScoped
class PlantingLogAcViewModel @Inject constructor(private val repository: PlantRepository) : ViewModel() {

    // 是否是公制
    val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)

    val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    /**
     * 图片上传船地址结合
     */
    private val _picAddress = MutableLiveData<MutableList<ImageUrl>>(mutableListOf())
    val picAddress: LiveData<MutableList<ImageUrl>> = _picAddress
    fun setPicAddress(url: ImageUrl) {
        _picAddress.value?.add(0, url)
    }
    fun deletePicAddress(index: Int) {
        if ((_picAddress.value?.size ?: 0) > 0) {
            _picAddress.value?.removeAt(index)
        }
    }

    fun clearPicAddress() {
        _picAddress.value?.clear()
    }

    // 上传Train的时候的两张照片
    private val _beforePicAddress = MutableLiveData<String?>()
    val beforePicAddress: LiveData<String?> = _beforePicAddress
    fun setBeforeAddress(address: String) {
        _beforePicAddress.value = address
    }
    fun setClearBeforeAddress() {
        _beforePicAddress.value = null
    }

    private val _afterPicAddress = MutableLiveData<String?>()
    val afterPicAddress: LiveData<String?> = _afterPicAddress
    fun setAfterAddress(address: String) {
        _afterPicAddress.value = address
    }
    fun setClearAfterAddress() {
        _afterPicAddress.value = null
    }

    private val _chooserTips = MutableLiveData<Boolean>()
    val chooserTips: LiveData<Boolean> = _chooserTips
    fun setChooserTips(address: Boolean) {
        _chooserTips.value = address
    }




    /**
     * 表单提交
     * 需要循环上传
     */
    fun submitTheForm(path: String): List<MultipartBody.Part> {
        //1.创建MultipartBody.Builder对象
        val builder = MultipartBody.Builder()
            //表单类型
            .setType(MultipartBody.FORM)

        //2.获取图片，创建请求体
        val file = File(path)
        //表单类x型
        //表单类型
        val body: RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)

        //3.调用MultipartBody.Builder的addFormDataPart()方法添加表单数据
        /**
         * ps:builder.addFormDataPart("code","123456");
         * ps:builder.addFormDataPart("file",file.getName(),body);
         */
        builder.addFormDataPart("imgType", "trend") //传入服务器需要的key，和相应value值
        builder.addFormDataPart("files", file.name, body) //添加图片数据，body创建的请求体
        //4.创建List<MultipartBody.Part> 集合，
        //  调用MultipartBody.Builder的build()方法会返回一个新创建的MultipartBody
        //  再调用MultipartBody的parts()方法返回MultipartBody.Part集合
        return builder.build().parts
    }

    /**
     * 上传多张图片
     */
    private val _uploadImg = MutableLiveData<Resource<MutableList<String>>>()
    val uploadImg: LiveData<Resource<MutableList<String>>> = _uploadImg
    fun uploadImg(body: List<MultipartBody.Part>) = viewModelScope.launch {
        repository.uploadImages(body)
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
                        "${it.message}"
                    )
                )
            }.collectLatest {
                _uploadImg.value = it
            }
    }

    /**
     * 新增或者修改日志详情
     */
    private val _logSaveOrUpdate = MutableLiveData<Resource<Boolean>>()
    val logSaveOrUpdate: LiveData<Resource<Boolean>> = _logSaveOrUpdate
    fun saveOrUpdateLog(logSaveOrUpdateReq: LogSaveOrUpdateReq) {
        viewModelScope.launch {
            repository.logSaveOrUpdate(logSaveOrUpdateReq).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {}.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _logSaveOrUpdate.value = it
            }
        }
    }

    /**
     * 获取日志类型列表
     */
    private val _getLogTypeList = MutableLiveData<Resource<List<LogTypeListDataItem>>>()
    val getLogTypeList: LiveData<Resource<List<LogTypeListDataItem>>> = _getLogTypeList
    fun getLogTypeList(showType: String, logId: String?) {
        viewModelScope.launch {
            repository.getLogTypeList(showType, logId).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {}.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _getLogTypeList.value = it
            }
        }
    }


    /**
     * 获取日志详情
     */
    private val _getLogById = MutableLiveData<Resource<LogSaveOrUpdateReq>>()
    val getLogById: LiveData<Resource<LogSaveOrUpdateReq>> = _getLogById
    fun getLogById(logId: String?) {
        viewModelScope.launch {
            repository.getLogById(logId).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {}.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _getLogById.value = it
            }
        }
    }


    /**
     * 获取植物基本信息
     */
    private val _plantInfo = MutableLiveData<Resource<PlantInfoData>>()
    val plantInfo: LiveData<Resource<PlantInfoData>> = _plantInfo
    fun plantInfo() {
        viewModelScope.launch {
            repository.plantInfo().map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {}.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _plantInfo.value = it
            }
        }
    }

    /**
     * 根据DeviceId获取植物Id
     */
    private val _getPlantIdByDeviceId = MutableLiveData<Resource<MutableList<PlantIdByDeviceIdData>>>()
    val getPlantIdByDeviceId: LiveData<Resource<MutableList<PlantIdByDeviceIdData>>> = _getPlantIdByDeviceId
    fun getPlantIdByDeviceId(deviceId: String) = viewModelScope.launch {
        repository.getPlantIdByDeviceId(deviceId).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _getPlantIdByDeviceId.value = it
        }
    }

    /**
     * 根据植物ID获取植物的信息
     */
    private val _getPlantInfoByPlantId = MutableLiveData<Resource<PlantInfoByPlantIdData>>()
    val getPlantInfoByPlantId: LiveData<Resource<PlantInfoByPlantIdData>> = _getPlantInfoByPlantId
    fun getPlantInfoByPlantId(plantId: Int) = viewModelScope.launch {
        repository.getPlantInfoByPlantId(plantId).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _getPlantInfoByPlantId.value = it
        }
    }

    /**
     * 根据植物Id、和植物周期、获取植物日志列表
     */
    private val _getLogList = MutableLiveData<Resource<MutableList<LogListDataItem>>>()
    val getLogList: LiveData<Resource<MutableList<LogListDataItem>>> = _getLogList
    fun getLogList(body: LogListReq) = viewModelScope.launch {
        repository.getLogList(body).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _getLogList.value = it
        }
    }


    /**
     * 蓝牙相关
     */
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
                    else -> ""
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
     * 断开Ph设备
     */
    fun disConnectPhDevice() {
        BleManager.get().getAllConnectedDevice()?.firstOrNull { it.deviceName == Constants.Ble.KEY_PH_DEVICE_NAME }?.let {
            BleManager.get().disConnect(it)
        }
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
                "Disconnect"
            )
            refreshMutableStateFlow.value = RefreshBleDevice(bleDevice, System.currentTimeMillis())
            //发送断开的通知
            /* val message = MessageEvent()
             message.data = bleDevice
             EventBus.getDefault().post(message)*/
        }
        onConnectSuccess { bleDevice, _ ->
            logI("BLe -> msg: Connection successful: (${bleDevice.deviceAddress})")
            // ToastUtil.shortShow("Connection successful:${bleDevice.deviceName}")
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


}