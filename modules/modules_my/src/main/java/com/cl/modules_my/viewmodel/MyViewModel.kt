package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.bean.WallpaperListBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_my.repository.MyRepository
import com.cl.modules_my.request.DigitalAsset
import com.cl.modules_my.request.DigitalAssetData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.http.Body
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
     * 获取壁纸列表
     */
    private val _wallpaperList = MutableLiveData<Resource<MutableList<WallpaperListBean>>>()
    val wallpaperList: LiveData<Resource<MutableList<WallpaperListBean>>> = _wallpaperList
    fun wallpaperList() = viewModelScope.launch {
        repository.wallpaperList()
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
                _wallpaperList.value = it
            }
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
}