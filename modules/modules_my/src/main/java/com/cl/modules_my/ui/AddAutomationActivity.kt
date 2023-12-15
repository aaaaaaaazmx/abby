package com.cl.modules_my.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.core.widget.doAfterTextChanged
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToBigDecimal
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseThreeTextPop
import com.cl.common_base.pop.ChooseHumidityPop
import com.cl.common_base.pop.ChooseTemperaturePop
import com.cl.common_base.pop.ChooseTimerPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_my.databinding.MyAddAutomationBinding
import com.cl.modules_my.repository.GetAutomationRuleBean
import com.cl.modules_my.request.ConfiguationExecuteRuleReq
import com.cl.modules_my.viewmodel.AddAutomationViewModel
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnSelectListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * 添加自动化页面
 */
@AndroidEntryPoint
class AddAutomationActivity : BaseActivity<MyAddAutomationBinding>() {

    @Inject
    lateinit var mViewModel: AddAutomationViewModel

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

    /**
     * 排插的端口ID
     */
    private val portId by lazy {
        intent.getStringExtra("portId")
    }

    /**
     * 自动化ID
     */
    private val automationId by lazy {
        intent.getStringExtra(BasePopActivity.KEY_AUTOMATION_ID)
    }

    override fun initView() {
        // 获取自动化信息。
        mViewModel.getAutomationInfo(automationId ?: "", accessoryId)

    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun observe() {
        mViewModel.autoTypeList.observe(this@AddAutomationActivity, resourceObserver {
            error { errorMsg, code ->
                ToastUtil.shortShow(errorMsg)
            }
            success {
                // 获取到自动化列表类型
                runCatching {
                    val list = mViewModel.automationInfo.value?.data?.list
                    if ((list?.size ?: 0) > 0) {
                        val bean = list?.get(0)
                        binding.tvIfType.text = data?.firstOrNull { it.type == bean?.type }?.name
                        logI("12312312: ${data?.firstOrNull { it.type == bean?.type }?.name}")
                        when (bean?.operator) {
                            ">=" -> {
                                binding.tvIfText.text = "≤ ${bean.value}"
                            }

                            "<=" -> {
                                binding.tvIfText.text = "≥ ${bean.value}"
                            }
                        }
                        when (bean?.type) {
                            KEY_ROOM_RH -> {
                                list.firstOrNull { it.type == KEY_ROOM_RH }.apply {
                                    mViewModel.setRoomRhTemperature(if (this == null) "40" else value.toString())
                                    if (this == null) {
                                        binding.tvIfText.text = "≤ 40%"
                                        return@apply
                                    }
                                    binding.tvIfText.text =
                                        "${if (operator == ">=") "≥" else "≤"} $value%"
                                    return@apply
                                }
                            }
                            KEY_ROOM_TEMP -> {
                                //  需要做单位转换
                                list.firstOrNull { it.type == KEY_ROOM_TEMP }.apply {
                                    mViewModel.setRoomTemperature(if (this == null) "70" else value.toString())

                                    kotlin.runCatching {
                                        if (this == null) {
                                            binding.tvIfText.text = if (mViewModel.isMetricSystem) {
                                                // 摄氏度
                                                "≥ ${
                                                    ((70.minus(32)).times(5f).div(9f)).roundToInt()
                                                }°C"
                                            } else {
                                                // 华氏度
                                                "≥  70F"
                                            }
                                            return@apply
                                        }
                                        binding.tvIfText.text = if (mViewModel.isMetricSystem) {
                                            // 摄氏度
                                            "${if (operator == ">=") "≥" else "≤"} ${
                                                ((value?.minus(32))?.times(5f)?.div(9f))?.roundToInt()
                                            }°C"
                                        } else {
                                            // 华氏度
                                            "${if (operator == ">=") "≥" else "≤"} ${value}F"
                                        }
                                    }

                                    return@apply
                                }
                            }
                            KEY_TEMPERATURE -> {
                                //  需要做单位转换
                                list.firstOrNull { it.type == KEY_TEMPERATURE }.apply {
                                    mViewModel.setTemperature(if (this == null) "70" else value.toString())

                                    kotlin.runCatching {
                                        if (this == null) {
                                            binding.tvIfText.text = if (mViewModel.isMetricSystem) {
                                                // 摄氏度
                                                "≥ ${
                                                    ((70.minus(32)).times(5f).div(9f)).roundToInt()
                                                }°C"
                                            } else {
                                                // 华氏度
                                                "≥  70F"
                                            }
                                            return@apply
                                        }
                                        binding.tvIfText.text = if (mViewModel.isMetricSystem) {
                                            // 摄氏度
                                            "${if (operator == ">=") "≥" else "≤"} ${
                                                ((value?.minus(32))?.times(5f)?.div(9f))?.roundToInt()
                                            }°C"
                                        } else {
                                            // 华氏度
                                            "${if (operator == ">=") "≥" else "≤"} ${value}F"
                                        }
                                    }

                                    return@apply
                                }
                            }

                            KEY_HUMIDITY -> {
                                list.firstOrNull { it.type == KEY_HUMIDITY }.apply {
                                    mViewModel.setHumidity(if (this == null) "40" else value.toString())
                                    if (this == null) {
                                        binding.tvIfText.text = "≤ 40%"
                                        return@apply
                                    }
                                    binding.tvIfText.text =
                                        "${if (operator == ">=") "≥" else "≤"} $value%"
                                    return@apply
                                }
                            }

                            KEY_TIMER -> {
                                // 这个时候需要判断当前时间显示
                                list.firstOrNull { it.type == KEY_TIMER }.apply {
                                    mViewModel.setTime(if (this == null) "7" else value.toString())
                                    if (this == null) {
                                        binding.tvIfText.text = "7: 00"
                                        return@apply
                                    }
                                    binding.tvIfText.text = "$value: 00"
                                    return@apply
                                }
                            }
                        }
                    }

                }

            }
        })
        mViewModel.configAutomationRule.observe(this@AddAutomationActivity, resourceObserver {
            error { errorMsg, code ->
                ToastUtil.shortShow(errorMsg)
            }
            success {
                startActivity(
                    Intent(
                        this@AddAutomationActivity,
                        DeviceAutomationActivity::class.java
                    )
                )
            }
        })
        mViewModel.automationInfo.observe(this@AddAutomationActivity, resourceObserver {
            error { errorMsg, code ->
                ToastUtil.shortShow(errorMsg)
            }
            success {
                logI("获取自动化信息成功")
                binding.thenText.text = data?.thenDescribe
                data?.iocn?.let {
                    val requestOptions = RequestOptions()
                    requestOptions.placeholder(R.mipmap.placeholder)
                    requestOptions.error(R.mipmap.errorholder)
                    /*requestOptions.override(
                        Target.SIZE_ORIGINAL,
                        Target.SIZE_ORIGINAL
                    )*/
                    Glide.with(this@AddAutomationActivity).load(it)
                        .apply(requestOptions)
                        .into(binding.ivThenImg)
                }
                binding.etEmail.setText(data?.accessoryName)
                binding.tvThenType.text = if (data?.status == 0) "Turn Off" else "Turn On"

                // 获取自动化列表类型
                deviceId?.let { mViewModel.getAutoType(it) }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        binding.title.setLeftClickListener { finish() }

        binding.clIf.setOnClickListener {
            xpopup(this@AddAutomationActivity) {
                dismissOnTouchOutside(true)
                isDestroyOnDismiss(false)
                asCenterList("", mViewModel.autoTypeList.value?.data?.map { it.name }?.toTypedArray(), OnSelectListener { position, text ->
                    // 选中的是哪个/根据Type来判断
                    val type = mViewModel.autoTypeList.value?.data?.get(position)?.type
                    val name = mViewModel.autoTypeList.value?.data?.get(position)?.name
                    when (type) {
                        KEY_ROOM_RH -> {
                            // 切换之后需要展示默认值。
                            binding.tvIfType.text = name
                            mViewModel.automationInfo.value?.data?.list?.firstOrNull { it.type == type }
                                .apply {
                                    if (this == null) {
                                        binding.tvIfText.text =
                                            "${if (mViewModel.setHumidityType.value == 0) "≥" else "≤"} ${mViewModel.setRoomRhTemperature.value}%"

                                    } else {
                                        binding.tvIfText.text =
                                            "${if (operator == ">=") "≥" else "≤"} $value%"

                                        mViewModel.setRoomRhTemperature("$value")
                                        mViewModel.setHumidityType(if (operator == ">=") 0 else 1)
                                    }
                                    valueCLickPop(type)
                                }
                        }

                        KEY_ROOM_TEMP -> {
                            binding.tvIfType.text = name
                            // 需要做单位转换
                            // 切换之后需要展示默认值。
                            mViewModel.automationInfo.value?.data?.list?.firstOrNull { it.type == type }
                                .apply {
                                    logI("12124124: ${mViewModel.setRoomTemperature.value}")
                                    kotlin.runCatching {
                                        if (this == null) {
                                            binding.tvIfText.text =
                                                if (mViewModel.isMetricSystem) "${if (mViewModel.setTemperatureType.value == 0) "≥" else "≤"} ${
                                                    ((mViewModel.setRoomTemperature.value?.safeToInt()
                                                        ?.minus(32))?.times(5f)
                                                        ?.div(9f))?.roundToInt()
                                                }°C" else "${if (mViewModel.setTemperatureType.value == 0) "≥" else "≤"} ${mViewModel.setRoomTemperature.value}F"
                                        } else {
                                            binding.tvIfText.text = if (mViewModel.isMetricSystem) {
                                                // 摄氏度
                                                "${if (operator == ">=") "≥" else "≤"} ${
                                                    ((value?.minus(32))?.times(5f)
                                                        ?.div(9f))?.roundToInt()
                                                }°C"
                                            } else {
                                                // 华氏度
                                                "${if (operator == ">=") "≥" else "≤"} ${value}F"
                                            }
                                            mViewModel.setRoomTemperature("$value")
                                            mViewModel.setTemperatureType(if (operator == ">=") 0 else 1)
                                        }
                                        valueCLickPop(type)
                                    }
                                }
                        }

                        KEY_TEMPERATURE -> {
                            binding.tvIfType.text = name
                            // 需要做单位转换
                            // 切换之后需要展示默认值。
                            mViewModel.automationInfo.value?.data?.list?.firstOrNull { it.type == type }
                                .apply {
                                    logI("12124124: ${mViewModel.setTemperature.value}")
                                    kotlin.runCatching {
                                        if (this == null) {
                                            binding.tvIfText.text =
                                                if (mViewModel.isMetricSystem) "${if (mViewModel.setTemperatureType.value == 0) "≥" else "≤"} ${
                                                    ((mViewModel.setTemperature.value?.safeToInt()
                                                        ?.minus(32))?.times(5f)
                                                        ?.div(9f))?.roundToInt()
                                                }°C" else "${if (mViewModel.setTemperatureType.value == 0) "≥" else "≤"} ${mViewModel.setTemperature.value}F"
                                        } else {
                                            binding.tvIfText.text = if (mViewModel.isMetricSystem) {
                                                // 摄氏度
                                                "${if (operator == ">=") "≥" else "≤"} ${
                                                    ((value?.minus(32))?.times(5f)
                                                        ?.div(9f))?.roundToInt()
                                                }°C"
                                            } else {
                                                // 华氏度
                                                "${if (operator == ">=") "≥" else "≤"} ${value}F"
                                            }
                                            mViewModel.setTemperature("$value")
                                            mViewModel.setTemperatureType(if (operator == ">=") 0 else 1)
                                        }
                                        valueCLickPop(type)
                                    }
                                }
                        }
                        KEY_HUMIDITY -> {
                            // 切换之后需要展示默认值。
                            binding.tvIfType.text = name
                            mViewModel.automationInfo.value?.data?.list?.firstOrNull { it.type == type }
                                .apply {
                                    if (this == null) {
                                        binding.tvIfText.text =
                                            "${if (mViewModel.setHumidityType.value == 0) "≥" else "≤"} ${mViewModel.setHumidity.value}%"

                                    } else {
                                        binding.tvIfText.text =
                                            "${if (operator == ">=") "≥" else "≤"} $value%"

                                        mViewModel.setHumidity("$value")
                                        mViewModel.setHumidityType(if (operator == ">=") 0 else 1)
                                    }
                                    valueCLickPop(type)
                                }
                        }
                        KEY_TIMER -> {
                            // 切换之后需要展示默认值。
                            binding.tvIfType.text = name
                            mViewModel.automationInfo.value?.data?.list?.firstOrNull { it.type == type }
                                .apply {
                                    if (this == null) {
                                        binding.tvIfText.text = "${mViewModel.setTime.value}:00"
                                    } else {
                                        binding.tvIfText.text = "$value:00"
                                        mViewModel.setTime("$value")
                                    }
                                    valueCLickPop(type)
                                }
                        }
                    }
                }).show()
            }
        }
        binding.clThen.setOnClickListener {
            XPopup.Builder(this@AddAutomationActivity)
                .dismissOnTouchOutside(true)
                .isDestroyOnDismiss(false)
                .asCustom(
                    BaseThreeTextPop(
                        this@AddAutomationActivity,
                        oneLineText = "Turn On",
                        twoLineText = "Turn Off",
                        oneLineCLickEventAction = {
                            binding.tvThenType.text = "Turn On"
                        },
                        twoLineCLickEventAction = {
                            binding.tvThenType.text = "Turn Off"
                        },
                    )
                ).show()
        }
        binding.tvIfText.setOnClickListener {
            // 判断是大于还是小雨
            // 需要根据当前的文案来选择
            // 由于是写死的。所以这里就不做判断了。
            mViewModel.autoTypeList.value?.data?.firstOrNull { it.name == binding.tvIfType.text.toString() }?.type?.let {
                valueCLickPop(it)
            }
        }
        binding.thenText.setOnClickListener { }

        // 确认点击时间
        binding.btnSuccess.setOnClickListener {
            kotlin.runCatching {
                val automationName = binding.etEmail.text.toString()
                val type = mViewModel.autoTypeList.value?.data?.firstOrNull { it.name == binding.tvIfType.text.toString() }?.type
                val req = ConfiguationExecuteRuleReq(
                    accessoryId = accessoryId,
                    accessoryName = automationName,
                    automationId = automationId,
                    deviceId = deviceId,
                    portId = portId,
                    status = if (binding.tvThenType.text == "Turn On") 1 else 0,
                    list =
                    mutableListOf(
                        GetAutomationRuleBean.AutomationRuleListBean(
                            operator = if (type == KEY_TEMPERATURE || type == KEY_ROOM_TEMP) if (mViewModel.setTemperatureType.value == 0) ">=" else "<=" else if (mViewModel.setHumidityType.value == 0) ">=" else "<=",
                            type = type,
                            value = when (type) {
                                KEY_TEMPERATURE -> mViewModel.setTemperature.value?.safeToInt()
                                KEY_HUMIDITY -> mViewModel.setHumidity.value?.safeToInt()
                                KEY_TIMER -> mViewModel.setTime.value?.safeToInt()
                                KEY_ROOM_RH -> mViewModel.setRoomRhTemperature.value?.safeToInt()
                                KEY_ROOM_TEMP -> mViewModel.setRoomTemperature.value?.safeToInt()
                                else -> mViewModel.setTemperature.value?.safeToInt()
                            }
                        )
                    )
                )
                mViewModel.getConfigAutomationRule(req)
            }
        }
        binding.ivClearEmail.setOnClickListener {
            binding.etEmail.setText("")
        }
    }

    private fun valueCLickPop(type: String) {
        when (type) {
            KEY_ROOM_TEMP -> {
                XPopup.Builder(this@AddAutomationActivity)
                    .isDestroyOnDismiss(false)
                    .dismissOnTouchOutside(false)
                    .asCustom(
                        ChooseTemperaturePop(
                            this@AddAutomationActivity,
                            scope = mViewModel.setTemperatureType.value ?: 0,
                            value = mViewModel.setRoomTemperature.value ?: "70",
                            onConfirmAction = { scope, value ->
                                logI("1231231231: $scope, $value")
                                mViewModel.setTemperatureType(scope)
                                mViewModel.setRoomTemperature(value)
                                kotlin.runCatching {
                                    when (scope) {
                                        0 -> {
                                            binding.tvIfText.text =
                                                if (mViewModel.isMetricSystem) {
                                                    // 摄氏度
                                                    "≥ ${
                                                        ((value.safeToInt().minus(32)).times(5f)
                                                            .div(9f)).roundToInt()
                                                    }°C"
                                                } else {
                                                    // 华氏度
                                                    "≥ ${value}F"
                                                }
                                        }

                                        1 -> {
                                            binding.tvIfText.text =
                                                if (mViewModel.isMetricSystem) {
                                                    // 摄氏度
                                                    "≤ ${
                                                        ((value.safeToInt().minus(32)).times(5f)
                                                            .div(9f)).roundToInt()
                                                    }°C"
                                                } else {
                                                    // 华氏度
                                                    "≤ ${value}F"
                                                }
                                        }
                                    }
                                }
                            },
                            onCancelAction = {})
                    ).show()
            }
            KEY_ROOM_RH -> {
                XPopup.Builder(this@AddAutomationActivity)
                    .isDestroyOnDismiss(false)
                    .dismissOnTouchOutside(false)
                    .asCustom(
                        ChooseHumidityPop(
                            this@AddAutomationActivity,
                            scope = mViewModel.setHumidityType.value ?: 0,
                            value = mViewModel.setRoomRhTemperature.value ?: "40",
                            onConfirmAction = { scope, value ->
                                mViewModel.setHumidityType(scope)
                                mViewModel.setRoomRhTemperature(value)
                                when (scope) {
                                    0 -> {
                                        binding.tvIfText.text = "≥ $value%"
                                    }

                                    1 -> {
                                        binding.tvIfText.text = "≤ $value%"
                                    }
                                }
                            },
                            onCancelAction = {})
                    ).show()
            }
            KEY_TEMPERATURE -> {
                XPopup.Builder(this@AddAutomationActivity)
                    .isDestroyOnDismiss(false)
                    .dismissOnTouchOutside(false)
                    .asCustom(
                        ChooseTemperaturePop(
                            this@AddAutomationActivity,
                            scope = mViewModel.setTemperatureType.value ?: 0,
                            value = mViewModel.setTemperature.value ?: "70",
                            onConfirmAction = { scope, value ->
                                logI("1231231231: $scope, $value")
                                mViewModel.setTemperatureType(scope)
                                mViewModel.setTemperature(value)
                                kotlin.runCatching {
                                    when (scope) {
                                        0 -> {
                                            binding.tvIfText.text =
                                                if (mViewModel.isMetricSystem) {
                                                    // 摄氏度
                                                    "≥ ${
                                                        ((value.safeToInt().minus(32)).times(5f)
                                                            .div(9f)).roundToInt()
                                                    }°C"
                                                } else {
                                                    // 华氏度
                                                    "≥ ${value}F"
                                                }
                                        }

                                        1 -> {
                                            binding.tvIfText.text =
                                                if (mViewModel.isMetricSystem) {
                                                    // 摄氏度
                                                    "≤ ${
                                                        ((value.safeToInt().minus(32)).times(5f)
                                                            .div(9f)).roundToInt()
                                                    }°C"
                                                } else {
                                                    // 华氏度
                                                    "≤ ${value}F"
                                                }
                                        }
                                    }
                                }
                            },
                            onCancelAction = {})
                    ).show()
            }

            KEY_HUMIDITY -> {
                XPopup.Builder(this@AddAutomationActivity)
                    .isDestroyOnDismiss(false)
                    .dismissOnTouchOutside(false)
                    .asCustom(
                        ChooseHumidityPop(
                            this@AddAutomationActivity,
                            scope = mViewModel.setHumidityType.value ?: 0,
                            value = mViewModel.setHumidity.value ?: "40",
                            onConfirmAction = { scope, value ->
                                mViewModel.setHumidityType(scope)
                                mViewModel.setHumidity(value)
                                when (scope) {
                                    0 -> {
                                        binding.tvIfText.text = "≥ $value%"
                                    }

                                    1 -> {
                                        binding.tvIfText.text = "≤ $value%"
                                    }
                                }
                            },
                            onCancelAction = {})
                    ).show()
            }

            KEY_TIMER -> {
                XPopup.Builder(this@AddAutomationActivity)
                    .dismissOnTouchOutside(false)
                    .isDestroyOnDismiss(false)
                    .asCustom(
                        ChooseTimerPop(
                            this@AddAutomationActivity,
                            time = mViewModel.setTime.value?.safeToInt() ?: 7,
                            onConfirmAction = {
                                binding.tvIfText.text = "$it:00"
                                mViewModel.setTime("$it")
                            },
                            onCancelAction = {}
                        )).show()
            }
        }
    }

    companion object {
        //Timer
        const val KEY_TIMER = "Timer"
        // Humidity
        const val KEY_HUMIDITY = "Humidity"
        // Temperature
        const val KEY_TEMPERATURE = "Temperature"
        // Room_RH
        const val KEY_ROOM_RH = "Room_RH"
        // Room_Temp
        const val KEY_ROOM_TEMP = "Room_Temp"
    }
}