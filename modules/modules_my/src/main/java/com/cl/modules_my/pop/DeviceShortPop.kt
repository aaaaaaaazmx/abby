package com.cl.modules_my.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.BaseApplication
import com.cl.modules_my.R
import com.cl.modules_my.adapter.DeviceShortsAdapter
import com.cl.modules_my.databinding.MyShortPopBinding
import com.cl.modules_my.request.DeviceShortBean
import com.lxj.xpopup.core.BubbleAttachPopupView

/**
 * 周期选择弹窗
 */
class DeviceShortPop(
    context: Context,
    private val onConfirmAction: ((txt: String) -> Unit)? = null,
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_short_pop
    }

    private val adapter by lazy {
        DeviceShortsAdapter(mutableListOf())
    }

    private val list by lazy {
        val list = mutableListOf<DeviceShortBean>()
        // Sort by Name
        // Sort by Strain
        // Sort by Status
        // Sort by Subscription
        list.add(DeviceShortBean(BaseApplication.getContext().getString(R.string.my_sort_name), false))
        list.add(DeviceShortBean(BaseApplication.getContext().getString(R.string.my_sort_strain), false))
        list.add(DeviceShortBean(BaseApplication.getContext().getString(R.string.my_sort_status), false))
        list.add(DeviceShortBean(BaseApplication.getContext().getString(R.string.my_sort_subscription), false))
        // list.add(DeviceShortBean("Auto", false))
        list
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<MyShortPopBinding>(popupImplView)?.apply {
            rvPeriod.layoutManager = LinearLayoutManager(context)
            rvPeriod.adapter = this@DeviceShortPop.adapter
            this@DeviceShortPop.adapter.setList(list)


            this@DeviceShortPop.adapter.addChildClickViewIds(R.id.check_period)
            this@DeviceShortPop.adapter.setOnItemChildClickListener { adapter, view, position ->
                val item = adapter.getItem(position) as DeviceShortBean
                if (view.id == R.id.check_period) {
                    this@DeviceShortPop.adapter.data.indexOfFirst {
                        it.isSelected
                    }.apply {
                        if (this != -1) {
                            this@DeviceShortPop.adapter.data[this].isSelected = false
                            this@DeviceShortPop.adapter.notifyItemChanged(this)
                        }
                    }
                    item.isSelected = !(item.isSelected ?: false)
                    this@DeviceShortPop.adapter.notifyItemChanged(position)
                    
                    onConfirmAction?.invoke(item.period)
                    dismiss()
                }
            }
        }
    }
}