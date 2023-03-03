package com.cl.modules_my.ui

import android.content.Intent
import android.text.method.LinkMovementMethod
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.span.appendClickable
import com.cl.common_base.web.BaseWebActivity
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.code.RedeemCodeInputView
import com.cl.common_base.widget.code.VerificationCodeInputView
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyActivityRedeemBinding
import com.cl.modules_my.viewmodel.RedeemViewModel
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 *  赎回界面
 */
@AndroidEntryPoint
class ReDeemActivity : BaseActivity<MyActivityRedeemBinding>(), RedeemCodeInputView.OnInputListener {
    @Inject
    lateinit var mViewModel: RedeemViewModel

    private val userInfoBean by lazy {
        val json = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        val bean = GSON.parseObject(json, UserinfoBean::class.java)
        bean
    }

    private val pop by lazy {
        XPopup.Builder(this@ReDeemActivity)
    }

    override fun initView() {
        binding.codeView.setOnInputListener(this@ReDeemActivity)
        // 富文本
        // htmlSpan()

    }

    private fun htmlSpan() {
        binding.tvHtml.text = buildSpannedString {
            append("If you would like to extend your growing service, please go to our official website ")
            appendClickable("https://heyabby.com/pages/subscription") {
                // 跳转到重新连接页面
                val intent = Intent(
                    this@ReDeemActivity,
                    WebActivity::class.java
                )
                intent.putExtra(WebActivity.KEY_WEB_URL, "https://heyabby.com/pages/subscription")
                startActivity(
                    intent
                )
            }
            append(" to purchase the option most suitable for you. This service includes our one-on-one support, growing pack (fertilizers, basket, carbon filters, etc), and device connectivity to our server.")

            appendLine()
            appendLine()
            append("After purchase, you’ll receive a verification code via email. Enter the code below to extend your service validity accordingly.")
        }
        binding.tvHtml.movementMethod = LinkMovementMethod.getInstance() // 设置了才能点击
        binding.tvHtml.highlightColor = ResourcesCompat.getColor( // 设置之后点击才不会出现背景颜色
            resources,
            com.cl.common_base.R.color.transparent,
            theme
        )
    }

    override fun observe() {
        mViewModel.apply {
            checkSubscriberNumber.observe(this@ReDeemActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    //  弹出确认框
                    pop.asCustom(
                        BaseCenterPop(
                            this@ReDeemActivity,
                            onConfirmAction = {
                                //  调用赎回接口
                                val subscriberNumber = data?.subscriberNumber
                                subscriberNumber?.let { mViewModel.topUpSubscriberNumber(it) }
                            },
                            content = if (data?.month == "1") "${data?.month} month will be added to your account " else "${data?.month} months will be added to your account ",
                            cancelText = getString(com.cl.common_base.R.string.my_cancel),
                            confirmText = getString(com.cl.common_base.R.string.base_ok),
                            richText = data?.email
                        )
                    ).show()
                }
            })

            // 充值
            topUpSubscriberNumber.observe(this@ReDeemActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    finish()
                }
            })
        }
    }

    override fun initData() {
        binding.btnSuccess.setOnClickListener {
            // 检测验证码是否正确
            binding.codeView.code?.let {
                if (it.isEmpty()) {
                    return@let
                }
                // 里面会包含一、需要去除
                val split = it.split("一")
                var codes = ""
                split.forEach { string ->
                    codes += string
                }
                // 验证赎回code是否正确
                mViewModel.checkSubscriberNumber(codes)
            }
        }

        binding.btnPurchase.setOnClickListener {
            val url = "https://heyabby.com/pages/subscription?selling_plan=3451781334&variant=42758697582806&utm_source=app&utm_medium=extension+lp&utm_campaign=281"
            // 跳转到下载界面
            val intent = Intent(this@ReDeemActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, url)
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Purchase")
            startActivity(intent)
        }
    }

    override fun onComplete(code: String?) {
        code?.let {
            if (it.isEmpty()) {
                return@let
            }
            binding.btnSuccess.isEnabled = it.length == 9
            // 里面会包含一、需要去除
            /*val split = it.split("一")
            var codes = ""
            split.forEach { string ->
                codes += string
            }*/
            // 打印
            // ToastUtil.shortShow(codes)
        }
    }

    override fun onInput() {
    }
}