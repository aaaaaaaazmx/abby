package com.cl.modules_my.adapter

import android.provider.ContactsContract.CommonDataKinds.Relation
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bhm.ble.BleManager
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyDeviceListItemBinding
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.temperatureConversion
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.FeatureItemSwitch
import com.cl.modules_my.databinding.MyDeviceListTextItemBinding
import com.cl.modules_my.databinding.MyPairListItemBinding
import com.cl.modules_my.databinding.MyPairTentInnerItemBinding

class DeviceListAdapter(
    data: MutableList<ListDeviceBean>?,
    private val switchListener: ((accessoryId: String, deviceId: String, isCheck: Boolean, usbPort: String?) -> Unit)? = null,
    private val luoSiListener: ((accessoryData: ListDeviceBean.AccessoryList, bean: ListDeviceBean) -> Unit)? = null,
) :
    BaseMultiItemQuickAdapter<ListDeviceBean, BaseViewHolder>(data) {

    // 是否是公制
    val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)

    init {
        addItemType(ListDeviceBean.KEY_TYPE_TEXT, R.layout.my_device_list_text_item)// 文字描述
        addItemType(ListDeviceBean.KEY_TYPE_BOX, R.layout.my_device_list_item)  // 设备
        addItemType(ListDeviceBean.KEY_TYPE_PH, R.layout.my_pair_list_item)  // ph配件
        addItemType(ListDeviceBean.MONITOR_VIEW_OUT, R.layout.my_pair_tent_inner_item) // 带屏外部温湿度传感器
        addItemType(ListDeviceBean.KEY_MONITOR_OUT, R.layout.my_pair_tent_inner_item) // 不带屏外部温湿度传感器
    }


    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when (holder.itemViewType) {
            ListDeviceBean.KEY_TYPE_TEXT -> {
                val binding = DataBindingUtil.bind<MyDeviceListTextItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }

            ListDeviceBean.MONITOR_VIEW_OUT -> {
                val binding = DataBindingUtil.bind<MyPairTentInnerItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }

            ListDeviceBean.KEY_MONITOR_OUT -> {
                val binding = DataBindingUtil.bind<MyPairTentInnerItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }

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
                runCatching {
                    //  底部的配件、会有多个、改为Recyclview了。
                    holder.getView<RecyclerView>(R.id.rv_accessory).apply {
                        layoutManager = LinearLayoutManager(context)
                        val accessAdapters = AccessAdapter(accList, item.isChooser ?: false, switchListener = { accessoryId, isCheck, usbPort ->
                            switchListener?.invoke(accessoryId, item.deviceId.toString(), isCheck, usbPort)
                        }, item.deviceType)
                        adapter = accessAdapters
                        accessAdapters.addChildClickViewIds(R.id.iv_luosi, R.id.cl_pair)
                        accessAdapters.setOnItemChildClickListener { adapter, view, position ->
                            luoSiListener?.invoke(adapter.data[position] as ListDeviceBean.AccessoryList, item)
                        }
                    }
                }
                holder.setText(R.id.tv_title, showName(item.deviceName, item.plantName, item.strainName))
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

            ListDeviceBean.MONITOR_VIEW_OUT, ListDeviceBean.KEY_MONITOR_OUT -> {
                // 需要根据单位来进行转变
                val temp = temperatureConversion(item.temperature.safeToFloat(), isMetric)
                val tempUnit = if (isMetric) "℃" else "℉"
                val humidity = item.humidity ?: ""
                val humidityUnit = "%"
                val endUnit = "RH"
                logI("123123, : -> $temp, $humidity, $isMetric")
                ViewUtils.setVisible(temp.isNotEmpty() || humidity.isNotEmpty(), holder.getView(R.id.tv_device_status))
                // 分别判断temp和humidity是否为空
                if (temp.isNotEmpty() && humidity.isNotEmpty()) {
                    holder.setText(R.id.tv_device_status, "$temp$tempUnit $humidity$humidityUnit $endUnit")
                } else if (temp.isNotEmpty() && humidity.isEmpty()) {
                    holder.setText(R.id.tv_device_status, "$temp$tempUnit $endUnit")
                } else if (temp.isEmpty() && humidity.isNotEmpty()) {
                    holder.setText(R.id.tv_device_status, "$humidity$humidityUnit $endUnit")
                } else {
                    holder.setText(R.id.tv_device_status, "")
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