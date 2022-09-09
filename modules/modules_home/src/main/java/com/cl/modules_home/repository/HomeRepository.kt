package com.cl.modules_home.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.modules_home.request.AutomaticLoginReq
import com.cl.modules_home.response.AutomaticLoginData
import com.cl.modules_home.response.GuideInfoData
import com.cl.modules_home.response.PlantInfoData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ActivityRetainedScoped
class HomeRepository @Inject constructor(private var remoteRepository: HomeRemoteRepository) {

    /**
     * 登录
     */
    fun automaticLogin(body: AutomaticLoginReq): Flow<HttpResult<AutomaticLoginData>> {
        return remoteRepository.automaticLogin(body)
    }

    /**
     * 图文引导获取
     */
    fun getGuideInfo(body: String): Flow<HttpResult<GuideInfoData>> {
        return remoteRepository.getGuideInfo(body)
    }

    /**
     * 上报引导标记
     */
    fun saveOrUpdate(body: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.saveOrUpdate(body)
    }

    /**
     * 开始种植植物
     */
    fun startRunning(botanyId: String?, goon: Boolean): Flow<HttpResult<Boolean>> {
        return remoteRepository.startRunning(botanyId, goon)
    }

    /**
     * 获取植物基本信息
     */
    fun plantInfo(): Flow<HttpResult<PlantInfoData>> {
        return remoteRepository.plantInfo()
    }

    /**
     * 获取广告图文信息
     */
    fun advertising(type: String): Flow<HttpResult<MutableList<AdvertisingData>>> {
        return remoteRepository.advertising(type)
    }

    /**
     * 获取植物的环境信息
     */
    fun environmentInfo(type: String): Flow<HttpResult<MutableList<EnvironmentInfoData>>> {
        return remoteRepository.environmentInfo(type)
    }

    /**
     * 获取未读消息
     */
    fun getUnread(): Flow<HttpResult<MutableList<UnreadMessageData>>> {
        return remoteRepository.getUnread()
    }

    /**
     * 标记为已读消息
     */
    fun getRead(messageId: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.getRead(messageId)
    }

    /**
     * 解锁花期
     */
    fun unlockJourney(name: String, weight: String? = null): Flow<HttpResult<BaseBean>> {
        return remoteRepository.unlockJourney(name, weight)
    }

    /**
     * 检查版本更新
     */
    fun getAppVersion(): Flow<HttpResult<AppVersionData>> {
        return remoteRepository.getAppVersion()
    }

    /**
     * 用户标记步骤到哪一步了。用户排水、加水、加肥
     */
    fun userMessageFlag(flag: String, messageId: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.userMessageFlag(flag, messageId)
    }

    /**
     * 设备开始操作
     */
    fun deviceOperateStart(businessId: String, type: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.deviceOperateStart(businessId, type)
    }

    /**
     * 设备完成 操作
     */
    fun deviceOperateFinish(type: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.deviceOperateFinish(type)
    }

    /**
     * 获取完成界面配置
     */
    fun getFinishPage(): Flow<HttpResult<FinishPageData>> {
        return remoteRepository.getFinishPage()
    }

    /**
     * 获取图文详情
     */
    fun getDetailByLearnMoreId(learnMoreId: String): Flow<HttpResult<DetailByLearnMoreIdData>> {
        return remoteRepository.getDetailByLearnMoreId(learnMoreId)
    }

    /**
     * 删除植物、重新种植
     */
    fun plantDelete(uuid: String): Flow<HttpResult<Boolean>> {
        return remoteRepository.plantDelete(uuid)
    }

    /**
     * 检查是否种植过
     */
    fun checkPlant(uuid: String): Flow<HttpResult<CheckPlantData>> {
        return remoteRepository.checkPlant(uuid)
    }


    /**
     * 种植完成
     */
    fun plantFinish(botanyId: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.plantFinish(botanyId)
    }

    /**
     * 获取图文详情
     */
    fun getMessageDetail(messageId: String): Flow<HttpResult<DetailByLearnMoreIdData>> {
        return remoteRepository.getMessageDetail(messageId)
    }
}