package com.cl.modules_my.ui

import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath
import com.cl.modules_my.databinding.MyCameraSettingBinding
import com.cl.modules_my.databinding.MyStoraceOptionBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 照片存储选项界面
 */
@AndroidEntryPoint
class CameraSettingActivity: BaseActivity<MyCameraSettingBinding>() {
    override fun initView() {

    }

    override fun observe() {
    }

    override fun onBackPressed() {
        ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
            .navigation()
        finish()
    }
    override fun initData() {
        binding.title.setLeftClickListener {
            ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                .navigation()
            finish()
        }

        binding.curingBox.setOnCheckedChangeListener { buttonView, isChecked ->
            // 当前如果是选中的，那么另外一个就取消选中，如果当前不是选中，那么另外一个选中
            binding.curingBoxPhoto.isChecked = !isChecked
        }
        binding.curingBoxPhoto.setOnCheckedChangeListener { buttonView, isChecked ->
            // 当前如果是选中的，那么另外一个就取消选中，如果当前不是选中，那么另外一个选中
            binding.curingBox.isChecked = !isChecked
        }

        /*binding.btnSuccess.setOnClickListener {
            // 判断当前哪一个选中了
            if (binding.curingBox.isChecked) {
                // todo 选中了存储到本地， 然后返回，onBackPressed
            } else {
                // todo 选中了存储到云端 然后返回 onBackPressed
            }
        }*/
    }
}