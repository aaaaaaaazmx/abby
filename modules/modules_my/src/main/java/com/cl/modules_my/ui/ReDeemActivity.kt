package com.cl.modules_my.ui

import com.cl.common_base.base.BaseActivity
import com.cl.common_base.widget.code.RedeemCodeInputView
import com.cl.common_base.widget.code.VerificationCodeInputView
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyActivityRedeemBinding

/**
 *  赎回界面
 */
class ReDeemActivity: BaseActivity<MyActivityRedeemBinding>(), RedeemCodeInputView.OnInputListener {
    override fun initView() {

    }

    override fun observe() {
    }

    override fun initData() {
    }

    override fun onComplete(code: String?) {
        code?.let {
            if (it.isEmpty()) {
                return@let
            }
            // 里面会包含一、需要去除
            val split = it.split("一")
            var codes = ""
            split.forEach { string ->
                codes+=string
            }
            // 打印
            ToastUtil.shortShow(codes)
            // todo 验证赎回code是否正确
        }
    }

    override fun onInput() {
    }
}