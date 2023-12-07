package com.cl.modules_pairing_connection.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AccessoryAddData
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.HttpResult
import com.cl.common_base.bean.SyncDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ActivityRetainedScoped
class PairRepository @Inject constructor(private var remoteRepository: PairRemoteRepository) {

    /**
     * 绑定设备
     */
    fun bindDevice(deviceId: String, deviceUuid: String): Flow<HttpResult<String>> {
        return remoteRepository.bindDevice(deviceId, deviceUuid)
    }

    fun accessoryAdd(automationId: String, deviceId: String, accessoryDeviceId: String? = null): Flow<HttpResult<AccessoryAddData>> {
        return remoteRepository.accessoryAdd(automationId, deviceId, accessoryDeviceId)
    }

    /**
     * 检查用户是否种植过植物
     */
    fun checkPlant(deviceSn: String? = ""): Flow<HttpResult<CheckPlantData>> {
        return remoteRepository.checkPlant(deviceSn)
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
}