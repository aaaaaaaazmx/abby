package com.cl.common_base.adapter

import android.text.Editable
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.R
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.bean.GuideInfoData
import com.cl.common_base.constants.UnReadConstants
import com.cl.common_base.databinding.HomeItemCuringPopBinding
import com.cl.common_base.databinding.HomeItemEditPopBinding
import com.cl.common_base.databinding.HomeItemIncubationPopBinding
import com.cl.common_base.databinding.HomeItemPopBinding

/**
 * 种植周期，多type布局
 */
class PlantInitMultiplePopAdapter(
    data: MutableList<GuideInfoData.PlantInfo>?,
    private val dryingEtWeightChange: ((ed:Editable?, position: Int, etView: EditText, typeTwoBox: CheckBox, typeBox: CheckBox, bean: GuideInfoData.PlantInfo, dataList: MutableList<GuideInfoData.PlantInfo>) -> Unit)? = null,
    private val curingEtWeightChange: ((ed:Editable?, position: Int, etView: EditText, typeBox: CheckBox, bean: GuideInfoData.PlantInfo, dataList: MutableList<GuideInfoData.PlantInfo>) -> Unit)? = null
) :
    BaseMultiItemQuickAdapter<GuideInfoData.PlantInfo, BaseViewHolder>(data) {

    init {
        addItemType(UnReadConstants.Plant.KEY_PLANT.toInt(), R.layout.home_item_pop)
        addItemType(UnReadConstants.Plant.KEY_DRYING.toInt(), R.layout.home_item_edit_pop)
        addItemType(UnReadConstants.Plant.KEY_CURING.toInt(), R.layout.home_item_curing_pop)
        addItemType(UnReadConstants.Plant.KEY_INCUBATION.toInt(), R.layout.home_item_incubation_pop)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when ("$holder.itemViewType") {
            UnReadConstants.Plant.KEY_PLANT -> {
                val binding = DataBindingUtil.bind<HomeItemPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            UnReadConstants.Plant.KEY_DRYING -> {
                val binding = DataBindingUtil.bind<HomeItemEditPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            UnReadConstants.Plant.KEY_CURING -> {
                val binding = DataBindingUtil.bind<HomeItemCuringPopBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            UnReadConstants.Plant.KEY_INCUBATION -> {
                val binding = DataBindingUtil.bind<HomeItemIncubationPopBinding>(holder.itemView)
                if (binding!= null) {
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
        }
    }

    override fun convert(holder: BaseViewHolder, item: GuideInfoData.PlantInfo) {
        // 获取 Binding
        when ("${item.isCurrentStatus}") {
            UnReadConstants.Plant.KEY_PLANT -> {

            }
            UnReadConstants.Plant.KEY_DRYING -> {
                val etWeight = holder.getView<EditText>(R.id.et_weight)
                val typeTwoBox = holder.getView<CheckBox>(R.id.type_two_box)
                val typeBox = holder.getView<CheckBox>(R.id.type_box)
                ViewUtils.setEditTextInputSpace(holder.getView(R.id.et_weight))
                etWeight.addTextChangedListener {
                    val check = typeTwoBox.isChecked
                    if (it.isNullOrEmpty()) {
                        typeBox.isChecked = false
                        data[holder.layoutPosition].isCheck = false
                    } else {
                        if (check) typeTwoBox.isChecked = false
                        typeBox.isChecked = true
                        data[holder.layoutPosition].isCheck = true
                    }
                    dryingEtWeightChange?.invoke(it,holder.layoutPosition, etWeight, typeTwoBox, typeBox, data[holder.layoutPosition], data)
                }
            }
            UnReadConstants.Plant.KEY_CURING -> {
                ViewUtils.setEditTextInputSpace(holder.getView(R.id.curing_et_weight))
                val etWeight = holder.getView<EditText>(R.id.curing_et_weight)
                val curingBox = holder.getView<CheckBox>(R.id.curing_box)
                etWeight.addTextChangedListener {
                    curingBox.isChecked = !it.isNullOrEmpty()
                    data[holder.layoutPosition].isCheck = !it.isNullOrEmpty()
                    curingEtWeightChange?.invoke(it,holder.layoutPosition, etWeight, curingBox, data[holder.layoutPosition], data)
                }
            }

        }
    }
}