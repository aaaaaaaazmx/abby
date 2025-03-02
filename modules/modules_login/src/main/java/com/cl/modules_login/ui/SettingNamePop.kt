package com.cl.modules_login.ui

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_login.R
import com.cl.modules_login.databinding.LoginEditProfilePopBinding
import com.lxj.xpopup.core.BottomPopupView

class SettingNamePop(context: Context,private val doneAction: ((String) -> Unit) ? = null): BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.login_edit_profile_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<LoginEditProfilePopBinding>(popupImplView)?.apply {
            executePendingBindings()
            // btn_success 点击之后根据对应设备id 保存 strainName
            // 然后跳转到主页
            btnSuccess.setOnClickListener {
                // todo 保存 strainName
                doneAction?.invoke(etEmail.text.toString())
                dismiss()
            }
        }
    }
}