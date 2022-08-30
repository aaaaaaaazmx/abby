package com.cl.modules_pairing_connection.widget

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.cl.modules_pairing_connection.R
import com.lxj.xpopup.core.BubbleAttachPopupView

class ReconnectTipsPop(
    context: Context,
    private val onDismissAction: (() -> Unit)? = null
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pair_reconnect_tips
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun dismiss() {
        super.dismiss()
        onDismissAction?.invoke()
    }
}