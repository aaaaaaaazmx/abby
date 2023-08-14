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
import com.cl.common_base.widget.edittext.bean.FormatItemResult
import com.cl.modules_contact.repository.ContactRepository
import com.cl.modules_contact.request.AddTrendData
import com.cl.modules_contact.request.AddTrendReq
import com.cl.common_base.bean.ImageUrl
import com.cl.modules_contact.response.MentionData
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

     val shareToPublic by lazy {
        Prefs.getBoolean(Constants.Contact.KEY_SHARE_TO_PUBLIC, true)
    }

     val plantDataIsVisible by lazy {
        Prefs.getBoolean(Constants.Contact.KEY_PLANT_DATA_IS_VISIBLE, true)
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
     * 上传之后才发布的标记
     */
    private val _uploadImageFlag = MutableLiveData<Boolean>(false)
    val uploadImageFlag: LiveData<Boolean> = _uploadImageFlag
    fun setUploadImageFlag(req: Boolean) {
        _uploadImageFlag.value = req
    }

    /**
     * 选择gif的时长
     */
    private val _gifCheckBox = MutableLiveData<Boolean>(false)
    val gifCheckBox: LiveData<Boolean> = _gifCheckBox
    fun setGifDuration(req: Boolean) {
        _gifCheckBox.value = req
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

    /**
     * 图片上传船地址结合
     */
    private val _picAddress = MutableLiveData<MutableList<ImageUrl>>(mutableListOf())
    val picAddress: LiveData<MutableList<ImageUrl>> = _picAddress
    fun setPicAddress(url: ImageUrl) {
        _picAddress.value?.add(0, url)
    }
    fun deletePicAddress(index: Int) {
        if ((_picAddress.value?.size ?: 0) > 0) {
            _picAddress.value?.removeAt(index)
        }
    }

    fun clearPicAddress() {
        _picAddress.value?.clear()
    }

    /**
     * 记录已经选的好友
     */
    private val _selectFriends = MutableLiveData<MutableList<MentionData>>(mutableListOf())
    val selectFriends: LiveData<MutableList<MentionData>> = _selectFriends
    fun setSelectFriends(bean: MutableList<MentionData>) {
        _selectFriends.value?.clear()
        _selectFriends.value?.addAll(bean)
    }

    fun setSelectFriendsClear() {
        _selectFriends.value?.clear()
    }

    fun serSelectFriendsRemove(bean: MentionData) {
        _selectFriends.value?.remove(bean)
    }

    fun findDifferentItems(list1: MutableList<MentionData>, list2: MutableList<FormatItemResult>? = mutableListOf()): MutableList<MentionData> {
        val result = mutableListOf<MentionData>()
        if (list2?.isEmpty() == true) return result

        for (item1 in list1) {
            var found = false
            for (item2 in list2!!) {
                if (item1.userId == item2.id) {
                    found = true
                    break
                }
            }
            if (!found) {
                result.add(item1)
            }
        }
        logI("!2312312312: ${result.size}")
        return result
    }


    fun findDifferentItemForuserList(list1: MutableList<MentionData>, list2: MutableList<FormatItemResult>? = mutableListOf()): MutableList<FormatItemResult> {
        val result = mutableListOf<FormatItemResult>()
        if (list2?.isEmpty() == true) return result

        for (item1 in list2!!) {
            var found = false
            for (item2 in list1) {
                if (item1.id == item2.userId) {
                    found = true
                    break
                }
            }
            if (!found) {
                result.add(item1)
            }
        }

        result.forEach {
            logI("12313123: ${it.name}, ${it.id}, ${it.fromIndex}, ${it.length}")
        }

        return result
    }
}