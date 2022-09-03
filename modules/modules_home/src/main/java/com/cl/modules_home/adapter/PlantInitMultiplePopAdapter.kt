package com.cl.modules_home.adapter

import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.bbgo.module_home.R
import com.bbgo.module_home.databinding.HomeItemCuringPopBinding
import com.bbgo.module_home.databinding.HomeItemEditPopBinding
import com.bbgo.module_home.databinding.HomeItemPopBinding
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.ext.logI
import com.cl.common_base.util.ViewUtils
import com.cl.modules_home.response.GuideInfoData
import okio.blackholeSink

/**
 * 种植周期，多type布局
 */
class PlantInitMultiplePopAdapter(data: MutableList<GuideInfoData.PlantInfo>?) :
    BaseMultiItemQuickAdapter<GuideInfoData.PlantInfo, BaseViewHolder>(data) {

    init {
        addItemType(GuideInfoData.VALUE_STATUS_NORMAL, R.layout.home_item_pop)
        addItemType(GuideInfoData.VALUE_STATUS_DRYING, R.layout.home_item_edit_pop)
        addItemType(GuideInfoData.VALUE_STATUS_CURING, R.layout.home_item_curing_pop)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when (holder.itemViewType) {
            GuideInfoData.VALUE_STATUS_NORMAL -> {
                val binding = DataBindingUtil.bind<HomeItemPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            GuideInfoData.VALUE_STATUS_DRYING -> {
                val binding = DataBindingUtil.bind<HomeItemEditPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            GuideInfoData.VALUE_STATUS_CURING -> {
                val binding = DataBindingUtil.bind<HomeItemCuringPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
        }
    }

    override fun convert(holder: BaseViewHolder, item: GuideInfoData.PlantInfo) {
        // 获取 Binding
        when (item.isCurrentStatus) {
            GuideInfoData.VALUE_STATUS_NORMAL -> {

            }
            GuideInfoData.VALUE_STATUS_DRYING -> {
                val etWeight = holder.getView<EditText>(R.id.et_weight)
                val typeTwoBox = holder.getView<CheckBox>(R.id.type_two_box)
                val typeBox = holder.getView<CheckBox>(R.id.type_box)
                ViewUtils.setEditTextInputSpace(holder.getView(R.id.et_weight))
                etWeight.addTextChangedListener {
                    val check = typeTwoBox.isChecked
                    if (check) typeTwoBox.isChecked = !it.isNullOrEmpty()
                    typeBox.isChecked = it.isNullOrEmpty() == false
                    data[holder.layoutPosition].isCheck = it.isNullOrEmpty() == false
                }
            }
            GuideInfoData.VALUE_STATUS_CURING -> {
                ViewUtils.setEditTextInputSpace(holder.getView(R.id.curing_et_weight))
                val etWeight = holder.getView<EditText>(R.id.curing_et_weight)
                val curingBox = holder.getView<CheckBox>(R.id.curing_box)
                etWeight.addTextChangedListener {
                    if (!it.isNullOrEmpty()) {
                        curingBox.isChecked = false
                    }
                }
            }

        }
    }
}