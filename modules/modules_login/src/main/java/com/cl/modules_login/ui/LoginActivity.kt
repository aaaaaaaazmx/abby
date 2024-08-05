package com.cl.modules_login.ui

import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import chip.setuppayload.SetupPayloadParser.InvalidEntryCodeFormatException
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.TuYaInfo
import com.cl.common_base.bean.UserinfoBean
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
import com.cl.common_base.ext.letMultiple
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.util.login.GoogleLoginHelper
import com.cl.common_base.util.login.GoogleLoginHelper.Companion.REQ_ONE_TAP
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.luck.picture.lib.utils.ToastUtils
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

    private val googleHelp by lazy {
        GoogleLoginHelper(this@LoginActivity)
    }

    override fun initView() {
        ARouter.getInstance().inject(this) // 设置默认的账号
        binding.accountEditText.setText(mViewModel.account)
        ServiceCreators.TokenCache.token = null
        Prefs.removeKey(Constants.Login.KEY_LOGIN_DATA_TOKEN)
        binding.passwordEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.passwordEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) { // 调用Login
                login()
            }
            false
        }

        // 获取焦点才能弹出密码提示框
        binding.accountEditText.isFocusable = true;
        binding.accountEditText.isFocusableInTouchMode = true;
        binding.accountEditText.requestFocus();

        // 更新小组件
        // 只要每次进入到这个界面，那么就更新一次小组件。
        // 判断是否有无token
        if (Prefs.getString(Constants.Login.KEY_LOGIN_DATA_TOKEN, "").isNotEmpty()) {
            updateWidget(this@LoginActivity)
        }
    }

    private val plantSix by lazy {
        XPopup.Builder(this@LoginActivity).isDestroyOnDismiss(false).enableDrag(false).dismissOnTouchOutside(false).asCustom(LoginSelectEnvPop(this@LoginActivity))
    }

    private val pop by lazy {
        XPopup.Builder(this@LoginActivity).dismissOnTouchOutside(false).isDestroyOnDismiss(false)
    }

    private val privacyPop by lazy {
        PrivacyPop(context = this@LoginActivity, onCancelAction = {}, onConfirmAction = { // 点击同意隐私协议
            Prefs.putBooleanAsync(Constants.PrivacyPolicy.KEY_PRIVACY_POLICY_IS_AGREE, true)
            checkLogin()
        }, onTermUsAction = { // 跳转到使用条款H5
            val intent = Intent(this@LoginActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, Constants.H5.PERSONAL_URL)
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Terms of Use")
            startActivity(intent)
        }, onPrivacyAction = { // 跳转到隐私协议H5
            val intent = Intent(this@LoginActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, Constants.H5.PRIVACY_POLICY_URL)
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Privacy Policy")
            startActivity(intent)
        })
    }


    private lateinit var userInfoBean: LoginData
    override fun observe() {
        mViewModel.noBindEmail.observe(this@LoginActivity) {
            if (it == false) return@observe
            when(mViewModel.thirdSource.value) {
                "google" -> {
                    // 重新走一遍创建账号流程
                    val intent = Intent(this@LoginActivity, CreateAccountActivity::class.java)
                    intent.putExtra(KEY_SOURCE, mViewModel.thirdSource.value)
                    intent.putExtra(KEY_THIRD_TOKEN, mViewModel.thirdToken.value)
                    startActivity(intent)
                }
                else -> {
                    hideProgressLoading()
                    ToastUtil.shortShow("not exist user")
                }
            }
        }

        mViewModel.registerLoginLiveData.observe(this@LoginActivity) {
            when (it) {
                is Resource.Loading -> {
                    showProgressLoading()
                }

                is Resource.Success -> {
                    userInfoBean = it.data!! // 保存当前的信息.
                    val tuYaInfo = TuYaInfo(
                        tuyaCountryCode = userInfoBean.tuyaCountryCode,
                        tuyaPassword = userInfoBean.tuyaPassword,
                        tuyaUserId = userInfoBean.tuyaUserId,
                        tuyaUserType = userInfoBean.tuyaUserType
                    )
                    GSON.toJsonInBackground(tuYaInfo) { tuyainfos ->
                        logI("tuYaInfoL: $tuyainfos")
                        Prefs.putStringAsync(Constants.Login.KEY_TU_YA_INFO, tuyainfos)
                    }
                    GSON.toJsonInBackground(it.data) { data ->
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
                    Prefs.putStringAsync(
                        Constants.Login.KEY_LOGIN_ACCOUNT, binding.accountEditText.text.toString()
                    )
                    Prefs.putStringAsync(
                        Constants.Login.KEY_LOGIN_PSD, AESCipher.aesEncryptString(
                            binding.passwordEditText.text.toString(), AESCipher.KEY
                        )
                    ) // 获取InterCome同步数据
                    mViewModel.getInterComeData()
                }

                is Resource.DataError -> {
                    hideProgressLoading() // 错误信息显示出来
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
         * InterCome同步数据
         */
        mViewModel.getInterComeData.observe(this@LoginActivity, resourceObserver {
            error { errorMsg, code ->
                /**
                 * 登录涂鸦
                 */
                mViewModel.userDetail()
            }
            success {
                mViewModel.userDetail()
            }
        })

        mViewModel.userDetail.observe(this@LoginActivity, resourceObserver {
            error { errorMsg, code ->
                ToastUtil.shortShow(errorMsg)
            }
            success {
                /**
                 * 登录涂鸦
                 */
                val it = mViewModel.registerLoginLiveData.value
                mViewModel.tuYaLogin(
                    map = mapOf(),
                    interComeUserId = it?.data?.externalId,
                    userInfo = data,
                    deviceId = it?.data?.deviceId,
                    code = it?.data?.tuyaCountryCode,
                    email = it?.data?.email,
                    password = AESCipher.aesDecryptString(it?.data?.tuyaPassword ?: "", AESCipher.KEY),
                    onRegisterReceiver = { devId ->
                        val intent = Intent(this@LoginActivity, TuYaDeviceUpdateReceiver::class.java)
                        startService(intent)
                    },
                    onError = { code, error ->
                        hideProgressLoading()
                        error?.let { ToastUtil.shortShow(it) }
                    })
            }
        })

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
                kotlin.runCatching { // 初始化SDK
                    InitSdk.init() // 是否种植过
                    // 保存账号
                    Prefs.putString(Constants.USER_NAME, binding.accountEditText.text.toString())
                    data?.let { PlantCheckHelp().plantStatusCheck(this@LoginActivity, it) }
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
    override fun initData() { // todo 测试专用
        binding.tvLogin.setOnClickListener {
            if (BuildConfig.DEBUG) {
                plantSix.show()
            }
        }

        binding.tvCreate.setOnClickListener { // 跳转到注册界面
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        binding.tvForget.setOnClickListener { // 跳转到忘记密码界面
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            intent.putExtra(ForgetPasswordActivity.KEY_FORGET_NAME, binding.accountEditText.text.toString())
            startActivity(intent)
        }

        // 邮箱登录
        binding.tvEmailLogin.setOnClickListener {
            val intent = Intent(this, EmailLoginActivity::class.java)
            intent.putExtra(EmailLoginActivity.KEY_FORGET_NAME, binding.accountEditText.text.toString())
            startActivity(intent)
        }

        binding.rlBtn.setOnClickListener {
            login()
        }

        binding.ivGoogle.setOnClickListener {
            // 谷歌登录
            googleHelp.login()
        }

        binding.ivSms.setOnClickListener {
            mViewModel.setThirdSource("sms")
            // sms登录
            val intent = Intent(this@LoginActivity, CreateAccountActivity::class.java)
            intent.putExtra(KEY_SOURCE, mViewModel.thirdSource.value ?: "sms")
            startActivity(intent)
        }

        binding.ivFacebook.setOnClickListener {

        }

    }

    private fun login() { // 账号密码
        if (BuildConfig.DEBUG) {
            when(binding.accountEditText.text.toString()) {
                "1" -> {
                    binding.accountEditText.setText("xianlin.zhang@baypacclub.com")
                    binding.passwordEditText.setText("zxl123456")
                    login()
                    return
                }
                "22" -> {
                    binding.accountEditText.setText("m17680319466@163.com")
                    binding.passwordEditText.setText("zxl123456")
                    login()
                    return
                }

                "2" -> {
                    binding.accountEditText.setText("448477235@qq.com")
                    binding.passwordEditText.setText("zxl123456")
                    login()
                    return
                }
                "3" -> {
                    binding.accountEditText.setText("1286227844@qq.com")
                    binding.passwordEditText.setText("c12345678")
                    login()
                    return
                }
                "4" -> {
                    binding.accountEditText.setText("2192292392@qq.com")
                    binding.passwordEditText.setText("lll000000")
                    login()
                    return
                }
            }
        }
        val account = binding.accountEditText.text.toString()
        val password = binding.passwordEditText.text.toString() // 直接劝退
        if (account.isEmpty()) {
            ToastUtil.shortShow(getString(R.string.login_account_empty))
            return
        }
        if (password.isEmpty()) {
            ToastUtil.shortShow(getString(R.string.login_password_empty))
            return
        }

        // 需要先同意隐私协议
        val privatePropertyAgree = Prefs.getBoolean(Constants.PrivacyPolicy.KEY_PRIVACY_POLICY_IS_AGREE)
        logI("$privatePropertyAgree")
        if (!privatePropertyAgree) {
            pop.asCustom(privacyPop).show()
            return
        }
        checkLogin()
    }

    private fun checkLogin() { // 账号密码
        val account = binding.accountEditText.text.toString()
        val password = binding.passwordEditText.text.toString() // 直接劝退
        // 直接劝退
        if (account.isEmpty()) {
            ToastUtil.shortShow("Account cannot be empty")
            return
        }
        if (password.isEmpty()) {
            ToastUtil.shortShow("Password cannot be empty")
            return
        }
        letMultiple(account, password) { ac, ps ->
            mViewModel.loginReq.value?.let {
                it.userName = account
                it.password = AESCipher.aesEncryptString(
                    password, AESCipher.KEY
                )
                it.source = null
                it.autoToken = null
                it.sourceUserId = null
                mViewModel.login()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = googleHelp.getOneTapClient().getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    logI("user: ${credential.id}")

                    when {
                        idToken != null -> { // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            logI("Got ID token.")

                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            Firebase.auth.signInWithCredential(firebaseCredential).addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                                    logI("signInWithCredential:success")
                                    val user = Firebase.auth.currentUser
                                    logI("user: ${user?.uid}")
                                    logI("user: ${user?.email}")
                                    logI("user: ${user?.providerId}")
                                    logI("user: ${user?.tenantId}")
                                    mViewModel.setThirdSource("google")
                                    mViewModel.setThirdToken(idToken)
                                    // 调用登录接口
                                    mViewModel.loginReq.value?.let {
                                        it.userName = null
                                        it.password = null
                                        it.source = "google"
                                        it.autoToken = idToken
                                        it.sourceUserId = AESCipher.aesEncryptString(
                                            user?.email, AESCipher.KEY
                                        )
                                        mViewModel.login()
                                    }

                                } else { // If sign in fails, display a message to the user.
                                    logI("signInWithCredential:failure ${task.exception}")
                                    logI("user: login fail")
                                    ToastUtil.shortShow(task.exception?.localizedMessage)
                                }
                            }
                        }

                        else -> { // Shouldn't happen.
                            logI("No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            // todo 需要保存，不然重复关闭多次会导致24小时封号
                            logI("One-tap dialog was closed.")
                            // showOneTapUI = false
                        }

                        CommonStatusCodes.NETWORK_ERROR -> {
                            ToastUtil.shortShow("One-tap encountered a network error.")
                        }

                        else -> {
                            ToastUtil.shortShow(e.localizedMessage)
                            logI("Couldn't get credential from result. (${e.localizedMessage})")
                        }
                    }
                }
            }
        }
    }

    companion object {
        // 来源
        const val KEY_SOURCE = "source"
        // 第三方登录token
        const val KEY_THIRD_TOKEN = "third_token"
    }
}