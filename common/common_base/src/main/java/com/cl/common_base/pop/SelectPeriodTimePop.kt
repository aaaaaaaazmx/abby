package com.cl.common_base.pop

import android.content.Context
import android.os.Build
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
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

class SelectPeriodTimePop(context: Context, private val timeString: String, private var weekList: MutableList<String> ? = null,  private var dayList: MutableList<String>? = null, private val selectAction: ((String, String) -> Unit)? = null) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_select_period_time_pop
    }

    // 直接借口调用
    private val service = ServiceCreators.create(BaseApiService::class.java)
    private lateinit var binding: BaseSelectPeriodTimePopBinding

    private val loadingPopup by lazy {
        XPopup.Builder(context).asLoading("Loading...")
    }

    private var selectWeekString: String = ""
    private var selectDayString: String = ""

    private fun extractNumbers(input: String): Pair<Int, Int> {
        // 定义正则表达式，匹配 "Week X Day Y" 其中 X 和 Y 为数字，周围可能有多余的空格
        val regex = """Week\s+(\d+)\s+Day\s+(\d+)""".toRegex()
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

            // 设置默认值。
            if (weekList.isNullOrEmpty()) {
                weekList = mutableListOf("Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6", "Week 7", "Week 8", "Week 9", "Week 10", "Week 11", "Week 12")
            }

            if (dayList.isNullOrEmpty()) {
                dayList = mutableListOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Day 7")
            }

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