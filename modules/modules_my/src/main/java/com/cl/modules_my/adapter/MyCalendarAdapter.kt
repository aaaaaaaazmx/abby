package com.cl.modules_my.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.bean.CalendarData
import com.cl.common_base.bean.DetailByLearnMoreIdData
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.calendar.Calendar
import com.cl.common_base.util.calendar.CalendarUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyCalendarItemBinding
import com.cl.modules_my.databinding.MyCalendarTitleItemBinding
import com.cl.modules_my.databinding.MyTroubleItemBinding
import com.cl.modules_my.repository.MyTroubleData
import okio.blackholeSink
import java.time.temporal.TemporalAccessor
import java.util.*


/**
 * 日历
 */
class MyCalendarAdapter(data: MutableList<Calendar>?) :
    BaseQuickAdapter<Calendar, BaseViewHolder>(R.layout.my_calendar_item, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<MyCalendarItemBinding>(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: Calendar) {
        holder.getBinding<MyCalendarItemBinding>()?.apply {
            // 设置数据
            data = item
            adapter = this@MyCalendarAdapter
            executePendingBindings()
        }
        setCircleBg(holder, item)
    }

    /**
     * 设置背景小圆点
     */
    private fun setCircleBg(
        holder: BaseViewHolder,
        item: Calendar
    ): Boolean {
        // 设置周期小圆点
        val changWater = holder.getView<ImageView>(R.id.iv_change_water)
        val changeUnlock = holder.getView<ImageView>(R.id.iv_change_unlock)
        val changElse = holder.getView<ImageView>(R.id.iv_change_else)
        val llPoint = holder.getView<LinearLayout>(R.id.ll_point)

        // 过期的颜色展示
        val changWaterGray = holder.getView<ImageView>(R.id.iv_change_water_gray)
        val changUnlockGray = holder.getView<ImageView>(R.id.iv_change_unlock_gray)
        val changElseGray = holder.getView<ImageView>(R.id.iv_change_else_gray)
        val llPointGray = holder.getView<LinearLayout>(R.id.ll_point_gray)

        // 当前字体颜色
        val currentColor = holder.getView<TextView>(R.id.text_date_day).currentTextColor

        if (null == item.calendarData) {
            ViewUtils.setInvisible(llPoint)
            ViewUtils.setGone(llPointGray)
            ViewUtils.setGone(changWater, changeUnlock, changElse)
            return true
        } else {
            ViewUtils.setVisible(llPoint)
        }
        // 现在有且只有3个点
        item.calendarData.taskList?.forEachIndexed { index, data ->
            val itemTime = Date(item.timeInMillis)
            val currentTime = Date()

            if (DateHelper.after(currentTime, itemTime)) {
                // 当前时间大于itemTime
                when (index) {
                    0 -> {
                        ViewUtils.setVisible(llPointGray)
                        ViewUtils.setGone(llPoint)
                        ViewUtils.setVisible(changWaterGray)
                    }
                    1 -> {
                        ViewUtils.setVisible(llPointGray)
                        ViewUtils.setGone(llPoint)
                        ViewUtils.setVisible(changUnlockGray)
                    }
                    2 -> {
                        ViewUtils.setVisible(llPointGray)
                        ViewUtils.setGone(llPoint)
                        ViewUtils.setVisible(changElseGray)
                    }
                }
            } else if (DateHelper.after(itemTime, currentTime)) {
                // 当前时间小于itemTime
                when (data.taskType) {
                    "Unlock" -> {
                        ViewUtils.setVisible(changeUnlock)
                    }
                    "changing_water" -> {
                        ViewUtils.setVisible(changWater)
                    }
                    else -> {
                        ViewUtils.setVisible(changElse)
                    }
                }
            } else if (item.ymd == CalendarUtil.getFormat("yyyy-MM-dd").format(Date())) {
                when (data.taskType) {
                    "Unlock" -> {
                        ViewUtils.setVisible(changeUnlock)
                    }
                    "changing_water" -> {
                        ViewUtils.setVisible(changWater)
                    }
                    else -> {
                        ViewUtils.setVisible(changElse)
                    }
                }
            }
        }
        return false
    }


    fun getBg(item: Calendar): Any? {
        if (item.calendarData == null) return null
        val epochStartTime = item.calendarData.epochStartTime?.let { epochStartTime ->
            DateHelper.formatToLong(
                epochStartTime, "yyyy-MM-dd"
            )
        } ?: 0L

        val epochEndTime = item.calendarData.epochEndTime?.let { epochEndTime ->
            DateHelper.formatToLong(
                epochEndTime, "yyyy-MM-dd"
            )
        } ?: 0L

        return if (item.ymd == item.calendarData.epochStartTime) {
            // 判断是否是最后一个
            if (CalendarUtil.isSaturday(item)) {
                ContextCompat.getDrawable(
                    context,
                    com.cl.common_base.R.drawable.bg_calendar_first_and_circle
                )
            } else {
                ContextCompat.getDrawable(
                    context,
                    com.cl.common_base.R.drawable.bg_calendar_first
                )
            }
        } else if (item.ymd == item.calendarData.epochEndTime) {
            if (CalendarUtil.isSunday(item)) {
                ContextCompat.getDrawable(
                    context,
                    com.cl.common_base.R.drawable.bg_calendar_end_and_circle
                )
            } else {
                ContextCompat.getDrawable(
                    context,
                    com.cl.common_base.R.drawable.bg_calendar_end
                )
            }
        } else if (item.timeInMillis in (epochStartTime + 1) until epochEndTime) {
            if (CalendarUtil.isSaturday(item)) {
                ContextCompat.getDrawable(
                    context,
                    com.cl.common_base.R.drawable.bg_calendar_normal_and_rtb_circle
                )
            } else if (CalendarUtil.isSunday(item)) {
                ContextCompat.getDrawable(
                    context,
                    com.cl.common_base.R.drawable.bg_calendar_normal_and_ltb_circle
                )
            } else {
                ContextCompat.getDrawable(
                    context,
                    com.cl.common_base.R.drawable.bg_calendar_normal
                )
            }
        } else {
            null
        }
    }

    fun getTextColors(item: Calendar): Int {
        val itemTime = Date(item.timeInMillis)
        val currentTime = Date()
        // 选中状态为白色
        if (item.isChooser) {
            Color.WHITE
        } else
        // 时间判断
        if (DateHelper.after(currentTime, itemTime)) {
            // 当前时间大于itemTime
            return ContextCompat.getColor(
                context,
                com.cl.common_base.R.color.calendarGray
            )
        } else
        if (DateHelper.after(itemTime, currentTime)) {
            // 当前时间小于itemTime
            return Color.BLACK
        } else
        if (item.ymd == CalendarUtil.getFormat("yyyy-MM-dd").format(Date())) {
            return Color.BLACK
        }
        return -1
    }
}