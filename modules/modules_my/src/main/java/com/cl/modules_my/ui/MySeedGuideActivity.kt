package com.cl.modules_my.ui

import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath
import com.cl.modules_my.databinding.MySeedGuideBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 选择种子之后的向导界面
 */
@Route(path = RouterPath.My.PAGE_MY_GUIDE_SEED)
@AndroidEntryPoint
class MySeedGuideActivity: BaseActivity<MySeedGuideBinding>() {
    override fun initView() {
    }

    override fun observe() {
    }

    override fun initData() {
        binding.btnSuccess.setOnClickListener { finish() }
    }
}