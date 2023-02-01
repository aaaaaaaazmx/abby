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
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyEditProfilePopBinding
import com.cl.modules_my.service.HttpMyApiService
import com.google.gson.annotations.Until
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 设备修改名字、 修改属性弹窗
 */
class EditPlantProfilePop(
    context: Context,
    private val plantName: String? = null,
    private val onConfirmAction: ((planeName: String?, strainName: String?) -> Unit)? = null,
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_edit_profile_pop
    }

    private val userInfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    private var binding: MyEditProfilePopBinding? = null

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<MyEditProfilePopBinding>(popupImplView)?.apply {
            etEmail.setText(plantName)

            ivClose.setOnClickListener { dismiss() }

            tvDec.text = "Which account would you like to merge with ${userInfoBean?.email}?"

            etEmail.doAfterTextChanged {

                // btnSuccess.isEnabled = !(etCode.text.toString().isEmpty() || it.toString().isEmpty())
            }

            etCode.doAfterTextChanged {
                // btnSuccess.isEnabled = !(etEmail.text.toString().isEmpty() || it.toString().isEmpty())
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
                /*var email = etEmail.text.toString()
                // 如果带过来不是空的，但是确认的时候空了，则为默认
                if (plantName?.isNotEmpty() == true && email.isEmpty()) {
                    email = plantName
                }*/

                // 跳转到合并确认页面
                onConfirmAction?.invoke(etEmail.text.toString(), etCode.text.toString())
                dismiss()
            }
        }
    }


    /**
     * 发送验证码
     */
    private val service = ServiceCreators.create(HttpMyApiService::class.java)
    private suspend fun verifyEmail(email: String) {
        service.verifyEmail(email = email, type = "4").map {
            if (it.code != Constants.APP_SUCCESS) {
                if (it.msg == "not exist user") {
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
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                // todo 发送完毕。
                binding?.btnSendCode?.text = "Send successfully"
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.Main).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            logI(it.toString())
        }
    }
}