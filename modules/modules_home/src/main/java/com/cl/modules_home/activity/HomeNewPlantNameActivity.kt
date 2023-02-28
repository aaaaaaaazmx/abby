package com.cl.modules_home.activity

import android.content.Intent
import com.bbgo.module_home.databinding.HomeNewPlantBinding
import com.cl.common_base.base.BaseActivity

/**
 * 新的种植输入名称
 */
class HomeNewPlantNameActivity : BaseActivity<HomeNewPlantBinding>() {
    override fun initView() {
        binding.btnSuccess.setOnClickListener {
            val intent = Intent(this, HomePlantProfileActivity::class.java)
            intent.putExtra(HomePlantProfileActivity.KEY_PLANT_NAME, binding.etEmail.text.toString())
            startActivity(intent)
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }
}