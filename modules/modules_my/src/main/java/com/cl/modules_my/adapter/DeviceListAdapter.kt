package com.cl.modules_my.adapter

import android.provider.ContactsContract.CommonDataKinds.Relation
import android.text.TextUtils
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bhm.ble.BleManager
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyDeviceListItemBinding
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.FeatureItemSwitch
import com.cl.modules_my.databinding.MyPairListItemBinding

class DeviceListAdapter(data: MutableList<ListDeviceBean>?, private val switchListener: ((accessoryId: String, deviceId: String, isCheck: Boolean) -> Unit)? = null) :
    BaseMultiItemQuickAdapter<ListDeviceBean, BaseViewHolder>(data) {


    init {
        addItemType(ListDeviceBean.KEY_TYPE_BOX, R.layout.my_device_list_item)  // 舍诶
        addItemType(ListDeviceBean.KEY_TYPE_PH, R.layout.my_pair_list_item)  // 配件
    }


    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when (holder.itemViewType) {
            ListDeviceBean.KEY_TYPE_BOX -> {
                val binding = DataBindingUtil.bind<MyDeviceListItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }

            ListDeviceBean.KEY_TYPE_PH -> {
                val binding = DataBindingUtil.bind<MyPairListItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
        }
    }

    override fun convert(holder: BaseViewHolder, item: ListDeviceBean) {
        when (holder.itemViewType) {
            ListDeviceBean.KEY_TYPE_BOX -> {
                val accList = item.accessoryList ?: mutableListOf()
                if (accList.isNotEmpty()) {
                    val pairData = accList[0]
                    val checkView = holder.getView<FeatureItemSwitch>(R.id.ft_check)
                    val textView = holder.getView<TextView>(R.id.tv_auto_desc)
                    // 配件的相关事件
                    checkView.apply {
                        setSwitchCheckedChangeListener { _, isChecked ->
                            if ((item.accessoryList?.size ?: 0) > 0) {
                                switchListener?.invoke(pairData.accessoryId.toString(), item.deviceId ?: "", isChecked)
                            }
                        }
                    }
                    // 显示checkView & textView
                    val openSize  = pairData.isAuto
                    val status = pairData.status
                    ViewUtils.setVisible(openSize == 0, checkView)
                    ViewUtils.setVisible(openSize != 0, textView)
                    checkView.setItemChecked(status == 1)
                    textView.text =  if (status == 1 && openSize == 1) "Auto\nOn" else "Auto\nOff"
                }


                /**
                 *        val openSize = data?.list?.filter { it.status == 1 }?.size ?: 0
                 *                     ViewUtils.setVisible(openSize == 0, binding.ftCheck)
                 *                     ViewUtils.setVisible(openSize != 0, binding.tvAutoDesc)
                 *                     data?.status?.let {
                 *                         binding.ftCheck.setItemChecked(it == 1)
                 *                     }
                 *                     // 主开关需要开启，才显示
                 *                       binding.tvAutoDesc.text =
                 *                         if (data?.status == 1 && openSize == 1) "Auto\nOn" else "Auto\nOff"
                 */

            }

            ListDeviceBean.KEY_TYPE_PH -> {
                if (BleManager.get().getAllConnectedDevice()?.any { it.deviceName == Constants.Ble.KEY_PH_DEVICE_NAME } == true) {
                    holder.setText(R.id.tv_ble_status, "Connected")
                    holder.setBackgroundColor(R.id.rl_ble_status, ContextCompat.getColor(context, com.cl.common_base.R.color.mainColor))
                } else {
                    holder.setText(R.id.tv_ble_status, "Disconnected")
                    holder.setBackgroundColor(R.id.rl_ble_status, ContextCompat.getColor(context, com.cl.common_base.R.color.textError))
                }
            }
        }

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