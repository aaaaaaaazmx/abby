package com.cl.modules_my.ui

import android.content.Intent
import cn.mtjsoft.barcodescanning.extentions.dp2px
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants.Global.KEY_IS_CHOOSE_CLONE
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.pop.ChooserSeedPop
import com.cl.common_base.pop.StrainNamePop
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyCloneAndReplantBinding
import com.cl.modules_my.viewmodel.CloneAndReplantViewModel
import com.cl.modules_my.viewmodel.SettingViewModel
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 选择Clone 或者 从种子阶段开始
 */

@Route(path = RouterPath.My.PAGE_MT_CLONE_SEED)
@AndroidEntryPoint
class MyCloneAndReplantActivity : BaseActivity<MyCloneAndReplantBinding>() {

    @Inject
    lateinit var mViewModel: CloneAndReplantViewModel


    private val pop by lazy {
        XPopup.Builder(this@MyCloneAndReplantActivity)
            .maxHeight(dp2px(600))
            .enableDrag(false)
            .dismissOnTouchOutside(false)
    }

    override fun initView() {
        binding.title.setLeftClickListener {
            setResult(RESULT_OK, Intent().putExtra(KEY_IS_CHOOSE_CLONE, false))
        }
    }

    override fun observe() {
        mViewModel.apply {

        }
    }

    override fun initData() {
        binding.checkClone.setOnCheckedChangeListener { _, b ->
            if (binding.checkSeed.isChecked) {
                binding.checkSeed.isChecked = !b
            }
            binding.btnSuccess.isEnabled =
                binding.checkClone.isChecked || binding.checkSeed.isChecked
        }

        binding.checkSeed.setOnCheckedChangeListener { _, b ->
            if (binding.checkClone.isChecked) {
                binding.checkClone.isChecked = !b
            }
            binding.btnSuccess.isEnabled =
                binding.checkClone.isChecked || binding.checkSeed.isChecked
        }

        binding.btnSuccess.setOnClickListener {
            // 具体执行哪一步
            val cloneCheck = binding.checkClone.isChecked
            val seedCheck = binding.checkSeed.isChecked
            if (cloneCheck) {
                // 继承植物
                setResult(RESULT_OK, Intent().putExtra(KEY_IS_CHOOSE_CLONE, true))
            }

            if (seedCheck) {
                // todo 走的是新的逻辑
                // 选择的是种子阶段
                pop.asCustom(
                    ChooserSeedPop(this@MyCloneAndReplantActivity, onConfirmAction = {
                        // 点击确定之后的跳转
                        // 跳转到Strain名字填写弹窗
                        pop.asCustom(
                            StrainNamePop(this@MyCloneAndReplantActivity, onConfirmAction = {

                            })
                        )
                    })
                ).show()
            }
        }
    }
}