package com.cl.common_base.pop

import android.content.Context
import android.os.Build
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.cl.common_base.BaseApplication
import com.cl.common_base.R
import com.cl.common_base.bean.MessageConfigBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.BaseSelectPeriodTimePopBinding
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.common_base.widget.wheel.time.StringPicker
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView

class SelectPeriodTimePop(context: Context, private val timeString: String, private val selectAction: ((String, String) -> Unit)? = null) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_select_period_time_pop
    }

    // 直接借口调用
    private val service = ServiceCreators.create(BaseApiService::class.java)
    private lateinit var binding: BaseSelectPeriodTimePopBinding

    private val loadingPopup by lazy {
        XPopup.Builder(context).asLoading(BaseApplication.getContext().getString(R.string.string_216))
    }
    private val weekList by lazy {
        // Week 1 - 217 -288
        mutableListOf(
            BaseApplication.getContext().getString(R.string.string_217),
            BaseApplication.getContext().getString(R.string.string_218),
            BaseApplication.getContext().getString(R.string.string_219),
            BaseApplication.getContext().getString(R.string.string_220),
            BaseApplication.getContext().getString(R.string.string_221),
            BaseApplication.getContext().getString(R.string.string_222),
            BaseApplication.getContext().getString(R.string.string_223),
            BaseApplication.getContext().getString(R.string.string_224),
            BaseApplication.getContext().getString(R.string.string_225),
            BaseApplication.getContext().getString(R.string.string_226),
            BaseApplication.getContext().getString(R.string.string_227),
            BaseApplication.getContext().getString(R.string.string_228),
        )
    }

    private val dayList by lazy {
        // string_229 - string_235
        mutableListOf(
            BaseApplication.getContext().getString(R.string.string_229),
            BaseApplication.getContext().getString(R.string.string_230),
            BaseApplication.getContext().getString(R.string.string_231),
            BaseApplication.getContext().getString(R.string.string_232),
            BaseApplication.getContext().getString(R.string.string_233),
            BaseApplication.getContext().getString(R.string.string_234),
            BaseApplication.getContext().getString(R.string.string_235),
        )
    }

    private var selectWeekString: String = ""
    private var selectDayString: String = ""

    private fun extractNumbers(input: String): Pair<Int, Int> {
        // 定义正则表达式，匹配 "Week X Day Y" 其中 X 和 Y 为数字，周围可能有多余的空格
        val regex = """${context.getString(R.string.week)}\s+(\d+)\s+${context.getString(R.string.day)}\s+(\d+)""".toRegex()
        // 在输入字符串中查找第一个匹配项
        val matchResult = regex.find(input.trim())  // 使用 trim() 移除输入字符串两端的空白字符

        // 如果找到匹配，提取数字并返回；否则返回 (0, 0)
        return if (matchResult != null) {
            val (weekNumber, dayNumber) = matchResult.destructured
            Pair(weekNumber.toInt(), dayNumber.toInt())
        } else {
            Pair(0, 0)  // 如果没有找到匹配，返回 (0, 0)
        }
    }


    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseSelectPeriodTimePopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@SelectPeriodTimePop
            executePendingBindings()

            tvCancel.setSafeOnClickListener { dismiss() }
            tvConfirm.setSafeOnClickListener {
                if (!datePickerLayoutDate.dataList.isNullOrEmpty()) {
                    if (selectWeekString.isEmpty()) {
                        selectWeekString = datePickerLayoutDate.dataList[0]
                    }
                }

                if (!hourPickerLayoutTime.dataList.isNullOrEmpty()) {
                    if (selectDayString.isEmpty()) {
                        selectDayString = hourPickerLayoutTime.dataList[0]
                    }
                }

                selectAction?.invoke(selectWeekString, selectDayString)
                dismiss()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                datePickerLayoutDate.setStringList(weekList)
                val extractNumbers = extractNumbers(timeString)
                datePickerLayoutDate.setSelectedScope(if (extractNumbers.first !=0 ) extractNumbers.first -1 else 0)

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                hourPickerLayoutTime.setStringList(dayList)
                val extractNumbers = extractNumbers(timeString)
                hourPickerLayoutTime.setSelectedScope(if (extractNumbers.second !=0 ) extractNumbers.second -1 else 0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                datePickerLayoutDate.setOnStringSelectedListener(object : StringPicker.OnStringSelectedListener {
                    override fun onScopeSelected(index: Int) {

                    }

                    override fun onScopeSelected(index: String?) {
                        index?.let {
                            selectWeekString = it
                        }
                    }
                })
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                hourPickerLayoutTime.setOnStringSelectedListener(object : StringPicker.OnStringSelectedListener {
                    override fun onScopeSelected(index: Int) {

                    }

                    override fun onScopeSelected(index: String?) {
                        index?.let {
                            selectDayString = it
                        }
                    }
                })
            }
        }
    }


}