package com.cl.modules_my.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.base.KnowMoreActivity
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.AddTenAccessoryAdapter
import com.cl.modules_my.databinding.MyAddAccessoryBinding
import com.cl.common_base.bean.AccessoryListBean
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.ViewUtils
import com.cl.modules_my.viewmodel.AddAccessoryViewModel
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import java.text.Bidi
import javax.inject.Inject

/**
 * 添加附件页面
 */
@Route(path = RouterPath.My.PAGE_ADD_ACCESSORY)
@AndroidEntryPoint
class AddAccessoryActivity : BaseActivity<MyAddAccessoryBinding>() {
    @Inject
    lateinit var mViewModel: AddAccessoryViewModel

    /*private val adapter by lazy {
        AddAccessoryAdapter(mutableListOf())
    }*/

    // 帐篷设备页面
    private val tenAdapter by lazy {
        AddTenAccessoryAdapter(mutableListOf())
    }

    private val tenNoShareAdapter by lazy {
        AddTenAccessoryAdapter(mutableListOf())
    }

    // 设备ID
    private val deviceId by lazy {
        intent.getStringExtra("deviceId")
    }

    // 智能设备配件数量
    private val accessoryList by lazy {
        intent.getSerializableExtra("accessoryList") as? MutableList<ListDeviceBean.AccessoryList>
            ?: mutableListOf()
    }

    // 设备列表
    private val deviceList by lazy {
        intent.getSerializableExtra("deviceList") as? MutableList<ListDeviceBean>
            ?: mutableListOf()
    }

    // 当前设备类型
    private val spaceType by lazy {
        intent.getStringExtra("spaceType")
    }

    override fun initView() {
        mViewModel.getAccessoryList(if (spaceType != ListDeviceBean.KEY_SPACE_TYPE_BOX) "tent" else "box", deviceId.toString())

        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = tenAdapter

        binding.rvListDevice.layoutManager = LinearLayoutManager(this)
        binding.rvListDevice.adapter = tenNoShareAdapter
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

                    data?.filter { it.isShared == true }?.apply {
                        ViewUtils.setVisible(this.isNotEmpty(), binding.tvTitles, binding.tvDesc)
                        tenAdapter.setList(this)
                    }
                    data?.filter { it.isShared == false }?.apply {
                        ViewUtils.setVisible(this.isNotEmpty(), binding.tvTitleDedicated, binding.tvDescDedicated)
                        tenNoShareAdapter.setList(this)
                    }
                    // tenAdapter.setList(data)
                }
            })
        }
    }

    override fun initData() {
        binding.ftbTitle.setLeftClickListener { finish() }

        // 条目点击事件
        /*adapter.addChildClickViewIds(R.id.cl_root)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val itemData = adapter.data[position] as? AccessoryListBean
            mViewModel.setAccessoryId("${itemData?.accessoryId}")
            when (view.id) {
                R.id.cl_root -> {
                    addAccess(itemData)
                }
            }
        }*/


        //  帐篷条目点击
        tenAdapter.addChildClickViewIds(R.id.tv_add, R.id.tv_buy)
        tenAdapter.setOnItemChildClickListener(itemChildListener)

        tenNoShareAdapter.addChildClickViewIds(R.id.tv_add, R.id.tv_buy)
        tenNoShareAdapter.setOnItemChildClickListener(itemChildListener)
    }

    private val itemChildListener = OnItemChildClickListener { adapter, view, position ->
        val itemData = adapter.data[position] as? AccessoryListBean
        when (view.id) {
            R.id.tv_add -> {
                // 不能添加
                if (itemData?.canAdd == false) {
                    if (itemData.isShared == true) {
                        // 不能添加
                        ToastUtil.shortShow(itemData.cannotMsg)
                        return@OnItemChildClickListener
                    }
                    xpopup(this@AddAccessoryActivity) {
                        isDestroyOnDismiss(false)
                        dismissOnTouchOutside(true)
                        asCustom(
                            BaseCenterPop(this@AddAccessoryActivity, content = itemData.cannotMsg, isShowCancelButton = true, cancelText = "No", confirmText = "Yes", onConfirmAction = {
                                // 这个后台逻辑是覆盖最早一个添加的配件。
                                addAccess(itemData)
                            })
                        ).show()
                    }
                    return@OnItemChildClickListener
                }
                // 直接添加
                addAccess(itemData)
            }

            R.id.tv_buy -> {
                // 跳转到网页
                val intent = Intent(this@AddAccessoryActivity, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_WEB_URL, itemData?.buyLink)
                startActivity(intent)
            }
        }
    }

    private fun addAccess(itemData: AccessoryListBean?) {
        when (itemData?.accessoryType) {
            // 蓝牙相关的配件
            AccessoryListBean.KEY_PHB -> {
                startActivity(Intent(this@AddAccessoryActivity, PhPairActivity::class.java).apply {
                    putExtra("accessoryId", "${itemData.accessoryId}")
                    putExtra("deviceId", deviceId)
                    putExtra(Constants.Ble.KEY_BLE_TYPE, Constants.Ble.TYPE_PH)
                })
                return
            }
            // 温湿度传感器
            AccessoryListBean.KEY_MONITOR_VIEW_OUT, AccessoryListBean.KEY_MONITOR_OUT, AccessoryListBean.KEY_MONITOR_VIEW_IN, AccessoryListBean.KEY_MONITOR_IN -> {
                // 跳转到添加tent内部温湿度传感器界面
                ARouter.getInstance().build(RouterPath.PairConnect.PAGE_WIFI_DEVICE_SCAN)
                    .withString(Constants.Pair.KEY_PAIR_WIFI_DEVICE, itemData.accessoryType)
                    .withString("deviceId", deviceId)
                    .withString("accessoryId", "${itemData.accessoryId}")
                    .navigation()
                return
            }
            // 排插
            // 这个是不共享的，但是不占用usb，目前重复添加时也只需要判断是否已经拥有，跳转各自的详情就好了。
            AccessoryListBean.KEY_OUTLETS -> {
                // 跳转到排插界面去
                // 没有找到就跳转到配对界面去
                // 跳转到收入wifi密码界面
                ARouter.getInstance()
                    .build(RouterPath.PairConnect.PAGE_WIFI_CONNECT)
                    .withString(
                        Constants.Global.KEY_WIFI_PAIRING_PARAMS,
                        Constants.Global.KEY_GLOBAL_PAIR_DEVICE_OUTLETS
                    )
                    .withString("deviceId", deviceId)
                    .withString(
                        "accessoryId",
                        "${itemData.accessoryId}"
                    )
                    .navigation(this@AddAccessoryActivity)
                return
            }
            // 摄像头
            AccessoryListBean.KEY_CAMERA -> {
                // 跳转到摄像头配对页面
                ARouter.getInstance().build(RouterPath.PairConnect.PAGE_WIFI_CONNECT).withString(
                    Constants.Global.KEY_WIFI_PAIRING_PARAMS, Constants.Global.KEY_GLOBAL_PAIR_DEVICE_CAMERA
                ).withString("deviceId", deviceId).withString(
                    "accessoryId", "${itemData.accessoryId}"
                ).navigation(
                    this@AddAccessoryActivity, Constants.Global.KEY_WIFI_PAIRING_BACK
                )
                return
            }

            else -> {
                // 添加其他USB配件
                // 判断是否包含了camera\因为camera需要自己手动解绑
                // 跳转到富文本界面
                val intent = Intent(this, KnowMoreActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, itemData?.textId)
                intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_NEW_ACCESSORIES)
                intent.putExtra(BasePopActivity.KEY_INTENT_UNLOCK_TASK, true)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON, true)
                intent.putExtra(BasePopActivity.KEY_IS_SHOW_UNLOCK_BUTTON_ENGAGE, "Slide to Unlock")
                intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceId)
                intent.putExtra(BasePopActivity.KEY_PART_ID, "${itemData?.accessoryId}")
                accessoryList.firstOrNull { it.accessoryType == AccessoryListBean.KEY_CAMERA }
                    ?.apply {
                        // 添加Camera的Id、 用于手动解绑。
                        intent.putExtra(
                            BasePopActivity.KEY_CAMERA_ID,
                            accessoryDeviceId
                        )
                    }
                startActivity(intent)
            }
        }
    }

    /**
     * 回调刷新页面
     */
    private val startActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                // 保存成功
                ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                    .navigation()
                finish()
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
                    startActivity(
                        Intent(
                            this@AddAccessoryActivity,
                            PairTheCameraActivity::class.java
                        ).apply {
                            putExtra("qrcodeUrl", url)
                            putExtra("deviceId", deviceId)
                            putExtra("wifiName", wifiName)
                            putExtra("wifiPsd", wifiPwd)
                            putExtra("token", token)
                            putExtra("accessoryId", mViewModel.accessoryId.value)
                        })
                }
            }
        }
    }
}