package com.cl.modules_home.service

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.bean.AutomaticLoginReq
import com.cl.common_base.bean.AutomaticLoginData
import com.cl.common_base.bean.GuideInfoData
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.bean.UpdateInfoReq
import kotlinx.coroutines.flow.Flow
import retrofit2.http.*

/**
 * 登录注册忘记密码接口地址
 */
interface HttpHomeApiService {
    /**
     * 自动刷新token,更新token接口
     * /abby/user/app/automaticLogin
     */
    @POST("abby/user/app/automaticLogin")
    fun automaticLogin(
        @Body requestBody: AutomaticLoginReq,
    ): Flow<HttpResult<AutomaticLoginData>>

    /**
     * 夜间模式
     */
    @POST("abby/userDevice/updateDeviceInfo")
    fun updateDeviceInfo(
        @Body body: UpDeviceInfoReq
    ): Flow<HttpResult<BaseBean>>

    /**
     * 种植引导图文获取
     */
    @FormUrlEncoded
    @POST("abby/guide/getGuideInfo")
    fun getGuideInfo(@Field("type") type: String): Flow<HttpResult<GuideInfoData>>

    /**
     * 更新用户信息
     */
    @POST("abby/user/modifyUserDetail")
    fun modifyUserDetail(@Body body: ModifyUserDetailReq): Flow<HttpResult<Boolean>>

    /**
     * 上报引导标记
     */
    @FormUrlEncoded
    @POST("abby/user/saveOrUpdate")
    fun saveOrUpdate(@Field("guideNo") guideNo: String): Flow<HttpResult<BaseBean>>

    /**
     * 开始种植
     */
    @FormUrlEncoded
    @POST("abby/plant/startRuning")
    fun startRunning(
        @Field("botanyId") botanyId: String?,
        @Field("goon") goon: Boolean
    ): Flow<HttpResult<Boolean>>

    /**
     * 获取植物的基本信息
     */
    @POST("abby/plant/plantInfo")
    fun plantInfo(): Flow<HttpResult<PlantInfoData>>

    /**
     * 获取广告图文
     *  默认0 -> 换水
     */
    @FormUrlEncoded
    @POST("abby/advertising/advertising")
    fun advertising(@Field("type") type: String): Flow<HttpResult<MutableList<AdvertisingData>>>

    /**
     * 获取环境信息
     *  默认0 -> 换水
     */
    @POST("abby/plant/environment")
    fun environmentInfo(@Body dev: EnvironmentInfoReq): Flow<HttpResult<EnvironmentInfoData>>

    /**
     * 获取未读信息
     */
    @POST("abby/userMessage/getUnread")
    fun getUnread(): Flow<HttpResult<MutableList<UnreadMessageData>>>

    /**
     * 获取用户信息
     */
    @POST("abby/user/userDetail")
    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>>

    /**
     * 获取已读信息
     */
    @FormUrlEncoded
    @POST("abby/userMessage/read")
    fun getRead(@Field("messageId") messageId: String): Flow<HttpResult<BaseBean>>

    /**
     * 解锁花期
     */
    @FormUrlEncoded
    @POST("abby/plant/unlockJourney")
    fun unlockJourney(@Field("journeyName") journeyName: String, @Field("weight") weight: String? = null): Flow<HttpResult<BaseBean>>

    /**
     * AppVersionData 更新
     */
    @FormUrlEncoded
    @POST("abby/base/getAppVersion")
    fun getAppVersion(@Field("osType") osType: String = "1"): Flow<HttpResult<AppVersionData>>

    /**
     * 标记换水的步骤
     */
    @FormUrlEncoded
    @POST("abby/userMessage/flag")
    fun userMessageFlag(@Field("flag")flag: String, @Field("messageId")messageId: String): Flow<HttpResult<BaseBean>>

    /**
     * 设备操作开始
     */
    @FormUrlEncoded
    @POST("abby/deviceOperate/start")
    fun deviceOperateStart(@Field("businessId")businessId: String, @Field("type")type: String): Flow<HttpResult<BaseBean>>

    /**
     * 设备操作完成
     */
    @FormUrlEncoded
    @POST("abby/deviceOperate/finish")
    fun deviceOperateFinish(@Field("type")type: String): Flow<HttpResult<BaseBean>>

    /**
     * 获取完成界面配置参数
     */
    @POST("abby/plant/getFinishPage")
    fun getFinishPage(): Flow<HttpResult<FinishPageData>>

    /**
     * 图文获取
     */
    @FormUrlEncoded
    @POST("abby/moments/getDetailByLearnMoreId")
    fun getDetailByLearnMoreId(@Field("learnMoreId") learnMoreId: String):Flow<HttpResult<DetailByLearnMoreIdData>>

    /**
     * 删除植物
     */
    @FormUrlEncoded
    @POST("abby/plant/delete")
    fun plantDelete(@Field("deviceUuid")deviceUuid: String): Flow<HttpResult<Boolean>>

    /**
     * 是否种植
     */
    @FormUrlEncoded
    @POST("abby/plant/check")
    fun checkPlant(@Field("deviceUuid") body: String? = ""): Flow<HttpResult<CheckPlantData>>

    @FormUrlEncoded
    @POST("abby/plant/plantFinish")
    fun plantFinish(@Field("botanyId") botanyId: String): Flow<HttpResult<BaseBean>>

    /**
     * 图文获取
     */
    @FormUrlEncoded
    @POST("abby/userMessage/getMessageDetail")
    fun getMessageDetail(@Field("messageId") messageId: String): Flow<HttpResult<DetailByLearnMoreIdData>>

    /**
     * 新的开始种植
     */
    @POST("abby/plant/start")
    fun start(): Flow<HttpResult<String>>


    /**
     * 日历-完成任务
     */
    @POST("abby/calendar/finishTask")
    fun finishTask(
      @Body body: FinishTaskReq
    ): Flow<HttpResult<String>>

    @POST("abby/calendar/updateTask")
    fun updateTask(@Body body: UpdateReq): Flow<HttpResult<String>>

    @POST("abby/academy/getAcademyList")
    fun getAcademyList(): Flow<HttpResult<MutableList<AcademyListData>>>


    @FormUrlEncoded
    @POST("abby/academy/getAcademyDetails")
    fun getAcademyDetails(@Field("academyId") academyId: String): Flow<HttpResult<MutableList<AcademyDetails>>>

    /**
     * 已读学院消息
     */
    @FormUrlEncoded
    @POST("abby/academy/read")
    fun messageRead(@Field("academyDetailsId") academyDetailsId: String): Flow<HttpResult<BaseBean>>

    /**
     * 消息统计
     */
    @POST("abby/base/homePage")
    fun getHomePageNumber(): Flow<HttpResult<HomePageNumberData>>

    /**
     * 开启订阅
     */
    @POST("abby/user/startSubscriber")
    fun startSubscriber(): Flow<HttpResult<BaseBean>>

    /**
     * 开启订阅
     */
    @POST("abby/user/checkFirstSubscriber")
    fun checkFirstSubscriber(): Flow<HttpResult<Boolean>>


    /**
     * 检查是否需要补偿订阅
     */
    @POST("abby/user/whetherSubCompensation")
    fun whetherSubCompensation(): Flow<HttpResult<WhetherSubCompensationData>>

    /**
     * 补偿订阅
     */
    @POST("abby/user/compensatedSubscriber")
    fun compensatedSubscriber(): Flow<HttpResult<BaseBean>>

    /**
     * 设备列表
     */
    @POST("abby/userDevice/listDevice")
    fun listDevice(): Flow<HttpResult<MutableList<ListDeviceBean>>>

    @FormUrlEncoded
    @POST("abby/userDevice/switchDevice")
    fun switchDevice(@Field("deviceId") deviceId: String): Flow<HttpResult<String>>

    /**
     * 修改植物信息
     */
    @POST("abby/plant/updatePlantInfo")
    fun updatePlantInfo(
        @Body body: UpPlantInfoReq
    ): Flow<HttpResult<BaseBean>>

    @POST("abby/plant/skipGerminate")
    fun skipGerminate(): Flow<HttpResult<BaseBean>>


    @POST("abby/plant/intoPlantBasket")
    fun intoPlantBasket(): Flow<HttpResult<BaseBean>>

    /**
     * 获取InterCome同步信息
     */
    @POST("abby/user/intercomDataAttributeSync")
    fun intercomDataAttributeSync(): Flow<HttpResult<Map<String, Any>>>

    /**
     * 保存camera的信息
     */
    @POST("abby/accessory/updateInfo")
    fun updateInfo(@Body body: UpdateInfoReq): Flow<HttpResult<BaseBean>>


    /**
     * 获取配件信息
     */
    @FormUrlEncoded
    @POST("abby/accessory/getAccessoryInfo")
    fun getAccessoryInfo(@Field("deviceId") deviceId: String, @Field("accessoryDeviceId") accessoryDeviceId: String): Flow<HttpResult<UpdateInfoReq>>

    /**
     * 获取氧气币列表
     */
    @POST("abby/oxygenGrant/getGrantOxygenList")
    fun oxygenCoinList(): Flow<HttpResult<MutableList<OxygenCoinListBean>>>

    /**
     * 领取氧气币
     */
    @FormUrlEncoded
    @POST("abby/oxygenGrant/getGrantOxygen")
    fun getGrantOxygen(@Field("orderNo") orderNo: String): Flow<HttpResult<BaseBean>>

    /**
     * 获取氧气币账单流水
     */
    @POST("abby/account/getAccountFlowing")
    fun oxygenCoinBillList(@Body req: AccountFlowingReq): Flow<HttpResult<OxygenCoinBillList>>

    /**
     * 灯光参数同步
     */
    @FormUrlEncoded
    @POST("abby/plant/syncLightParam")
    fun syncLightParam(@Field("deviceId")deviceId: String): Flow<HttpResult<BaseBean>>


    /**
     * 提前解锁下一个周期
     */
    @FormUrlEncoded
    @POST("abby/plant/unlockNow")
    fun unlockNow(@Field("plantId")deviceId: String): Flow<HttpResult<BaseBean>>


    /**
     * 获取滴灌参数设置
     */
    @FormUrlEncoded
    @POST("abby/accessory/getTrickleIrrigationConfig")
    fun getTrickleIrrigationConfig(@Field("deviceId")deviceId: String): Flow<HttpResult<TrickData>>

    /**
     * 滴灌参数设置
     */
    @POST("abby/accessory/trickleIrrigationConfig")
    fun trickleIrrigationConfig(@Body req: TrickData): Flow<HttpResult<BaseBean>>

    /**
     * 获得勋章时的弹窗
     */
    @POST("abby/digitalAsset/popupList")
    fun popupList(): Flow<HttpResult<MutableList<MedalPopData>>>

    /**
     * 新增Pormode预设模板
     */
    @POST("abby/plant/addProModeRecord")
    fun addProModeRecord(@Body req: ProModeInfoBean): Flow<HttpResult<BaseBean>>

    /**
     * 修改Pormode预设模板
     */
    @POST("abby/plant/updateProModeRecord")
    fun updateProModeRecord(@Body req: ProModeInfoBean): Flow<HttpResult<BaseBean>>

    /**
     * 根据设备ID获取Pormode预设模板
     */
    @FormUrlEncoded
    @POST("abby/plant/getProModeByDeviceId")
    fun getProModeByDeviceId(@Field("deviceId") deviceId: String): Flow<HttpResult<MutableList<ProModeInfoBean>>>

    // 新增proMode信息
    @POST("abby/plant/addProModeInfo")
    fun addProModeInfo(@Body req: ProModeInfoBean): Flow<HttpResult<BaseBean>>

    // 获取当前proMode信息
    @FormUrlEncoded
    @POST("abby/plant/getProModeInfo")
    fun getProModeInfo(@Field("deviceId") deviceId: String): Flow<HttpResult<ProModeInfoBean>>


    /**
     * 获取种植数据
     */
    @FormUrlEncoded
    @POST("abby/plant/getPlantData")
    fun getPlantData(@Field("plantId") plantId: String): Flow<HttpResult<PlantData>>

    /**
     * 根据设备ID获取所有植物ID
     */
    @FormUrlEncoded
    @POST("abby/log/getPlantIdByDeviceId")
    fun getPlantIdByDeviceId(@Field("deviceId") deviceId: String): Flow<HttpResult<MutableList<PlantIdByDeviceIdData>>>
}