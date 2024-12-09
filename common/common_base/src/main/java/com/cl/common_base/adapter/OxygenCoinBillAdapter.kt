package com.cl.common_base.adapter

import android.graphics.Color
import android.text.SpannedString
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.R
import com.cl.common_base.bean.Flowing
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.databinding.MyItemOxyBinding

class OxygenCoinBillAdapter(
    data: MutableList<Flowing>?,
) :
    BaseQuickAdapter<Flowing, BaseDataBindingHolder<MyItemOxyBinding>>(R.layout.my_item_oxy, data) {

    override fun convert(holder: BaseDataBindingHolder<MyItemOxyBinding>, item: Flowing) {
        holder.dataBinding?.apply {
            flowing = item
            executePendingBindings()
        }

        holder.setText(R.id.tv_income, spanText(item.income))
        holder.setText(R.id.tv_expense, spanExpenseText(item.expense))

        holder.getView<RecyclerView>(R.id.rv_item).apply {
            layoutManager = LinearLayoutManager(context)
            //添加自定义分割线
            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(context, com.cl.common_base.R.drawable.custom_divider)!!)
            addItemDecoration(divider)
            val oxygenAdapter = OxygenAdapter(item.list.toMutableList())
            adapter = oxygenAdapter
            oxygenAdapter.addChildClickViewIds(R.id.iv_avatar)
            oxygenAdapter.setOnItemChildClickListener { adapter, view, position ->
                if (view.id == R.id.iv_avatar) {
                    // 跳转到他的资产界面
                    ARouter.getInstance().build(RouterPath.Contact.PAGE_OTHER_JOURNEY)
                        .withString("key_user_id", oxygenAdapter.data[position].rewardUserId)
                        .withString("key_user_name", oxygenAdapter.data[position].tips)
                        .navigation()
                }
            }
            /*ViewUtils.setVisible(!item.list.toMutableList().isNullOrEmpty(), holder.getView(R.id.vvtwo))*/
        }
    }

    private fun spanText(inCome: String?): SpannedString {
        return inCome?.let {
            buildSpannedString {
                append(context.getString(R.string.income))
                bold {
                    color(Color.parseColor("#B22234")) {
                        append(context.getString(R.string.string_oxy_g, "$it"))
                    }
                }
            }
        } ?: SpannedString("")
    }

    private fun spanExpenseText(expense: String?): SpannedString {
        return expense?.let {
            buildSpannedString {
                append(context.getString(R.string.expense))
                bold {
                    color(Color.parseColor("#006241")) {
                        append(context.getString(R.string.string_oxy_g, "$it"))
                    }
                }
            }
        } ?: SpannedString("")
    }
}