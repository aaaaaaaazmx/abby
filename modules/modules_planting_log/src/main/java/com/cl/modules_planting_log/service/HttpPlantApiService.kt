package com.cl.modules_planting_log.service

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.CheckPlantData
import com.cl.common_base.bean.HttpResult
import com.cl.common_base.bean.PlantInfoData
import com.cl.common_base.bean.SyncDeviceInfoReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.modules_planting_log.request.LogByIdData
import com.cl.modules_planting_log.request.LogListDataItem
import com.cl.modules_planting_log.request.LogListReq
import com.cl.modules_planting_log.request.LogSaveOrUpdateReq
import com.cl.modules_planting_log.request.LogTypeListDataItem
import com.cl.modules_planting_log.request.PlantIdByDeviceIdData
import com.cl.modules_planting_log.request.PlantInfoByPlantIdData
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * Pair 配网接口
 *
 * @author 李志军 2022-08-04 10:27
 */
interface HttpPlantApiService {

    /**
     * 获取植物的基本信息
     */
    @POST("abby/plant/plantInfo")
    fun plantInfo(): Flow<HttpResult<PlantInfoData>>

    @FormUrlEncoded
    @POST("abby/userDevice/bindDevice")
    fun bindDevice(
        @Field("deviceId") deviceId: String,
        @Field("deviceUuid") deviceUuid: String
    ): Flow<HttpResult<String>>

    /**
     * 是否种植
     */
    @FormUrlEncoded
    @POST("abby/plant/check")
    fun checkPlant(@Field("deviceUuid") body: String? = ""): Flow<HttpResult<CheckPlantData>>

    /**
     * 获取用户信息
     */
    @POST("abby/user/userDetail")
    fun userDetail(): Flow<HttpResult<UserinfoBean.BasicUserBean>>


    @FormUrlEncoded
    @POST("abby/userDevice/checkSN")
    fun checkSN(@Field("deviceSn") deviceSn: String): Flow<HttpResult<BaseBean>>

    /**
     * 设备信息同步
     */
    @POST("abby/userDevice/syncDeviceInfo")
    fun syncDeviceInfo(@Body req: SyncDeviceInfoReq): Flow<HttpResult<BaseBean>>


    /**
     * 获取日志详情
     */
    @FormUrlEncoded
    @POST("abby/log/getLogById")
    fun getLogById(@Field("logId") logId: String?): Flow<HttpResult<LogSaveOrUpdateReq>>

    /**
     * 获取日志类型列表
     */
    @FormUrlEncoded
    @POST("abby/log/getLogTypeList")
    fun getLogTypeList(@Field("showType") showType: String, @Field("logId") logId: String?): Flow<HttpResult<List<LogTypeListDataItem>>>


    /**
     * 根据设备ID获取所有植物ID
     */
    @FormUrlEncoded
    @POST("abby/log/getPlantIdByDeviceId")
    fun getPlantIdByDeviceId(@Field("deviceId") deviceId: String): Flow<HttpResult<MutableList<PlantIdByDeviceIdData>>>


    /**
     * 根据植物ID获取植物信息
     */
    @FormUrlEncoded
    @POST("abby/log/getPlantInfoByPlantId")
    fun getPlantInfoByPlantId(@Field("plantId") plantId: Int): Flow<HttpResult<PlantInfoByPlantIdData>>


    /**
     * 获取日志列表
     */
    @POST("abby/log/list")
    fun getLogList(@Body body: LogListReq): Flow<HttpResult<MutableList<LogListDataItem>>>


    @POST("abby/log/logSaveOrUpdate")
    fun logSaveOrUpdate(@Body body: LogSaveOrUpdateReq): Flow<HttpResult<Boolean>>

    /**
     * 上传图片多张
     */
    @Multipart
    @POST("abby/base/uploadImgs")
    fun uploadImages(@Part partLis: List<MultipartBody.Part>): Flow<HttpResult<MutableList<String>>>


    /**
     * 删除tips卡片，一天只显示一次
     */
    @FormUrlEncoded
    @POST("abby/log/closeTips")
    fun closeTips(@Field("period") period: String, @Field("plantId") plantId: String): Flow<HttpResult<Boolean>>

}