package com.cl.common_base.adapter

import android.graphics.Color
import android.text.SpannedString
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.R
import com.cl.common_base.bean.Flowing
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
            adapter = OxygenAdapter(item.list.toMutableList())
            /*ViewUtils.setVisible(!item.list.toMutableList().isNullOrEmpty(), holder.getView(R.id.vvtwo))*/
        }
    }

    private fun spanText(inCome: String?): SpannedString {
        return inCome?.let {
            buildSpannedString {
                append("Income ")
                bold {
                    color(Color.parseColor("#B22234")) {
                        if (it.startsWith("-")) {
                            append("$it g")
                        } else {
                            append("+$it g")
                        }
                    }
                }
            }
        } ?: SpannedString("")
    }

    private fun spanExpenseText(expense: String?): SpannedString {
        return expense?.let {
            buildSpannedString {
                append("Expense ")
                bold {
                    color(Color.parseColor("#006241")) {
                        if (it.startsWith("-")) {
                            append("$it g")
                        } else {
                            append("+$it g")
                        }
                    }
                }
            }
        } ?: SpannedString("")
    }
}