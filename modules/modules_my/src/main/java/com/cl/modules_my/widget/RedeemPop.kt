package com.cl.modules_my.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyPopRedeemBinding
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.core.CenterPopupView

class RedeemPop(context: Context, private val userInfo: UserinfoBean?, private val chooserOxygen: Int, private val exchangeRate: Int, private val oxygen: Int?, private val onConfirm: (() -> Unit)? = null): BottomPopupView(context) {


    override fun getImplLayoutId(): Int {
        return R.layout.my_pop_redeem
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyPopRedeemBinding>(popupImplView)?.apply {
            executePendingBindings()

            tvTitle.text =  if (userInfo?.email.isNullOrEmpty()) {
                """
                Redeemed Oxygen Coins：$chooserOxygen
                Account Balance: ${oxygen?.minus(chooserOxygen)}
                E-Gift Value:${'$'}$exchangeRate
            """.trimIndent()
            } else {
                """
                Redeemed Oxygen Coins：$chooserOxygen
                Account Balance: ${oxygen?.minus(chooserOxygen)}
                E-Gift Value:${'$'}$exchangeRate

                A e-gift card secrect code will send to your ${userInfo?.email}
            """.trimIndent()
            }

            tvDescription.setSafeOnClickListener {
                onConfirm?.invoke()
                dismiss()
            }
        }
    }
}