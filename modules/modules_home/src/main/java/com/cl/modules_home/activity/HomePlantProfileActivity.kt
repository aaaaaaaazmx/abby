package com.cl.modules_home.activity

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeNewPlantBinding
import com.bbgo.module_home.databinding.HomePlantProfileBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UpPlantInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.util.Prefs
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.viewmodel.HomePlantProfileViewModel
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 选择植物属性页面
 */
@AndroidEntryPoint
class HomePlantProfileActivity : BaseActivity<HomePlantProfileBinding>() {

    @Inject
    lateinit var mViewModel: HomePlantProfileViewModel

    // 带过来的属性名字
    private val plantName by lazy {
        intent.getStringExtra(KEY_PLANT_NAME)
    }

    // 获取植物ID
    private val plantId by lazy {
        Prefs.getString(Constants.Global.KEY_PLANT_ID)
    }

    private val pop by lazy {
        XPopup.Builder(this).isDestroyOnDismiss(false).dismissOnTouchOutside(false)
    }

    override fun initView() {
        binding.btnSuccess.setOnClickListener {
            val category = if (binding.checkSeed.isChecked) {
                100001
            } else if (binding.checkClone.isChecked) {
                100002
            } else if (binding.autoCheckSeed.isChecked) {
                100003
            } else if (binding.autoCheckClone.isChecked) {
                100004
            } else {
                100001
            }

            if (binding.etEmail.text.toString().isEmpty()) {
                ToastUtil.shortShow("Please enter a strain name")
                return@setOnClickListener
            }

            kotlin.runCatching {
                mViewModel.updatePlantInfo(
                    UpPlantInfoReq(
                        plantId = plantId.toInt(),
                        plantName = plantName ?: "",
                        strainName = binding.etEmail.text.toString(),
                        categoryCode = category
                    )
                )
            }

        }

        binding.clHow.setOnClickListener {
            pop.asCustom(
                BaseCenterPop(
                    this,
                    titleText = "Photo vs Auto",
                    content = "Photoperiod strains are more common. Unless you’ve confirmed with your retailer that it is autoflower, please select “photoperiod” here.\n" + "\n" + "Hey abby can generate an algorithm to tailor for either type.",
                    isShowCancelButton = false
                )
            ).show()
        }

        binding.clNot.setOnClickListener {
            pop.asCustom(
                BaseCenterPop(
                    this,
                    titleText = "Not Sure?",
                    content = "Please check with your local retailer or seed bank. You can also usually find this information in your order invoice. \n\n Note: choosing the wrong setting can delay flowering. We strongly encourage you to confirm with your provider first. ",
                    isShowCancelButton = false
                )
            ).show()
        }

        binding.etEmail.doAfterTextChanged {

        }

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

    override fun observe() {
        mViewModel.apply {
            updatePlantInfo.observe(this@HomePlantProfileActivity, resourceObserver {
                loading {
                    showProgressLoading()
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    // 跳转富文本界面
                    val intent = Intent(this@HomePlantProfileActivity, BasePopActivity::class.java)
                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_PREPARE_THE_SEED)
                    intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_PREPARE_THE_SEED)
                    intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                    intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                    startActivity(intent)
                }
            })
        }
    }

    override fun initData() {
    }

    companion object {
        const val KEY_PLANT_NAME = "key_plant_name"
    }
}