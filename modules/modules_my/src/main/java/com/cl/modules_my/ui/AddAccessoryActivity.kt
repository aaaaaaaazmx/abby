package com.cl.modules_my.ui

import android.app.Activity
import android.content.Intent
import android.telephony.SmsManager
import android.view.View
import android.widget.ResourceCursorAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.internal.ZslControlNoOpImpl
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.base.KnowMoreActivity
import com.cl.common_base.bean.FinishTaskReq
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.AddAccessoryAdapter
import com.cl.modules_my.databinding.MyAddAccessoryBinding
import com.cl.modules_my.repository.AccessoryListBean
import com.cl.modules_my.viewmodel.AddAccessoryViewModel
import com.cl.modules_my.viewmodel.SettingViewModel
import com.lxj.xpopup.XPopup
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 添加附件页面
 */
@AndroidEntryPoint
class AddAccessoryActivity : BaseActivity<MyAddAccessoryBinding>() {
    @Inject
    lateinit var mViewModel: AddAccessoryViewModel

    private val adapter by lazy {
        AddAccessoryAdapter(mutableListOf())
    }

    // 设备ID
    private val deviceId by lazy {
        intent.getStringExtra("deviceId")
    }

    // 智能设备数量
    private val accessoryList by lazy {
        intent.getSerializableExtra("accessoryList") as? MutableList<ListDeviceBean.AccessoryList>
    }

    override fun initView() {
        mViewModel.getAccessoryList()

        binding.rvList.layoutManager = GridLayoutManager(this, 2)
        binding.rvList.adapter = adapter
    }

    override fun observe() {
        mViewModel.apply {
            accessoryList.observe(this@AddAccessoryActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    adapter.setList(data)
                }
            })
        }
    }

    override fun initData() {
        binding.ftbTitle.setLeftClickListener { finish() }

        // 条目点击事件
        adapter.addChildClickViewIds(R.id.cl_root)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val itemData = adapter.data[position] as? AccessoryListBean
            mViewModel.setAccessoryId("${itemData?.accessoryId}")
            when (view.id) {
                R.id.cl_root -> {
                    // 判断当前是否是camera，并且是没有添加过的
                    if (accessoryList?.none { it.accessoryId == itemData?.accessoryId } == true && itemData?.accessoryName == "Smart Camera") {
                        if ((accessoryList?.size ?: 0) > 0) {
                            XPopup.Builder(this@AddAccessoryActivity)
                                .isDestroyOnDismiss(false)
                                .dismissOnTouchOutside(false)
                                .asCustom(
                                    BaseCenterPop(
                                        this@AddAccessoryActivity,
                                        content = "Hey abby only contains one USB port to support a single smart accessory. If you add a new accessory, the current one will be deleted. Are you sure you want to proceed?",
                                        cancelText = "No",
                                        confirmText = "Yes",
                                        onConfirmAction = {
                                            // 跳转到摄像头配对页面
                                            ARouter.getInstance().build(RouterPath.PairConnect.PAGE_WIFI_CONNECT)
                                                .withBoolean(Constants.Global.KEY_WIFI_PAIRING_PARAMS, true)
                                                .navigation(this@AddAccessoryActivity, Constants.Global.KEY_WIFI_PAIRING_BACK)
                                        }
                                    )
                                ).show()
                            return@setOnItemChildClickListener
                        } else {
                            // 跳转到摄像头配对页面
                            ARouter.getInstance().build(RouterPath.PairConnect.PAGE_WIFI_CONNECT)
                                .withBoolean(Constants.Global.KEY_WIFI_PAIRING_PARAMS, true)
                                .navigation(this@AddAccessoryActivity, Constants.Global.KEY_WIFI_PAIRING_BACK)
                        }
                        return@setOnItemChildClickListener
                    }

                    // 判断当前有几个智能设备
                    if (accessoryList?.size == 0) {
                        // 跳转到富文本界面
                        val intent = Intent(this, KnowMoreActivity::class.java)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, itemData?.textId)
                        intent.putExtra(
                            BasePopActivity.KEY_FIXED_TASK_ID,
                            Constants.Fixed.KEY_FIXED_ID_NEW_ACCESSORIES
                        )
                        intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, true)
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON, true)
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE, "Slide to Unlock")
                        intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceId)
                        intent.putExtra(BasePopActivity.KEY_PART_ID, "${itemData?.accessoryId}")
                        startActivity(intent)
                        return@setOnItemChildClickListener
                    }

                    // 如果点击的是当前已经添加过的。
                    accessoryList?.firstOrNull { it.accessoryId == itemData?.accessoryId }?.apply {
                        // 跳转到摄像头界面
                        if (accessoryList?.none { it.accessoryId == itemData?.accessoryId } == false && itemData?.accessoryName == "Smart Camera") {
                            startActivity(Intent(this@AddAccessoryActivity, CameraSettingActivity::class.java).apply {
                                putExtra("accessoryDeviceId", accessoryDeviceId)
                                putExtra("deviceId", deviceId)
                            })
                            return@setOnItemChildClickListener
                        }
                        // 跳转到智能设备信息界面
                        val intent = Intent(this@AddAccessoryActivity, DeviceAutomationActivity::class.java)
                        intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceId)
                        intent.putExtra(BasePopActivity.KEY_PART_ID, "$accessoryId")
                        startActivity(intent)
                        return@setOnItemChildClickListener
                    }

                    // 前面2个都不满足，表明添加的是新设备
                    if ((accessoryList?.size ?: 0) > 0) {

                        XPopup.Builder(this@AddAccessoryActivity)
                            .isDestroyOnDismiss(false)
                            .dismissOnTouchOutside(false)
                            .asCustom(
                                BaseCenterPop(
                                    this@AddAccessoryActivity,
                                    content = "Hey abby only contains one USB port to support a single smart accessory. If you add a new accessory, the current one will be deleted. Are you sure you want to proceed?",
                                    cancelText = "No",
                                    confirmText = "Yes",
                                    onConfirmAction = {
                                        val intent = Intent(this, KnowMoreActivity::class.java)
                                        intent.putExtra(
                                            Constants.Global.KEY_TXT_ID,
                                            itemData?.textId
                                        )
                                        intent.putExtra(
                                            BasePopActivity.KEY_FIXED_TASK_ID,
                                            Constants.Fixed.KEY_FIXED_ID_NEW_ACCESSORIES
                                        )
                                        intent.putExtra(
                                            BasePopActivity.KEY_INTENT_UNLOCK_TASK,
                                            true
                                        )
                                        intent.putExtra(
                                            BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON,
                                            true
                                        )
                                        intent.putExtra(
                                            BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE,
                                            "Slide to Unlock"
                                        )
                                        intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceId)
                                        intent.putExtra(
                                            BasePopActivity.KEY_PART_ID,
                                            "${itemData?.accessoryId}"
                                        )
                                        startActivity(intent)
                                    }
                                )
                            ).show()
                        return@setOnItemChildClickListener
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.Global.KEY_WIFI_PAIRING_BACK -> {
                    val url = data?.getStringExtra("qrcodeUrl")
                    val wifiName = data?.getStringExtra("wifiName")
                    val wifiPwd = data?.getStringExtra("wifiPsd")
                    val token = data?.getStringExtra("token")


                    // 说明绑定成功，跳转到二维码生成界面
                    startActivity(Intent(this@AddAccessoryActivity, PairTheCameraActivity::class.java).apply {
                        putExtra("qrcodeUrl", url)
                        putExtra("deviceId", deviceId)
                        putExtra("wifiName", wifiName)
                        putExtra("wifiPsd", wifiPwd)
                        putExtra("token", token)
                        putExtra("accessoryId", mViewModel.accessoryId.value)
                        putExtra("deviceId", deviceId)
                    })
                }
            }
        }
    }
}