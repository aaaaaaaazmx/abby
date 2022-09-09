package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.bean.DetailByLearnMoreIdData
import com.cl.modules_my.repository.MyRepository
import com.cl.modules_my.repository.MyTroubleData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class MyTroubleViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {


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


    enum class Type {
        GROWING, ABBY, CONNECT
    }
}