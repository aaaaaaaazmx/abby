package com.cl.common_base.net.interceptor

import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.report.Reporter
import com.cl.common_base.widget.toast.ToastUtil
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class LoggingInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
//        val request = chain.request()
//        val t1 = System.nanoTime()
////        logI("Sending request: ${request.url} \n ${request.headers}")
//
//        val response = chain.proceed(request)
//
//        val t2 = System.nanoTime()
//
//        val responseBody = response.body
//        val rBody: String
//        val source = responseBody!!.source()
//        source.request(java.lang.Long.MAX_VALUE)
//        val buffer = source.buffer()
//        var charset: Charset? = Charset.forName("UTF-8")
//        val contentType = responseBody.contentType()
//        contentType?.let {
//            try {
//                charset = contentType.charset(charset)
//            } catch (e: UnsupportedCharsetException) {
//                e.message?.let { it1 -> logE(it1) }
//            }
//        }
//        rBody = buffer.clone().readString(charset!!)
//
//        logI(
//            "Received response for  ${response.request.url} in ${(t2 - t1) / 1e6} ms\n${response.headers} \n response:${rBody}"
//        )
//        return response

        val UTF8 = Charset.forName("UTF-8")
        val request = chain.request()
        val requestBody = request.body
        var body: String? = null
        requestBody?.let {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            var charset: Charset? = UTF8
            val contentType = requestBody.contentType()
            contentType?.let {
                charset = contentType.charset(UTF8)
            }
            body = buffer.readString(charset!!)
        }

        logI(
            "发送请求: method：" + request.method
                    + "\nurl：" + request.url
                    + "\n请求头：" + request.headers
                    + "\n请求参数: " + body
        )


        val startNs = System.nanoTime()
        val response = chain.proceed(request)
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body
        val rBody: String

        val source = responseBody!!.source()
        source.request(java.lang.Long.MAX_VALUE)
        val buffer = source.buffer()

        var charset: Charset? = UTF8
        val contentType = responseBody.contentType()
        contentType?.let {
            try {
                charset = contentType.charset(UTF8)
            } catch (e: UnsupportedCharsetException) {
                e.message?.let { it1 -> logE(it1) }
            }
        }
        rBody = buffer.clone().readString(charset!!)

        logI(
            "收到响应: code:" + response.code
                    + "\n请求url：" + response.request.url
                    + "\n请求body：" + body
                    + "\nResponse: " + rBody
        )


        // 如果是code != 200 都是错误，需要上报
        thread {
            if (response.code != Constants.APP_SUCCESS) {
                val url = response.request.url.toString()
                Reporter.reportApiError(url = url, query = body, httpCode = response.code, bizCode = "", error = rBody)
            }
        }

        return response
    }

    companion object {
        const val TAG = "LoggingInterceptor"
    }
}