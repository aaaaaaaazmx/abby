package com.cl.modules_pairing_connection.ui

import android.Manifest
import androidx.test.espresso.ViewInteractionModule_ProvideRemoteInteractionFactory
import cn.mtjsoft.barcodescanning.ScanningManager
import cn.mtjsoft.barcodescanning.config.Config
import cn.mtjsoft.barcodescanning.config.ScanType.Companion.CODE_BAR
import cn.mtjsoft.barcodescanning.config.ScanType.Companion.QR_CODE
import cn.mtjsoft.barcodescanning.interfaces.ScanResultListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.JpushMessageData
import com.cl.common_base.bean.UnreadMessageData
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.glide.GlideEngineForScanCode
import com.cl.common_base.util.json.GSON
import com.cl.common_base.util.livedatabus.LiveEvent
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.util.permission.PermissionChecker
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_pairing_connection.databinding.PairFontScanCodeBinding
import com.cl.modules_pairing_connection.viewmodel.PairFrontScanCodeViewModel
import com.cl.modules_pairing_connection.widget.ActivationFailPop
import com.cl.modules_pairing_connection.widget.ActivationSucceededPop
import com.lxj.xpopup.XPopup
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 扫码前置页面
 *
 * @author 李志军 2022-08-19 16:29
 */
@AndroidEntryPoint
@Route(path = RouterPath.PairConnect.PAGE_SCAN_CODE)
class PairFrontScanCodeActivity : BaseActivity<PairFontScanCodeBinding>() {

    @Inject
    lateinit var mViewModel: PairFrontScanCodeViewModel

    override fun initView() {

    }

    private val pop by lazy {
        XPopup.Builder(this@PairFrontScanCodeActivity)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .dismissOnTouchOutside(false)
    }


    private val failPop by lazy {
        ActivationFailPop(this@PairFrontScanCodeActivity, onTryAction = {
            checkPermissionAndStartScan()
        }, onCancelAction = {
            finish()
        })
    }

    override fun observe() {
        mViewModel.apply {
            checkSN.observe(this@PairFrontScanCodeActivity, resourceObserver {
                loading { showProgressLoading() }
                success {
                    hideProgressLoading()
                    // 2、如果有效，那么发送给设备135：OK
                    // 3、等到发送设备走成功回调时，那么表示OK
                    mViewModel.SN.value?.let {
                        DeviceControl.get().success {
                            getActivationStatus()
                        }.error { code, error ->
                            logE(
                                """
                               code: $code
                               error: $error
                           """.trimIndent()
                            )
                            pop.asCustom(failPop).show()
                        }.repairSN(it)
                    }
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    errorMsg?.let { ToastUtil.shortShow(it) }
                    pop.asCustom(failPop).show()
                }
            })
        }
    }

    override fun initData() {
        // 跳转到到扫码界面
        binding.btnSuccess.setOnClickListener {
            checkPermissionAndStartScan()
        }
    }


    /**
     * 检查权限以及开启扫描
     */
    private fun checkPermissionAndStartScan() {
        PermissionHelp().applyPermissionHelp(
            this@PairFrontScanCodeActivity,
            getString(com.cl.common_base.R.string.string_1609),
            object : PermissionHelp.OnCheckResultListener {
                override fun onResult(result: Boolean) {
                    if (!result) return
                    startScanCode()
                }
            },
            Manifest.permission.CAMERA,
        )
    }

    private fun startScanCode() {
        ScanningManager.instance.openScanningActivity(
            this,
            Config(
                true,
                CODE_BAR,
                null,
                object : ScanResultListener {
                    override fun onSuccessListener(value: String?) {
                        logI(
                            """
                        ScanCodeResult：
                        value： $value
                    """.trimIndent()
                        )
                    }

                    override fun onFailureListener(error: String) {
                        // 扫码失败
                    }

                    override fun onCompleteListener(value: String?) {
                        // 扫码结束，不管是否成功都会回调
                        // 成功时，value 不是空且是扫码结果
                        logI(
                            """
                        ScanCodeResult：
                        onCompleteListener value： $value
                    """.trimIndent()
                        )
                        // 这个时候判断长度
                        if (value.isNullOrEmpty()) {
                            pop.asCustom(failPop).show()
                            return
                        }

                        if (value.startsWith("AbbyAAYA", true)) {
                            if (value.length == 10 || value.length == 18) {
                                // 1、先检查这个扫出来的结果是否有效
                                mViewModel.setSn(value)
                                mViewModel.checkSN(value)
                            } else {
                                pop.asCustom(failPop).show()
                            }
                        } else {
                            pop.asCustom(failPop).show()
                        }
                    }
                })
        )
    }


    /**
     * 设备指令监听
     */
    override fun onTuYaToAppDataChange(status: String) {
        GSON.parseObjectInBackground(status, Map::class.java) { map->
            map?.forEach { (key, value) ->
                when (key) {
                    // SN修复的通知
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_SN_INSTRUCTION -> {
                        logI("ScanCodeActivity: KEY_DEVICE_REPAIR_SN： $value")
                        // 修复SN的监听
                        if (value == "OK") {
                            // 上报成功，那么啥也不管了，
                            val asCustom =
                                pop.asCustom(ActivationSucceededPop(this@PairFrontScanCodeActivity) {
                                    finish()
                                })
                            if (asCustom.isShow) return@forEach
                            asCustom.show()
                        }
                        if (value == "NG") {
                            val asCustom = pop.asCustom(failPop)
                            if (asCustom.isShow) return@forEach
                            asCustom.show()
                        }
                    }
                }
            }
        }

    }

}