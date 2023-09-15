package com.cl.common_base.net

import com.cl.common_base.BaseApplication
import com.cl.common_base.BuildConfig
import com.cl.common_base.constants.Constants
import com.cl.common_base.net.adapter.FlowCallAdapterFactory
import com.cl.common_base.net.adapter.GsonTypeAdapterFactory
import com.cl.common_base.net.interceptor.*
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

/**
 *  description: 服务创建类
 */
object ServiceCreators {

    // 服务器地址
//    private val BASE_URL = if (BuildConfig.DEBUG) HttpsUrl.OUTER_ANG_URL else HttpsUrl.PRODUCTION_URL
    val BASE_URL = if (BuildConfig.DEBUG) HttpsUrl.TEST_URL else HttpsUrl.PRODUCTION_URL
//    val BASE_URL = HttpsUrl.TEST_URL
//    private val BASE_URL = HttpsUrl.PRODUCTION_URL
//    private val BASE_URL = HttpsUrl.BD_URL
//    private val BASE_URL = HttpsUrl.OUTER_ANG_URL

    private const val MAX_CACHE_SIZE: Long = 1024 * 1024 * 50 // 50M 的缓存大小

    //设置 请求的缓存的大小跟位置
    private val cacheFile = File(BaseApplication.getContext().cacheDir, "httpCache")
    private val cache = Cache(cacheFile, MAX_CACHE_SIZE)
    //    private val cookieJar = PersistentCookieJar(
    //        SetCookieCache(),
    //        SharedPrefsCookiePersistor(BaseApplication.getContext())
    //    )

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(TokenInterceptor())
        .addInterceptor(MultiBaseUrlInterceptor())
        .addInterceptor(LoggingInterceptor())
        .addInterceptor(AddHeadInterceptor())
        .addInterceptor(BasicParamsInterceptor())
        /*.hostnameVerifier { hostname, session ->
            // 信任主机
            // 也可以使用DNS优化政策
            val hv: HostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
            hv.verify("beheyabby.com", session)
        }*/
        .build()

    private val builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addCallAdapterFactory(FlowCallAdapterFactory.create())
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().registerTypeAdapterFactory(
                    GsonTypeAdapterFactory()
                ).create()
            )
        )

    private var retrofit = builder.build()

    //    val service: HttpApiService by lazy { retrofit.create(HttpApiService::class.java) }

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    fun newBuilder(url: String) {
        if (BuildConfig.DEBUG) {
            retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(httpClient)
                .addCallAdapterFactory(FlowCallAdapterFactory.create())
                .addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder().registerTypeAdapterFactory(
                            GsonTypeAdapterFactory()
                        ).create()
                    )
                ).build()
        }
    }

    object HttpsUrl {
        // 测试服务器
        const val TEST_URL = Constants.HttpUrl.TEST_URL
        const val BD_URL = Constants.HttpUrl.BD_URL
        const val PRODUCTION_URL = Constants.HttpUrl.FORMAL_URL
        const val DEVELOPMENT_URL = Constants.HttpUrl.DEVELOPMENT_URL
        const val OUTER_ANG_URL = Constants.HttpUrl.OUTER_ANG_URL
    }

    object TokenCache {
        var token: String? = null
    }
}