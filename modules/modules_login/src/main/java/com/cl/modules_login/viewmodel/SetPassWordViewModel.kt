package com.cl.modules_login.viewmodel

import androidx.lifecycle.*
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.modules_login.repository.RegisterLoginRepository
import com.cl.modules_login.request.UpdatePwdReq
import com.cl.modules_login.request.UserRegisterReq
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class SetPassWordViewModel @Inject constructor(private val repository: RegisterLoginRepository) :
    ViewModel() {

    private val _passWordState = MutableLiveData<Boolean>(true)
    val passWordState: LiveData<Boolean> = _passWordState

    fun setPassWordState(state: Boolean) {
        _passWordState.value = state
    }

    /**
     * 用户注册
     */
    private val _isVerifySuccess = MutableLiveData<Resource<Boolean>>()
    val isVerifySuccess: LiveData<Resource<Boolean>> = _isVerifySuccess
    fun registerAccount(body: UserRegisterReq) =
        viewModelScope.launch {
            repository.registerAccount(body)
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
                    _isVerifySuccess.value = it
                }
        }

    /**
     * 忘记密码,修改密码
     */
    private val _updatePwds = MutableLiveData<Resource<Boolean>>()
    val updatePwds: LiveData<Resource<Boolean>> = _updatePwds
    fun updatePwd(body: UpdatePwdReq) =
        viewModelScope.launch {
            repository.updatePwd(body)
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


    companion object {
        private const val TAG = "LoginViewModel"
    }
}