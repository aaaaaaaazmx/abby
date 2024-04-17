package com.cl.modules_home.ui

import android.graphics.Color
import com.bbgo.module_home.databinding.HomePeriodChartActivityBinding
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.pop.PlantIdListPop
import com.cl.common_base.util.chat.EnhancedChartUtil
import com.cl.common_base.widget.toast.ToastUtil
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.thingclips.smart.camera.middleware.p2p.ThingSmartCameraP2P
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PeriodActivity : BaseActivity<HomePeriodChartActivityBinding>() {

    @Inject
    lateinit var mviewmodel: HomeChartViewModel

    override fun initView() {
        // 植物信息
        mviewmodel.plantInfo()


        binding.chart1.setOnChartValueSelectedListener(MyChartValueSelectedListener(binding.chart1))
        binding.chart2.setOnChartValueSelectedListener(MyChartValueSelectedListener(binding.chart2))

        binding.flGetPlantList.setSafeOnClickListener {
            // 获取到所有的植物ID
            mviewmodel.getPlantIdByDeviceId(mviewmodel.userInfo?.deviceId ?: "")
        }

        binding.tvPlantName.setSafeOnClickListener {
            // 获取到所有的植物ID
            mviewmodel.getPlantIdByDeviceId(mviewmodel.userInfo?.deviceId ?: "")
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
                     EnhancedChartUtil().setupEnhancedLineChart(binding.chart1, data?.phList, "humidity", "Grow Chamber Humidity")
                    // ph
                    EnhancedChartUtil().setupEnhancedLineChart(binding.chart2, data?.phList, "ph", "PH")

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
                        .atView(binding.ivGetPlantList).asCustom(this@PeriodActivity?.let {
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

    override fun initData() {
        binding.cbHumidity.setOnClickListener {
            val isChecked = binding.cbHumidity.isChecked
            binding.cbHumidity.isChecked = isChecked
            binding.cbTemperature.isChecked = !isChecked

            if (isChecked) {
                binding.cbHumidity.setTextColor(Color.WHITE)
                binding.cbTemperature.setTextColor(Color.parseColor("#006241"))
                EnhancedChartUtil().setupEnhancedLineChart(binding.chart1, mviewmodel.getPlantData.value?.data?.humidityList, "humidity", "Grow Chamber Humidity")
            } else {
                binding.cbTemperature.setTextColor(Color.WHITE)
                binding.cbHumidity.setTextColor(Color.parseColor("#006241"))
                EnhancedChartUtil().setupEnhancedLineChart(binding.chart1, mviewmodel.getPlantData.value?.data?.termpertureList, "temp", "Grow Chamber Temperture")
            }
        }
        binding.cbTemperature.setOnClickListener {
            val isChecked = binding.cbTemperature.isChecked
            binding.cbTemperature.isChecked = isChecked
            binding.cbHumidity.isChecked = !isChecked

            if (isChecked) {
                binding.cbTemperature.setTextColor(Color.WHITE)
                binding.cbHumidity.setTextColor(Color.parseColor("#006241"))
                EnhancedChartUtil().setupEnhancedLineChart(binding.chart1, mviewmodel.getPlantData.value?.data?.termpertureList, "temp", "Grow Chamber Temperture")
            } else {
                binding.cbHumidity.setTextColor(Color.WHITE)
                binding.cbTemperature.setTextColor(Color.parseColor("#006241"))
                EnhancedChartUtil().setupEnhancedLineChart(binding.chart1, mviewmodel.getPlantData.value?.data?.humidityList, "humidity", "Grow Chamber Humidity")
            }
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        binding.chart1.setOnChartValueSelectedListener(null)
        binding.chart2.setOnChartValueSelectedListener(null)
    }

    class MyChartValueSelectedListener(private val chart: LineChart) : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry, h: Highlight) {
            // 根据传入的chart实例处理选中事件
            chart.centerViewToAnimated(
                e.x, e.y, chart.data.getDataSetByIndex(h.dataSetIndex)
                    .axisDependency, 500
            )
        }

        override fun onNothingSelected() {
            // 处理没有选中任何值的情况
        }
    }
}
