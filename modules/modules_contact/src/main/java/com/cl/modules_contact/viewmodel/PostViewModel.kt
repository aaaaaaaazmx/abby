package com.cl.modules_contact.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_contact.repository.ContactRepository
import com.cl.modules_contact.request.AddTrendData
import com.cl.modules_contact.request.AddTrendReq
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.response.MessageListData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

class PostViewModel @Inject constructor(private val repository: ContactRepository) : ViewModel() {
    val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    /**
     * 新增动态
     */
    private val _addData = MutableLiveData<Resource<AddTrendData>>()
    val addData: LiveData<Resource<AddTrendData>> = _addData
    fun add(req: AddTrendReq) = viewModelScope.launch {
        repository.add(req).map {
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
            _addData.value = it
        }
    }

    /**
     * 上传多张图片
     */
    private val _uploadImg = MutableLiveData<Resource<MutableList<String>>>()
    val uploadImg: LiveData<Resource<MutableList<String>>> = _uploadImg
    fun uploadImg(body: List<MultipartBody.Part>) = viewModelScope.launch {
        repository.uploadImg(body)
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
                        "${it.message}"
                    )
                )
            }.collectLatest {
                _uploadImg.value = it
            }
    }

    /**
     * 记录当前pH的值
     */
    private val _phValue = MutableLiveData<String?>(null)
    val phValue: LiveData<String?> = _phValue
    fun setPhValue(value: String?) {
        _phValue.value = value
    }
}