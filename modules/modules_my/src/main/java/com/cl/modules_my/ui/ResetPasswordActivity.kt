package com.cl.modules_my.ui

import android.os.Build
import android.text.InputType
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.salt.AESCipher
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyResetPasswordBinding
import com.cl.modules_my.request.ResetPwdReq
import com.cl.modules_my.viewmodel.ResetPassWordViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * 重置密码
 */
@AndroidEntryPoint
class ResetPasswordActivity : BaseActivity<MyResetPasswordBinding>() {


    @Inject
    lateinit var mViewModel: ResetPassWordViewModel
    override fun initView() {
        ARouter.getInstance().inject(this)

        // 不能输入空格
        ViewUtils.setEditTextInputSpace(binding.etPassword)
    }

    override fun observe() {
        mViewModel.apply {
            resetPwd.observe(this@ResetPasswordActivity, resourceObserver {
                success {
                    hideProgressLoading()
                    finish()
                }
                loading {
                    showProgressLoading()
                }

                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initData() {
        // 修改密码
        binding.btnSuccess.setOnClickListener {
            kotlin.runCatching {
                mViewModel.resetPwd(ResetPwdReq(newPassword = binding.etPassword.text.toString()))
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
                        ContextCompat.getDrawable(this@ResetPasswordActivity, com.cl.common_base.R.mipmap.login_psd_open)
                } else {
                    // 密码
                    binding.etPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.cbCheck.background = ContextCompat.getDrawable(
                        this@ResetPasswordActivity,
                        com.cl.common_base.R.mipmap.login_psd_close
                    )
                }
                binding.etPassword.setSelection(binding.etPassword.text.length)
                mViewModel.setPassWordState(!(mViewModel.passWordState.value ?: true))
            }

        }
    }

    companion object {
        const val CONTAIN_DIGIT_REGEX = ".*[0-9].*"
        const val CONTAIN_LETTER_REGEX = ".*[a-zA-Z].*"
    }
}