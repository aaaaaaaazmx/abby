package com.cl.modules_contact.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.bean.BaseBean
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_contact.repository.ContactRepository
import com.cl.modules_contact.request.CommentByMomentReq
import com.cl.modules_contact.request.DeleteReq
import com.cl.modules_contact.request.LikeReq
import com.cl.modules_contact.request.MomentsDetailsReq
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.request.PublishReq
import com.cl.modules_contact.request.ReplyReq
import com.cl.modules_contact.request.ReportReq
import com.cl.modules_contact.request.RewardReq
import com.cl.modules_contact.request.SyncTrendReq
import com.cl.modules_contact.response.CommentByMomentData
import com.cl.modules_contact.response.CommentDetailsData
import com.cl.modules_contact.response.MessageListData
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.response.PublishData
import com.cl.modules_contact.response.ReplyData
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
class ContactCommentViewModel @Inject constructor(private val repository: ContactRepository) : ViewModel() {

    val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }


    /**
     * 获取消息列表数据
     */
    private val _messageListData = MutableLiveData<Resource<MutableList<MessageListData>>>()
    val messageListData: LiveData<Resource<MutableList<MessageListData>>> = _messageListData
    fun messageList(req: NewPageReq) = viewModelScope.launch {
        repository.messageList(req).map {
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
            _messageListData.value = it
        }
    }

    /**
     * 获取评论列表
     */
    private val _commentListData = MutableLiveData<Resource<MutableList<CommentByMomentData>>>()
    val commentListData: LiveData<Resource<MutableList<CommentByMomentData>>> = _commentListData
    fun commentList(req: CommentByMomentReq) = viewModelScope.launch {
        repository.getCommentByMomentId(req).map {
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
            _commentListData.value = it
        }
    }


    /**
     * 获取动态详情
     */
    private val _momentDetailData = MutableLiveData<Resource<CommentDetailsData>>()
    val momentDetailData: LiveData<Resource<CommentDetailsData>> = _momentDetailData
    fun momentDetail(momentsId: Int) = viewModelScope.launch {
        repository.getMomentsDetails(momentsId).map {
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
            _momentDetailData.value = it
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

    /**
     * 发表评论 publish
     */
    private val _publishData = MutableLiveData<Resource<PublishData>>()
    val publishData: LiveData<Resource<PublishData>> = _publishData
    fun publish(req: PublishReq) = viewModelScope.launch {
        repository.publish(req).map {
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
            _publishData.value = it
        }
    }


    /**
     * 发表回复 reply
     */
    private val _replyData = MutableLiveData<Resource<ReplyData>>()
    val replyData: LiveData<Resource<ReplyData>> = _replyData
    fun reply(req: ReplyReq) = viewModelScope.launch {
        repository.reply(req).map {
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
            _replyData.value = it
        }
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




    // 更改Current页码
    private val _updateCurrent = MutableLiveData<Int>(1)
    val updateCurrent: LiveData<Int> = _updateCurrent
    fun updateCurrent(current: Int) {
        _updateCurrent.value = current
    }

    // 获取当前的点中的position
    private val _currentPosition = MutableLiveData<Int>(0)
    val currentPosition: LiveData<Int> = _currentPosition
    fun updateCurrentPosition(position: Int) {
        _currentPosition.value = position
    }

    /**
     * likeData
     */
    private val _likeReq = MutableLiveData<LikeReq>()
    val likeReq: LiveData<LikeReq> = _likeReq
    fun updateLikeData(req: LikeReq) {
        _likeReq.value = req
    }
}