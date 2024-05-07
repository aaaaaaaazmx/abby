package com.cl.modules_my.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UpdateInfoReq
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.letMultiple
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.BaseInputPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.adapter.DeviceAutomationAdapter
import com.cl.modules_my.databinding.MyDeviceAutomationBinding
import com.cl.modules_my.request.OpenAutomationReq
import com.cl.modules_my.request.UpdateSubportReq
import com.cl.modules_my.viewmodel.DeviceAutomationViewModel
import com.cl.modules_my.widget.AutomationEditPop
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.thingclips.smart.camera.middleware.p2p.ThingSmartCameraP2P
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 设备自动化信息界面
 */
@Route(path = RouterPath.My.PAGE_MY_DEVICE_AUTOMATION)
@AndroidEntryPoint
class DeviceAutomationActivity : BaseActivity<MyDeviceAutomationBinding>() {
    @Inject
    lateinit var mViewModel: DeviceAutomationViewModel

    /**
     * 设备ID
     */
    private val deviceId by lazy {
        intent.getStringExtra(BasePopActivity.KEY_DEVICE_ID)
    }

    /**
     * 配件的设备ID
     */
    private val accessoryDeviceId by lazy {
        intent.getStringExtra("accessoryDeviceId")
    }

    /**
     * 当前端口的开关状态
     */
    private val status by lazy {
        intent.getBooleanExtra("status", false)
    }

    /**
     * 配件ID
     */
    private val accessoryId by lazy {
        intent.getStringExtra(BasePopActivity.KEY_PART_ID)
    }

    /**
     * 排插的端口ID
     */
    private val portId by lazy {
        intent.getStringExtra("portId")
    }

    /**
     * relationId、用于删除和修改配件 必须的
     */
    private val relationId by lazy {
        intent.getStringExtra("relationId")
    }

    /**
     * 排插名字
     */
    private val portName by lazy {
        intent.getStringExtra("portName")
    }

    private val adapter by lazy {
        DeviceAutomationAdapter(mutableListOf()) { automationId, isCheck ->
            letMultiple(accessoryId, deviceId) { a, b ->
                val req = OpenAutomationReq(
                    accessoryId = a,
                    automationId = automationId,
                    deviceId = b,
                    status = if (isCheck) 1 else 0,
                    usbPort = mViewModel.setUsbPort.value
                )
                mViewModel.automationSwitch(req)
            }
        }
    }

    /**
     * 重新回到这个界面，需要重新刷新数据
     */
    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        letMultiple(accessoryId, deviceId) { a, b ->
            if (portId.isNullOrEmpty()) {
                mViewModel.getRuleList(a, b)
            } else {
                mViewModel.getRuleList(a, b, portId)
            }
        }
    }

    override fun initView() {
        binding.rvDeivceAutoInfo.layoutManager = LinearLayoutManager(this)
        binding.rvDeivceAutoInfo.adapter = adapter
        letMultiple(accessoryId, deviceId) { a, b ->
            if (portId.isNullOrEmpty()) {
                mViewModel.getRuleList(a, b)
            } else {
                mViewModel.getRuleList(a, b, portId)
            }
        }
        binding.ftbTitle.setLeftClickListener {
            setResult(RESULT_OK)
            finish()
        }

        // 清除名字
        binding.etEmail.setSafeOnClickListener(lifecycleScope) {
            xpopup(this@DeviceAutomationActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(false)
                asCustom(
                    BaseInputPop(this@DeviceAutomationActivity, title = "Outlet Name", hintText = binding.etEmail.text.toString(), onConfirmAction = {
                        // 上传名字
                        binding.etEmail.text = it
                        // 保存名字\修改配件信息
                        val req = UpdateSubportReq(
                            accessoryDeviceId = accessoryDeviceId,
                            portId = portId,
                            status = status,
                            subName = it
                        )
                        mViewModel.updateAccessory(req)
                    })
                ).show()
            }
        }
    }


    override fun MyDeviceAutomationBinding.initBinding() {
        portId = this@DeviceAutomationActivity.portId
        portName = this@DeviceAutomationActivity.portName
        lifecycleOwner = this@DeviceAutomationActivity
        executePendingBindings()
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    @SuppressLint("CheckResult")
    override fun observe() {
        mViewModel.apply {
            /**
             * 修改配件信息
             */
            updateAccessory.observe(this@DeviceAutomationActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    ToastUtil.shortShow("Update successfully")
                }
            })
            saveCameraSetting.observe(this@DeviceAutomationActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }

                loading { showProgressLoading() }

                success {
                    hideProgressLoading()
                    if (mViewModel.isUnbind.value == true) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        // 保存成功
                        ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                            .navigation()
                        finish()
                    }
                }
            })

            // 删除自动化
            deleteAutomation.observe(this@DeviceAutomationActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    letMultiple(accessoryId, deviceId) { a, b ->
                        if (portId.isNullOrEmpty()) {
                            mViewModel.getRuleList(a, b)
                        } else {
                            mViewModel.getRuleList(a, b, portId)
                        }
                    }
                }
            })
            // 自动化开关
            automationSwitch.observe(this@DeviceAutomationActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    letMultiple(accessoryId, deviceId) { a, b ->
                        if (portId.isNullOrEmpty()) {
                            mViewModel.getRuleList(a, b)
                        } else {
                            mViewModel.getRuleList(a, b, portId)
                        }
                    }
                }
            })
            // 规则列表
            ruleList.observe(this@DeviceAutomationActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    // 设置usbPort
                    setUsbPort(data?.usbPort)
                    data?.image?.let {
                        val requestOptions = RequestOptions()
                        requestOptions.placeholder(R.mipmap.placeholder)
                        requestOptions.error(R.mipmap.errorholder)
                        /*requestOptions.override(
                            Target.SIZE_ORIGINAL,
                            Target.SIZE_ORIGINAL
                        )*/
                        Glide.with(this@DeviceAutomationActivity).load(it)
                            .apply(requestOptions)
                            .into(binding.ivIcon)
                    }

                    data?.accessoryName?.let {
                        binding.tvDeviceName.text = it
                    }

                    data?.title?.let {
                        binding.ftbTitle.setTitle(it)
                    }

                    // 状态显示
                    val openSize = data?.list?.filter { it.status == 1 }?.size ?: 0
                    ViewUtils.setVisible(openSize == 0, binding.ftCheck)
                    ViewUtils.setVisible(openSize != 0, binding.tvAutoDesc)
                    binding.etEmail.text = data?.subName
                    data?.status?.let {
                        binding.ftCheck.setItemChecked(it == 1)
                    }
                    // 主开关需要开启，才显示
                    binding.tvAutoDesc.text =
                        if (data?.status == 1 && openSize == 1) "Auto\nOn" else "Auto\nOff"

                    adapter.setList(data?.list)
                }
            })

            // 配件的开关状态
            accessoryStatus.observe(this@DeviceAutomationActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    letMultiple(accessoryId, deviceId) { a, b ->
                        if (portId.isNullOrEmpty()) {
                            mViewModel.getRuleList(a, b)
                        } else {
                            mViewModel.getRuleList(a, b, portId)
                        }
                    }
                }
            })
        }
    }

    override fun initData() {
        binding.ftCheck.setSwitchCheckedChangeListener { _, isChecked ->
            letMultiple(accessoryId, deviceId) { a, b ->
                mViewModel.getAccessoryStatus(a, b, if (isChecked) "1" else "0", mViewModel.setUsbPort.value)
            }
        }

        binding.unbindCamera.setOnClickListener {
            XPopup.Builder(this@DeviceAutomationActivity).isDestroyOnDismiss(false)
                .dismissOnTouchOutside(false).asCustom(
                    BaseCenterPop(this@DeviceAutomationActivity,
                        content = "Are you certain you wish to delete",
                        cancelText = "No",
                        confirmText = "Yes",
                        onCancelAction = {},
                        onConfirmAction = { // 解绑设备
                            // 上传解绑状态
                            mViewModel.setUnbind(true)
                            mViewModel.cameraSetting(
                                UpdateInfoReq(
                                    binding = false,
                                    deviceId = deviceId,
                                    relationId = relationId.toString()
                                )
                            )
                        })
                ).show()
            /* xpopup {
                 title("解绑摄像头")
                 content("确定要解绑摄像头吗？")
                 positiveButton("确定") {
                     unBindDevice()
                     // 解绑相机
                     accessoryDeviceId?.let { it1 ->
                         tuyaUtils.unBindCamera(it1, onErrorAction = {
                             ToastUtil.shortShow(it)
                         }) {
                             // 绑定成功，结束当前页面，刷新配件列表
                             setResult(Activity.RESULT_OK)
                             finish()
                         }
                     }
                 }
                 negativeButton("取消") {
                     dismiss()
                 }
             }.show()*/
        }

        binding.ivAddDevice.setOnClickListener {
            val isDefault =
                mViewModel.ruleList.value?.data?.list?.firstOrNull { data -> data.isDefault == 1 }
            // 有默认、并且是开启状态
            if (isDefault != null && isDefault.status == 1) {
                XPopup.Builder(this@DeviceAutomationActivity)
                    .isDestroyOnDismiss(false)
                    .dismissOnTouchOutside(false)
                    .asCustom(
                        BaseCenterPop(this@DeviceAutomationActivity,
                            content = "Adding an automation will turn off the default smart profile. Are you sure you want to proceed?",
                            cancelText = "Cancel",
                            confirmText = "Confirm",
                            onConfirmAction = {
                                // 先关闭默认的
                                val req = OpenAutomationReq(
                                    accessoryId = accessoryId,
                                    status = 0,
                                    automationId = "${isDefault.automationId}",
                                    deviceId = deviceId,
                                    usbPort = mViewModel.setUsbPort.value
                                )
                                mViewModel.automationSwitch(req)
                                // 重新获取一次
                                letMultiple(accessoryId, deviceId) { a, b ->
                                    if (portId.isNullOrEmpty()) {
                                        mViewModel.getRuleList(a, b)
                                    } else {
                                        mViewModel.getRuleList(a, b, portId)
                                    }
                                }

                                //  跳转到添加自定义规则界面
                                val intent = Intent(
                                    this@DeviceAutomationActivity,
                                    AddAutomationActivity::class.java
                                )
                                intent.putExtra("portId", portId)
                                intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceId)
                                intent.putExtra(BasePopActivity.KEY_PART_ID, accessoryId)
                                intent.putExtra(BasePopActivity.KEY_USB_PORT, mViewModel.setUsbPort.value)
                                startActivity(intent)
                            })
                    ).show()
            } else {
                //  跳转到添加自定义规则界面
                val intent =
                    Intent(this@DeviceAutomationActivity, AddAutomationActivity::class.java)
                intent.putExtra("portId", portId)
                intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceId)
                intent.putExtra(BasePopActivity.KEY_PART_ID, accessoryId)
                intent.putExtra(BasePopActivity.KEY_USB_PORT, mViewModel.setUsbPort.value)
                startActivity(intent)
            }
        }

        adapter.addChildClickViewIds(com.cl.modules_my.R.id.iv_edit)
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                com.cl.modules_my.R.id.iv_edit -> {
                    // 居中显示
                    XPopup.Builder(this@DeviceAutomationActivity)
                        .popupPosition(PopupPosition.Bottom)
                        .dismissOnTouchOutside(true)
                        .isClickThrough(false)  //点击透传
                        .hasShadowBg(true) // 去掉半透明背景
                        //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                        .atView(view)
                        .isCenterHorizontal(false)
                        .asCustom(
                            AutomationEditPop(
                                this@DeviceAutomationActivity,
                                onEditClick = {
                                    // 跳转到添加自定义规则界面
                                    val intent = Intent(
                                        this@DeviceAutomationActivity,
                                        AddAutomationActivity::class.java
                                    )
                                    intent.putExtra("portId", portId)
                                    intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceId)
                                    intent.putExtra(BasePopActivity.KEY_PART_ID, accessoryId)
                                    intent.putExtra(BasePopActivity.KEY_USB_PORT, mViewModel.setUsbPort.value)
                                    intent.putExtra(
                                        BasePopActivity.KEY_AUTOMATION_ID,
                                        "${adapter.data[position].automationId}"
                                    )
                                    startActivity(intent)
                                },
                                onDeleteClick = {
                                    XPopup.Builder(this@DeviceAutomationActivity)
                                        .isDestroyOnDismiss(false)
                                        .dismissOnTouchOutside(false)
                                        .asCustom(
                                            BaseCenterPop(this@DeviceAutomationActivity,
                                                content = "Are you sure you want to delete this automation?",
                                                cancelText = "No",
                                                confirmText = "Yes",
                                                onConfirmAction = {
                                                    // 删除
                                                    mViewModel.deleteAutomation(
                                                        "${adapter.data[position].automationId}"
                                                    )
                                                })
                                        ).show()
                                }).setBubbleBgColor(Color.WHITE) //气泡背景
                                .setArrowWidth(XPopupUtils.dp2px(this@DeviceAutomationActivity, 3f))
                                .setArrowHeight(
                                    XPopupUtils.dp2px(
                                        this@DeviceAutomationActivity,
                                        3f
                                    )
                                )
                                //.setBubbleRadius(100)
                                .setArrowRadius(
                                    XPopupUtils.dp2px(
                                        this@DeviceAutomationActivity,
                                        2f
                                    )
                                )
                        ).show()
                }
            }
        }
    }
}