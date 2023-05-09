package com.cl.modules_contact.viewmodel

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
import com.cl.modules_contact.repository.ContactRepository
import com.cl.modules_contact.request.DeleteReq
import com.cl.modules_contact.request.LikeReq
import com.cl.modules_contact.request.MomentsDetailsReq
import com.cl.modules_contact.request.MyMomentsReq
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.request.ReportReq
import com.cl.modules_contact.request.RewardReq
import com.cl.modules_contact.request.SyncTrendReq
import com.cl.modules_contact.response.NewPageData
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
class MyJourneyViewModel @Inject constructor(private val repository: ContactRepository) : ViewModel() {


    val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
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
     * 获取用户信息
     */
    private val _userDetail = MutableLiveData<Resource<UserinfoBean.BasicUserBean>>()
    val userDetail: LiveData<Resource<UserinfoBean.BasicUserBean>> = _userDetail
    fun userDetail(userId: String) = viewModelScope.launch {
        repository.getOtherUserInfo(userId)
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

    /**
     * 点赞
     */
    private val _likeData = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val likeData: LiveData<Resource<com.cl.common_base.BaseBean>> = _likeData
    fun like(req: LikeReq) = viewModelScope.launch {
        repository.like(req).map {
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
        repository.unlike(req).map {
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
     * 打赏了多少氧气币，避免频繁刷新
     */
    private val _rewardOxygen = MutableLiveData<Int>(0)
    val rewardOxygen: LiveData<Int> = _rewardOxygen
    fun updateRewardOxygen(oxygen: Int) {
        _rewardOxygen.value = oxygen
    }


    /**
     *  打赏
     */
    private val _rewardData = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val rewardData: LiveData<Resource<com.cl.common_base.BaseBean>> = _rewardData
    fun reward(req: RewardReq) = viewModelScope.launch {
        repository.reward(req).map {
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
     * 获取我的动态
     */
    private val _myPageData = MutableLiveData<Resource<NewPageData>>()
    val myPageData: LiveData<Resource<NewPageData>> = _myPageData
    fun getMyPage(req: MyMomentsReq) = viewModelScope.launch {
        repository.myMoments(req).map {
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
            _myPageData.value = it
        }
    }


    /**
     * 删除动态
     */
    private val _deleteData = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val deleteData: LiveData<Resource<com.cl.common_base.BaseBean>> = _deleteData
    fun delete(req: DeleteReq) = viewModelScope.launch {
        repository.delete(req).map {
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
            _deleteData.value = it
        }
    }

    /**
     * 举报动态
     */
    private val _reportData = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val reportData: LiveData<Resource<com.cl.common_base.BaseBean>> = _reportData
    fun report(req: ReportReq) = viewModelScope.launch {
        repository.report(req).map {
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
            _reportData.value = it
        }
    }

    // 更改Current页码
    private val _updateCurrent = MutableLiveData<Int>(1)
    val updateCurrent: LiveData<Int> = _updateCurrent
    fun updateCurrent(current: Int) {
        _updateCurrent.value = current
    }

    /**
     * 公开动态
     */
    private val _publicData = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val publicData: LiveData<Resource<com.cl.common_base.BaseBean>> = _publicData
    fun public(req: SyncTrendReq) = viewModelScope.launch {
        repository.public(req).map {
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
            _publicData.value = it
        }
    }

    // 获取当前的点中的position
    private val _currentPosition = MutableLiveData<Int>(0)
    val currentPosition: LiveData<Int> = _currentPosition
    fun updateCurrentPosition(position: Int) {
        _currentPosition.value = position
    }

}