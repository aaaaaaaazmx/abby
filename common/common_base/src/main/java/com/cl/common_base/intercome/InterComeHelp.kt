package com.cl.common_base.intercome

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.cl.common_base.BaseApplication
import com.cl.common_base.BuildConfig
import com.cl.common_base.bean.AutomaticLoginData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.px2dp
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.screenHeight
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.luck.lib.camerax.utils.DensityUtil
import io.intercom.android.sdk.*
import io.intercom.android.sdk.identity.Registration
import io.intercom.android.sdk.push.IntercomPushClient

/**
 * InterCome 功能实现类
 */
class InterComeHelp {

    // 实现单例
    companion object {
        val INSTANCE: InterComeHelp by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            InterComeHelp()
        }
    }

    private constructor() {
        // 初始化时做的事情
        // 是否显示右下角悬浮图标
        // Intercom.client().setLauncherVisibility(Intercom.Visibility.VISIBLE)
        // 设置底部弹窗的高度
        Intercom.client().setBottomPadding((px2dp(screenHeight.toFloat()) * 2.5).safeToInt())
        // 消息显示。
        Intercom.client().setInAppMessageVisibility(Intercom.Visibility.VISIBLE)
        // 添加未读消息监听
        Intercom.client().addUnreadConversationCountListener {
            logI("addUnreadConversationCountListener Count = $it")
            // Handle count update
            LiveEventBus.get()
                .with(Constants.InterCome.KEY_INTER_COME_UNREAD_MESSAGE, Int::class.java)
                .postEvent(it)
        }
    }

    /**
     * 登录InterCome、并且更新用户信息
     *
     * @param map 后台返回的设备信息，需要同步到InterCome用户属性上
     * @param interComeUserId InterCome 唯一ID
     * @param userInfo 用户信息
     * @param refreshUserInfo 刷新用户信息
     * @param updateSuccess 更新成功回调
     * @param updateFail 更新失败回调
     */
    fun successfulLogin(
        map: Map<String, Any>?,
        interComeUserId: String?,
        userInfo: UserinfoBean.BasicUserBean? = null,
        refreshUserInfo: AutomaticLoginData? = null,
        updateSuccess: (() -> Unit)? = null,
        updateFail: ((intercomError: IntercomError) -> Unit)? = null
    ) {
        // 登录不明身份的用户 访客模式。
        // Intercom.client().loginUnidentifiedUser()
        /* For best results, use a unique user_id if you have one. */
        Intercom.client().logout()
        interComeUserId?.let { Registration().withUserId(it) }
            ?.apply {
                Intercom.client().loginIdentifiedUser(
                    userRegistration = this,
                    intercomStatusCallback = object : IntercomStatusCallback {
                        override fun onSuccess() {
                            // Handle successl
                            // 上报给自定义收集器InterCome登录成功
                            /*Reporter.reportError(
                                Reporter.ErrorType.ApiError, "InterCome: Login success interComeUserId: $interComeUserId",
                                "userId" to userInfo?.userId.toString(),
                                "interComeUserId" to interComeUserId,
                                "userExternalId" to userInfo?.externalId.toString(),
                                "deviceId" to userInfo?.deviceId.toString(),
                                "isVip" to userInfo?.isVip.toString(),
                            )*/
                            logI("InterCome: Login success interComeUserId: $interComeUserId")
                            //  登录成功之后更新用户信息。
                           /* updateInterComeUserInfo(
                                map = map,
                                userInfo = userInfo,
                                refreshUserInfo = refreshUserInfo,
                                updateSuccess = updateSuccess,
                                updateFail = updateFail
                            )*/
                        }

                        override fun onFailure(intercomError: IntercomError) {
                            // Handle failure
                            logI("InterCome: Login onFailure ${intercomError.errorMessage}")
                            updateFail?.invoke(intercomError)
                        }
                    }
                )
            }
    }

    /**
     * 更新interCOme用户信息
     */
    fun updateInterComeUserInfo(
        map: Map<String, Any>?,
        userInfo: UserinfoBean.BasicUserBean? = null,
        refreshUserInfo: AutomaticLoginData? = null,
        updateSuccess: (() -> Unit)? = null,
        updateFail: ((intercomError: IntercomError) -> Unit)? = null
    ) {
        // UserAttributes.Builder().withCustomAttribute("paid_subscriber", "Yes")  可以直接创建没定义的字段ID
        var userAttributes: UserAttributes? = null
        userAttributes = if (null == userInfo) {
            UserAttributes.Builder()
                .withCustomAttributes(map ?: mapOf<String, Any>())
                .withName(refreshUserInfo?.nickName)
                .withEmail(refreshUserInfo?.email)
                .withPhone(refreshUserInfo?.phoneNumber)
                .withUserId(refreshUserInfo?.userId.toString())
                // .withUserId(refreshUserInfo?.userId.toString())
                .build()
        } else if (null == refreshUserInfo) {
            UserAttributes.Builder()
                .withCustomAttributes(map ?: mapOf<String, String>())
                .withName(userInfo.nickName)
                .withEmail(userInfo.email)
                .withPhone(userInfo.phoneNumber)
                .withUserId(userInfo.userId.toString())
                .build()
        } else {
            UserAttributes.Builder()
                .withCustomAttributes(map ?: mapOf<String, Any>())
                .build()
        }

        Intercom.client().updateUser(
            userAttributes = userAttributes,
            intercomStatusCallback = object : IntercomStatusCallback {
                override fun onSuccess() {
                    logI("InterCome updateUser : onSuccess")
                    // Handle success
                    updateSuccess?.invoke()
                }

                override fun onFailure(intercomError: IntercomError) {
                    logI("InterCome updateUser : failure ${intercomError.errorMessage}")
                    // Handle failure
                    updateFail?.invoke(intercomError)
                }
            }
        )
    }

    // 获取InterCome未读消息数量
    fun getUnreadConversationCount(): Int {
        logI("getUnreadConversationCount Count = ${Intercom.client().unreadConversationCount}")
        return Intercom.client().unreadConversationCount
    }

    // 通过InterCome打开各种空间、比如打开文章
    fun openInterComeSpace(space: InterComeSpace? = InterComeSpace.Home, id: String? = null) {
        when (space) {
            // 打开具体文章
            is InterComeSpace.Article -> {
                id?.let {
                    Intercom.client().presentContent(content = IntercomContent.Article(id = it))
                }
            }

            is InterComeSpace.Carousel -> {
                id?.let {
                    Intercom.client().presentContent(content = IntercomContent.Carousel(id = it))
                }
            }
            /* is InterComeSpace.HelpCenterCollections -> {
                 Intercom.client().presentContent(content = IntercomContent.HelpCenterCollections(id = id))
             }*/
            is InterComeSpace.Survey -> {
                id?.let {
                    Intercom.client().presentContent(content = IntercomContent.Survey(id = it))
                }
            }

            // 打开具体页面
            is InterComeSpace.Messages -> {
                Intercom.client().present(space = IntercomSpace.Messages)
            }

            is InterComeSpace.HelpCenter -> {
                Intercom.client().present(space = IntercomSpace.HelpCenter)
            }

            is InterComeSpace.Home -> {
                Intercom.client().present(space = IntercomSpace.Home)
            }

            else -> {
                id?.let {
                    Intercom.client().presentContent(content = IntercomContent.Article(id = it))
                }
            }
        }
    }

    // 打开聊天界面
    fun openInterComeChat() {
        Intercom.client().present(space = IntercomSpace.Messages)
    }

    // 打开帮助中心
    fun openInterComeHelpCenter() {
        Intercom.client().present(space = IntercomSpace.HelpCenter)
    }

    // 打开主页面
    fun openInterComeHome() {
        // 用户信息
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val parseObject = GSON.parseObject(bean, UserinfoBean::class.java)
        if (parseObject?.isVip == 1) {
            Intercom.client().present(space = IntercomSpace.Home)
        } else {
            Intercom.client().present(space = IntercomSpace.HelpCenter)
        }
    }

    /**
     * 注销用户
     */
    fun logout() {
        /* This clears the Intercom SDK's cache of your user's identity
         * and wipes the slate clean. */
        Intercom.client().logout()
    }


    sealed class InterComeSpace {
        object Article : InterComeSpace()
        object Carousel : InterComeSpace()
        object HelpCenterCollections : InterComeSpace()
        object Survey : InterComeSpace()

        // 打开具体页面
        object Home : InterComeSpace()
        object Messages : InterComeSpace()
        object HelpCenter : InterComeSpace()
    }
}