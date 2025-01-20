package com.cl.modules_my.adapter

import android.content.Intent
import android.graphics.Paint
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.bean.CalendarData
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.util.ViewUtils
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyCalendarTaskListItemBinding
import com.lxj.xpopup.XPopup
import java.io.Serializable


class CalendarTaskAdapter(
    data: MutableList<CalendarData.TaskList>?,
    var onDeleteClick: ((CalendarData.TaskList.SubPlantList?, CalendarData.TaskList, Int, SubChooseAdapter) -> Unit)? = null,
    var onDelayClick: ((CalendarData.TaskList.SubPlantList?, CalendarData.TaskList, Int, SubChooseAdapter) -> Unit)? = null,
    var onCompleteClick: ((CalendarData.TaskList.SubPlantList?, CalendarData.TaskList, Int, SubChooseAdapter) -> Unit)? = null,
    var onPreViewClick: ((CalendarData.TaskList.SubTaskList?, CalendarData.TaskList) -> Unit)? = null,
    var onJumpClick: ((CalendarData.TaskList.SubTaskList?, CalendarData.TaskList) -> Unit)? = null
) :
    BaseQuickAdapter<CalendarData.TaskList, BaseDataBindingHolder<MyCalendarTaskListItemBinding>>(R.layout.my_calendar_task_list_item, data) {

    override fun convert(holder: BaseDataBindingHolder<MyCalendarTaskListItemBinding>, item: CalendarData.TaskList) {
        holder.dataBinding?.apply {
            taskData = item
            executePendingBindings()

            tvTaskName.text = item.taskName
            tvTaskName.paintFlags = tvTaskName.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            tvTaskName.setSafeOnClickListener {
                /*XPopup.Builder(context).dismissOnTouchOutside(false).isDestroyOnDismiss(false).asCustom(
                    BaseCenterPop(
                        context,
                        onConfirmAction = {
                        },
                        confirmText = context.getString(com.cl.common_base.R.string.string_1398),
                        content = item.articleDetails,
                    )
                ).show()*/
                // 预览任务包
                onPreViewClick?.invoke(item.subTaskList?.get(0), item)
            }
            ivGt.setSafeOnClickListener {
                XPopup.Builder(context).dismissOnTouchOutside(false).isDestroyOnDismiss(false).asCustom(
                    BaseCenterPop(
                        context,
                        onConfirmAction = {
                            // 跳转到InterCome文章详情里面去
                            InterComeHelp.INSTANCE.openInterComeSpace(space = InterComeHelp.InterComeSpace.Article, id = item.articleId)
                        },
                        confirmText = context.getString(com.cl.common_base.R.string.string_1398),
                        content = item.articleDetails,
                    )
                ).show()
            }

            svtGrayUnlock.setSafeOnClickListener {
                // 任务按钮点击，判断是这是什么任务。目前单独提出2个问题，排水、解锁周期。需要单独显示按钮和处理
                onJumpClick?.invoke(item.subTaskList?.get(0), item)
            }

            // 是否显示子任务列表
            ViewUtils.setVisible(item.subPlantList?.isNotEmpty() == true, rvSubTaskList)

            // 初始化rv
            rvSubTaskList.apply {
                layoutManager = LinearLayoutManager(context)
                val suPlantAdapter = SubChooseAdapter(item.subPlantList)
                adapter =  suPlantAdapter
                suPlantAdapter.setList(item.subPlantList)
                suPlantAdapter.addChildClickViewIds(R.id.curing_box, R.id.ivTaskDelete, R.id.ivTaskDelay)
                suPlantAdapter.setOnItemChildClickListener { adapter, view, position ->
                    when (view.id) {
                        R.id.curing_box -> {
                            // 调用完成任务
                            onCompleteClick?.invoke(item.subPlantList?.get(position), item, position, adapter as SubChooseAdapter)
                        }

                        R.id.ivTaskDelete -> {
                            // 调用删除接口
                            onDeleteClick?.invoke(item.subPlantList?.get(position), item, position, adapter as SubChooseAdapter)
                        }

                        R.id.ivTaskDelay -> {
                            // 调用延时接口
                            onDelayClick?.invoke(item.subPlantList?.get(position), item, position, adapter as SubChooseAdapter)
                        }
                    }
                }
            }
        }
    }
}