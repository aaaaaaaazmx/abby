package com.cl.modules_pairing_connection.ui

import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.PairLocationPop
import com.cl.common_base.report.Reporter
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.lcoation.LocationUtil
import com.cl.common_base.util.network.NetWorkUtil
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_pairing_connection.databinding.PairPlantHomeBinding
import com.cl.modules_pairing_connection.widget.PairLoginOutPop
import com.lxj.xpopup.XPopup
import com.thingclips.smart.android.user.api.ILogoutCallback
import com.thingclips.smart.home.sdk.ThingHomeSdk
import dagger.hilt.android.AndroidEntryPoint

/**
 * 没有设备-添加扫描以及配网
 */
@Route(path = RouterPath.PairConnect.PAGE_PLANT_CHECK)
@AndroidEntryPoint
class PlantActivity : BaseActivity<PairPlantHomeBinding>() {
    /**
     * 确认退出弹窗
     */
    private val confirm by lazy {
        XPopup.Builder(this@PlantActivity)
            .isDestroyOnDismiss(false)
            .dismissOnTouchOutside(false)
            .asCustom(PairLoginOutPop(this) {
                ThingHomeSdk.getUserInstance().logout(object : ILogoutCallback {
                    override fun onSuccess() {
                        // 清除缓存数据
                        Prefs.removeKey(Constants.Login.KEY_LOGIN_DATA_TOKEN)
                        // 清除上面所有的Activity
                        // 跳转到Login页面
                        ARouter.getInstance().build(RouterPath.LoginRegister.PAGE_LOGIN)
                            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            .navigation()
                    }

                    override fun onError(code: String?, error: String?) {
                        logE(
                            """
                           logout -> onError:
                            code: $code
                            error: $error
                        """.trimIndent()
                        )
                        ToastUtil.shortShow(error)
                        Reporter.reportTuYaError("getUserInstance", error, code)
                    }
                })
            })
    }

    override fun initView() {
        binding.title.setLeftText("Log out", true)
            .setTitle("Add Device")
            .setLeftClickListener { // 退出
                confirm.show()
            }
    }

    override fun observe() {
    }

    override fun initData() {
        // 跳转到第一页引导
        binding.tvScan.setOnClickListener {
            checkPer()
        }
    }

    private fun checkPer() {
        PermissionHelp().checkConnectForTuYaBle(
            this@PlantActivity,
            object :
                PermissionHelp.OnCheckResultListener {
                override fun onResult(result: Boolean) {
                    if (!result) return
                    // 如果权限都已经同意了,
                    startActivity(
                        Intent(
                            this@PlantActivity,
                            PairOnePageActivity::class.java
                        )
                    )
                }
            })
    }
}