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
    @Multipart//这里用Multipart,不添加的话会引起崩溃反应
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
    fun plantDelete(@Field("deviceUuid")deviceUuid: String): Flow<HttpResult<Boolean>>

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
     * 获取疑问列表
     */
    @POST("abby/base/troubleShooting")
    fun troubleShooting(): Flow<HttpResult<MyTroubleData>>

    /**
     * 图文获取
     */
    @FormUrlEncoded
    @POST("abby/moments/getDetailByLearnMoreId")
    fun getDetailByLearnMoreId(@Field("learnMoreId") learnMoreId: String):Flow<HttpResult<DetailByLearnMoreIdData>>

    /**
     * HowTo
     */
    @POST("abby/base/howTo")
    fun howTo() :Flow<HttpResult<MutableList<MyTroubleData.Bean>>>
}