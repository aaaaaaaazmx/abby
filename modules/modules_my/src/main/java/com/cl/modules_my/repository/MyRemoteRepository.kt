package com.cl.modules_my.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.net.ServiceCreators
import com.cl.modules_my.request.ConfiguationExecuteRuleReq
import com.cl.modules_my.request.MergeAccountReq
import com.cl.modules_my.request.ModifyUserDetailReq
import com.cl.modules_my.request.OpenAutomationReq
import com.cl.modules_my.service.HttpMyApiService
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * 我的界面提供
 */
@ActivityRetainedScoped
class MyRemoteRepository @Inject constructor() {
    private val service = ServiceCreators.create(HttpMyApiService::class.java)

    fun uploadImg(body: List<MultipartBody.Part>): Flow<HttpResult<String>> {
        return service.uploadImg(body)
    }

    fun modifyUserDetail(body: ModifyUserDetailReq): Flow<HttpResult<Boolean>> {
        return service.modifyUserDetail(body)
    }

    fun updatePlantInfo(body: UpPlantInfoReq): Flow<HttpResult<BaseBean>> {
        return service.updatePlantInfo(body)
    }

    fun updateDeviceInfo(body: UpDeviceInfoReq): Flow<HttpResult<BaseBean>> {
        return service.updateDeviceInfo(body)
    }

    fun plantInfo(): Flow<HttpResult<PlantInfoData>> {
        return service.plantInfo()
    }

    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return service.userDetail()
    }

    fun deleteDevice(deviceId: String): Flow<HttpResult<BaseBean>> {
        return service.deleteDevice(deviceId)
    }

    fun plantDelete(uuid: String): Flow<HttpResult<Boolean>> {
        return service.plantDelete(uuid)
    }

    fun checkPlant(uuid: String): Flow<HttpResult<CheckPlantData>> {
        return service.checkPlant(uuid)
    }

    fun getAppVersion(): Flow<HttpResult<AppVersionData>> {
        return service.getAppVersion()
    }

    fun advertising(type: String): Flow<HttpResult<MutableList<AdvertisingData>>> {
        return service.advertising(type)
    }

    fun getGuideInfo(type: String): Flow<HttpResult<GuideInfoData>> {
        return service.getGuideInfo(type)
    }

    fun troubleShooting(): Flow<HttpResult<MyTroubleData>> {
        return service.troubleShooting()
    }

    fun howTo(): Flow<HttpResult<MutableList<MyTroubleData.Bean>>> {
        return service.howTo()
    }

    fun getDetailByLearnMoreId(learnMoreId: String): Flow<HttpResult<DetailByLearnMoreIdData>> {
        return service.getDetailByLearnMoreId(learnMoreId)
    }

    fun getCalendar(
        startDate: String,
        endDate: String
    ): Flow<HttpResult<MutableList<CalendarData>>> {
        return service.getCalendar(startDate, endDate)
    }

    fun updateTask(body: UpdateReq): Flow<HttpResult<String>> {
        return service.updateTask(body)
    }

    fun unlockJourney(name: String, weight: String? = null): Flow<HttpResult<BaseBean>> {
        return service.unlockJourney(name, weight)
    }

    fun finishTask(body: FinishTaskReq): Flow<HttpResult<String>> {
        return service.finishTask(body)
    }

    fun deviceOperateStart(businessId: String, type: String): Flow<HttpResult<BaseBean>> {
        return service.deviceOperateStart(businessId, type)
    }

    fun deviceOperateFinish(type: String): Flow<HttpResult<BaseBean>> {
        return service.deviceOperateFinish(type)
    }

    fun checkSubscriberNumber(number: String): Flow<HttpResult<CheckSubscriberNumberBean>> {
        return service.checkSubscriberNumber(number)
    }

    fun topUpSubscriberNumber(number: String): Flow<HttpResult<Boolean>> {
        return service.topUpSubscriberNumber(number)
    }

    fun automaticLogin(body: AutomaticLoginReq): Flow<HttpResult<AutomaticLoginData>> {
        return service.automaticLogin(body)
    }

    fun giveUpCheck(): Flow<HttpResult<GiveUpCheckData>> {
        return service.giveUpCheck()
    }

    fun verifyEmail(email: String, type: String): Flow<HttpResult<Boolean>> {
        return service.verifyEmail(email, type)
    }

    fun mergeAccount(req: MergeAccountReq): Flow<HttpResult<String>> {
        return service.mergeAccount(req)
    }

    fun listDevice(): Flow<HttpResult<MutableList<ListDeviceBean>>> {
        return service.listDevice()
    }

    fun switchDevice(deviceId: String): Flow<HttpResult<String>> {
        return service.switchDevice(deviceId)
    }

    fun automationList(
        accessoryId: String,
        deviceId: String
    ): Flow<HttpResult<AutomationListBean>> {
        return service.automationList(accessoryId, deviceId)
    }

    fun statusSwitch(
        accessoryId: String,
        deviceId: String,
        status: String
    ): Flow<HttpResult<BaseBean>> {
        return service.statusSwitch(accessoryId, deviceId, status)
    }

    fun openAutomation(req: OpenAutomationReq): Flow<HttpResult<BaseBean>> {
        return service.openAutomation(req)
    }

    fun getAutomationRule(automationId: String?, accessoryId: String?): Flow<HttpResult<GetAutomationRuleBean>> {
        return service.getAutomationRule(automationId, accessoryId)
    }

    fun deleteAutomation(automationId: String): Flow<HttpResult<BaseBean>> {
        return service.deleteAutomation(automationId)
    }

    fun configuationExecuteRule(req: ConfiguationExecuteRuleReq): Flow<HttpResult<BaseBean>> {
        return service.configuationExecuteRule(req)
    }

    fun verifyCode(code: String, email: String): Flow<HttpResult<Boolean>> {
        return service.verifyCode(code, email)
    }

    fun accessoryList(): Flow<HttpResult<MutableList<AccessoryListBean>>> {
        return service.accessoryList()
    }

    fun intercomDataAttributeSync(): Flow<HttpResult<Map<String, Any>>> {
        return service.intercomDataAttributeSync()
    }

    fun wallpaperList(): Flow<HttpResult<MutableList<WallpaperListBean>>> {
        return service.wallpaperList()
    }


}