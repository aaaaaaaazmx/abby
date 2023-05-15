package com.cl.modules_my.adapter

import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyDeviceListItemBinding
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.ext.logI
import com.cl.common_base.widget.FeatureItemSwitch
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylist
import com.luck.picture.lib.animators.ViewHelper
import dagger.Reusable

class DeviceListAdapter(data: MutableList<ListDeviceBean>?, private val switchListener: ((accessoryId: String, deviceId: String, isCheck: Boolean) -> Unit)? = null) :
    BaseQuickAdapter<ListDeviceBean, BaseViewHolder>(R.layout.my_device_list_item, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<MyDeviceListItemBinding>(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: ListDeviceBean) {
        // 获取 Binding
        val binding: MyDeviceListItemBinding? = holder.getBinding()
        binding?.apply {
            data = item
            adapter = this@DeviceListAdapter
            executePendingBindings()
        }

        holder.getView<FeatureItemSwitch>(R.id.ft_check).apply {
            setSwitchCheckedChangeListener { buttonView, isChecked ->
                switchListener?.invoke(item.accessoryList?.get(0)?.accessoryId.toString(), item.deviceId ?: "", isChecked)
            }
        }

        holder.setText(R.id.tv_auto_desc, if (item.accessoryList?.get(0)?.isAuto == 1) "Auto\nOn" else "Auto\nOff")
    }

    // 显示名字。
    fun showName(deviceName: String?, planeName: String?, strainName: String?): String {
        if (planeName.isNullOrEmpty() && strainName.isNullOrEmpty()) {
            return deviceName.toString()
        } else if (planeName.isNullOrEmpty() && !strainName.isNullOrEmpty()) {
            return strainName
        } else if (strainName.isNullOrEmpty() && !planeName.isNullOrEmpty()) {
            return planeName
        }
        return deviceName.toString()
    }
}