package com.cl.modules_my.ui

import android.content.Intent
import androidx.core.text.buildSpannedString
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.*
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyUpdateFirmwareBinding
import com.cl.modules_my.viewmodel.FirmwareUpdateViewModel
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 固件升级Activity
 * @author 李志军 2022-08-16 17:27
 */
@AndroidEntryPoint
@Route(path = RouterPath.My.PAGE_MY_FIRMWARE_UPDATE)
class FirmwareUpdateActivity : BaseActivity<MyUpdateFirmwareBinding>() {
    @Inject
    lateinit var mViewModel: FirmwareUpdateViewModel

    // 是否是强制升级
    @Autowired(name = Constants.Global.KEY_GLOBAL_MANDATORY_UPGRADE)
    @JvmField
    var isMandatoryUpgrade = false


    // 统一xPopUp
    private val pop by lazy {
        // 升级提示框
        XPopup.Builder(this@FirmwareUpdateActivity)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .dismissOnTouchOutside(false)
    }

    // 升级弹窗
    private val updatePop by lazy {
        FirmwareUpdatePop(this@FirmwareUpdateActivity, onConfirmAction = {
            // 跳转到友情提示界面
            pop.asCustom(upDateTipsPop).show()
        })
    }

    // 友情提示弹窗
    private val upDateTipsPop by lazy {
        UpdateTipsPop(this@FirmwareUpdateActivity) {
            mViewModel.startOta()
            //  跳转到转圈圈界面
            pop.asCustom(updateProgressPop).show()
        }
    }

    // 转圈圈界面
    private val updateProgressPop by lazy {
        UpdateProgressPop(this@FirmwareUpdateActivity)
    }

    // 升级失败界面
    private val updateFailPop by lazy {
        UpdateFailPop(this@FirmwareUpdateActivity, onRetryAction = {
            //  重试
            // 清空为0，然后重新开始
            mViewModel.startOta()
            pop.asCustom(updateProgressPop).show()
        }, {
            updateProgressPop.dismiss()
        })
    }

    // 强制升级需要重新配对弹窗
    private val forceUpdatePop by lazy {
        FirmwareReplantPop(this@FirmwareUpdateActivity, onConfirmAction = {
            mViewModel.delete()
        })
    }


    // 升级成功界面
    private val updateSuccessPop by lazy {
        UpdateSuccessPop(this@FirmwareUpdateActivity) {
            // 需要判断此次升级是否是强制升级，如果是强制升级，需要解除设备，重新绑定。
            mViewModel.upgradeInfoBeans.value?.let {
                it.firstOrNull { bean -> bean.type == 9 }?.let { upgradeInfoBean ->
                    //0：App 提醒升级
                    //2：App 强制升级
                    //3：检测升级
                    if (upgradeInfoBean.upgradeType == 2) {
                        // 需要解除设备，然后跳到绑定界面
                        // 弹出解除绑定弹窗
                        pop.asCustom(forceUpdatePop).show()
                        return@UpdateSuccessPop
                    }
                }
            }
            // 这是普通升级 0，3
            // 成功之后需要做的准备
            checkOta()
        }
    }

    override fun initView() {
        ARouter.getInstance().inject(this)

        if (isMandatoryUpgrade) {
            binding.title.setLeftVisible(false)
        }
    }

    override fun observe() {
        mViewModel.apply {
            deleteDevice.observe(this@FirmwareUpdateActivity, resourceObserver {
                success {
                    //  删除设备之后应该去哪？
                    // 跳转 Adddevice 界面
                    logI("deleteDevice is Success")
                    ARouter.getInstance()
                        .build(RouterPath.PairConnect.PAGE_PLANT_CHECK)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .navigation()
                }
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
            })
        }
    }

    override fun initData() {
        checkOta()

        // 点击升级按钮
        binding.btnSuccess.setOnClickListener {
            // 弹出升级框
            mViewModel.upgradeInfoBeans.value?.let {
                it.firstOrNull { bean -> bean.type == 9 }?.let {
                    // 跳转到友情提示界面
                    pop.asCustom(upDateTipsPop).show()
                }
            }
        }

        // 升级监听
        mViewModel.setOtaListener(
            onSuccess = {
                runOnUiThread {
                    // 消失进度条
                    updateProgressPop.dismiss()
                    pop.asCustom(updateSuccessPop).show()
                }
            },
            onFailure = { otaType, code, error ->
                runOnUiThread {
                    // 消失进度条
                    updateProgressPop.dismiss()
                    error?.let { updateFailPop.setFailtext(it) }
                    pop.asCustom(updateFailPop).show()
                }
            },
            onFailureWithText = { otaType, code, messageBean ->
                runOnUiThread {
                    updateProgressPop.dismiss()
                    messageBean?.text?.let { updateFailPop.setFailtext(it) }
                    pop.asCustom(updateFailPop).show()
                }
            },
            onProgress = { otaType, progress ->
                runOnUiThread {
                    updateProgressPop.setData(progress)
                }
            },
            onTimeOut = {
                runOnUiThread {
                    // 消失进度条
                    updateProgressPop.dismiss()
                    updateFailPop.setFailtext("Time Out")
                    pop.asCustom(updateFailPop).show()
                }
            }
        )
    }

    private fun checkOta() =
        mViewModel.checkFirmwareUpdateInfo(
            onOtaInfo = { bean, isShow ->
                ViewUtils.setVisible(isShow, binding.btnSuccess)
                bean?.firstOrNull { it.type == 9 }?.let { data ->
                    if (isShow) {
                        binding.tvVersion.text = "v${data.version}"
                        if (data.desc.isNullOrEmpty()) {
                            binding.tvMs.text = buildSpannedString {
                                appendLine("1.Follow the APP instruction, change")
                                appendLine("2.open the front door of the device, put the pipe into the bucket, operate on APP to start the pumping water; In the meantime, turn the water tank cover over to observe the situation of Root; after the pumping, check whether there are dead roots or trash on the pipe filter. If so, clean it up, and then place the tube back to the fixed position at the bottom of the tank;")
                            }
                        } else {
                            binding.tvMs.text = data.desc
                        }
                    } else {
                        binding.tvVersion.text = "v${data.currentVersion}"
                        binding.tvMs.text = "Your firmware is up to date"
                    }
                }
            }
        )

    override fun onBackPressed() {
        if (isMandatoryUpgrade) return
        super.onBackPressed()
    }

}