package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.bean.*
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.modules_my.repository.MyRepository
import com.cl.common_base.bean.ModifyUserDetailReq
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class RedeemViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {

    /**
     * 检查订阅码
     */
    private val _checkSubscriberNumber = MutableLiveData<Resource<CheckSubscriberNumberBean>>()
    val checkSubscriberNumber: LiveData<Resource<CheckSubscriberNumberBean>> = _checkSubscriberNumber
    fun checkSubscriberNumber(number: String) = viewModelScope.launch {
        repository.checkSubscriberNumber(number)
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
                _checkSubscriberNumber.value = it
            }
    }



    /**
     * 获取InterCome同步数据
     */
    private val _getInterComeData = MutableLiveData<Resource<Map<String, Any>>>()
    val getInterComeData: LiveData<Resource<Map<String, Any>>> = _getInterComeData
    fun getInterComeData() = viewModelScope.launch {
        repository.intercomDataAttributeSync()
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
                _getInterComeData.value = it
            }
    }


    /**
     * 订阅码激活
     */
    private val _topUpSubscriberNumber = MutableLiveData<Resource<Boolean>>()
    val topUpSubscriberNumber: LiveData<Resource<Boolean>> = _topUpSubscriberNumber
    fun topUpSubscriberNumber(number: String) = viewModelScope.launch {
        repository.topUpSubscriberNumber(number)
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
                _topUpSubscriberNumber.value = it
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


}