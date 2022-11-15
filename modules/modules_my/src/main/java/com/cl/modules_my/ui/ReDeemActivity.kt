package com.cl.modules_my.ui

import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.code.RedeemCodeInputView
import com.cl.common_base.widget.code.VerificationCodeInputView
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyActivityRedeemBinding
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint

/**
 *  赎回界面
 */
@AndroidEntryPoint
class ReDeemActivity : BaseActivity<MyActivityRedeemBinding>(), RedeemCodeInputView.OnInputListener {


    private val userInfoBean by lazy {
        val json = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val bean = GSON.parseObject(json, UserinfoBean::class.java)
        bean
    }

    private val pop by lazy {
        XPopup.Builder(this@ReDeemActivity)
    }

    override fun initView() {

    }

    override fun observe() {
    }

    override fun initData() {
        binding.btnSuccess.setOnClickListener {
            // todo 弹出确认框
            //
            pop.asCustom(
                BaseCenterPop(
                    this@ReDeemActivity,
                    onConfirmAction = {
                        // todo 调用赎回接口
                    },
                    content = "3 Month Digital will be added to ",
                    confirmText = getString(com.cl.common_base.R.string.my_cancel),
                    cancelText = getString(com.cl.common_base.R.string.base_ok),
                    richText = userInfoBean?.email.toString()
                )
            ).show()
        }
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
                codes += string
            }
            // 打印
            ToastUtil.shortShow(codes)
            // todo 验证赎回code是否正确
        }
    }

    override fun onInput() {
    }
}