package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.bean.AppVersionData
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.modules_my.repository.MyRepository
import com.cl.modules_my.request.ModifyUserDetailReq
import com.tuya.smart.android.device.bean.UpgradeInfoBean
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.IGetOtaInfoCallback
import com.tuya.smart.sdk.api.IResultCallback
import com.tuya.smart.sdk.bean.DeviceBean
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class SettingViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {

    /**
     * 删除用户设备
     */
    private val _deleteDevice = MutableLiveData<Resource<BaseBean>>()
    val deleteDevice: LiveData<Resource<BaseBean>> = _deleteDevice
    fun deleteDevice() = viewModelScope.launch {
        repository.deleteDevice()
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
     * 删除植物
     */
    private val _plantDelete = MutableLiveData<Resource<Boolean>>()
    val plantDelete: LiveData<Resource<Boolean>> = _plantDelete
    fun plantDelete(uuid: String) = viewModelScope.launch {
        repository.plantDelete(uuid)
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
                _plantDelete.value = it
            }
    }


    /**
     * 检查是否种植过植物
     */
    private val _checkPlant = MutableLiveData<Resource<CheckPlantData>>()
    val checkPlant: LiveData<Resource<CheckPlantData>> = _checkPlant
    fun checkPlant(uuid: String) = viewModelScope.launch {
        repository.checkPlant(uuid)
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
     * 检查app版本
     */
    private val _getAppVersion = MutableLiveData<Resource<AppVersionData>>()
    val getAppVersion: LiveData<Resource<AppVersionData>> = _getAppVersion
    fun getAppVersion() {
        viewModelScope.launch {
            repository.getAppVersion()
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
                    _getAppVersion.value = it
                }
        }
    }

    /**
     * 更新用户信息
     */
    private val _modifyUserDetail = MutableLiveData<Resource<Boolean>>()
    val modifyUserDetail: LiveData<Resource<Boolean>> = _modifyUserDetail
    fun modifyUserDetail(body: ModifyUserDetailReq) = viewModelScope.launch {
        repository.modifyUserDetail(body)
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
                _modifyUserDetail.value = it
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


    // 获取当前设备信息
    private val tuYaDeviceBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    /**
     * 查询固件升级信息
     */
    fun checkFirmwareUpdateInfo(
        onOtaInfo: ((upgradeInfoBeans: MutableList<UpgradeInfoBean>?, isShow: Boolean) -> Unit)? = null,
    ) {
        TuyaHomeSdk.newOTAInstance(tuYaDeviceBean?.devId).getOtaInfo(object : IGetOtaInfoCallback {
            override fun onSuccess(upgradeInfoBeans: MutableList<UpgradeInfoBean>?) {
                logI("getOtaInfo:  ${GSON.toJson(upgradeInfoBeans?.firstOrNull { it.type == 9 })}")
                // 如果可以升级
                if (hasHardwareUpdate(upgradeInfoBeans)) {
                    onOtaInfo?.invoke(upgradeInfoBeans, true)
                } else {
                    // 如果不可以升级过
                    onOtaInfo?.invoke(upgradeInfoBeans, false)
                }
            }

            override fun onFailure(code: String?, error: String?) {
                logI(
                    """
                        getOtaInfo:
                        code: $code
                        error: $error
                    """.trimIndent()
                )
                Reporter.reportTuYaError("newOTAInstance", error, code)
            }
        })
    }

    /**
     * 获取图文广告
     */
    private val _advertising = MutableLiveData<Resource<MutableList<AdvertisingData>>>()
    val advertising: LiveData<Resource<MutableList<AdvertisingData>>> = _advertising
    fun advertising(type: String? = "0") {
        viewModelScope.launch {
            repository.advertising(type ?: "0")
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
                    _advertising.value = it
                }
        }
    }


    // 获取SN
    fun getSn() {
        TuyaHomeSdk.newDeviceInstance(tuYaDeviceBean?.devId)?.let {
            it.getDp(TuYaDeviceConstants.KEY_DEVICE_REPAIR_REST_STATUS, object : IResultCallback {
                override fun onError(code: String?, error: String?) {
                    logI(
                        """
                        KEY_DEVICE_REPAIR_REST_STATUS: error
                        code: $code
                        error: $error
                    """.trimIndent()
                    )
                    Reporter.reportTuYaError("newDeviceInstance", error, code)
                }

                override fun onSuccess() {
                    logI("sdasdas")
                }
            })
        }
    }

    // 获取激活状态
    fun getActivationStatus() {
        TuyaHomeSdk.newDeviceInstance(tuYaDeviceBean?.devId)?.let {
            it.getDp(TuYaDeviceConstants.KEY_DEVICE_REPAIR_SN, object : IResultCallback {
                override fun onError(code: String?, error: String?) {
                    logI(
                        """
                        KEY_DEVICE_REPAIR_REST_STATUS: error
                        code: $code
                        error: $error
                    """.trimIndent()
                    )
                    Reporter.reportTuYaError("newDeviceInstance", error, code)
                }

                override fun onSuccess() {
                logI("sdasdas")
                }
            })
        }
    }

    /**
     * 检查固件是否可以升级
     */
    private fun hasHardwareUpdate(list: MutableList<UpgradeInfoBean>?): Boolean {
        if (null == list || list.size == 0) return false
        return list.firstOrNull { it.type == 9 }?.upgradeStatus == 1
    }

    /**
     * 设备是否在线
     */
    private val _isOffLine = MutableLiveData<Boolean>(false)
    val isOffLine: LiveData<Boolean> = _isOffLine
    fun setOffLine(offline: Boolean) {
        _isOffLine.value = offline
    }

}