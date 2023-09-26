package com.cl.modules_my.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.bean.DeviceDetailInfo
import com.cl.common_base.ext.logI
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyGrowSpaceSetUpItemBinding


class GrowSpaceSetUpAdapter(
    data: MutableList<DeviceDetailInfo>?,
    private val onEditDoAfterAction: ((editable: Editable, editText: EditText, clRoot: ConstraintLayout) -> Unit)? = null
) :
    BaseQuickAdapter<DeviceDetailInfo, BaseDataBindingHolder<MyGrowSpaceSetUpItemBinding>>(
        R.layout.my_grow_space_set_up_item,
        data
    ) {

    // 在convert方法之前设定一个标志
    var shouldHandleTextChange = true
    override fun convert(
        holder: BaseDataBindingHolder<MyGrowSpaceSetUpItemBinding>,
        item: DeviceDetailInfo
    ) {
        shouldHandleTextChange = false
        holder.dataBinding?.apply {
            data = item
            position = holder.layoutPosition
            executePendingBindings()
        }
        shouldHandleTextChange = true

        val oldStrainName = item.strainName // Store old value

        holder.getView<EditText>(R.id.et_strain_plant).apply {
            doAfterTextChanged {
                val newStrainName = it.toString()

                // Check if the value has changed
                if (newStrainName != oldStrainName && shouldHandleTextChange) {
                    if (it != null) {
                        onEditDoAfterAction?.invoke(it, this@apply, holder.getView(R.id.cl_strain_name))
                    }
                }
            }
        }
    }
}