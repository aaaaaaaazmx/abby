package com.cl.modules_login.ui

import android.content.Intent
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.Constants.Global.KEY_REGISTER_OR_FORGET_PASSWORD
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.init.InitSdk
import com.cl.common_base.listener.TuYaDeviceUpdateReceiver
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.salt.AESCipher
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.code.VerificationCodeInputView
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.databinding.ActivityVerifyEmailBinding
import com.cl.modules_login.request.BindSourceEmailReq
import com.cl.modules_login.request.UserRegisterReq
import com.cl.modules_login.response.LoginData
import com.cl.modules_login.viewmodel.VerifyEmailViewModel
import com.cl.modules_login.widget.RetransmissionPop
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
     * 第三方登录token
     */
    private val thirdToken by lazy {
        intent.getStringExtra(LoginActivity.KEY_THIRD_TOKEN)
    }

    /**
     * 第三方登录来源
     */
    private val thirdSource by lazy {
        intent.getStringExtra(LoginActivity.KEY_SOURCE)
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

    /**
     * 判断是否用于邮箱登录，优先级大于之前2个参数
     */
    private val isEmailLogin by lazy {
        intent.getBooleanExtra(KEY_IS_VERIFY, false)
    }

    private val sendPop by lazy {
        RetransmissionPop(context = this@VerifyEmailActivity, onAgainAction = {
            if (!thirdSource.isNullOrEmpty()) {
                emailName?.let { mViewModel.verifyEmail(it, "5") }
                return@RetransmissionPop
            }
            // 邮箱验证码登录
            if (isEmailLogin) {
                emailName?.let { mViewModel.verifyEmail(it, "6") }
                return@RetransmissionPop
            }
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
        // binding.codeView.setEtNumber(if (isEmailLogin) 4 else 6)
        //We sent an email to pinnachan@abby.com enter the verification code sent to your email address，and you can reset you password.
        binding.tvDesc.text =
            if (isRegister || isEmailLogin) {
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
                            " $emailName "
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
                            " $emailName "
                        )
                    }
                    append(getString(com.cl.common_base.R.string.register_email_tips))
                }
            }

        // 文字修改
        binding.vmLog.text = if (isEmailLogin) "Enter the OTP" else if (isRegister) "Verify email" else "Reset password"
        binding.btnSuccess.text = if (isEmailLogin) "Login" else "Continue"
        binding.codeView.setOnInputListener(this)
    }

    private lateinit var userInfoBean: LoginData

    override fun observe() {
        mViewModel.apply {
            /**
             * 检查是否种植过
             */
            checkPlant.observe(this@VerifyEmailActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { msg -> ToastUtil.shortShow(msg) }
                }
                success {
                    hideProgressLoading()
                    kotlin.runCatching { // 初始化SDK
                        InitSdk.init() // 是否种植过
                        // 保存账号
                        userInfoBean.email?.let { Prefs.putString(Constants.USER_NAME, it) }
                        data?.let { PlantCheckHelp().plantStatusCheck(this@VerifyEmailActivity, it) }
                    }
                    finish()
                }
            })

            registerLoginLiveData.observe(this@VerifyEmailActivity) {
                when (it) {
                    is Resource.Loading -> {
                        showProgressLoading()
                    }

                    is Resource.Success -> {
                        userInfoBean = it.data!! // 保存当前的信息.
                        GSON.toJson(it.data)?.let { data ->
                            logI("LoginData: $data")
                            Prefs.putStringAsync(Constants.Login.KEY_LOGIN_DATA, data)
                        } // 保存Token
                        it.data?.token?.let { it1 ->
                            Prefs.putStringAsync(
                                Constants.Login.KEY_LOGIN_DATA_TOKEN, it1
                            )
                            ServiceCreators.TokenCache.token = it1
                        }

                        // 保存账号密码
                        it.data?.email?.let { it1 ->
                            Prefs.putStringAsync(
                                Constants.Login.KEY_LOGIN_ACCOUNT, it1
                            )
                        }

                        /**
                         * 登录涂鸦
                         */
                        val it = mViewModel.registerLoginLiveData.value
                        mViewModel.tuYaLogin(
                            map = mapOf(),
                            interComeUserId = it?.data?.externalId,
                            userInfo = UserinfoBean.BasicUserBean(userId = it?.data?.userId, email = it?.data?.email, userName = it?.data?.nickName),
                            deviceId = it?.data?.deviceId,
                            code = it?.data?.tuyaCountryCode,
                            email = it?.data?.email,
                            password = AESCipher.aesDecryptString(it?.data?.tuyaPassword, AESCipher.KEY),
                            onRegisterReceiver = { devId ->
                                val intent = Intent(this@VerifyEmailActivity, TuYaDeviceUpdateReceiver::class.java)
                                startService(intent)
                            },
                            onError = { code, error ->
                                hideProgressLoading()
                                error?.let { ToastUtil.shortShow(it) }
                            })
                    }

                    is Resource.DataError -> {
                        hideProgressLoading() // 错误信息显示出来
                        when (it.errorCode) {
                            else -> {
                                it.errorMsg?.let { msg -> ToastUtil.shortShow(msg) }
                            }
                        }
                    }

                    else -> {
                    }
                }
            }


            /**
             * 检查此邮箱是否已经注册过
             */
            isBindEmail.observe(this@VerifyEmailActivity, resourceObserver {
                success {
                    if (data == false) {
                        // 跳转到修改密码界面, 需要区分是注册用户还是忘记密码用户,接口不一样
                        val intent =
                            Intent(this@VerifyEmailActivity, SetPasswordActivity::class.java)
                        intent.putExtra(
                            KEY_REGISTER_OR_FORGET_PASSWORD,
                            true
                        )
                        // 用户注册的一些必要参数
                        intent.putExtra(
                            CreateAccountActivity.KEY_USER_REGISTER_BEAN,
                            userRegisterBean
                        )
                        intent.putExtra(KEY_EMAIL_NAME, emailName)
                        intent.putExtra(KEY_EMAIL_CODE, binding.codeView.code)
                        intent.putExtra(LoginActivity.KEY_SOURCE, thirdSource)
                        intent.putExtra(LoginActivity.KEY_THIRD_TOKEN, thirdToken)
                        startActivity(intent)
                    } else {
                        // 不管有没有绑定过，都需要绑定，只是检查是否存在
                        bindSourceEmail(
                            BindSourceEmailReq(
                                emailName, thirdSource, sourceUserId = AESCipher.aesEncryptString(
                                    Firebase.auth.currentUser?.email, AESCipher.KEY
                                )
                            )
                        )
                    }
                }
            })

            /**
             * 第三方绑定邮箱
             */
            bindEmail.observe(this@VerifyEmailActivity, resourceObserver {
                error { errorMsg, code ->

                }
                success {
                    if (isBindEmail.value?.data == true) {
                        //  如果已经绑定过，那么直接使用第三方登录
                        when (thirdSource) {
                            "google" -> {
                                // 如果是谷歌登录
                                // 调用登录接口
                                mViewModel.loginReq.value?.let {
                                    it.userName = null
                                    it.password = null
                                    it.source = "google"
                                    it.autoToken = thirdToken
                                    it.sourceUserId = AESCipher.aesEncryptString(
                                        Firebase.auth.currentUser?.email, AESCipher.KEY
                                    )
                                    mViewModel.login()
                                }
                            }
                        }
                    } else {
                        // 跳转到修改密码界面, 需要区分是注册用户还是忘记密码用户,接口不一样
                        val intent =
                            Intent(this@VerifyEmailActivity, SetPasswordActivity::class.java)
                        intent.putExtra(
                            KEY_REGISTER_OR_FORGET_PASSWORD,
                            true
                        )
                        // 用户注册的一些必要参数
                        intent.putExtra(
                            CreateAccountActivity.KEY_USER_REGISTER_BEAN,
                            userRegisterBean
                        )
                        intent.putExtra(KEY_EMAIL_NAME, emailName)
                        intent.putExtra(KEY_EMAIL_CODE, binding.codeView.code)
                        intent.putExtra(LoginActivity.KEY_SOURCE, thirdSource)
                        intent.putExtra(LoginActivity.KEY_THIRD_TOKEN, thirdToken)
                        startActivity(intent)
                    }
                }
            })

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

                        // 判断是否是第三方登录，用第三方来源是否为空来判断
                        if (!thirdSource.isNullOrEmpty()) {
                            // 检查邮箱是否被绑定过
                            emailName?.let { it1 -> mViewModel.isBindSourceEmail(it1) }
                            return@observe
                        }


                        // 跳转到修改密码界面, 需要区分是注册用户还是忘记密码用户,接口不一样
                        val intent =
                            Intent(this@VerifyEmailActivity, SetPasswordActivity::class.java)
                        intent.putExtra(
                            KEY_REGISTER_OR_FORGET_PASSWORD,
                            isRegister
                        )
                        // 用户注册的一些必要参数
                        intent.putExtra(
                            CreateAccountActivity.KEY_USER_REGISTER_BEAN,
                            userRegisterBean
                        )
                        intent.putExtra(KEY_EMAIL_NAME, emailName)
                        intent.putExtra(KEY_EMAIL_CODE, binding.codeView.code)
                        startActivity(intent)
                    }

                    is Resource.Loading -> {
                        /*showProgressLoading()*/
                    }
                }
            }


        }
    }

    override fun initData() {

        binding.btnSuccess.setOnClickListener {
            // 验证验证吗
            if (binding.codeView.code.isNullOrEmpty()) return@setOnClickListener
            // 邮箱登录
            if (isEmailLogin) {
                mViewModel.loginReq.value?.let {
                    it.userName = emailName
                    it.autoCode = binding.codeView.code
                    mViewModel.login()
                }
                return@setOnClickListener
            }
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
            // 验证码邮箱是否正确
            // emailName?.let { name -> mViewModel.verifyCode(it, name) }
        }
    }

    override fun onInput() {
    }

    companion object {
        // 是否是注册还是忘记密码?
        const val KEY_IS_REGISTER = "key_is_register"

        // 是否是用验证码登录
        const val KEY_IS_VERIFY = "key_is_verify"

        // 邮箱
        const val KEY_EMAIL_NAME = "key_email_name"

        // 验证码
        const val KEY_EMAIL_CODE = "key_code"
    }
}