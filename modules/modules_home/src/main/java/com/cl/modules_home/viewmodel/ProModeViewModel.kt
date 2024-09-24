package com.cl.modules_home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.UpDeviceInfoReq
import com.cl.common_base.bean.UpPlantInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_home.repository.HomeRepository
import com.cl.modules_home.request.CheckEnvData
import com.cl.modules_home.request.CycleListBean
import com.cl.modules_home.request.DeleteTaskReq
import com.cl.modules_home.request.EnvData
import com.cl.modules_home.request.EnvDeleteReq
import com.cl.modules_home.request.EnvParamListBeanItem
import com.cl.modules_home.request.EnvParamListReq
import com.cl.modules_home.request.EnvSaveReq
import com.cl.modules_home.request.PeriodListBody
import com.cl.modules_home.request.PeriodListSaveReq
import com.cl.modules_home.request.SaveTaskReq
import com.cl.modules_home.request.Task
import com.cl.modules_home.request.TaskConfigurationListData
import com.cl.modules_home.request.TempData
import com.thingclips.smart.sdk.bean.DeviceBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProModeViewModel @Inject constructor(private val repository: HomeRepository) :
    ViewModel() {

    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }


    var muteOn: String? = null
    fun setmuteOn(muteOn: String?) {
        this.muteOn = muteOn
    }

    var muteOff: String? = null
    fun setmuteOff(muteOff: String?) {
        this.muteOff = muteOff
    }

    /**
     * 获取任务配置列表
     */
    private val _taskConfigurationList = MutableLiveData<Resource<TaskConfigurationListData>>()
    val taskConfigurationList: LiveData<Resource<TaskConfigurationListData>> = _taskConfigurationList
    fun getTaskConfigurationList(req: EnvSaveReq) = viewModelScope.launch {
        repository.taskConfigurationList(req).map {
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
            _taskConfigurationList.value = it
        }
    }

    /**
     * 旧的开始种植植物
     */
    private val _startRunning = MutableLiveData<Resource<Boolean>>()
    val startRunning: LiveData<Resource<Boolean>> = _startRunning
    fun startRunning(botanyId: String?, goon: Boolean, templateId: String? = null) {
        viewModelScope.launch {
            repository.startRunning(botanyId, goon, templateId).map {
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
     * 删除任务
     */
    private val _deleteTask = MutableLiveData<Resource<Boolean>>()
    val deleteTask: LiveData<Resource<Boolean>> = _deleteTask
    fun deleteTask(req: DeleteTaskReq) = viewModelScope.launch {
        repository.taskDelete(req).map {
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
            _deleteTask.value = it
        }
    }


    /**
     * 获取任务列表
     */
    private val _taskList = MutableLiveData<Resource<MutableList<Task>>>()
    val taskList: LiveData<Resource<MutableList<Task>>> = _taskList
    fun getTaskList(req: EnvSaveReq) = viewModelScope.launch {
        repository.taskList(req).map {
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
            _taskList.value = it
        }
    }

    /**
     * 保存和修改任务
     */
    private val _saveTask = MutableLiveData<Resource<BaseBean>>()
    val saveTask: LiveData<Resource<BaseBean>> = _saveTask
    fun saveTask(req: SaveTaskReq) = viewModelScope.launch {
        repository.taskListSave(req).map {
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
            _saveTask.value = it
        }
    }

    /**
     * 更新设备信息
     */
    private val _updateDeviceInfo = MutableLiveData<Resource<BaseBean>>()
    val updateDeviceInfo: LiveData<Resource<BaseBean>> = _updateDeviceInfo
    fun updateDeviceInfo(req: UpDeviceInfoReq) = viewModelScope.launch {
        repository.updateDeviceInfo(req).map {
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
     * 获取周期环境列表
     */
    private val _cycleEnvList = MutableLiveData<Resource<EnvData>>()
    val cycleEnvList: LiveData<Resource<EnvData>> = _cycleEnvList
    fun getCycleEnvList(req: EnvParamListReq) = viewModelScope.launch {
        repository.getEnvParamList(req).map {
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
            _cycleEnvList.value = it
        }
    }

    /**
     * 删除环境参数模版
     */
    private val _deleteEnvParam = MutableLiveData<Resource<BaseBean>>()
    val deleteEnvParam: LiveData<Resource<BaseBean>> = _deleteEnvParam
    fun deleteEnvParam(req: EnvDeleteReq) = viewModelScope.launch {
        repository.envParamDelete(req).map {
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
            _deleteEnvParam.value = it
        }
    }

    /**
     * 创建日历模版
     */
    private val _calendarTemp = MutableLiveData<Resource<TempData>>()
    val calendarTemp: LiveData<Resource<TempData>> = _calendarTemp
    fun createCalendar(req: PeriodListBody) = viewModelScope.launch {
        repository.createCalendar(req).map {
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
            _calendarTemp.value = it
        }
    }

    /**
     * 获取周期列表接口
     * getCycleList
     */
    private val _cycleList = MutableLiveData<Resource<MutableList<CycleListBean>>>()
    val cycleList: LiveData<Resource<MutableList<CycleListBean>>> = _cycleList
    fun getCycleList(plantId: PeriodListBody) {
        viewModelScope.launch {
            repository.getCycleList(plantId)
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
                    _cycleList.value = it
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
     * 修改和保存周期
     */
    private val _updateCycle = MutableLiveData<Resource<Boolean>>()
    val updateCycle: LiveData<Resource<Boolean>> = _updateCycle
    fun updateCycle(body: PeriodListSaveReq) {
        viewModelScope.launch {
            repository.periodListSave(body)
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
                    _updateCycle.value = it
                }
        }
    }

    /**
     * 检查环境参数列表的合法性
     */
    private val _checkEnvParam = MutableLiveData<Resource<CheckEnvData>>()
    val checkEnvParam: LiveData<Resource<CheckEnvData>> = _checkEnvParam
    fun checkEnvParam(req: EnvSaveReq) = viewModelScope.launch {
        repository.envParamListCheck(req).map {
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
            _checkEnvParam.value = it
        }
    }


    /**
     * 保存或者修改环境参数列表
     */
    private val _saveEnvParam = MutableLiveData<Resource<BaseBean>>()
    val saveEnvParam: LiveData<Resource<BaseBean>> = _saveEnvParam
    fun saveEnvParam(req: EnvSaveReq) = viewModelScope.launch {
        repository.envParamListSave(req).map {
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
            _saveEnvParam.value = it
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

    // 是否移除了自己
    var isDeleteSelf = false
    fun setIsDeleteSelf(delete: Boolean) {
        isDeleteSelf = delete
    }
}