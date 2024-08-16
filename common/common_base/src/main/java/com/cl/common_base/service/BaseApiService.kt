package com.cl.common_base.service

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
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
        @Query("taskId") taskId: String? = null,
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
     * 保存camera的信息
     * 修改配件，绑定是否没绑定
     */
    @POST("abby/accessory/updateInfo")
    fun updateInfo(@Body body: UpdateInfoReq): Flow<HttpResult<BaseBean>>

    /**
     * 删除设备
     */
    @FormUrlEncoded
    @POST("abby/userDevice/delete")
    fun deleteDevice(@Field("deviceId") deviceId: String): Flow<HttpResult<BaseBean>>

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
    fun checkPlant(@Field("deviceUuid") body: String? = ""): Flow<HttpResult<CheckPlantData>>

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
        @Field("usbPort") usbId: String? = null,
    ): Flow<HttpResult<AccessoryAddData>>

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

    /**
     * 延迟任务
     */
    @POST("abby/calendar/snooze")
    fun snooze(@Body requestBody: SnoozeReq): Flow<HttpResult<BaseBean>>


    /**
     * 提前解锁下一个周期
     */
    @FormUrlEncoded
    @POST("abby/plant/unlockNow")
    fun unlockNow(@Field("plantId")deviceId: String): Flow<HttpResult<BaseBean>>

    /**
     * 获取DisCord的token连接
     */
    @GET("abby/discord/authorizeLink")
    fun authorizeLink(): Flow<HttpResult<String>>

    /**
     * 是否绑定成功
     */
    @GET("abby/discord/isBind")
    fun isBind(): Flow<HttpResult<Boolean>>

    /**
     * 获得勋章时的弹窗
     */
    @POST("abby/digitalAsset/popupList")
    fun popupList(): Flow<HttpResult<MutableList<MedalPopData>>>

    /**
     * 保存展示frames
     */
    @FormUrlEncoded
    @POST("abby/digitalAsset/showFrame")
    fun showFrame(@Field("frameId") frameId: Int): Flow<HttpResult<BaseBean>>

    /**
     * 保存展示成就
     */
    @FormUrlEncoded
    @POST("abby/digitalAsset/showAchievement")
    fun showAchievement(@Field("achievementId") achievementId: Int): Flow<HttpResult<BaseBean>>

    // 删除proMode预设模版
    @FormUrlEncoded
    @POST("abby/plant/deleteProModeRecord")
    fun deleteProModeRecord(@Field("id") id: String): Flow<HttpResult<BaseBean>>

    /**
     * 推送消息设置
     */
    @POST("abby/userMessage/messageConfig")
    fun messageConfig(@Body baseBean: MessageConfigBean) : Flow<HttpResult<BaseBean>>

    /**
     * 获取消息配置
     */
    @POST("abby/userMessage/messageConfigList")
    fun messageConfigList() : Flow<HttpResult<MessageConfigBean>>

    /**
     * 获取控件信息
     */
    @POST("abby/plant/controlInfo")
    fun controlInfo() : Flow<HttpResult<ControlInfoBean>>

    /**
     * 获取周期列表
     */
    @FormUrlEncoded
    @POST("abby/plant/getPeriodList")
    fun getPeriodList(@Field("plantId")plantId: String): Flow<HttpResult<MutableList<String>>>

    /**
     * 修改植物信息
     */
    @POST("abby/plant/updatePlantInfo")
    fun updatePlantInfo(
        @Body body: UpPlantInfoReq
    ): Flow<HttpResult<BaseBean>>

    // 开启主动发话服务
    @FormUrlEncoded
    @POST("abby/plant/conversations")
    fun conversations(@Field("taskNo")taskNo: String? = null, @Field("textId")textId: String? = null): Flow<HttpResult<ConversationsBean>>

    /**
     * 上传图片多张
     */
    @Multipart
    @POST("abby/base/uploadImgs")
    fun uploadImages(@Part partLis: List<MultipartBody.Part>): Flow<HttpResult<MutableList<String>>>

    /**
     * ai 周期检查
     * plant/checkPeriod
     */
    @FormUrlEncoded
    @POST("abby/plant/checkPeriod")
    fun aiCheckPeriod(@Field("plantId") plantId: String, @Field("url")url: String): Flow<HttpResult<AiCheckBean>>

}