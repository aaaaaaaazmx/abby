package com.cl.common_base.net.download.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 *  description: http api
 */
interface DownloadApiService {


    @Streaming //添加这个注解用来下载大文件
    @GET
    suspend fun downloadFile(@Url fileUrl: String): ResponseBody
}