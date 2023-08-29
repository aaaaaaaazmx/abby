package com.cl.modules_planting_log.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.HttpResult
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.bean.SyncDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.modules_planting_log.request.LogByIdData
import com.cl.modules_planting_log.request.LogListDataItem
import com.cl.modules_planting_log.request.LogListReq
import com.cl.modules_planting_log.request.LogSaveOrUpdateReq
import com.cl.modules_planting_log.request.LogTypeListDataItem
import com.cl.modules_planting_log.request.PlantIdByDeviceIdData
import com.cl.modules_planting_log.request.PlantInfoByPlantIdData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityRetainedScoped
class PlantRepository @Inject constructor(private var remoteRepository: PlantRemoteRepository) {

    /**
     * 绑定设备
     */
    fun bindDevice(deviceId: String, deviceUuid: String): Flow<HttpResult<String>> {
        return remoteRepository.bindDevice(deviceId, deviceUuid)
    }

    /**
     * 检查用户是否种植过植物
     */
    fun checkPlant(body: String): Flow<HttpResult<CheckPlantData>> {
        return remoteRepository.checkPlant(body)
    }

    /**
     * 获取用户信息
     */
    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return remoteRepository.userDetail()
    }

    /**
     * 检查用户SN是否正确
     */
    fun checkSN(deviceSn: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.checkSN(deviceSn)
    }

    fun syncDeviceInfo(syncDeviceInfo: SyncDeviceInfoReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.syncDeviceInfo(syncDeviceInfo)
    }

    fun getLogById(id: String?): Flow<HttpResult<LogSaveOrUpdateReq>> {
        return remoteRepository.getLogById(id)
    }

    fun getLogTypeList(showType: String, logId: String?): Flow<HttpResult<List<LogTypeListDataItem>>> {
        return remoteRepository.getLogTypeList(showType, logId)
    }

    fun getPlantIdByDeviceId(deviceId: String): Flow<HttpResult<MutableList<PlantIdByDeviceIdData>>> {
        return remoteRepository.getPlantIdByDeviceId(deviceId)
    }

    fun getPlantInfoByPlantId(plantId: Int): Flow<HttpResult<PlantInfoByPlantIdData>> {
        return remoteRepository.getPlantInfoByPlantId(plantId)
    }

    fun getLogList(body: LogListReq): Flow<HttpResult<MutableList<LogListDataItem>>> {
        return remoteRepository.getLogList(body)
    }

    fun logSaveOrUpdate(body: LogSaveOrUpdateReq): Flow<HttpResult<Boolean>> {
        return remoteRepository.logSaveOrUpdate(body)
    }

    /**
     * 获取植物基本信息
     */
    fun plantInfo(): Flow<HttpResult<PlantInfoData>> {
        return remoteRepository.plantInfo()
    }

    fun uploadImages(body: List<MultipartBody.Part>): Flow<HttpResult<MutableList<String>>> {
        return remoteRepository.uploadImages(body)
    }

    fun closeTips(period: String, plantId: String): Flow<HttpResult<Boolean>> {
        return remoteRepository.closeTips(period, plantId)
    }
}