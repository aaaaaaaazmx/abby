package com.cl.modules_my.ui

import android.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.modules_my.databinding.MyPlantSetupActivityBinding

/**
 * This is a short description.
 *
 * @author 李志军 2023-09-17 20:45
 *
 * 植物设置界面
 */
class PlantSetUpActivity: BaseActivity<MyPlantSetupActivityBinding>() {

    private val number by lazy {
        intent.getIntExtra(KEY_NUMBER, -1)
    }

    override fun initView() {
        binding.title
            .setRightButtonTextBack(R.drawable.background_check_tags_r5)
            .setRightButtonText("Done")
            .setRightButtonTextSize(13f)
            .setRightButtonTextHeight(25f)
            .setRightButtonTextColor(Color.WHITE)
            .setRightClickListener {  }


        binding.rvPlantSet.layoutManager = LinearLayoutManager(this@PlantSetUpActivity)
    }


    override fun observe() {
    }

    override fun initData() {
        TODO("Not yet implemented")
    }

    companion object {
        const val KEY_NUMBER = "number"
    }
}