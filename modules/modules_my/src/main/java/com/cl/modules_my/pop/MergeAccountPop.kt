package com.cl.modules_my.pop

import android.content.Context
import android.os.CountDownTimer
import android.text.Editable
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyMergeAccountPopBinding
import com.cl.modules_my.service.HttpMyApiService
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


/**
 * 合并账号弹窗
 */
class MergeAccountPop(
    context: Context,
    private val onConfirmAction: ((email: String?, code: String?) -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_merge_account_pop
    }

    private val userInfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    private var binding: MyMergeAccountPopBinding? = null

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<MyMergeAccountPopBinding>(popupImplView)?.apply {
            ivClose.setOnClickListener { dismiss() }
            tvDec.text = "Which account would you like to merge with ${userInfoBean?.email}?"

            etEmail.doAfterTextChanged {
                btnIsEnabel()
            }

            etCode.doAfterTextChanged {
                btnSuccess.isEnabled = !(etEmail.text.toString().isEmpty() || it.toString().isEmpty())
            }

            ivClearEmail.setOnClickListener {
                etEmail.setText("")
                btnIsEnabel()
            }
            ivClearCode.setOnClickListener {
                etCode.setText("")
            }

            btnSendCode.setOnClickListener {
                if (etEmail.text.toString().isEmpty()) return@setOnClickListener
                // 发送验证码
                lifecycleScope.launch {
                    verifyEmail(etEmail.text.toString())
                }
            }

            btnSuccess.setOnClickListener {
                if (etCode.text.toString().isEmpty() && etEmail.text.toString().isEmpty()) {
                    return@setOnClickListener
                }
                // 需要验证验证码码
                lifecycleScope.launch {
                    userInfoBean?.email?.let { it1 -> verifyCode(etCode.text.toString(), it1, etEmail.text.toString()) }
                }
            }
        }
    }

    private fun MyMergeAccountPopBinding.btnIsEnabel() {
        btnSuccess.isEnabled = !(etCode.text.toString().isEmpty() || etEmail.text.toString().isEmpty())
        if (etEmail.text.toString().isEmpty()) {
            btnSendCode.setBackgroundColor(
                ContextCompat.getColor(
                    context, com.cl.common_base.R.color.buttonGray
                )
            )
            btnSendCode.setTextColor(
                ContextCompat.getColor(
                    context, com.cl.common_base.R.color.black
                )
            )
        } else {
            btnSendCode.setBackgroundColor(
                ContextCompat.getColor(
                    context, com.cl.common_base.R.color.mainColor
                )
            )
            btnSendCode.setTextColor(
                ContextCompat.getColor(
                    context, com.cl.common_base.R.color.white
                )
            )
        }
    }


    /**
     * 发送验证码
     */
    private val service = ServiceCreators.create(HttpMyApiService::class.java)
    private suspend fun verifyEmail(email: String) {
        service.verifyEmail(email = userInfoBean?.email.toString(), type = "4", mergeEmail = email).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            when (it) {
                is Resource.Success -> {
                    // todo 发送完毕。
                    countDownTime()
                }
                is Resource.DataError -> {
                    // todo 弹出没有这个弹窗的pop
                    XPopup.Builder(context)
                        .isDestroyOnDismiss(false)
                        .isDestroyOnDismiss(false)
                        .asCustom(
                            BaseCenterPop(context,
                                content = it.errorMsg, isShowCancelButton = false, onConfirmAction = {
                                })
                        )
                        .show()
                }
                else -> {
                }
            }
            logI(it.toString())
        }
    }

    //用安卓自带的CountDownTimer实现
    private val mTimer: CountDownTimer = object : CountDownTimer((60 * 1000).toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding?.btnSendCode?.text = (millisUntilFinished / 1000).toString()
        }

        override fun onFinish() {
            binding?.btnSendCode?.isEnabled = true
            binding?.btnSendCode?.text = "Send now"
            cancel()
        }
    }

    // 发送验证码， 倒计时
    private fun countDownTime() {
        mTimer.start()
        binding?.btnSendCode?.isEnabled = false
    }

    override fun onDismiss() {
        super.onDismiss()
        mTimer.cancel()
    }

    /**
     * 验证验证码
     */
    private suspend fun verifyCode(code: String, email: String, mergeEmail: String? = null) =
        service.verifyCode(code, email, mergeEmail)
            .map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code,
                        it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }
            .flowOn(Dispatchers.IO)
            .onStart {
                emit(Resource.Loading())
            }
            .catch {
                logD("verifyEmail: catch $it")
                emit(
                    Resource.DataError(
                        -1,
                        "$it"
                    )
                )
            }.collectLatest {
                when (it) {
                    is Resource.Success -> {
                        // 跳转到合并确认页面
                        onConfirmAction?.invoke(binding?.etEmail?.text.toString(), binding?.etCode?.text.toString())
                        dismiss()
                    }
                    is Resource.DataError -> {
                        // 弹出错误提示框
                        ToastUtil.shortShow(it.errorMsg)
                    }
                    else -> {}
                }
                logI("verifyCode: $it")
            }
}