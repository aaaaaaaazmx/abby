package com.cl.modules_login.ui

import android.content.Intent
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.widget.code.VerificationCodeInputView
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.R
import com.cl.modules_login.databinding.ActivityVerifyEmailBinding
import com.cl.modules_login.request.UserRegisterReq
import com.cl.modules_login.viewmodel.VerifyEmailViewModel
import com.cl.modules_login.widget.RetransmissionPop
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 验证邮箱验证码界面
 */
@AndroidEntryPoint
class VerifyEmailActivity : BaseActivity<ActivityVerifyEmailBinding>(),
    VerificationCodeInputView.OnInputListener {
    /**
     * 这个是从,忘记密码\注册界面 传过来的
     */
    private val emailName by lazy {
        val name = intent.getStringExtra(KEY_EMAIL_NAME)
        name
    }

    /**
     * 是从注册界面传过来的.
     * 如果是从忘记密码传过来,那么这个参数是没有的
     */
    private val userRegisterBean by lazy {
        val bean =
            intent.getSerializableExtra(CreateAccountActivity.KEY_USER_REGISTER_BEAN) as? UserRegisterReq
        bean
    }

    /**
     * 这个是从,忘记密码\注册界面 传过来的
     */
    private val isRegister by lazy {
        val booleanExtra = intent.getBooleanExtra(KEY_IS_REGISTER, true)
        booleanExtra
    }

    private val sendPop by lazy {
        RetransmissionPop(context = this@VerifyEmailActivity, onAgainAction = {
            // 重新发送验证,
            // 需要判断当前是注册还是忘记密码
            if (isRegister) {
                emailName?.let { mViewModel.verifyEmail(it, "1") }
            } else {
                emailName?.let { mViewModel.updatePwd(it, "2") }
            }
        })
    }

    private val xPopup by lazy {
        XPopup.Builder(this@VerifyEmailActivity)
            .hasStatusBar(false)
            .isDestroyOnDismiss(false)
            .asCustom(sendPop)
    }

    @Inject
    lateinit var mViewModel: VerifyEmailViewModel

    override fun initView() {
        ARouter.getInstance().inject(this)

        //We sent an email to pinnachan@abby.com enter the verification code sent to your email address，and you can reset you password.
        binding.tvDesc.text =
            if (isRegister) {
                buildSpannedString {
                    append(getString(com.cl.common_base.R.string.send_email))
                    color(
                        ResourcesCompat.getColor(
                            resources,
                            com.cl.common_base.R.color.mainColor,
                            theme
                        )
                    ) {
                        append(
                            emailName
                        )
                    }
                    append(getString(com.cl.common_base.R.string.send_email_register))
                }
            } else {
                buildSpannedString {
                    append(getString(com.cl.common_base.R.string.send_email))
                    color(
                        ResourcesCompat.getColor(
                            resources,
                            com.cl.common_base.R.color.mainColor,
                            theme
                        )
                    ) {
                        append(
                            emailName
                        )
                    }
                    append(getString(com.cl.common_base.R.string.register_email_tips))
                }
            }

        // 文字修改
        binding.vmLog.text = if (isRegister) "Verify email" else "Reset password"
        binding.codeView.setOnInputListener(this)
    }

    override fun observe() {
        mViewModel.apply {
            /**
             *  验证验证码
             */
            isVerifySuccess.observe(this@VerifyEmailActivity) {
                when (it) {
                    is Resource.DataError -> {
                        hideProgressLoading()
                        it.errorMsg?.let { it1 -> ToastUtil.shortShow(it1) }
                    }
                    is Resource.Success -> {
                        hideProgressLoading()
                        logD("isVerifySuccess: ${it.data}")

                        // 跳转到修改密码界面, 需要区分是注册用户还是忘记密码用户,接口不一样
                        val intent =
                            Intent(this@VerifyEmailActivity, SetPasswordActivity::class.java)
                        intent.putExtra(
                            SetPasswordActivity.KEY_REGISTER_OR_FORGET_PASSWORD,
                            isRegister
                        )
                        // 用户注册的一些必要参数
                        intent.putExtra(
                            CreateAccountActivity.KEY_USER_REGISTER_BEAN,
                            userRegisterBean
                        )
                        intent.putExtra(KEY_EMAIL_NAME, emailName)
                        startActivity(intent)
                    }
                    is Resource.Loading -> {
                        showProgressLoading()
                    }
                }
            }


        }
    }

    override fun initData() {

        binding.btnSuccess.setOnClickListener {
            // 验证验证吗
            if (binding.codeView.code.isNullOrEmpty()) return@setOnClickListener
            emailName?.let { name -> mViewModel.verifyCode(binding.codeView.code, name) }
        }

        // 重新发送验证
        binding.tvSend.setOnClickListener {
            xPopup.show()
        }
    }

    override fun onComplete(code: String?) {
        code?.let {
            if (it.isEmpty()) {
                return@let
            }
            binding.btnSuccess.isEnabled = true
        }
    }

    override fun onInput() {
    }

    companion object {
        // 是否是注册还是忘记密码?
        const val KEY_IS_REGISTER = "key_is_register"

        // 邮箱
        const val KEY_EMAIL_NAME = "key_email_name"
    }
}