package com.cl.modules_login.ui

import android.app.LocaleManager
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import android.widget.Toast
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
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 忘记密码界面
 */
@AndroidEntryPoint
class OffLineForgetPasswordActivity : BaseActivity<ActivityForgetPasswordBinding>() {

    @Inject
    lateinit var mViewModel: ForgetPassWordViewModel

    private val mResetPasswordType = 3

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
    }

    override fun initData() {
        binding.btnSuccess.setOnClickListener {
            // 点击发送验证
            // 2 忘记密码
            // Get verification code code
            ThingHomeSdk.getUserInstance().sendVerifyCodeWithUserName(
                binding.etName.text.toString(),
                "",
                "86",
                mResetPasswordType,
                object : IResultCallback {
                    override fun onSuccess() {
                        Toast.makeText(
                            this@OffLineForgetPasswordActivity,
                            "Got validateCode",
                            Toast.LENGTH_LONG
                        ).show()

                        // 跳转邮箱验证,传入忘记密码key = false
                        val intent =
                            Intent(this@OffLineForgetPasswordActivity, OffLineVerifyEmailActivity::class.java)
                        intent.putExtra(VerifyEmailActivity.KEY_IS_REGISTER, false)
                        intent.putExtra(
                            VerifyEmailActivity.KEY_EMAIL_NAME,
                            binding.etName.text.toString()
                        )
                        startActivity(intent)
                    }

                    override fun onError(code: String?, error: String?) {
                        Toast.makeText(
                            this@OffLineForgetPasswordActivity,
                            "getValidateCode error->$error",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                })
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