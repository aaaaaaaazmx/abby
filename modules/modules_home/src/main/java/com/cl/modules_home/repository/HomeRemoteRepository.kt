package com.cl.modules_home.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.net.ServiceCreators
import com.cl.modules_home.request.AutomaticLoginReq
import com.cl.modules_home.response.AutomaticLoginData
import com.cl.modules_home.response.GuideInfoData
import com.cl.modules_home.response.PlantInfoData
import com.cl.modules_home.service.HttpHomeApiService
import com.google.api.Http
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 登录注册忘记密码提供
 */
@ActivityRetainedScoped
class HomeRemoteRepository @Inject constructor() {
    private val service = ServiceCreators.create(HttpHomeApiService::class.java)

    fun automaticLogin(body: AutomaticLoginReq): Flow<HttpResult<AutomaticLoginData>> {
        return service.automaticLogin(body)
    }

    fun getGuideInfo(body: String): Flow<HttpResult<GuideInfoData>> {
        return service.getGuideInfo(body)
    }

    fun saveOrUpdate(body: String): Flow<HttpResult<BaseBean>> {
        return service.saveOrUpdate(body)
    }

    fun startRunning(botanyId: String?, goon: Boolean): Flow<HttpResult<Boolean>> {
        return service.startRunning(botanyId, goon)
    }

    fun plantInfo(): Flow<HttpResult<PlantInfoData>> {
        return service.plantInfo()
    }

    fun advertising(type: String): Flow<HttpResult<MutableList<AdvertisingData>>> {
        return service.advertising(type)
    }

    fun environmentInfo(type: String): Flow<HttpResult<MutableList<EnvironmentInfoData>>> {
        return service.environmentInfo(type)
    }

    fun getUnread(): Flow<HttpResult<MutableList<UnreadMessageData>>> {
        return service.getUnread()
    }

    fun getRead(messageId: String): Flow<HttpResult<BaseBean>> {
        return service.getRead(messageId)
    }

    fun unlockJourney(name: String, weight: String? = null): Flow<HttpResult<BaseBean>> {
        return service.unlockJourney(name, weight)
    }

    fun getAppVersion(): Flow<HttpResult<AppVersionData>> {
        return service.getAppVersion()
    }

    fun userMessageFlag(flag: String, messageId: String): Flow<HttpResult<BaseBean>> {
        return service.userMessageFlag(flag, messageId)
    }

    fun deviceOperateStart(businessId: String, type: String): Flow<HttpResult<BaseBean>> {
        return service.deviceOperateStart(businessId, type)
    }

    fun deviceOperateFinish(type:String): Flow<HttpResult<BaseBean>> {
        return service.deviceOperateFinish(type)
    }
}