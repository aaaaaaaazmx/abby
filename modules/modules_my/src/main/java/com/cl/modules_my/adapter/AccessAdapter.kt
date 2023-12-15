package com.cl.modules_my.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.bean.AccessoryListBean
import com.cl.common_base.bean.ListDeviceBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.temperatureConversion
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.FeatureItemSwitch
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyAccessItemBinding

class AccessAdapter(data: MutableList<ListDeviceBean.AccessoryList>?, val isChooser: Boolean, private val switchListener: ((accessoryId: String, isCheck: Boolean) -> Unit)? = null) :
    BaseQuickAdapter<ListDeviceBean.AccessoryList, BaseDataBindingHolder<MyAccessItemBinding>>(R.layout.my_access_item, data) {
    val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)

    override fun convert(holder: BaseDataBindingHolder<MyAccessItemBinding>, item: ListDeviceBean.AccessoryList) {
        holder.dataBinding?.apply {
            isChooser = this@AccessAdapter.isChooser
            datas = item
            executePendingBindings()
        }

        val pairData = item
        val isDisplay = (item.accessoryType != AccessoryListBean.KEY_OUTLETS && item.accessoryType != AccessoryListBean.KEY_MONITOR_IN && item.accessoryType != AccessoryListBean.KEY_MONITOR_VIEW_IN)
        holder.setVisible(R.id.rl_check, isDisplay)
        val checkView = holder.getView<FeatureItemSwitch>(R.id.ft_check)
        val textView = holder.getView<TextView>(R.id.tv_auto_desc)
        // 配件的相关事件
        checkView.apply {
            setSwitchCheckedChangeListener { _, isChecked ->
                switchListener?.invoke(pairData.accessoryId.toString(), isChecked)
            }
        }
        // 显示checkView & textView
        val openSize = pairData.isAuto
        val status = pairData.status
        ViewUtils.setVisible(openSize == 0, checkView)
        ViewUtils.setVisible(openSize != 0, textView)
        checkView.setItemChecked(status == 1)
        textView.text = if (status == 1 && openSize == 1) "Auto\nOn" else "Auto\nOff"


        // 需要根据单位来进行转变
        val temp = temperatureConversion(item.temperature.safeToFloat(), isMetric)
        val tempUnit = if (isMetric) "℃" else "℉"
        val humidity = item.humidity ?: ""
        val humidityUnit = "%"
        val endUnit = "RH"
        logI("123123, : -> $temp, $humidity, $isMetric")
        ViewUtils.setVisible(!item.temperature.isNullOrEmpty() || !item.humidity.isNullOrEmpty(), holder.getView(R.id.tv_device_status))
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