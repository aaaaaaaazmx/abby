package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.bean.FolowerData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_my.repository.MyRepository
import com.cl.common_base.bean.DigitalAsset
import com.cl.common_base.bean.DigitalAssetData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class MyViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {

    // 用户信息
    val userInfo = {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
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
            }
            .catch {
                logD("catch ${it.message}")
                emit(
                    Resource.DataError(
                        -1,
                        "${it.message}"
                    )
                )
            }.collectLatest {
                _userDetail.value = it
            }
    }

    fun parseTime(time: String?): String {
        return time?.let {
            it.split("-")[1]
        } ?: ""
    }

    /**
     * 获取个人资产
     */
    private val _userAssets = MutableLiveData<Resource<DigitalAssetData>>()
    val userAssets: LiveData<Resource<DigitalAssetData>> = _userAssets
    fun userAssets() = viewModelScope.launch {
        repository.getDigitalAsset(DigitalAsset(userInfo()?.userId))
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
            }
            .catch {
                logD("catch ${it.message}")
                emit(
                    Resource.DataError(
                        -1,
                        "${it.message}"
                    )
                )
            }.collectLatest {
                _userAssets.value = it
            }
    }

    /**
     * 获取关注着列表 follower
     */
    private val _followList = MutableLiveData<Resource<MutableList<FolowerData>>>()
    val followList: LiveData<Resource<MutableList<FolowerData>>> = _followList
    fun followList() = viewModelScope.launch {
        repository.follower()
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
            }
            .catch {
                logD("catch ${it.message}")
                emit(
                    Resource.DataError(
                        -1,
                        "${it.message}"
                    )
                )
            }.collectLatest {
                _followList.value = it
            }
    }

    /**
     * 获取关注着列表 following
     */
    private val _followingList = MutableLiveData<Resource<MutableList<FolowerData>>>()
    val followingList: LiveData<Resource<MutableList<FolowerData>>> = _followingList
    fun followingList() = viewModelScope.launch {
        repository.following()
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
            }
            .catch {
                logD("catch ${it.message}")
                emit(
                    Resource.DataError(
                        -1,
                        "${it.message}"
                    )
                )
            }.collectLatest {
                _followingList.value = it
            }
    }

    fun isVipSHowText(): String {
        return if (userDetail.value?.data?.isVip == 0) {
            "Subscription Expired"
        } else {
            if (userDetail.value?.data?.continuousVip == 1) {
                // 如果是连续包月
                "Subscription Active"
            } else {
                "Subscription valid till " + parseTime(userDetail.value?.data?.subscriptionTime)
            }
        }
    }

}