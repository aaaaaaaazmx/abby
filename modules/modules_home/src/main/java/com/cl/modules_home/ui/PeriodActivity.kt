package com.cl.modules_home.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import com.cl.modules_home.databinding.HomePeriodChartActivityBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.safeToLong
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.temperatureConversionTwo
import com.cl.common_base.pop.PlantIdListPop
import com.cl.common_base.util.Prefs
import com.cl.common_base.widget.toast.ToastUtil
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartLineDashStyleType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartZoomType
import com.github.aachartmodel.aainfographics.aachartcreator.AAOptions
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aachartcreator.aa_toAAOptions
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AACrosshair
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AALabels
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAMarker
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAPlotOptions
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAScrollablePlotArea
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AASeries
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AATitle
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AATooltip
import com.github.aachartmodel.aainfographics.aatools.AAColor
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PeriodActivity : BaseActivity<HomePeriodChartActivityBinding>() {

    @Inject
    lateinit var mviewmodel: HomeChartViewModel

    private val aBoolean by lazy {
        Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false);
    }

    private val unit by lazy {
        // false 摄氏度， true 华氏度
        val unit = if (aBoolean) "°C" else "°F"
        unit
    }
    private var lastX = 0f
    private var lastY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        // 植物信息
        mviewmodel.plantInfo()

        binding.cs.setSafeOnClickListener {
            // 获取到所有的植物ID
            mviewmodel.getPlantIdByDeviceId(mviewmodel.userInfo?.deviceId ?: "")
        }

        binding.chart1.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    // 默认不允许ScrollView拦截触摸事件
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = Math.abs(event.x - lastX)
                    val deltaY = Math.abs(event.y - lastY)
                    // 只有当水平滑动距离大于垂直滑动距离时，ScrollView不应拦截事件
                    v.parent.requestDisallowInterceptTouchEvent(deltaX > deltaY)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 重置拦截设置，允许ScrollView正常处理事件
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false // 返回false让事件能继续传递给图表处理
        }

        binding.chart2.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    // 默认不允许ScrollView拦截触摸事件
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = Math.abs(event.x - lastX)
                    val deltaY = Math.abs(event.y - lastY)
                    // 只有当水平滑动距离大于垂直滑动距离时，ScrollView不应拦截事件
                    v.parent.requestDisallowInterceptTouchEvent(deltaX > deltaY)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 重置拦截设置，允许ScrollView正常处理事件
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false // 返回false让事件能继续传递给图表处理
        }
    }

    override fun observe() {
        mviewmodel.apply {
            plantInfo.observe(this@PeriodActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.show(errorMsg) }
                success {
                    if (null == data) return@success
                    // 设置plantId，然后请求当前的数据
                    setPlantIds(data?.plantId.toString())
                    data?.plantId?.let { getPlantData(it.toString()) }
                }
            })

            getPlantData.observe(this@PeriodActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 默认显示湿度
                    // EnhancedChartUtil().setupEnhancedLineChart(binding.chart1, data?.humidityList, "humidity", "Grow Chamber Humidity")
                    val dataTime = mviewmodel.getPlantData.value?.data?.humidityList?.map { it.dateTime.safeToLong().toDateString() }?.toList()
                    val yValue = mviewmodel.getPlantData.value?.data?.humidityList?.map { it.codeValue.safeToFloat() }?.toList()
                    val setupChart = ChartUtils.setupChart(dataTime, yValue, "Grow Chamber Humidity %", "#70D9FF")
                    binding.chart1.aa_drawChartWithChartOptions(setupChart)

                    // ph
                    // EnhancedChartUtil().setupEnhancedLineChart(binding.chart2, data?.phList, "ph", "PH")

                    val dataTime1 = mviewmodel.getPlantData.value?.data?.phList?.map { it.dateTime.safeToLong().toDateStringPh() }?.toList()
                    val yValue1 = mviewmodel.getPlantData.value?.data?.phList?.map { it.codeValue.safeToFloat() }?.toList()
                    binding.chart2.aa_drawChartWithChartOptions(ChartUtils.setupChart(dataTime1, yValue1, "ph", "#4CD964"))


                    binding.tvPlantName.text = data?.plantName
                    binding.tvPeriodName.text = data?.period
                }
            })

            getPlantIdByDeviceId.observe(this@PeriodActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.show(errorMsg) }
                success {
                    // 获取3所有的植物ID
                    if (data.isNullOrEmpty()) return@success

                    XPopup.Builder(this@PeriodActivity).popupPosition(PopupPosition.Bottom).dismissOnTouchOutside(true).isClickThrough(false)  //点击透传
                        .hasShadowBg(true) // 去掉半透明背景
                        .offsetX(XPopupUtils.dp2px(this@PeriodActivity, -5f))
                        .offsetY(XPopupUtils.dp2px(this@PeriodActivity, 10f))
                        .atView(binding.tvPlantName).asCustom(this@PeriodActivity.let {
                            PlantIdListPop(it, plantId.value?.safeToInt(), data, onConfirmAction = { plantId ->
                                // 设置植物ID
                                setPlantIds(plantId)
                                // 根据plantId获取植物信息
                                getPlantData(plantId)
                            }).setBubbleBgColor(Color.WHITE) //气泡背景
                                .setArrowWidth(XPopupUtils.dp2px(this@PeriodActivity, 6f)).setArrowHeight(
                                    XPopupUtils.dp2px(
                                        this@PeriodActivity, 6f
                                    )
                                ) //.setBubbleRadius(100)
                                .setArrowRadius(
                                    XPopupUtils.dp2px(
                                        this@PeriodActivity, 1f
                                    )
                                )
                        }).show()
                }
            })
        }
    }

    private fun Long.toDateString(): String {
        val sdf = java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(this * 1000))
    }

    private fun Long.toDateStringPh(): String {
        val sdf = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(this * 1000))
    }

    override fun initData() {
        binding.cbHumidity.setOnClickListener {
            val isChecked = binding.cbHumidity.isChecked
            binding.cbHumidity.isChecked = isChecked
            binding.cbTemperature.isChecked = !isChecked
            if (isChecked) {
                binding.cbHumidity.setTextColor(Color.WHITE)
                binding.cbTemperature.setTextColor(Color.parseColor("#006241"))
                val dataTime = mviewmodel.getPlantData.value?.data?.humidityList?.map { it.dateTime.safeToLong().toDateString() }?.toList()
                val yValue = mviewmodel.getPlantData.value?.data?.humidityList?.map { it.codeValue.safeToFloat() }?.toList()
                binding.chart1.aa_drawChartWithChartOptions(ChartUtils.setupChart(dataTime, yValue, "Grow Chamber Humidity %", "#70D9FF"))
            } else {
                binding.cbTemperature.setTextColor(Color.WHITE)
                binding.cbHumidity.setTextColor(Color.parseColor("#006241"))
                // 解析文案。
                val dataTime = mviewmodel.getPlantData.value?.data?.termpertureList?.map { it.dateTime.safeToLong().toDateString() }?.toList()
                val yValue = mviewmodel.getPlantData.value?.data?.termpertureList?.map { temperatureConversionTwo(it.codeValue.safeToFloat(), aBoolean).safeToFloat() }?.toList()
                binding.chart1.aa_drawChartWithChartOptions(ChartUtils.setupChart(dataTime, yValue, "Grow Chamber Temperature $unit", "#006241"))
            }
        }
        binding.cbTemperature.setOnClickListener {
            val isChecked = binding.cbTemperature.isChecked
            binding.cbTemperature.isChecked = isChecked
            binding.cbHumidity.isChecked = !isChecked
            if (isChecked) {
                binding.cbTemperature.setTextColor(Color.WHITE)
                binding.cbHumidity.setTextColor(Color.parseColor("#006241"))
                // 解析文案。
                val dataTime = mviewmodel.getPlantData.value?.data?.termpertureList?.map { it.dateTime.safeToLong().toDateString() }?.toList()
                val yValue = mviewmodel.getPlantData.value?.data?.termpertureList?.map { temperatureConversionTwo(it.codeValue.safeToFloat(), aBoolean).safeToFloat() }?.toList()
                binding.chart1.aa_drawChartWithChartOptions(ChartUtils.setupChart(dataTime, yValue, "Grow Chamber Temperature $unit", "#006241"))
            } else {
                binding.cbHumidity.setTextColor(Color.WHITE)
                binding.cbTemperature.setTextColor(Color.parseColor("#006241"))

                val dataTime = mviewmodel.getPlantData.value?.data?.humidityList?.map { it.dateTime.safeToLong().toDateString() }?.toList()
                val yValue = mviewmodel.getPlantData.value?.data?.humidityList?.map { it.codeValue.safeToFloat()}?.toList()
                binding.chart1.aa_drawChartWithChartOptions(ChartUtils.setupChart(dataTime, yValue, "Grow Chamber Humidity %", "#70D9FF"))
            }
        }
    }

    object ChartUtils {
        fun setupChart(categories: List<String>?, data: List<Float>?, seriesName: String, color: String): AAOptions {
            val aaChartModel = AAChartModel()
                .chartType(AAChartType.Spline)
                .categories(categories?.toTypedArray() ?: arrayOf())
                .zoomType(AAChartZoomType.X)
                .scrollablePlotArea(
                    AAScrollablePlotArea()
                        .minWidth(400)
                        .scrollPositionX(1f)
                )

            val aaPlotOptions = AAPlotOptions()
                .series(
                    AASeries()
                        .marker(
                            AAMarker()
                            .radius(4f))
                )

            val aaOptions = aaChartModel.aa_toAAOptions()

            // 假设图表库支持在设置中直接使用 JavaScript 函数字符串
            /*val jsFormatter = """
        function() {
            return this.value.replace(' ', '<br/>');
        }
    """.trimIndent()

            aaOptions.xAxis?.labels = AALabels()
                .enabled(true)
                .rotation(0f)
                .formatter(jsFormatter)*/  // 这里设置 JavaScript 函数字符串

            aaOptions.plotOptions = aaPlotOptions
            aaOptions.yAxis?.apply {
                title(AATitle().text(""))
                labels(AALabels().enabled(true))
            }
            aaOptions.tooltip = AATooltip()
                .enabled(true)
                .shared(true)
            aaOptions.xAxis?.crosshair = AACrosshair()
                .color(AAColor.Gray)
                .width(1f)
                .dashStyle(AAChartLineDashStyleType.LongDashDotDot)
            aaOptions.yAxis?.crosshair = AACrosshair()
                .color(AAColor.Gray)
                .width(1f)
                .dashStyle(AAChartLineDashStyleType.LongDashDotDot)

            // aaOptions.chart?.pinchType(AAChartZoomType.None)

            aaOptions.series(arrayOf(
                AASeriesElement()
                    .name(seriesName)
                    .data(data?.toTypedArray() ?: arrayOf())
                    .color(color)
            ))

            return aaOptions
        }
    }
}
