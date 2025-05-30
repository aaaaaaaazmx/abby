package com.cl.modules_my.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
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
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityRetainedScoped
class MyRepository @Inject constructor(private var remoteRepository: MyRemoteRepository) {

    /**
     * 上传图片
     */
    fun uploadImg(body: List<MultipartBody.Part>): Flow<HttpResult<String>> {
        return remoteRepository.uploadImg(body)
    }

    /**
     * 更新用户信息
     */
    fun modifyUserDetail(body: ModifyUserDetailReq): Flow<HttpResult<Boolean>> {
        return remoteRepository.modifyUserDetail(body)
    }

    /**
     * 更新植物信息
     */
    fun updatePlantInfo(body: UpPlantInfoReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.updatePlantInfo(body)
    }

    fun updateDeviceInfo(body: UpDeviceInfoReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.updateDeviceInfo(body)
    }

    fun addDevice(body: DeviceDetailsBean): Flow<HttpResult<BaseBean>> {
        return remoteRepository.addDevice(body)
    }

    /**
     * 获取植物信息
     */
    fun plantInfo(): Flow<HttpResult<PlantInfoData>> {
        return remoteRepository.plantInfo()
    }

    /**
     * 获取用户信息
     */
    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return remoteRepository.userDetail()
    }


    /**
     * 删除设备
     */
    fun deleteDevice(deviceId: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.deleteDevice(deviceId)
    }

    /**
     * 删除植物
     */
    fun plantDelete(uuid: String): Flow<HttpResult<Boolean>> {
        return remoteRepository.plantDelete(uuid)
    }

    /**
     * 是否种植
     */
    fun checkPlant(deviceSn: String? = ""): Flow<HttpResult<CheckPlantData>> {
        return remoteRepository.checkPlant(deviceSn)
    }

    /**
     * 更新app
     */
    fun getAppVersion(): Flow<HttpResult<AppVersionData>> {
        return remoteRepository.getAppVersion()
    }

    /**
     * 换水获取图文
     */
    fun advertising(type: String): Flow<HttpResult<MutableList<AdvertisingData>>> {
        return remoteRepository.advertising(type)
    }

    /**
     * 换水获取图文
     */
    fun getGuideInfo(type: String): Flow<HttpResult<GuideInfoData>> {
        return remoteRepository.getGuideInfo(type)
    }


    /**
     * 获取疑问信息
     */
    fun troubleShooting(): Flow<HttpResult<MyTroubleData>> {
        return remoteRepository.troubleShooting()
    }

    /**
     * 获取图文接口
     */
    fun getDetailByLearnMoreId(learnMoreId: String): Flow<HttpResult<DetailByLearnMoreIdData>> {
        return remoteRepository.getDetailByLearnMoreId(learnMoreId)
    }

    /**
     * HowTo
     */
    fun howTo(): Flow<HttpResult<MutableList<MyTroubleData.Bean>>> {
        return remoteRepository.howTo()
    }

    /**
     *  获取日历任务
     */
    fun getCalendar(startDate: String, endDate: String): Flow<HttpResult<MutableList<CalendarData>>> {
        return remoteRepository.getCalendar(startDate, endDate)
    }

    /**
     * 更新日历任务
     */
    fun updateTask(body: UpdateReq): Flow<HttpResult<String>> {
        return remoteRepository.updateTask(body)
    }

    /**
     * 解锁花期
     */
    fun unlockJourney(name: String, weight: String? = null): Flow<HttpResult<BaseBean>> {
        return remoteRepository.unlockJourney(name, weight)
    }

    /**
     * 日历-完成任务
     */
    fun finishTask(body: FinishTaskReq): Flow<HttpResult<String>> {
        return remoteRepository.finishTask(body)
    }

    /**
     * 跳过换水记录上报接口
     */
    fun deviceOperateStart(businessId: String, type: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.deviceOperateStart(businessId, type)
    }

    /**
     * 上报排水成功
     */
    fun deviceOperateFinish(type:String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.deviceOperateFinish(type)
    }

    fun checkSubscriberNumber(number:String): Flow<HttpResult<CheckSubscriberNumberBean>> {
        return remoteRepository.checkSubscriberNumber(number)
    }

    fun topUpSubscriberNumber(number:String): Flow<HttpResult<Boolean>> {
        return remoteRepository.topUpSubscriberNumber(number)
    }

    fun automaticLogin(body: AutomaticLoginReq): Flow<HttpResult<AutomaticLoginData>> {
        return remoteRepository.automaticLogin(body)
    }

    fun giveUpCheck(): Flow<HttpResult<GiveUpCheckData>> {
        return remoteRepository.giveUpCheck()
    }

    fun verifyEmail(email: String, type: String): Flow<HttpResult<Boolean>> {
        return remoteRepository.verifyEmail(email, type)
    }

    fun mergeAccount(req: MergeAccountReq): Flow<HttpResult<String>> {
        return remoteRepository.mergeAccount(req)
    }


    fun listDevice(): Flow<HttpResult<MutableList<ListDeviceBean>>> {
        return remoteRepository.listDevice()
    }

    fun automationList(accessoryId: String, deviceId: String, portId: String? = null, usbPort: String? = null): Flow<HttpResult<AutomationListBean>> {
        return remoteRepository.automationList(accessoryId, deviceId, portId, usbPort)
    }

    fun switchDevice(deviceId: String): Flow<HttpResult<String>> {
        return remoteRepository.switchDevice(deviceId)
    }

    fun statusSwitch(accessoryId: String, deviceId: String, status: String, usbPort: String? = null): Flow<HttpResult<BaseBean>> {
        return remoteRepository.statusSwitch(accessoryId, deviceId, status, usbPort)
    }

    fun openAutomation(req: OpenAutomationReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.openAutomation(req)
    }

    fun getAutomationRule(automationId: String?, accessoryId: String?): Flow<HttpResult<GetAutomationRuleBean>> {
        return remoteRepository.getAutomationRule(automationId, accessoryId)
    }

    fun deleteAutomation(automationId: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.deleteAutomation(automationId)
    }

    fun accessoryAdd(automationId: String, deviceId: String, accessoryDeviceId: String? = null): Flow<HttpResult<AccessoryAddData>> {
        return remoteRepository.accessoryAdd(automationId, deviceId, accessoryDeviceId)
    }

    fun configuationExecuteRule(req: ConfiguationExecuteRuleReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.configuationExecuteRule(req)
    }

    fun verifyCode(code: String, email: String): Flow<HttpResult<Boolean>> {
        return remoteRepository.verifyCode(code, email)
    }

    fun accessoryList(spaceType: String, deviceId: String): Flow<HttpResult<MutableList<AccessoryListBean>>> {
        return remoteRepository.accessoryList(spaceType, deviceId)
    }

    fun intercomDataAttributeSync(): Flow<HttpResult<Map<String, Any>>> {
        return remoteRepository.intercomDataAttributeSync()
    }

    fun wallpaperList(): Flow<HttpResult<MutableList<WallpaperListBean>>> {
        return remoteRepository.wallpaperList()
    }
    fun oxygenCoinList(): Flow<HttpResult<MutableList<OxygenCoinListBean>>> {
        return remoteRepository.oxygenCoinList()
    }

    fun oxygenCoinBillList(req: AccountFlowingReq): Flow<HttpResult<OxygenCoinBillList>> {
        return remoteRepository.oxygenCoinBillList(req)
    }

    fun resetPwd(req: ResetPwdReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.resetPwd(req)
    }

    fun updateInfo(body: UpdateInfoReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.updateInfo(body)
    }

    fun getAccessoryInfo(deviceId: String, accessoryDeviceId: String): Flow<HttpResult<UpdateInfoReq>> {
        return remoteRepository.getAccessoryInfo(deviceId, accessoryDeviceId)
    }

    fun getDeviceDetails(deviceId: String): Flow<HttpResult<DeviceDetailsBean>> {
        return remoteRepository.getDeviceDetails(deviceId)
    }

    fun getStrainName(strainName: String): Flow<HttpResult<MutableList<String>>> {
        return remoteRepository.getStrainName(strainName)
    }

    fun getSystemConfig(strainName: String): Flow<HttpResult<MutableList<SystemConfigBeanItem>>> {
        return remoteRepository.getSystemConfig(strainName)
    }

    fun getDigitalAsset(body: DigitalAsset): Flow<HttpResult<DigitalAssetData>> {
        return remoteRepository.getDigitalAsset(body)
    }

    fun showAchievement(achievementId: Int): Flow<HttpResult<BaseBean>> {
        return remoteRepository.showAchievement(achievementId)
    }

    fun getAchievements(): Flow<HttpResult<MutableList<AchievementBean>>> {
        return remoteRepository.getAchievements()
    }

    fun getFrames(): Flow<HttpResult<MutableList<AchievementBean>>> {
        return remoteRepository.getFrames()
    }

    fun showFrame(frameId: Int): Flow<HttpResult<BaseBean>> {
        return remoteRepository.showFrame(frameId)
    }

    fun follower(): Flow<HttpResult<MutableList<FolowerData>>> {
        return remoteRepository.follower()
    }

    fun following(): Flow<HttpResult<MutableList<FolowerData>>> {
        return remoteRepository.following()
    }

    fun accessorySubport(accessoryId: String, accessoryDeviceId: String?): Flow<HttpResult<AccessorySubportData>> {
        return remoteRepository.accessorySubport(accessoryId, accessoryDeviceId)
    }

    fun automationType(deviceId: String): Flow<HttpResult<MutableList<AutomationTypeBean>>> {
        return remoteRepository.automationType(deviceId)
    }

    fun updateSubport(deviceId: UpdateSubportReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.updateSubport(deviceId)
    }

    fun usbSwitch(req: UsbSwitchReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.usbSwitch(req)
    }
    fun getDpCache(req: String): Flow<HttpResult<UsbSwitchReq>> {
        return remoteRepository.getDpCache(req)
    }

    fun getPlantData(req: String): Flow<HttpResult<PlantData>> {
        return remoteRepository.getPlantData(req)
    }

    fun getVoucherList(): Flow<HttpResult<MutableList<VoucherBean>>> {
        return remoteRepository.getVoucherList()
    }

    fun exchangeInfo(): Flow<HttpResult<ExchangeInfoBean>> {
        return remoteRepository.exchangeInfo()
    }

    fun exchangeGiftVoucher(discountCode: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.exchangeGiftVoucher(discountCode)
    }

    fun conversations(taskNo: String? = null, textId: String? = null): Flow<HttpResult<ConversationsBean>> {
        return remoteRepository.conversations(taskNo, textId)
    }

}