package com.cl.modules_planting_log.ui

import com.cl.common_base.base.BaseActivity
import com.cl.modules_planting_log.databinding.PlantingLogActivityBinding
import com.cl.modules_planting_log.viewmodel.PlantingLogAcViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * 种植日志记录页面
 */
@AndroidEntryPoint
class PlantingLogActivity: BaseActivity<PlantingLogActivityBinding>() {

    @Inject
    lateinit var viewModel: PlantingLogAcViewModel

    // 植物ID， 用于新增日志
    private val plantId by lazy {
        intent.getStringExtra("plantId")
    }

    // logId 用于修改日志
    private val logId by lazy {
        intent.getStringExtra("logId")
    }

    override fun initView() {


    }

    override fun observe() {
    }

    override fun initData() {
    }
}