package com.cl.modules_login.ui

import android.R.attr.editable
import android.content.Intent
import android.os.Build
import android.text.InputType
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.salt.AESCipher
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.R
import com.cl.modules_login.databinding.ActivitySetPasswordBinding
import com.cl.modules_login.request.UpdatePwdReq
import com.cl.modules_login.request.UserRegisterReq
import com.cl.modules_login.viewmodel.SetPassWordViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject


/**
 * 设置密码界面
 */
@AndroidEntryPoint
class SetPasswordActivity : BaseActivity<ActivitySetPasswordBinding>() {
    /**
     * 传过来的用户注册的必要参数
     */
    private val userRegisterBean by lazy {
        val bean =
            intent.getSerializableExtra(CreateAccountActivity.KEY_USER_REGISTER_BEAN) as? UserRegisterReq
        bean
    }

    /**
     * 邮箱号
     */
    private val emailName by lazy {
        val name = intent.getStringExtra(VerifyEmailActivity.KEY_EMAIL_NAME) ?: ""
        name
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

    override fun observe() {
        mViewModel.apply {
            // 注册用户
            isVerifySuccess.observe(this@SetPasswordActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    startActivity(Intent(this@SetPasswordActivity, LoginActivity::class.java))
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
            updatePwds.observe(this@SetPasswordActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    startActivity(Intent(this@SetPasswordActivity, LoginActivity::class.java))
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
                    userRegisterBean?.password = AESCipher.aesEncryptString(
                        binding.etPassword.text.toString(),
                        AESCipher.KEY
                     )
                    userRegisterBean?.let { bean -> mViewModel.registerAccount(bean) }
                } else {
                    // 修改密码
                    updatePwdReq.password = AESCipher.aesEncryptString(
                        binding.etPassword.text.toString(),
                        AESCipher.KEY
                    )
                    updatePwdReq.userEmail = emailName
                    mViewModel.updatePwd(updatePwdReq)
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
                        ContextCompat.getDrawable(this@SetPasswordActivity, R.mipmap.login_psd_open)
                } else {
                    // 密码
                    binding.etPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.cbCheck.background = ContextCompat.getDrawable(
                        this@SetPasswordActivity,
                        R.mipmap.login_psd_close
                    )
                }
                binding.etPassword.setSelection(binding.etPassword.text.length)
                mViewModel.setPassWordState(!(mViewModel.passWordState.value ?: true))
            }

        }
    }

    companion object {
        // 注册或者是忘记密码  true 是注册 false忘记密码
        const val KEY_REGISTER_OR_FORGET_PASSWORD = "key_register_or_forget_password"
        const val CONTAIN_DIGIT_REGEX = ".*[0-9].*"
        const val CONTAIN_LETTER_REGEX = ".*[a-zA-Z].*"
    }
}