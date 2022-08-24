package com.cl.modules_login.viewmodel

import androidx.lifecycle.*
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.modules_login.repository.RegisterLoginRepository
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class VerifyEmailViewModel @Inject constructor(private val repository: RegisterLoginRepository) :
    ViewModel() {


    private val _isVerifySuccess = MutableLiveData<Resource<Boolean>>()
    val isVerifySuccess: LiveData<Resource<Boolean>> = _isVerifySuccess

    /**
     * 验证验证码
     */
    fun verifyCode(code: String, email: String) =
        viewModelScope.launch {
            repository.verifyCode(code, email)
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
                    _isVerifySuccess.value = it
                }
        }

    /**
     * 忘记密码,发送邮箱验证码
     */
    private val _updatePwds = MutableLiveData<Resource<Boolean>>()
    val updatePwds: LiveData<Resource<Boolean>> = _updatePwds
    fun updatePwd(email: String, type: String) =
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
                    logD("registerAccount: catch $it")
                    emit(
                        Resource.DataError(
                            -1,
                            "$it"
                        )
                    )
                }.collectLatest {
                    _updatePwds.value = it
                }
        }

    /**
     * 发送验证码
     */
    private val _sendStates = MutableLiveData<Resource<Boolean>>()
    val sendStates: LiveData<Resource<Boolean>> = _sendStates
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

    companion object {
        private const val TAG = "LoginViewModel"
    }
}