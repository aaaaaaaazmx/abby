package com.cl.modules_my.service

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.bean.DetailByLearnMoreIdData
import com.cl.common_base.bean.ListDeviceBean
import com.cl.modules_my.repository.AccessoryListBean
import com.cl.modules_my.repository.GetAutomationRuleBean
import com.cl.modules_my.repository.MyTroubleData
import com.cl.common_base.bean.OxygenCoinBillList
import com.cl.common_base.bean.AccountFlowingReq
import com.cl.modules_my.request.ConfiguationExecuteRuleReq
import com.cl.modules_my.request.MergeAccountReq
import com.cl.modules_my.request.ModifyUserDetailReq
import com.cl.modules_my.request.OpenAutomationReq
import com.cl.common_base.bean.OxygenCoinListBean
import com.cl.modules_my.request.DeviceDetailsBean
import com.cl.modules_my.request.ResetPwdReq
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * 登录注册忘记密码接口地址
 */
interface HttpMyApiService {
    /**
     * 上传图片
     */
    @Multipart //这里用Multipart,不添加的话会引起崩溃反应
    @POST("abby/base/uploadImg")
    fun uploadImg(
        @Part partLis: List<MultipartBody.Part>,
    ): Flow<HttpResult<String>>


    /**
     * 自动刷新token,更新token接口
     * /abby/user/app/automaticLogin
     */
    @POST("abby/user/app/automaticLogin")
    fun automaticLogin(
        @Body requestBody: AutomaticLoginReq,
    ): Flow<HttpResult<AutomaticLoginData>>

    /**
     * 更新用户信息
     */
    @POST("abby/user/modifyUserDetail")
    fun modifyUserDetail(@Body body: ModifyUserDetailReq): Flow<HttpResult<Boolean>>

    /**
     * 获取用户信息
     */
    @POST("abby/user/userDetail")
    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>>

    /**
     * 删除设备
     */
    @FormUrlEncoded
    @POST("abby/userDevice/delete")
    fun deleteDevice(@Field("deviceId") deviceId: String): Flow<HttpResult<BaseBean>>

    /**
     * 删除植物
     */
    @FormUrlEncoded
    @POST("abby/plant/delete")
    fun plantDelete(@Field("deviceUuid") deviceUuid: String): Flow<HttpResult<Boolean>>

    /**
     * 是否种植
     */
    @FormUrlEncoded
    @POST("abby/plant/check")
    fun checkPlant(@Field("deviceUuid") body: String? = ""): Flow<HttpResult<CheckPlantData>>

    /**
     * 检查App更新
     */
    @FormUrlEncoded
    @POST("abby/base/getAppVersion")
    fun getAppVersion(@Field("osType") osType: String = "1"): Flow<HttpResult<AppVersionData>>

    /**
     * 获取广告图文
     *  默认0 -> 换水
     */
    @FormUrlEncoded
    @POST("abby/advertising/advertising")
    fun advertising(@Field("type") type: String? = "0"): Flow<HttpResult<MutableList<AdvertisingData>>>

    /**
     * 种植引导图文获取
     */
    @FormUrlEncoded
    @POST("abby/guide/getGuideInfo")
    fun getGuideInfo(@Field("type") type: String): Flow<HttpResult<GuideInfoData>>

    /**
     * 获取疑问列表
     */
    @POST("abby/base/troubleShooting")
    fun troubleShooting(): Flow<HttpResult<MyTroubleData>>

    /**
     * 图文获取
     */
    @FormUrlEncoded
    @POST("abby/moments/getDetailByLearnMoreId")
    fun getDetailByLearnMoreId(@Field("learnMoreId") learnMoreId: String): Flow<HttpResult<DetailByLearnMoreIdData>>

    /**
     * HowTo
     */
    @POST("abby/base/howTo")
    fun howTo(): Flow<HttpResult<MutableList<MyTroubleData.Bean>>>

    /**
     * 修改植物信息
     */
    @POST("abby/plant/updatePlantInfo")
    fun updatePlantInfo(
        @Body body: UpPlantInfoReq
    ): Flow<HttpResult<BaseBean>>

    /**
     * 夜间模式
     */
    @POST("abby/userDevice/updateDeviceInfo")
    fun updateDeviceInfo(
        @Body body: UpDeviceInfoReq
    ): Flow<HttpResult<BaseBean>>

    /**
     * 添加帐篷
     */
    @POST("abby/userDevice/addDevice")
    fun addDevice(
        @Body body: DeviceDetailsBean
    ): Flow<HttpResult<BaseBean>>


    /**
     * 获取植物的基本信息
     */
    @POST("abby/plant/plantInfo")
    fun plantInfo(): Flow<HttpResult<PlantInfoData>>

    @FormUrlEncoded
    @POST("abby/calendar/getCalendar")
    fun getCalendar(
        @Field("startDate") startDate: String,
        @Field("endDate") endDate: String
    ): Flow<HttpResult<MutableList<CalendarData>>>

    /**
     * 更新任务
     */
    @POST("abby/calendar/updateTask")
    fun updateTask(@Body body: UpdateReq): Flow<HttpResult<String>>

    /**
     * 解锁花期
     */
    @FormUrlEncoded
    @POST("abby/plant/unlockJourney")
    fun unlockJourney(
        @Field("journeyName") journeyName: String,
        @Field("weight") weight: String? = null
    ): Flow<HttpResult<BaseBean>>

    /**
     * 日历-完成任务
     */
    @POST("abby/calendar/finishTask")
    fun finishTask(
        @Body boyd: FinishTaskReq
    ): Flow<HttpResult<String>>


    /**
     * 设备操作开始
     */
    @FormUrlEncoded
    @POST("abby/deviceOperate/start")
    fun deviceOperateStart(
        @Field("businessId") businessId: String,
        @Field("type") type: String
    ): Flow<HttpResult<BaseBean>>


    /**
     * 设备操作完成
     */
    @FormUrlEncoded
    @POST("abby/deviceOperate/finish")
    fun deviceOperateFinish(@Field("type") type: String): Flow<HttpResult<BaseBean>>

    /**
     * 订阅码检查
     */
    @FormUrlEncoded
    @POST("abby/user/checkSubscriberNumber")
    fun checkSubscriberNumber(@Field("subscriberNumber") subscriberNumber: String): Flow<HttpResult<CheckSubscriberNumberBean>>


    /**
     * 充值订阅
     */
    @FormUrlEncoded
    @POST("abby/user/topUpSubscriberNumber")
    fun topUpSubscriberNumber(@Field("subscriberNumber") subscriberNumber: String): Flow<HttpResult<Boolean>>


    /**
     * 充值订阅
     */
    @POST("abby/plant/giveUpCheck")
    fun giveUpCheck(): Flow<HttpResult<GiveUpCheckData>>

    /**
     * 发送验证码
     */
    @GET("abby/user/verify/email")
    fun verifyEmail(
        @Query("email") email: String,
        @Query("type") type: String,
        @Query("mergeEmail") mergeEmail: String? = null
    ): Flow<HttpResult<Boolean>>


    @POST("abby/user/merge")
    fun mergeAccount(@Body req: MergeAccountReq): Flow<HttpResult<String>>

    @POST("abby/userDevice/listDevice")
    fun listDevice(): Flow<HttpResult<MutableList<ListDeviceBean>>>

    @FormUrlEncoded
    @POST("abby/userDevice/switchDevice")
    fun switchDevice(@Field("deviceId") deviceId: String): Flow<HttpResult<String>>


    /**
     * 邮箱验证码
     */
    @GET("abby/user/verify/code")
    fun verifyCode(
        @Query("code") code: String,
        @Query("email") email: String,
        @Query("mergeEmail") mergeEmail: String? = null
    ): Flow<HttpResult<Boolean>>

    /**
     * 配件列表
     */
    @FormUrlEncoded
    @POST("abby/accessory/list")
    fun accessoryList(@Field("spaceType")spaceType: String): Flow<HttpResult<MutableList<AccessoryListBean>>>

    /**
     * 配件规则列表
     */
    @FormUrlEncoded
    @POST("abby/accessory/automationList")
    fun automationList(
        @Field("accessoryId") accessoryId: String,
        @Field("deviceId") deviceId: String
    ): Flow<HttpResult<AutomationListBean>>

    /**
     * 配件状态开关
     */
    @FormUrlEncoded
    @POST("abby/accessory/statusSwitch")
    fun statusSwitch(
        @Field("accessoryId") accessoryId: String,
        @Field("deviceId") deviceId: String,
        @Field("status") status: String
    ): Flow<HttpResult<BaseBean>>

    /**
     * 自动化规则开关
     */
    @POST("abby/accessory/openAutomation")
    fun openAutomation(@Body req: OpenAutomationReq): Flow<HttpResult<BaseBean>>

    /**
     * 获取自动化信息
     */
    @FormUrlEncoded
    @POST("abby/accessory/getAutomationRule")
    fun getAutomationRule(@Field("automationId") automationId: String?, @Field("accessoryId") accessoryId: String?): Flow<HttpResult<GetAutomationRuleBean>>

    /**
     * 删除自动化
     */
    @FormUrlEncoded
    @POST("abby/accessory/deleteAutomation")
    fun deleteAutomation(@Field("automationId") automationId: String): Flow<HttpResult<BaseBean>>

    /**
     * 配置自动执行规划
     */
    @POST("abby/accessory/configuationExecuteRule")
    fun configuationExecuteRule(@Body req: ConfiguationExecuteRuleReq): Flow<HttpResult<BaseBean>>


    /**
     * 获取InterCome同步信息
     */
    @POST("abby/user/intercomDataAttributeSync")
    fun intercomDataAttributeSync(): Flow<HttpResult<Map<String, Any>>>

    /**
     * 获取壁纸列表
     */
    @POST("abby/user/getWallList")
    fun wallpaperList(): Flow<HttpResult<MutableList<WallpaperListBean>>>

    /**
     * 获取氧气币列表
     */
    @POST("abby/oxygenGrant/getGrantOxygenList")
    fun oxygenCoinList(): Flow<HttpResult<MutableList<OxygenCoinListBean>>>

    /**
     * 获取氧气币账单流水
     */
    @POST("abby/account/getAccountFlowing")
    fun oxygenCoinBillList(@Body req: AccountFlowingReq): Flow<HttpResult<OxygenCoinBillList>>

    /**
     * 修改密码
     */
    @POST("abby/user/resetPwd")
    fun resetPwd(
       @Body req: ResetPwdReq
    ): Flow<HttpResult<BaseBean>>


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
    fun getAccessoryInfo(@Field("deviceId") deviceId: String): Flow<HttpResult<UpdateInfoReq>>

    /**
     * 帐篷设备的设备详情
     */
    @FormUrlEncoded
    @POST("abby/userDevice/getDeviceDetails")
    fun getDeviceDetails(@Field("deviceId") deviceId: String): Flow<HttpResult<DeviceDetailsBean>>

    /**
     * 获取名字
     */
    @FormUrlEncoded
    @POST("abby/plant/getStrainName")
    fun getStrainName(
        @Field("strainName") strainName: String
    ): Flow<HttpResult<MutableList<String>>>
}