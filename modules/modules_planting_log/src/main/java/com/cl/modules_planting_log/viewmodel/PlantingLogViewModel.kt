package com.cl.modules_planting_log.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_planting_log.repository.PlantRepository
import com.cl.modules_planting_log.request.LogListDataItem
import com.cl.modules_planting_log.request.LogListReq
import com.cl.modules_planting_log.request.PlantIdByDeviceIdData
import com.cl.modules_planting_log.request.PlantInfoByPlantIdData
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class PlantingLogViewModel @Inject constructor(private val repository: PlantRepository) : ViewModel() {


    val userinfoBean = {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    // 更改Current页码
    private val _updateCurrent = MutableLiveData<Int>(1)
    val updateCurrent: LiveData<Int> = _updateCurrent
    fun updateCurrent(current: Int) {
        _updateCurrent.value = current
    }

    /**
     * 存储选的plantId
     */
    private val _plantId = MutableLiveData<String>()
    val plantId: LiveData<String> = _plantId
    fun setPlantIds(plantId: String) {
        _plantId.value = plantId
    }

    /**
     * 存储选的period
     */
    private val _period = MutableLiveData<String>()
    val period: LiveData<String> = _period
    fun setPeriod(period: String) {
        _period.value = period
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
     * 关闭tips卡片，interCome卡片
     */
    private val _closeTips = MutableLiveData<Resource<Boolean>>()
    val closeTips: LiveData<Resource<Boolean>> = _closeTips
    fun closeTips(period: String, plantId: String) = viewModelScope.launch {
        repository.closeTips(period, plantId).map {
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
            _closeTips.value = it
        }
    }

}