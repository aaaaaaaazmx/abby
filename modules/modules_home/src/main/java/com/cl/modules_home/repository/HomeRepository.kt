package com.cl.modules_home.repository

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.bean.AutomaticLoginReq
import com.cl.common_base.bean.AutomaticLoginData
import com.cl.common_base.bean.GuideInfoData
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.bean.UpdateInfoReq
import com.cl.modules_home.request.CheckEnvData
import com.cl.modules_home.request.CycleListBean
import com.cl.modules_home.request.DeleteTaskReq
import com.cl.modules_home.request.EnvData
import com.cl.modules_home.request.EnvDeleteReq
import com.cl.modules_home.request.EnvParamListBeanItem
import com.cl.modules_home.request.EnvParamListReq
import com.cl.modules_home.request.EnvSaveReq
import com.cl.modules_home.request.MultiPlantListData
import com.cl.modules_home.request.PeriodListBody
import com.cl.modules_home.request.PeriodListSaveReq
import com.cl.modules_home.request.SaveTaskReq
import com.cl.modules_home.request.SeedCheckNoTaprootData
import com.cl.modules_home.request.Task
import com.cl.modules_home.request.TaskConfigurationListData
import com.cl.modules_home.request.TempData
import com.cl.modules_home.request.UpdateFanModelReq
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
     * 更新设备信息
     */
    fun updateDeviceInfo(body: UpDeviceInfoReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.updateDeviceInfo(body)
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
    fun startRunning(botanyId: String?, goon: Boolean, templateId: String? = null, step: String? = null,): Flow<HttpResult<Boolean>> {
        return remoteRepository.startRunning(botanyId, goon, templateId, step)
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
    fun environmentInfo(type: EnvironmentInfoReq): Flow<HttpResult<EnvironmentInfoData>> {
        return remoteRepository.environmentInfo(type)
    }

    /**
     * 获取未读消息
     */
    fun getUnread(): Flow<HttpResult<MutableList<UnreadMessageData>>> {
        return remoteRepository.getUnread()
    }

    /**
     * 获取用户信息
     */
    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>> {
        return remoteRepository.userDetail()
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
    fun checkPlant(deviceSn: String? = ""): Flow<HttpResult<CheckPlantData>> {
        return remoteRepository.checkPlant(deviceSn)
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

    /**
     * 开始种植
     */
    fun start(): Flow<HttpResult<String>> {
        return remoteRepository.start()
    }

    /**
     * 任务完成、解锁周期
     */
    fun finishTask(body: FinishTaskReq): Flow<HttpResult<String>> {
        return remoteRepository.finishTask(body)
    }

    /**
     * 更新任务
     */
    fun updateTask(body: UpdateReq): Flow<HttpResult<String>> {
        return remoteRepository.updateTask(body)
    }

    fun getAcademyList(): Flow<HttpResult<MutableList<AcademyListData>>> {
        return remoteRepository.getAcademyList()
    }

    fun getAcademyDetails(academyId: String): Flow<HttpResult<MutableList<AcademyDetails>>> {
        return remoteRepository.getAcademyDetails(academyId)
    }

    /**
     * 消息已读
     */
    fun messageRead(academyDetailsId: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.messageRead(academyDetailsId)
    }

    fun getHomePageNumber(): Flow<HttpResult<HomePageNumberData>> {
        return remoteRepository.getHomePageNumber()
    }

    fun startSubscriber(): Flow<HttpResult<BaseBean>> {
        return remoteRepository.startSubscriber()
    }

    fun checkFirstSubscriber(): Flow<HttpResult<Boolean>> {
        return remoteRepository.checkFirstSubscriber()
    }

    fun whetherSubCompensation(): Flow<HttpResult<WhetherSubCompensationData>> {
        return remoteRepository.whetherSubCompensation()
    }

    fun compensatedSubscriber(): Flow<HttpResult<BaseBean>> {
        return remoteRepository.compensatedSubscriber()
    }

    fun listDevice(): Flow<HttpResult<MutableList<ListDeviceBean>>> {
        return remoteRepository.listDevice()
    }

    fun switchDevice(deviceId: String): Flow<HttpResult<String>> {
        return remoteRepository.switchDevice(deviceId)
    }

    fun updatePlantInfo(body: UpPlantInfoReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.updatePlantInfo(body)
    }

    fun skipGerminate(): Flow<HttpResult<BaseBean>> {
        return remoteRepository.skipGerminate()
    }

    fun intoPlantBasket(): Flow<HttpResult<BaseBean>> {
        return remoteRepository.intoPlantBasket()
    }

    fun intercomDataAttributeSync(): Flow<HttpResult<Map<String, Any>>> {
        return remoteRepository.intercomDataAttributeSync()
    }

    fun unlockNow(plantId: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.unlockNow(plantId)
    }

    fun updateInfo(body: UpdateInfoReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.updateInfo(body)
    }

    fun getAccessoryInfo(deviceId: String, accessoryDeviceId: String): Flow<HttpResult<UpdateInfoReq>> {
        return remoteRepository.getAccessoryInfo(deviceId, accessoryDeviceId)
    }

    fun oxygenCoinList(): Flow<HttpResult<MutableList<OxygenCoinListBean>>> {
        return remoteRepository.oxygenCoinList()
    }

    fun getGrantOxygen(orderNo: String): Flow<HttpResult<BaseBean>> {
        return remoteRepository.getGrantOxygen(orderNo)
    }

    fun popupList(): Flow<HttpResult<MutableList<MedalPopData>>> {
        return remoteRepository.popupList()
    }

    fun addProModeRecord(req: ProModeInfoBean): Flow<HttpResult<BaseBean>> {
        return remoteRepository.addProModeRecord(req)
    }
    fun updateProModeRecord(req: ProModeInfoBean): Flow<HttpResult<BaseBean>> {
        return remoteRepository.updateProModeRecord(req)
    }

    fun getProModeByDeviceId(req: String): Flow<HttpResult<MutableList<ProModeInfoBean>>> {
        return remoteRepository.getProModeByDeviceId(req)
    }

    fun addProModeInfo(req: ProModeInfoBean): Flow<HttpResult<BaseBean>> {
        return remoteRepository.addProModeInfo(req)
    }

    fun getProModeInfo(req: String): Flow<HttpResult<ProModeInfoBean>> {
        return remoteRepository.getProModeInfo(req)
    }

    fun getPlantData(req: String): Flow<HttpResult<PlantData>> {
        return remoteRepository.getPlantData(req)
    }

    fun getPlantIdByDeviceId(deviceId: String): Flow<HttpResult<MutableList<PlantIdByDeviceIdData>>> {
        return remoteRepository.getPlantIdByDeviceId(deviceId)
    }

    fun getCycleList(plantId: PeriodListBody): Flow<HttpResult<MutableList<CycleListBean>>> {
        return remoteRepository.getCycleList(plantId)
    }

    fun createCalendar(plantId: PeriodListBody): Flow<HttpResult<TempData>> {
        return remoteRepository.createCalendar(plantId)
    }

    fun periodListSave(plantId: PeriodListSaveReq): Flow<HttpResult<Boolean>> {
        return remoteRepository.periodListSave(plantId)
    }

    fun getEnvParamList(plantId: EnvParamListReq): Flow<HttpResult<EnvData>> {
        return remoteRepository.getEnvParamList(plantId)
    }

    fun envParamDelete(plantId: EnvDeleteReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.envParamDelete(plantId)
    }

    fun envParamListCheck(plantId: EnvSaveReq): Flow<HttpResult<CheckEnvData>> {
        return remoteRepository.envParamListCheck(plantId)
    }

    fun envParamListSave(plantId: EnvSaveReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.envParamListSave(plantId)
    }

    fun taskConfigurationList(req: EnvSaveReq): Flow<HttpResult<TaskConfigurationListData>> {
        return remoteRepository.taskConfigurationList(req)
    }

    fun taskList(req: EnvSaveReq): Flow<HttpResult<MutableList<Task>>> {
        return remoteRepository.taskList(req)
    }

    fun taskListSave(req: SaveTaskReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.taskListSave(req)
    }

    fun taskDelete(req: DeleteTaskReq): Flow<HttpResult<Boolean>> {
        return remoteRepository.taskDelete(req)
    }

    fun getFanModel(): Flow<HttpResult<MutableList<UpdateFanModelReq>>> {
        return remoteRepository.getFanModel()
    }

    fun updateFanModel(req: UpdateFanModelReq): Flow<HttpResult<BaseBean>> {
        return remoteRepository.updateFanModel(req)
    }

    fun seedCheckNoTaproot(plantId: String): Flow<HttpResult<SeedCheckNoTaprootData>> {
        return remoteRepository.seedCheckNoTaproot(plantId)
    }

    fun multiplantList (plantId: String): Flow<HttpResult<MutableList<MultiPlantListData>>> {
        return remoteRepository.multiplantList(plantId)
    }

    fun updateMultiplant (req: MutableList<MultiPlantListData>): Flow<HttpResult<BaseBean>> {
        return remoteRepository.updateMultiplant(req)
    }
}