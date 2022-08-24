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

/**
 * 忘记密码重新发送验证码
 */
@ActivityRetainedScoped
class ForgetPassWordViewModel @Inject constructor(private val repository: RegisterLoginRepository) :
    ViewModel() {

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


    companion object {
        private const val TAG = "LoginViewModel"
    }
}