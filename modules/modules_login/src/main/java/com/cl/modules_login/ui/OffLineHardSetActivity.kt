package com.cl.modules_login.ui

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.lifecycle.repeatOnLifecycle
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.AllDpBean
import com.cl.common_base.bean.ProModeInfoBean
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.temperatureConversionTwo
import com.cl.common_base.pop.ChooseTimePop
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_login.R
import com.cl.modules_login.databinding.LoginOfflineHardBinding
import com.cl.modules_login.viewmodel.OffLineMainModel
import com.lxj.xpopup.XPopup
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OffLineHardSetActivity : BaseActivity<LoginOfflineHardBinding>() {
    @Inject
    lateinit var mViewMode: OffLineMainModel

    private val dps by lazy {
        intent.getStringExtra("123123")
    }

    private val devId by lazy {
        intent.getStringExtra("devId")
    }

    override fun initView() {
        // json to map
        GSON.parseObjectInBackground(dps, Map::class.java) { map ->
            map?.forEach { (key, value) ->
                when (key) {
                    TuYaDeviceConstants.KEY_DEVICE_VENTILATION -> {
                        // 排气扇
                        binding.tvFanExhaustValue.text = value.safeToInt().toString()
                        binding.fanIntakeSeekbarsss.setProgress(value.safeToFloat())
                    }

                    TuYaDeviceConstants.KEY_DEVICE_INPUT_AIR_FLOW -> {
                        // 进气扇
                        //tv_fan_value.
                        binding.tvFanValue.text = value.safeToInt().toString()
                        // fan_intake_seekbar
                        binding.fanIntakeSeekbar.setProgress(value.safeToFloat())
                    }

                    TuYaDeviceConstants.KEY_DEVICE_BRIGHT_VALUE -> {
                        // 灯光 是否开启
                        binding.tvFanValues.text = value.safeToInt().toString()
                        binding.fanIntakeSeekbars.setProgress(value.safeToFloat())
                    }

                    TuYaDeviceConstants.KEY_DEVICE_MULTIPLE_DP -> {
                        // 解析时间
                        val dpBean = GSON.parseObject(value.toString(), AllDpBean::class.java)
                        val closeTime = dpBean?.gle.safeToInt()
                        val openTime = dpBean?.gls.safeToInt()
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


                        val fanIntake = dpBean?.`in`
                        val fanExhaust = dpBean?.ex
                        val lightIntensity = dpBean?.gl
                        val lightSchedule = "$ftTurnOn-$ftTurnOff"
                        val muteOn = startTime
                        val muteOff = endTime
                        logI("toDP: $fanIntake, $fanExhaust, $lightIntensity, $lightSchedule, $muteOn, $muteOff")

                        fanIntake?.let { it1 -> mViewMode.setFanIntake(it1.toString()) }
                        fanExhaust?.let { it1 -> mViewMode.setFanExhaust(it1.toString()) }
                        lightIntensity?.let { it1 -> mViewMode.setGrowLight(it1.toString()) }
                        binding.ftTimer.text = "$lightSchedule"
                        mViewMode.setmuteOff(muteOff.toString())
                        mViewMode.setmuteOn(muteOn.toString())

                    }
                }
            }
        }

        binding.rlBtn.setSafeOnClickListener {
            finish()
        }

    }

    override fun observe() {
        mViewMode.apply {
            getGrowLight.observe(this@OffLineHardSetActivity) {
                binding.tvFanValues.text = it.toString()
                binding.fanIntakeSeekbars.setProgress(it.toFloat())
            }

            getFanIntake.observe(this@OffLineHardSetActivity) {
                binding.tvFanValue.text = it.toString()
                binding.fanIntakeSeekbar.setProgress(it.toFloat())
            }

            getFanExhaust.observe(this@OffLineHardSetActivity) {
                binding.tvFanExhaustValue.text = it.toString()
                binding.fanIntakeSeekbarsss.setProgress(it.toFloat())
            }
        }
    }

    private var index = 0
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initData() {
        // home_constraintlayouts
        binding.homeConstraintlayouts.setSafeOnClickListener {
            chooserTime()
        }

        // photo
        // 点击事件1：设置index为1
        binding.rlOne.setSafeOnClickListener {
            index = if (index == 1) {
                0
            } else {
                1
            }
            updateBackground(index - 1)  // 由于index从1开始，数组索引从0开始，因此减去1

            val (startTime, endTime) = pair(6, 0)
            setAndSendData(startTime, endTime, "3", "6", "3")
        }
        // 点击事件2：设置index为2
        binding.rlTwo.setSafeOnClickListener {
            index = if (index == 2) {
                0
            } else {
                2
            }
            updateBackground(index - 1)  // 由于index从1开始，数组索引从0开始，因此减去1

            val (startTime, endTime) = pair(6, 18)
            setAndSendData(startTime, endTime, "6", "8", "7")
        }

        binding.rlThree.setSafeOnClickListener {
            index = if (index == 3) {
                0
            } else {
                3
            }
            updateBackground(index - 1)  // 由于index从1开始，数组索引从0开始，因此减去1

            val (startTime, endTime) = pair(6, 18)
            setAndSendData(startTime, endTime, "6", "8", "7")
        }

        binding.rlFour.setSafeOnClickListener {
            index = if (index == 4) {
                0
            } else {
                4
            }
            updateBackground(index - 1)  // 由于index从1开始，数组索引从0开始，因此减去1

            val (startTime, endTime) = pair(0, 0)
            setAndSendData(startTime, endTime, "0", "6", "0")
        }

        binding.rlAutoOne.setSafeOnClickListener {
            index = if (index == 5) {
                0
            } else {
                5
            }
            updateBackground(index - 1)  // 由于index从1开始，数组索引从0开始，因此减去1

            val (startTime, endTime) = pair(6, 0)
            setAndSendData(startTime, endTime, "1", "3", "1")
        }

        binding.rlAutoTwo.setSafeOnClickListener {
            index = if (index == 6) {
                0
            } else {
                6
            }
            updateBackground(index - 1)  // 由于index从1开始，数组索引从0开始，因此减去1

            val (startTime, endTime) = pair(6, 0)
            setAndSendData(startTime, endTime, "3", "6", "3")
        }

        binding.rlAutoThree.setSafeOnClickListener {
            index = if (index == 7) {
                0
            } else {
                7
            }
            updateBackground(index - 1)  // 由于index从1开始，数组索引从0开始，因此减去1

            val (startTime, endTime) = pair(6, 0)
            setAndSendData(startTime, endTime, "6", "8", "7")
        }

        binding.rlAutoFour.setSafeOnClickListener {
            index = if (index == 8) {
                0
            } else {
                8
            }
            updateBackground(index - 1)  // 由于index从1开始，数组索引从0开始，因此减去1

            val (startTime, endTime) = pair(0, 0)
            setAndSendData(startTime, endTime, "0", "6", "0")
        }


        binding.fanIntakeSeekbars.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {

            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                mViewMode.setGrowLight(seekBar?.progress.safeToInt().toString())
                // 发送dp点
                val dpBean = AllDpBean(
                    cmd = "6",  gl = seekBar?.progress.safeToInt().toString()
                )
                GSON.toJsonInBackground(dpBean) { it1 ->
                    DeviceControl.get().success { logI("dp to success") }.error { code, error -> ToastUtil.shortShow(error) }
                        .sendDps(it1)
                }
            }
        }

        // fan_intake_seekbar
        binding.fanIntakeSeekbar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {

            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                mViewMode.setFanIntake(seekBar?.progress.safeToInt().toString())
                // 发送dp点
                val dpBean = AllDpBean(
                    cmd = "6",  `in` = seekBar?.progress.safeToInt().toString()
                )
                GSON.toJsonInBackground(dpBean) { it1 ->
                    DeviceControl.get().success { logI("dp to success") }.error { code, error -> ToastUtil.shortShow(error) }
                        .sendDps(it1)
                }
            }
        }

        binding.fanIntakeSeekbarsss.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {

            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                mViewMode.setFanExhaust(seekBar?.progress.safeToInt().toString())
                // 发送dp点
                val dpBean = AllDpBean(
                    cmd = "6", ex = seekBar?.progress.safeToInt().toString()
                )
                GSON.toJsonInBackground(dpBean) { it1 ->
                    DeviceControl.get().success { logI("dp to success") }.error { code, error -> ToastUtil.shortShow(error) }
                        .sendDps(it1)
                }
            }
        }
    }

    // 提取公共代码：更新背景
    private fun updateBackground(selectedIndex: Int) {
        val defaultBackground = getDrawable(com.cl.common_base.R.drawable.background_button_main_color_r100)
        val selectedBackground = getDrawable(com.cl.common_base.R.drawable.background_button_white_r100)

        val buttons = listOf(binding.rlOne, binding.rlTwo, binding.rlThree, binding.rlFour,
            binding.rlAutoOne, binding.rlAutoTwo, binding.rlAutoThree, binding.rlAutoFour)

        buttons.forEachIndexed { index, view ->
            view.background = if (index == selectedIndex) defaultBackground else selectedBackground
        }

        val viexs = listOf(binding.textView, binding.textView2, binding.textView3, binding.textView4,
            binding.auto1, binding.auto2, binding.auto3, binding.auto4)

        viexs.forEachIndexed { index, view ->
            view.setTextColor(if (index == selectedIndex) Color.WHITE else getColor(com.cl.common_base.R.color.mainColor))
        }
    }

    // 提取公共代码：设置和发送数据
    private fun setAndSendData(startTime: Int, endTime: Int, intake: String, exhaust: String, growLight: String) {
        val (ftTurnOn, ftTurnOff) = pairTwo(startTime, endTime)
        mViewMode.setFanIntake(intake)
        mViewMode.setFanExhaust(exhaust)
        mViewMode.setGrowLight(growLight)
        mViewMode.setmuteOn(startTime.toString())
        mViewMode.setmuteOff(endTime.toString())

        val lightSchedule = "$ftTurnOn-$ftTurnOff"
        binding.ftTimer.text = lightSchedule

        val dpBean = AllDpBean(
            cmd = "6",
            gl = mViewMode.getGrowLight.value.toString(),
            gle = when (endTime) {
                12 -> 0
                24 -> 12
                else -> endTime
            }.toString(),
            gls = when (startTime) {
                12 -> 0
                24 -> 12
                else -> startTime
            }.toString(),
            ex = mViewMode.getFanExhaust.value.toString(),
            `in` = mViewMode.getFanIntake.value.toString()
        )

        GSON.toJsonInBackground(dpBean) { it1 ->
            DeviceControl.get().success { logI("dp to success") }
                .error { code, error -> ToastUtil.shortShow(error) }
                .sendDps(it1)
        }
    }

    private fun pair(openTime:Int, closeTime:Int): Pair<Int, Int> {
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

    private fun pairTwo(startTime:Int, endTime:Int): Pair<String, String> {
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

    private val pop by lazy {
        XPopup.Builder(this@OffLineHardSetActivity)
    }

    private fun chooserTime() {
        pop.asCustom(
            ChooseTimePop(
                this@OffLineHardSetActivity,
                turnOnText = getString(com.cl.common_base.R.string.string_1359),
                turnOffText = getString(com.cl.common_base.R.string.string_1360),
                isShowNightMode = false,
                isTheSpacingHours = false,
                turnOnHour = mViewMode.muteOn?.safeToInt(),
                turnOffHour = mViewMode.muteOff?.safeToInt(),
                isProMode = true,
                lightIntensity = mViewMode.getGrowLight.value.safeToInt(),
                proModeAction = { onTime, offMinute, timeOn, timeOff, timeOpenHour, timeCloseHour, lightIntensity ->
                    mViewMode.setGrowLight(lightIntensity.toString())

                    binding.ftTimer.text = "$onTime-$offMinute"
                    mViewMode.setmuteOn("$timeOn")
                    mViewMode.setmuteOff("$timeOff")

                    // 开灯时间
                    // 调节灯光事件, 需要用到140字段
                    val dpBean = AllDpBean(
                        cmd = "6", gl = lightIntensity.toString(), gle = when (timeOff) {
                            12 -> 0
                            24 -> 12
                            else -> timeOff
                        }.toString(), gls = when (timeOn) {
                            12 -> 0
                            24 -> 12
                            else -> timeOn
                        }.toString(), ex = mViewMode.getFanExhaust.value.toString(), `in` = mViewMode.getFanIntake.value.toString()
                    )
                    GSON.toJsonInBackground(dpBean) { it1 ->
                        DeviceControl.get().success { logI("dp to success") }.error { code, error -> ToastUtil.shortShow(error) }
                            .sendDps(it1)
                    }
                })
        ).show()
    }
}