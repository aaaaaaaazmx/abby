package com.cl.modules_login.ui

import android.content.Intent
import androidx.core.widget.doAfterTextChanged
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.init.InitSdk
import com.cl.common_base.util.EmailUtil
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.databinding.ActivityCreateAccountBinding
import com.cl.modules_login.request.UserRegisterReq
import com.cl.modules_login.ui.VerifyEmailActivity.Companion.KEY_EMAIL_NAME
import com.cl.modules_login.ui.VerifyEmailActivity.Companion.KEY_IS_REGISTER
import com.cl.modules_login.viewmodel.CreateAccountViewModel
import com.cl.modules_login.widget.PrivacyPop
import com.lxj.xpopup.XPopup
import com.tencent.bugly.proguard.ad
import com.tuya.bouncycastle.asn1.x509.X509ObjectIdentifiers.countryName
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable
import javax.inject.Inject


/**
 * 创建账号
 */

@AndroidEntryPoint
class CreateAccountActivity : BaseActivity<ActivityCreateAccountBinding>() {
    @Inject
    lateinit var mViewModel: CreateAccountViewModel

    private val privacyPop by lazy {
        PrivacyPop(
            context = this@CreateAccountActivity,
            onCancelAction = {
            },
            onConfirmAction = {
                // 点击同意隐私协议
                // 初始化SDK
                InitSdk.init()
                // 发送验证码
                mViewModel.verifyEmail(email = binding.etEmail.text.toString(), "1")
            },
            onTermUsAction = {
                // 跳转到使用条款H5
                val intent = Intent(this@CreateAccountActivity, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_WEB_URL, Constants.H5.PERSONAL_URL)
                intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Terms of Use")
                startActivity(intent)
            },
            onPrivacyAction = {
                // 跳转到隐私协议H5
                val intent = Intent(this@CreateAccountActivity, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_WEB_URL, Constants.H5.PRIVACY_POLICY_URL)
                intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Privacy Policy")
                startActivity(intent)
            }
        )
    }
    private val pop by lazy {
        XPopup.Builder(this@CreateAccountActivity)
            .hasStatusBar(false)
            .isDestroyOnDismiss(false)
            .asCustom(privacyPop)
    }

    /**
     * 用户注册Bean
     */
    private val userRegisterBean by lazy {
        UserRegisterReq()
    }

    override fun initView() {
        ARouter.getInstance().inject(this)

        // 获取列表
        mViewModel.getCountList()


    }

    override fun ActivityCreateAccountBinding.initBinding() {
        binding.viewModel = mViewModel
        binding.executePendingBindings()
    }

    override fun observe() {
        mViewModel.apply {
            /**
             * 获取国家列表
             */
            countList.observe(this@CreateAccountActivity) {
                when (it) {
                    is Resource.DataError -> {
                        hideProgressLoading()
                        it.errorMsg?.let { it1 -> ToastUtil.shortShow(it1) }
                    }
                    is Resource.Success -> {
                        hideProgressLoading()
                        logD("countList: ${it.data}")
                        // 获取第一个给用户展示
                        it.data?.let { list ->
                            if (list.isEmpty()) return@let
                            binding.tvCounttry.text = list.firstOrNull()?.countryName
                            userRegisterBean.country = list.firstOrNull()?.countryName
                            userRegisterBean.countryCode = list.firstOrNull()?.countryCode
                        }
                    }
                    is Resource.Loading -> {
                        showProgressLoading()
                    }
                }
            }


            /**
             * 发送验证码
             */
            sendStates.observe(this@CreateAccountActivity) {
                when (it) {
                    is Resource.DataError -> {
                        hideProgressLoading()
                        it.errorMsg?.let { it1 -> ToastUtil.shortShow(it1) }
                    }
                    is Resource.Success -> {
                        hideProgressLoading()
                        logD("sendStates: ${it.data}")

                        userRegisterBean.userName = binding.etEmail.text.toString()
                        // 跳转到发邮箱界面
                        val intent =
                            Intent(this@CreateAccountActivity, VerifyEmailActivity::class.java)
                        intent.putExtra(KEY_EMAIL_NAME, binding.etEmail.text.toString())
                        intent.putExtra(KEY_IS_REGISTER, true)
                        intent.putExtra(KEY_USER_REGISTER_BEAN, userRegisterBean)
                        startActivity(intent)
                    }
                    is Resource.Loading -> {
                        showProgressLoading()
                    }
                }
            }


        }
    }

    override fun initData() {
        binding.tvCounttry.setOnClickListener {
            logD("country: ${mViewModel.countList.value}")
            // 跳转选择国家列表
            val intent =
                Intent(this@CreateAccountActivity, SelectCountryActivity::class.java)
            intent.putExtra(
                COUNT_LIST,
                (mViewModel.countList.value?.data ?: mutableListOf()) as? Serializable
            )
            startActivityForResult(intent, CHOOSER_COUNT_CODE)
        }

        binding.etEmail.doAfterTextChanged {
            val email = it.toString()
            // 按钮是否可用
            binding.btnContinue.isEnabled =
                EmailUtil.isEmail(email) && !binding.tvCounttry.text.isNullOrEmpty()
        }

        binding.btnContinue.setOnClickListener {
            pop.show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSER_COUNT_CODE) {
            // 选中的国家的下标
            val position = data?.getIntExtra(SelectCountryActivity.KEY_POSITION, -1)
            if (position == -1) return
            val datas = position?.let { mViewModel.countList.value?.data?.get(it) }
            binding.tvCounttry.text = datas?.countryName
            userRegisterBean.country = datas?.countryName
            userRegisterBean.countryCode = datas?.countryCode
        }
    }


    companion object {
        const val COUNT_LIST = "count_List"
        const val CHOOSER_COUNT_CODE = 100

        const val KEY_USER_REGISTER_BEAN = "key_userregister_bean"
    }
}