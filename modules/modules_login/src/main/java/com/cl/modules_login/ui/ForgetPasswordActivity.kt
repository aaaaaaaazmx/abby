package com.cl.modules_login.ui

import android.app.LocaleManager
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doAfterTextChanged
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.util.EmailUtil
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.databinding.ActivityForgetPasswordBinding
import com.cl.modules_login.viewmodel.ForgetPassWordViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 忘记密码界面
 */
@AndroidEntryPoint
class ForgetPasswordActivity : BaseActivity<ActivityForgetPasswordBinding>() {

    @Inject
    lateinit var mViewModel: ForgetPassWordViewModel

    // 账号名字
    private val account by lazy {
        intent.getStringExtra(KEY_FORGET_NAME)
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        ViewUtils.setEditTextInputSpace(binding.etName)
        // 带过来的名字
        binding.etName.setText(account)
        binding.btnSuccess.isEnabled = EmailUtil.isEmail(binding.etName.text.toString())
    }

    override fun observe() {
        mViewModel.apply {
            // 发送验证码
            updatePwds.observe(this@ForgetPasswordActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    // 跳转邮箱验证,传入忘记密码key = false
                    val intent =
                        Intent(this@ForgetPasswordActivity, VerifyEmailActivity::class.java)
                    intent.putExtra(VerifyEmailActivity.KEY_IS_REGISTER, false)
                    intent.putExtra(
                        VerifyEmailActivity.KEY_EMAIL_NAME,
                        binding.etName.text.toString()
                    )
                    startActivity(intent)
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

    // 获取系统语言
    private val currentLanguage by lazy {
        AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
    }

    override fun initData() {
        binding.btnSuccess.setOnClickListener {
            // 点击发送验证
            // 2 忘记密码
            mViewModel.updatePwd(currentLanguage = currentLanguage.uppercase(), binding.etName.text.toString(), "2")
        }

        // 输入监听
        binding.etName.doAfterTextChanged {
            val name = it.toString()
            binding.btnSuccess.isEnabled = EmailUtil.isEmail(name)
        }
        // 设置默认账号
//        binding.etName.setText(mViewModel.account)
    }

    companion object {
        const val KEY_FORGET_NAME = "key_forget_name"
    }
}