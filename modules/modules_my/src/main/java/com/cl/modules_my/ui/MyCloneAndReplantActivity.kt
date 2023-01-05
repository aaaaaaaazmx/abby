package com.cl.modules_my.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import cn.mtjsoft.barcodescanning.extentions.dp2px
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.Constants.Global.KEY_IS_CHOOSE_CLONE
import com.cl.common_base.constants.Constants.Global.KEY_IS_CHOOSE_SEED
import com.cl.common_base.constants.Constants.Global.KEY_IS_SHOW_CHOOSER_TIPS
import com.cl.common_base.constants.Constants.Global.KEY_PLANT_ID
import com.cl.common_base.constants.Constants.Global.KEY_REFRESH_PLANT_INFO
import com.cl.common_base.constants.Constants.Global.KEY_USER_NO_ATTRIBUTE
import com.cl.common_base.constants.Constants.Global.KEY_USER_NO_STRAIN_NAME
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.*
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyCloneAndReplantBinding
import com.cl.modules_my.viewmodel.CloneAndReplantViewModel
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

    /**
     * 是否没有名字
     */
    private val isNoStrainName by lazy {
        intent.getBooleanExtra(KEY_USER_NO_STRAIN_NAME, false)
    }

    /**
     * 是否没有属性名
     */
    private val isNoAttribute by lazy {
        intent.getBooleanExtra(KEY_USER_NO_ATTRIBUTE, false)
    }

    /**
     * 是否优先弹出选择tips框
     * 默认优先弹出的。
     */
    private val isShowChooserTips by lazy {
        intent.getBooleanExtra(KEY_IS_SHOW_CHOOSER_TIPS, false)
    }

    private val plantId by lazy {
        intent.getStringExtra(KEY_PLANT_ID)
    }

    /**
     * 通用弹窗
     * 网络请求
     */
    /*private val custom by lazy {
        BasePlantUsuallyGuidePop(
            context = this@MyCloneAndReplantActivity,
            onNextAction = {
                // 点击 Done， 需要解锁周期为Seed
                // 跳转到HomeFragment界面
                setResult(RESULT_OK, Intent().putExtra(KEY_IS_CHOOSE_SEED, true))
                finish()
            }
        )
    }*/

    private val chooserTipsPop by lazy {
        ChooserTipsPop(
            this@MyCloneAndReplantActivity,
            onConfirmAction = {
                XPopup.Builder(this@MyCloneAndReplantActivity)
                    .isDestroyOnDismiss(false)
                    .enableDrag(false)
                    .moveUpToKeyboard(false)
                    .dismissOnTouchOutside(false)
                    .asCustom(
                        chooseSeedOrClonePop
                    ).show()
            },
            onLearnMoreAction = {
                //  跳转post
                val intent = Intent(this@MyCloneAndReplantActivity, BasePopActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_HOW_TO_PICK_STRAIN)
                startActivity(intent)
            },
            onNotReadAction = {
                //  跳转post
                val intent = Intent(this@MyCloneAndReplantActivity, BasePopActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_HOW_TO_PICK_STRAIN)
                startActivity(intent)
            },
            onCancelAction = {
                finish()
            }
        )
    }

    private val chooseSeedOrClonePop by lazy {
        ChooserSeedOrClonePop(
            this@MyCloneAndReplantActivity,
            onConfirmAction = { cloneCheck ->
                // 进行下一步操作
                startCloneOrSeed(cloneCheck, !cloneCheck)
            }
        )
    }


    /**
     * 选择纸杯子还是塑料杯子
     */
    private val chooserPaperOrPlasticPop by lazy {
        ChooserPaperOrPlasticPop(
            this@MyCloneAndReplantActivity,
            onConfirmAction = { isPaperCheck ->
                logI("chooserPaperOrPlasticPop: $isPaperCheck")
                // 纸杯子还是塑料杯子
                mViewModel.upPlantInfoReq.value?.cupType = if (!isPaperCheck) 0 else 1
                // 这个时候上报一次，然后弹出通用图文弹窗
                mViewModel.upPlantInfoReq.value?.let { req ->
                    mViewModel.updatePlantInfo(
                        req
                    )
                }
            }
        )
    }

    override fun initView() {
        // 如果是没名字、没属性、tips那么直接隐藏
        ViewUtils.setGone(binding.llSeed, isNoAttribute || isNoStrainName || isShowChooserTips)

        binding.title.setLeftClickListener {
            setResult(RESULT_OK, Intent().putExtra(KEY_IS_CHOOSE_CLONE, false))
            finish()
        }

        plantId?.let {
            kotlin.runCatching {
                mViewModel.upPlantInfoReq.value?.id = it.toInt()
            }
        }
        // 没有tips弹窗
        if (isShowChooserTips) {
            XPopup.Builder(this@MyCloneAndReplantActivity)
                .isDestroyOnDismiss(false)
                .enableDrag(false)
                .moveUpToKeyboard(false)
                .dismissOnTouchOutside(false)
                .asCustom(
                    chooserTipsPop
                ).show()
        }

        // 没有属性
        if (isNoAttribute) {
            XPopup.Builder(this@MyCloneAndReplantActivity)
                .isDestroyOnDismiss(false)
                .maxHeight(dp2px(700))
                .enableDrag(false)
                .moveUpToKeyboard(false)
                .dismissOnTouchOutside(false).asCustom(
                    // 选择photo、seed弹擦混改
                    ChooserSeedPop(this@MyCloneAndReplantActivity, onConfirmAction = { attribute ->
                        // 点击确定之后的跳转
                        // 跳转到Strain名字填写弹窗
                        // 种植属性
                        mViewModel.upPlantInfoReq.value?.attribute = attribute
                        XPopup.Builder(this@MyCloneAndReplantActivity)
                            .maxHeight(dp2px(700))
                            .maxWidth(dp2px(700))
                            .enableDrag(false)
                            .moveUpToKeyboard(false)
                            .dismissOnTouchOutside(false)
                            .isDestroyOnDismiss(false).asCustom(
                                // 填入Strain名字弹擦混改
                                StrainNamePop(
                                    this@MyCloneAndReplantActivity,
                                    onConfirmAction = { name ->
                                        // 弹出通用图文弹窗
                                        // 传入数字8
                                        mViewModel.upPlantInfoReq.value?.strainName = name
                                        // 这个时候上报一次，然后弹出通用图文弹窗
                                        mViewModel.upPlantInfoReq.value?.let { req ->
                                            mViewModel.updatePlantInfo(
                                                req
                                            )
                                        }
                                    }, onCancelAction = {
                                        // 如果是直接弹窗的，也就是老用户，但是没输入名字 或者没有属性名
                                        if (isNoAttribute || isNoStrainName) {
                                            setResultForRefreshPlantInfo()
                                        }
                                    }, isNoStrainName = isNoStrainName
                                )
                            ).show()
                    }, onCancelAction = {
                        if (isNoAttribute) {
                            // 这个是没有选择属性名
                            setResultForRefreshPlantInfo()
                        }
                    }, isNoAttribute = isNoAttribute)
                ).show()
        }

        // 没有名字
        if (isNoStrainName) {
            XPopup.Builder(this@MyCloneAndReplantActivity)
                .maxHeight(dp2px(700))
                .maxWidth(dp2px(700))
                .enableDrag(false)
                .moveUpToKeyboard(false)
                .dismissOnTouchOutside(false)
                .isDestroyOnDismiss(false).asCustom(
                    // 填入Strain名字弹擦混改
                    StrainNamePop(
                        this@MyCloneAndReplantActivity,
                        onConfirmAction = { name ->
                            // 弹出通用图文弹窗
                            // 传入数字8
                            mViewModel.upPlantInfoReq.value?.strainName = name
                            // 这个时候上报一次，然后弹出通用图文弹窗
                            mViewModel.upPlantInfoReq.value?.let { req ->
                                mViewModel.updatePlantInfo(
                                    req
                                )
                            }
                        }, onCancelAction = {
                            // 如果是直接弹窗的，也就是老用户，但是没输入名字 或者没有属性名
                            if (isNoAttribute || isNoStrainName) {
                                setResultForRefreshPlantInfo()
                            }
                        }, isNoStrainName = isNoStrainName
                    )
                ).show()
        }
    }

    /**
     * 返回按键 -> 就不用管了。
     */
    private fun setResultForRefreshPlantInfo(isRefreshPlantInfo: Boolean? = true) {
        setResult(RESULT_OK, Intent().putExtra(KEY_REFRESH_PLANT_INFO, isRefreshPlantInfo))
        finish()
    }

    override fun observe() {
        mViewModel.apply {
            updatePlantInfo.observe(this@MyCloneAndReplantActivity, resourceObserver {
                loading { }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let {
                        ToastUtil.show(it)
                    }
                }
                success {
                    hideProgressLoading()
                    // 只是为了老用户填入属性and名字
                    if (isNoAttribute || isNoStrainName) {
                        setResultForRefreshPlantInfo()
                        return@success
                    }

                    // 只有选择seed的时候才会弹出、因为选择了种子需要选择种子的属性，那么属性就必定不会为空
                    if (mViewModel.upPlantInfoReq.value?.attribute?.isNotEmpty() == true) {
                        // 弹出通用图文页面
                        /*mViewModel.getGuideInfo("8")*/

                        //  跳转通用post
                        val intent = Intent(this@MyCloneAndReplantActivity, BasePopActivity::class.java)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_SEED_KIT_CUP_TYPE)
                        myActivityLauncher.launch(intent)
                    }
                }
            })

            // 图文引导界面
           /* getGuideInfo.observe(this@MyCloneAndReplantActivity, resourceObserver {
                loading { }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let {
                        ToastUtil.show(it)
                    }
                }

                success {
                    hideProgressLoading()
                    // 弹窗引导界面的弹窗
                    custom.setData(data)
                    XPopup.Builder(this@MyCloneAndReplantActivity)
                        .maxHeight(dp2px(600))
                        .enableDrag(false)
                        .moveUpToKeyboard(false)
                        .dismissOnTouchOutside(false)
                        .isDestroyOnDismiss(false).asCustom(custom).show()
                }
            })*/
        }
    }

    override fun initData() {
        /*binding.checkClone.setOnCheckedChangeListener { _, b ->
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
        }*/

        binding.btnSuccess.setOnClickListener {
            // 具体执行哪一步
            val cloneCheck = binding.checkClone.isChecked
            val seedCheck = binding.checkSeed.isChecked

            startCloneOrSeed(cloneCheck, seedCheck)
        }
    }

    private fun startCloneOrSeed(cloneCheck: Boolean, seedCheck: Boolean) {
        // 隐藏弹窗
        if (chooserTipsPop.isShow) chooserTipsPop.dismiss()

        // 种植方式
        mViewModel.upPlantInfoReq.value?.plantWay = if (cloneCheck) KEY_CLONE else KEY_SEED

        // 继承植物
        if (cloneCheck) {
            XPopup.Builder(this@MyCloneAndReplantActivity)
                .maxHeight(dp2px(700))
                .maxWidth(dp2px(700))
                .enableDrag(false)
                .moveUpToKeyboard(false)
                .dismissOnTouchOutside(false)
                .isDestroyOnDismiss(false).asCustom(
                    // 填入Strain名字弹擦混改
                    StrainNamePop(
                        this@MyCloneAndReplantActivity,
                        onConfirmAction = { name ->
                            mViewModel.upPlantInfoReq.value?.strainName = name
                            // 这个时候上报一次，然后弹出通用图文弹窗
                            mViewModel.upPlantInfoReq.value?.let { req ->
                                mViewModel.updatePlantInfo(
                                    req
                                )
                            }
                            setResult(RESULT_OK, Intent().putExtra(KEY_IS_CHOOSE_CLONE, true))
                            finish()
                        }, onCancelAction = {
                            // 如果是直接弹窗的，也就是老用户，但是没输入名字 或者没有属性名
                            if (!isNoAttribute || !isNoStrainName) {
                                setResultForRefreshPlantInfo()
                            }
                        }, isNoStrainName = isNoStrainName
                    )
                ).show()
            return
        }

        // 种子
        if (seedCheck) {
            // todo 走的是新的逻辑
            // 选择的是种子阶段
            XPopup.Builder(this@MyCloneAndReplantActivity)
                .isDestroyOnDismiss(false)
                .maxHeight(dp2px(600))
                .enableDrag(false)
                .moveUpToKeyboard(false)
                .dismissOnTouchOutside(false).asCustom(
                    // 选择photo、seed弹擦混改
                    ChooserSeedPop(
                        this@MyCloneAndReplantActivity,
                        onConfirmAction = { attribute ->
                            // 点击确定之后的跳转
                            // 跳转到Strain名字填写弹窗
                            // 种植属性
                            mViewModel.upPlantInfoReq.value?.attribute = attribute
                            XPopup.Builder(this@MyCloneAndReplantActivity)
                                .maxHeight(dp2px(700))
                                .maxWidth(dp2px(700))
                                .enableDrag(false)
                                .moveUpToKeyboard(false)
                                .dismissOnTouchOutside(false)
                                .isDestroyOnDismiss(false).asCustom(
                                    // 填入Strain名字弹擦混改
                                    StrainNamePop(
                                        this@MyCloneAndReplantActivity,
                                        onConfirmAction = { name ->
                                            // 弹出通用图文弹窗
                                            mViewModel.upPlantInfoReq.value?.strainName = name
                                            // 需要让用户选择是纸杯子还是塑料杯子
                                            XPopup.Builder(this@MyCloneAndReplantActivity)
                                                .isDestroyOnDismiss(false)
                                                .enableDrag(false)
                                                .moveUpToKeyboard(false)
                                                .dismissOnTouchOutside(false)
                                                .asCustom(
                                                    chooserPaperOrPlasticPop
                                                ).show()
                                        })
                                ).show()
                        },
                        onCancelAction = {
                            finish()
                        })
                ).show()
        }
    }


    /**
     * 通用pop界面的回调
     */
    private val myActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                logI("myActivityLauncher result Code")
                // seek引导图之后u的回调、
                // 点击 Done， 需要解锁周期为Seed
                // 跳转到HomeFragment界面
                setResult(RESULT_OK, Intent().putExtra(KEY_IS_CHOOSE_SEED, true))
                finish()
            }
        }

    companion object {
        const val KEY_CLONE = "Clone"
        const val KEY_SEED = "Seed"
    }
}