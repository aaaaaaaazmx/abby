package com.cl.common_base.pop

import android.content.Context
import android.text.format.DateUtils
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseTimeChoosePopBinding
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.util.calendar.CalendarUtil
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.common_base.widget.wheel.time.DatePicker
import com.cl.common_base.widget.wheel.time.HourPicker
import com.cl.common_base.widget.wheel.time.MinutePicker
import com.google.android.gms.common.util.DataUtils
import com.lxj.xpopup.core.CenterPopupView
import java.time.Year
import java.util.*

/**
 * 时间选择弹窗
 */
class BaseTimeChoosePop(
    context: Context,
    private val currentTime: Long? = null,
    private val onConfirmAction: ((time: String, timeMis: Long) -> Unit)? = null,
) : CenterPopupView(context), DatePicker.OnDateSelectedListener, HourPicker.OnHourSelectedListener,
    MinutePicker.OnMinuteSelectedListener {

    override fun getImplLayoutId(): Int {
        return R.layout.base_time_choose_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseTimeChoosePopBinding>(popupImplView)?.apply {
            // 滑动监听
            hourPickerLayoutTime.setOnHourSelectedListener(this@BaseTimeChoosePop)
            datePickerLayoutDate.setOnDateSelectedListener(this@BaseTimeChoosePop)
            minutePickerLayoutTime.setOnMinuteSelectedListener(this@BaseTimeChoosePop)

            // 传递当前时间给日期
            currentTime?.let {
                datePickerLayoutDate.setCurrentDate(it)
                hourPickerLayoutTime.setSelectedHour(DateHelper.formatTime(it, "HH").toInt())
                minutePickerLayoutTime.setSelectedMinute(DateHelper.formatTime(it, "mm").toInt())
            }

            tvConfirm.setOnClickListener {
                dismiss()
                logI(
                    """
                    tvConfirm:  
                    ${"$date $hour-$minute"}
                    ${Date().time}
                    ${DateHelper.formatToLong("$date $hour:$minute", "yyyy-MM-dd HH:mm")}
                """.trimIndent()
                )
                // todo 需要判断当前时间和选中的时间的比较
                currentTime?.let {
                    // 判断是否是今天之前的就
                    if (DateHelper.formatToLong("$date $hour:$minute", "yyyy-MM-dd HH:mm") < Date().time) {
                        ToastUtil.shortShow(context.getString(R.string.base_please_try_again))
                        return@setOnClickListener
                    }
                }
                // 传给后台的时间为 yyyy-MM-dd HH:mm
                // 需要除以1000，不然不行。
                onConfirmAction?.invoke(
                    "$date $hour:$minute",
                    DateHelper.formatToLong("$date $hour:$minute", "yyyy-MM-dd HH:mm")
                )
            }

            tvCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    /**
     * 日期选择
     */
    private var date: String? = null
    private var chooseDateTime: Long? = null
    override fun onDateSelected(date: String?, time: Long) {
        this.date = date
        this.chooseDateTime = time
    }

    /**
     * 时间选择
     */
    private var hour: Int? = null
    override fun onHourSelected(hour: Int?) {
        logI("onHourSelected: $hour")
        this.hour = hour
    }

    /**
     * 分钟选择
     */
    private var minute: Int? = 0
    override fun onMinuteSelected(minute: Int) {
        logI("onMinuteSelected: $hour")
        this.minute = minute
    }
}