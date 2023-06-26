package com.cl.modules_home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AcademyDetails
import com.cl.common_base.bean.AcademyListData
import com.cl.common_base.bean.RichTextData
import com.cl.common_base.bean.UpdateInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.file.FileUtil
import com.cl.common_base.util.file.SDCard
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.repository.HomeRepository
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@ActivityRetainedScoped
class HomeCameraViewModel @Inject constructor(private val repository: HomeRepository) : ViewModel() {

    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }


    // 获取当前设备信息
    val tuYaDeviceBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    /**
     * 保存SN
     */
    private val _sn = MutableLiveData<String>()
    val sn: LiveData<String> = _sn
    fun saveSn(sn: String) {
        _sn.value = sn
    }

    // 获取SN
    fun getSn() {
        ThingHomeSdk.newDeviceInstance(tuYaDeviceBean?.devId)?.let {
            it.getDp(TuYaDeviceConstants.KEY_DEVICE_REPAIR_REST_STATUS, object : IResultCallback {
                override fun onError(code: String?, error: String?) {
                    logI(
                        """
                        KEY_DEVICE_REPAIR_REST_STATUS: error
                        code: $code
                        error: $error
                    """.trimIndent()
                    )
                    ToastUtil.shortShow(error)
                    Reporter.reportTuYaError("newDeviceInstance", error, code)
                }

                override fun onSuccess() {
                    logI("sdasdas")
                }
            })
        }
    }

    /**
     * 获取配件信息
     */
    private val _getAccessoryInfo = MutableLiveData<Resource<UpdateInfoReq>>()
    val getAccessoryInfo: LiveData<Resource<UpdateInfoReq>> = _getAccessoryInfo
    fun getAccessoryInfo(deviceId: String) {
        viewModelScope.launch {
            repository.getAccessoryInfo(deviceId)
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
                    _getAccessoryInfo.value = it
                }
        }
    }

    /**
     * 富文本图文图文接口、所用东西都是从接口拉取
     */
    private val _richText = MutableLiveData<Resource<RichTextData>>()
    val richText: LiveData<Resource<RichTextData>> = _richText
    fun getRichText(txtId: String? = null, type: String? = null) {
        viewModelScope.launch {
            repository.getRichText(txtId, type)
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
                    _richText.value = it
                }
        }
    }

    /**
     * 获取配件信息
     */
    private val _getPartsInfo = MutableLiveData<Resource<UpdateInfoReq>>()
    val getPartsInfo: LiveData<Resource<UpdateInfoReq>> = _getPartsInfo
    fun getPartsInfo(deviceId: String = tuYaDeviceBean?.devId ?: "") {
        viewModelScope.launch {
            repository.getAccessoryInfo(deviceId)
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
                    _getPartsInfo.value = it
                }
        }
    }

    /**
     * 获取学院列表
     */
    private val _getAcademyList = MutableLiveData<Resource<UpdateInfoReq>>()
    val getAcademyList: LiveData<Resource<UpdateInfoReq>> = _getAcademyList
    fun getAcademyList(deviceId: String) {
        viewModelScope.launch {
            repository.getAccessoryInfo(deviceId)
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