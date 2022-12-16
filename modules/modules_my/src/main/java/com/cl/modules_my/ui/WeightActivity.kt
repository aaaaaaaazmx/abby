package com.cl.modules_my.ui

import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs
import com.cl.modules_my.databinding.MyWeightActivityBinding

/**
 * 重量单位选择
 */
class WeightActivity : BaseActivity<MyWeightActivityBinding>() {

    // false 英制、 true 公制
    private val weightUnit by lazy {
        Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
    }

    override fun initView() {
        binding.metric.isChecked = weightUnit
        binding.imperial.isChecked = !weightUnit

        // 点击监听
        binding.imperial.setOnCheckedChangeListener { _, b ->
            binding.metric.isChecked = !b
        }
        binding.metric.setOnCheckedChangeListener { _, b ->
            binding.imperial.isChecked = !b
        }

        binding.clMetric.setOnClickListener{
            val imperialsChecked = binding.imperial.isChecked
            binding.imperial.isChecked = !imperialsChecked
            binding.metric.isChecked = imperialsChecked
        }

        binding.clImperial.setOnClickListener {
            val metricIsChecked = binding.metric.isChecked
            binding.metric.isChecked = !metricIsChecked
            binding.imperial.isChecked = metricIsChecked
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }

    override fun onPause() {
        super.onPause()
        logI("123123123: ${binding.metric.isChecked}")
        Prefs.putBooleanAsync(Constants.My.KEY_MY_WEIGHT_UNIT, binding.imperial.isChecked)
    }
}