package com.cl.modules_login.widget

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.databinding.DataBindingUtil
import com.cl.common_base.constants.Constants
import com.cl.common_base.util.NoUnderlineClickSpan
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.span.appendClickable
import com.cl.common_base.util.span.appendSpace
import com.cl.modules_login.R
import com.cl.modules_login.databinding.PopPrivacyBinding
import com.lxj.xpopup.core.CenterPopupView


/**
 * 隐私协议弹窗
 */
class PrivacyPop(
    context: Context,
    val onCancelAction: (() -> Unit)? = null,
    val onConfirmAction: (() -> Unit)? = null,
    open val onTermUsAction: (() -> Unit)? = null,
    val onPrivacyAction: (() -> Unit)? = null
) : CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.pop_privacy
    }

    override fun onCreate() {
        super.onCreate()
        val bind = DataBindingUtil.bind<PopPrivacyBinding>(popupImplView)
        bind?.executePendingBindings()

        bind?.tvContent?.text = buildSpannedString {
            append(context.getString(com.cl.common_base.R.string.private_policy) + " ")
            color(
                ResourcesCompat.getColor(
                    resources,
                    com.cl.common_base.R.color.mainColor,
                    context.theme
                )
            ) { appendClickable(context.getString(com.cl.common_base.R.string.terms_us), isUnderlineText = false) {
                // 跳转到使用条款H5
                onTermUsAction?.invoke()
            } }
            append(context.getString(com.cl.common_base.R.string.string_1699))
            color(
                ResourcesCompat.getColor(
                    resources,
                    com.cl.common_base.R.color.mainColor,
                    context.theme
                )
            ) { appendClickable(context.getString(com.cl.common_base.R.string.policy), isUnderlineText = false){
                // 跳转到隐私协议H5
                onPrivacyAction?.invoke()
            } }
        }

        bind?.tvContent?.movementMethod = LinkMovementMethod.getInstance() // 设置了才能点击
        bind?.tvContent?.highlightColor = ResourcesCompat.getColor( // 设置之后点击才不会出现背景颜色
            resources,
            com.cl.common_base.R.color.transparent,
            context.theme
        )

        bind?.tvCancel?.setOnClickListener {
            onCancelAction?.invoke()
            dismiss()
        }
        bind?.tvConfirm?.setOnClickListener {
            onConfirmAction?.invoke()
            // 同意隐私协议
            Prefs.putBoolean(Constants.PrivacyPolicy.KEY_PRIVACY_POLICY_IS_AGREE, true)
            dismiss()
        }
    }
}