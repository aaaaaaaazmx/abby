package com.cl.modules_planting_log.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.modules_planting_log.R
import com.cl.modules_planting_log.adapter.PlantPeriodAdapter
import com.cl.modules_planting_log.databinding.PlantingPeriodPopBinding
import com.cl.modules_planting_log.request.PlantIdByDeviceIdData
import com.lxj.xpopup.core.BubbleAttachPopupView

class PlantIdListPop (
    context: Context,
    private val plantId: Int? = null,
    private val list: MutableList<PlantIdByDeviceIdData>? = null,
    private val onConfirmAction: ((txt: String) -> Unit)? = null,
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.planting_period_pop
    }

    private val adapter by lazy {
        PlantPeriodAdapter(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<PlantingPeriodPopBinding>(popupImplView)?.apply {
            // 找到相同的plantId，并且选为True
            list?.find { it.plantId == plantId }?.isSelected = true

            rvPeriod.layoutManager = LinearLayoutManager(context)
            rvPeriod.adapter = this@PlantIdListPop.adapter
            this@PlantIdListPop.adapter.setList(list)


            this@PlantIdListPop.adapter.addChildClickViewIds(R.id.check_period)
            this@PlantIdListPop.adapter.setOnItemChildClickListener { adapter, view, position ->
                val item = adapter.getItem(position) as PlantIdByDeviceIdData
                if (view.id == R.id.check_period) {
                    this@PlantIdListPop.adapter.data.indexOfFirst {
                        it.isSelected
                    }.apply {
                        if (this != -1) {
                            this@PlantIdListPop.adapter.data[this].isSelected = false
                            this@PlantIdListPop.adapter.notifyItemChanged(this)
                        }
                    }
                    item.isSelected = !(item.isSelected ?: false)
                    this@PlantIdListPop.adapter.notifyItemChanged(position)

                    onConfirmAction?.invoke(item.plantId.toString())
                    dismiss()
                }
            }
        }
    }
}