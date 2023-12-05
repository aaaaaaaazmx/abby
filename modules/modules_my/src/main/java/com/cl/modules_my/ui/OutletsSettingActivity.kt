package com.cl.modules_my.ui

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UpdateInfoReq
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.adapter.OutletsAdapter
import com.cl.modules_my.databinding.MyOutletsSettingActivityBinding
import com.cl.modules_my.viewmodel.MyOutletsViewModel
import com.thingclips.smart.camera.middleware.p2p.ThingSmartCameraP2P
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 排插设置界面
 */
@AndroidEntryPoint
class OutletsSettingActivity : BaseActivity<MyOutletsSettingActivityBinding>() {

    @Inject
    lateinit var mViewMode: MyOutletsViewModel

    private val accessoryId by lazy {
        intent.getIntExtra("accessoryId", -1)
    }

    private val accessoryDeviceId by lazy {
        intent.getStringExtra("accessoryDeviceId")
    }

    private val deviceId by lazy {
        intent.getStringExtra("deviceId")
    }

    private val adapter by lazy {
        OutletsAdapter(mutableListOf())
    }

    override fun MyOutletsSettingActivityBinding.initBinding() {
        lifecycleOwner = this@OutletsSettingActivity
        viewModel = this@OutletsSettingActivity.mViewMode
        executePendingBindings()
    }

    override fun initView() {
        mViewMode.getSupport(accessoryId.toString(), accessoryDeviceId)

        binding.rvDevice.layoutManager = LinearLayoutManager(this@OutletsSettingActivity)
        binding.rvDevice.adapter = adapter

        binding.ftbTitle.setLeftClickListener {
            startActivity(Intent(this@OutletsSettingActivity, DeviceListActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@OutletsSettingActivity, DeviceListActivity::class.java))
        finish()
    }

    override fun observe() {
        mViewMode.apply {
            accessorySubport.observe(this@OutletsSettingActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    adapter.setList(data?.list)
                }
            })

            saveCameraSetting.observe(this@OutletsSettingActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    ToastUtil.shortShow("Unbound successfully")
                    finish()
                }
            })
        }
    }

    override fun initData() {
        binding.unbindCamera.setSafeOnClickListener(lifecycleScope) {
            xpopup(this@OutletsSettingActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(true)
                asCustom(
                    BaseCenterPop(this@OutletsSettingActivity,
                        content = "Are you certain you wish to delete the Outlets?",
                        cancelText = "No",
                        confirmText = "Yes",
                        onCancelAction = {},
                        onConfirmAction = {
                            // 删除配件
                            ThingHomeSdk.newDeviceInstance(accessoryDeviceId).removeDevice(object : IResultCallback {
                                override fun onError(code: String?, error: String?) {
                                    logI("123123: $code --> $error")
                                    if (code == "11002") {
                                        // 解除绑定
                                        mViewMode.cameraSetting(UpdateInfoReq(binding = false, deviceId = deviceId))
                                        return
                                    }
                                    ToastUtil.shortShow(error)
                                }

                                override fun onSuccess() {
                                    // 解除绑定
                                    mViewMode.cameraSetting(UpdateInfoReq(binding = false, deviceId = deviceId))
                                }
                            })
                        })
                ).show()
            }
        }
    }
}