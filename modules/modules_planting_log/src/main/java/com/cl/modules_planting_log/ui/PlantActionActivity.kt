package com.cl.modules_planting_log.ui

import android.graphics.Color
import android.widget.EditText
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.gallonConversion
import com.cl.common_base.ext.letMultiple
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.temperatureConversion
import com.cl.common_base.ext.unitsConversion
import com.cl.common_base.ext.weightConversion
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_planting_log.adapter.CustomViewGroupAdapter
import com.cl.modules_planting_log.adapter.EditTextValueChangeListener
import com.cl.modules_planting_log.databinding.PlantingActionActivityBinding
import com.cl.modules_planting_log.request.CardInfo
import com.cl.modules_planting_log.request.FieldAttributes
import com.cl.modules_planting_log.request.LogSaveOrUpdateReq
import com.cl.modules_planting_log.request.LogTypeListDataItem
import com.cl.modules_planting_log.request.PlantInfoByPlantIdData
import com.cl.modules_planting_log.request.PlantLogTypeBean
import com.cl.modules_planting_log.viewmodel.PlantingLogAcViewModel
import com.cl.modules_planting_log.widget.CustomViewGroup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 种植活动记录
 */
@AndroidEntryPoint
class PlantActionActivity : BaseActivity<PlantingActionActivityBinding>(), EditTextValueChangeListener {

    @Inject
    lateinit var viewModel: PlantingLogAcViewModel

    private val plantInfoData by lazy {
        intent.getSerializableExtra("plantInfoData") as? PlantInfoByPlantIdData
    }

    // 植物ID， 用于新增日志
    private val plantId by lazy {
        intent.getStringExtra("plantId")
    }

    // logId 用于修改和查询日志
    private val logId by lazy {
        intent.getStringExtra("logId")
    }

    // period 用户新增时传递的周期参数
    private val period by lazy {
        intent.getStringExtra("period")
    }

    // showType,用于请求日志类型列表w
    private val showType by lazy {
        intent.getStringExtra("showType") ?: CardInfo.TYPE_ACTION_CARD
    }

    // 是否是新增的
    private val isAdd by lazy {
        intent.getBooleanExtra("isAdd", true)
    }

    override fun PlantingActionActivityBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@PlantActionActivity
            plantInfoData = this@PlantActionActivity.plantInfoData
            executePendingBindings()
        }
    }


    // 属性数组
    private val maps by lazy {
        mapOf(
            "logTime" to FieldAttributes("Date*", "", "", CustomViewGroup.TYPE_CLASS_TEXT),
            "logType" to FieldAttributes("Action Type", "", "", CustomViewGroup.TYPE_CLASS_TEXT),
            "waterType" to FieldAttributes("Water Type", "", "", CustomViewGroup.TYPE_CLASS_TEXT, false),
            "volume" to FieldAttributes("Volume", "", "", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL, false, "L", "Gal"),
            "feedingType" to FieldAttributes("Feeding Type", "", "", CustomViewGroup.TYPE_CLASS_TEXT, false),
            "repellentType" to FieldAttributes("Repellent Type", "", "", CustomViewGroup.TYPE_CLASS_TEXT, false),
            "declareDeathType" to FieldAttributes("DeclareDeath Type", "", "", CustomViewGroup.TYPE_CLASS_TEXT, false),
            "driedWeight" to FieldAttributes("Yield (Dried weight)", "", "", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL, false, "g", "Oz"),
            "wetWeight" to FieldAttributes("Yield (Wet weight)", "", "", CustomViewGroup.TYPE_NUMBER_FLAG_DECIMAL, false, "g", "Oz"),
        )
    }

    /**
     * 日志适配器
     */
    private val logAdapter by lazy {
        CustomViewGroupAdapter(
            this@PlantActionActivity,
            listOf(
                "logTime", "logType", "waterType", "volume", "feedingType", "repellentType", "declareDeathType", "driedWeight", "wetWeight"
            ),
            listOf(
                "logTime", "logType", "waterType", "feedingType", "repellentType", "declareDeathType"
            ),
            maps,
            this@PlantActionActivity
        )
    }


    override fun initView() {
        binding.title
            .setRightButtonTextBack(R.drawable.background_check_tags_r5)
            .setRightButtonText("Save")
            .setRightButtonTextSize(13f)
            .setRightButtonTextHeight(25f)
            .setRightButtonTextColor(Color.WHITE)
            .setRightClickListener { handleSaveOrUpdateLog() }


        // 请求日志类型列表
        viewModel.getLogTypeList(showType, logId)

        binding.rvAction.adapter = logAdapter
    }

    /**
     * 修改或者保存Action
     */
    private fun handleSaveOrUpdateLog() {
        val logSaveOrUpdateReq = logAdapter.getLogData()
        if (logSaveOrUpdateReq.logType.isNullOrEmpty()) {
            ToastUtil.shortShow("Please select the Action type")
            return
        }
        logSaveOrUpdateReq.period = period
        logSaveOrUpdateReq.notes = binding.etNote.text.toString()
        updateUnit(logSaveOrUpdateReq, viewModel.isMetric, true)
        if (logId.isNullOrEmpty()) {
            createNewLog(logSaveOrUpdateReq)
        } else {
            modifyExistingLog(logSaveOrUpdateReq)
        }
    }

    private fun modifyExistingLog(logSaveOrUpdateReq: LogSaveOrUpdateReq) {
        logI("修改日志")
        logSaveOrUpdateReq.logId = logId
        // Insert code for modifying an existing log entry
        viewModel.saveOrUpdateLog(logSaveOrUpdateReq)
        logI("Log Details: $logSaveOrUpdateReq")
    }

    private fun createNewLog(logSaveOrUpdateReq: LogSaveOrUpdateReq) {
        logI("新增日志")
        logSaveOrUpdateReq.plantId = plantId
        //  Insert code for creating a new log entry
        viewModel.saveOrUpdateLog(logSaveOrUpdateReq)
        logI("Log Details: $logSaveOrUpdateReq")
    }

    private fun updateUnit(logSaveOrUpdateReq: LogSaveOrUpdateReq, isMetric: Boolean, isUpload: Boolean) {
        logSaveOrUpdateReq.logTime = if (isUpload) logSaveOrUpdateReq.logTime else DateHelper.formatTime(logSaveOrUpdateReq.logTime?.toLongOrNull() ?: System.currentTimeMillis(), CustomViewGroupAdapter.KEY_FORMAT_TIME)
        /*logSaveOrUpdateReq.driedWeight = weightConversion((logSaveOrUpdateReq.driedWeight?.toFloatOrNull() ?: 0f), isMetric, isUpload)
        logSaveOrUpdateReq.wetWeight = weightConversion((logSaveOrUpdateReq.wetWeight?.toFloatOrNull() ?: 0f), isMetric, isUpload)
        logSaveOrUpdateReq.volume = gallonConversion((logSaveOrUpdateReq.volume?.toFloatOrNull() ?: 0f), isMetric, isUpload)*/
        logSaveOrUpdateReq.logType =
            if (isUpload) viewModel.getLogTypeList.value?.data?.toList()?.firstOrNull { it.showUiText == logSaveOrUpdateReq.logType }?.logType ?: "" else viewModel.getLogTypeList.value?.data?.toList()?.firstOrNull { it.logType == logSaveOrUpdateReq.logType }?.showUiText ?: ""
    }

    override fun observe() {
        viewModel.apply {
            logSaveOrUpdate.observe(this@PlantActionActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 提交成功 or 修改成功
                    finish()
                }
            })

            getLogById.observe(this@PlantActionActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 获取日志详情信息
                    if (null == data) return@success
                    data?.let {
                        updateUnit(it, viewModel.isMetric, false)
                        // 展示和隐藏条目
                        // 进来的时候有可能是编辑状态，所以需要展示已经选好的条目，并不能以空作为判断依据
                        maps.forEach { (field, value) ->
                            // 只针对默认显示为False的条目进行判断，为true的都是必须显示的。
                            val declaredFiled = it::class.java.getDeclaredField(field)
                            declaredFiled.isAccessible = true
                            val values = declaredFiled.get(it)?.toString()
                            if (!value.isVisible) {
                                val mapValue = logAdapter.logTypeMap[it.logType]
                                // 条目是否可见
                                if (mapValue.isNullOrEmpty()) {
                                    logAdapter.fieldsAttributes[field]?.isVisible = !values.isNullOrEmpty()
                                } else {
                                    // 找到相对应的mapValue，判断是否相等
                                    logAdapter.fieldsAttributes[field]?.isVisible = mapValue.contains(field)
                                }
                            }
                            // 转换公英制
                            if (logAdapter.fieldsAttributes[field]?.unit?.isEmpty() == true) {
                                logAdapter.fieldsAttributes[field]?.unit = if (data?.inchMetricMode == "inch") logAdapter.fieldsAttributes[field]?.imperialUnits.toString() else logAdapter.fieldsAttributes[field]?.metricUnits.toString()
                            }
                        }
                        logAdapter.setData(it)
                        // 添加备注
                        binding.etNote.setText(it.notes)
                    }
                }
            })

            getLogTypeList.observe(this@PlantActionActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 请求日志详情
                    viewModel.getLogById(logId)
                }
            })
        }
    }

    override fun initData() {
    }

    override fun onValueChanged(position: Int, newValue: String) {

    }

    override fun onEditTextClick(position: Int, editText: EditText, customViewGroup: CustomViewGroup) {
        val selectedLogType = editText.text.toString()
        val logTypeListDataItems = viewModel.getLogTypeList.value?.data?.map { item ->
            item.copy(isSelected = item.showUiText == selectedLogType)
        } ?: mutableListOf()

        (logTypeListDataItems as? MutableList<LogTypeListDataItem>)?.let {
            customViewGroup.setRvListData(
                it, true
            )
        }
    }
}