package com.cl.common_base.net.interceptor

import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.bean.BaseBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.CoroutineFlowUtils
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import okhttp3.Interceptor
import okhttp3.Response
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import kotlin.concurrent.thread


/**
 * Token 拦截器
 *
 * @author 李志军 2022-08-08 17:05
 */
class TokenInterceptor : Interceptor {
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
        kotlin.runCatching {
            charset?.let {
                buffer.clone().readString(it)
                GSON.parseObject(buffer.clone().readString(it), BaseBean::class.java)
            }
        }.onFailure {
            logE(
                """
            TokenInterceptor: GSON
                ${it.message}
        """.trimIndent()
            )
            kotlin.runCatching {
                CoroutineFlowUtils.executeInMainThread(task = {
                    Reporter.reportApiError(url = request.url.toString(), query = "", -1, "", it.message)
                })
            }
        }.onSuccess {
            kotlin.runCatching {
                if (it?.code == 401) {
                    InterComeHelp.INSTANCE.logout()
                    //token过期 发通知
                    Prefs.removeKey(Constants.Login.KEY_LOGIN_DATA_TOKEN)
                    ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .navigation()
                }
            }
        }
        return response
    }
}