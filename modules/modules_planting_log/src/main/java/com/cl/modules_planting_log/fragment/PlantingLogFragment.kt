package com.cl.modules_planting_log.fragment

import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.constants.RouterPath
import com.cl.modules_planting_log.databinding.PlantingMainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 种植日志主页
 */
@Route(path = RouterPath.Plant.PAGE_PLANT)
@AndroidEntryPoint
class PlantingLogFragment  : BaseFragment<PlantingMainFragmentBinding>() {
    override fun PlantingMainFragmentBinding.initBinding() {

    }

    override fun initView(view: View) {
    }

    override fun lazyLoad() {
    }
}