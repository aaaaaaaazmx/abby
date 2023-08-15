package com.cl.modules_planting_log.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.ImageUrl
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_planting_log.repository.PlantRepository
import com.cl.modules_planting_log.request.LogByIdData
import com.cl.modules_planting_log.request.LogListDataItem
import com.cl.modules_planting_log.request.LogListReq
import com.cl.modules_planting_log.request.LogSaveOrUpdateReq
import com.cl.modules_planting_log.request.LogTypeListDataItem
import com.cl.modules_planting_log.request.PlantIdByDeviceIdData
import com.cl.modules_planting_log.request.PlantInfoByPlantIdData
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

@ActivityRetainedScoped
class PlantingLogAcViewModel @Inject constructor(private val repository: PlantRepository) : ViewModel() {

    // 是否是公制
    val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)


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
     * 表单提交
     * 需要循环上传
     */
    fun submitTheForm(path: String): List<MultipartBody.Part> {
        //1.创建MultipartBody.Builder对象
        val builder = MultipartBody.Builder()
            //表单类型
            .setType(MultipartBody.FORM)

        //2.获取图片，创建请求体
        val file = File(path)
        //表单类x型
        //表单类型
        val body: RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)

        //3.调用MultipartBody.Builder的addFormDataPart()方法添加表单数据
        /**
         * ps:builder.addFormDataPart("code","123456");
         * ps:builder.addFormDataPart("file",file.getName(),body);
         */
        builder.addFormDataPart("imgType", "trend") //传入服务器需要的key，和相应value值
        builder.addFormDataPart("files", file.name, body) //添加图片数据，body创建的请求体
        //4.创建List<MultipartBody.Part> 集合，
        //  调用MultipartBody.Builder的build()方法会返回一个新创建的MultipartBody
        //  再调用MultipartBody的parts()方法返回MultipartBody.Part集合
        return builder.build().parts
    }

    /**
     * 上传多张图片
     */
    private val _uploadImg = MutableLiveData<Resource<MutableList<String>>>()
    val uploadImg: LiveData<Resource<MutableList<String>>> = _uploadImg
    fun uploadImg(body: List<MultipartBody.Part>) = viewModelScope.launch {
        repository.uploadImages(body)
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
     * 新增或者修改日志详情
     */
    private val _logSaveOrUpdate = MutableLiveData<Resource<Boolean>>()
    val logSaveOrUpdate: LiveData<Resource<Boolean>> = _logSaveOrUpdate
    fun saveOrUpdateLog(logSaveOrUpdateReq: LogSaveOrUpdateReq) {
        viewModelScope.launch {
            repository.logSaveOrUpdate(logSaveOrUpdateReq).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {}.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _logSaveOrUpdate.value = it
            }
        }
    }

    /**
     * 获取日志类型列表
     */
    private val _getLogTypeList = MutableLiveData<Resource<List<LogTypeListDataItem>>>()
    val getLogTypeList: LiveData<Resource<List<LogTypeListDataItem>>> = _getLogTypeList
    fun getLogTypeList(showType: String, logId: String?) {
        viewModelScope.launch {
            repository.getLogTypeList(showType, logId).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {}.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _getLogTypeList.value = it
            }
        }
    }


    /**
     * 获取日志详情
     */
    private val _getLogById = MutableLiveData<Resource<LogSaveOrUpdateReq>>()
    val getLogById: LiveData<Resource<LogSaveOrUpdateReq>> = _getLogById
    fun getLogById(logId: String) {
        viewModelScope.launch {
            repository.getLogById(logId).map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {}.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _getLogById.value = it
            }
        }
    }


    /**
     * 获取植物基本信息
     */
    private val _plantInfo = MutableLiveData<Resource<PlantInfoData>>()
    val plantInfo: LiveData<Resource<PlantInfoData>> = _plantInfo
    fun plantInfo() {
        viewModelScope.launch {
            repository.plantInfo().map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code, it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }.flowOn(Dispatchers.IO).onStart {}.catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1, "${it.message}"
                    )
                )
            }.collectLatest {
                _plantInfo.value = it
            }
        }
    }

    /**
     * 根据DeviceId获取植物Id
     */
    private val _getPlantIdByDeviceId = MutableLiveData<Resource<MutableList<PlantIdByDeviceIdData>>>()
    val getPlantIdByDeviceId: LiveData<Resource<MutableList<PlantIdByDeviceIdData>>> = _getPlantIdByDeviceId
    fun getPlantIdByDeviceId(deviceId: String) = viewModelScope.launch {
        repository.getPlantIdByDeviceId(deviceId).map {
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
            _getPlantIdByDeviceId.value = it
        }
    }

    /**
     * 根据植物ID获取植物的信息
     */
    private val _getPlantInfoByPlantId = MutableLiveData<Resource<PlantInfoByPlantIdData>>()
    val getPlantInfoByPlantId: LiveData<Resource<PlantInfoByPlantIdData>> = _getPlantInfoByPlantId
    fun getPlantInfoByPlantId(plantId: Int) = viewModelScope.launch {
        repository.getPlantInfoByPlantId(plantId).map {
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
            _getPlantInfoByPlantId.value = it
        }
    }

    /**
     * 根据植物Id、和植物周期、获取植物日志列表
     */
    private val _getLogList = MutableLiveData<Resource<MutableList<LogListDataItem>>>()
    val getLogList: LiveData<Resource<MutableList<LogListDataItem>>> = _getLogList
    fun getLogList(body: LogListReq) = viewModelScope.launch {
        repository.getLogList(body).map {
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
            _getLogList.value = it
        }
    }



}