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
import com.cl.modules_contact.request.DeleteReq
import com.cl.modules_contact.request.LikeReq
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.request.ReportReq
import com.cl.modules_contact.request.SyncTrendReq
import com.cl.modules_contact.response.MessageListData
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
class ContactNotificationViewModel @Inject constructor(private val repository: ContactRepository) : ViewModel() {

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
}