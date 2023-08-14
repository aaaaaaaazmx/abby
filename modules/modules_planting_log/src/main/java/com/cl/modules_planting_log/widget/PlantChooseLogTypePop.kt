package com.cl.modules_planting_log.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.modules_planting_log.R
import com.cl.modules_planting_log.adapter.PlantChooserLogAdapter
import com.cl.modules_planting_log.databinding.PlantingChooserLogTypeBinding
import com.cl.modules_planting_log.request.PlantLogTypeBean
import com.lxj.xpopup.core.BubbleAttachPopupView

class PlantChooseLogTypePop(
    context: Context,
    private val list: MutableList<PlantLogTypeBean>? = null,
    private val onConfirmAction: ((txt: String) -> Unit)? = null,
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.planting_chooser_log_type
    }

    private val adapter by lazy {
        PlantChooserLogAdapter(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<PlantingChooserLogTypeBinding>(popupImplView)?.apply {
            rvPeriod.layoutManager = LinearLayoutManager(context)
            rvPeriod.adapter = this@PlantChooseLogTypePop.adapter
            this@PlantChooseLogTypePop.adapter.setList(list)


            this@PlantChooseLogTypePop.adapter.addChildClickViewIds(R.id.check_period)
            this@PlantChooseLogTypePop.adapter.setOnItemChildClickListener { adapter, view, position ->
                val item = adapter.getItem(position) as PlantLogTypeBean
                if (view.id == R.id.check_period) {
                    this@PlantChooseLogTypePop.adapter.data.indexOfFirst {
                        it.isSelected
                    }.apply {
                        if (this != -1) {
                            this@PlantChooseLogTypePop.adapter.data[this].isSelected = false
                            this@PlantChooseLogTypePop.adapter.notifyItemChanged(this)
                        }
                    }
                    item.isSelected = !(item.isSelected ?: false)
                    this@PlantChooseLogTypePop.adapter.notifyItemChanged(position)

                    onConfirmAction?.invoke(item.period)
                    dismiss()
                }
            }
        }
    }
}