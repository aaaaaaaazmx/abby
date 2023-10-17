package com.cl.modules_planting_log.adapter

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.TimePickerPop
import com.cl.common_base.util.Prefs
import com.cl.modules_planting_log.databinding.PlantingCustomViewGroupItemBinding
import com.cl.modules_planting_log.request.FieldAttributes
import com.cl.modules_planting_log.request.LogSaveOrUpdateReq
import com.cl.modules_planting_log.request.LogTypeListDataItem
import com.cl.modules_planting_log.widget.CustomViewGroup
import java.util.Calendar
import kotlin.math.log

/**
 * 自定义ViewGroup适配器
 * fileds和下面的描述文件一定是一一对应的，数量上一定是要一致的。
 *
 * 字段名： LogSaveOrUpdateReq , showUiText: LogTypeListDataItem
 *
 * @param   fields 想要展示的元素个数以及名称  ,, 由于要和后台返回的Bean类映射，所有传递的都是字段名
 * @param   noKeyboardFields EditText不可点击的元素名称, 用字段名来识别
 * @param   fieldsAttributes 主要用于显示单位、别名、hint描述等 ， key = 字段名
 * @param   interFaceEditTextValueChangeListener 暴露给外部的处理事件
 * @param   logTypeMap 这个是固定的选择logType之后，需要显示的映射相对应的显示和隐藏的条目, key = showUiText字段
 * @param   chooseTypedData 这个是根据选择的logType来对应的下面的可选列表，目前写死。 key = 字段名段
 */
class CustomViewGroupAdapter(
    private val context: Context,
    private val fields: List<String>,
    private val noKeyboardFields: List<String>,
    var fieldsAttributes: Map<String, FieldAttributes>,
    private val interFaceEditTextValueChangeListener: EditTextValueChangeListener? = null,
) : RecyclerView.Adapter<CustomViewGroupAdapter.ViewHolder>(),
    EditTextValueChangeListener, ShowOrHideTypeChangListener {

    // 可以用map来接收，防止排列顺序不对，数据错乱的问题
    private val data: MutableList<String> = MutableList(fields.size) { "" }

    // 这个需要根据当前是国际单位来更改
    private val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)

    // 这个是固定的选择logType之后，需要显示的映射相对应的显示和隐藏的条目
    // 这个目前也是固定的，主要是选择logType之后，显示listOf里面的条目，或者是选择其他的隐藏里面的条目
    val logTypeMap = mapOf(
        /*LogSaveOrUpdateReq.KEY_LOG_TYPE_WATER_TYPE to mutableListOf("waterType", "volume"),
        LogSaveOrUpdateReq.KEY_LOG_TYPE_FEEDING to mutableListOf("feedingType"),
        LogSaveOrUpdateReq.KEY_LOG_TYPE_REPELLENT to mutableListOf("repellentType"),
        LogSaveOrUpdateReq.KEY_LOG_TYPE_DECLARE_DEATH to mutableListOf("declareDeathType"),
        LogSaveOrUpdateReq.KEY_LOG_TYPE_HARVEST to mutableListOf("driedWeight", "wetWeight")*/

        "Change water" to mutableListOf("waterType", "volume"),
        "Feeding" to mutableListOf("feedingType"),
        "Repellent" to mutableListOf("repellentType"),
        "Declare Death" to mutableListOf("declareDeathType"),
        "Harvest" to mutableListOf("driedWeight", "wetWeight")
    )

    // 显示和隐藏的条目
    /*private val isShowOrHideType  = mutableListOf(
        "waterType", "volume","feedingType","repellentType","declareDeathType","driedWeight", "wetWeight"
    )*/

    // 这个是根据选择的logType来对应的下面的可选列表，目前写死。
    // key 是对应 后台返回LogTypeListDataItem类里面的showUiText字段
    private val chooseTypedData = mapOf(
        LogSaveOrUpdateReq.KEY_LOG_TYPE_WATER_TYPE to mutableListOf(
            LogTypeListDataItem("Purified", "Purified", false),
            LogTypeListDataItem("Distilled", "Distilled", false),
            LogTypeListDataItem("Tap Water", "Tap Water", false),
            LogTypeListDataItem("Others", "Others", false),
        ),

        LogSaveOrUpdateReq.KEY_LOG_TYPE_FEEDING to mutableListOf(
            LogTypeListDataItem("Liquid", "Liquid", false),
            LogTypeListDataItem("Solid", "Solid", false),
            LogTypeListDataItem("Spray", "Spray", false),
        ),

        LogSaveOrUpdateReq.KEY_LOG_TYPE_REPELLENT to mutableListOf(
            LogTypeListDataItem("Liquid", "Liquid", false),
            LogTypeListDataItem("Solid", "Solid", false),
            LogTypeListDataItem("Spray", "Spray", false),
        ),


        LogSaveOrUpdateReq.KEY_LOG_TYPE_DECLARE_DEATH to mutableSetOf(
            LogTypeListDataItem("Transplant shock", "Transplant shock", false),
            LogTypeListDataItem("Environmental factors", "Environmental factors", false),
            LogTypeListDataItem("Underwatering", "Underwatering", false),
            LogTypeListDataItem("Pest and Diseases", "Pest and Diseases", false),
            LogTypeListDataItem("Nutritent Imbalance", "Nutritent Imbalance", false),
            LogTypeListDataItem("Chemical Contaminants", "Chemical Contaminants", false),
            LogTypeListDataItem("Others", "Others", false),
        ),
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlantingCustomViewGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // 在你的Adapter中
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        // 设置隐藏和显示以及设置文案
        val attributes = fieldsAttributes[fields[position]]
        if (payloads.isEmpty()) {
            // 设置相关customViewGroup的值
            if (holder.customViewGroup.listener == null) {
                holder.customViewGroup.listener = this@CustomViewGroupAdapter
            }
            if (holder.customViewGroup.showOrHideListener == null) {
                holder.customViewGroup.showOrHideListener = this@CustomViewGroupAdapter
            }
            holder.customViewGroup.tag = position
            // 设置输入类型
            holder.customViewGroup.setInputType(attributes?.inputType ?: CustomViewGroup.TYPE_CLASS_TEXT)
            holder.customViewGroup.setTextView1Text(attributes?.description)
            holder.customViewGroup.setEditText1HintText(attributes?.hintDescription)
            holder.customViewGroup.setTextView2Text(attributes?.unit)
            holder.customViewGroup.setTextView2Visibility(!attributes?.unit.isNullOrBlank())
            holder.customViewGroup.setRefreshIconVisibility(attributes?.isShowRefreshIcon == true, attributes?.isConnect == true)
            chooseTypedData[fields[position]]?.let { list ->
                val updatedList = list.map { item ->
                    item.copy(isSelected = item.showUiText == data[position])
                }.toMutableList() as MutableList<LogTypeListDataItem>
                holder.customViewGroup.setRvListData(updatedList, false)
            }
            // 设置rv数据
            holder.customViewGroup.setEditText1Text(position, data[position])
            holder.customViewGroup.setRootVisible(attributes?.isVisible == true) // 是否是显示和隐藏的logType字段


            // editText不可点击
            val field = fields[position]
            val noKeyboard = noKeyboardFields.contains(field)
            holder.customViewGroup.setNoKeyboard(noKeyboard)
        } else {
            // 这一款主要是针对editText显示不正确的问题
            // 外加隐藏和显示条目、隐藏和显示都默认位空
            holder.customViewGroup.setEditText1Text(position, "")
            data[position] = ""
            holder.customViewGroup.setRootVisible(attributes?.isVisible == true) // 是否是显示和隐藏的logType字段
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun getItemCount() = data.size

    /**
     * EditText的监听事件
     */
    override fun onValueChanged(position: Int, newValue: String) {
        data[position] = newValue
    }

    // 开关灯时间
    private var turnOnHour: Int? = null

    /**
     * EditText的点击事件
     */
    @SuppressLint("SetTextI18n")
    override fun onEditTextClick(position: Int, editText: EditText, customViewGroup: CustomViewGroup) {
        when (fields[position]) {
            LogSaveOrUpdateReq.KEY_LOG_TIME -> {
                val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    logI("选择的日期是：$year-${monthOfYear + 1}-$dayOfMonth")
                    // 保存选择的日期
                    // 转成毫秒
                    editText.setText("${monthOfYear + 1}/$dayOfMonth/$year")
                }

                // 在用户打开日期选择器时，设置初始选中的日期为上次选择的日期
                Calendar.getInstance().apply {
                    val timeMill = DateHelper.formatToLong(editText.text.toString(), KEY_FORMAT_TIME)
                    timeInMillis = (timeMill)
                    // 月份从0开始，所以需要加1
                    DatePickerDialog(context, dateSetListener, get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH)).show()
                }
            }

            // 开灯时间
            LogSaveOrUpdateReq.KEY_LIGHTING_ON -> {
                // todo 弹窗
                val currentOnTime = editText.text.toString()
                val pattern = """(\d{1,2}):(\d{2}) ([AP]M)""".toRegex()
                val matchResult = pattern.matchEntire(currentOnTime)
                if (matchResult != null) {
                    val (housr, minute, amPm) = matchResult.destructured  // 使用解构声明来获取组的值
                    logI("Hour: $housr")
                    logI("Minute: $minute")
                    logI("AM/PM: $amPm")
                    turnOnHour = housr.safeToInt()
                    if (amPm == "PM") {
                        turnOnHour = (turnOnHour ?: 0) + 12
                    }
                } else {
                    logI("The time string does not match the expected format.")
                    turnOnHour = 12
                }
                xpopup(context) {
                    asCustom(
                        TimePickerPop(context, onConfirmAction = { time, timeMis ->
                            runCatching {
                                // 返回的是24小时制度。
                                val hour = if (time.safeToInt() == 0) 12 else time.safeToInt()

                                if (hour > 12) {
                                    editText.setText("${hour - 12}:00 PM")
                                } else if (hour < 12) {
                                    editText.setText("${hour}:00 AM")
                                } else if (hour == 12) {
                                    editText.setText("12:00 AM")
                                }
                                turnOnHour = hour
                                logI("123123: $turnOnHour")
                            }

                        }, chooseTime = turnOnHour ?: 12)
                    ).show()
                }
            }

            // 关灯时间
            LogSaveOrUpdateReq.KEY_LIGHTING_OFF -> {
                // todo 弹窗
                val currentOnTime = editText.text.toString()
                val pattern = """(\d{1,2}):(\d{2}) ([AP]M)""".toRegex()
                val matchResult = pattern.matchEntire(currentOnTime)
                if (matchResult != null) {
                    val (housr, minute, amPm) = matchResult.destructured  // 使用解构声明来获取组的值
                    turnOnHour = housr.safeToInt()
                    if (amPm == "PM") {
                        turnOnHour = (turnOnHour ?: 0) + 12
                    }
                    logI("Hour: $housr")
                    logI("Minute: $minute")
                    logI("AM/PM: $amPm")
                } else {
                    logI("The time string does not match the expected format.")
                    turnOnHour = 12
                }
                xpopup(context) {
                    asCustom(
                        TimePickerPop(context, onConfirmAction = { time, timeMis ->
                            runCatching {
                                // 返回的是24小时制度。
                                val hour = if (time.safeToInt() == 0) 12 else time.safeToInt()

                                if (hour > 12) {
                                    editText.setText("${hour - 12}:00 PM")
                                } else if (hour < 12) {
                                    editText.setText("${hour}:00 AM")
                                } else if (hour == 12) {
                                    editText.setText("12:00 AM")
                                }
                                turnOnHour = hour
                                logI("123123: $turnOnHour")
                            }

                        }, chooseTime = turnOnHour ?: 12)
                    ).show()
                }
            }

            LogSaveOrUpdateReq.KEY_LOG_TYPE
            -> {
                // 弹出日志类型选择列表
                //  由外部调用，
                interFaceEditTextValueChangeListener?.onEditTextClick(position, editText, customViewGroup)
            }

            LogSaveOrUpdateReq.KEY_LOG_TYPE_WATER_TYPE,
            LogSaveOrUpdateReq.KEY_LOG_TYPE_DECLARE_DEATH,
            LogSaveOrUpdateReq.KEY_LOG_TYPE_FEEDING,
            LogSaveOrUpdateReq.KEY_LOG_TYPE_REPELLENT -> {
                if (customViewGroup.getRvListData()) customViewGroup.getRvListData() else interFaceEditTextValueChangeListener?.onEditTextClick(position, editText, customViewGroup)

            }
        }
    }


    /**
     * 刷新按钮的点击事件
     */
    override fun onRefreshData(position: Int, imageview: ImageView, customViewGroup: CustomViewGroup) {
        when (fields[position]) {
            LogSaveOrUpdateReq.KEY_LOG_PH -> {
                // 点击Ph按钮时，需要刷新当前的Ph、tds、ec值
                interFaceEditTextValueChangeListener?.onRefreshData(position, imageview, customViewGroup)
            }

            else -> {}
        }
    }

    /**
     * 外部处理interFaceEditTextValueChangeListener回调时调用的设置单个数据的方法
     */
    fun setData(position: Int, inputData: String) {
        runCatching {
            when (fields[position]) {
                LogSaveOrUpdateReq.KEY_LOG_PH -> {
                    data[position] = inputData
                    notifyItemChanged(position)
                }
            }
        }

    }

    fun setData(logData: LogSaveOrUpdateReq) {
        runCatching {
            // 使用反射设置对应的值
            fields.forEachIndexed { index, field ->
                val declaredFiled = logData::class.java.getDeclaredField(field)
                declaredFiled.isAccessible = true
                val value = declaredFiled.get(logData)?.toString()
                data[index] = value ?: ""
            }
            notifyDataSetChanged()
        }
    }

    // 提供方法获取数据并填充到LogData对象
    // 提供方法获取数据并填充到LogData对象
    fun getLogData(): LogSaveOrUpdateReq {
        return runCatching {
            val logData = LogSaveOrUpdateReq()
            fields.forEachIndexed { index, field ->
                val declaredFiled = logData::class.java.getDeclaredField(field)
                declaredFiled.isAccessible = true
                when (field) {
                    // 用户选择了时间，可能会返回字符串
                    LogSaveOrUpdateReq.KEY_LOG_TIME -> {
                        declaredFiled.set(logData, DateHelper.formatToLong(data[index], KEY_FORMAT_TIME).toString())
                    }

                    else -> declaredFiled.set(logData, data[index])
                }
            }
            logData
        }.getOrElse { LogSaveOrUpdateReq() }
    }

    class ViewHolder(val binding: PlantingCustomViewGroupItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val customViewGroup = itemView as CustomViewGroup
    }

    companion object {
        const val KEY_FORMAT_TIME = "MM/dd/yyyy"
    }

    /**
     * 用于显示和隐藏的条目回调
     */
    override fun showOrHide(previousSelectedIndex: Int, currentIndex: Int, beforeShowUiText: String?, showUiText: String, customViewGroup: CustomViewGroup) {
        // 2、显示当前的type
        logTypeMap[showUiText]?.forEach { value ->
            fieldsAttributes[value]?.isVisible = true
            notifyItemChanged(fields.indexOf(value), "payLoads")
        }

        // 1、先隐藏上个的type
        beforeShowUiText?.let {
            logTypeMap[it]?.forEach { value ->
                // 隐藏
                fieldsAttributes[value]?.isVisible = false
                data[fields.indexOf(value)] = ""
                // 刷新
                notifyItemChanged(fields.indexOf(value), "payLoads")
            }
        }
    }
}

interface EditTextValueChangeListener {
    fun onValueChanged(position: Int, newValue: String)
    fun onEditTextClick(position: Int, editText: EditText, customViewGroup: CustomViewGroup)

    fun onRefreshData(position: Int, imageview: ImageView, customViewGroup: CustomViewGroup)
}

interface ShowOrHideTypeChangListener {
    fun showOrHide(previousSelectedIndex: Int, currentIndex: Int, beforeShowUiText: String?, showUiText: String, customViewGroup: CustomViewGroup)
}


