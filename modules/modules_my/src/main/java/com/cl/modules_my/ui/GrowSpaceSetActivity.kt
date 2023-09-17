package com.cl.modules_my.ui

import android.content.Context
import android.content.Intent
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyGrowSpaceActivityBinding

/**
 * This is a short description.
 *
 * @author 李志军 2023-09-17 20:12
 *
 * 添加帐篷设置界面
 */
class GrowSpaceSetActivity : BaseActivity<MyGrowSpaceActivityBinding>() {
    override fun initView() {
        binding.tvSend.setOnClickListener {
            val spaceName = binding.etApsceName.text.toString()
            val type = binding.etTypeName.text.toString()
            val number = binding.etNumberPlant.text.toString()
            val led = binding.etLedNumber.text.toString()

            if (spaceName.isNullOrEmpty() || type.isNullOrEmpty() || number.isNullOrEmpty() || led.isNullOrEmpty()) {
                ToastUtil.shortShow("Please improve the content")
                return@setOnClickListener
            }

            startActivity(Intent(this@GrowSpaceSetActivity, PlantSetUpActivity::class.java).apply {
                intent.putExtra(
                    PlantSetUpActivity.KEY_NUMBER,
                    kotlin.runCatching { number }.getOrDefault(-1))
            })
        }
    }

    override fun observe() {
        TODO("Not yet implemented")
    }

    override fun initData() {
        TODO("Not yet implemented")
    }
}