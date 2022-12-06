package com.cl.modules_home.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.bean.AutomaticLoginReq
import com.cl.common_base.bean.AutomaticLoginData
import com.cl.common_base.bean.GuideInfoData
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.service.BaseApiService
import com.cl.modules_home.service.HttpHomeApiService
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

    fun getRichText(txtId: String?, type: String?): Flow<HttpResult<RichTextData>> {
        return service.getRichText(txtId, type)
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

    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return service.userDetail()
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

    fun getFinishPage(): Flow<HttpResult<FinishPageData>> {
        return service.getFinishPage()
    }

    fun getDetailByLearnMoreId(learnMoreId: String): Flow<HttpResult<DetailByLearnMoreIdData>> {
        return service.getDetailByLearnMoreId(learnMoreId)
    }

    fun plantDelete(uuid: String): Flow<HttpResult<Boolean>> {
        return service.plantDelete(uuid)
    }

    fun checkPlant(uuid: String): Flow<HttpResult<CheckPlantData>> {
        return service.checkPlant(uuid)
    }

    fun plantFinish(botanyId: String): Flow<HttpResult<BaseBean>> {
        return service.plantFinish(botanyId)
    }

    fun getMessageDetail(messageId: String): Flow<HttpResult<DetailByLearnMoreIdData>> {
        return service.getMessageDetail(messageId)
    }

    fun start(): Flow<HttpResult<String>> {
        return service.start()
    }

    fun finishTask(body: FinishTaskReq): Flow<HttpResult<String>> {
        return service.finishTask(body)
    }

    fun updateTask(body: UpdateReq): Flow<HttpResult<String>> {
        return service.updateTask(body)
    }

    fun getAcademyList(): Flow<HttpResult<MutableList<AcademyListData>>> {
        return service.getAcademyList()
    }

    fun getAcademyDetails(academyId: String): Flow<HttpResult<MutableList<AcademyDetails>>> {
        return service.getAcademyDetails(academyId)
    }

    fun messageRead(academyDetailsId: String): Flow<HttpResult<BaseBean>> {
        return service.messageRead(academyDetailsId)
    }

    fun getHomePageNumber(): Flow<HttpResult<HomePageNumberData>> {
        return service.getHomePageNumber()
    }
}