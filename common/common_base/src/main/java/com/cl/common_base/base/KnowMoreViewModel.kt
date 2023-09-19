package com.cl.common_base.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class KnowMoreViewModel  @Inject constructor() : ViewModel() {
    private val service = ServiceCreators.create(BaseApiService::class.java)

    val homeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }

    /**
     * 滑块的具体文案
     */
    private val _sliderText = MutableLiveData<String?>()
    val sliderText: LiveData<String?> = _sliderText
    fun getSliderText(txt: String?) {
        _sliderText.value = txt
    }

    /**
     * 涂鸦信息
     */
    val tuYaUser by lazy {
        val bean = Prefs.getString(Constants.Tuya.KEY_DEVICE_USER)
        GSON.parseObject(bean, User::class.java)
    }

    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    /**
     * 富文本图文图文接口、所用东西都是从接口拉取
     */
    private val _richText = MutableLiveData<Resource<RichTextData>>()
    val richText: LiveData<Resource<RichTextData>> = _richText
    fun getRichText(txtId: String? = null, type: String? = null, taskId: String? = null) {
        viewModelScope.launch {
            service.getRichText(taskId, txtId, type)
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
                    _richText.value = it
                }
        }
    }


    /**
     * 任务完成
     */
    private val _finishTask = MutableLiveData<Resource<String>>()
    val finishTask: LiveData<Resource<String>> = _finishTask
    fun finishTask(body: FinishTaskReq) {
        viewModelScope.launch {
            service.finishTask(body)
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
                    _finishTask.value = it
                }
        }
    }

    /**
     * 新增配件
     */
    private val _addAccessory = MutableLiveData<Resource<BaseBean>>()
    val addAccessory: LiveData<Resource<BaseBean>> = _addAccessory
    fun addAccessory(accessoryId: String, deviceId: String, accessoryDeviceId: String? = null) {
        viewModelScope.launch {
            service.accessoryAdd(accessoryId, deviceId, accessoryDeviceId)
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
                    _addAccessory.value = it
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
            service.startRunning().map {
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
                        // 检查是否种植
                        tuYaUser?.uid?.let { uid -> checkPlant(uid) }
                    }
                    else ->  _startRunning.value = it
                }
            }
        }
    }

    /**
     * 插入篮子植物
     */
    private val _intoPlantBasket = MutableLiveData<Resource<BaseBean>>()
    val intoPlantBasket: LiveData<Resource<BaseBean>> = _intoPlantBasket
    fun intoPlantBasket() {
        viewModelScope.launch {
            service.intoPlantBasket().map {
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
                _intoPlantBasket.value = it
            }
        }
    }


    /**
     * 检查是否种植过植物
     */
    private val _checkPlant = MutableLiveData<Resource<CheckPlantData>>()
    val checkPlant: LiveData<Resource<CheckPlantData>> = _checkPlant
    fun checkPlant(uuid: String) = viewModelScope.launch {
        service.checkPlant(uuid).map {
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
     * 更新设备信息
     */
    private val _updateDeviceInfo = MutableLiveData<Resource<BaseBean>>()
    val updateDeviceInfo: LiveData<Resource<BaseBean>> = _updateDeviceInfo
    fun updateDeviceInfo(req: UpDeviceInfoReq) = viewModelScope.launch {
        service.updateDeviceInfo(req).map {
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
            _updateDeviceInfo.value = it
        }
    }

    /**
     * 延迟任务
     */
    private val _delayTask = MutableLiveData<Resource<BaseBean>>()
    val delayTask: LiveData<Resource<BaseBean>> = _delayTask
    fun delayTask(req: SnoozeReq) = viewModelScope.launch {
        service.snooze(req).map {
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
            _delayTask.value = it
        }
    }


    /**
     * 提前解锁
     */
    private val _unLockNow = MutableLiveData<Resource<BaseBean>>()
    val unLockNow: LiveData<Resource<BaseBean>> = _unLockNow
    fun getUnLockNow(plantId: String) = viewModelScope.launch {
        service.unlockNow(plantId)
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
                _unLockNow.value = it
            }
    }

}