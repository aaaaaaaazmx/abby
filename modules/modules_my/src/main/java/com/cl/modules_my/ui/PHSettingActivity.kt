package com.cl.modules_my.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alibaba.android.arouter.launcher.ARouter
import com.bhm.ble.BleManager
import com.bhm.ble.utils.BleLogger
import com.cl.common_base.bean.ServiceNode
import com.cl.common_base.bean.CharacteristicNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.xpopup
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.video.videoUiHelp
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyPhSettingActivityBinding
import com.cl.modules_my.viewmodel.BlePairViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ph笔数据设置界面
 */
@AndroidEntryPoint
class PHSettingActivity : BaseActivity<MyPhSettingActivityBinding>() {

    @Inject
    lateinit var mViewMode: BlePairViewModel

    // true 直接跳转到设备列表、 false 直接关闭当前页面
    private val intentFlag by lazy {
        intent.getBooleanExtra("intent_flag", false)
    }

    // 设备ID
    private val deviceId by lazy {
        intent.getStringExtra("deviceId")
    }

    override fun initView() {
        binding.title.setLeftClickListener {
            if (intentFlag) {
                // 跳转到设备列表
                ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                    .navigation()
            }
            setResult(RESULT_OK)
            finish()
        }
        mViewMode.systemConfig()

        // 检查背景状态
        /*if (mViewMode.notFindConnectPhDevice()) binding.tvSync.setBackgroundResource(com.cl.common_base.R.drawable.create_button_uncheck) else binding.tvSync.setBackgroundResource(
            com.cl.common_base.R.drawable.create_button_check
        )
        ViewUtils.setVisible(mViewMode.notFindConnectPhDevice(), binding.tvUnConnect)
        mViewMode.currentBleDevice.value?.let { BleManager.get().replaceBleConnectCallback(it, mViewMode.getConnectCallback()) }*/

        checkPermissionAndStartScan()
    }

    override fun onBackPressed() {
        if (intentFlag) {
            // 跳转到设备列表
            ARouter.getInstance().build(RouterPath.My.PAGE_MY_DEVICE_LIST)
                .navigation()
        }
        setResult(RESULT_OK)
        finish()
    }

    /**
     * 查找当前设备是否有连接过
     */
    private fun checkHasPhBle() {
        // 没有指定链接设备，因为老板认为用户只能买的起一个
        // 那么就只能判断，当前是否连接，没连接那么就开始扫描，然后连接第一个BLE-9908的设备。
        BleManager.get().getAllConnectedDevice()?.firstOrNull { it.deviceName == Constants.Ble.KEY_PH_DEVICE_NAME }.apply {
            if (this == null) binding.tvSync.setBackgroundResource(com.cl.common_base.R.drawable.create_button_uncheck) else binding.tvSync.setBackgroundResource(
                com.cl.common_base.R.drawable.create_button_check
            )
            ViewUtils.setVisible(this == null, binding.tvUnConnect)
            if (this == null) {
                mViewMode.disConnectPhDevice()
                lifecycleScope.launch {
                    showProgressLoading()
                    delay(1200)
                    // 开始扫描，连接第一个扫描出来的设备
                    BleManager.get().startScan(mViewMode.getScanCallback(true))
                }
                return
            }
            if (!mViewMode.isConnected(this)) {
                lifecycleScope.launch {
                    showProgressLoading()
                    delay(1200)
                    // 连接设备
                    mViewMode.connect(this@apply)
                }
                return
            }
            showProgressLoading()
            // 有设备，那么就获取数据
            mViewMode.setCurrentBleDevice(this)
            // 获取值
            getPhData()
        }
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
                binding.tvSync.setBackgroundResource(com.cl.common_base.R.drawable.create_button_uncheck)
                ViewUtils.setVisible(binding.tvUnConnect)
                ToastUtil.shortShow("Bluetooth is turned off")
                logI("KEY_BLE_OFF")
            }
        }
    }

    private fun checkPermissionAndStartScan() {
        PermissionHelp().checkConnect(
            this@PHSettingActivity,
            supportFragmentManager,
            true,
            object : PermissionHelp.OnCheckResultListener {
                override fun onResult(result: Boolean) {
                    if (!result) return
                    checkHasPhBle()
                }
            })
    }

    override fun observe() {
        mViewMode.apply {
            systemConfig.observe(this@PHSettingActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    if (data == null) return@success
                    if (data?.isEmpty() == true) return@success
                    runCatching {
                        data?.get(0)?.value?.let {
                            binding.videoItemPlayer.apply {
                                videoUiHelp(it, 1)
                                // seekOnStart = item.videoPosition ?: 0L
                            }
                        }
                    }
                }
            })

            deleteDevice.observe(this@PHSettingActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    // 断开当前蓝牙
                    mViewMode.disConnectPhDevice()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            })

            scanLiveData.observe(this@PHSettingActivity, Observer {
                hideProgressLoading()
            })
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mViewMode.listLogStateFlow.collect {
                        hideProgressLoading()
                        // 解析当前特征值
                        val value = it.byteArray
                        value?.let { it1 ->
                            parseValues(deCode(it1), time = it.time)
                        } ?: let { _ ->
                            if (it.msg.contains("数据")) return@collect
                            ToastUtil.shortShow(it.msg)
                        }
                    }
                }

                // 连接成功回调
                launch {
                    mViewMode.refreshStateFlow.collect {
                        // 刷新设备，点击连接，成功与否。
                        delay(300)
                        hideProgressLoading()
                        it?.bleDevice?.let { bleDevice ->
                            val isConnected = mViewMode.isConnected(bleDevice)
                            if (isConnected) {
                                logI("BLe -> msg: 连接成功")
                                // ToastUtil.shortShow("Connection successful.")
                                binding.tvSync.setBackgroundResource(com.cl.common_base.R.drawable.create_button_check)
                                ViewUtils.setGone(binding.tvUnConnect)
                                // 有设备，那么就获取数据
                                mViewMode.setCurrentBleDevice(bleDevice)
                                // 获取数据
                                checkPermissionAndStartScan()
                            } else {
                                binding.tvSync.setBackgroundResource(com.cl.common_base.R.drawable.create_button_uncheck)
                                ViewUtils.setVisible(binding.tvUnConnect)
                                logI("BLe -> msg: 连接失败")
                                // ToastUtil.shortShow("Connection failed.")
                            }
                        }
                    }
                }
            }
        }
    }

    // 解密
    private fun deCode(pValue: ByteArray): ByteArray {
        val len = pValue.size
        for (i in len - 1 downTo 1) {
            var tmp = pValue[i].toInt()
            val hibit1 = (tmp and 0x55) shl 1
            val lobit1 = (tmp and 0xAA) shr 1
            tmp = pValue[i - 1].toInt()
            val hibit = (tmp and 0x55) shl 1
            val lobit = (tmp and 0xAA) shr 1

            pValue[i] = (hibit1 or lobit).inv().toByte()
            pValue[i - 1] = (hibit or lobit1).inv().toByte()
        }
        BleLogger.i("pValue: $pValue")
        return pValue
    }

    // 解析
    @SuppressLint("SetTextI18n")
    private fun parseValues(decrypted: ByteArray, time: Long) {
        val phHigh = decrypted[3].toInt() and 0xFF
        val phLow = decrypted[4].toInt() and 0xFF
        val ecHigh = decrypted[5].toInt() and 0xFF
        val ecLow = decrypted[6].toInt() and 0xFF
        val tdsHigh = decrypted[7].toInt() and 0xFF
        val tdsLow = decrypted[8].toInt() and 0xFF
        val tempHigh = decrypted[13].toInt() and 0xFF
        val tempLow = decrypted[14].toInt() and 0xFF

        val ph = ((phHigh shl 8) or phLow) / 100.0
        val ec = (ecHigh shl 8) or ecLow
        val tds = (tdsHigh shl 8) or tdsLow
        val temp = (tempHigh shl 8) or tempLow

        //BleLogger.i("pH: $ph, EC: $ec, TDS: $tds, TEMP: $temp")
        logI("decrypted: ${decrypted.toList()}")
        logI("pH: $ph, EC: $ec, TDS: $tds, TEMP: $temp, backlightStatus: ${getBacklightStatus(decrypted)}")
        // 上述的代码
        val toSpeak = "The pH value is: $ph, The EC value is: $ec, $ec, The TDS value is: $tds"
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        // 检查TalkBack是否启用
        if (accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled) {
            val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
            event.text.add(toSpeak)
            accessibilityManager.sendAccessibilityEvent(event)
        }

        binding.editText1.text = ph.toString()
        binding.editText1.setTextColor(ContextCompat.getColor(this@PHSettingActivity, com.cl.common_base.R.color.mainColor))

        binding.editText11.text = tds.toString()
        binding.editText11.setTextColor(ContextCompat.getColor(this@PHSettingActivity, com.cl.common_base.R.color.mainColor))

        binding.editText12.text = ec.toString()
        binding.editText12.setTextColor(ContextCompat.getColor(this@PHSettingActivity, com.cl.common_base.R.color.mainColor))

        // Last data synced on 08/22/2023 11:23AM.
        ViewUtils.setVisible(binding.tvSyncDesc)
        binding.tvSyncDesc.text = "Last data synced on ${DateHelper.formatTime(time, "MM/dd/yyyy hh:mm a")}"

        // 背光状态
        binding.editText123.isChecked = getBacklightStatus(decrypted)
    }

    override fun initData() {
        binding.editText123.setOnClickListener {
            logI("editText123")
            mViewMode.currentBleDevice.value?.let { device ->
                showProgressLoading()
                mViewMode.writeDataForPh(device, binding.editText123.isChecked)
            }
        }

        binding.tvSync.setOnClickListener {
            if (binding.tvUnConnect.isVisible) {
                xpopup(this@PHSettingActivity) {
                    isDestroyOnDismiss(false)
                    asCustom(
                        BaseCenterPop(
                            this@PHSettingActivity,
                            content = "Please pair a bluetooth PH meter first to obtain the data, if you already paired one, please make sure to turn it on.",
                            isShowCancelButton = false,
                            onConfirmAction = {
                                // 先断开次设备、
                                // mViewMode.disConnectPhDevice()
                                mViewMode.stopScan()
                                checkPermissionAndStartScan()
                            })
                    ).show()
                }
                return@setOnClickListener
            }
            checkPermissionAndStartScan()
        }
        binding.unbindCamera.setOnClickListener {
            xpopup(this@PHSettingActivity) {
                isDestroyOnDismiss(false)
                dismissOnTouchOutside(false)
                asCustom(
                    BaseCenterPop(
                        this@PHSettingActivity,
                        content = "Are you certain you wish to delete the PH Meter?",
                        isShowCancelButton = true,
                        cancelText = "No",
                        confirmText = "Yes",
                        onConfirmAction = {
                            // 删除Ph笔
                            mViewMode.deleteDevice(deviceId.toString())
                        })
                ).show()
            }
        }
    }

    /**
     * 获取当前ph的数据
     */
    private fun getPhData() {
        // 获取ph的数据
        mViewMode.currentBleDevice.value?.let {
            val gatt = BleManager.get().getBluetoothGatt(it)
            val list: MutableList<BaseNode> = arrayListOf()
            gatt?.services?.forEachIndexed { index, service ->
                val childList: MutableList<BaseNode> = arrayListOf()
                service.characteristics?.forEachIndexed { position, characteristics ->
                    val characteristicNode = CharacteristicNode(
                        position.toString(),
                        service.uuid.toString(),
                        characteristics.uuid.toString(),
                        getOperateType(characteristics),
                        characteristics.properties,
                        enableNotify = false,
                        enableIndicate = false,
                        enableWrite = false
                    )
                    // 设置当前的服务ID、特征ID
                    if (characteristics.uuid.toString().startsWith(Constants.Ble.KEY_BLE_PH_CHARACTERISTIC_UUID)) {
                        mViewMode.setCurrentCharacteristicId(characteristics.uuid.toString())
                        mViewMode.setCurrentServiceId(service.uuid.toString())
                        mViewMode.currentBleDevice.value?.let { bleDevice ->
                            mViewMode.readData(bleDevice, characteristicNode)
                        }
                    }
                    childList.add(characteristicNode)
                }
                val serviceNode = ServiceNode(
                    index.toString(),
                    service.uuid.toString(),
                    childList
                )
                list.add(serviceNode)
            }
        }
    }

    /**
     * 获取特征值的属性
     */
    private fun getOperateType(characteristic: BluetoothGattCharacteristic): String {
        val property = StringBuilder()
        val charaProp: Int = characteristic.properties
        if (charaProp and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
            property.append("Read")
            property.append(" , ")
        }
        if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
            property.append("Write")
            property.append(" , ")
        }
        if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
            property.append("Write No Response")
            property.append(" , ")
        }
        if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
            property.append("Notify")
            property.append(" , ")
        }
        if (charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
            property.append("Indicate")
            property.append(" , ")
        }
        if (property.length > 1) {
            property.delete(property.length - 2, property.length - 1)
        }
        return if (property.isNotEmpty()) {
            property.toString()
        } else {
            ""
        }
    }

    /**
     * 获取背光状态
     */
    fun getBacklightStatus(pValue: ByteArray): Boolean {
        /*val len = pValue.size
        for (i in 0 until len - 1) {
            val tmp = pValue[i].toInt().inv()
            val hibit = (tmp and 0xAA) shr 1
            val lobit = (tmp and 0x55) shl 1
            val tmp1 = pValue[i + 1].toInt().inv()
            val hibit1 = (tmp1 and 0xAA) shr 1
            val lobit1 = (tmp1 and 0x55) shl 1

            pValue[i] = (hibit or lobit1).toByte()
            pValue[i + 1] = (hibit1 or lobit).toByte()
        }*/
        // 第17个字节第3位（从1开始计数）
        return (pValue[17].toInt() shr 3 and 1) == 1
    }
}