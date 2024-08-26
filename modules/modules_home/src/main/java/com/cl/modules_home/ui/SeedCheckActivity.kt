package com.cl.modules_home.ui

import android.content.Intent
import com.cl.modules_home.databinding.HomeSeekCheckActivityBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.base.KnowMoreActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.modules_home.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * 种子检查界面
 */
@AndroidEntryPoint
class SeedCheckActivity: BaseActivity<HomeSeekCheckActivityBinding>() {
    override fun initView() {

    }

    override fun observe() {
    }

    override fun initData() {

        binding.curingBox.setOnClickListener {
            binding.sunkBox.isChecked = !binding.curingBox.isChecked
        }

        binding.sunkBox.setOnClickListener {
            binding.curingBox.isChecked = !binding.sunkBox.isChecked
        }

        binding.clCuringType.setOnClickListener {
            val curingBox = binding.curingBox.isChecked
            binding.curingBox.isChecked = !curingBox
            binding.sunkBox.isChecked = curingBox
        }

        binding.clCunkType.setOnClickListener {
            val sunkBox = binding.sunkBox.isChecked
            binding.curingBox.isChecked = sunkBox
            binding.sunkBox.isChecked = !sunkBox
        }

        binding.nextBtn.setOnClickListener {
            // 跳转到下一个界面
            val curingBox = binding.curingBox.isChecked
            val sunkBox = binding.sunkBox.isChecked
            // 重新种植
            if (curingBox) startActivity(Intent(this@SeedCheckActivity, ChangTheSeedActivity::class.java))
            if (sunkBox) {
                // 跳转到富文本界面
                // 解锁种子期间-》 veg
                // 跳准到富文本页面
                val intent = Intent(this@SeedCheckActivity, KnowMoreActivity::class.java)
                intent.putExtra(
                    BasePopActivity.KEY_IS_SHOW_BUTTON,
                    true
                )
                intent.putExtra(
                    BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT,
                    getString(com.cl.common_base.R.string.string_262)
                )
                intent.putExtra(
                    BasePopActivity.KEY_INTENT_JUMP_PAGE,
                    true
                )
                intent.putExtra(
                    Constants.Global.KEY_TXT_ID,
                    Constants.Fixed.KEY_FIXED_ID_ACTION_NEEDED
                )
                intent.putExtra(
                    BasePopActivity.KEY_FIXED_TASK_ID,
                    Constants.Fixed.KEY_FIXED_ID_ACTION_NEEDED
                )
                startActivity(intent)
            }
        }
    }
}