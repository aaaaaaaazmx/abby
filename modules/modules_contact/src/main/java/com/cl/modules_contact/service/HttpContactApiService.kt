package com.cl.modules_contact.service

import com.cl.common_base.BaseBean
import com.cl.common_base.bean.HttpResult
import com.cl.modules_contact.request.DeleteReq
import com.cl.modules_contact.request.LikeReq
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.request.ReportReq
import com.cl.modules_contact.request.SyncTrendReq
import com.cl.modules_contact.response.NewPageData
import kotlinx.coroutines.flow.Flow
import retrofit2.http.*

/**
 * 登录注册忘记密码接口地址
 */
interface HttpContactApiService {

    // 获取最新动态
    @POST("abby/moments/newPage")
    fun newPage(
        @Body requestBody: NewPageReq,
    ): Flow<HttpResult<NewPageData>>

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
     * 删除动态
     */
    @POST("abby/moments/delete")
    fun delete(
        @Body requestBody: DeleteReq,
    ): Flow<HttpResult<BaseBean>>

    /**
     * 公开动态
     */
    @POST("abby/moments/syncTrend")
    fun public(
        @Body requestBody: SyncTrendReq,
    ): Flow<HttpResult<BaseBean>>

    /**
     * 举报动态
     */
    @POST("abby/moments/reportAdd")
    fun report(
        @Body requestBody: ReportReq,
    ): Flow<HttpResult<BaseBean>>
}