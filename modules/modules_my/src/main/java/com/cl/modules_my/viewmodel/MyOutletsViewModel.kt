package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.bean.DetailByLearnMoreIdData
import com.cl.common_base.bean.UpdateInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_my.repository.MyRepository
import com.cl.modules_my.repository.MyTroubleData
import com.cl.modules_my.request.AccessorySubportData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class MyOutletsViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {

    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }


    private val _howTo = MutableLiveData<Resource<MutableList<MyTroubleData.Bean>>>()
    val howTo: LiveData<Resource<MutableList<MyTroubleData.Bean>>> = _howTo
    fun howTo() = viewModelScope.launch {
        repository.howTo()
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
                _howTo.value = it
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
            repository.getDetailByLearnMoreId(type)
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
                    _getDetailByLearnMoreId.value = it
                }
        }
    }

    /**
     * 获取配件参数
     */
    private val _accessorySubport = MutableLiveData<Resource<AccessorySubportData>>()
    val accessorySubport: LiveData<Resource<AccessorySubportData>> =
        _accessorySubport
    fun getSupport(accessoryId: String, accessoryDeviceId: String?) {
        viewModelScope.launch {
            repository.accessorySubport(accessoryId, accessoryDeviceId)
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
                    _accessorySubport.value = it
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



}