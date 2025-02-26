package com.cl.modules_login.ui

import android.app.LocaleManager
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import android.text.InputType
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.Constants.Global.KEY_REGISTER_OR_FORGET_PASSWORD
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.init.InitSdk
import com.cl.common_base.listener.TuYaDeviceUpdateReceiver
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.salt.AESCipher
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.databinding.ActivitySetPasswordBinding
import com.cl.modules_login.request.BindSourceEmailReq
import com.cl.modules_login.request.UpdatePwdReq
import com.cl.modules_login.request.UserRegisterReq
import com.cl.modules_login.response.LoginData
import com.cl.modules_login.viewmodel.SetPassWordViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thingclips.smart.android.user.api.IRegisterCallback
import com.thingclips.smart.android.user.api.IResetPasswordCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
import javax.inject.Inject


/**
 * 设置密码界面
 */
@AndroidEntryPoint
class OffLineSetPasswordActivity : BaseActivity<ActivitySetPasswordBinding>() {
    /**
     * 传过来的用户注册的必要参数
     */
    private val userRegisterBean by lazy {
        val bean =
            intent.getSerializableExtra(CreateAccountActivity.KEY_USER_REGISTER_BEAN) as? UserRegisterReq
        bean
    }

    /**
     * 第三方登录来源
     */
    private val thirdSource by lazy {
        intent.getStringExtra(LoginActivity.KEY_SOURCE)
    }

    /**
     * 第三方token
     */
    private val thirdToken by lazy {
        intent.getStringExtra(LoginActivity.KEY_THIRD_TOKEN)
    }

    /**
     * 邮箱号
     */
    private val emailName by lazy {
        val name = intent.getStringExtra(VerifyEmailActivity.KEY_EMAIL_NAME) ?: ""
        name
    }

    /**
     * 验证码
     */
    private val emailCode by lazy {
        val code = intent.getStringExtra(VerifyEmailActivity.KEY_EMAIL_CODE) ?: ""
        code
    }

    /**
     * 判断是否是注册还是忘记密码, 默认是注册
     */
    private val isRegisterOrForget by lazy {
        val isRegister = intent.getBooleanExtra(KEY_REGISTER_OR_FORGET_PASSWORD, true)
        isRegister
    }

    /**
     * 修改密码Bean
     */
    private val updatePwdReq by lazy {
        UpdatePwdReq()
    }

    @Inject
    lateinit var mViewModel: SetPassWordViewModel
    override fun initView() {
        ARouter.getInstance().inject(this)

        // 不能输入空格
        ViewUtils.setEditTextInputSpace(binding.etPassword)
    }

    private lateinit var userInfoBean: LoginData

    override fun observe() {
        mViewModel.apply {
            /**
             * 检查是否种植过
             */
            checkPlant.observe(this@OffLineSetPasswordActivity, resourceObserver {
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
                        data?.let { PlantCheckHelp().plantStatusCheck(this@OffLineSetPasswordActivity, it) }
                    }
                    finish()
                }
            })

            registerLoginLiveData.observe(this@OffLineSetPasswordActivity) {
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
                                val intent = Intent(this@OffLineSetPasswordActivity, TuYaDeviceUpdateReceiver::class.java)
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

            // 绑定第三方邮箱
            bindEmail.observe(this@OffLineSetPasswordActivity, resourceObserver {
                success {
                    when (thirdSource) {
                        "google" -> {
                            // 如果是谷歌登录
                            // 调用登录接口
                            mViewModel.loginReq.value?.let {
                                it.userName = emailName
                                it.password = AESCipher.aesEncryptString(
                                    binding.etPassword.text.toString(), AESCipher.KEY
                                )
                                it.source = "google"
                                it.autoToken = thirdToken
                                it.sourceUserId = AESCipher.aesEncryptString(
                                    Firebase.auth.currentUser?.email, AESCipher.KEY
                                )
                                mViewModel.login(currentLanguage = currentLanguage.uppercase())
                            }
                        }
                    }
                }
            })

            // 注册用户
            isVerifySuccess.observe(this@OffLineSetPasswordActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    if (!thirdSource.isNullOrEmpty()) {
                        // 如果是第三方登录，注册成之后，直接去登录。
                        when (thirdSource) {
                            "google" -> {
                                bindSourceEmail(
                                    BindSourceEmailReq(
                                        emailName, thirdSource, sourceUserId = AESCipher.aesEncryptString(
                                            Firebase.auth.currentUser?.email, AESCipher.KEY
                                        )
                                    )
                                )
                            }
                        }
                        return@success
                    }
                    startActivity(Intent(this@OffLineSetPasswordActivity, LoginActivity::class.java))
                    finish()
                }
                error { msg, code ->
                    hideProgressLoading()
                    msg?.let { it1 -> ToastUtil.shortShow(it1) }
                }
                loading {
                    showProgressLoading()
                }
            })

            // 忘记密码
            updatePwds.observe(this@OffLineSetPasswordActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    startActivity(Intent(this@OffLineSetPasswordActivity, LoginActivity::class.java))
                    finish()
                }
                error { msg, code ->
                    hideProgressLoading()
                    msg?.let { it1 -> ToastUtil.shortShow(it1) }
                }
                loading {
                    showProgressLoading()
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initData() {
        // 修改密码
        binding.btnSuccess.setOnClickListener {
            kotlin.runCatching {
                // 注册用户
                if (isRegisterOrForget) {
                    val callback = object : IRegisterCallback {
                        override fun onSuccess(user: User?) {
                            Toast.makeText(
                                this@OffLineSetPasswordActivity,
                                "Register success",
                                Toast.LENGTH_LONG
                            ).show()

                            // Clear cache
                            Prefs.clear()

                            // Navigate to User Func Navigation Page
                            val intent = Intent(this@OffLineSetPasswordActivity, OffLineLoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }

                        override fun onError(code: String?, error: String?) {
                            Toast.makeText(
                                this@OffLineSetPasswordActivity,
                                "Register error->$error",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    // Register by email
                    ThingHomeSdk.getUserInstance().registerAccountWithEmail(
                        "86",
                        emailName,
                        binding.etPassword.text.toString(),
                        emailCode,
                        callback
                    )
                } else {
                    val callback = object : IResetPasswordCallback {
                        override fun onSuccess() {
                            Toast.makeText(
                                this@OffLineSetPasswordActivity,
                                "Register success",
                                Toast.LENGTH_LONG
                            ).show()

                            // Clear cache
                            Prefs.clear()

                            // Navigate to User Func Navigation Page
                            val intent = Intent(this@OffLineSetPasswordActivity, OffLineLoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }

                        override fun onError(code: String?, error: String?) {
                            Toast.makeText(
                                this@OffLineSetPasswordActivity,
                                "Register error->$error",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    // Reset email password
                    ThingHomeSdk.getUserInstance().resetEmailPassword(
                        "86",
                        emailName,
                        emailCode,
                        binding.etPassword.text.toString(),
                        callback
                    )

                    /*// 修改密码
                    updatePwdReq.password = AESCipher.aesEncryptString(
                        binding.etPassword.text.toString(),
                        AESCipher.KEY
                    )
                    updatePwdReq.autoCode = emailCode
                    updatePwdReq.userEmail = emailName
                    updatePwdReq.language = currentLanguage.uppercase()
                    mViewModel.updatePwd(updatePwdReq)*/
                }
            }.onFailure {
                logE("btnSuccess.setOnClickListener < Build.VERSION_CODES.O Catch")
            }
        }

        binding.etPassword.doAfterTextChanged {
            val password = it.toString()
            // 超出20位，那么直接删除
            if ((it?.length ?: 0) > 20) {
                it?.delete(20, it.length)
            }

            // 文字长度大于8
            binding.tvLength.setTextColor(
                if (password.length >= 8) {
                    ResourcesCompat.getColor(
                        resources,
                        com.cl.common_base.R.color.mainColor,
                        theme
                    )
                } else {
                    ResourcesCompat.getColor(
                        resources,
                        com.cl.common_base.R.color.textError,
                        theme
                    )
                }
            )

            val letter = Pattern.matches(
                CONTAIN_LETTER_REGEX,
                password
            ) // 是否有中英文

            binding.tvLetter.setTextColor(
                if (letter) {
                    ResourcesCompat.getColor(
                        resources,
                        com.cl.common_base.R.color.mainColor,
                        theme
                    )
                } else {
                    ResourcesCompat.getColor(
                        resources,
                        com.cl.common_base.R.color.textError,
                        theme
                    )
                }
            )

            val number = Pattern.matches(
                CONTAIN_DIGIT_REGEX,
                password
            ) // 是否有数字

            binding.tvNumber.setTextColor(
                if (number) {
                    ResourcesCompat.getColor(
                        resources,
                        com.cl.common_base.R.color.mainColor,
                        theme
                    )
                } else {
                    ResourcesCompat.getColor(
                        resources,
                        com.cl.common_base.R.color.textError,
                        theme
                    )
                }
            )

            // 设置按钮是否可用
            binding.btnSuccess.isEnabled = password.length >= 8 && number && letter

            binding.flPsdState.setOnClickListener {
                if (mViewModel.passWordState.value == true) {
                    // 明文
                    binding.etPassword.inputType =
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                    binding.cbCheck.background =
                        ContextCompat.getDrawable(this@OffLineSetPasswordActivity, com.cl.common_base.R.mipmap.login_psd_open)
                } else {
                    // 密码
                    binding.etPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.cbCheck.background = ContextCompat.getDrawable(
                        this@OffLineSetPasswordActivity,
                        com.cl.common_base.R.mipmap.login_psd_close
                    )
                }
                binding.etPassword.setSelection(binding.etPassword.text.length)
                mViewModel.setPassWordState(!(mViewModel.passWordState.value ?: true))
            }

        }
    }

    // 获取系统语言
    private val currentLanguage by lazy {
        AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
    }

    companion object {
        const val CONTAIN_DIGIT_REGEX = ".*[0-9].*"
        const val CONTAIN_LETTER_REGEX = ".*[a-zA-Z].*"
    }
}