package com.cl.modules_pairing_connection.ui

import android.content.Intent
import android.text.method.LinkMovementMethod
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logE
import com.cl.common_base.ext.logI
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.GuideBlePop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.span.appendClickable
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_pairing_connection.PairScanListAdapter
import com.cl.modules_pairing_connection.R
import com.cl.modules_pairing_connection.databinding.PairScanBleBinding
import com.cl.modules_pairing_connection.request.PairBleData
import com.cl.modules_pairing_connection.viewmodel.PairDistributionWifiViewModel
import com.cl.common_base.report.Reporter
import com.cl.modules_pairing_connection.databinding.PairWifiScanBleBinding
import com.lxj.xpopup.XPopup
import com.thingclips.smart.android.ble.api.BleConfigType
import com.thingclips.smart.android.ble.api.ScanType
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.ConfigProductInfoBean
import com.thingclips.smart.sdk.api.IThingDataCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


/**
 *  涂鸦wifi设备配对的第一个界面、扫描
 *  当能扫描到设备了，那么就是第二个配对界面了
 * @author 李志军 2022-08-03 14:43
 */
@Route(path = RouterPath.PairConnect.PAGE_WIFI_DEVICE_SCAN)
@AndroidEntryPoint
class PairWifiOnePageActivity : BaseActivity<PairWifiScanBleBinding>() {

    // 列表适配器
    private val adapter by lazy {
        PairScanListAdapter(mutableListOf())
    }

    // HomeID
    private val tuYaHomeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }

    /**
     * 不同的涂鸦wifi设备
     */
    private val tuYaWifiDevice by lazy {
        intent.getStringExtra(Constants.Pair.KEY_PAIR_WIFI_DEVICE)
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
            color(ContextCompat.getColor(this@PairWifiOnePageActivity, R.color.mainColor)) {
                appendClickable("Reconnect") {
                    // 跳转到重新连接页面
                    startActivity(
                        Intent(
                            this@PairWifiOnePageActivity,
                            PairReconnectActivity::class.java
                        )
                    )
                }
            }
        }

        binding.tvDesc.text = when(tuYaWifiDevice) {
            Constants.Pair.KEY_PAIR_WIFI_DEVICE_BIG_TEMP -> "Searching for the device...\n" +
                    "Please press the on/off button for 3-5 seconds till you see the Bluetooth icon."
            Constants.Pair.KEY_PAIR_WIFI_DEVICE_SMALL_TEMP -> "Searching for the device...\n" +
                    "Please press the on/off button for 3-5 seconds till you see the Bluetooth icon."
            else -> ""
        }

        binding.ivOne.setBackgroundResource(when(tuYaWifiDevice) {
            Constants.Pair.KEY_PAIR_WIFI_DEVICE_BIG_TEMP -> R.mipmap.pair_wifi_device_one
            Constants.Pair.KEY_PAIR_WIFI_DEVICE_SMALL_TEMP -> R.mipmap.pair_wifi_device_two
            else -> R.mipmap.pair_wifi_device_one
        })

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
        //  这个延时任务5分钟
        job = mViewModel.countDownCoroutines(
            10 * 6 * 5,
            lifecycleScope,
            onTick = {
                if (it != 0) return@countDownCoroutines
                startActivity(
                    Intent(
                        this@PairWifiOnePageActivity,
                        PairBleScanTimeOutActivity::class.java
                    )
                )
                job?.cancel()
            },
            onStart = {},
            onFinish = {
                // todo 这个finish也指的是当前页面被关闭, 定时任务不能放在这个地方.
                job?.cancel()
            })
    }

    override fun observe() {
    }

    override fun initData() {
        adapter.setOnItemClickListener { _, _, position ->
            // 跳转配网界面,  附带设备名字
            val intent = Intent(this@PairWifiOnePageActivity, PairDistributionWifiActivity::class.java)
            intent.putExtra(KEY_DEVICE_DATA, adapter.data[position])
            logI("KEY_DEVICE_DATA: ${adapter.data[position]}")
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
     * 检查权限以及开启扫描
     */
    private fun checkPermissionAndStartScan() {
        PermissionHelp().checkConnect(
            this@PairWifiOnePageActivity,
            supportFragmentManager,
            true,
            object : PermissionHelp.OnCheckResultListener {
                override fun onResult(result: Boolean) {
                    if (!result) return
                    startScan()
                }
            })
    }

    /**
     * 调用涂鸦都蓝牙扫描
     */
    private fun startScan() {
        logI("one Page：startScaning")
        // 开启涂鸦扫描共呢个
        // Scan Single Ble Device
        ThingHomeSdk.getBleOperator().startLeScan(60 * 1000, ScanType.SINGLE) { bean ->
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
                ThingHomeSdk.getActivatorInstance().getActivatorDeviceInfo(
                    bean.productId,
                    bean.uuid,
                    bean.mac,
                    object : IThingDataCallback<ConfigProductInfoBean?> {
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
                            Reporter.reportTuYaError("getActivatorInstance", errorMessage, errorCode)
                        }
                    })
//                ThingHomeSdk.getBleManager().startBleConfig(tuYaHomeId.toLong(), bean.uuid, null,
//                    object : IThingBleConfigListener {
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