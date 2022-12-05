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
import com.cl.common_base.constants.UnReadConstants
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
            holders = holder
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
        val changeAcademyTask = holder.getView<ImageView>(R.id.iv_academy_task)
        val llPoint = holder.getView<LinearLayout>(R.id.ll_point)

        // 过期的颜色展示
        val changWaterGray = holder.getView<ImageView>(R.id.iv_change_water_gray)
        val changUnlockGray = holder.getView<ImageView>(R.id.iv_change_unlock_gray)
        val changElseGray = holder.getView<ImageView>(R.id.iv_change_else_gray)
        val changAcadeMyTaskGray = holder.getView<ImageView>(R.id.iv_change_academy_task_gray)
        val llPointGray = holder.getView<LinearLayout>(R.id.ll_point_gray)

        // 放置布局错乱，应该是全部都先隐藏，然后在根据滑动的时候来进行显示
        ViewUtils.setGone(changWater, changeUnlock, changElse)
        if (item.calendarData?.taskList.isNullOrEmpty()) {
            ViewUtils.setInvisible(llPoint)
            ViewUtils.setGone(llPointGray)
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
                    else -> {

                    }
                }
            } else if (DateHelper.after(itemTime, currentTime) || item.ymd == CalendarUtil.getFormat("yyyy-MM-dd").format(Date())) {
                // 当前时间小于或者等于itemTime
                if (null == data) return@forEachIndexed
                if (data.taskType.isNullOrEmpty()) return@forEachIndexed
                when (data.taskType) {
                    UnReadConstants.PlantStatus.TASK_TYPE_CHECK_TRANSPLANT,
                    UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_AUTOFLOWERING,
                    UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_FLUSHING,
                    UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_HARVEST,
                    UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_DRYING,
                    UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_CURING,
                    UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_FINISH,
                    UnReadConstants.PlantStatus.TASK_TYPE_CHECK_CHECK_FLOWERING -> {
                        ViewUtils.setVisible(changeUnlock)
                    }
                    KEY_CHANGE_WATER,
                    KEY_CHANGE_CUP_WATER -> {
                        ViewUtils.setVisible(changWater)
                    }
                    // 学院消息
                    KEY_TEST -> {
                        ViewUtils.setVisible(changeAcademyTask)
                    }
                    else -> {
                        ViewUtils.setVisible(changElse)
                    }
                }
            }
        }
        return false
    }


    /**
     * 设置周期背景
     */
    fun getBg(item: Calendar): Any? {
        return when (item.bgFlag) {
            Calendar.KEY_START -> {
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
            }
            Calendar.KEY_NORMAL -> {
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
            }
            Calendar.KEY_END -> {
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
            }
            else -> {
                null
            }
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

    companion object {
        const val KEY_CHANGE_CUP_WATER = "change_cup_water"
        const val KEY_CHANGE_WATER = "change_water"
        const val KEY_LST = "lst"
        const val KEY_TOPPING = "topping"
        const val KEY_TRIM = "trim"
        const val KEY_TEST = "test" // 学院消息
    }
}