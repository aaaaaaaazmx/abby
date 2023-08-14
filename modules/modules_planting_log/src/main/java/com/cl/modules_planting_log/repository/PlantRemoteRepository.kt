package com.cl.modules_planting_log.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.HttpResult
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.bean.SyncDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.net.ServiceCreators
import com.cl.modules_planting_log.request.LogByIdData
import com.cl.modules_planting_log.request.LogListDataItem
import com.cl.modules_planting_log.request.LogListReq
import com.cl.modules_planting_log.request.LogSaveOrUpdateReq
import com.cl.modules_planting_log.request.LogTypeListDataItem
import com.cl.modules_planting_log.request.PlantIdByDeviceIdData
import com.cl.modules_planting_log.request.PlantInfoByPlantIdData
import com.cl.modules_planting_log.service.HttpPlantApiService
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * 登录注册忘记密码提供
 */
@ActivityRetainedScoped
class PlantRemoteRepository @Inject constructor() {
    private val service = ServiceCreators.create(HttpPlantApiService::class.java)

    fun bindDevice(deviceId: String, deviceUuid: String): Flow<HttpResult<String>> {
        return service.bindDevice(deviceId, deviceUuid)
    }

    fun checkPlant(body: String): Flow<HttpResult<CheckPlantData>> {
        return service.checkPlant(body)
    }

    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return service.userDetail()
    }

    fun checkSN(deviceSn: String): Flow<HttpResult<BaseBean>> {
        return service.checkSN(deviceSn)
    }

    fun syncDeviceInfo(syncDeviceInfo: SyncDeviceInfoReq): Flow<HttpResult<BaseBean>> {
        return service.syncDeviceInfo(syncDeviceInfo)
    }

    fun getLogById(id: Int): Flow<HttpResult<LogSaveOrUpdateReq>> {
        return service.getLogById(id)
    }

    fun getLogTypeList(showType: String, logId: Int): Flow<HttpResult<List<LogTypeListDataItem>>> {
        return service.getLogTypeList(showType, logId)
    }

    fun getPlantIdByDeviceId(deviceId: String): Flow<HttpResult<MutableList<PlantIdByDeviceIdData>>> {
        return service.getPlantIdByDeviceId(deviceId)
    }


    fun getPlantInfoByPlantId(plantId: Int): Flow<HttpResult<PlantInfoByPlantIdData>> {
        return service.getPlantInfoByPlantId(plantId)
    }

    fun getLogList(body: LogListReq): Flow<HttpResult<MutableList<LogListDataItem>>> {
        return service.getLogList(body)
    }

    fun logSaveOrUpdate(body: LogSaveOrUpdateReq): Flow<HttpResult<Boolean>> {
        return service.logSaveOrUpdate(body)
    }

    fun plantInfo(): Flow<HttpResult<PlantInfoData>> {
        return service.plantInfo()
    }

    fun uploadImages(body: List<MultipartBody.Part>): Flow<HttpResult<MutableList<String>>> {
        return service.uploadImages(body)
    }

    fun closeTips(period: String, plantId: String): Flow<HttpResult<Boolean>> {
        return service.closeTips(period, plantId)
    }

}