package com.cl.abby.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.UnReadConstants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.modules_home.repository.HomeRepository
import com.cl.common_base.intercome.InterComeHelp
import com.thingclips.smart.android.device.bean.UpgradeInfoBean
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IGetOtaInfoCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@ActivityRetainedScoped
class MainViewModel @Inject constructor(private val repository: HomeRepository) : ViewModel() {
    // 账号
    val account by lazy {
        Prefs.getString(Constants.Login.KEY_LOGIN_ACCOUNT)
    }

    // 密码
    val psd by lazy {
        Prefs.getString(Constants.Login.KEY_LOGIN_PSD)
    }

    val tuYaUser by lazy {
        val bean = Prefs.getString(Constants.Tuya.KEY_DEVICE_USER)
        GSON.parseObject(bean, User::class.java)
    }

    /**
     * 设备信息
     */
    val thingDeviceBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    /**
     * 返回当前设备所有的dps
     * 这里面的dps都是会变化的，需要实时更新
     * 不能直接用
     */
    private val getDeviceDps by lazy {
        thingDeviceBean?.dps
    }

    // 水的容积。=， 多少升
    private val _getWaterVolume =
        MutableLiveData(getDeviceDps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_WATER_STATUS }
            ?.get(TuYaDeviceConstants.KEY_DEVICE_WATER_STATUS).toString())
    val getWaterVolume: LiveData<String> = _getWaterVolume
    fun setWaterVolume(volume: String) {
        // 暂时不做水箱的容积判断，手动赋值默认就是为0L
        _getWaterVolume.value = "0"
    }

    // 保存公英制信息
    private val _saveUnit = MutableLiveData<Boolean>()
    val saveUnit: LiveData<Boolean> = _saveUnit
    fun setSaveUnit(boolean: Boolean) {
        _saveUnit.value = boolean
    }

    // 是否需要修复SN
    // 需要在设备在线的情况下才展示修复
    private val _repairSN = MutableLiveData(if (thingDeviceBean?.isOnline == true) {
        getDeviceDps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_REPAIR_SN }
            ?.get(TuYaDeviceConstants.KEY_DEVICE_REPAIR_SN).toString()
    } else {
        "OK"
    })
    val repairSN: LiveData<String> = _repairSN
    fun setRepairSN(sn: String) {
        _repairSN.value = sn
    }

    /**
     * 获取图文引导
     *
     * 引导类型:0-种植、1-开始种植、2-开始花期、3-开始清洗期、5-开始烘干期、6-完成种植
     */
    private val _getGuideInfo = MutableLiveData<Resource<GuideInfoData>>()
    val getGuideInfo: LiveData<Resource<GuideInfoData>> = _getGuideInfo
    fun getGuideInfo(req: String) {
        viewModelScope.launch {
            repository.getGuideInfo(req).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _getGuideInfo.value = it
            }
        }
    }

    /**
     * 任务更新，推迟到多少天之后
     */
    private val _updateTask = MutableLiveData<Resource<String>>()
    val updateTask: LiveData<Resource<String>> = _updateTask
    fun updateTask(body: UpdateReq) = viewModelScope.launch {
        repository.updateTask(body).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                // 删除第一条信息
                removeFirstUnreadMessage()
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            _updateTask.value = it
        }
    }


    /**
     * 上报引导
     */
    private val _saveOrUpdate = MutableLiveData<Resource<BaseBean>>()
    val saveOrUpdate: LiveData<Resource<BaseBean>> = _saveOrUpdate
    fun saveOrUpdate(req: String) {
        viewModelScope.launch {
            repository.saveOrUpdate(req).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _saveOrUpdate.value = it
            }
        }
    }

    /**
     * 旧的开始种植植物
     */
    private val _startRunning = MutableLiveData<Resource<Boolean>>()
    val startRunning: LiveData<Resource<Boolean>> = _startRunning
    fun startRunning(botanyId: String?, goon: Boolean) {
        viewModelScope.launch {
            repository.startRunning(botanyId, goon).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _startRunning.value = it
            }
        }
    }


    /**
     * 开始种植植物
     */
    private val _start = MutableLiveData<Resource<String>>()
    val start: LiveData<Resource<String>> = _start
    fun start() {
        viewModelScope.launch {
            repository.start().map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _start.value = it
            }
        }
    }

    /**
     * 获取图文广告
     */
    private val _getDetailByLearnMoreId = MutableLiveData<Resource<DetailByLearnMoreIdData>>()
    val getDetailByLearnMoreId: LiveData<Resource<DetailByLearnMoreIdData>> =
        _getDetailByLearnMoreId

    fun getDetailByLearnMoreId(type: String) {
        viewModelScope.launch {
            repository.getDetailByLearnMoreId(type).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _getDetailByLearnMoreId.value = it
            }
        }
    }


    /**
     * 获取图文广告
     */
    private val _getMessageDetail = MutableLiveData<Resource<DetailByLearnMoreIdData>>()
    val getMessageDetail: LiveData<Resource<DetailByLearnMoreIdData>> = _getMessageDetail

    fun getMessageDetail(type: String) {
        viewModelScope.launch {
            repository.getMessageDetail(type).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _getMessageDetail.value = it
            }
        }
    }

    /**
     * 获取图文广告
     */
    private val _advertising = MutableLiveData<Resource<MutableList<AdvertisingData>>>()
    val advertising: LiveData<Resource<MutableList<AdvertisingData>>> = _advertising
    fun advertising(type: String? = "0") {
        viewModelScope.launch {
            repository.advertising(type ?: "0").map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _advertising.value = it
            }
        }
    }


    /**
     * 获取植物的环境信息
     */
    private val _environmentInfo = MutableLiveData<Resource<EnvironmentInfoData>>()
    val environmentInfo: LiveData<Resource<EnvironmentInfoData>> = _environmentInfo
    fun environmentInfo(type: EnvironmentInfoReq) {
        viewModelScope.launch {
            repository.environmentInfo(type).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _environmentInfo.value = it
            }
        }
    }

    /**
     * 获取未读消息
     */
    private val _getUnread = MutableLiveData<Resource<MutableList<UnreadMessageData>>>()
    val getUnread: LiveData<Resource<MutableList<UnreadMessageData>>> = _getUnread
    fun getUnread() {
        viewModelScope.launch {
            getUnread
            repository.getUnread().map {
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
                _getUnread.value = it
            }
        }
    }

    /**
     * 标记已读消息
     */
    private val _getRead = MutableLiveData<Resource<BaseBean>>()
    val getRead: LiveData<Resource<BaseBean>> = _getRead
    fun getRead(messageId: String) {
        viewModelScope.launch {
            repository.getRead(messageId).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    // 删除第一条信息
                    removeFirstUnreadMessage()
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _getRead.value = it
            }
        }
    }

    /**
     * 解锁花期--弃用
     */
    private val _unlockJourney = MutableLiveData<Resource<BaseBean>>()
    val unlockJourney: LiveData<Resource<BaseBean>> = _unlockJourney
    fun unlockJourney(name: String, weight: String? = null) {
        viewModelScope.launch {
            repository.unlockJourney(name, weight).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    // 删除第一条信息
                    removeFirstUnreadMessage()
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _unlockJourney.value = it
            }
        }
    }

    /**
     * 获取消息统计
     */
    private val _getHomePageNumber = MutableLiveData<Resource<HomePageNumberData>>()
    val getHomePageNumber: LiveData<Resource<HomePageNumberData>> = _getHomePageNumber
    fun getHomePageNumber() {
        viewModelScope.launch {
            repository.getHomePageNumber().map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _getHomePageNumber.value = it
            }
        }
    }


    /**
     * 解锁花期、这个是最终的
     */
    private val _finishTask = MutableLiveData<Resource<String>>()
    val finishTask: LiveData<Resource<String>> = _finishTask
    fun finishTask(body: FinishTaskReq) {
        viewModelScope.launch {
            repository.finishTask(body).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    // 删除第一条信息
                    removeFirstUnreadMessage()
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _finishTask.value = it
            }
        }
    }


    /**
     * 检查app版本
     */
    private val _getAppVersion = MutableLiveData<Resource<AppVersionData>>()
    val getAppVersion: LiveData<Resource<AppVersionData>> = _getAppVersion
    fun getAppVersion() {
        viewModelScope.launch {
            repository.getAppVersion().map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _getAppVersion.value = it
            }
        }
    }

    /**
     * 设备操作结束
     */
    private val _deviceOperateFinish = MutableLiveData<Resource<BaseBean>>()
    val deviceOperateFinish: LiveData<Resource<BaseBean>> = _deviceOperateFinish
    fun deviceOperateFinish(type: String) {
        viewModelScope.launch {
            repository.deviceOperateFinish(type).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _deviceOperateFinish.value = it
            }
        }
    }

    /**
     * 设备操作开始
     */
    private val _deviceOperateStart = MutableLiveData<Resource<BaseBean>>()
    val deviceOperateStart: LiveData<Resource<BaseBean>> = _deviceOperateStart
    fun deviceOperateStart(business: String, type: String) {
        viewModelScope.launch {
            repository.deviceOperateStart(business, type).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _deviceOperateStart.value = it
            }
        }
    }

    /**
     * 用户操作到哪一步了，上报 continues1 continues2 continues3
     * 这个是用来当用户取消弹窗时上报的。
     */
    private val _userMessageFlag = MutableLiveData<Resource<BaseBean>>()
    val userMessageFlag: LiveData<Resource<BaseBean>> = _userMessageFlag
    fun userMessageFlag(flagId: String, messageId: String) {
        viewModelScope.launch {
            repository.userMessageFlag(flagId, messageId).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _userMessageFlag.value = it
            }
        }
    }


    /**
     * 设置当前获取图文引导的状态，默认为s0
     */
    private val _currentReqStatus = MutableLiveData(0)
    val currentReqStatus: LiveData<Int> = _currentReqStatus
    fun setCurrentReqStatus(state: Int) {
        _currentReqStatus.value = state
    }

    /**
     * 当前获取引导图文详情到状态，默认为0
     *      * type	引导类型:0-种植、1-开始种植、2-开始花期、3-开始清洗期、5-开始烘干期、6-完成种植
     */
    private val _typeStatus = MutableLiveData<String>()
    val typeStatus: LiveData<String> = _typeStatus
    fun setTypeStatus(status: String) {
        _typeStatus.value = status
    }

    /**
     * 当前周期信息
     */
    private val _periodData = MutableLiveData<MutableList<PlantInfoData.InfoList>>()
    val periodData: LiveData<MutableList<PlantInfoData.InfoList>> = _periodData
    fun setPeriodList(data: MutableList<PlantInfoData.InfoList>) {
        _periodData.value = data
    }

    /**
     * 未读消息列表
     */
    private val _unreadMessageList = MutableLiveData<MutableList<UnreadMessageData>>()
    val unreadMessageList: LiveData<MutableList<UnreadMessageData>> = _unreadMessageList
    fun setUnreadMessageList(list: MutableList<UnreadMessageData>) {
        _unreadMessageList.value = list
    }

    fun getUnreadMessageList(): MutableList<UnreadMessageData> {
        val value = _unreadMessageList.value
        return if (value.isNullOrEmpty()) mutableListOf() else value
    }

    // 删除当前第一条消息
    fun removeFirstUnreadMessage() {
        kotlin.runCatching {
            if (_unreadMessageList.value.isNullOrEmpty()) return
            _unreadMessageList.value?.let { unreadMessage ->
                if (unreadMessage.size == 0) return
                unreadMessage.removeFirst()
            }
        }
    }

    // 删除所有未读消息
    fun removeUnreadMessage() {
        _unreadMessageList.value?.clear()
    }

    // 固件信息
    // 获取当前设备信息
    private val tuYaDeviceBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    private val tuYaHomeSdk by lazy {
        ThingHomeSdk.newOTAInstance(tuYaDeviceBean?.devId)
    }

    /**
     * 查询固件升级信息
     */
    fun checkFirmwareUpdateInfo(
        onOtaInfo: ((upgradeInfoBeans: MutableList<UpgradeInfoBean>?, isShow: Boolean) -> Unit)? = null,
    ) {
        tuYaHomeSdk.getOtaInfo(object : IGetOtaInfoCallback {
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
                Reporter.reportTuYaError("getOtaInfo", error, code)
            }
        })
    }


    /**
     * 获取种植完成界面参数
     */
    private val _getFinishPage = MutableLiveData<Resource<FinishPageData>>()
    val getFinishPage: LiveData<Resource<FinishPageData>> = _getFinishPage
    fun getFinishPage() {
        viewModelScope.launch {
            repository.getFinishPage().map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _getFinishPage.value = it
            }
        }
    }

    /**
     * 删除植物
     */
    private val _plantFinish = MutableLiveData<Resource<BaseBean>>()
    val plantFinish: LiveData<Resource<BaseBean>> = _plantFinish
    fun plantFinish(botanyId: String) = viewModelScope.launch {
        repository.plantFinish(botanyId).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                // 检查是否种植过
                tuYaUser?.uid?.let { uid -> checkPlant(uid) }
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            _plantFinish.value = it
        }
    }

    /**
     * 检查是否种植过植物
     */
    private val _checkPlant = MutableLiveData<Resource<CheckPlantData>>()
    val checkPlant: LiveData<Resource<CheckPlantData>> = _checkPlant
    private fun checkPlant(uuid: String) = viewModelScope.launch {
        repository.checkPlant(uuid).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            _checkPlant.value = it
        }
    }

    /**
     * 获取用户信息
     */
    private val _userDetail = MutableLiveData<Resource<UserinfoBean.BasicUserBean>>()
    val userDetail: LiveData<Resource<UserinfoBean.BasicUserBean>> = _userDetail
    fun userDetail() = viewModelScope.launch {
        repository.userDetail().map {
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
            _userDetail.value = it
        }
    }

    /**
     * 是否种植
     */
    private val _isPlant = MutableLiveData<Boolean>()
    val isPlant: LiveData<Boolean> = _isPlant
    fun setIsPlants(boolean: Boolean) {
        _isPlant.postValue(boolean)
    }


    /**
     * 检查固件是否可以升级
     */
    private fun hasHardwareUpdate(list: MutableList<UpgradeInfoBean>?): Boolean {
        if (null == list || list.size == 0) return false
        return list.firstOrNull { it.type == 9 }?.upgradeStatus == 1
    }

    /**
     * 继承之后选择的状态
     */

    /**
     *  周期弹窗时的状态选择，目前此状态只用于周期弹窗，目的是为了解锁，后期可以优化
     *  周期Id
     *  改动为map集合，保存了解锁ID（图文展示）、以及TaskId（解锁）
     */
    private val _popPeriodStatus = MutableLiveData<HashMap<String, String?>?>(hashMapOf())
    val popPeriodStatus: LiveData<HashMap<String, String?>?> = _popPeriodStatus
    fun setPopPeriodStatus(
        guideId: String? = null, taskId: String? = null, taskTime: String? = null
    ) {
        guideId?.let {
            _popPeriodStatus.value?.set(KEY_GUIDE_ID, it)
            // 获取图文引导，然后解锁。
            when (it) {
                UnReadConstants.Device.KEY_CHANGING_WATER -> {}
                UnReadConstants.Device.KEY_ADD_WATER -> {}
                UnReadConstants.Device.KEY_ADD_MANURE -> {}
                else -> {
                    getGuideInfo(it)
                }
            }
        }
        _popPeriodStatus.value?.set(KEY_TASK_ID, taskId)
        _popPeriodStatus.value?.set(KEY_TASK_TIME, taskTime)
    }

    // 清空
    fun clearPopPeriodStatus() {
        if (_popPeriodStatus.value.isNullOrEmpty()) return
        _popPeriodStatus.value?.clear()
    }

    /**
     * 气泡的点击事件
     */
    private val _bubbleOnClickEvent = MutableLiveData(false)
    val bubbleOnClickEvent: LiveData<Boolean?> = _bubbleOnClickEvent
    fun bubbleOnClickEvent() {
        _bubbleOnClickEvent.value = true
    }

    /**
     * 已读全部消息
     *
     * 后台没有做这个操作
     */
    fun unReadAll() {
        if (unreadMessageList.value?.isNotEmpty() == true) {
            unreadMessageList.value?.forEach {
                getRead("${it.messageId}")
            }
        }
    }

    /**
     * 获取环信未读消息
     */
    private val _unReadMessageNumber = MutableLiveData<Int>(0)
    val unReadMessageNumber: LiveData<Int?> = _unReadMessageNumber
    private fun getEaseUINumber() {
        // 只有当设备绑定且在线的时候、才去添加
        // if (refreshToken.value?.data?.deviceStatus == "1" && refreshToken.value?.data?.deviceOnlineStatus == "1") {
            _unReadMessageNumber.postValue(InterComeHelp.INSTANCE.getUnreadConversationCount())
        // }
    }


    /**
     * 请求消息数量接口
     */
    fun getMessageNumber() {
        // 获取环信消息数量
        getEaseUINumber()
        // 获取设备环境消息
        environmentInfo(EnvironmentInfoReq(deviceId = thingDeviceBean?.devId))
        getHomePageNumber()
    }

    /**
     * 定时器
     */
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
        }.flowOn(Dispatchers.Main).onStart { onStart?.invoke() }.onCompletion { onFinish?.invoke() }
            .onEach { onTick.invoke(it) }.launchIn(scope)
    }

    companion object {
        const val KEY_GUIDE_ID = "key_guide_id"
        const val KEY_TASK_ID = "key_task_id"
        const val KEY_TASK_TIME = "key_task_time"
    }
}