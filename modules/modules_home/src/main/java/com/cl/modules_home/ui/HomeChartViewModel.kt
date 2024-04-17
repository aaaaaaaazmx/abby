package com.cl.modules_home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AcademyDetails
import com.cl.common_base.bean.AcademyListData
import com.cl.common_base.bean.PlantData
import com.cl.common_base.bean.PlantIdByDeviceIdData
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.bean.RichTextData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_home.repository.HomeRepository
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class HomeChartViewModel @Inject constructor(private val repository: HomeRepository) : ViewModel() {


    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
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
     * 存储选的plantId
     */
    private val _plantId = MutableLiveData<String>()
    val plantId: LiveData<String> = _plantId
    fun setPlantIds(plantId: String) {
        _plantId.value = plantId
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
     * 获取学院列表
     */
    private val _getAcademyList = MutableLiveData<Resource<MutableList<AcademyListData>>>()
    val getAcademyList: LiveData<Resource<MutableList<AcademyListData>>> = _getAcademyList
    fun getAcademyList() {
        viewModelScope.launch {
            repository.getAcademyList()
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
                    _getAcademyList.value = it
                }
        }
    }


    /**
     * 获取学院详情列表
     */
    private val _getAcademyDetails = MutableLiveData<Resource<MutableList<AcademyDetails>>>()
    val getAcademyDetails: LiveData<Resource<MutableList<AcademyDetails>>> = _getAcademyDetails
    fun getAcademyDetails(academyId: String) {
        viewModelScope.launch {
            repository.getAcademyDetails(academyId)
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
                    _getAcademyDetails.value = it
                }
        }
    }


    /**
     * 学院已读消息
     */
    private val _messageRead = MutableLiveData<Resource<BaseBean>>()
    val messageRead: LiveData<Resource<BaseBean>> = _messageRead
    fun messageRead(academyDetailsId: String) {
        viewModelScope.launch {
            repository.messageRead(academyDetailsId)
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
                    _messageRead.value = it
                }
        }
    }

    // 已读列表
     val messageReadList = mutableListOf<String>()
    fun setReadList(id: String) {
        messageReadList?.add(id)
    }

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
}