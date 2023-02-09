package com.cl.modules_my.pop

import android.content.Context
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
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyMergeAccountPopBinding
import com.cl.modules_my.service.HttpMyApiService
import com.google.gson.annotations.Until
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
                btnSuccess.isEnabled = !(etCode.text.toString().isEmpty() || it.toString().isEmpty())
            }

            etCode.doAfterTextChanged {
                btnSuccess.isEnabled = !(etEmail.text.toString().isEmpty() || it.toString().isEmpty())
            }

            ivClearEmail.setOnClickListener {
                etEmail.setText("")
            }
            ivClearCode.setOnClickListener {
                etCode.setText("")
            }

            btnSendCode.setOnClickListener {
                // 发送验证码
                lifecycleScope.launch {
                    verifyEmail(etEmail.text.toString())
                }
            }

            btnSuccess.setOnClickListener {
                // 需要验证验证码码
                lifecycleScope.launch {
                    verifyCode(etCode.text.toString(), etEmail.text.toString())
                }
            }
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
                    binding?.btnSendCode?.text = "Send successfully"
                }
                is Resource.DataError -> {
                    if (it.errorMsg == "not exist user") {
                        // todo 弹出没有这个弹窗的pop
                        XPopup.Builder(context)
                            .isDestroyOnDismiss(false)
                            .isDestroyOnDismiss(false)
                            .asCustom(
                                BaseCenterPop(context,
                                    content = "The account you entered does not exist", isShowCancelButton = false, onConfirmAction = {
                                    })
                            )
                            .show()
                    }
                }
                else -> {}
            }
            logI(it.toString())
        }
    }

    /**
     * 验证验证码
     */
    private suspend fun verifyCode(code: String, email: String) =
        service.verifyCode(code, email)
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