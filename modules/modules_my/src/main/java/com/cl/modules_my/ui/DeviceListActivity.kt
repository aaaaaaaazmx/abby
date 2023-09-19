package com.cl.modules_my.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.activity.result.contract.ActivityResultContracts
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
import com.cl.common_base.bean.LiveDataDeviceInfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.isCanToBigDecimal
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.common_base.widget.FeatureItemSwitch
import com.cl.modules_my.pop.MyChooerTipPop
import com.cl.modules_my.viewmodel.ListDeviceViewModel
import com.cl.modules_my.widget.MyDeleteDevicePop
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileInputStream
import java.io.Serializable
import javax.inject.Inject


/**
 * 设备列表界面
 */
@Route(path = RouterPath.My.PAGE_MY_DEVICE_LIST)
@AndroidEntryPoint
class DeviceListActivity : BaseActivity<MyDeviceListActivityBinding>() {
    private val adapter by lazy {
        DeviceListAdapter(mutableListOf()) { accessoryId, deviceId, isChooser ->
            // 选择设备开关
            mViewModel.setDeviceStatus(accessoryId, deviceId, if (isChooser) "1" else "0")
        }
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
            // 删除原先的、或者切换了设备
            // 跳转到主页、加载。
            // 切换了主页，应该直接回到首页、在合并界面也能跳转到这个地方。应该需要使用其他的方法。
            // 改用Eventbus吧。
            // 切换了设备，需要重新刷新主页。
            logI("123123123: $deviceId,,$spaceType")
            ARouter.getInstance()
                .build(RouterPath.Main.PAGE_MAIN).navigation()
            LiveEventBus.get().with(Constants.Global.KEY_IS_SWITCH_DEVICE, LiveDataDeviceInfoBean::class.java)
                .postEvent(LiveDataDeviceInfoBean(deviceId, spaceType))
        }
        finish()
    }

    /**
     * 再次跳转进来
     */
    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mViewModel.listDevice()
    }

    override fun onResume() {
        super.onResume()
        // 只弹出一次
        kotlin.runCatching {
            if (intent.getBooleanExtra(KEY_SHOW_BIND_POP, false)) {
                // 升级提示框
                XPopup.Builder(this@DeviceListActivity).isDestroyOnDismiss(false).enableDrag(false)
                    .dismissOnTouchOutside(false).asCustom(
                        BaseCenterPop(
                            this@DeviceListActivity,
                            content = "Please note that the merged device needs to be re-paired.",
                            isShowCancelButton = false
                        )
                    ).show()
            }
        }
    }

    override fun observe() {
        // 更改设备附件开光
        mViewModel.deviceStatus.observe(this@DeviceListActivity, resourceObserver {
            error { errorMsg, code ->
                ToastUtil.shortShow(errorMsg)
            }
            success {
                // 刷新设备
                mViewModel.listDevice()
            }
        })
        // 获取设备列表
        mViewModel.listDevice.observe(this@DeviceListActivity, resourceObserver {
            error { errorMsg, code ->
                ToastUtil.shortShow(errorMsg)
            }
            success {
                val dataList = adapter.data
                if (dataList.isNotEmpty()) {
                    dataList.indexOfFirst { it.isChooser == true }.apply {
                        if (this != -1) {
                            data?.get(this)?.isChooser = true
                            return@apply
                        }
                        dataList.indexOfFirst { it.currentDevice == 1 }?.apply {
                            if (this == -1) {
                                if (data.isNullOrEmpty()) return@apply
                                // 如果没有找到相对应的, 选中当前第一个。
                                data?.get(0)?.isChooser = true
                                return@apply
                            }
                            if (data.isNullOrEmpty()) return@apply
                            data?.get(this)?.isChooser = true
                        }
                    }
                } else {
                    data?.indexOfFirst { it.currentDevice == 1 }?.apply {
                        if (this == -1) {
                            if (data.isNullOrEmpty()) return@apply
                            // 如果没有找到相对应的, 选中当前第一个。
                            data?.get(0)?.isChooser = true
                            return@apply
                        }
                        if (data.isNullOrEmpty()) return@apply
                        data?.get(this)?.isChooser = true
                    }
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

            // 弹出是添加帐篷还是添加abby
            XPopup.Builder(this@DeviceListActivity)
                .popupPosition(PopupPosition.Left)
                .dismissOnTouchOutside(true)
                .isClickThrough(false)  //点击透传
                .hasShadowBg(true) // 去掉半透明背景
                //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                .atView(it)
                .isCenterHorizontal(false)
                .asCustom(
                    MyChooerTipPop(
                        this@DeviceListActivity,
                        onPhotoPostAction = {
                            // abby
                            xpopup(this@DeviceListActivity) {
                                isDestroyOnDismiss(false)
                                dismissOnTouchOutside(false)
                                asCustom(
                                    BaseCenterPop(
                                        this@DeviceListActivity,
                                        content = "You’re about to add a new device",
                                        confirmText = "Confirm",
                                        onConfirmAction = {
                                            PermissionHelp().checkConnectForTuYaBle(this@DeviceListActivity,
                                                object : PermissionHelp.OnCheckResultListener {
                                                    override fun onResult(result: Boolean) {
                                                        if (!result) return
                                                        // 如果权限都已经同意了
                                                        ARouter.getInstance()
                                                            .build(RouterPath.PairConnect.PAGE_PLANT_SCAN)
                                                            .navigation()
                                                    }
                                                })
                                        },
                                        isShowCancelButton = true
                                    )
                                ).show()
                            }
                        },
                        onReelPostAction = {
                            //  这是跳转到添加帐篷
                            startActivity(
                                Intent(
                                    this@DeviceListActivity,
                                    GrowSpaceSetActivity::class.java
                                )
                            )
                        }
                    ).setBubbleBgColor(Color.WHITE) //气泡背景
                        .setArrowWidth(XPopupUtils.dp2px(this@DeviceListActivity, 3f))
                        .setArrowHeight(
                            XPopupUtils.dp2px(
                                this@DeviceListActivity,
                                3f
                            )
                        )
                        //.setBubbleRadius(100)
                        .setArrowRadius(
                            XPopupUtils.dp2px(
                                this@DeviceListActivity,
                                3f
                            )
                        )
                ).show()
        }

        mViewModel.listDevice()

        adapter.addChildClickViewIds(
            R.id.btn_chang,
            R.id.btn_add_accessory,
            R.id.btn_jump_to_device,
            R.id.cl_root,
            R.id.iv_luosi,
        )
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val deviceBean = (adapter.data[position] as? ListDeviceBean)
            logI("123131231: ${deviceBean?.deviceId},,,,${deviceBean?.nightTimer}")
            when (view.id) {
                R.id.iv_luosi -> {
                    // camera跳转到专属页面
                    if (deviceBean?.accessoryList?.get(0)?.accessoryName == "Smart Camera") {
                        val accessoryDeviceId = deviceBean.accessoryList?.get(0)?.accessoryDeviceId
                        startActivityLauncher.launch(
                            Intent(
                                this@DeviceListActivity,
                                CameraSettingActivity::class.java
                            ).apply {
                                // 配件Id 就是cameraId
                                putExtra("accessoryDeviceId", accessoryDeviceId)
                                putExtra("deviceId", deviceBean.deviceId)
                            })
                        return@setOnItemChildClickListener
                    }
                    val intent =
                        Intent(this@DeviceListActivity, DeviceAutomationActivity::class.java)
                    intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceBean?.deviceId)
                    intent.putExtra(
                        BasePopActivity.KEY_PART_ID,
                        "${deviceBean?.accessoryList?.get(0)?.accessoryId}"
                    )
                    startActivityLauncher.launch(intent)
                }

                R.id.btn_chang -> {
                    // 是帐篷
                    if (deviceBean?.spaceType != ListDeviceBean.KEY_SPACE_TYPE_BOX) {
                        startActivity(
                            Intent(
                                this@DeviceListActivity,
                                GrowSpaceSetActivity::class.java
                            ).apply {
                                putExtra(
                                    GrowSpaceSetActivity.KEY_DEVICE_DETAIL_INFO,
                                    deviceBean?.deviceId
                                )
                            })
                        return@setOnItemChildClickListener
                    }

                    //  修改属性、弹窗pop
                    XPopup.Builder(this@DeviceListActivity).isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(false)
                        .autoOpenSoftInput(false)
                        .autoFocusEditText(false)
                        .asCustom(EditPlantProfilePop(this@DeviceListActivity,
                            beanData = deviceBean,
                            plantName = deviceBean?.plantName,
                            strainName = deviceBean?.strainName,
                            onConfirmAction = { plantName, strainName ->
                                // 修改属性名
                                if (strainName.isNullOrEmpty() && plantName?.isNotEmpty() == true) {
                                    mViewModel.updatePlantInfo(
                                        UpPlantInfoReq(
                                            plantName = plantName,
                                            plantId = deviceBean?.plantId
                                        )
                                    )
                                }
                                if (plantName.isNullOrEmpty() && strainName?.isNotEmpty() == true) {
                                    mViewModel.updatePlantInfo(
                                        UpPlantInfoReq(
                                            strainName = strainName,
                                            plantId = deviceBean?.plantId
                                        )
                                    )
                                } else {
                                    mViewModel.updatePlantInfo(
                                        UpPlantInfoReq(
                                            strainName = strainName,
                                            plantName = plantName,
                                            plantId = deviceBean?.plantId
                                        )
                                    )
                                }
                            },
                            onDeviceChanged = {
                                mViewModel.listDevice()
                            }
                        )).show()
                }
                /*R.id.btn_delete -> {
                    XPopup.Builder(this@DeviceListActivity).isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(true)
                        .asCustom(MyDeleteDevicePop(this) {
                            ThingHomeSdk.newDeviceInstance(deviceBean?.deviceId)
                                .removeDevice(object : IResultCallback {
                                    override fun onError(code: String?, error: String?) {
                                        ToastUtil.shortShow(error)
                                        Reporter.reportTuYaError("newDeviceInstance", error, code)
                                        deviceBean?.deviceId?.let { mViewModel.deleteDevice(it) }
                                    }

                                    override fun onSuccess() {
                                        //  调用接口请求删除设备
                                        // 删除设备
                                        deviceBean?.deviceId?.let { mViewModel.deleteDevice(it) }
                                    }
                                })
                        }).show()

                }*/

                /**
                 * 添加设备附件
                 */
                R.id.btn_add_accessory -> {
                    val intent = Intent(this@DeviceListActivity, AddAccessoryActivity::class.java)
                    intent.putExtra("deviceId", deviceBean?.deviceId)
                    intent.putExtra("accessoryList", deviceBean?.accessoryList as Serializable?)
                    intent.putExtra("spaceType", deviceBean?.spaceType)
                    startActivity(intent)
                }

                R.id.cl_root -> {
                    // 更新数据
                    val bean = (adapter.data as? MutableList<ListDeviceBean>)
                        ?: return@setOnItemChildClickListener
                    bean.indexOfFirst { it.isChooser == true }.apply {
                        if (this == -1) {
                            return@apply
                        }
                        bean[this].isChooser = false
                    }
                    bean[position].isChooser = true
                    this@DeviceListActivity.adapter.setList(bean)
                }

                /**
                 * 跳转到首页
                 */
                R.id.btn_jump_to_device -> {
                    // todo 跳转到首页
                    // (data.period.equals("No plant") &amp;&amp; data.isChooser &amp;&amp; !data.onlineStatus.equals("Offline")) ? View.VISIBLE : View.GONE
                    this.adapter.data.firstOrNull { it.isChooser == true }?.apply {
                        if (onlineStatus.equals("Offline")) {
                            // 如果权限都已经同意了
                            ARouter.getInstance().build(RouterPath.PairConnect.PAGE_PLANT_SCAN)
                                .navigation()
                            return@apply
                        }
                        // 跳转到主页、加载。
                        // 切换了主页，应该直接回到首页、在合并界面也能跳转到这个地方。应该需要使用其他的方法。
                        // 改用Eventbus吧。
                        // 切换了设备，需要重新刷新主页。
                        ARouter.getInstance()
                            .build(RouterPath.Main.PAGE_MAIN).navigation()
                        LiveEventBus.get()
                            .with(Constants.Global.KEY_IS_SWITCH_DEVICE, LiveDataDeviceInfoBean::class.java)
                            .postEvent(LiveDataDeviceInfoBean(deviceId, spaceType))
                        finish()
                    }
                }
            }
        }
    }

    /**
     * 回调刷新页面
     */
    private val startActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                // 重新请求数据
                mViewModel.listDevice()
            }
        }


    companion object {
        const val KEY_SHOW_BIND_POP = "key_show_bind_pop"
    }

}