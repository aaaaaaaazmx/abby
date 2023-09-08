package com.cl.common_base.net.interceptor

import android.os.Build
import androidx.annotation.RequiresApi
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.util.AppUtil
import com.cl.common_base.util.Prefs
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 添加头部参数
 */
class BasicParamsInterceptor : Interceptor {
    // 是否同意隐私协议，如果同意了，那么需要更新
    private val privacyPolicy by lazy {
        Prefs.getBoolean(Constants.PrivacyPolicy.KEY_PRIVACY_POLICY_IS_AGREE, false)
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalHttpUrl = originalRequest.url
        // 添加body参数
        val url = originalHttpUrl.newBuilder().apply {
            addQueryParameter("osType", "1")
            addQueryParameter("uuid", if (privacyPolicy) AppUtil.getDeviceSerial() else null)
            addQueryParameter("mobileModel", if (privacyPolicy) AppUtil.deviceModel else null)
            addQueryParameter("mobileBrand", if (privacyPolicy) AppUtil.deviceBrand else null)
            addQueryParameter("version", if (privacyPolicy) AppUtil.appVersionName else null)
            addQueryParameter("system_version_code", "${Build.VERSION.SDK_INT}")
            addQueryParameter("timeZone", "${DateHelper.getTimeZOneNumber()}")
            // addQueryParameter("token", TokenCache.token ?: Prefs.getString(Constants.Login.KEY_LOGIN_DATA_TOKEN))
        }.build()
        val request = originalRequest.newBuilder().url(url).method(
            originalRequest.method,
            originalRequest.body
        ).build()
        return chain.proceed(request)
    }
}