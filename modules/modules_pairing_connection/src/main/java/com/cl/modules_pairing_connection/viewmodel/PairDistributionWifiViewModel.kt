package com.cl.modules_pairing_connection.viewmodel

import android.Manifest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AccessoryAddData
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.EnvironmentInfoReq
import com.cl.common_base.bean.SyncDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.letMultiple
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.modules_pairing_connection.repository.PairRepository
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * 配网ViewModel
 *
 * @author 李志军 2022-08-04 10:12
 */
@ActivityRetainedScoped
class PairDistributionWifiViewModel @Inject constructor(private val repository: PairRepository) :
    ViewModel() {

    val wifiName by lazy {
        Prefs.getString(Constants.Pair.KEY_PAIR_WIFI_NAME)
    }

    val wifiPsd by lazy {
        Prefs.getString(Constants.Pair.KEY_PAIR_WIFI_PASSWORD)
    }

    private val _passWordState = MutableLiveData<Boolean>(false)
    val passWordState: LiveData<Boolean> = _passWordState

    fun setPassWordState(state: Boolean) {
        _passWordState.value = state
    }

    /**
     * 上传后台接口绑定
     */
    private val _bindDevice = MutableLiveData<Resource<String>>()
    val bindDevice: LiveData<Resource<String>> = _bindDevice
    private fun bindDevice(deviceId: String, deviceUuid: String) = viewModelScope.launch {
        repository.bindDevice(deviceId, deviceUuid)
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
                _bindDevice.value = it
            }
    }

    /**
     * 获取用户信息
     */
    private val _userDetail = MutableLiveData<Resource<UserinfoBean.BasicUserBean>>()
    val userDetail: LiveData<Resource<UserinfoBean.BasicUserBean>> = _userDetail
    fun userDetail() = viewModelScope.launch {
        repository.userDetail()
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
                _userDetail.value = it
            }
    }


    /**
     * 检查过是否种植过植物
     */
    private val _checkPlant = MutableLiveData<Resource<CheckPlantData>>()
    val checkPlant: LiveData<Resource<CheckPlantData>> = _checkPlant
    fun checkPlant() = viewModelScope.launch {
        repository.checkPlant()
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
                _checkPlant.value = it
            }
    }

    /**
     * 设备信息同步
     */
    // syncDeviceInfo
    private val _syncDeviceInfo = MutableLiveData<Resource<BaseBean>>()
    val syncDeviceInfo: LiveData<Resource<BaseBean>> = _syncDeviceInfo
    private fun syncDeviceInfo(
        body: SyncDeviceInfoReq
    ) = viewModelScope.launch {
        repository.syncDeviceInfo(body)
            .map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code,
                        it.msg
                    )
                } else {
                    // 绑定设备
                    letMultiple(bean?.devId, bean?.uuid) { devId, uuid ->
                        bindDevice(devId, uuid)
                    }
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
                _syncDeviceInfo.value = it
            }
    }

    fun countDownCoroutines(
        total: Int,
        scope: CoroutineScope,
        onTick: (Int) -> Unit,
        onStart: (() -> Unit)? = null,
        onFinish: (() -> Unit)? = null,
    ): Job {
        return flow {
            for (i in total downTo 0) {
                emit(i)
                delay(1000)
            }
        }.flowOn(Dispatchers.Main)
            .onStart { onStart?.invoke() }
            .onCompletion { onFinish?.invoke() }
            .onEach { onTick.invoke(it) }
            .launchIn(scope)
    }

    // 设置Bean
    var bean: DeviceBean? = null

    // 数据同步
    fun getDps(bean: DeviceBean?) {
        bean?.let {
            // 设置Bean
            this@PairDistributionWifiViewModel.bean = it
            val envReq = SyncDeviceInfoReq(deviceId = it.devId)
            it.dps?.forEach { (key, value) ->
                when (key) {
                    TuYaDeviceConstants.KEY_DEVICE_AIR_PUMP -> {
                        envReq.airPump = value.toString().toBooleanStrictOrNull()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_BRIGHT_VALUE -> {
                        envReq.brightValue = value.safeToInt()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_REPAIR_REST_STATUS -> {
                        envReq.deviceStatus = value.toString()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_DOOR -> {
                        envReq.door = value.toString().toBooleanStrictOrNull()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_FAN_ENABLE -> {
                        envReq.fanEnable = value.toString().toBooleanStrictOrNull()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_PLANT_HEIGHT -> {
                        envReq.height = value.safeToInt()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_HUMIDITY_CURRENT -> {
                        envReq.humidityCurrent = value.safeToInt()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_INPUT_AIR_FLOW -> {
                        envReq.inputAirFlow = value.safeToInt()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_SILENT_MODE -> {
                        envReq.silentMode = value.toString()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_SWITCH -> {
                        envReq.startPlant = value.toString().toBooleanStrictOrNull()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_TEMP_CURRENT -> {
                        envReq.tempCurrent = value.safeToInt()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_TURN_OFF_LIGHT -> {
                        envReq.turnOffLight = value.safeToInt()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_TURN_ON_THE_LIGHT -> {
                        envReq.turnOnTheLight = value.safeToInt()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_VENTILATION -> {
                        envReq.ventilation = value.safeToInt()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_WATER_LEVEL -> {
                        envReq.waterLevel = value.toString()
                    }
                    TuYaDeviceConstants.KEY_DEVICE_WATER_TEMPERATURE -> {
                        envReq.waterTemperature = value.safeToInt()
                    }
                }
            }
            // 请求环境信息
            syncDeviceInfo(envReq)
        }
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
}