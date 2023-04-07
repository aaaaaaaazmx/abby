package com.cl.modules_my.ui

import android.content.Intent
import android.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.letMultiple
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.adapter.DeviceAutomationAdapter
import com.cl.modules_my.databinding.MyDeviceAutomationBinding
import com.cl.modules_my.request.OpenAutomationReq
import com.cl.modules_my.viewmodel.DeviceAutomationViewModel
import com.cl.modules_my.widget.AutomationEditPop
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
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
     * 配件ID
     */
    private val accessoryId by lazy {
        intent.getStringExtra(BasePopActivity.KEY_PART_ID)
    }

    private val adapter by lazy {
        DeviceAutomationAdapter(mutableListOf()) { automationId, isCheck ->
            letMultiple(accessoryId, deviceId) { a, b ->
                val req = OpenAutomationReq(
                    accessoryId = a,
                    automationId = automationId,
                    deviceId = b,
                    status = if (isCheck) 1 else 0
                )
                mViewModel.automationSwitch(req)
            }
        }
    }

    /**
     * 重新回到这个界面，需要重新刷新数据
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        letMultiple(accessoryId, deviceId) { a, b ->
            mViewModel.getRuleList(a, b)
        }
    }

    override fun initView() {
        binding.rvDeivceAutoInfo.layoutManager = LinearLayoutManager(this)
        binding.rvDeivceAutoInfo.adapter = adapter
        letMultiple(accessoryId, deviceId) { a, b ->
            mViewModel.getRuleList(a, b)
        }
        binding.ftbTitle.setLeftClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    override fun observe() {
        mViewModel.apply {
            // 删除自动化
            deleteAutomation.observe(this@DeviceAutomationActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    letMultiple(accessoryId, deviceId) { a, b ->
                        mViewModel.getRuleList(a, b)
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
                        mViewModel.getRuleList(a, b)
                    }
                }
            })
            // 规则列表
            ruleList.observe(this@DeviceAutomationActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    data?.image?.let {
                        val requestOptions = RequestOptions()
                        requestOptions.placeholder(R.mipmap.placeholder)
                        requestOptions.error(R.mipmap.errorholder)
                        requestOptions.override(
                            com.bumptech.glide.request.target.Target.SIZE_ORIGINAL,
                            Target.SIZE_ORIGINAL
                        )
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

                    data?.status?.let {
                        binding.ftCheck.setItemChecked(it == 1)
                    }

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
                        mViewModel.getRuleList(a, b)
                    }
                }
            })
        }
    }

    override fun initData() {
        binding.ftCheck.setSwitchCheckedChangeListener { _, isChecked ->
            letMultiple(accessoryId, deviceId) { a, b ->
                mViewModel.getAccessoryStatus(a, b, if (isChecked) "1" else "0")
            }
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
                                    deviceId = deviceId
                                )
                                mViewModel.automationSwitch(req)
                                // 重新获取一次
                                letMultiple(accessoryId, deviceId) { a, b ->
                                    mViewModel.getRuleList(a, b)
                                }

                                //  跳转到添加自定义规则界面
                                val intent = Intent(
                                    this@DeviceAutomationActivity,
                                    AddAutomationActivity::class.java
                                )
                                intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceId)
                                intent.putExtra(BasePopActivity.KEY_PART_ID, accessoryId)
                                startActivity(intent)
                            })
                    ).show()
            } else {
                //  跳转到添加自定义规则界面
                val intent =
                    Intent(this@DeviceAutomationActivity, AddAutomationActivity::class.java)
                intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceId)
                intent.putExtra(BasePopActivity.KEY_PART_ID, accessoryId)
                startActivity(intent)
            }
        }

        adapter.addChildClickViewIds(com.cl.modules_my.R.id.iv_edit)
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                /* R.id.cl_root -> {
                     //  跳转到自定义规则详情界面
                     val intent = Intent(this@DeviceAutomationActivity, AddAutomationActivity::class.java)
                     intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceId)
                     intent.putExtra(BasePopActivity.KEY_PART_ID, accessoryId)
                     startActivity(intent)
                 }*/

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
                                    intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceId)
                                    intent.putExtra(BasePopActivity.KEY_PART_ID, accessoryId)
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