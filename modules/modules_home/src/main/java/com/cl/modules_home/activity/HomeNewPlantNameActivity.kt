package com.cl.modules_home.activity

import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.bbgo.module_home.databinding.HomeNewPlantBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath

/**
 * 新的种植输入名称
 */
@Route(path = RouterPath.Home.PAGE_PLANT_NAME)
class HomeNewPlantNameActivity : BaseActivity<HomeNewPlantBinding>() {
    override fun initView() {
        binding.btnSuccess.setOnClickListener {
            val intent = Intent(this, HomePlantProfileActivity::class.java)
            intent.putExtra(HomePlantProfileActivity.KEY_PLANT_NAME, binding.etEmail.text.toString())
            startActivity(intent)
        }
        binding.ivClearCode.setOnClickListener {
            binding.etEmail.setText("")
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }
}