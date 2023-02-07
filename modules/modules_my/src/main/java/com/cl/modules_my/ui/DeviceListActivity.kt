package com.cl.modules_my.ui

import android.app.Activity
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UpPlantInfoReq
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.report.Reporter
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.DeviceListAdapter
import com.cl.modules_my.databinding.MyDeviceListActivityBinding
import com.cl.modules_my.pop.EditPlantProfilePop
import com.cl.modules_my.pop.MergeAccountPop
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.modules_my.viewmodel.ListDeviceViewModel
import com.cl.modules_my.widget.MyDeleteDevicePop
import com.lxj.xpopup.XPopup
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.IResultCallback
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileInputStream
import javax.inject.Inject


/**
 * 设备列表界面
 */
@Route(path = RouterPath.My.PAGE_MY_DEVICE_LIST)
@AndroidEntryPoint
class DeviceListActivity : BaseActivity<MyDeviceListActivityBinding>() {
    private val adapter by lazy {
        DeviceListAdapter(mutableListOf())
    }

    @Inject
    lateinit var mViewModel: ListDeviceViewModel


    override fun initView() {
        binding.rvList.layoutManager = LinearLayoutManager(this@DeviceListActivity)
        binding.rvList.adapter = adapter

        binding.title.setLeftClickListener {
            isSwitchDevice()
        }
    }

    override fun onBackPressed() {
        isSwitchDevice()
    }

    private fun isSwitchDevice() {
        val list = adapter.data
        if (list.isEmpty()) {
            finish()
            return
        }
        list.firstOrNull { it.isChooser == true }?.apply {
            if (deviceId == mViewModel.tuyaDeviceBean()?.devId) {
                finish()
            } else {
                // 删除原先的、或者切换了设备
                // 跳转到主页、加载。
                // 切换了主页，应该直接回到首页、在合并界面也能跳转到这个地方。应该需要使用其他的方法。
                // 改用Eventbus吧。
                // 切换了设备，需要重新刷新主页。
                LiveEventBus.get().with(Constants.Global.KEY_IS_SWITCH_DEVICE, String::class.java)
                    .postEvent(deviceId)
                /*setResult(RESULT_OK, Intent().putExtra(Constants.Global.KEY_IS_SWITCH_DEVICE, deviceId))*/
                finish()
            }
        }
    }

    /**
     * 再次跳转进来
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mViewModel.listDevice()
    }

    override fun observe() {
        // 获取设备列表
        mViewModel.listDevice.observe(this@DeviceListActivity, resourceObserver {
            loading { showProgressLoading() }
            error { errorMsg, code ->
                hideProgressLoading()
                ToastUtil.shortShow(errorMsg)
            }
            success {
                hideProgressLoading()
                data?.indexOfFirst { it.deviceId == mViewModel.tuyaDeviceBean()?.devId }?.apply {
                    if (data.isNullOrEmpty()) return@success
                    data?.get(this)?.isChooser = true
                }
                adapter.setList(data)
            }
        })


        // 删除设备
        mViewModel.deleteDevice.observe(this@DeviceListActivity, resourceObserver {
            loading { showProgressLoading() }
            error { errorMsg, code ->
                hideProgressLoading()
                ToastUtil.shortShow(errorMsg)
            }
            success {
                hideProgressLoading()
                mViewModel.listDevice()
            }
        })

        // 切换设备
        mViewModel.switchDevice.observe(this@DeviceListActivity, resourceObserver {
            loading { showProgressLoading() }
            error { errorMsg, code ->
                ToastUtil.shortShow(errorMsg)
                hideProgressLoading()
            }
            success {
                hideProgressLoading()
            }
        })

        // 修改植物属性
        mViewModel.updatePlantInfo.observe(this@DeviceListActivity, resourceObserver {
            loading { showProgressLoading() }
            error { errorMsg, code ->
                ToastUtil.shortShow(errorMsg)
                hideProgressLoading()
            }
            success {
                hideProgressLoading()
                mViewModel.listDevice()
            }
        })
    }

    // 统一xPopUp
    private val pop by lazy {
        // 升级提示框
        XPopup.Builder(this@DeviceListActivity).isDestroyOnDismiss(false).enableDrag(false)
            .dismissOnTouchOutside(false)
    }

    override fun initData() {
        binding.ivAddDevice.setOnClickListener {
            PermissionHelp().checkConnectForTuYaBle(this@DeviceListActivity,
                object : PermissionHelp.OnCheckResultListener {
                    override fun onResult(result: Boolean) {
                        if (!result) return
                        // 如果权限都已经同意了
                        ARouter.getInstance().build(RouterPath.PairConnect.PAGE_PLANT_SCAN)
                            .navigation()
                    }
                })
        }

        mViewModel.listDevice()

        adapter.addChildClickViewIds(R.id.btn_chang, R.id.btn_delete, R.id.cl_root)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val deviceBean = (adapter.data[position] as? ListDeviceBean)
            when (view.id) {
                R.id.btn_chang -> {
                    //  修改属性、弹窗pop
                    XPopup.Builder(this@DeviceListActivity).isDestroyOnDismiss(false).dismissOnTouchOutside(false)
                        .asCustom(EditPlantProfilePop(this@DeviceListActivity, plantName = deviceBean?.plantName, strainName = deviceBean?.strainName, onConfirmAction = { plantName, strainName ->
                            // 修改属性名
                            if (strainName.isNullOrEmpty() && plantName?.isNotEmpty() == true) {
                                mViewModel.updatePlantInfo(UpPlantInfoReq(plantName = plantName, plantId = deviceBean?.plantId))
                            }
                            if (plantName.isNullOrEmpty() && strainName?.isNotEmpty() == true) {
                                mViewModel.updatePlantInfo(UpPlantInfoReq(strainName = strainName, plantId = deviceBean?.plantId))
                            } else {
                                mViewModel.updatePlantInfo(UpPlantInfoReq(strainName = strainName, plantName = plantName, plantId = deviceBean?.plantId))
                            }
                        })).show()
                }
                R.id.btn_delete -> {
                    XPopup.Builder(this@DeviceListActivity).isDestroyOnDismiss(false).dismissOnTouchOutside(true)
                        .asCustom(MyDeleteDevicePop(this) {
                            TuyaHomeSdk.newDeviceInstance(deviceBean?.deviceId)
                                .removeDevice(object : IResultCallback {
                                    override fun onError(code: String?, error: String?) {
                                        Reporter.reportTuYaError("newDeviceInstance", error, code)
                                        // 删除设备
                                        deviceBean?.deviceId?.let { mViewModel.deleteDevice(it) }
                                    }

                                    override fun onSuccess() {
                                        //  调用接口请求删除设备
                                        // 删除设备
                                        deviceBean?.deviceId?.let { mViewModel.deleteDevice(it) }
                                    }
                                })
                        }).show()

                }

                R.id.cl_root -> {
                    // 更新数据
                    val bean = (adapter.data as? MutableList<ListDeviceBean>) ?: return@setOnItemChildClickListener
                    bean.indexOfFirst { it.isChooser == true }.apply {
                        bean[this].isChooser = false
                    }
                    bean[position].isChooser = true
                    this@DeviceListActivity.adapter.setList(bean)
                }
            }
        }
    }
}