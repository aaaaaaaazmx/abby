package com.cl.modules_planting_log.adapter

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.temperatureConversion
import com.cl.common_base.ext.unitsConversion
import com.cl.common_base.ext.weightConversion
import com.cl.common_base.util.Prefs
import com.cl.modules_planting_log.databinding.PlantingCustomViewGroupItemBinding
import com.cl.modules_planting_log.request.FieldAttributes
import com.cl.modules_planting_log.request.LogSaveOrUpdateReq
import com.cl.modules_planting_log.widget.CustomViewGroup
import java.nio.file.attribute.FileAttribute
import java.util.Calendar

/**
 * 自定义ViewGroup适配器
 * fileds和下面的描述文件一定是一一对应的，数量上一定是要一致的。
 *
 * @param   fields 想要展示的元素个数以及名称  ,, 由于要和后台返回的Bean类映射，所有传递的都是字段名
 * @param   noKeyboardFields EditText不可点击的元素名称
 */
class CustomViewGroupAdapter(private val fields: List<String>, private val noKeyboardFields: List<String>, private val context: Context) : RecyclerView.Adapter<CustomViewGroupAdapter.ViewHolder>(),
    EditTextValueChangeListener {
    // 可以用map来接收，防止排列顺序不对，数据错乱的问题
    private val data: MutableList<String> = MutableList(fields.size) { "" }

    // 这个需要根据当前是国际单位来更改
    private val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
    private val fieldsAttributes = mapOf(
        "logTime" to FieldAttributes("Date*", "自动填写（可更改，下拉日历弹框）", "", CustomViewGroup.TYPE_CLASS_TEXT),
        "spaceTemp" to FieldAttributes("Space Temp(ST)", "自动填写（可更改)", if (isMetric) "C" else "F", CustomViewGroup.TYPE_CLASS_NUMBER),
        "waterTemp" to FieldAttributes("Water Temp (WT)", "自动填写（可更改)", if (isMetric) "C" else "F", CustomViewGroup.TYPE_CLASS_NUMBER),
        "humidity" to FieldAttributes("Humidity (RH)", "自动填写（可更改)", "%", CustomViewGroup.TYPE_CLASS_NUMBER),
        "ph" to FieldAttributes("PH", "手动填写", "", CustomViewGroup.TYPE_CLASS_NUMBER),
        "tdsEc" to FieldAttributes("TDS/EC", "手动填写", "", CustomViewGroup.TYPE_CLASS_NUMBER),
        "plantHeight" to FieldAttributes("Height (HT)", "自动填写（可更改)", if (isMetric) "cm" else "In", CustomViewGroup.TYPE_CLASS_NUMBER),
        "vpd" to FieldAttributes("VPD", "自动填写（可更改)", "", CustomViewGroup.TYPE_CLASS_NUMBER),
        "driedWeight" to FieldAttributes("Yield (Dried weight)", "手动填写", if (isMetric) "g" else "Oz", CustomViewGroup.TYPE_CLASS_NUMBER),
        "wetWeight" to FieldAttributes("Yield (Wet weight)", "手动填写", if (isMetric) "g" else "Oz", CustomViewGroup.TYPE_CLASS_NUMBER),
        "lightingSchedule" to FieldAttributes("Lighting Schedule", "自动填写（可更改)", "", CustomViewGroup.TYPE_CLASS_TEXT),
        "co2Concentration" to FieldAttributes("CO2 Concentration", "手动填写", "", CustomViewGroup.TYPE_CLASS_NUMBER),
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlantingCustomViewGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 设置相关customViewGroup的值
        holder.customViewGroup.listener = this@CustomViewGroupAdapter
        holder.customViewGroup.tag = position
        // 设置隐藏和显示以及设置文案
        val attributes = fieldsAttributes[fields[position]]
        holder.customViewGroup.setTextView1Text(attributes?.description)
        holder.customViewGroup.setEditText1HintText(attributes?.hintDescription)
        holder.customViewGroup.setTextView2Text(attributes?.unit)
        holder.customViewGroup.setTextView2Visibility(!attributes?.unit.isNullOrBlank())
        holder.customViewGroup.setEditText1Text(data[position])

        // editText不可点击
        val field = fields[position]
        val noKeyboard = noKeyboardFields.contains(field)
        holder.customViewGroup.setNoKeyboard(noKeyboard)
        // 设置输入类型
        holder.customViewGroup.setInputType(attributes?.inputType ?: CustomViewGroup.TYPE_CLASS_TEXT)
    }

    override fun getItemCount() = data.size

    /**
     * EditText的监听事件
     */
    override fun onValueChanged(position: Int, newValue: String) {
        data[position] = newValue
    }

    /**
     * EditText的点击事件
     */
    @SuppressLint("SetTextI18n")
    override fun onEditTextClick(position: Int, editText: EditText) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            logI("选择的日期是：$year-${monthOfYear + 1}-$dayOfMonth")
            // 保存选择的日期
            // 转成毫秒
            editText.setText("$year-${monthOfYear + 1}-$dayOfMonth")
            data[position] = DateHelper.formatToLong("$year-${monthOfYear + 1}-$dayOfMonth", KEY_FORMAT_TIME).toString()
        }

        // 在用户打开日期选择器时，设置初始选中的日期为上次选择的日期
        Calendar.getInstance().apply {
            val timeMill = data[position].toLongOrNull() ?: DateHelper.formatToLong(data[position], KEY_FORMAT_TIME)
            timeInMillis = (timeMill)
            // 月份从0开始，所以需要加1
            DatePickerDialog(context, dateSetListener, get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    fun setData(logData: LogSaveOrUpdateReq) {
        // 使用反射设置对应的值
        fields.forEachIndexed { index, field ->
            val declaredFiled = logData::class.java.getDeclaredField(field)
            declaredFiled.isAccessible = true
            val value = declaredFiled.get(logData)?.toString()
            // 公英制转换,
            // 需要根据特定的配型，来转换温度、长度、单位
            kotlin.runCatching {
                when (field) {
                    LogSaveOrUpdateReq.KEY_LOG_TIME -> data[index] = DateHelper.formatTime(value?.toLongOrNull() ?: 0L, KEY_FORMAT_TIME)
                    LogSaveOrUpdateReq.KEY_SPACE_TEMP -> data[index] = temperatureConversion(value?.toFloatOrNull() ?: 0f, isMetric, false)
                    LogSaveOrUpdateReq.KEY_WATER_TEMP -> data[index] = temperatureConversion(value?.toFloatOrNull() ?: 0f, isMetric, false)
                    LogSaveOrUpdateReq.KEY_PLANT_HEIGHT -> data[index] = unitsConversion(value?.toFloatOrNull() ?: 0f, isMetric, false)
                    LogSaveOrUpdateReq.KEY_DRIED_WEIGHT -> data[index] = weightConversion(value?.toFloatOrNull() ?: 0f, isMetric, false)
                    LogSaveOrUpdateReq.KEY_WET_WEIGHT -> data[index] = weightConversion(value?.toFloatOrNull() ?: 0f, isMetric, false)
                    else -> data[index] = value ?: ""
                }
            }
        }
        notifyDataSetChanged()
    }

    // 提供方法获取数据并填充到LogData对象
    fun getLogData(): LogSaveOrUpdateReq {
        val logData = LogSaveOrUpdateReq()
        fields.forEachIndexed { index, field ->
            val declaredFiled = logData::class.java.getDeclaredField(field)
            declaredFiled.isAccessible = true
            declaredFiled.set(logData, data[index])
        }
        return logData
    }

    class ViewHolder(val binding: PlantingCustomViewGroupItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val customViewGroup = itemView as CustomViewGroup
    }

    companion object {
        const val KEY_FORMAT_TIME = "yyyy-MM-dd"
    }
}

interface EditTextValueChangeListener {
    fun onValueChanged(position: Int, newValue: String)
    fun onEditTextClick(position: Int, editText: EditText)
}


