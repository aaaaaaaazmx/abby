package com.cl.modules_login.ui

import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.modules_login.databinding.LoginItemPlantOneBinding
import dagger.hilt.android.AndroidEntryPoint

@Route(path = "/login/StartActivity")
@AndroidEntryPoint
class StartActivity: BaseActivity<LoginItemPlantOneBinding>() {
    override fun initView() {

        // todo 跳转到环境设置界面
        binding.ivStart.setSafeOnClickListener {

        }

    }

    override fun observe() {
    }

    override fun initData() {
    }
}