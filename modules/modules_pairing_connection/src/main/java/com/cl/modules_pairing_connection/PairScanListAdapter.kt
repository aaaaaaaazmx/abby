package com.cl.modules_pairing_connection

import androidx.databinding.DataBindingUtil
import com.cl.modules_pairing_connection.databinding.PairBleListItemBinding
import com.cl.modules_pairing_connection.request.PairBleData
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 扫描出来的设备适配器
 *
 * @author 李志军 2022-08-03 22:09
 */
class PairScanListAdapter(data: MutableList<PairBleData>?) :
    BaseQuickAdapter<PairBleData, BaseViewHolder>(R.layout.pair_ble_list_item, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<PairBleListItemBinding>(viewHolder.itemView);
    }

    override fun convert(helper: BaseViewHolder, item: PairBleData) {
        // 获取 Binding
        val binding: PairBleListItemBinding? = helper.getBinding()
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }
    }
}