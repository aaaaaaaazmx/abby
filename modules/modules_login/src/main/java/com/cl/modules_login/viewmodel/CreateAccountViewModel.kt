package com.cl.modules_login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.modules_login.repository.RegisterLoginRepository
import com.cl.modules_login.response.CountData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class CreateAccountViewModel @Inject constructor(private val repository: RegisterLoginRepository) :
    ViewModel() {

    private val _countList = MutableLiveData<Resource<MutableList<CountData>>>()
    val countList: LiveData<Resource<MutableList<CountData>>> = _countList

    /**
     * 注册国家列表
     */
    fun getCountList() = viewModelScope.launch {
        repository.getCountList()
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
                logI("catch $it")
                emit(
                    Resource.DataError(
                        -1,
                        "$it"
                    )
                )
            }.collectLatest {
                _countList.value = it
            }
    }

    private val _sendStates = MutableLiveData<Resource<Boolean>>()
    val sendStates: LiveData<Resource<Boolean>> = _sendStates

    /**
     * 发送验证码
     */
    fun verifyEmail(email: String, type: String) =
        viewModelScope.launch {
            repository.verifyEmail(email, type)
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
                    logD("verifyEmail: catch $it")
                    emit(
                        Resource.DataError(
                            -1,
                            "$it"
                        )
                    )
                }.collectLatest {
                    _sendStates.value = it
                }
        }
}