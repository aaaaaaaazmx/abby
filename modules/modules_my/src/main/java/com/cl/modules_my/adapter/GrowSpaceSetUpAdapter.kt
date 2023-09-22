package com.cl.modules_my.adapter

import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.bean.DeviceDetailInfo
import com.cl.common_base.ext.logI
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyGrowSpaceSetUpItemBinding


class GrowSpaceSetUpAdapter(data: MutableList<DeviceDetailInfo>?) :
    BaseQuickAdapter<DeviceDetailInfo, BaseDataBindingHolder<MyGrowSpaceSetUpItemBinding>>(
        R.layout.my_grow_space_set_up_item,
        data
    ) {
    override fun convert(
        holder: BaseDataBindingHolder<MyGrowSpaceSetUpItemBinding>,
        item: DeviceDetailInfo
    ) {
        holder.dataBinding?.apply {
            data = item
            position = holder.layoutPosition
            executePendingBindings()
        }

        /*holder.getView<EditText>(R.id.et_space_plant).apply {
            doAfterTextChanged {
                data[holder.layoutPosition].plantName = it.toString()
                logI("1231plantName: ${data[holder.layoutPosition].plantName}")
            }
        }*/

        holder.getView<EditText>(R.id.et_strain_plant).apply {
            /*doAfterTextChanged {
                data[holder.layoutPosition].strainName = it.toString()
                logI("1231strainName: ${data[holder.layoutPosition].strainName}")
            }*/
        }
    }
}