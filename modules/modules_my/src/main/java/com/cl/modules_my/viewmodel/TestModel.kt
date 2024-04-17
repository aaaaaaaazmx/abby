package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.repository.MyRepository
import com.cl.common_base.bean.ModifyUserDetailReq
import com.cl.modules_my.repository.UsbSwitchReq
import com.thingclips.smart.android.device.bean.UpgradeInfoBean
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IGetOtaInfoCallback
import com.thingclips.smart.sdk.api.IResultCallback
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class TestModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {

    /**
     * refreshToken 接口返回
     */
    val deviceInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    /**
     * 保存当前设备ID
     */
    private val _saveDeviceId = MutableLiveData<String?>()
    val saveDeviceId: LiveData<String?> = _saveDeviceId
    fun saveDeviceId(id: String?) {
        _saveDeviceId.value = id
    }

    /**
     * 删除用户设备
     */
    private val _deleteDevice = MutableLiveData<Resource<BaseBean>>()
    val deleteDevice: LiveData<Resource<BaseBean>> = _deleteDevice
    fun deleteDevice(deviceId: String) = viewModelScope.launch {
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
                        "${it.message}"
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
                        "${it.message}"
                    )
                )
            }.collectLatest {
                _plantDelete.value = it
            }
    }


    /**
     * 删除植物
     */
    private val _getPlantData = MutableLiveData<Resource<PlantData>>()
    val getPlantData: LiveData<Resource<PlantData>> = _getPlantData
    fun getPlantData(uuid: String) = viewModelScope.launch {
        repository.getPlantData(uuid)
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
                _getPlantData.value = it
            }
    }


    /**
     * 检查是否种植过植物
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
                _userDetail.value = it
            }
    }

    /**
     * 合并账号
     */
    private val _listDevice = MutableLiveData<Resource<MutableList<ListDeviceBean>>>()
    val listDevice: LiveData<Resource<MutableList<ListDeviceBean>>> = _listDevice
    fun listDevice() {
        viewModelScope.launch {
            repository.listDevice()
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
                    _listDevice.value = it
                }
        }
    }

    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    /**
     * 查询固件升级信息
     */
    fun checkFirmwareUpdateInfo(
        onOtaInfo: ((upgradeInfoBeans: MutableList<UpgradeInfoBean>?, isShow: Boolean) -> Unit)? = null,
    ) {
        userInfo?.deviceId?.let {
            ThingHomeSdk.newOTAInstance(it).getOtaInfo(object : IGetOtaInfoCallback {
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

    /**
     * 放弃种子检查
     */
    private val _giveUpCheck = MutableLiveData<Resource<GiveUpCheckData>>()
    val giveUpCheck: LiveData<Resource<GiveUpCheckData>> = _giveUpCheck
    fun giveUpCheck() {
        viewModelScope.launch {
            repository.giveUpCheck()
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
                    _giveUpCheck.value = it
                }
        }
    }


    // 获取SN
    fun getSn() {
        ThingHomeSdk.newDeviceInstance(userInfo?.deviceId)?.let {
            it.getDp(TuYaDeviceConstants.KEY_DEVICE_REPAIR_REST_STATUS, object : IResultCallback {
                override fun onError(code: String?, error: String?) {
                    logI(
                        """
                        KEY_DEVICE_REPAIR_REST_STATUS: error
                        code: $code
                        error: $error
                    """.trimIndent()
                    )
                    ToastUtil.shortShow(error)
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
        ThingHomeSdk.newDeviceInstance(userInfo?.deviceId)?.let {
            it.getDp(TuYaDeviceConstants.KEY_DEVICE_REPAIR_SN, object : IResultCallback {
                override fun onError(code: String?, error: String?) {
                    logI(
                        """
                        KEY_DEVICE_REPAIR_REST_STATUS: error
                        code: $code
                        error: $error
                    """.trimIndent()
                    )
                    ToastUtil.shortShow(error)
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
     * 是否是修改的usb
     */
    private val _usbUpdate = MutableLiveData<Boolean>()
    val uspUpdate: LiveData<Boolean> = _usbUpdate
    fun setUsbUpdate(update: Boolean) {
        _usbUpdate.value = update
    }

    /**
     * 修改植物信息
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
     * 修改植物信息
     */
    private val _updatePlantInfo = MutableLiveData<Resource<BaseBean>>()
    val updatePlantInfo: LiveData<Resource<BaseBean>> = _updatePlantInfo
    fun updatePlantInfo(body: UpPlantInfoReq) {
        viewModelScope.launch {
            repository.updatePlantInfo(body)
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
                    _updatePlantInfo.value = it
                }
        }
    }

    /**
     * 设备是否在线
     * false、不在线 true、 在线
     */
    private val _isOffLine = MutableLiveData<Boolean>(true)
    val isOffLine: LiveData<Boolean> = _isOffLine
    fun setOffLine(offline: Boolean) {
        _isOffLine.value = offline
    }

    /**
     * 获取当前设备信息
     */
    private val _devicesInfo = MutableLiveData<ListDeviceBean>()
    val devicesInfo: LiveData<ListDeviceBean> = _devicesInfo
    fun updateDevicesInfo(info: ListDeviceBean) {
        _devicesInfo.value = info
    }

    /**
     * 是否是点击采取查询升级信息
     */
    private val _isClickUpdate = MutableLiveData<Boolean>(false)
    val isClickUpdate: LiveData<Boolean> = _isClickUpdate
    fun setClickUpdate(isClickUpdate: Boolean) {
        _isClickUpdate.value = isClickUpdate
    }

    /**
     * usb选择
     */
    private val _usbSwitch = MutableLiveData<Resource<Boolean>>()
    val usbSwitch: LiveData<Resource<Boolean>> = _usbSwitch
    fun usbSwitch(body: UsbSwitchReq) {
        viewModelScope.launch {
            repository.usbSwitch(body)
                .map {
                    if (it.code != Constants.APP_SUCCESS) {
                        Resource.DataError(
                            it.code,
                            it.msg
                        )
                    } else {
                        Resource.Success(true)
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
                    _usbSwitch.value = it
                }
        }
    }

    /**
     * 获取所有dp点
     */
    private val _getDp = MutableLiveData<Resource<UsbSwitchReq>>()
    val getDp: LiveData<Resource<UsbSwitchReq>> = _getDp
    fun getDp() {
        viewModelScope.launch {
            repository.getDpCache(deviceInfo?.deviceId ?: "")
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
                    _getDp.value = it
                }
        }
    }
}