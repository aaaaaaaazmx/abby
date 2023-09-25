package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.bean.UpdateInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.modules_my.repository.MyRepository
import com.cl.modules_my.request.DeviceDetailsBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

class GrowSpaceSetViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {
    /**
     * 删除用户设备
     */
    private val _deleteDevice = MutableLiveData<Resource<BaseBean>>()
    val deleteDevice: LiveData<Resource<BaseBean>> = _deleteDevice
    fun deleteDevice(devId: String) = viewModelScope.launch {
        repository.deleteDevice(devId)
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

    /**
     * 获取属性名
     */
    private val _stranNameList = MutableLiveData<Resource<MutableList<String>>>()
    val stranNameList: LiveData<Resource<MutableList<String>>> = _stranNameList
    fun getName(devId: String) = viewModelScope.launch {
        repository.getStrainName(devId)
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
                _stranNameList.value = it
            }
    }

    /**
     * 修改设备信息
     */
    private val _updateDeviceInfo = MutableLiveData<Resource<BaseBean>>()
    val updateDeviceInfo: LiveData<Resource<BaseBean>> = _updateDeviceInfo
    fun updateDeviceInfo(body: UpDeviceInfoReq) {
        viewModelScope.launch {
            repository.updateDeviceInfo(body)
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
                    _updateDeviceInfo.value = it
                }
        }
    }

    /**
     * 添加帐篷
     */
    private val _addDevice = MutableLiveData<Resource<BaseBean>>()
    val addDevice: LiveData<Resource<BaseBean>> = _addDevice
    fun addDevice(body: DeviceDetailsBean) {
        viewModelScope.launch {
            repository.addDevice(body)
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
                    _addDevice.value = it
                }
        }
    }

    /**
     * 删除配件
     */
    private val _saveCameraSetting = MutableLiveData<Resource<BaseBean>>()
    val saveCameraSetting: LiveData<Resource<BaseBean>> = _saveCameraSetting
    fun cameraSetting(body: UpdateInfoReq) {
        viewModelScope.launch {
            repository.updateInfo(body)
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
                    _saveCameraSetting.value = it
                }
        }
    }

    /**
     * 获取帐篷设备的信息
     */
    private val _deviceDetails = MutableLiveData<Resource<DeviceDetailsBean>>()
    val deviceDetails: LiveData<Resource<DeviceDetailsBean>> = _deviceDetails
    fun getDeviceDetails(deviceId: String) {
        viewModelScope.launch {
            repository.getDeviceDetails(deviceId)
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
                    _deviceDetails.value = it
                }

        }
    }
}