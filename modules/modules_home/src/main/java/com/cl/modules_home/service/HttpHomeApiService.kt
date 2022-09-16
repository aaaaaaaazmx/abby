package com.cl.modules_home.service

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.modules_home.request.AutomaticLoginReq
import com.cl.modules_home.response.AutomaticLoginData
import com.cl.common_base.bean.GuideInfoData
import com.cl.modules_home.response.PlantInfoData
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
     * 种植引导图文获取
     */
    @FormUrlEncoded
    @POST("abby/guide/getGuideInfo")
    fun getGuideInfo(@Field("type") type: String): Flow<HttpResult<GuideInfoData>>

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
    @FormUrlEncoded
    @POST("abby/plant/environmentInfo")
    fun environmentInfo(@Field("deviceId") deviceId: String): Flow<HttpResult<MutableList<EnvironmentInfoData>>>

    /**
     * 获取未读信息
     */
    @POST("abby/userMessage/getUnread")
    fun getUnread(): Flow<HttpResult<MutableList<UnreadMessageData>>>

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
    fun checkPlant(@Field("deviceUuid") body: String): Flow<HttpResult<CheckPlantData>>

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
    fun start(): Flow<HttpResult<BaseBean>>

}