package com.cl.common_base.report

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.tencent.bugly.crashreport.CrashReport

/**
 * 用于处理数据上报，自定义异常处理等等
 *
 * @author Scott Smith 2020-11-12 10:15
 */
object Reporter {

    /**
     * 上报错误到服务端
     *
     * @param errorType 错误类型 @see [ErrorType]
     * @param error 具体错误信息
     * @param options 其它具体参数，用于更加详细地描述该错误
     */
    @JvmStatic
    fun reportError(errorType: ErrorType, error: String, vararg options: Pair<String, String>) {
        val params = mapOf(*options)
        val log = """
            error: $error,
            options: $params
        """.trimIndent()
        CrashReport.postCatchedException(
            Throwable(
                """
           errorType: $errorType,
           error: $error,
           params: $params
        """.trimIndent()
            )
        )
    }

    /**
     * 上报接口错误
     *
     * @param url 请求地址
     * @param query 请求参数
     * @param httpCode http状态码
     * @param bizCode 业务错误码
     * @param error 具体错误信息
     */
    @JvmStatic
    fun reportApiError(
        url: String,
        query: String?,
        httpCode: Int,
        bizCode: String?,
        error: String?
    ) {
        reportError(
            ErrorType.ApiError, error.toString(),
            "url" to url,
            "query" to query.toString(),
            "httpCode" to httpCode.toString(),
            "bizCode" to bizCode.toString()
        )
    }

    /**
     * 上报自定义错误
     *
     * @param error 具体错误信息
     * @param options 其它具体参数，用于更加详细描述该错误
     */
    @JvmStatic
    fun reportTuYaError(
        method: String?,
        error: String?,
        errorCode: String?
    ) {
        reportError(
            ErrorType.TuYaError, error.toString(),
            "TuYaMethod" to method.toString(),
            "errorCode" to errorCode.toString(),
        )
    }


    /**
     * @param error 异常信息
     */
    @JvmStatic
    fun reportCatchError(
        error: String?,
        localizedMessage: String?,
        throwString: String?,
        response: String? = null
    ) {
        reportError(ErrorType.CatchError, error.toString(), "localizedMessage" to localizedMessage.toString(), "throwString" to throwString.toString(), "response" to response.toString())
    }

    /**
     * 上报Activity页面的时长统计, 使用本方法不需要在Activity的onResume和onPause 2个方法中多次注册上报事件,
     * 只需要在Activity的onResume生命周期方法之前调用本方法即可.
     */
//    @JvmStatic
//    fun autoPageStatistical(activity: AppCompatActivity) {
//        activity.lifecycle.addObserver(object : LifecycleEventObserver {
//            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//                val className = source.javaClass.name
//                when (event) {
//                    Lifecycle.Event.ON_RESUME -> {
//                        Timber.i("U-App report activity page on resume event with $className.")
//                        MobclickAgent.onResume(activity)
//                    }
//                    Lifecycle.Event.ON_PAUSE -> {
//                        Timber.i("U-App report activity page on resume event with $className.")
//                        MobclickAgent.onPause(activity)
//                    }
//                    else -> {}
//                }
//            }
//        })
//    }

    /**
     * 上报Fragment页面的时长统计, 使用本方法不需要在Fragment的onResume和onPause 2个方法中多次注册上报事件,
     * 只需要在Fragment的onResume生命周期方法之前调用本方法即可.
     * 注意 : Fragment的上报存在问题, 当一个Fragment页面内嵌了其他Fragment页面, 则需要外层的Fragment或者内嵌的Fragment的上报做取舍,
     * 只能上报其中之一, 否则会出现友盟统计的生命周期异常, 导致上报失败
     */
//    @JvmStatic
//    fun autoPageStatistical(fragment: Fragment) {
//        fragment.lifecycle.addObserver(object : LifecycleEventObserver {
//            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//                val className = source.javaClass.name
//                when (event) {
//                    Lifecycle.Event.ON_RESUME -> {
//                        Timber.i("UMLog : U-App report fragment page on resume event with $className.")
//                        MobclickAgent.onPageStart(className)
//                    }
//                    Lifecycle.Event.ON_PAUSE -> {
//                        Timber.i("UMLog : U-App report fragment page on pause event with $className.")
//                        MobclickAgent.onPageEnd(className)
//                    }
//                    else -> {}
//                }
//            }
//        })
//    }

    /**
     * 上报错误类型，用于区分不同上报错误类型
     */
    enum class ErrorType(val type: String) {
        ApiError("ApiError"),
        TuYaError("TuYaError"),
        CatchError("CatchError")
    }


    /**
     * 点击事件埋点上报
     */
    enum class EventId(val type: String) {
        // 酒店
        GoHotelDetail("hotel_detail"),
        GoHotelCreatOrder("hotel_creat_order"),
        GoHotelSearch("hotel_search"),

        // 加油
        GoRefuelList("refuel_list"),
        GoRefuelCreatOrder("refuel_creat"),
        GoRefuelCheckMember("refuel_check_member"),

        // 支付
        GoPayMoney("pay_money"),

        // 海豹会员首页会员开通 事件ID -> member_open
        MEMBER_OPEN("member_open"),

        // 海豹会员首页会员升级 事件ID -> member_up
        MEMBER_UP("member_up"),

        // 海豹会员首页会员降级 事件ID -> member_down
        MEMBER_DOWN("member_down"),

        // 海豹会员首页会员续费 事件ID -> member_renew
        MEMBER_RENEW("member_renew"),

        // 活动会员
        // 浏览渠道会员弹窗
        ACTIVITY_MEMBER_POPUPS("member_channel_popups"),

        // 点击渠道会员弹窗
        ACTIVITY_MEMBER_CHECKED("member_channel_tap_popups"),

        // 超能海豹点击开通
        ACTIVITY_MEMBER_OPEN("tap_activate_membership"),

        // 超能海豹开通成功
        ACTIVITY_MEMBER_OPEN_SUCCESS("success_membership_activation"),

        // 高策
        // 登录成功
        LOGIN_SUCCESS("Android_login_success"),

        // 酒店首页被点击
        HOTEL_HOME_CLICK("Android_Home_Hotel"),

        // 酒店首页被点击
        MEMBER_TAB("Android_member_tab"),
    }

    /**
     * 点击上报
     * @param context 上下文
     * @param eventId 事件ID
     * @param params 携带参数
     */
//    @JvmStatic
//    fun report(context: Context, eventId: EventId, vararg params: Pair<String, String>) {
//        if (params.isEmpty()) {
//            MobclickAgent.onEventObject(context, eventId.type, null)
//        } else {
//            MobclickAgent.onEventObject(context, eventId.type, mapOf(*params))
//        }
//    }
}