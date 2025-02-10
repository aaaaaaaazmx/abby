package com.cl.modules_home.ui

import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.modules_home.databinding.HomeSeekCheckActivityBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.base.KnowMoreActivity
import com.cl.common_base.bean.UpPlantInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeGrowCheckActivityBinding
import com.cl.modules_home.databinding.HomeNewSeekCheckActivityBinding
import com.cl.modules_home.viewmodel.HomePlantProfileViewModel
import com.cl.modules_home.viewmodel.SeedCheckViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 种子检查界面
 */
@AndroidEntryPoint
class GrowCheckActivity : BaseActivity<HomeGrowCheckActivityBinding>() {

    @Inject
    lateinit var viewModel: SeedCheckViewModel

    // plantId
    private val plantId by lazy {
        intent.getIntExtra("plantId", -1)
    }

    // 是否是自家的帐篷， 只要是不为空，那么就是 tent_kit
    private val plantType by lazy {
        intent.getStringExtra(Constants.Global.KEY_PLANT_TYPE)
    }

    override fun initView() {

    }

    override fun observe() {
        viewModel.apply {
            updatePlantInfo.observe(this@GrowCheckActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    // 跳转到下一个界面
                    val curingBox = binding.curingBox.isChecked
                    val sunkBox = binding.sunkBox.isChecked
                    // 重新种植
                    if (curingBox) {
                        // 跳转到富文本界面
                        // 解锁种子期间-》 veg
                        // 跳准到富文本页面
                        val intent = Intent(this@GrowCheckActivity, KnowMoreActivity::class.java)
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
                            Constants.Fixed.KEY_FIXED_ID_ACTION_GROW_NEEDED
                        )
                        intent.putExtra(
                            BasePopActivity.KEY_FIXED_TASK_ID,
                            Constants.Fixed.KEY_FIXED_ID_ACTION_GROW_NEEDED
                        )
                        startActivity(intent)
                    }
                    if (sunkBox) {
                        // 跳转到富文本界面
                        // 解锁种子期间-》 veg
                        // 跳准到富文本页面
                        val intent = Intent(this@GrowCheckActivity, KnowMoreActivity::class.java)
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
                loading { showProgressLoading() }
            })
        }
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
            if (plantType.isNullOrEmpty()) {
                viewModel.updatePlantInfo(UpPlantInfoReq(plantId = plantId, growBlock = if (binding.curingBox.isChecked) 0 else 1))
            } else {
                // tent_kit帐篷种植
                ARouter.getInstance().build(RouterPath.Home.PAGE_TENT_KIT_PLANT_SETUP)
                    .withString(Constants.Global.KEY_PLANT_TYPE, plantType)
                    .withString(Constants.Global.KEY_PLANT_ID, plantId.toString())
                    .navigation(this@GrowCheckActivity)
            }
        }
    }
}