package com.cl.modules_my.adapter

import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.bean.AutomationListBean
import com.cl.common_base.widget.FeatureItemSwitch
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyDeviceAutoListItemBinding

class DeviceAutomationAdapter(data: MutableList<AutomationListBean.AutoBean>?, private val switchListener: ((automationId: String, isCheck: Boolean) -> Unit)? = null) :
    BaseQuickAdapter<AutomationListBean.AutoBean, BaseViewHolder>(R.layout.my_device_auto_list_item, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<MyDeviceAutoListItemBinding>(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: AutomationListBean.AutoBean) {
        // 获取 Binding
        val binding: MyDeviceAutoListItemBinding? = holder.getBinding()
        binding?.apply {
            data = item
            adapter = this@DeviceAutomationAdapter
            executePendingBindings()
        }

        holder.getView<FeatureItemSwitch>(R.id.ft_check).apply {
            setSwitchCheckedChangeListener { _, isChecked ->
                switchListener?.invoke(item.automationId.toString(), isChecked)
            }
        }
    }
}