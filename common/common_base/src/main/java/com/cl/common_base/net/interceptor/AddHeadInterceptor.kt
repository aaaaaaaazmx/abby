package com.cl.common_base.net.interceptor

import android.os.Build
import androidx.annotation.RequiresApi
import com.cl.common_base.constants.Constants
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.util.Prefs
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AddHeadInterceptor : Interceptor {
    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val request: Request = original.newBuilder()
            .header("token", ServiceCreators.TokenCache.token ?: Prefs.getString(Constants.Login.KEY_LOGIN_DATA_TOKEN))
            .addHeader("Connection","close")

            .build()
        /*builder.addHeader("Cookie", "设置的自定义的值")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("X-Requested-With", "XMLHttpRequest");*/

        /*builder.addHeader("Cookie", "设置的自定义的值")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("X-Requested-With", "XMLHttpRequest");*/
        return chain.proceed(request)
    }
}