package com.cl.modules_login.ui

import android.app.LocaleManager
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.databinding.LoginEmailLoginActivityBinding
import com.cl.modules_login.viewmodel.ForgetPassWordViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 邮箱登录
 */
@AndroidEntryPoint
class EmailLoginActivity : BaseActivity<LoginEmailLoginActivityBinding>() {

    @Inject
    lateinit var mViewModel: ForgetPassWordViewModel

    // 邮箱地址
    private val emailAddress by lazy {
        intent.getStringExtra(KEY_FORGET_NAME)
    }

    override fun initView() {
        emailAddress?.let {
            binding.accountEditText.setText(emailAddress)
        }
    }

    override fun observe() {
        mViewModel.apply {
            updatePwds.observe(this@EmailLoginActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    // 跳转邮箱验证,传入忘记密码key = false
                    val intent =
                        Intent(this@EmailLoginActivity, VerifyEmailActivity::class.java)
                    intent.putExtra(VerifyEmailActivity.KEY_IS_REGISTER, false)
                    intent.putExtra(VerifyEmailActivity.KEY_IS_VERIFY, true)
                    intent.putExtra(
                        VerifyEmailActivity.KEY_EMAIL_NAME,
                        binding.accountEditText.text.toString()
                    )
                    startActivity(intent)
                }
            })
        }
    }

    override fun initData() {
        binding.rlBtn.setOnClickListener {
            // 调用接口
            val address = binding.accountEditText.text.toString()
            if (address.isEmpty()) {
                ToastUtil.shortShow(getString(com.cl.common_base.R.string.string_1697))
                return@setOnClickListener
            }
            if (address.contains("@") && address.contains(".")) {
                // 包含 "@" 和 "."
                mViewModel.updatePwd(currentLanguage = currentLanguage, address, "6")
            } else {
                // 不包含 "@" 或 "."
                ToastUtil.shortShow(getString(com.cl.common_base.R.string.string_1698))
                return@setOnClickListener
            }
        }
    }

    // 获取系统语言
    private val currentLanguage by lazy {
        var language = "en"
        val currentAppLocales: LocaleList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applicationContext.getSystemService(LocaleManager::class.java).getApplicationLocales("com.cl.abby")
        } else {
            // For lower Android versions, fallback to the legacy approach
            resources.configuration.locales
        }

        if (!currentAppLocales.isEmpty) {
            // Get the language from the first locale
            language = currentAppLocales[0].language
            // Try to find the index of the current language in the availableLanguage list
        }
        language
    }

    companion object {
        const val KEY_FORGET_NAME = "key_forget_name"
    }
}