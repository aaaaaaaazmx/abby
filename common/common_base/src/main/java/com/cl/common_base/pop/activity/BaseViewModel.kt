package com.cl.common_base.pop.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.bean.AiCheckBean
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.ConversationsBean
import com.cl.common_base.bean.FinishTaskReq
import com.cl.common_base.bean.RichTextData
import com.cl.common_base.bean.SnoozeReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.thingclips.smart.android.user.bean.User
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityRetainedScoped
class BaseViewModel @Inject constructor(): ViewModel() {
    private val service = ServiceCreators.create(BaseApiService::class.java)

    /**
     * 涂鸦信息
     */
    val tuYaUser by lazy {
        val bean = Prefs.getString(Constants.Tuya.KEY_DEVICE_USER)
        GSON.parseObject(bean, User::class.java)
    }

    // 用户信息
    val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        parseObject
    }

    /**
     * 滑块的具体文案
     */
    private val _sliderText = MutableLiveData<String?>()
    val sliderText: LiveData<String?> = _sliderText
    fun getSliderText(txt: String?) {
        _sliderText.value = txt
    }

    /**
     * 上传多张图片
     */
    private val _uploadImg = MutableLiveData<Resource<MutableList<String>>>()
    val uploadImg: LiveData<Resource<MutableList<String>>> = _uploadImg
    fun uploadImg(body: List<MultipartBody.Part>) = viewModelScope.launch {
        service.uploadImages(body)
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
     * ai检查
     */
    private val _aiCheck = MutableLiveData<Resource<AiCheckBean>>()
    val aiCheck: LiveData<Resource<AiCheckBean>> = _aiCheck
    fun aiCheck(plantId: String, url: String) = viewModelScope.launch {
        service.aiCheckPeriod(plantId, url)
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
                _aiCheck.value = it
            }
    }

    // 会话ID
    private val _conversationId = MutableLiveData<Resource<ConversationsBean>>()
    val conversationId: LiveData<Resource<ConversationsBean>> = _conversationId
    fun conversations(taskNo: String? = null, textId: String? = null) {
        viewModelScope.launch {
            service.conversations(taskNo, textId)
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
                    _conversationId.value = it
                }
        }
    }

    /**
     * 富文本图文图文接口、所用东西都是从接口拉取
     */
    private val _richText = MutableLiveData<Resource<RichTextData>>()
    val richText: LiveData<Resource<RichTextData>> = _richText
    fun getRichText(taskId: String? = null, txtId: String? = null, type: String? = null) {
        viewModelScope.launch {
            service.getRichText(taskId, txtId, type)
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
     * 任务完成
     */
    private val _finishTask = MutableLiveData<Resource<String>>()
    val finishTask: LiveData<Resource<String>> = _finishTask
    fun finishTask(body: FinishTaskReq) {
        viewModelScope.launch {
            service.finishTask(body)
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
                    _finishTask.value = it
                }
        }
    }


    /**
     * 旧的开始种植植物
     */
    private val _startRunning = MutableLiveData<Resource<Boolean>>()
    val startRunning: LiveData<Resource<Boolean>> = _startRunning
    fun startRunning(botanyId: String?, goon: Boolean) {
        viewModelScope.launch {
            service.startRunning().map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                when(it) {
                   is Resource.Success -> {
                       // 检查是否种植
                       checkPlant()
                    }
                    else -> _startRunning.value = it
                }
            }
        }
    }

    /**
     * 插入篮子植物
     */
    private val _intoPlantBasket = MutableLiveData<Resource<BaseBean>>()
    val intoPlantBasket: LiveData<Resource<BaseBean>> = _intoPlantBasket
    fun intoPlantBasket() {
        viewModelScope.launch {
            service.intoPlantBasket().map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {
                emit(Resource.Loading())
            }.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _intoPlantBasket.value = it
            }
        }
    }

    /**
     * 检查是否种植过植物
     */
    private val _checkPlant = MutableLiveData<Resource<CheckPlantData>>()
    val checkPlant: LiveData<Resource<CheckPlantData>> = _checkPlant
    fun checkPlant() = viewModelScope.launch {
        service.checkPlant("").map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            _checkPlant.value = it
        }
    }

    /**
     * 延迟任务
     */
    private val _delayTask = MutableLiveData<Resource<BaseBean>>()
    val delayTask: LiveData<Resource<BaseBean>> = _delayTask
    fun delayTask(req: SnoozeReq) = viewModelScope.launch {
        service.snooze(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            _delayTask.value = it
        }
    }

}