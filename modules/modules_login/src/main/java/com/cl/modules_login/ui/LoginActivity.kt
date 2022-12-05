package com.cl.modules_login.ui

import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.RequiresApi
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.init.InitSdk
import com.cl.common_base.listener.TuYaDeviceUpdateReceiver
import com.cl.common_base.salt.AESCipher
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.BuildConfig
import com.cl.modules_login.databinding.ActivityLoginBinding
import com.cl.modules_login.response.LoginData
import com.cl.modules_login.viewmodel.LoginViewModel
import com.cl.modules_login.widget.LoginSelectEnvPop
import com.cl.modules_login.widget.PrivacyPop
import com.goldentec.android.tools.util.letMultiple
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 登录界面
 */
@Route(path = RouterPath.LoginRegister.PAGE_LOGIN)
@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    @Inject
    lateinit var mViewModel: LoginViewModel

    override fun initView() {
        ARouter.getInstance().inject(this)
        // 设置默认的账号
        binding.accountEditText.setText(mViewModel.account)

        binding.passwordEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.passwordEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                // 调用Login
                login()
            }
            false
        }

    }

    private val plantSix by lazy {
        XPopup.Builder(this@LoginActivity)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .dismissOnTouchOutside(false)
            .asCustom(LoginSelectEnvPop(this@LoginActivity))
    }

    private val pop by lazy {
        XPopup.Builder(this@LoginActivity)
            .dismissOnTouchOutside(false)
            .isDestroyOnDismiss(false)
    }

    private val privacyPop by lazy {
        PrivacyPop(
            context = this@LoginActivity,
            onCancelAction = {
            },
            onConfirmAction = {
                // 点击同意隐私协议
                Prefs.putBooleanAsync(Constants.PrivacyPolicy.KEY_PRIVACY_POLICY_IS_AGREE, true)
                checkLogin()
            },
            onTermUsAction = {
                // 跳转到使用条款H5
                val intent = Intent(this@LoginActivity, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_WEB_URL, Constants.H5.PERSONAL_URL)
                intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Terms of Use")
                startActivity(intent)
            },
            onPrivacyAction = {
                // 跳转到隐私协议H5
                val intent = Intent(this@LoginActivity, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_WEB_URL, Constants.H5.PRIVACY_POLICY_URL)
                intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Privacy Policy")
                startActivity(intent)
            }
        )
    }


    private lateinit var userInfoBean: LoginData
    override fun observe() {
        mViewModel.registerLoginLiveData.observe(this@LoginActivity) {
            when (it) {
                is Resource.Loading -> {
                     showProgressLoading()
                }
                is Resource.Success -> {
                    userInfoBean = it.data!!
                    // 保存当前的信息.
                    GSON.toJson(it.data)
                        ?.let { data ->
                            logI("LoginData: $data")
                            Prefs.putStringAsync(Constants.Login.KEY_LOGIN_DATA, data)
                        }
                    // 保存Token
                    it.data?.token?.let { it1 ->
                        Prefs.putStringAsync(
                            Constants.Login.KEY_LOGIN_DATA_TOKEN,
                            it1
                        )
                    }

                    // 保存账号密码
                    Prefs.putStringAsync(
                        Constants.Login.KEY_LOGIN_ACCOUNT,
                        binding.accountEditText.text.toString()
                    )
                    Prefs.putStringAsync(
                        Constants.Login.KEY_LOGIN_PSD, AESCipher.aesEncryptString(
                            binding.passwordEditText.text.toString(),
                            AESCipher.KEY
                        )
                    )

                    /**
                     * 登录涂鸦
                     */
                    mViewModel.tuYaLogin(
                        code = it.data?.tuyaCountryCode,
                        email = it.data?.email,
                        password = AESCipher.aesDecryptString(it.data?.tuyaPassword, AESCipher.KEY),
                        onRegisterReceiver = { devId ->
                            val intent =
                                Intent(this@LoginActivity, TuYaDeviceUpdateReceiver::class.java)
                            startService(intent)
                        },
                        onError = { code, error ->
                            hideProgressLoading()
                            error?.let { ToastUtil.shortShow(it) }
                        }
                    )
                }
                is Resource.DataError -> {
                    hideProgressLoading()
                    // 错误信息显示出来
                    when (it.errorCode) {
                        1007 -> {
                            binding.tvErrorInfo.text = "Incorrect password!"
                            ViewUtils.setVisible(binding.tvErrorInfo)
                        }
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
         * 检查是否种植过
         */
        mViewModel.checkPlant.observe(this@LoginActivity, resourceObserver {
            loading { showProgressLoading() }
            error { errorMsg, code ->
                hideProgressLoading()
                errorMsg?.let { msg -> ToastUtil.shortShow(msg) }
            }
            success {
                hideProgressLoading()
                kotlin.runCatching {
                    // 初始化SDK
                    InitSdk.init()
                    // 是否种植过
                    data?.let { PlantCheckHelp().plantStatusCheck(it) }
//                    when (userInfoBean.deviceStatus) {
//                        // 1-> 绑定设备、 2-> 未绑定设备
//                        "1" -> {
//
//                        }
//                        "2" -> {
//                            // 跳转绑定界面
//                            ARouter.getInstance()
//                                .build(RouterPath.PairConnect.PAGE_PLANT_CHECK)
//                                .navigation()
//                        }
//                        else -> {}
//                    }
                }
                finish()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initData() {
        // todo 测试专用
        binding.tvLogin.setOnClickListener {
            if (BuildConfig.DEBUG) {
                plantSix.show()
            }
        }

        binding.tvCreate.setOnClickListener {
            // 跳转到注册界面
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        binding.tvForget.setOnClickListener {
            // 跳转到忘记密码界面
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            intent.putExtra(ForgetPasswordActivity.KEY_FORGET_NAME, binding.accountEditText.text.toString())
            startActivity(intent)
        }

        binding.rlBtn.setOnClickListener {
            login()
        }


    }

    private fun login() {
        // 账号密码
        val account = binding.accountEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        // 直接劝退
        if (account.isNullOrEmpty()) {
            ToastUtil.shortShow(getString(R.string.login_account_empty))
            return
        }
        if (password.isNullOrEmpty()) {
            ToastUtil.shortShow(getString(R.string.login_password_empty))
            return
        }

        // 需要先同意隐私协议
        val privatePropertyAgree =
            Prefs.getBoolean(Constants.PrivacyPolicy.KEY_PRIVACY_POLICY_IS_AGREE)
        logI("$privatePropertyAgree")
        if (!privatePropertyAgree) {
            pop.asCustom(privacyPop).show()
            return
        }
        checkLogin()
    }

    private fun checkLogin() {
        // 账号密码
        val account = binding.accountEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        // 直接劝退
        // 直接劝退
        if (account.isNullOrEmpty()) {
            ToastUtil.shortShow("Account cannot be empty")
            return
        }
        if (password.isNullOrEmpty()) {
            ToastUtil.shortShow("Password cannot be empty")
            return
        }
        letMultiple(account, password) { ac, ps ->
            val value = mViewModel.loginReq.value
            value?.password = AESCipher.aesEncryptString(
                password,
                AESCipher.KEY
            )
            value?.userName = account
            mViewModel.login()
        }
    }
}