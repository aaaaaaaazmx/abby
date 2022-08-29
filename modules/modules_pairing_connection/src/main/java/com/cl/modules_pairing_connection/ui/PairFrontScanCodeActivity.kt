package com.cl.modules_pairing_connection.ui

import android.Manifest
import cn.mtjsoft.barcodescanning.ScanningManager
import cn.mtjsoft.barcodescanning.config.Config
import cn.mtjsoft.barcodescanning.config.ScanType.Companion.QR_CODE
import cn.mtjsoft.barcodescanning.interfaces.ScanResultListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.glide.GlideEngineForScanCode
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
                    DeviceControl.get().success {
                        // 上报成功，那么啥也不管了，
                        pop.asCustom(ActivationSucceededPop(this@PairFrontScanCodeActivity) {
                            finish()
                        }).show()
                    }.error { code, error ->
                        // 上报失败，其实也啥也不管了，
                        pop.asCustom(failPop).show()
                    }.repairSN("OK")
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
            "You need to grant hey abby permission to take photos and also give hey abby access to photos to add recent photos.",
            object : PermissionHelp.OnCheckResultListener {
                override fun onResult(result: Boolean) {
                    if (!result) return
                    startScanCode()
                }
            },
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun startScanCode() {
        ScanningManager.instance.openScanningActivity(
            this,
            Config(
                true,
                QR_CODE,
                GlideEngineForScanCode.createGlideEngine(),
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

                        if (value.length == 10 || value.length == 18) {
                            // 1、先检查这个扫出来的结果是否有效
                            mViewModel.checkSN(value)
                        }
                    }
                })
        )

    }

}