package com.cl.common_base.intercome

import com.cl.common_base.BuildConfig
import com.cl.common_base.bean.AutomaticLoginData
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.util.livedatabus.LiveEventBus
import io.intercom.android.sdk.*
import io.intercom.android.sdk.identity.Registration

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
        // 添加未读消息监听
        Intercom.client().addUnreadConversationCountListener {
            // Handle count update
            LiveEventBus.get()
                .with(Constants.InterCome.KEY_INTER_COME_UNREAD_MESSAGE, Int::class.java)
                .postEvent(it)
        }
    }

    /**
     * 登录InterCome、并且更新用户信息
     */
    fun successfulLogin(
        interComeUserId: String?,
        userInfo: UserinfoBean.BasicUserBean? = null,
        refreshUserInfo: AutomaticLoginData? = null,
        updateSuccess: (() -> Unit)? = null,
        updateFail: ((intercomError: IntercomError) -> Unit)? = null
    ) {
        // 登录不明身份的用户 访客模式。
        // Intercom.client().loginUnidentifiedUser()

        /* For best results, use a unique user_id if you have one. */
        interComeUserId?.let { Registration.create().withUserId(it) }?.apply {
            Intercom.client().loginIdentifiedUser(
                userRegistration = this,
                intercomStatusCallback = object : IntercomStatusCallback {
                    override fun onSuccess() {
                        // Handle success
                        // todo 登录成功之后更新用户信息。
                        updateInterComeUserInfo(
                            userInfo = userInfo,
                            refreshUserInfo = refreshUserInfo,
                            updateSuccess = updateSuccess,
                            updateFail = updateFail
                        )
                    }

                    override fun onFailure(intercomError: IntercomError) {
                        // Handle failure
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
        userInfo: UserinfoBean.BasicUserBean? = null,
        refreshUserInfo: AutomaticLoginData? = null,
        updateSuccess: (() -> Unit)? = null,
        updateFail: ((intercomError: IntercomError) -> Unit)? = null
    ) {
        // UserAttributes.Builder().withCustomAttribute("paid_subscriber", "Yes")  可以直接创建没定义的字段ID
        var userAttributes: UserAttributes? = null
        userAttributes = if (null == userInfo) {
            UserAttributes.Builder()
                .withName(refreshUserInfo?.nickName)
                .withEmail(refreshUserInfo?.email)
                .withUserId(if (BuildConfig.DEBUG) "test_${refreshUserInfo?.userId}" else refreshUserInfo?.userId.toString())
                .build()
        } else {
            UserAttributes.Builder()
                .withName(userInfo.nickName)
                .withEmail(userInfo.email)
                .withUserId(if (BuildConfig.DEBUG) "test_${userInfo.userId}" else userInfo.userId.toString())
                .build()
        }

        Intercom.client().updateUser(
            userAttributes = userAttributes,
            intercomStatusCallback = object : IntercomStatusCallback {
                override fun onSuccess() {
                    // Handle success
                    updateSuccess?.invoke()
                }

                override fun onFailure(intercomError: IntercomError) {
                    // Handle failure
                    updateFail?.invoke(intercomError)
                }
            }
        )
    }

    // 获取InterCome未读消息数量
    fun getUnreadConversationCount(): Int {
        return Intercom.client().unreadConversationCount
    }

    // 通过InterCome打开各种空间、比如打开文章
    fun openInterComeSpace(space: InterComeSpace, id: String? = null) {
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
        Intercom.client().present(space = IntercomSpace.Home)
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