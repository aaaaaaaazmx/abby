package com.cl.modules_pairing_connection.viewmodel

import android.Manifest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.modules_pairing_connection.repository.PairRepository
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * 配网ViewModel
 *
 * @author 李志军 2022-08-04 10:12
 */
@ActivityRetainedScoped
class PairDistributionWifiViewModel @Inject constructor(private val repository: PairRepository) :
    ViewModel() {

    private val _passWordState = MutableLiveData<Boolean>(true)
    val passWordState: LiveData<Boolean> = _passWordState

    fun setPassWordState(state: Boolean) {
        _passWordState.value = state
    }

    /**
     * 上传后台接口绑定
     */
    private val _bindDevice = MutableLiveData<Resource<String>>()
    val bindDevice: LiveData<Resource<String>> = _bindDevice
    fun bindDevice(deviceId: String, deviceUuid: String) = viewModelScope.launch {
        repository.bindDevice(deviceId, deviceUuid)
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
                _bindDevice.value = it
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


    /**
     * 检查过是否种植过植物
     */
    private val _checkPlant = MutableLiveData<Resource<CheckPlantData>>()
    val checkPlant: LiveData<Resource<CheckPlantData>> = _checkPlant
    fun checkPlant(
        body: String
    ) = viewModelScope.launch {
        repository.checkPlant(body)
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
                _checkPlant.value = it
            }
    }


    private var _blePer: MutableLiveData<Array<String>> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MutableLiveData(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            MutableLiveData(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    val blePer: LiveData<Array<String>> = _blePer


    fun countDownCoroutines(
        total: Int,
        scope: CoroutineScope,
        onTick: (Int) -> Unit,
        onStart: (() -> Unit)? = null,
        onFinish: (() -> Unit)? = null,
    ): Job {
        return flow {
            for (i in total downTo 0) {
                emit(i)
                delay(1000)
            }
        }.flowOn(Dispatchers.Main)
            .onStart { onStart?.invoke() }
            .onCompletion { onFinish?.invoke() }
            .onEach { onTick.invoke(it) }
            .launchIn(scope)
    }
}