package com.cl.common_base.net.interceptor

import android.os.Build
import android.text.PrecomputedText.Params
import androidx.annotation.RequiresApi
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.HmacMD5Util
import com.cl.common_base.util.JwtParser
import com.cl.common_base.util.Prefs
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class AddHeadInterceptor : Interceptor {
    /*@RequiresApi(Build.VERSION_CODES.CUPCAKE)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val request: Request = original.newBuilder()


            .build()
        *//*builder.addHeader("Cookie", "设置的自定义的值")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("X-Requested-With", "XMLHttpRequest");*//*

        *//*builder.addHeader("Cookie", "设置的自定义的值")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("X-Requested-With", "XMLHttpRequest");*//*
        return chain.proceed(request)
    }*/

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalHttpUrl = originalRequest.url

        val timestamp = System.currentTimeMillis() / 1000
        val organization = "heyabby"

        // Create the signature plaintext based on the content type and parameters
        val signaturePlaintext = buildSignaturePlaintext(originalRequest, timestamp, organization)
        // Encrypt the signature plaintext using HmacMD5 and convert to uppercase
        val signature = HmacMD5Util.creatSign(signaturePlaintext, "8E)mujI2")
        logI(
            """
            signaturePlaintext ====> $signaturePlaintext 
            url ====>                $originalHttpUrl
            signature ====>          $signature
        """.trimIndent()
        )

        // Create the new request with the updated URL and additional headers
        val newRequest = originalRequest.newBuilder()
            .url(originalHttpUrl)
            .addHeader("sign", signature)
            .addHeader("version", AppUtil.appVersionName)
            .addHeader("timestamp", timestamp.toString())
            .header("token", ServiceCreators.TokenCache.token ?: Prefs.getString(Constants.Login.KEY_LOGIN_DATA_TOKEN))
            //.addHeader("Connection", "close")
            .method(originalRequest.method, originalRequest.body)
            .build()

        return chain.proceed(newRequest)
    }

    private fun buildSignaturePlaintext(request: Request, timestamp: Long, organization: String): String {
        val token = ServiceCreators.TokenCache.token ?: Prefs.getString(Constants.Login.KEY_LOGIN_DATA_TOKEN)
        return if (token.isEmpty()) {
            "uri=${getRequestUrl(request)}&timestamp=$timestamp&organization=$organization"
        } else {
            val userId = JwtParser.parser(token)
            logI("userId ====> $userId")
            val last = timestamp.toString().last()
            val id = userId.safeToInt() * last.toString().toInt() * 66
            "userId=$id&uri=${getRequestUrl(request)}&timestamp=$timestamp&organization=$organization"
        }
        /*return when (request.method) {
            "GET" -> {
                val params = request.url.queryParameterNames.joinToString("&") { "$it=${request.url.queryParameter(it)}" }
                if (params.isEmpty()) "timestamp=$timestamp&organization=$organization" else "$params&timestamp=$timestamp&organization=$organization"
            }

            "POST" -> {
                val params = request.body?.let {
                    bodyToJson(it)
                }
                if (params.isNullOrEmpty()) "timestamp=$timestamp&organization=$organization" else "$params&timestamp=$timestamp&organization=$organization"
            }

            else -> {
                "timestamp=$timestamp&organization=$organization"
            }
        }*/
    }

    private fun getRequestUrl(request: Request): String {
        return request.url.encodedPath  // This gets the path of the URL without the base URL
    }


    private fun bodyToJson(body: RequestBody): String {
        val buffer = okio.Buffer()
        body.writeTo(buffer)
        return buffer.readUtf8()
    }
}