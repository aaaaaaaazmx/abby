package com.cl.modules_home.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.xpopup
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.pop.TimePickerPop
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeDripItemBinding
import com.cl.modules_home.databinding.HomePlantItemBinding
import com.cl.modules_home.request.DripListData
import com.cl.modules_home.request.Task.PlantList

class PlantListAdapter(item: MutableList<PlantList>?) :
    BaseQuickAdapter<PlantList, BaseDataBindingHolder<HomePlantItemBinding>>(R.layout.home_plant_item, item) {

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseDataBindingHolder<HomePlantItemBinding>, item: PlantList) {
        holder.dataBinding?.apply {
            executePendingBindings()

            container.background = if (item.isSelect == true) context.getDrawable(com.cl.common_base.R.drawable.background_button_main_color_r100) else context.getDrawable(com.cl.common_base.R.drawable.background_button_white_r100)
            textView.text = item.growSpaceName
            textView.setTextColor(if(item.isSelect == true) context.getColor(com.cl.common_base.R.color.white) else context.getColor(com.cl.common_base.R.color.mainColor))
        }
    }
}