package com.cl.abby

import android.content.Context
import android.util.Log
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.api.JPushMessage
import cn.jpush.android.api.NotificationMessage
import cn.jpush.android.service.JPushMessageReceiver
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.util.livedatabus.LiveEventBus


/**
 * 极光通知类
 *
 * @author 李志军 2022-08-12 15:17
 */
class JpushReceiver: JPushMessageReceiver() {

    /**
     * 极光推送消息
     */
    override fun onNotifyMessageArrived(context: Context?, message: NotificationMessage) {
        logI("[onNotifyMessageArrived] $message")
    }

    /**
     * 系统内消息
     */
    override fun onMessage(p0: Context?, p1: CustomMessage?) {
        logI("[JPUSH:CustomMessage] ${p1.toString()}")
        // 需要根据messageId去重复，& ， 加入到unread消息列表当中
        LiveEventBus.get().with(Constants.Jpush.KEY_IN_APP_MESSAGE, String::class.java)
            .postEvent(p1?.extra.toString())
    }

    /**
     * 别名回调
     */
    override fun onAliasOperatorResult(p0: Context?, p1: JPushMessage?) {
        super.onAliasOperatorResult(p0, p1)
        logI("""
            onAliasOperatorResult
            JPushMessage: 
            ${p1.toString()}
        """.trimIndent())
    }


    /**
     * tag回调
     */
    override fun onTagOperatorResult(p0: Context?, p1: JPushMessage?) {
        super.onTagOperatorResult(p0, p1)
        logI("""
            onTagOperatorResult
            JPushMessage: 
            ${p1.toString()}
        """.trimIndent())
    }
}