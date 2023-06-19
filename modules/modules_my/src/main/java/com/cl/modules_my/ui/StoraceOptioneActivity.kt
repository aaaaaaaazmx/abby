package com.cl.modules_my.ui

import com.cl.common_base.base.BaseActivity
import com.cl.modules_my.databinding.MyStoraceOptionBinding

/**
 * 照片存储选项界面
 */
class StoraceOptioneActivity: BaseActivity<MyStoraceOptionBinding>() {
    override fun initView() {

    }

    override fun observe() {
    }

    override fun initData() {
        binding.curingBox.setOnCheckedChangeListener { buttonView, isChecked ->
            // 当前如果是选中的，那么另外一个就取消选中，如果当前不是选中，那么另外一个选中
            binding.curingBoxPhoto.isChecked = !isChecked
        }
        binding.curingBoxPhoto.setOnCheckedChangeListener { buttonView, isChecked ->
            // 当前如果是选中的，那么另外一个就取消选中，如果当前不是选中，那么另外一个选中
            binding.curingBox.isChecked = !isChecked
        }

        binding.btnSuccess.setOnClickListener {
            // 判断当前哪一个选中了
            if (binding.curingBox.isChecked) {
                // todo 选中了存储到本地
            } else {
                // todo 选中了存储到云端
            }
        }
    }
}