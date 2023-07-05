package com.cl.common_base.service

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import kotlinx.coroutines.flow.Flow
import retrofit2.http.*

interface BaseApiService {
    /**
     * 获取名字
     */
    @FormUrlEncoded
    @POST("abby/plant/getStrainName")
    fun getStrainName(
        @Field("strainName") strainName: String
    ): Flow<HttpResult<MutableList<String>>>

    @FormUrlEncoded
    @POST("abby/deviceOperate/start")
    fun deviceOperateStart(
        @Field("businessId") businessId: String,
        @Field("type") type: String
    ): Flow<HttpResult<BaseBean>>

    /**
     * 富文本图文接口 统一
     */
    @GET("abby/richText/getRichText")
    fun getRichText(
        @Query("txtId") txtId: String? = null,
        @Query("txtType") txtType: String? = null
    ): Flow<HttpResult<RichTextData>>

    /**
     * 日历-完成任务
     */
    @POST("abby/calendar/finishTask")
    fun finishTask(
        @Body body: FinishTaskReq
    ): Flow<HttpResult<String>>

    /**
     * 开始种植
     */
    @POST("abby/plant/startRuning")
    fun startRunning(): Flow<HttpResult<Boolean>>

    /**
     * 是否种植
     */
    @FormUrlEncoded
    @POST("abby/plant/check")
    fun checkPlant(@Field("deviceUuid") body: String): Flow<HttpResult<CheckPlantData>>

    @POST("abby/plant/intoPlantBasket")
    fun intoPlantBasket(): Flow<HttpResult<BaseBean>>

    /**
     * 夜间模式
     */
    @POST("abby/userDevice/updateDeviceInfo")
    fun updateDeviceInfo(
        @Body body: UpDeviceInfoReq
    ): Flow<HttpResult<BaseBean>>

    /**
     * 新增配件接口
     */
    @FormUrlEncoded
    @POST("abby/accessory/add")
    fun accessoryAdd(
        @Field("accessoryId") accessoryId: String,
        @Field("deviceId") deviceId: String,
        @Field("accessoryDeviceId")accessoryDeviceId: String? = null,
    ): Flow<HttpResult<BaseBean>>

    /**
     * 获取广告图文
     *  默认0 -> 换水
     */
    @FormUrlEncoded
    @POST("abby/advertising/advertising")
    fun advertising(@Field("type") type: String? = "0", @Field("current") current: Int, @Field("size") size: Int): Flow<HttpResult<MutableList<AdvertisingData>>>

    /**
     * 点赞
     */
    @POST("abby/moments/like")
    fun like(
        @Body requestBody: LikeReq,
    ): Flow<HttpResult<BaseBean>>

    /**
     * 取消点赞
     */
    @POST("abby/moments/dislike")
    fun unlike(
        @Body requestBody: LikeReq,
    ): Flow<HttpResult<BaseBean>>


    /**
     * 打赏
     */
    @POST("abby/moments/reward")
    fun reward(@Body requestBody: RewardReq): Flow<HttpResult<BaseBean>>
}