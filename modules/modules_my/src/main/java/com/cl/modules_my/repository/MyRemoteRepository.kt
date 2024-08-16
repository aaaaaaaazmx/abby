package com.cl.modules_my.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.bean.AccountFlowingReq
import com.cl.modules_my.request.ConfiguationExecuteRuleReq
import com.cl.modules_my.request.MergeAccountReq
import com.cl.common_base.bean.ModifyUserDetailReq
import com.cl.modules_my.request.OpenAutomationReq
import com.cl.common_base.bean.OxygenCoinListBean
import com.cl.modules_my.request.AccessorySubportData
import com.cl.modules_my.request.AchievementBean
import com.cl.modules_my.request.AutomationTypeBean
import com.cl.modules_my.request.DeviceDetailsBean
import com.cl.common_base.bean.DigitalAsset
import com.cl.common_base.bean.DigitalAssetData
import com.cl.common_base.bean.ConversationsBean
import com.cl.modules_my.request.ExchangeInfoBean
import com.cl.modules_my.request.ResetPwdReq
import com.cl.modules_my.request.UpdateSubportReq
import com.cl.modules_my.request.VoucherBean
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

    fun addDevice(body: DeviceDetailsBean): Flow<HttpResult<BaseBean>> {
        return service.addDevice(body)
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

    fun checkPlant(deviceSn: String? = ""): Flow<HttpResult<CheckPlantData>> {
        return service.checkPlant(deviceSn)
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
        deviceId: String,
        portId: String? = null,
        usbPort: String? = null,
    ): Flow<HttpResult<AutomationListBean>> {
        return service.automationList(accessoryId, deviceId, portId, usbPort)
    }

    fun statusSwitch(
        accessoryId: String,
        deviceId: String,
        status: String,
        usbPort: String? = null,
    ): Flow<HttpResult<BaseBean>> {
        return service.statusSwitch(accessoryId, deviceId, status, usbPort)
    }

    fun openAutomation(req: OpenAutomationReq): Flow<HttpResult<BaseBean>> {
        return service.openAutomation(req)
    }

    fun getAutomationRule(
        automationId: String?,
        accessoryId: String?
    ): Flow<HttpResult<GetAutomationRuleBean>> {
        return service.getAutomationRule(automationId, accessoryId)
    }

    fun deleteAutomation(automationId: String): Flow<HttpResult<BaseBean>> {
        return service.deleteAutomation(automationId)
    }

    fun accessoryAdd(automationId: String, deviceId: String, accessoryDeviceId: String? = null): Flow<HttpResult<AccessoryAddData>> {
        return service.accessoryAdd(automationId, deviceId, accessoryDeviceId)
    }

    fun configuationExecuteRule(req: ConfiguationExecuteRuleReq): Flow<HttpResult<BaseBean>> {
        return service.configuationExecuteRule(req)
    }

    fun verifyCode(code: String, email: String): Flow<HttpResult<Boolean>> {
        return service.verifyCode(code, email)
    }

    fun accessoryList(spaceType: String, deviceId: String): Flow<HttpResult<MutableList<AccessoryListBean>>> {
        return service.accessoryList(spaceType, deviceId)
    }

    fun intercomDataAttributeSync(): Flow<HttpResult<Map<String, Any>>> {
        return service.intercomDataAttributeSync()
    }

    fun wallpaperList(): Flow<HttpResult<MutableList<WallpaperListBean>>> {
        return service.wallpaperList()
    }


    fun oxygenCoinList(): Flow<HttpResult<MutableList<OxygenCoinListBean>>> {
        return service.oxygenCoinList()
    }

    fun oxygenCoinBillList(req: AccountFlowingReq): Flow<HttpResult<OxygenCoinBillList>> {
        return service.oxygenCoinBillList(req)
    }

    fun resetPwd(req: ResetPwdReq): Flow<HttpResult<BaseBean>> {
        return service.resetPwd(req)
    }

    fun updateInfo(body: UpdateInfoReq): Flow<HttpResult<BaseBean>> {
        return service.updateInfo(body)
    }

    fun getAccessoryInfo(deviceId: String, accessoryDeviceId: String): Flow<HttpResult<UpdateInfoReq>> {
        return service.getAccessoryInfo(deviceId, accessoryDeviceId)
    }

    fun getDeviceDetails(deviceId: String): Flow<HttpResult<DeviceDetailsBean>> {
        return service.getDeviceDetails(deviceId)
    }

    fun getStrainName(strainName: String): Flow<HttpResult<MutableList<String>>> {
        return service.getStrainName(strainName)
    }

    fun getSystemConfig(strainName: String): Flow<HttpResult<MutableList<SystemConfigBeanItem>>> {
        return service.getSystemConfig(strainName)
    }

    fun getDigitalAsset(body: DigitalAsset): Flow<HttpResult<DigitalAssetData>> {
        return service.getDigitalAsset(body)
    }

    fun showAchievement(achievementId: Int): Flow<HttpResult<BaseBean>> {
        return service.showAchievement(achievementId)
    }

    fun getAchievements(): Flow<HttpResult<MutableList<AchievementBean>>> {
        return service.getAchievements()
    }

    fun getFrames(): Flow<HttpResult<MutableList<AchievementBean>>> {
        return service.getFrames()
    }

    fun showFrame(frameId: Int): Flow<HttpResult<BaseBean>> {
        return service.showFrame(frameId)
    }

    fun follower(): Flow<HttpResult<MutableList<FolowerData>>> {
        return service.follower()
    }

    fun following(): Flow<HttpResult<MutableList<FolowerData>>> {
        return service.following()
    }

    fun accessorySubport(accessoryId: String, accessoryDeviceId: String?): Flow<HttpResult<AccessorySubportData>> {
        return service.accessorySubport(accessoryId, accessoryDeviceId)
    }

    fun automationType(deviceId: String): Flow<HttpResult<MutableList<AutomationTypeBean>>> {
        return service.automationType(deviceId)
    }

    fun updateSubport(deviceId: UpdateSubportReq): Flow<HttpResult<BaseBean>> {
        return service.updateSubport(deviceId)
    }

    fun usbSwitch(req: UsbSwitchReq): Flow<HttpResult<BaseBean>> {
        return service.usbSwitch(req)
    }
    fun getDpCache(req: String): Flow<HttpResult<UsbSwitchReq>> {
        return service.getDpCache(req)
    }

    fun getPlantData(req: String): Flow<HttpResult<PlantData>> {
        return service.getPlantData(req)
    }

    fun getVoucherList(): Flow<HttpResult<MutableList<VoucherBean>>> {
        return service.getVoucherList()
    }

    fun exchangeInfo(): Flow<HttpResult<ExchangeInfoBean>> {
        return service.exchangeInfo()
    }

    fun exchangeGiftVoucher(discountCode: String): Flow<HttpResult<BaseBean>> {
        return service.exchangeGiftVoucher(discountCode)
    }

    fun conversations(taskNo: String? = null, textId: String? = null): Flow<HttpResult<ConversationsBean>> {
        return service.conversations(taskNo, textId)
    }

}