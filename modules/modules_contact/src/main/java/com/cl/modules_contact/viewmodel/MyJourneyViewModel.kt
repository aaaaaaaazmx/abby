package com.cl.modules_contact.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.FolowerData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.bean.WallpaperListBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_contact.repository.ContactRepository
import com.cl.modules_contact.request.DeleteReq
import com.cl.common_base.bean.LikeReq
import com.cl.modules_contact.request.MyMomentsReq
import com.cl.modules_contact.request.ReportReq
import com.cl.common_base.bean.RewardReq
import com.cl.common_base.bean.UpdateFollowStatusReq
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
     * 判断是否取消和关注的动作
     */
    private val _isFollowAction = MutableLiveData<Boolean>(false)
    val isFollowAction: LiveData<Boolean> = _isFollowAction
    fun updateIsFollowAction(isFollow: Boolean) {
        _isFollowAction.value = isFollow
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
     * 获取用户信息
     */
    private val _otherDetail = MutableLiveData<Resource<UserinfoBean.BasicUserBean>>()
    val userAssets: LiveData<Resource<UserinfoBean.BasicUserBean>> = _otherDetail
    fun otherUserDetail(userId: String) = viewModelScope.launch {
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
                _otherDetail.value = it
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
     * 获取最新动态信息
     */
    private val _hotReduce = MutableLiveData<Resource<BaseBean>>()
    val hotReduce: LiveData<Resource<BaseBean>> = _hotReduce
    fun hotReduce(req: String) = viewModelScope.launch {
        repository.hotReduce(req).map {
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
            _hotReduce.value = it
        }
    }
    /**
     * 公开动态
     */
    private val _publicData = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val publicData: LiveData<Resource<com.cl.common_base.BaseBean>> = _publicData
    fun public(syncTrend: Int, momentId: String) = viewModelScope.launch {
        repository.public(syncTrend, momentId).map {
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

    /**
     * 修改跟随者状态
     */
    private val _updateFollowStatus = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val updateFollowStatus: LiveData<Resource<com.cl.common_base.BaseBean>> = _updateFollowStatus
    fun updateFollowStatus(req: UpdateFollowStatusReq) = viewModelScope.launch {
        repository.updateFollowStatus(req).map {
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
            _updateFollowStatus.value = it
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


}