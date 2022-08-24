package com.cl.common_base.net.interceptor

import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException

class LoggingInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val t1 = System.nanoTime()
//        logI("Sending request: ${request.url} \n ${request.headers}")

        val response = chain.proceed(request)

        val t2 = System.nanoTime()

        val responseBody = response.body
        val rBody: String
        val source = responseBody!!.source()
        source.request(java.lang.Long.MAX_VALUE)
        val buffer = source.buffer()
        var charset: Charset? = Charset.forName("UTF-8")
        val contentType = responseBody.contentType()
        contentType?.let {
            try {
                charset = contentType.charset(charset)
            } catch (e: UnsupportedCharsetException) {
                e.message?.let { it1 -> logE(it1) }
            }
        }
        rBody = buffer.clone().readString(charset!!)

        logI(
            "Received response for  ${response.request.url} in ${(t2 - t1) / 1e6} ms\n${response.headers} \n response:${rBody}"
        )
        return response
    }

    companion object {
        const val TAG = "LoggingInterceptor"
    }
}