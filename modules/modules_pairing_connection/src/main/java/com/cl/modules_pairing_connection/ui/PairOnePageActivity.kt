package com.cl.modules_pairing_connection.ui

import android.content.Intent
import android.provider.Settings
import android.text.method.LinkMovementMethod
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ble.BleUtil
import com.cl.common_base.util.lcoation.LocationUtil
import com.cl.common_base.util.permission.PermissionChecker
import com.cl.common_base.util.span.appendClickable
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_pairing_connection.PairScanListAdapter
import com.cl.modules_pairing_connection.R
import com.cl.modules_pairing_connection.databinding.PairScanBleBinding
import com.cl.modules_pairing_connection.request.PairBleData
import com.cl.modules_pairing_connection.viewmodel.PairDistributionWifiViewModel
import com.cl.modules_pairing_connection.widget.GuideBlePop
import com.cl.modules_pairing_connection.widget.PairLocationPop
import com.lxj.xpopup.XPopup
import com.permissionx.guolindev.PermissionX
import com.tuya.smart.android.ble.api.BleConfigType
import com.tuya.smart.android.ble.api.ScanType
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.bean.ConfigProductInfoBean
import com.tuya.smart.sdk.api.ITuyaDataCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject


/**
 *  配对的第一个界面、扫描
 *  当能扫描到设备了，那么就是第二个配对界面了
 * @author 李志军 2022-08-03 14:43
 */
@AndroidEntryPoint
class PairOnePageActivity : BaseActivity<PairScanBleBinding>() {

    // 列表适配器
    private val adapter by lazy {
        PairScanListAdapter(mutableListOf())
    }

    // HomeID
    private val tuYaHomeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }

    @Inject
    lateinit var mViewModel: PairDistributionWifiViewModel

    /**
     * 引导开启蓝牙弹窗
     */
    private val guideBlePop by lazy {
        XPopup.Builder(this)
            .isDestroyOnDismiss(false)
            .asCustom(GuideBlePop(this))
    }

    var job: Job? = null

    override fun initView() {
        // 标题设置
        binding.title.setLeftText("Cancel")
            .setTitle("1/3")
            .setLeftClickListener { finish() }

        // 设置富文本
        binding.tvTwo.text = buildSpannedString {
            append("Can't find your abby currently？")
            color(ContextCompat.getColor(this@PairOnePageActivity, R.color.mainColor)) {
                appendClickable("Reconnect") {
                    // 跳转到重新连接页面
                    startActivity(
                        Intent(
                            this@PairOnePageActivity,
                            PairReconnectActivity::class.java
                        )
                    )
                }
            }
        }
        binding.tvTwo.movementMethod = LinkMovementMethod.getInstance() // 设置了才能点击
        binding.tvTwo.highlightColor = ResourcesCompat.getColor( // 设置之后点击才不会出现背景颜色
            resources,
            com.cl.common_base.R.color.transparent,
            theme
        )


        // Rv
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = adapter
        binding.rvList.isNestedScrollingEnabled = false

        // 延时任务
        job = mViewModel.countDownCoroutines(
            1000 * 60 * 50,
            lifecycleScope,
            onTick = {},
            onStart = {},
            onFinish = {
                runOnUiThread {
                    startActivity(
                        Intent(
                            this@PairOnePageActivity,
                            PairBleScanTimeOutActivity::class.java
                        )
                    )
                }
            })
    }

    override fun observe() {
    }

    override fun initData() {
        adapter.setOnItemClickListener { _, _, position ->
            // 跳转配网界面,  附带设备名字
            val intent = Intent(this@PairOnePageActivity, PairDistributionWifiActivity::class.java)
            intent.putExtra(KEY_DEVICE_DATA, adapter.data[position])
            startActivity(intent)
        }
    }

    override fun onBleChange(status: String) {
        super.onBleChange(status)
        when (status) {
            Constants.Ble.KEY_BLE_ON -> {
                checkPermissionAndStartScan()
                logI("KEY_BLE_ON")
            }
            Constants.Ble.KEY_BLE_OFF -> {
                ToastUtil.shortShow("Bluetooth is turned off")
                logI("KEY_BLE_OFF")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.setList(mutableListOf())
        checkPermissionAndStartScan()
    }

    /**
     * 定位权限开启弹窗
     */
    private val locationPop by lazy {
        XPopup.Builder(this@PairOnePageActivity)
            .isDestroyOnDismiss(false)
            .enableDrag(false)
            .maxHeight(dp2px(600f))
            .dismissOnTouchOutside(true)
            .asCustom(
                PairLocationPop(this@PairOnePageActivity) {
                    val intent =
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            )
    }

    /**
     * 检查权限以及开启扫描
     */
    private fun checkPermissionAndStartScan() {
        if (PermissionChecker.hasPermissions(
                this@PairOnePageActivity,
                *mViewModel.blePer.value!!
            )
        ) {
            // 权限都同意之后，那么直接开启扫描
            startScan()
        } else {
            // 适配Android12
            // 首先判断地理位置权限
            PermissionX.init(this@PairOnePageActivity)
                .permissions(
                    *mViewModel.blePer.value!!
                )
                .onForwardToSettings { scope, deniedList ->
                    // 用户点击不再询问时,回调
                    // 或者点击肯定时,也会回调此方法
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "You need to allow necessary permissions in Settings manually",
                        "OK",
                        "Cancel"
                    )
                }
                .explainReasonBeforeRequest()
                .onExplainRequestReason { scope, deniedList ->
                    // 用户单次拒绝权限时,回调
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Core fundamental are based on these permissions",
                        "OK",
                        "Cancel"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        logI("These permissions are Granted: $deniedList")
                        if (LocationUtil.isLocationEnabled(this@PairOnePageActivity)) {
                            // 判断蓝牙是否打开
                            if (BleUtil.isBleEnabled()) {
                                // 调用涂鸦开始扫描
                                startScan()
                            } else {
                                // 没有打开蓝牙
                                // 底部弹窗开启蓝牙
                                guideBlePop.show()
                            }
                        } else {
                            // todo 开启定位开关
                            // Open location service
                            locationPop.show()
                        }
                    } else {
                        // todo 拒绝的提示
                        logE("These permissions are denied: $deniedList")
                    }
                }
        }
    }

    /**
     * 调用涂鸦都蓝牙扫描
     */
    private fun startScan() {
        logI("one Page：startScaning")
        // 开启涂鸦扫描共呢个
        // Scan Single Ble Device
        TuyaHomeSdk.getBleOperator().startLeScan(60 * 1000, ScanType.SINGLE) { bean ->
            logI("startScan $bean")
            val bleDats = PairBleData()
            val myBleData = PairBleData.MyScanDeviceBean()
            // 拼接设备名字
            bleDats.subName =
                "hey abby-${bean.uuid.substring(bean.uuid.length - 4, bean.uuid.length)}"
            /**
             *   String id;
            String name;
            String providerName;
            String data;
            String configType;
            String productId;
            String uuid;
            String mac;
            String address;
            int deviceType;
            boolean isbind = false;
            int flag = 0;
             */
            myBleData.id = bean.id
            myBleData.name = bean.name
            myBleData.providerName = bean.providerName
            myBleData.data = bean.data
            myBleData.configType = bean.configType
            myBleData.productId = bean.productId
            myBleData.uuid = bean.uuid
            myBleData.mac = bean.mac
            myBleData.address = bean.address
            myBleData.deviceType = bean.deviceType
            myBleData.isbind = bean.isbind
            myBleData.flag = bean.flag
            bleDats.bleData = myBleData
            // 单点蓝牙
            if (bean?.configType == BleConfigType.CONFIG_TYPE_WIFI.type) {
                // 查询设备名称
                TuyaHomeSdk.getActivatorInstance().getActivatorDeviceInfo(
                    bean.productId,
                    bean.uuid,
                    bean.mac,
                    object : ITuyaDataCallback<ConfigProductInfoBean?> {
                        override fun onSuccess(result: ConfigProductInfoBean?) {
                            // 设置适配器 , 添加新数据
                            bleDats.name = result?.name
                            bleDats.icon = result?.icon
                            // 需要过滤
                            if (adapter.data.filter { it.subName == bleDats.subName }.isEmpty()) {
                                adapter.addData(bleDats)
                            }
                            // 设置标题
                            binding.title.setTitle("2/3")
                        }

                        override fun onError(errorCode: String, errorMessage: String) {
                            logE("startScan, getActivatorInstance : $errorCode :: errorMessage: $errorMessage")
                        }
                    })
//                TuyaHomeSdk.getBleManager().startBleConfig(tuYaHomeId.toLong(), bean.uuid, null,
//                    object : ITuyaBleConfigListener {
//                        override fun onSuccess(bean: DeviceBean?) {
//                            setPbViewVisible(false)
//                            Toast.makeText(
//                                this@DeviceConfigBleActivity,
//                                "Config Success",
//                                Toast.LENGTH_LONG
//                            ).show()
//                            finish()
//                        }
//
//                        override fun onFail(code: String?, msg: String?, handle: Any?) {
//                            setPbViewVisible(false)
//                            Toast.makeText(
//                                this@DeviceConfigBleActivity,
//                                "Config Failed",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    companion object {
        // 设备数据
        const val KEY_DEVICE_DATA = "key_device_data"
    }

}