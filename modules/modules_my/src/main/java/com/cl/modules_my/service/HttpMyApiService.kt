package com.cl.modules_my.service

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.bean.DetailByLearnMoreIdData
import com.cl.modules_my.repository.MyTroubleData
import com.cl.modules_my.request.ModifyUserDetailReq
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
    @POST("abby/userDevice/delete")
    fun deleteDevice(): Flow<HttpResult<BaseBean>>

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
    fun checkPlant(@Field("deviceUuid") body: String): Flow<HttpResult<CheckPlantData>>

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
    fun deviceOperateStart(@Field("businessId") businessId: String, @Field("type") type: String): Flow<HttpResult<BaseBean>>


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
}