package com.cl.modules_login.ui

import android.content.Intent
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatDelegate
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.containsIgnoreCase
import com.cl.common_base.ext.letMultiple
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.listener.TuYaDeviceUpdateReceiver
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.report.Reporter
import com.cl.common_base.salt.AESCipher
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.BuildConfig
import com.cl.modules_login.databinding.ActivityLoginBinding
import com.cl.modules_login.response.OffLineDeviceBean
import com.cl.modules_login.ui.CreateAccountActivity.Companion.KEY_USER_REGISTER_BEAN
import com.cl.modules_login.ui.VerifyEmailActivity.Companion.KEY_EMAIL_NAME
import com.cl.modules_login.ui.VerifyEmailActivity.Companion.KEY_IS_REGISTER
import com.cl.modules_login.viewmodel.LoginViewModel
import com.cl.modules_login.widget.LoginSelectEnvPop
import com.cl.modules_login.widget.PrivacyPop
import com.lxj.xpopup.XPopup
import com.thingclips.smart.android.user.api.ILoginCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.camera.middleware.p2p.ThingSmartNvrP2P
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@Route(path = RouterPath.LoginRegister.PAGE_LOGIN)
@AndroidEntryPoint
class OffLineLoginActivity : BaseActivity<ActivityLoginBinding>() {
    @Inject
    lateinit var mViewModel: LoginViewModel

    private val devId by lazy {
        Prefs.getString(Constants.Tuya.KEY_DEVICE_ID)
    }

    override fun initView() {
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
        binding.accountEditText.isFocusableInTouchMode = true
        binding.accountEditText.requestFocus();

        // 更新小组件
        // 只要每次进入到这个界面，那么就更新一次小组件。
        // 判断是否有无token
        if (Prefs.getString(Constants.Login.KEY_LOGIN_DATA_TOKEN, "").isNotEmpty()) {
            updateWidget(this@OffLineLoginActivity)
        }


        binding.rlBtn.setSafeOnClickListener {
            login()
        }
        binding.tvForget.setSafeOnClickListener {
            val intent = Intent(this, OffLineForgetPasswordActivity::class.java)
            intent.putExtra(ForgetPasswordActivity.KEY_FORGET_NAME, binding.accountEditText.text.toString())
            startActivity(intent)
        }
        binding.tvCreate.setSafeOnClickListener {
            // 跳转到注册界面
            val intent = Intent(this, OffLineCreateAccountActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login() { // 账号密码
        if (BuildConfig.DEBUG) {
            when (binding.accountEditText.text.toString()) {
                "1" -> {
                    binding.accountEditText.setText("xianlin.zhang@baypacclub.com")
                    binding.passwordEditText.setText("zxl123456")
                    login()
                    return
                }

                "22" -> {
                    binding.accountEditText.setText("17680319466")
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
                    binding.passwordEditText.setText("lll111111")
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

    private val privacyPop by lazy {
        PrivacyPop(context = this@OffLineLoginActivity, onCancelAction = {}, onConfirmAction = { // 点击同意隐私协议
            Prefs.putBooleanAsync(Constants.PrivacyPolicy.KEY_PRIVACY_POLICY_IS_AGREE, true)
            checkLogin()
        }, onTermUsAction = { // 跳转到使用条款H5
            val localLanguage = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
            val intent = Intent(this@OffLineLoginActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, String.format(Constants.H5.PERSONAL_URL, localLanguage))
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, getString(R.string.about_terms))
            startActivity(intent)
        }, onPrivacyAction = { // 跳转到隐私协议H5
            val localLanguage = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
            val intent = Intent(this@OffLineLoginActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_WEB_URL, String.format(Constants.H5.PRIVACY_POLICY_URL, localLanguage))
            intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, getString(R.string.about_policy))
            startActivity(intent)
        })
    }

    private val plantSix by lazy {
        XPopup.Builder(this@OffLineLoginActivity).isDestroyOnDismiss(false).enableDrag(false).dismissOnTouchOutside(false).asCustom(LoginSelectEnvPop(this@OffLineLoginActivity))
    }

    private val pop by lazy {
        XPopup.Builder(this@OffLineLoginActivity).dismissOnTouchOutside(false).isDestroyOnDismiss(false)
    }

    private fun checkLogin() { // 账号密码
        val account = binding.accountEditText.text.toString()
        val password = binding.passwordEditText.text.toString() // 直接劝退
        // 直接劝退
        if (account.isEmpty()) {
            ToastUtil.shortShow(getString(com.cl.common_base.R.string.string_1687))
            return
        }
        if (password.isEmpty()) {
            ToastUtil.shortShow(getString(com.cl.common_base.R.string.string_1688))
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

                // 直接登录
                mViewModel.tuYaLoginForOffLine(code = "86", email = binding.accountEditText.text.toString(), password = binding.passwordEditText.text.toString(), onSuccess = { user ->
                    // 检查权限
                    PermissionHelp().checkConnectForTuYaBle(this@OffLineLoginActivity, object : PermissionHelp.OnCheckResultListener {
                        override fun onResult(result: Boolean) {
                            if (!result) return
                            // 保存账号密码。
                            Prefs.putStringAsync(Constants.Login.KEY_LOGIN_ACCOUNT, binding.accountEditText.text.toString())
                            Prefs.putStringAsync(Constants.Login.KEY_LOGIN_PSD, binding.passwordEditText.text.toString())
                            if (user?.deviceList?.size == 0) {
                                // 跳转到添加设备界面
                                val intent = Intent(this@OffLineLoginActivity, BindDeviceActivity::class.java)
                                startActivity(intent)
                                finish()
                                return
                            }
                            val devList = user?.deviceList?.filter {
                                it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1) || it.productId.containsIgnoreCase(
                                    OffLineDeviceBean.DEVICE_VERSION_OG
                                ) || it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_OG_BLACK) || it.productId.containsIgnoreCase(
                                    OffLineDeviceBean.DEVICE_VERSION_OG_PRO
                                ) || it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1_PRO) || it.productId.containsIgnoreCase(
                                    OffLineDeviceBean.DEVICE_VERSION_O1_SOIL
                                )
                            }
                            // 判断当前设备只有一个，
                            if (null == user) {
                                // 跳转到添加设备界面
                                val intent = Intent(this@OffLineLoginActivity, BindDeviceActivity::class.java)
                                startActivity(intent)
                            } else if ((devList?.size ?: 0) >= 1) {
                                // 如果当前设备失效了.就选择机器, 反之就直接跳转
                                if (null == devList?.firstOrNull { it.devId == devId }) {
                                    val currentDevice = user.deviceList?.firstOrNull {
                                        it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1) || it.productId.containsIgnoreCase(
                                            OffLineDeviceBean.DEVICE_VERSION_OG
                                        ) || it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_OG_BLACK) || it.productId.containsIgnoreCase(
                                            OffLineDeviceBean.DEVICE_VERSION_OG_PRO
                                        ) || it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1_PRO) || it.productId.containsIgnoreCase(
                                            OffLineDeviceBean.DEVICE_VERSION_O1_SOIL
                                        )
                                    }
                                    Prefs.putStringAsync(
                                        Constants.Tuya.KEY_DEVICE_ID,
                                        currentDevice?.devId.toString()
                                    )
                                }
                                val intent = Intent(
                                    this@OffLineLoginActivity, OffLineMainActivity::class.java
                                )
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }  else {
                                // 跳转到添加设备界面
                                val intent = Intent(this@OffLineLoginActivity, BindDeviceActivity::class.java)
                                startActivity(intent)
                            }
                            finish()
                        }
                    })
                }, onRegisterReceiver = { devId ->
                    val intent = Intent(this@OffLineLoginActivity, TuYaDeviceUpdateReceiver::class.java)
                    startService(intent)
                }, onError = { code, error ->
                    error?.let { ToastUtil.shortShow(it) }
                })
            }
        }
    }


    override fun observe() {
    }

    override fun initData() {
    }
}