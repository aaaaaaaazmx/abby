package com.cl.modules_home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AcademyDetails
import com.cl.common_base.bean.AcademyListData
import com.cl.common_base.bean.RichTextData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_home.repository.HomeRepository
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class HomeAcademyViewModel @Inject constructor(private val repository: HomeRepository) : ViewModel() {


    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    /**
     * 获取学院列表
     */
    private val _getAcademyList = MutableLiveData<Resource<MutableList<AcademyListData>>>()
    val getAcademyList: LiveData<Resource<MutableList<AcademyListData>>> = _getAcademyList
    fun getAcademyList() {
        viewModelScope.launch {
            repository.getAcademyList()
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
                    _getAcademyList.value = it
                }
        }
    }


    /**
     * 获取学院详情列表
     */
    private val _getAcademyDetails = MutableLiveData<Resource<MutableList<AcademyDetails>>>()
    val getAcademyDetails: LiveData<Resource<MutableList<AcademyDetails>>> = _getAcademyDetails
    fun getAcademyDetails(academyId: String) {
        viewModelScope.launch {
            repository.getAcademyDetails(academyId)
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
                    _getAcademyDetails.value = it
                }
        }
    }


    /**
     * 学院已读消息
     */
    private val _messageRead = MutableLiveData<Resource<BaseBean>>()
    val messageRead: LiveData<Resource<BaseBean>> = _messageRead
    fun messageRead(academyDetailsId: String) {
        viewModelScope.launch {
            repository.messageRead(academyDetailsId)
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
                    _messageRead.value = it
                }
        }
    }

    // 已读列表
     val messageReadList = mutableListOf<String>()
    fun setReadList(id: String) {
        messageReadList?.add(id)
    }
}