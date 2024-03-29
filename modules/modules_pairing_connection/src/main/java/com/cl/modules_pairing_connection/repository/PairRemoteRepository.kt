package com.cl.modules_pairing_connection.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.AccessoryAddData
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.HttpResult
import com.cl.common_base.bean.SyncDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.net.ServiceCreators
import com.cl.modules_pairing_connection.service.HttpPairApiService
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 登录注册忘记密码提供
 */
@ActivityRetainedScoped
class PairRemoteRepository @Inject constructor() {
    private val service = ServiceCreators.create(HttpPairApiService::class.java)

    fun bindDevice(deviceId: String, deviceUuid: String): Flow<HttpResult<String>> {
        return service.bindDevice(deviceId, deviceUuid)
    }

    fun accessoryAdd(automationId: String, deviceId: String, accessoryDeviceId: String? = null): Flow<HttpResult<AccessoryAddData>> {
        return service.accessoryAdd(automationId, deviceId, accessoryDeviceId)
    }

    fun checkPlant(deviceUuid: String? = ""): Flow<HttpResult<CheckPlantData>> {
        return service.checkPlant(deviceUuid)
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
}