package com.cl.modules_my.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bhm.ble.BleManager
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.base.KnowMoreActivity
import com.cl.common_base.bean.UpPlantInfoReq
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.DeviceListAdapter
import com.cl.modules_my.databinding.MyDeviceListActivityBinding
import com.cl.modules_my.pop.EditPlantProfilePop
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.bean.LiveDataDeviceInfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.util.livedatabus.LiveEventBus
import com.cl.modules_my.pop.MyChooerTipPop
import com.cl.common_base.bean.AccessoryListBean
import com.cl.common_base.help.PlantCheckHelp
import com.cl.common_base.listener.TuYaDeviceUpdateReceiver
import com.cl.common_base.pop.activity.BasePopActivity.Companion.KEY_USB_PORT
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_my.pop.DeviceShortPop
import com.cl.modules_my.viewmodel.ListDeviceViewModel
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.thingclips.bouncycastle.asn1.x509.KeyUsage
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable
import java.util.ArrayList
import javax.inject.Inject


/**
 * 设备列表界面
 */
@Route(path = RouterPath.My.PAGE_MY_DEVICE_LIST)
@AndroidEntryPoint
class DeviceListActivity : BaseActivity<MyDeviceListActivityBinding>() {
    // 当前设备的ID
    private var isCurrentDeviceIndex = 0
    private val adapter by lazy {
        DeviceListAdapter(mutableListOf(), switchListener = { accessoryId, deviceId, isChooser, usbPort ->
            // 选择设备开关
            mViewModel.setDeviceStatus(accessoryId, deviceId, if (isChooser) "1" else "0", usbPort)
        }, luoSiListener = { accessoryData, accessListBean ->
            // 这是配件点击设置逻辑
            // camera跳转到专属页面
            if (accessoryData.accessoryType == AccessoryListBean.KEY_CAMERA) {
                val accessoryDeviceId = accessoryData.accessoryDeviceId
                startActivityLauncher.launch(
                    Intent(
                        this@DeviceListActivity,
                        CameraSettingActivity::class.java
                    ).apply {
                        // 配件Id 就是cameraId
                        putExtra("accessoryDeviceId", accessoryDeviceId)
                        putExtra("deviceId", accessListBean.deviceId)
                        putExtra("relationId", accessoryData.relationId)
                    })
                return@DeviceListAdapter
            }
            // 排插
            if (accessoryData.accessoryType == AccessoryListBean.KEY_OUTLETS) {
                startActivity(Intent(this@DeviceListActivity, OutletsSettingActivity::class.java).apply {
                    putExtra("accessoryId", accessoryData.accessoryId)
                    putExtra("accessoryDeviceId", accessoryData.accessoryDeviceId)
                    putExtra("deviceId", accessListBean.deviceId)
                    putExtra("relationId", accessoryData.relationId)
                })
                return@DeviceListAdapter
            }
            // 补光灯
            if (accessoryData.accessoryType == AccessoryListBean.KEY_FILL_LIGHT) {
                // 富文本id  = fill_light_view
                // 跳转到KnowMoreActivity
                val id = accessoryData.textId
                val intent = Intent(this@DeviceListActivity, KnowMoreActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_FILL_LIGHT_VIEW)
                intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_FILL_LIGHT_VIEW)
                intent.putExtra(BasePopActivity.KEY_DEVICE_ID, accessListBean.deviceId)
                intent.putExtra(BasePopActivity.KEY_SHARE_TYPE, accessoryData.accessoryType)
                intent.putExtra(BasePopActivity.KEY_RELATION_ID, accessoryData.relationId)
                startActivityLauncher.launch(intent)
                return@DeviceListAdapter
            }

            // 内部的温湿度传感器，只有帐篷才会有。Abby内部内置了温湿度传感器
            if (accessoryData.accessoryType == AccessoryListBean.KEY_MONITOR_IN || accessListBean.spaceType == AccessoryListBean.KEY_MONITOR_VIEW_IN) {
                // 跳转到KnowMoreActivity
                val id = accessoryData.textId
                val intent = Intent(this@DeviceListActivity, KnowMoreActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, id)
                intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, id)
                intent.putExtra(BasePopActivity.KEY_DEVICE_ID, accessListBean.deviceId)
                intent.putExtra(BasePopActivity.KEY_SHARE_TYPE, accessoryData.accessoryType)
                intent.putExtra(BasePopActivity.KEY_RELATION_ID, accessoryData.relationId)
                startActivityLauncher.launch(intent)
                return@DeviceListAdapter
            }
            // 配件属性加一个。needAutoSet
            // 为true的话，以后没配置过的未知设备就直接进自动化配置
            // false的话，就进富文本的配置页面
            if (accessoryData.needAutoSet == true) {
                val intent = Intent(this@DeviceListActivity, DeviceAutomationActivity::class.java)
                intent.putExtra("relationId", accessoryData.relationId)
                intent.putExtra(BasePopActivity.KEY_DEVICE_ID, accessListBean.deviceId)
                intent.putExtra(KEY_USB_PORT, accessoryData.usbPort)
                intent.putExtra(
                    BasePopActivity.KEY_PART_ID,
                    "${accessoryData.accessoryId}"
                )
                startActivityLauncher.launch(intent)
            } else {
                // 跳转富文本
                val id = accessoryData.textId
                val intent = Intent(this@DeviceListActivity, KnowMoreActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, id)
                intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, id)
                intent.putExtra(BasePopActivity.KEY_DEVICE_ID, accessListBean.deviceId)
                intent.putExtra(BasePopActivity.KEY_SHARE_TYPE, accessoryData.accessoryType)
                intent.putExtra(BasePopActivity.KEY_RELATION_ID, accessoryData.relationId)
                startActivityLauncher.launch(intent)
            }


        })
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
        // 获取适配器的数据列表
        val list = adapter.data

        // 如果列表为空，则结束当前操作
        if (list.isEmpty()) {
            finish()
            return
        }

        // 使用 indexOfFirst 查找符合条件的元素，返回值如果是 -1，表示未找到
        val index = list.indexOfFirst { it.isChooser == true }

        // 如果没有找到符合条件的元素，则不进行任何操作
        if (index == -1) {
            return
        }

        // 如果找到的设备索引与当前设备索引相同，结束操作
        if (index == isCurrentDeviceIndex) {
            finish()
        } else {
            // 查找符合条件的设备对象，避免空指针异常
            val device = list.getOrNull(index)

            // 如果找到了设备，进行设备切换逻辑
            device?.apply {
                // 确保设备ID不为空才进行切换
                deviceId?.let {
                    // 调用ViewModel进行设备切换
                    mViewModel.switchDevice(it)
                }
                // 删除原先的、或者切换了设备
                // 跳转到主页、加载。
                // 切换了主页，应该直接回到首页、在合并界面也能跳转到这个地方。应该需要使用其他的方法。
                // 改用Eventbus吧。
                // 切换了设备，需要重新刷新主页。
                /*logI("123123123: $deviceId,,$spaceType")
                ARouter.getInstance()
                    .build(RouterPath.Main.PAGE_MAIN).navigation()
                LiveEventBus.get()
                    .with(Constants.Global.KEY_IS_SWITCH_DEVICE, LiveDataDeviceInfoBean::class.java)
                    .postEvent(LiveDataDeviceInfoBean(deviceId, spaceType, onlineStatus))*/
            }
        }
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
                            content = getString(com.cl.common_base.R.string.string_1788),
                            isShowCancelButton = false
                        )
                    ).show()
            }
        }
    }

    override fun observe() {
        mViewModel.switchDevice.observe(this@DeviceListActivity, resourceObserver {
            loading { showProgressLoading() }
            error { errorMsg, code ->
                ToastUtil.shortShow(errorMsg)
                hideProgressLoading()
            }
            success {
                adapter.data.firstOrNull { data -> data.isChooser == true }?.apply {
                    if (spaceType != ListDeviceBean.KEY_SPACE_TYPE_BOX) {
                        //  切换设备之后、可以直接调用刷新userDtail接口，走到showView方法中、通过plantInfo和listDevice来显示和隐藏当前abby的信息。
                        // mViewMode.userDetail()
                        // 删除未读消息
                        // mViewMode.removeFirstUnreadMessage()
                        // 清空气泡状态
                        // mViewMode.clearPopPeriodStatus()
                        mViewModel.checkPlant()
                        return@success
                    }
                }
                // 更新涂鸦Bean
                ThingHomeSdk.newHomeInstance(mViewModel.homeId)
                    .getHomeDetail(object : IThingHomeResultCallback {
                        override fun onSuccess(bean: HomeBean?) {
                            bean?.let { it ->
                                val arrayList = it.deviceList as ArrayList<DeviceBean>
                                logI("123123123: ${arrayList.size}")
                                adapter.data.firstOrNull { data -> data.isChooser == true }?.apply {
                                    deviceId?.let { id ->
                                        arrayList.firstOrNull { dev -> dev.devId == id }.apply {
                                            logI("thingDeviceBean ID: $id")
                                            // 在线的、数据为空、并且是abby机器
                                            if (null == this && spaceType == ListDeviceBean.KEY_SPACE_TYPE_BOX && onlineStatus != "Offline") {
                                                ToastUtil.shortShow(getString(com.cl.common_base.R.string.string_631))
                                            }
                                            GSON.toJsonInBackground(this) {
                                                Prefs.putStringAsync(
                                                    Constants.Tuya.KEY_DEVICE_DATA, it
                                                )
                                            }
                                            // 重新注册服务
                                            // 开启服务
                                            val intent = Intent(
                                                this@DeviceListActivity, TuYaDeviceUpdateReceiver::class.java
                                            )
                                            startService(intent)
                                            // 切换之后需要重新刷新所有的东西
                                            mViewModel.checkPlant()
                                        }
                                    }
                                }
                            }
                        }

                        override fun onError(errorCode: String?, errorMsg: String?) {

                        }
                    })
            }
        })

        mViewModel.checkPlant.observe(this@DeviceListActivity, resourceObserver {
            error { errorMsg, code ->
                ToastUtil.shortShow(errorMsg)
                hideProgressLoading()
            }
            success {
                hideProgressLoading()
                data?.let {
                    PlantCheckHelp().plantStatusCheck(
                        this@DeviceListActivity,
                        it,
                        true,
                        isLeftSwapAnim = true,
                        isNoAnim = false
                    )
                }
            }
        })

        // 更改设备附件开光
        mViewModel.deviceStatus.observe(this@DeviceListActivity, resourceObserver {
            loading { showProgressLoading() }
            error { errorMsg, code ->
                hideProgressLoading()
                ToastUtil.shortShow(errorMsg)
            }
            success {
                hideProgressLoading()
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
                // 检查data是否非空和非空列表
                val dataInfo = data
                if (null == dataInfo) {
                    adapter.setList(dataInfo)
                    return@success
                }
                // 初次进来默认选择的排序方式
                val shortNumber = Prefs.getInt(Constants.Global.KEY_SORT_PERIOD, 1)
                var shortName = getString(com.cl.common_base.R.string.my_sort_name)

                //  根据isShared 来分为2组，并且在每组的第一个添加新类型类型
                // 1. 先找到isShared为true的设备
                val sharedList = dataInfo.filter { it.isShared == true }.toMutableList()
                // 2. 找到isShared为false的设备
                val unSharedList = dataInfo.filter { it.isShared == false }.toMutableList()
                // 3. 判断是否有isChooser为true的设备，如果有就新增一个元素spaceType = KEY_SPACE_TYPE_TEXT
                if (sharedList.isNotEmpty()) {
                    sharedList.add(0, ListDeviceBean(spaceType = ListDeviceBean.KEY_SPACE_TYPE_TEXT, textDesc = getString(com.cl.common_base.R.string.string_1789)))
                }

                // 排序
                // 提取通用的设备名称排序比较器
                val deviceNameComparator = compareBy<ListDeviceBean> { device ->
                    // 检查是否包含特殊字符
                    if (device.deviceName?.matches(Regex("^[a-zA-Z0-9\\s]+$")) == true) 0 else 1
                }.thenBy { device ->
                    device.deviceName?.replace("\\s".toRegex(), "")?.lowercase() ?: ""
                }.thenBy { device ->
                    device.deviceName?.replace("\\D".toRegex(), "")?.toIntOrNull() ?: 0
                }

                // 根据不同的排序类型，对unSharedList进行排序
                val comparator = when(shortNumber) {
                    1 -> {
                        shortName = getString(com.cl.common_base.R.string.my_sort_name)
                        deviceNameComparator
                    }
                    2 -> {
                        shortName = getString(com.cl.common_base.R.string.my_sort_strain)
                        compareBy<ListDeviceBean> {
                            it.strainName.isNullOrEmpty()
                        }.thenBy {
                            it.strainName?.lowercase() ?: ""
                        }
                    }
                    3 -> {
                        shortName = getString(com.cl.common_base.R.string.my_sort_status)
                        compareBy<ListDeviceBean> {
                            // 在线状态排序（在线优先）
                            if (it.onlineStatus == "Offline") 1 else 0
                        }.then(deviceNameComparator)
                    }
                    4 -> {
                        shortName = getString(com.cl.common_base.R.string.my_sort_subscription)
                        compareBy<ListDeviceBean> { device ->
                            // 第一优先级：是否有订阅服务
                            val subscription = device.subscription
                            when {
                                // 没有subscription字段，排最后
                                subscription.isNullOrEmpty() -> 2
                                // 有subscription但不包含数字(已过期)，排中间
                                !subscription.any { it.isDigit() } -> 1
                                // 有subscription且包含数字(未过期)，排最前
                                else -> 0
                            }
                        }.then(deviceNameComparator) // 第二优先级：设备名称排序
                    }
                    else -> deviceNameComparator
                }

                // 应用排序
                unSharedList.sortWith(comparator)
                // 4. 判断是否有isChooser为false的设备，如果有就新增一个元素spaceType = KEY_SPACE_TYPE_TEXT
                if (unSharedList.isNotEmpty()) {
                    unSharedList.add(0, ListDeviceBean(spaceType = ListDeviceBean.KEY_SPACE_TYPE_TEXT, textDesc = getString(com.cl.common_base.R.string.string_1790), shortText = shortName))
                }

                // 5. 将isShared为true的设备放在前面
                val newList = sharedList + unSharedList

                if (newList.isNotEmpty()) {
                    val indexChooser = newList.indexOfFirst { it.currentDevice == 1 }

                    // 检查是否找到匹配项
                    if (indexChooser != -1) {
                        newList[indexChooser].isChooser = true  // 设置isChooser标志
                        adapter.setList(newList)

                        // 设置RecyclerView的位置
                        binding.rvList.postDelayed({
                            binding.rvList.smoothScrollToPosition(indexChooser)
                        }, 200)

                    } else {
                        // 处理没有找到匹配项的情况，例如选择默认项
                        newList[0].isChooser = true
                        adapter.setList(newList)

                        // 设置RecyclerView的位置
                        binding.rvList.postDelayed({
                            binding.rvList.smoothScrollToPosition(0)
                        }, 200)
                    }

                    // 找到当前current的设备，然后记录下，用户返回的时候判断是否是切换了设备。
                    isCurrentDeviceIndex = adapter.data.indexOfFirst { it.isChooser == true }
                }
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
                                        content = getString(com.cl.common_base.R.string.string_1791),
                                        confirmText = getString(com.cl.common_base.R.string.my_confirm),
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
            R.id.iv_pair_luosi,
            R.id.cl_sort
        )
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val deviceBean = (adapter.data[position] as? ListDeviceBean)
            val type = adapter.getItemViewType(position)
            logI("123131231: ${deviceBean?.deviceId},,,,${deviceBean?.nightTimer}")
            when (view.id) {
                R.id.cl_sort -> {
                    // 对机器进行排序
                    XPopup.Builder(this@DeviceListActivity).popupPosition(PopupPosition.Bottom).dismissOnTouchOutside(true).isClickThrough(false)  //点击透传
                        .hasShadowBg(true) // 去掉半透明背景
                        //.offsetX(XPopupUtils.dpada2px(this@MainActivity, 10f))
                        .atView(view).isCenterHorizontal(false).asCustom(
                            DeviceShortPop(this@DeviceListActivity, onConfirmAction = { period ->
                                // 选中的时候 对适配器进行排序。
                                val ada = adapter.data as? MutableList<ListDeviceBean>
                                val unSharedList = ada?.filter { it.isShared == false }?.toMutableList() ?: mutableListOf()
                                val sharedList = ada?.filter { it.isShared == true }?.toMutableList() ?: mutableListOf()

                                // 提取通用的设备名称排序比较器
                                val deviceNameComparator = compareBy<ListDeviceBean> { device ->
                                    // 检查是否包含特殊字符
                                    if (device.deviceName?.matches(Regex("^[a-zA-Z0-9\\s]+$")) == true) 0 else 1
                                }.thenBy { device ->
                                    device.deviceName?.replace("\\s".toRegex(), "")?.lowercase() ?: ""
                                }.thenBy { device ->
                                    device.deviceName?.replace("\\D".toRegex(), "")?.toIntOrNull() ?: 0
                                }

                                // 根据不同的排序类型，对unSharedList进行排序
                                val comparator = when(period) {
                                    getString(com.cl.common_base.R.string.my_sort_name) -> {
                                        Prefs.putIntAsync(Constants.Global.KEY_SORT_PERIOD, 1)
                                        deviceNameComparator
                                    }
                                    getString(com.cl.common_base.R.string.my_sort_strain) -> {
                                        Prefs.putIntAsync(Constants.Global.KEY_SORT_PERIOD, 2)
                                        compareBy<ListDeviceBean> {
                                            it.strainName.isNullOrEmpty()
                                        }.thenBy {
                                            it.strainName?.lowercase() ?: ""
                                        }
                                    }
                                    getString(com.cl.common_base.R.string.my_sort_status) -> {
                                        Prefs.putIntAsync(Constants.Global.KEY_SORT_PERIOD, 3)
                                        compareBy<ListDeviceBean> {
                                            // 在线状态排序（在线优先）
                                            if (it.isOnline == false) 1 else 0
                                        }.then(deviceNameComparator)
                                    }
                                    getString(com.cl.common_base.R.string.my_sort_subscription) -> {
                                        Prefs.putIntAsync(Constants.Global.KEY_SORT_PERIOD, 4)
                                        compareBy<ListDeviceBean> { device ->
                                            // 第一优先级：是否有订阅服务
                                            val subscription = device.isSubscript
                                            when (// 没有subscription字段，排最后
                                                subscription) {
                                                false -> 1

                                                // 有subscription但不包含数字(已过期)，排中间
                                                true -> 0

                                                // 有subscription且包含数字(未过期)，排最前
                                                else -> 2
                                            }
                                        }.then(deviceNameComparator) // 第二优先级：设备名称排序
                                    }
                                    else -> deviceNameComparator
                                }

                                // 应用排序
                                unSharedList.sortWith(comparator)
                                if (sharedList.isNotEmpty()) {
                                    sharedList.add(0, ListDeviceBean(spaceType = ListDeviceBean.KEY_SPACE_TYPE_TEXT, textDesc = getString(com.cl.common_base.R.string.string_1789)))
                                }
                                if (unSharedList.isNotEmpty()) {
                                    unSharedList.add(0, ListDeviceBean(spaceType = ListDeviceBean.KEY_SPACE_TYPE_TEXT, textDesc = getString(com.cl.common_base.R.string.string_1790), shortText = period))
                                }
                                this@DeviceListActivity.adapter.setList(sharedList + unSharedList)
                            }).setBubbleBgColor(Color.WHITE) //气泡背景
                                .setArrowWidth(XPopupUtils.dp2px(this@DeviceListActivity, 6f)).setArrowHeight(
                                    XPopupUtils.dp2px(
                                        this@DeviceListActivity, 6f
                                    )
                                ) //.setBubbleRadius(100)
                                .setArrowRadius(
                                    XPopupUtils.dp2px(
                                        this@DeviceListActivity, 3f
                                    )
                                )
                        ).show()
                }
                R.id.iv_pair_luosi -> {
                    when (type) {
                        // PH笔
                        ListDeviceBean.KEY_TYPE_PH -> {
                            startActivityLauncher.launch(Intent(
                                this@DeviceListActivity,
                                PHSettingActivity::class.java
                            ).apply {
                                putExtra("deviceId", deviceBean?.deviceId)
                            })
                        }
                        // 温湿度传感器
                        ListDeviceBean.KEY_MONITOR_OUT, ListDeviceBean.MONITOR_VIEW_OUT -> {
                            // 跳转到KnowMoreActivity
                            val id = deviceBean?.textId
                            val intent = Intent(this@DeviceListActivity, KnowMoreActivity::class.java)
                            intent.putExtra(Constants.Global.KEY_TXT_ID, id)
                            intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, id)
                            intent.putExtra(BasePopActivity.KEY_DEVICE_ID, deviceBean?.deviceId)
                            intent.putExtra(BasePopActivity.KEY_SHARE_TYPE, type.toString())
                            startActivityLauncher.launch(intent)
                        }
                    }

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
                        .moveUpToKeyboard(false)
                        .asCustom(EditPlantProfilePop(this@DeviceListActivity,
                            beanData = deviceBean,
                            plantName = deviceBean.plantName,
                            strainName = deviceBean.strainName,
                            onConfirmAction = { plantName, strainName ->
                                // 修改属性名
                                if (strainName.isNullOrEmpty() && plantName?.isNotEmpty() == true) {
                                    mViewModel.updatePlantInfo(
                                        UpPlantInfoReq(
                                            plantName = plantName,
                                            plantId = deviceBean.plantId
                                        )
                                    )
                                }
                                if (plantName.isNullOrEmpty() && strainName?.isNotEmpty() == true) {
                                    mViewModel.updatePlantInfo(
                                        UpPlantInfoReq(
                                            strainName = strainName,
                                            plantId = deviceBean.plantId
                                        )
                                    )
                                } else {
                                    mViewModel.updatePlantInfo(
                                        UpPlantInfoReq(
                                            strainName = strainName,
                                            plantName = plantName,
                                            plantId = deviceBean.plantId
                                        )
                                    )
                                }
                                // 更新小组件
                                updateWidget(this@DeviceListActivity)
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
                    intent.putExtra("deviceList", adapter.data as? Serializable)
                    intent.putExtra("deviceId", deviceBean?.deviceId)
                    intent.putExtra("accessoryList", deviceBean?.accessoryList as? Serializable)
                    intent.putExtra("spaceType", deviceBean?.spaceType)
                    intent.putExtra("deviceType", deviceBean?.deviceType)
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
                    // 跳转到首页
                    // (data.period.equals("No plant") &amp;&amp; data.isChooser &amp;&amp; !data.onlineStatus.equals("Offline")) ? View.VISIBLE : View.GONE
                    isSwitchDevice()
                    /*this.adapter.data.firstOrNull { it.isChooser == true }?.apply {
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
                        logI("123123123: $deviceId,,$spaceType")
                        ARouter.getInstance()
                            .build(RouterPath.Main.PAGE_MAIN).navigation()
                        LiveEventBus.get()
                            .with(Constants.Global.KEY_IS_SWITCH_DEVICE, LiveDataDeviceInfoBean::class.java)
                            .postEvent(LiveDataDeviceInfoBean(deviceId, spaceType, onlineStatus))
                        finish()
                    }*/
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