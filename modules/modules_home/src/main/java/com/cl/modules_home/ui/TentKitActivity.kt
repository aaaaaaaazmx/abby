package com.cl.modules_home.ui

import android.content.Intent
import androidx.core.content.ContextCompat
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.base.KnowMoreActivity
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.UpPlantInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.util.Prefs
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.databinding.HomeActivityTentkitBinding
import com.cl.modules_home.viewmodel.TentKitViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TentKitActivity: BaseActivity<HomeActivityTentkitBinding>() {
    @Inject
    lateinit var tentKitViewModel: TentKitViewModel

    // 获取植物ID
    private val plantId by lazy {
        Prefs.getString(Constants.Global.KEY_PLANT_ID)
    }

    override fun initView() {

    }

    override fun observe() {
        tentKitViewModel.apply {
            updatePlantInfo.observe(this@TentKitActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    // startRunning(botanyId = "", goon = false)
                    val intent = Intent(this@TentKitActivity, BasePopActivity::class.java)
                    // 跳转到富文本 或者 首页
                    if (binding.checkSeed.isChecked) {
                        // 跳转富文本之后返回首页
                        intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_SEED)
                        intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_SEED)
                    } else if (binding.checkClone.isChecked) {
                        // 跳转富文本之后，直接跳转发芽流程
                        intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_CLONE)
                        intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_CLONE)
                    } else if (binding.autoCheckSeed.isChecked) {
                        // 跳转富文本之后返回首页
                        intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEED)
                        intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEED)
                    } else if (binding.autoCheckClone.isChecked) {
                        // 跳转富文本之后，直接跳转发芽流程
                        intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEEDING)
                        intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TENT_KIT_AUTO_SEEDING)
                    }
                    intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                    intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                    intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, true)
                    startActivity(intent)
                }
            })

            /*startRunning.observe(this@TentKitActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                }
            })*/
        }
    }

    override fun initData() {
        binding.btnSuccess.setSafeOnClickListener {
            // 帐篷且Photo Seed，跳转 发芽流程，富文本ID：PHOTO_SEED_GERMINATE
            // 帐篷且Photo Clone，跳转 移植流程，富文本ID：PHOTO_CLONE_GERMINATE
            // 帐篷且Auto Seed，跳转 发芽流程，富文本ID：AUTO_SEED_TRANSPLANT
            // 帐篷且Auto  Seeding，跳转 移植流程，富文本ID：AUTO_SEEDING_TRANSPLANT

            // 1. 点击Next后调用【更新植物基本信息】接口
            //新增 spaceType 种植空间类型 tent-帐篷,box-种植箱,tent_kit-智能种植帐篷
            //2. 再调用【开始运行】接口 startRuning 。直接跳转到首页

            val category = if (binding.checkSeed.isChecked) {
                100001
            } else if (binding.checkClone.isChecked) {
                100003
            } else if (binding.autoCheckSeed.isChecked) {
                100002
            } else if (binding.autoCheckClone.isChecked) {
                100004
            } else {
                100001
            }
            tentKitViewModel.updatePlantInfo(UpPlantInfoReq(plantId = plantId.safeToInt(), spaceName = binding.inputWeight.text.toString(), spaceType = ListDeviceBean.KEY_SPACE_TYPE_TENT_KIT, categoryCode = category.toString(), ))
        }

        // checkBox点击事件
        binding.checkClone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.checkSeed.isChecked = false
                binding.autoCheckSeed.isChecked = false
                binding.autoCheckClone.isChecked = false
                binding.tvAuto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.color_c4))
                binding.tvPhoto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.mainColor))
            }

            if (!isChecked) {
                if (!binding.checkSeed.isChecked && !binding.autoCheckSeed.isChecked && !binding.autoCheckClone.isChecked) {
                    binding.checkClone.isChecked = true
                }
            }
        }
        binding.checkSeed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.checkClone.isChecked = false
                binding.autoCheckSeed.isChecked = false
                binding.autoCheckClone.isChecked = false
                binding.tvAuto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.color_c4))
                binding.tvPhoto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.mainColor))
            }

            if (!isChecked) {
                if (!binding.checkClone.isChecked && !binding.autoCheckSeed.isChecked && !binding.autoCheckClone.isChecked) {
                    binding.checkSeed.isChecked = true
                }
            }
        }
        binding.autoCheckClone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.checkClone.isChecked = false
                binding.checkSeed.isChecked = false
                binding.autoCheckSeed.isChecked = false
                binding.tvAuto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.mainColor))
                binding.tvPhoto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.color_c4))
            }

            if (!isChecked) {
                if (!binding.checkClone.isChecked && !binding.checkSeed.isChecked && !binding.autoCheckSeed.isChecked) {
                    binding.autoCheckClone.isChecked = true
                }
            }
        }
        binding.autoCheckSeed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.checkClone.isChecked = false
                binding.checkSeed.isChecked = false
                binding.autoCheckClone.isChecked = false
                binding.tvAuto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.mainColor))
                binding.tvPhoto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.color_c4))
            }

            if (!isChecked) {
                if (!binding.checkClone.isChecked && !binding.checkSeed.isChecked && !binding.autoCheckClone.isChecked) {
                    binding.autoCheckSeed.isChecked = true
                }
            }
        }
    }

}