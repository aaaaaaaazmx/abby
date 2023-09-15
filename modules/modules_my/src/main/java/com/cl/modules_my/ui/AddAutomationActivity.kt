package com.cl.modules_my.ui

import android.content.Intent
import androidx.core.widget.doAfterTextChanged
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
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
     * 自动化ID
     */
    private val automationId by lazy {
        intent.getStringExtra(BasePopActivity.KEY_AUTOMATION_ID)
    }

    override fun initView() {
        // 获取自动化信息。
        mViewModel.getAutomationInfo(automationId ?: "", accessoryId)
    }

    override fun observe() {
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
                if ((data?.list?.size ?: 0) > 0) {
                    val bean = data?.list?.get(0)
                    binding.tvIfType.text = bean?.type
                    when (bean?.operator) {
                        ">=" -> {
                            binding.tvIfText.text = "≤ ${bean.value}"
                        }
                        "<=" -> {
                            binding.tvIfText.text = "≥ ${bean.value}"
                        }
                    }
                    when (bean?.type) {
                        "Temperature" -> {
                            //  需要做单位转换
                            data?.list?.firstOrNull { it.type == "Temperature" }.apply {
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
                        "Humidity" -> {
                            data?.list?.firstOrNull { it.type == "Humidity" }.apply {
                                mViewModel.setTemperature(if (this == null) "40" else value.toString())
                                if (this == null) {
                                    binding.tvIfText.text = "≤ 40%"
                                    return@apply
                                }
                                binding.tvIfText.text =
                                    "${if (operator == ">=") "≥" else "≤"} $value%"
                                return@apply
                            }
                        }
                        "Timer" -> {
                            // 这个时候需要判断当前时间显示
                            data?.list?.firstOrNull { it.type == "Timer" }.apply {
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
        })
    }

    override fun initData() {
        binding.title.setLeftClickListener { finish() }

        binding.clIf.setOnClickListener {
            XPopup.Builder(this@AddAutomationActivity)
                .dismissOnTouchOutside(true)
                .isDestroyOnDismiss(false)
                .asCustom(
                    BaseThreeTextPop(
                        this@AddAutomationActivity,
                        oneLineText = "Temperature",
                        twoLineText = "Humidity",
                        threeLineText = "Timer",
                        oneLineCLickEventAction = {
                            binding.tvIfType.text = "Temperature"
                            // 需要做单位转换
                            // 切换之后需要展示默认值。
                            mViewModel.automationInfo.value?.data?.list?.firstOrNull { it.type == "Temperature" }
                                .apply {
                                    kotlin.runCatching {
                                        if (this == null) {
                                            binding.tvIfText.text =
                                                if (mViewModel.isMetricSystem) "${if (mViewModel.setTemperatureType.value == 0) "≥" else "≤"} ${
                                                    ((mViewModel.setTemperature.value?.toInt()
                                                        ?.minus(32))?.times(5f)
                                                        ?.div(9f))?.roundToInt()
                                                }°C" else "${if (mViewModel.setTemperatureType.value == 0) "≥" else "≤"} 70F"
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
                                        valueCLickPop()
                                    }
                                }
                        },
                        twoLineCLickEventAction = {
                            // 切换之后需要展示默认值。
                            binding.tvIfType.text = "Humidity"
                            mViewModel.automationInfo.value?.data?.list?.firstOrNull { it.type == "Humidity" }
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
                                    valueCLickPop()
                                }
                        },
                        threeLineCLickEventAction = {
                            // 切换之后需要展示默认值。
                            binding.tvIfType.text = "Timer"
                            mViewModel.automationInfo.value?.data?.list?.firstOrNull { it.type == "Timer" }
                                .apply {
                                    if (this == null) {
                                        binding.tvIfText.text = "${mViewModel.setTime.value}:00"
                                    } else {
                                        binding.tvIfText.text = "$value:00"
                                        mViewModel.setTime("$value")
                                    }
                                    valueCLickPop()
                                }
                        }
                    )
                ).show()
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
            valueCLickPop()
        }
        binding.thenText.setOnClickListener { }

        // 确认点击时间
        binding.btnSuccess.setOnClickListener {
            kotlin.runCatching {
                val automationName = binding.etEmail.text.toString()
                val req = ConfiguationExecuteRuleReq(
                    accessoryId = accessoryId,
                    accessoryName = automationName,
                    automationId = automationId,
                    deviceId = deviceId,
                    status = if (binding.tvThenType.text == "Turn On") 1 else 0,
                    list =
                    mutableListOf(
                        GetAutomationRuleBean.AutomationRuleListBean(
                            operator = if (binding.tvIfType.text.toString() == "Temperature") if (mViewModel.setTemperatureType.value == 0) ">=" else "<=" else if (mViewModel.setHumidityType.value == 0) ">=" else "<=",
                            type = binding.tvIfType.text.toString(),
                            value = when (binding.tvIfType.text.toString()) {
                                "Temperature" -> mViewModel.setTemperature.value?.toInt()
                                "Humidity" -> mViewModel.setHumidity.value?.toInt()
                                else -> mViewModel.setTime.value?.toInt()
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

    private fun valueCLickPop() {
        when (binding.tvIfType.text) {
            "Temperature" -> {
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
                                                        ((value.toInt().minus(32)).times(5f)
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
                                                        ((value.toInt().minus(32)).times(5f)
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
            "Humidity" -> {
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
            "Timer" -> {
                XPopup.Builder(this@AddAutomationActivity)
                    .dismissOnTouchOutside(false)
                    .isDestroyOnDismiss(false)
                    .asCustom(
                        ChooseTimerPop(
                            this@AddAutomationActivity,
                            time = mViewModel.setTime.value?.toInt() ?: 7,
                            onConfirmAction = {
                                binding.tvIfText.text = "$it:00"
                                mViewModel.setTime("$it")
                            },
                            onCancelAction = {}
                        )).show()
            }
        }
    }
}