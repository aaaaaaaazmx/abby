package com.cl.modules_contact.service

import com.cl.common_base.bean.*
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.response.NewPageData
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
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
}