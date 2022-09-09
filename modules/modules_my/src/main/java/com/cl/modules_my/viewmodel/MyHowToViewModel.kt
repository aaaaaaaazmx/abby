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
class MyHowToViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {


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
}