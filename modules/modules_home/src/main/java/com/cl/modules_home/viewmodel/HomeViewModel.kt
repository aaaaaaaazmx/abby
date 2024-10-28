package com.cl.modules_home.viewmodel

import android.content.Context
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
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToDouble
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.modules_home.repository.HomeRepository
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.pop.MedalPop
import com.cl.common_base.util.device.TuyaCameraUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk
import com.thingclips.smart.android.device.bean.UpgradeInfoBean
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.api.IGetOtaInfoCallback
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.system.exitProcess


@ActivityRetainedScoped
class HomeViewModel @Inject constructor(private val repository: HomeRepository, @ApplicationContext private val context: Context) : ViewModel() {
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

    val homeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }


    /**
     * 涂鸦摄像头帮助类
     */
    val tuYaUtils by lazy {
        TuyaCameraUtils()
    }

    /**
     * 设备信息
     */
    val thingDeviceBean = {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    private val _deviceId =
        MutableLiveData(thingDeviceBean()?.devId.toString())
    val deviceId: LiveData<String> = _deviceId
    fun setDeviceId(deviceId: String) {
        // 暂时不做水箱的容积判断，手动赋值默认就是为0L
        _deviceId.value = deviceId
    }

    private val _deviceInfo = MutableLiveData<LiveDataDeviceInfoBean>()
    val deviceInfo: LiveData<LiveDataDeviceInfoBean> = _deviceInfo
    fun setDeviceInfo(deviceId: LiveDataDeviceInfoBean) {
        _deviceInfo.value = deviceId
    }


    // 童锁的开闭状态
    val _childLockStatus = MutableLiveData(
        thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_CHILD_LOCK }
            ?.get(TuYaDeviceConstants.KEY_DEVICE_CHILD_LOCK).toString()
    )
    val childLockStatus: LiveData<String> = _childLockStatus
    fun setChildLockStatus(status: String) {
        _childLockStatus.value = status
    }

    // 门的开闭状态
    private val _openDoorStatus = MutableLiveData(
        thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_DOOR_LOOK }
            ?.get(TuYaDeviceConstants.KEY_DEVICE_DOOR_LOOK).toString()
    )
    val openDoorStatus: LiveData<String> = _openDoorStatus
    fun setOpenDoorStatus(status: String) {
        _openDoorStatus.value = status
    }

    fun isShowDoorDrawable(): Boolean {
        return _openDoorStatus.value == "true" && childLockStatus.value == "true"
    }


    /**
     * 返回当前设备所有的dps
     * 这里面的dps都是会变化的，需要实时更新
     * 不能直接用
     */
    private val getDeviceDps = {
        thingDeviceBean()?.dps
    }

    // 水的容积。=， 多少升
    private val _getWaterVolume =
        MutableLiveData(getDeviceDps()?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_WATER_STATUS }
            ?.get(TuYaDeviceConstants.KEY_DEVICE_WATER_STATUS).toString())
    val getWaterVolume: LiveData<String> = _getWaterVolume
    fun setWaterVolume(volume: String) {
        // 暂时不做水箱的容积判断，手动赋值默认就是为0L
        _getWaterVolume.value = "0"
    }

    // 是否需要修复SN
    // 需要在设备在线的情况下才展示修复
    private val _repairSN = MutableLiveData(if (thingDeviceBean()?.isOnline == true) {
        getDeviceDps()?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_REPAIR_SN }
            ?.get(TuYaDeviceConstants.KEY_DEVICE_REPAIR_SN).toString()
    } else {
        "OK"
    })
    val repairSN: LiveData<String> = _repairSN
    fun setRepairSN(sn: String) {
        _repairSN.value = sn
    }

    /**
     * 获取氧气币列表
     */
    private val _getOxygenCoinList = MutableLiveData<Resource<MutableList<OxygenCoinListBean>>>()
    val getOxygenCoinList: LiveData<Resource<MutableList<OxygenCoinListBean>>> = _getOxygenCoinList
    fun getOxygenCoinList() {
        viewModelScope.launch {
            repository.oxygenCoinList()
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
                    _getOxygenCoinList.value = it
                }
        }
    }


    // 当前view的数量
    private val _viewCount = MutableLiveData(0)
    val viewCount: LiveData<Int> = _viewCount
    fun setViewCount(count: Int) {
        _viewCount.value = count
    }

    /**
     * 领取氧气币
     */
    private val _getOxygenCoin = MutableLiveData<Resource<BaseBean>>()
    val getOxygenCoin: LiveData<Resource<BaseBean>> = _getOxygenCoin
    fun getOxygenCoin(body: String) {
        viewModelScope.launch {
            repository.getGrantOxygen(body)
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
                    _getOxygenCoin.value = it
                }
        }
    }

    /**
     * 修改植物信息
     */
    private val _updatePlantInfo = MutableLiveData<Resource<BaseBean>>()
    val updatePlantInfo: LiveData<Resource<BaseBean>> = _updatePlantInfo
    fun updatePlantInfo(body: UpDeviceInfoReq) {
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
                    _updatePlantInfo.value = it
                }
        }
    }

    /**
     * refreshToken
     */
    private val _refreshToken = MutableLiveData<Resource<AutomaticLoginData>>()
    val refreshToken: LiveData<Resource<AutomaticLoginData>> = _refreshToken
    fun refreshToken(req: AutomaticLoginReq) {
        viewModelScope.launch {
            repository.automaticLogin(req).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    // 登录InterCome
                    // easeLogin(it.data.userId, it.data)
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
                _refreshToken.value = it
            }
        }
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
     * 获取植物基本信息\Look接口
     * 主要是用于查询当前是否是植物的关灯模式、也即是显示zzz的图片
     */
    private val _plantInfoLoop = MutableLiveData<Resource<PlantInfoData>>()
    val plantInfoLoop: LiveData<Resource<PlantInfoData>> = _plantInfoLoop
    fun plantInfoLoop() {
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
                _plantInfoLoop.value = it
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
                when (it) {
                    is Resource.Success -> {
                        val originalData = it.data
                        val envList = it.data?.environments
                        if (envList.isNullOrEmpty()) {
                            _environmentInfo.value = it
                            return@collectLatest
                        }
                        envList.forEach { data ->
                            data.fanIntake = type.inputAirFlow
                            data.fanExhaust = type.ventilation
                        }
                        originalData?.environments = envList
                        _environmentInfo.value = originalData?.let { it1 -> Resource.Success(it1) }
                    }

                    else -> _environmentInfo.value = it
                }
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
     * 是否是首次订阅
     *//*
    private val _checkFirstSubscriber = MutableLiveData<Resource<Boolean>>()
    val checkFirstSubscriber: LiveData<Resource<Boolean>> = _checkFirstSubscriber
    fun checkFirstSubscriber() {
        viewModelScope.launch {
            getUnread
            repository.checkFirstSubscriber().map {
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
                _checkFirstSubscriber.value = it
            }
        }
    }

    */
    /**
     * 开启订阅
     *//*
    private val _startSubscriber = MutableLiveData<Resource<BaseBean>>()
    val startSubscriber: LiveData<Resource<BaseBean>> = _startSubscriber
    fun startSubscriber() {
        viewModelScope.launch {
            getUnread
            repository.startSubscriber().map {
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
                _startSubscriber.value = it
            }
        }
    }*/


    /**
     * 是否需要补偿订阅
     */
    private val _whetherSubCompensation = MutableLiveData<Resource<WhetherSubCompensationData>>()
    val whetherSubCompensation: LiveData<Resource<WhetherSubCompensationData>> =
        _whetherSubCompensation

    fun whetherSubCompensation() {
        viewModelScope.launch {
            getUnread
            repository.whetherSubCompensation().map {
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
                _whetherSubCompensation.value = it
            }
        }
    }

    /**
     * 订阅补偿
     */
    private val _compensatedSubscriber = MutableLiveData<Resource<BaseBean>>()
    val compensatedSubscriber: LiveData<Resource<BaseBean>> = _compensatedSubscriber
    fun compensatedSubscriber() {
        viewModelScope.launch {
            getUnread
            repository.compensatedSubscriber().map {
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
                _compensatedSubscriber.value = it
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
                    // 获取环信消息数量
                    getEaseUINumber()
                    // 获取设备环境消息
                    plantInfoLoop()
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
     * 跳过种子阶段
     */
    private val _skipGerminate = MutableLiveData<Resource<BaseBean>>()
    val skipGerminate: LiveData<Resource<BaseBean>> = _skipGerminate
    fun skipGerminate() {
        viewModelScope.launch {
            repository.skipGerminate().map {
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
                _skipGerminate.value = it
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

    /**
     * 保存开关门的状态
     */
    private val _doorStatus = MutableLiveData(
        thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_DOOR }
            ?.get(TuYaDeviceConstants.KEY_DEVICE_DOOR).toString()
    )
    val doorStatus: LiveData<String> = _doorStatus
    fun setDoorStatus(status: String) {
        _doorStatus.value = status
    }

    /**
     * 查询固件升级信息
     */
    fun checkFirmwareUpdateInfo(
        onOtaInfo: ((upgradeInfoBeans: MutableList<UpgradeInfoBean>?, isShow: Boolean) -> Unit)? = null,
    ) {
        thingDeviceBean()?.devId?.let {
            ThingHomeSdk.newOTAInstance(it)?.getOtaInfo(object : IGetOtaInfoCallback {
                override fun onSuccess(upgradeInfoBeans: MutableList<UpgradeInfoBean>?) {
                    // logI("getOtaInfo:  ${GSON.toJson(upgradeInfoBeans?.firstOrNull { it.type == 9 })}")
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
                checkPlant()
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
    fun checkPlant() = viewModelScope.launch {
        repository.checkPlant().map {
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
     * 是否是帐篷，true 帐篷 false abby
     */
    private val _isZP = MutableLiveData<Boolean>()
    val isZp: LiveData<Boolean> = _isZP
    fun setZp(isZps: Boolean) {
        _isZP.value = isZps
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
     * 保存SN
     */
    private val _sn = MutableLiveData<String>()
    val sn: LiveData<String> = _sn
    fun saveSn(sn: String) {
        _sn.value = sn
    }

    /**
     * 获取SN
     */
    fun getSn() {
        ThingHomeSdk.newDeviceInstance(thingDeviceBean()?.devId)?.let {
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
     * 合并账号
     */
    private val _switchDevice = MutableLiveData<Resource<String>>()
    val switchDevice: LiveData<Resource<String>> = _switchDevice
    fun switchDevice(deviceId: String) {
        viewModelScope.launch {
            repository.switchDevice(deviceId)
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
                    _switchDevice.value = it
                }
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
                    when (it) {
                        is Resource.Success -> {
                            _listDevice.value = it
                        }

                        else -> {}
                    }
                }
        }
    }


    /**
     * 检查固件是否可以升级
     */
    private fun hasHardwareUpdate(list: MutableList<UpgradeInfoBean>?): Boolean {
        if (null == list || list.size == 0) return false
        return list.firstOrNull { it.type == 9 }?.upgradeStatus == 1
    }

    // transplant 周期回调。
    private val _transplantPeriodicity = MutableLiveData<String>()
    val transplantPeriodicity: LiveData<String> = _transplantPeriodicity

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
                UnReadConstants.Device.KEY_CHANGE_CUP_WATER -> {}
                UnReadConstants.Device.KEY_CLOSE_DOOR -> {}
                UnReadConstants.PlantStatus.TASK_TYPE_CHECK_TRANSPLANT -> {
                    // 这个周期目前自行处理、不走guideInfo接口
                    // 回调出去。自行处理
                    taskId?.let { ids ->
                        _transplantPeriodicity.value = ids
                    }
                }

                else -> {
                    // getGuideInfo(it)
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
    fun getEaseUINumber() {
        // 只有当设备绑定且在线的时候、才去添加
        if (userDetail.value?.data?.deviceStatus == "1" && userDetail.value?.data?.deviceOnlineStatus == "1" && _isZP.value == false) {
            _unReadMessageNumber.postValue(InterComeHelp.INSTANCE.getUnreadConversationCount())
        }
    }


    /**
     * 获取配件信息
     */
    private val _getAccessoryInfo = MutableLiveData<Resource<UpdateInfoReq>>()
    val getAccessoryInfo: LiveData<Resource<UpdateInfoReq>> = _getAccessoryInfo
    fun getAccessoryInfo(deviceId: String, accessoryDeviceId: String) {
        viewModelScope.launch {
            repository.getAccessoryInfo(deviceId, accessoryDeviceId)
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
                    _getAccessoryInfo.value = it
                }
        }
    }

    /**
     * 保存camera设置信息
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
     * 获取环境信息
     */
    var tuYaDps = thingDeviceBean()?.dps
    fun getEnvData() {
        thingDeviceBean()?.let {
            val envReq = EnvironmentInfoReq(deviceId = it.devId)
            tuYaDps?.forEach { (key, value) ->
                when (key) {
                    TuYaDeviceConstants.KEY_DEVICE_WATER_TEMPERATURE -> {
                        envReq.waterTemperature = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_VENTILATION -> {
                        envReq.ventilation = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_TEMP_CURRENT -> {
                        envReq.tempCurrent = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_INPUT_AIR_FLOW -> {
                        envReq.inputAirFlow = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_HUMIDITY_CURRENT -> {
                        envReq.humidityCurrent = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_BRIGHT_VALUE -> {
                        envReq.brightValue = value.safeToInt()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_WATER_LEVEL -> {
                        envReq.waterLevel = value.toString()
                    }
                }
            }
            // 请求环境信息
            environmentInfo(envReq)
        }
    }


    private var _shouldRunJob = MutableLiveData<Boolean>()
    val shouldRunJob: LiveData<Boolean> = _shouldRunJob
    fun setShouldRunJob(shouldRunJob: Boolean) {
        _shouldRunJob.value = shouldRunJob
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
                if (shouldRunJob.value == false) break
                emit(i)
                delay(1000)
            }
        }
            .flowOn(Dispatchers.Main)
            .onStart { onStart?.invoke() }
            .onCompletion { onFinish?.invoke() }
            .onEach {
                if (shouldRunJob.value == true) {
                    onTick.invoke(it)
                }
            }
            .catch { exception ->
                // Handle exception here
            }
            .launchIn(scope)
    }


    var isLeftSwap: Boolean = false
    fun setLeftSwaps(isLeft: Boolean) {
        isLeftSwap = isLeft
    }


    /**
     * 手动模式相关数据
     */
    private val _getPlantHeight = MutableLiveData<String>()
    val plantHeights: LiveData<String> = _getPlantHeight

    // 获取植物高度
    fun getPlantHeight() {
        kotlin.runCatching {
            _getPlantHeight.value = String.format(
                "%.1f",
                thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_PLANT_HEIGHT }
                    ?.get(TuYaDeviceConstants.KEY_DEVICE_PLANT_HEIGHT).toString().toFloat()
                    .div(25.4))
        }
    }

    fun setPlantHeight(height: String) {
        _getPlantHeight.value = height
    }

    private val _getWenDu = MutableLiveData<Int>()
    val getWenDu: LiveData<Int> = _getWenDu

    // 获取温度
    fun getWenDu() {
        kotlin.runCatching {
            _getWenDu.value =
                thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_WENDU }
                    ?.get(TuYaDeviceConstants.KEY_DEVICE_WENDU).toString().safeToDouble().safeToInt()
        }
    }

    fun setWenDu(wendu: String?) {
        _getWenDu.value = wendu?.safeToDouble()?.safeToInt()
    }


    private val _getHumidity = MutableLiveData<Int>()
    val getHumidity: LiveData<Int> = _getHumidity

    // 获取湿度
    fun getHumidity() {
        kotlin.runCatching {
            _getHumidity.value =
                thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_HUMIDITY }
                    ?.get(TuYaDeviceConstants.KEY_DEVICE_HUMIDITY).toString().safeToDouble().safeToInt()
        }
    }

    fun setHumidity(humidity: String?) {
        _getHumidity.value = humidity?.safeToDouble()?.safeToInt()
    }

    private val _getWaterWenDu = MutableLiveData<Int>()
    val getWaterWenDu: LiveData<Int> = _getWaterWenDu

    // 获取水温
    fun getWaterWenDu() {
        kotlin.runCatching {
            _getWaterWenDu.value =
                thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_WATER_WENDU }
                    ?.get(TuYaDeviceConstants.KEY_DEVICE_WATER_WENDU).toString().safeToDouble().safeToInt()
        }
    }

    fun setWaterWenDu(waterWenDu: String?) {
        _getWaterWenDu.value = waterWenDu?.safeToDouble()?.safeToInt()
    }

    private val _getFanIntake = MutableLiveData<Int>()
    val getFanIntake: LiveData<Int> = _getFanIntake

    // 进气风扇
    fun getFanIntake() {
        kotlin.runCatching {
            _getFanIntake.value =
                thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_INTAKE }
                    ?.get(TuYaDeviceConstants.KEY_DEVICE_INTAKE).toString().safeToDouble().safeToInt()
        }
    }

    fun setFanIntake(gear: String) {
        _getFanIntake.value = gear.safeToDouble().safeToInt()
    }

    private val _getFanExhaust = MutableLiveData<Int>()
    val getFanExhaust: LiveData<Int> = _getFanExhaust

    fun getFanExhaust() {
        kotlin.runCatching {
            _getFanExhaust.value =
                thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_EXHAUST }
                    ?.get(TuYaDeviceConstants.KEY_DEVICE_EXHAUST).toString().safeToDouble().safeToInt()
        }
    }

    fun setFanExhaust(gear: String) {
        _getFanExhaust.value = gear.safeToDouble().safeToInt()
    }

    // 植物预设灯光
    private val _getGrowLight = MutableLiveData<Int>(-1)
    val getGrowLight: LiveData<Int> = _getGrowLight

    fun setGrowLight(gear: String) {
        _getGrowLight.value = gear.safeToDouble().safeToInt()
    }

    // 植物当前灯光
    private val _getCurrentGrowLight = MutableLiveData<Int>()
    val getCurrentGrowLight: LiveData<Int> = _getCurrentGrowLight

    fun getCurrentGrowLight() {
        kotlin.runCatching {
            _getCurrentGrowLight.value =
                thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_GROW_LIGHT }
                    ?.get(TuYaDeviceConstants.KEY_DEVICE_GROW_LIGHT).toString().safeToDouble().safeToInt()
        }
    }

    fun setCurrentGrowLight(gear: String) {
        _getCurrentGrowLight.value = gear.safeToDouble().safeToInt()
    }

    // 气泵
    private val _getAirPump = MutableLiveData<Boolean>()
    val getAirPump: LiveData<Boolean> = _getAirPump
    fun getAirPump() {
        kotlin.runCatching {
            _getAirPump.value =
                thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_AIR_PUMP }
                    ?.get(TuYaDeviceConstants.KEY_DEVICE_AIR_PUMP).toString().toBoolean()
        }
    }

    fun setAirPump(gear: String?) {
        kotlin.runCatching {
            (gear == "true").also { _getAirPump.value = it }
        }
    }

    // 开灯时间
    private val _getLightTime = MutableLiveData<String>()
    val getLightTime: LiveData<String> = _getLightTime
    fun getLightTime() {
        runCatching {
            _getLightTime.value =
                thingDeviceBean()?.dps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_LIGHT_TIME }
                    ?.get(TuYaDeviceConstants.KEY_DEVICE_LIGHT_TIME).toString()
        }
    }

    // 关灯时间
    // 检查5次。5次之后还获取不到正确的值，那么无能为力
    private val _getCloseLightTime = MutableLiveData<String>()
    val getCloseLightTime: LiveData<String> = _getCloseLightTime
    fun getCloseLightTime(retryCount: Int = 0) {
        kotlin.runCatching {
            val deviceBean = thingDeviceBean()
            _getCloseLightTime.value = deviceBean?.dps
                ?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_LIGHT_OFF_TIME }
                ?.get(TuYaDeviceConstants.KEY_DEVICE_LIGHT_OFF_TIME)
                ?.toString()

            getTimeText()
            logI("getLightTime:${_getLightTime.value} -- ${_getCloseLightTime.value} --- ${getTimeText.value}")

            // 检查 _getCloseLightTime.value 或 _getLightTime.value 是否为 null
            if (_getCloseLightTime.value == null || _getLightTime.value == null) {
                if (retryCount < 5) {
                    logI("Retry fetching light time due to null value, attempt #${retryCount + 1}")
                    getCloseLightTime(retryCount + 1)  // 递归调用以重试
                } else {
                    logI("Failed to fetch light time after 5 attempts.")
                }
            }
        }.onFailure {
            if (retryCount < 5) {
                logI("Error fetching light time, retrying... Attempt #${retryCount + 1}: ${it.message}")
                getCloseLightTime(retryCount + 1)  // 异常时也重试
            } else {
                logI("Error fetching light time, no more retries left. Error: ${it.message}")
            }
        }
    }


    private val timeText = MutableLiveData<String>("")
    val getTimeText: LiveData<String> = timeText
    private fun setTimeText(text: String) {
        timeText.value = text
    }

    private fun getTimeText(): String {
        kotlin.runCatching {
            val lightTime = _getLightTime.value?.safeToDouble()?.safeToInt() ?: 0
            val closeLightTime = _getCloseLightTime.value?.safeToDouble()?.safeToInt() ?: 0

            muteOn = lightTime.toString()
            muteOff = closeLightTime.toString()

            if (lightTime == 0) {
                muteOn = "12"
            }

            if (closeLightTime == 0) {
                muteOff = "12"
            }

            val startTime = if ((muteOn?.safeToInt() ?: 12) <= 12) {
                "${(muteOn?.safeToInt() ?: 12)}:00 AM"
            } else {
                "${((muteOn?.safeToInt() ?: 12) - 12)}:00 PM"
            }

            val closeTime = if ((muteOff?.safeToInt() ?: 12) <= 12) {
                "${(muteOff?.safeToInt() ?: 12)}:00 AM"
            } else {
                "${((muteOff?.safeToInt() ?: 12) - 12)}:00 PM"
            }
            setTimeText("$startTime-$closeTime")
            return "$startTime-$closeTime"
        }
        return ""
    }

    var muteOn: String? = null
    fun setmuteOn(muteOn: String?) {
        this.muteOn = muteOn
    }

    var muteOff: String? = null
    fun setmuteOff(muteOff: String?) {
        this.muteOff = muteOff
    }

    // 水的Level
    private val _getWaterLevel = MutableLiveData<String>()
    val getWaterLevel: LiveData<String> = _getWaterLevel
    fun setWaterLevel(level: String) {
        // 0L 1L 2L 3L
        _getWaterLevel.value = when (level) {
            "0L" -> "Low"
            "2L", "1L" -> "OK"
            "3L" -> "Max"
            else -> "Low"
        }
    }

    // 公英制转换
    fun temperatureConversion(text: Int?): Int? {
        val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        // 默认为false
        if (isMetric) {
            kotlin.runCatching {
                // (1°F − 32) × 5/9
                // String result1 = String.format("%.2f", d);
                return String.format("%.1f", (text?.minus(32))?.times(5f)?.div(9f)).safeToDouble()
                    .safeToInt()
            }.getOrElse {
                return text
            }
        }
        return text
    }

    /**
     * 获取温度，如果有购买温湿度传感器的话。
     */
    fun temperatureConversionForTemp(text: Int?): String {
        val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        val data = _plantInfoLoop.value?.data ?: _plantInfo.value?.data
        val roomTemp = com.cl.common_base.ext.temperatureConversion(data?.envirVO?.roomTemp.safeToFloat(), isMetric)
        // 默认为false
        if (isMetric) {
            kotlin.runCatching {
                // (1°F − 32) × 5/9
                // String result1 = String.format("%.2f", d);
                return "${String.format("%.1f", (text?.minus(32))?.times(5f)?.div(9f)).safeToDouble().safeToInt()} ${if (roomTemp.isNotEmpty()) context.getString(com.cl.common_base.R.string.home_room_temp, "$roomTemp") else ""}"
            }.getOrElse {
                return "$text ${if (roomTemp.isNotEmpty()) context.getString(com.cl.common_base.R.string.home_room_temp, "$roomTemp") else ""}"
            }
        }
        return "$text ${if (roomTemp.isNotEmpty()) context.getString(com.cl.common_base.R.string.home_room_temp_desc, "$roomTemp") else ""}"
    }

    // 获取室内的湿度，在有数据的情况下
    fun getRoomHumidity(humidity: Int?): String {
        val data = _plantInfoLoop.value?.data ?: _plantInfo.value?.data
        val roomHumidity = data?.envirVO?.roomHumiture
        return if (roomHumidity.isNullOrEmpty()) "$humidity" else context.getString(com.cl.common_base.R.string.home_room, "$humidity", "$roomHumidity")
    }

    fun textCovert(): String {
        val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        return if (isMetric) context.getString(com.cl.common_base.R.string.string_1326) else context.getString(com.cl.common_base.R.string.string_1327)
    }

    fun incCovert(): String {
        val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        return if (isMetric) context.getString(com.cl.common_base.R.string.string_1328) else context.getString(com.cl.common_base.R.string.string_1328)
    }

    private val _loadFirst = MutableLiveData<Boolean>(false)
    val loadFirst: LiveData<Boolean> = _loadFirst
    fun setLoadFirst(loadFirst: Boolean) {
        _loadFirst.value = loadFirst
    }

    // 格式化植物的高度
    fun formatIncPlant(inc: String?): String {
        val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        if (isMetric) {
            kotlin.runCatching {
                val incPlant = inc?.toFloat()?.times(2.54f)
                if (incPlant != null) {
                    return if (incPlant <= 20) {
                        context.getString(com.cl.common_base.R.string.home_tt_cm)
                    } else {
                        context.getString(com.cl.common_base.R.string.home_cm, "$incPlant")
                    }
                }
            }
        } else {
            kotlin.runCatching {
                val incPlant = inc?.toFloat()
                if (incPlant != null) {
                    return if (incPlant <= 8) {
                        context.getString(com.cl.common_base.R.string.home_eight_inch)
                    } else {
                        context.getString(com.cl.common_base.R.string.home_inch, "$inc")
                    }
                }
            }
        }
        return inc ?: ""
    }

    // 排水的状态Flag
    private val _getDrainageFlag = MutableLiveData<Boolean>(false)
    val getDrainageFlag: LiveData<Boolean> = _getDrainageFlag
    fun setDrainageFlag(flag: Boolean) {
        _getDrainageFlag.value = flag
    }

    /**
     * 绑定的设备是否有摄像头
     *  false 表示没有摄像头
     *
     *  @param isHave 是否有摄像头
     *  @param isLoadCamera 展示摄像头
     *  @param cameraId 摄像头的ID
     */
    fun getCameraFlag(isHaveACamera: (isHave: Boolean, isLoadCamera: Boolean, cameraId: String, devId: String) -> Unit) {
        val isShowCamera = Prefs.getBoolean(Constants.Global.KEY_IS_SHOW_CAMERA, true)

        listDevice.value?.data?.firstOrNull { it.currentDevice == 1 }
            ?.accessoryList?.firstOrNull { it.accessoryType == AccessoryListBean.KEY_CAMERA }.apply {
                if (this == null) {
                    isHaveACamera.invoke(false, isShowCamera, "", "")
                } else {
                    isHaveACamera.invoke(true, isShowCamera, accessoryDeviceId ?: "", listDevice.value?.data?.firstOrNull { it.currentDevice == 1 }?.deviceId ?: "")
                }
            }

        // 如果是不显示摄像头
        /*if (!isShowCamera) {
            if (cameraAccessory != null) {
                isHaveACamera.invoke(true, false, cameraAccessory.accessoryDeviceId ?: "",  listDevice.value?.data?.firstOrNull {it.currentDevice == 1}?.deviceId ?: "")
            } else {
                isHaveACamera.invoke(false, false, "", "")
            }
        } else {
            if (cameraAccessory != null) {
                isHaveACamera.invoke(true, true, cameraAccessory.accessoryDeviceId ?: "",  listDevice.value?.data?.firstOrNull {it.currentDevice == 1}?.deviceId ?: "")
            } else {
                isHaveACamera.invoke(false, false, "", "")
            }
        }*/


        // 表示有，并且已经在展示状态
        /*if (cameraAccessory != null && isShowCamera) {
            isHaveACamera.invoke(true, true, cameraAccessory.accessoryDeviceId ?: "",  listDevice.value?.data?.firstOrNull {it.currentDevice == 1}?.deviceId ?: "")
        }  else {
            isHaveACamera.invoke(false, false, "", "")
        }*/
    }

    // popupList
    // 查看是否获取到了勋章
    private val _getMedal = MutableLiveData<Resource<MutableList<MedalPopData>>>()
    val getMedal: LiveData<Resource<MutableList<MedalPopData>>> = _getMedal
    fun getMedal() {
        viewModelScope.launch {
            repository.popupList()
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
                    _getMedal.value = it
                }
        }
    }

    // 添加预设模版
    private val _addPreset = MutableLiveData<Resource<BaseBean>>()
    val addPreset: LiveData<Resource<BaseBean>> = _addPreset
    fun addProModeRecord(req: ProModeInfoBean) {
        viewModelScope.launch {
            repository.addProModeRecord(req)
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
                    _addPreset.value = it
                }
        }
    }

    fun getNextUniqueId(): Int {
        // If the list is empty, start from 1 (or any other starting point you prefer)
        val existingIds = getPreset.value?.data
        if (existingIds?.isEmpty() == true) {
            return 1
        }
        // Find the maximum ID in the array directly, avoiding the creation of an intermediate list
        val maxId = existingIds?.maxOfOrNull { (it.id ?: 0) } ?: 0
        return maxId + 1
    }

    // 查询所有预设模版
    private val _getPreset = MutableLiveData<Resource<MutableList<ProModeInfoBean>>>()
    val getPreset: LiveData<Resource<MutableList<ProModeInfoBean>>> = _getPreset
    fun getProModeRecord(device:String) {
        viewModelScope.launch {
            repository.getProModeByDeviceId(device)
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
                    _getPreset.value = it
                }
        }
    }

    // 修改单个预设模版
    private val _updatePreset = MutableLiveData<Resource<BaseBean>>()
    val updatePreset: LiveData<Resource<BaseBean>> = _updatePreset
    fun updateProModeRecord(req: ProModeInfoBean) {
        viewModelScope.launch {
            repository.updateProModeRecord(req)
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
                    _updatePreset.value = it
                }
        }
    }

    /**
     * 添加当前proMode信息
     */
    private val _addCurrentProMode = MutableLiveData<Resource<BaseBean>>()
    val addCurrentProMode: LiveData<Resource<BaseBean>> = _addCurrentProMode
    fun addCurrentProMode(req: ProModeInfoBean) {
        viewModelScope.launch {
            repository.addProModeInfo(req)
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
                    _addCurrentProMode.value = it
                }
        }
    }

    /**
     * 获取当前proMode信息
     */
    private val _getCurrentProMode = MutableLiveData<Resource<ProModeInfoBean>>()
    val getCurrentProMode: LiveData<Resource<ProModeInfoBean>> = _getCurrentProMode
    fun getCurrentProMode(deviceId: String) {
        viewModelScope.launch {
            repository.getProModeInfo(deviceId)
                .map {
                    if (it.code != Constants.APP_SUCCESS) {
                        Resource.DataError(
                            it.code,
                            it.msg
                        )
                    } else {
                        if (it.data == null) {
                            Resource.DataError(
                                -1,
                                context.getString(com.cl.common_base.R.string.string_1331)
                            )
                        } else {
                            Resource.Success(it.data)
                        }
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
                    _getCurrentProMode.value = it
                }
        }
    }


    // 同步到后台到promMode预设模版中
    val isSyncing = AtomicBoolean(false)
    fun asyncProMode() {
        if (isSyncing.getAndSet(true)) {
            logI("Synchronization is already in progress.")
            return
        }
        try {
            // 你现有的代码
            val pres = Prefs.getObjects()
            logI("12312312#: ${pres?.size}")
            if (pres?.isNotEmpty() == true) {
                // 如果不是空的，那么就遍历然后上传到后台模版。
                pres.forEachIndexed { index, it ->
                    val asdasd = ProModeInfoBean(bright = it.lightIntensity.safeToInt(), deviceId = userDetail.value?.data?.deviceId,
                        fanIn = it.fanIntake.safeToInt(), fanOut = it.fanExhaust.safeToInt(), id = it.id?.plus(index), lightOn = it.muteOn.safeToInt(), lightOff = it.muteOff.safeToInt(), name = it.name,
                        notes = it.note
                    )
                    logI("1231231231#: $asdasd")
                    addProModeRecord(asdasd)
                }
            }
        } finally {
            isSyncing.set(false)
            Prefs.removeKey(Constants.Global.KEY_GLOBAL_PRO_MODEL)
        }
    }

    /**
     * 保存摄像头Id
     */
    private val _cameraId = MutableLiveData<String>()
    val cameraId: LiveData<String> = _cameraId
    fun setCameraId(flag: String) {
        _cameraId.value = flag
    }


    companion object {
        const val KEY_GUIDE_ID = "key_guide_id"
        const val KEY_TASK_ID = "key_task_id"
        const val KEY_TASK_TIME = "key_task_time"
    }
}