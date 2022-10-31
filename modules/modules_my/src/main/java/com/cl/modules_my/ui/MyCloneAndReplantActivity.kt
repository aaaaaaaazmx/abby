package com.cl.modules_my.ui

import android.content.Intent
import cn.mtjsoft.barcodescanning.extentions.dp2px
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants.Global.KEY_IS_CHOOSE_CLONE
import com.cl.common_base.constants.Constants.Global.KEY_IS_CHOOSE_SEED
import com.cl.common_base.constants.Constants.Global.KEY_PLANT_ID
import com.cl.common_base.constants.Constants.Global.KEY_REFRESH_PLANT_INFO
import com.cl.common_base.constants.Constants.Global.KEY_USER_NO_ATTRIBUTE
import com.cl.common_base.constants.Constants.Global.KEY_USER_NO_STRAIN_NAME
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.BasePlantUsuallyGuidePop
import com.cl.common_base.pop.ChooserSeedPop
import com.cl.common_base.pop.StrainNamePop
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

    private val plantId by lazy {
        intent.getStringExtra(KEY_PLANT_ID)
    }

    /**
     * 通用弹窗
     * 网络请求
     */
    private val custom by lazy {
        BasePlantUsuallyGuidePop(
            context = this@MyCloneAndReplantActivity,
            onNextAction = {
                // 点击 Done， 需要解锁周期为Seed
                // 跳转到HomeFragment界面
                setResult(RESULT_OK, Intent().putExtra(KEY_IS_CHOOSE_SEED, true))
                finish()
            }
        )
    }


    override fun initView() {
        // 如果是没名字、没属性那么直接隐藏
        ViewUtils.setGone(binding.llSeed, isNoAttribute || isNoStrainName)

        binding.title.setLeftClickListener {
            setResult(RESULT_OK, Intent().putExtra(KEY_IS_CHOOSE_CLONE, false))
            finish()
        }

        plantId?.let {
            kotlin.runCatching {
                mViewModel.upPlantInfoReq.value?.id = it.toInt()
            }
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

                    // 弹出通用图文页面
                    mViewModel.getGuideInfo("8")
                }
            })

            // 图文引导界面
            getGuideInfo.observe(this@MyCloneAndReplantActivity, resourceObserver {
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
            })
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
                                if (isNoAttribute || isNoStrainName) {
                                    setResultForRefreshPlantInfo()
                                }
                            }, isNoStrainName = isNoStrainName
                        )
                    ).show()
                return@setOnClickListener
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
                                    .maxHeight(dp2px(600))
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
                                            })
                                    ).show()
                            })
                    ).show()
            }

        }
    }

    companion object {
        const val KEY_CLONE = "Clone"
        const val KEY_SEED = "Seed"
    }
}