package com.cl.modules_my.ui

import android.content.Intent
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bhm.ble.BleManager
import com.bhm.ble.attribute.BleOptions
import com.bhm.ble.device.BleDevice
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.letMultiple
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.Prefs
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.R
import com.cl.modules_my.adapter.PairBleAdapter
import com.cl.modules_my.databinding.MyBlePairActivityBinding
import com.cl.modules_my.viewmodel.BlePairViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.cos

/**
 * Ph笔配对界面
 * 可能是所有蓝牙设备的配对界面
 */
@AndroidEntryPoint
class PhPairActivity : BaseActivity<MyBlePairActivityBinding>() {

    @Inject
    lateinit var mViewMode: BlePairViewModel

    // 配件ID
    private val accessoryId by lazy {
        intent.getStringExtra("accessoryId")
    }

    // 设备Id
    private val deviceId by lazy {
        intent.getStringExtra("deviceId")
    }

    // 蓝牙类型
    private val bleType by lazy {
        intent.getStringExtra(Constants.Ble.KEY_BLE_TYPE) ?: Constants.Ble.TYPE_PH
    }

    // 是否是第一次绑定
    private val isFirstBind by lazy {
        intent.getBooleanExtra(Constants.Ble.KEY_BLE_IS_FIRST_BIND, true)
    }

    override fun initView() {
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = adapter
        initBle()

        binding.title.setRightButtonText("Refresh").setRightClickListener {
            BleManager.get().stopScan()
            checkPermissionAndStartScan()
        }
    }

    private fun initBle() {
        /*BleManager.get().init(
            application,
            BleOptions.Builder()
                .setScanMillisTimeOut(5000)
                .setConnectMillisTimeOut(5000)
                //一般不推荐autoSetMtu，因为如果设置的等待时间会影响其他操作
                .setMtu(100, true)
                .setScanDeviceName(DEVICE_NAME)
                .setAutoConnect(true)
                .setMaxConnectNum(Constants.Ble.KEY_BLE_MAX_CONNECT)
                .setConnectRetryCountAndInterval(2, 1000)
                .build()
        )*/

        // 进入蓝牙配对界面，首先断开蓝牙。
        when(bleType) {
            Constants.Ble.TYPE_PH -> {
                BleManager.get().getAllConnectedDevice()?.firstOrNull{ it.deviceName == Constants.Ble.KEY_PH_DEVICE_NAME }?.let {
                    BleManager.get().disConnect(it)
                }
            }
        }
    }

    // 列表适配器
    private val adapter by lazy {
        PairBleAdapter(mViewMode.listDRData)
    }

    override fun onBleChange(status: String) {
        super.onBleChange(status)
        when (status) {
            Constants.Ble.KEY_BLE_ON -> {
                // 这个界面是不需要是否有连接过设备的， 其他界面在蓝牙开关的时候，都需要判断之前是否连接过设备，连接过那么就直接连接了。
                checkPermissionAndStartScan()
                logI("KEY_BLE_ON")
            }

            Constants.Ble.KEY_BLE_OFF -> {
                adapter.notifyItemRangeRemoved(0, mViewMode.listDRData.size)
                mViewMode.listDRData.clear()
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

    private fun checkPermissionAndStartScan() {
        PermissionHelp().checkConnect(
            this@PhPairActivity,
            supportFragmentManager,
            true,
            object : PermissionHelp.OnCheckResultListener {
                override fun onResult(result: Boolean) {
                    if (!result) return
                    BleManager.get().startScan(mViewMode.getScanCallback(false))
                }
            })
    }

    override fun observe() {

        mViewMode.apply {
            accessoryAdd.observe(this@PhPairActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    val devId = data?.accessoryDeviceId
                    when (bleType) {
                        Constants.Ble.TYPE_PH -> {
                            startActivity(Intent(this@PhPairActivity, PHSettingActivity::class.java).apply {
                                putExtra("intent_flag", true).apply {
                                    putExtra("deviceId", devId)
                                }
                            })
                            finish()
                        }
                    }
                }
                loading { showProgressLoading() }
            })
        }

        lifecycleScope.launch {
            mViewMode.scanStopStateFlow.collect {
                // 扫描停止
                hideProgressLoading()
            }
        }

        lifecycleScope.launch {
            mViewMode.listDRStateFlow.collect {
                // 扫描到的设备, 用于填充adapter
                if (it.deviceName != null && it.deviceAddress != null) {
                    if (it.deviceName == Constants.Ble.KEY_PH_DEVICE_NAME) {
                        val position = (adapter.itemCount) - 1
                        adapter.notifyItemInserted(position)
                        binding.rvList.smoothScrollToPosition(position)
                    }
                }
            }
        }

        lifecycleScope.launch {
            mViewMode.refreshStateFlow.collect {
                // 刷新设备，点击连接，成功与否。
                delay(300)
                hideProgressLoading()
                it?.bleDevice?.let { bleDevice ->
                    val position = adapter.data.indexOf(bleDevice)
                    if (position >= 0) {
                        adapter.notifyItemChanged(position)
                    }
                    val isConnected = mViewMode.isConnected(bleDevice)
                    if (isConnected) {
                        logI("BLe -> msg: 连接成功")
                        // ToastUtil.shortShow("Connection successful.")
                        // 连接成功，那么就绑定设备。 然后进行跳转到设置界面
                        letMultiple(accessoryId, deviceId) { a, b ->
                            mViewMode.accessoryAdd(a, b)
                        }
                    } else {
                        logI("BLe -> msg: 连接失败")
                        // ToastUtil.shortShow("Connection failed.")
                    }
                    /* if (it.bleDevice.deviceAddress == "7C:DF:A1:A3:5A:BE") {
                         viewBinding.btnConnect.isEnabled = !isConnected
                     }
                     if (isConnected && autoOpenDetailsActivity) {
                         openDetails(it.bleDevice)
                     }
                     autoOpenDetailsActivity = false*/
                }
            }
        }

    }

    override fun initData() {
        adapter.addChildClickViewIds(R.id.svt_add)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            // 点击连接
            val bleDevice: BleDevice? = adapter.data[position] as BleDevice?
            if (view.id == R.id.svt_add) {
                if (mViewMode.isConnected(bleDevice)) {
                    showProgressLoading("Disconnecting...")
                    mViewMode.disConnect(bleDevice)
                } else {
                    showProgressLoading("Connecting...")
                    mViewMode.connect(bleDevice)
                }
            }
        }
    }
}