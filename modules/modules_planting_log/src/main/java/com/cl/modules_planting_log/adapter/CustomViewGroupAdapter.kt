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
import com.cl.modules_planting_log.request.CardInfo
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
 * @param   fieldsAttributes 主要用于显示单位、别名、hint描述等
 * @param   interFaceEditTextValueChangeListener 暴露给外部的处理事件
 */
class CustomViewGroupAdapter(private val context: Context, private val fields: List<String>,
                             private val noKeyboardFields: List<String>,
                             private val fieldsAttributes: Map<String, FieldAttributes>,
                             private val interFaceEditTextValueChangeListener: EditTextValueChangeListener? = null,
) : RecyclerView.Adapter<CustomViewGroupAdapter.ViewHolder>(),
    EditTextValueChangeListener {
    // 可以用map来接收，防止排列顺序不对，数据错乱的问题
    private val data: MutableList<String> = MutableList(fields.size) { "" }

    // 这个需要根据当前是国际单位来更改
    private val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)

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
        when(fields[position]) {
            LogSaveOrUpdateReq.KEY_LOG_TIME -> {
                data[position] = DateHelper.formatToLong(newValue, KEY_FORMAT_TIME).toString()
            }
            else -> data[position] = newValue
        }
    }

    /**
     * EditText的点击事件
     */
    @SuppressLint("SetTextI18n")
    override fun onEditTextClick(position: Int, editText: EditText) {
        when (fields[position]) {
            LogSaveOrUpdateReq.KEY_LOG_TIME -> {
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

            LogSaveOrUpdateReq.KEY_LOG_TYPE -> {
                // 弹出日志类型选择列表
                //  由外部调用，
                interFaceEditTextValueChangeListener?.onEditTextClick(position, editText)
            }
        }
    }

    /**
     * 外部处理interFaceEditTextValueChangeListener回调时调用的设置单个数据的方法
     */
    fun setData(position: Int, editText: EditText, inputData: String) {
        when(fields[position]) {
            LogSaveOrUpdateReq.KEY_LOG_TYPE -> {
                editText.setText(inputData)
                data[position] = inputData
            }
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


