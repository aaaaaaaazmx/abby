package com.cl.modules_login.ui

import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.modules_login.databinding.LoginItemPlantOneBinding
import dagger.hilt.android.AndroidEntryPoint

@Route(path = RouterPath.LoginRegister.PAGE_PLANT_ONE)
@AndroidEntryPoint
class StartActivity: BaseActivity<LoginItemPlantOneBinding>() {
    override fun initView() {

        // todo 跳转到环境设置界面
        binding.ivStart.setSafeOnClickListener {
            // 弹出修改strainName的弹窗
            xpopup(this@StartActivity){
                asCustom(SettingNamePop(this@StartActivity)).show()
            }
        }

    }

    override fun observe() {
    }

    override fun initData() {
    }
}