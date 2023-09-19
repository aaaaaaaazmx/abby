package com.cl.modules_my.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.modules_my.R
import com.cl.modules_my.adapter.MySetUpAdapter
import com.cl.modules_my.databinding.MySetUpPopBinding
import com.cl.modules_my.request.MyPlantInfoData
import com.lxj.xpopup.core.BubbleAttachPopupView

class MySetUpPop(context: Context,
                 private val plantName: String? = null,
                 private val list: MutableList<MyPlantInfoData>? = null,
                 private val onConfirmAction: ((txt: String) -> Unit)? = null,
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_set_up_pop
    }

    private val adapter by lazy {
        MySetUpAdapter(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<MySetUpPopBinding>(popupImplView)?.apply {
            // 找到相同的plantId，并且选为True
            list?.find { it.plantName == plantName }?.isSelected = true

            rvPeriod.layoutManager = LinearLayoutManager(context)
            rvPeriod.adapter = this@MySetUpPop.adapter
            this@MySetUpPop.adapter.setList(list)


            this@MySetUpPop.adapter.addChildClickViewIds(R.id.check_period)
            this@MySetUpPop.adapter.setOnItemChildClickListener { adapter, view, position ->
                val item = adapter.getItem(position) as MyPlantInfoData
                if (view.id == R.id.check_period) {
                    this@MySetUpPop.adapter.data.indexOfFirst {
                        it.isSelected
                    }.apply {
                        if (this != -1) {
                            this@MySetUpPop.adapter.data[this].isSelected = false
                            this@MySetUpPop.adapter.notifyItemChanged(this)
                        }
                    }
                    item.isSelected = !(item.isSelected)
                    this@MySetUpPop.adapter.notifyItemChanged(position)

                    onConfirmAction?.invoke(item.plantName.toString())
                    dismiss()
                }
            }
        }
    }
}