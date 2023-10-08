package com.cl.modules_my.adapter

import androidx.databinding.DataBindingUtil
import com.bhm.ble.device.BleDevice
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyPairBleItemBinding

class PairBleAdapter(data: MutableList<BleDevice>?) :
    BaseQuickAdapter<BleDevice, BaseViewHolder>(R.layout.my_pair_ble_item, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<MyPairBleItemBinding>(viewHolder.itemView);
    }

    override fun convert(helper: BaseViewHolder, item: BleDevice) {
        // 获取 Binding
        val binding: MyPairBleItemBinding? = helper.getBinding()
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }
    }
}