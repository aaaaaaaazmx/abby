package com.cl.modules_my.ui

import android.content.Intent
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.xpopup
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.code.VerificationCodeInputView
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyDeleteAccountActivityBinding
import com.cl.modules_my.viewmodel.DeleteAccountViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lxj.xpopup.interfaces.OnSelectListener
import com.thingclips.smart.android.user.api.ILogoutCallback
import com.thingclips.smart.home.sdk.ThingHomeSdk
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 删除账户页面
 */
@AndroidEntryPoint
class DeleteAccountActivity : BaseActivity<MyDeleteAccountActivityBinding>(), VerificationCodeInputView.OnInputListener {

    private val userInfo by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    @Inject
    lateinit var viewModel: DeleteAccountViewModel

    override fun initView() {
        // 获取删除账户的验证码
        userInfo?.email?.let { viewModel.verifyEmail(it, "3") }

        binding.tvSend.setOnClickListener {
            xpopup(this@DeleteAccountActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(true)
                asCenterList("", arrayOf(getString(com.cl.common_base.R.string.string_1832), getString(com.cl.common_base.R.string.my_cancel)), OnSelectListener { position, text ->
                    when (position) {
                        0 -> {
                            // 重发邮件
                            userInfo?.email?.let { viewModel.verifyEmail(it, "3") }
                        }

                        1 -> {
                            // 取消
                        }
                    }
                }).show()
            }
        }

        binding.tvDesc.text = buildSpannedString {
            append(getString(R.string.send_email))
            color(
                ResourcesCompat.getColor(
                    resources,
                    R.color.mainColor,
                    theme
                )
            ) {
                append(
                    " ${userInfo?.email} "
                )
            }
            append(getString(com.cl.common_base.R.string.string_1833))
        }

        // 删除账户
        binding.btnSuccess.setOnClickListener {
            // 验证验证吗ka
            if (binding.codeView.code.isNullOrEmpty()) return@setOnClickListener
            userInfo?.email?.let { name -> viewModel.verifyCode(binding.codeView.code, name) }
        }
    }

    override fun observe() {
        viewModel.apply {
            sendStates.observe(this@DeleteAccountActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }

                loading { showProgressLoading() }

                success {
                    hideProgressLoading()
                }
            })

            isVerifySuccess.observe(this@DeleteAccountActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                loading { showProgressLoading() }

                success {
                    hideProgressLoading()
                    InterComeHelp.INSTANCE.logout()
                    ThingHomeSdk.onDestroy()
                    ThingHomeSdk.getUserInstance().logout(object : ILogoutCallback {
                        override fun onSuccess() {
                            // 清除缓存数据
                            Prefs.removeKey(Constants.Login.KEY_LOGIN_DATA_TOKEN)
                            // 推出firbase账号
                            Firebase.auth.signOut()
                            // 清除所有缓存
                            Prefs.clear()
                            // 清除上面所有的Activity
                            // 跳转到Login页面
                            ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN)
                                .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                .navigation()
                        }

                        override fun onError(code: String?, error: String?) {
                            logE(
                                """
                           logout -> onError:
                            code: $code
                            error: $error
                        """.trimIndent()
                            )
                            ToastUtil.shortShow(error)
                            Reporter.reportTuYaError("getUserInstance", error, code)
                        }
                    })
                }
            })
        }
    }

    override fun onComplete(code: String?) {
        code?.let {
            if (it.isEmpty()) {
                return@let
            }
            binding.btnSuccess.isEnabled = true
            // 验证码邮箱是否正确
            // emailName?.let { name -> mViewModel.verifyCode(it, name) }
        }
    }

    override fun onInput() {
    }

    override fun initData() {
        binding.codeView.setOnInputListener(this)
    }
}