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
            ivClose.setSafeOnClickListener { dismiss() }

            tvTitle.text =  if (userInfo?.email.isNullOrEmpty()) {
                context.getString(com.cl.common_base.R.string.home_reedee_desc_ont, chooserOxygen, oxygen?.minus(chooserOxygen), '$', exchangeRate).trimIndent()
            } else {
                context.getString(com.cl.common_base.R.string.home_reedee_desc_two, chooserOxygen, oxygen?.minus(chooserOxygen), '$', exchangeRate, userInfo?.email).trimIndent()
            }

            tvDescription.setSafeOnClickListener {
                onConfirm?.invoke()
                dismiss()
            }
        }
    }
}