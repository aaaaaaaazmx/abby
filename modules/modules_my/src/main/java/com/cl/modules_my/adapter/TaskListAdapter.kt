package com.cl.modules_my.adapter

import android.content.Intent
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.underline
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.bean.CalendarData
import com.cl.common_base.constants.Constants
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyTaskListItemBinding

/**
 * 右边布局adapter
 */
class TaskListAdapter(
    data: MutableList<CalendarData.TaskList.SubTaskList>?,
) :
    BaseQuickAdapter<CalendarData.TaskList.SubTaskList, BaseDataBindingHolder<MyTaskListItemBinding>>(R.layout.my_task_list_item, data) {

    override fun convert(holder: BaseDataBindingHolder<MyTaskListItemBinding>, item: CalendarData.TaskList.SubTaskList) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()
        }
        holder.getView<TextView>(R.id.tv_task_name).apply {
            text = buildSpannedString {
                underline {
                    append(item.description)
                }
            }
            /*setOnClickListener {
                // 跳转过去预览
                val intent = Intent(context, BasePopActivity::class.java)
                intent.putExtra(Constants.Global.KEY_TXT_ID, item.textId)
                intent.putExtra(BasePopActivity.KEY_PREVIEW, true)
                context?.startActivity(intent)
            }*/
        }
    }
}