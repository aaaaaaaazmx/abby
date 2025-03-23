package com.cl.modules_login.ui

import android.annotation.SuppressLint
import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.AllDpBean
import com.cl.common_base.bean.EnvironmentInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.containsIgnoreCase
import com.cl.common_base.ext.equalsIgnoreCase
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.temperatureConversionTwo
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.databinding.LoginOfflineMainBinding
import com.cl.modules_login.response.OffLineDeviceBean
import com.cl.modules_login.viewmodel.OffLineMainModel
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.bean.DeviceBean
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@Route(path = RouterPath.LoginRegister.PAGE_NEW_MAIN)
@AndroidEntryPoint
class OffLineMainActivity : BaseActivity<LoginOfflineMainBinding>() {

    private val isMetric = { Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false) }

    @Inject
    lateinit var viewModel: OffLineMainModel

    private val devId = {
        Prefs.getString(Constants.Tuya.KEY_DEVICE_ID)
    }

    // 涂鸦家庭ID
    private val homeId by lazy {
        Prefs.getLong(Constants.Tuya.KEY_HOME_ID, -1L)
    }


    private val strainName by lazy {
        intent.getStringExtra("strainName")
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val devId = intent?.getStringExtra(Constants.Tuya.KEY_DEVICE_ID)
        getCurrentDeviceData(devId)
    }

    override fun initView() {
        // 获取当前设备
        getCurrentDeviceData(devId())
        if (!strainName.isNullOrBlank()) {
            binding.tv.text = strainName
        }
    }

    override fun observe() {
    }

    override fun initData() {
        binding.ivLightStatus.setSafeOnClickListener {
            // 跳转到灯光设置页面
            GSON.toJsonInBackground(viewModel.currentDeviceData.value?.dps) { json ->
                startActivity(Intent(this, OffLineHardSetActivity::class.java).apply {
                    putExtra("123123", json)
                    putExtra("devId", viewModel.currentDeviceData.value?.devId)
                })
            }
        }

        // 跳转设置界面
        binding.cbNotify.setSafeOnClickListener {
            startActivity(Intent(this, OffLineSettingActivity::class.java))
        }
        // 排水
        binding.cbDrain.setSafeOnClickListener {
            with(DeviceControl) {
                get().success {}
                    .error { code, error ->
                        ToastUtil.shortShow(
                            """
                                      pumpWater:
                                      code-> $code
                                      errorMsg-> $error
                                """.trimIndent()
                        )
                    }
                    .pumpWater(binding.cbDrain.isChecked)
            }
        }

        // 童锁
        binding.cbLock.setSafeOnClickListener {
            // 是否打开童锁
            DeviceControl.get().success {}.error { code, error ->
                ToastUtil.shortShow(
                    """
                              childLock:
                              code-> $code
                              errorMsg-> $error
                             """.trimIndent()
                )
            }.childLock(binding.cbLock.isChecked)
        }

        binding.fanIntakeSeekbar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {

            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                DeviceControl.get().success {
                    binding.tvFanValue.text = seekBar?.progress.safeToInt().toString()
                }.error { code, error ->
                    ToastUtil.shortShow(
                        """
                              fanIntake: 
                              code-> $code
                              errorMsg-> $error
                                """.trimIndent()
                    )
                }.fanIntake(seekBar?.progress ?: 0)
            }
        }

        binding.fanExhaustSeekbar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {

            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                DeviceControl.get().success {
                    binding.tvFanExhaustValue.text = seekBar?.progress.safeToInt().toString()
                }.error { code, error ->
                    ToastUtil.shortShow(
                        """
                              fanExhaust: 
                              code-> $code
                              errorMsg-> $error
                                """.trimIndent()
                    )
                }.fanExhaust(seekBar?.progress ?: 0)
            }
        }

        // air_pump
        binding.airPump.setSwitchClickListener {
            val b = binding.airPump.isItemChecked
            logI("1231231: $b")
            // 打开的时候需要提示当前水箱是否有水。
            if (binding.tvGoingWater.text.toString().equalsIgnoreCase("0l")) {
                xpopup(this@OffLineMainActivity) {
                    isDestroyOnDismiss(false)
                    dismissOnTouchOutside(false)
                    asCustom(
                        BaseCenterPop(
                            this@OffLineMainActivity,
                            content = getString(com.cl.common_base.R.string.string_1356),
                            isShowCancelButton = false,
                            confirmText = getString(com.cl.common_base.R.string.string_10),
                            onCancelAction = {
                            },
                            onConfirmAction = {
                                binding.airPump.setItemChecked(false)
                                // 需要恢复到之前到档位
                                DeviceControl.get().success {}.error { code, error ->
                                    ToastUtil.shortShow(
                                        """
                              airPump: 
                              code-> $code
                              errorMsg-> $error
                                """.trimIndent()
                                    )
                                }.airPump(false)
                            })
                    ).show()
                }
                return@setSwitchClickListener
            }
            DeviceControl.get().success {}.error { code, error ->
                ToastUtil.shortShow(
                    """
                              airPump: 
                              code-> $code
                              errorMsg-> $error
                                """.trimIndent()
                )
            }.airPump(b)
        }

        binding.cbNight.setSafeOnClickListener {
            startActivity(Intent(this@OffLineMainActivity, OffLineDeviceActivity::class.java).apply {
                putExtra("devId", devId())
            })
        }
    }

    // 获取当前设备的ID
    // 当前选中的是第几台设备
    // 每次只需要更改这个数字即可。
    private fun getCurrentDeviceData(devId: String?) {
        // 通过涂鸦读取当前选中的台数数据
        ThingHomeSdk.newHomeInstance(homeId).getHomeDetail(object : IThingHomeResultCallback {
            override fun onSuccess(bean: HomeBean?) {
                // 当前设备
                var currentDevice = bean?.deviceList?.firstOrNull { it.devId == devId }
                if (null == currentDevice) {
                    // 默认选择第一个机器
                    currentDevice = bean?.deviceList?.firstOrNull {
                        it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1) || it.productId.containsIgnoreCase(
                            OffLineDeviceBean.DEVICE_VERSION_OG
                        ) || it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_OG_BLACK) || it.productId.containsIgnoreCase(
                            OffLineDeviceBean.DEVICE_VERSION_OG_PRO
                        ) || it.productId.containsIgnoreCase(OffLineDeviceBean.DEVICE_VERSION_O1_PRO) || it.productId.containsIgnoreCase(
                            OffLineDeviceBean.DEVICE_VERSION_O1_SOIL
                        )
                    }
                }
                currentDevice?.let { viewModel.setCurrentDeviceData(it) }
                Prefs.putStringAsync(Constants.Tuya.KEY_DEVICE_ID, currentDevice?.devId.toString())

                // 获取植物名字
                binding.tv.text = Prefs.getObjects()?.firstOrNull { it.id == currentDevice?.devId }?.strainName ?: currentDevice?.name


                // 获取环境信息
                getEnvData(currentDevice)
                if (currentDevice?.isOnline == false) {
                    // 跳转到设备离线页面
                    startActivity(Intent(this@OffLineMainActivity, OffLineActivity::class.java))
                    return
                }
            }

            override fun onError(errorCode: String?, errorMsg: String?) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
         getCurrentDeviceData(devId())
    }

    // 获取当前设备环境信息
    @SuppressLint("SetTextI18n")
    private fun getEnvData(device: DeviceBean?) {
        val tempUnit = if (isMetric()) "℃" else "℉"
        device?.let {
            val envReq = EnvironmentInfoReq(deviceId = it.devId)
            it.dps?.forEach { (key, value) ->
                when (key) {
                    TuYaDeviceConstants.KEY_DEVICE_WATER_TEMPERATURE -> {
                        envReq.waterTemperature = value.safeToInt()
                        // 水温
                        val temp = temperatureConversionTwo(value.safeToFloat(), isMetric())
                        binding.tvGoingTank.text = "$temp$tempUnit"
                    }

                    TuYaDeviceConstants.KEY_DEVICE_VENTILATION -> {
                        envReq.ventilation = value.safeToInt()
                        // 排气扇
                        binding.tvFanExhaustValue.text = value.safeToInt().toString()
                        binding.fanExhaustSeekbar.setProgress(value.safeToFloat())
                    }

                    TuYaDeviceConstants.KEY_DEVICE_TEMP_CURRENT -> {
                        envReq.tempCurrent = value.safeToInt()
                        // 盒子温度
                        val temp = temperatureConversionTwo(value.safeToFloat(), isMetric())
                        binding.tvGoingChamber.text = "$temp$tempUnit"
                    }

                    TuYaDeviceConstants.KEY_DEVICE_INPUT_AIR_FLOW -> {
                        envReq.inputAirFlow = value.safeToInt()
                        // 进气扇
                        //tv_fan_value.
                        binding.tvFanValue.text = value.safeToInt().toString()
                        // fan_intake_seekbar
                        binding.fanIntakeSeekbar.setProgress(value.safeToFloat())
                    }

                    TuYaDeviceConstants.KEY_DEVICE_HUMIDITY_CURRENT -> {
                        envReq.humidityCurrent = value.safeToInt()
                        // 湿度
                        val temp = temperatureConversionTwo(value.safeToFloat(), isMetric())
                        binding.tvGoing.text = "${value.safeToFloat()}%"
                    }

                    TuYaDeviceConstants.KEY_DEVICE_BRIGHT_VALUE -> {
                        envReq.brightValue = value.safeToInt()
                        // 灯光 是否开启
                        binding.tvLight.text = if (value.safeToFloat() > 0) "ON" else "OFF"
                    }

                    TuYaDeviceConstants.KEY_DEVICE_WATER_LEVEL -> {
                        envReq.waterLevel = value.toString()
                        // 水位
                        binding.tvGoingWater.text = value.toString()
                    }

                    TuYaDeviceConstants.KEY_DEVICE_AIR_PUMP -> {
                        envReq.airpump = value == "true"
                        // 气泵
                        binding.airPump.setItemChecked(value.toString() == "true")
                    }

                    TuYaDeviceConstants.KEY_DEVICE_TURN_OFF_LIGHT -> {
                        // 关灯时间
                        val (startTime, endTime) = pair(0, value.safeToInt())
                        val (ftTurnOn, ftTurnOff) = pairTwo(startTime, endTime)
                        // period_status
                        binding.periodStatus.text = "will turn off at $ftTurnOff today"
                    }

                    TuYaDeviceConstants.KEY_DEVICE_CHILD_LOCK -> {
                        // 童锁
                        binding.cbLock.isChecked = value.toString() == "true"
                    }
                }
            }
        }
    }


    // 广播监听
    /**
     * 设备指令监听
     */
    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onTuYaToAppDataChange(status: String) {
        val tempUnit = if (isMetric()) "℃" else "℉"
        // val map = GSON.parseObject(status, Map::class.java)
        GSON.parseObjectInBackground(status, Map::class.java) { map ->
            map?.forEach { (key, value) ->
                when (key) {
                    // 当用户加了水，是需要动态显示当前水的状态的
                    // 返回多少升水
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_WATER_STATUS_INSTRUCTIONS -> {
                        logI("KEY_DEVICE_WATER_STATUS： $value")
                        binding.tvGoingWater.text = value.toString()
                    }

                    // 排水结束
                    TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_FINISHED_INSTRUCTION -> {
                        binding.cbDrain.isChecked = false
                        /*binding.plantManual.ivDrainStatus.background =
                            resources.getDrawable(
                                R.mipmap.home_drain_start,
                                context?.theme
                            )*/
                    }
                    // 排水暂停
                    TuYaDeviceConstants.DeviceInstructions.KAY_PUMP_WATER_INSTRUCTIONS -> {

                    }

                    // SN修复的通知
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_SN_INSTRUCTION -> {
                        logI("KEY_DEVICE_REPAIR_SN： $value")
                    }

                    // 获取SN的通知
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_REPAIR_REST_STATUS_INSTRUCTION -> {
                        logI("KEY_DEVICE_REPAIR_REST_STATUS： $value")
                        // 判断是什么机型
                        // mViewMode.saveSn(value.toString().split("#")[1])
                    }

                    // 童锁开关
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_CHILD_LOCK_INSTRUCT -> {
                        // mViewMode.setChildLockStatus(value.toString())
                        binding.cbLock.isChecked = value.toString() == "true"
                    }

                    // 打开门
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_DOOR_LOOK_INSTRUCT -> {
                        // mViewMode.setOpenDoorStatus(value.toString())
                    }

                    // 是否关闭门
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_DOOR -> {
                    }

                    // ----- 开始， 下面的都是需要传给后台的环境信息
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_BRIGHT_VALUE_INSTRUCTION -> {
                        /*mViewMode.tuYaDps?.put(
                            TuYaDeviceConstants.KEY_DEVICE_BRIGHT_VALUE,
                            value.toString()
                        )
                        mViewMode.setCurrentGrowLight(value.toString())*/
                        // 查询灯光信息
                        // queryAllDp()
                    }

                    // 140 dp点
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_TIME_STAMP -> {
                        // val allDpBean = GSON.parseObject(value.toString(), AllDpBean::class.java)
                        GSON.parseObjectInBackground(
                            value.toString(),
                            AllDpBean::class.java
                        ) { allDpBean ->
                            // cmd == 3 返回实际灯光配置参数
                            // cmd == 1 返回实际全部配置参数
                        }
                    }

                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_HUMIDITY_CURRENT_INSTRUCTION -> {
                        // 湿度
                        val temp = temperatureConversionTwo(value.safeToFloat(), isMetric())
                        binding.tvGoing.text = "${value.safeToFloat()}%"
                    }

                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_INPUT_AIR_FLOW_INSTRUCTION -> {
                        // 进气扇
                        //tv_fan_value.
                        binding.tvFanValue.text = value.safeToInt().toString()
                        // fan_intake_seekbar
                        binding.fanIntakeSeekbar.setProgress(value.safeToFloat())
                    }

                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_TEMP_CURRENT_INSTRUCTION -> {
                        // 盒子温度
                        val temp = temperatureConversionTwo(value.safeToFloat(), isMetric())
                        binding.tvGoingChamber.text = "$temp$tempUnit"
                    }

                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_VENTILATION_INSTRUCTION -> {
                        // 排气扇
                        binding.tvFanExhaustValue.text = value.safeToInt().toString()
                        binding.fanExhaustSeekbar.setProgress(value.safeToFloat())
                    }

                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_WATER_TEMPERATURE_INSTRUCTION -> {
                        val temp = temperatureConversionTwo(value.safeToFloat(), isMetric())
                        binding.tvGoingTank.text = "$temp$tempUnit"
                    }
                    // --------- 到这里结束
                    // 植物高度
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_PLANT_HEIGHT_INSTRUCTION -> {
                        // mViewMode.setPlantHeight(value.toString())
                    }

                    // 气泵
                    TuYaDeviceConstants.DeviceInstructions.KEY_DEVICE_AIR_PUMP_INSTRUCTION -> {
                        binding.airPump.setItemChecked(value.toString() == "true")
                    }
                }
            }
        }
    }

    /**
     * 设备状态监听
     */
    override fun onDeviceChange(status: String) {
        super.onDeviceChange(status)
        when (status) {
            Constants.Device.KEY_DEVICE_OFFLINE -> {
                startActivity(Intent(this@OffLineMainActivity, OffLineActivity::class.java))
            }

            Constants.Device.KEY_DEVICE_ONLINE -> {
            }

            Constants.Device.KEY_DEVICE_REMOVE -> {
                // startActivity(Intent(this@OffLineMainActivity, OffLineActivity::class.java))
            }
        }
    }


    private fun pair(openTime: Int, closeTime: Int): Pair<Int, Int> {
        // 0- 12, 12-24
        val startTime = when (openTime) {
            0 -> 12
            12 -> 24
            else -> openTime ?: 12
        }

        val endTime = when (closeTime) {
            0 -> 12
            12 -> 24
            else -> closeTime ?: 12
        }
        return Pair(startTime, endTime)
    }

    private fun pairTwo(startTime: Int, endTime: Int): Pair<String, String> {
        // 0- 12, 12-24
        val ftTurnOn = startTime.let {
            if (it > 12) {
                "${it - 12}:00 PM"
            } else if (it < 12) {
                "${it}:00 AM"
            } else if (it == 12) {
                "12:00 AM"
            } else {
                "12:00 AM"
            }
        } ?: "12:00 AM"

        val ftTurnOff = endTime.let {
            if (it > 12) {
                "${it - 12}:00 PM"
            } else if (it < 12) {
                "${it}:00 AM"
            } else if (it == 12) {
                "12:00 AM"
            } else {
                "12:00 AM"
            }
        } ?: "12:00 PM"
        return Pair(ftTurnOn, ftTurnOff)
    }
}