package com.cl.common_base.pop.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.bean.FinishTaskReq
import com.cl.common_base.bean.LikeReq
import com.cl.common_base.bean.RewardReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class BasePumpViewModel @Inject constructor(): ViewModel() {

    /**
     * 排水第一步曲
     */
    private val service = ServiceCreators.create(BaseApiService::class.java)
    private val _deviceOperateStart = MutableLiveData<Resource<BaseBean>>()
    val deviceOperateStart: LiveData<Resource<BaseBean>> = _deviceOperateStart
    fun deviceOperateStart(business: String, type: String) {
        viewModelScope.launch {
            service.deviceOperateStart(business, type)
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
                    _deviceOperateStart.value = it
                }
        }
    }

    /**
     *  打赏
     */
    private val _rewardData = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val rewardData: LiveData<Resource<com.cl.common_base.BaseBean>> = _rewardData
    fun reward(req: RewardReq) = viewModelScope.launch {
        service.reward(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _rewardData.value = it
        }
    }

    /**
     * 点赞
     */
    private val _likeData = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val likeData: LiveData<Resource<com.cl.common_base.BaseBean>> = _likeData
    fun like(req: LikeReq) = viewModelScope.launch {
        service.like(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _likeData.value = it
        }
    }

    /**
     * 取消点赞
     */
    private val _unlikeData = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val unlikeData: LiveData<Resource<com.cl.common_base.BaseBean>> = _unlikeData
    fun unlike(req: LikeReq) = viewModelScope.launch {
        service.unlike(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _unlikeData.value = it
        }
    }

    /**
     * 任务完成
     */
    private val _finishTask = MutableLiveData<Resource<String>>()
    val finishTask: LiveData<Resource<String>> = _finishTask
    fun finishTask(body: FinishTaskReq) {
        viewModelScope.launch {
            service.finishTask(body)
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
                    _finishTask.value = it
                }
        }
    }

    // 更改Current页码
    private val _updateCurrent = MutableLiveData<Int>(1)
    val updateCurrent: LiveData<Int> = _updateCurrent
    fun updateCurrent(current: Int) {
        _updateCurrent.value = current
    }

    private val _advertising = MutableLiveData<Resource<MutableList<AdvertisingData>>>()
    val advertising: LiveData<Resource<MutableList<AdvertisingData>>> = _advertising
    fun advertising(type: String? = "0") {
        viewModelScope.launch {
            service.advertising(type ?: "0", updateCurrent.value ?: 1, 10)
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
                    _advertising.value = it
                }
        }
    }

    /**
     * 打赏了多少氧气币，避免频繁刷新
     */
    private val _rewardOxygen = MutableLiveData<Int>(0)
    val rewardOxygen: LiveData<Int> = _rewardOxygen
    fun updateRewardOxygen(oxygen: Int) {
        _rewardOxygen.value = oxygen
    }


    // 获取当前的点中的position
    private val _currentPosition = MutableLiveData<Int>(0)
    val currentPosition: LiveData<Int> = _currentPosition
    fun updateCurrentPosition(position: Int) {
        _currentPosition.value = position
    }
}