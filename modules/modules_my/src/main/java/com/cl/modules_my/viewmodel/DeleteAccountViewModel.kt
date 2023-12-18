package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.bean.DetailByLearnMoreIdData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_my.repository.MyRepository
import com.cl.modules_my.repository.MyTroubleData
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
class DeleteAccountViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {

    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    /**
     * 获取用户信息
     */
    private val _troubleShooting = MutableLiveData<Resource<MyTroubleData>>()
    val troubleShooting: LiveData<Resource<MyTroubleData>> = _troubleShooting
    fun troubleShooting() = viewModelScope.launch {
        repository.troubleShooting()
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
                _troubleShooting.value = it
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

    enum class Type {
        GROWING, ABBY, CONNECT
    }
}