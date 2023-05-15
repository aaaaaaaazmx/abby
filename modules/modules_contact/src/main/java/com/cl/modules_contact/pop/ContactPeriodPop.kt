package com.cl.modules_contact.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ContactPeriodAdapter
import com.cl.modules_contact.databinding.ContactPeriodPopBinding
import com.cl.modules_contact.databinding.ContactReportPopBinding
import com.cl.modules_contact.response.ContactPeriodBean
import com.lxj.xpopup.core.BubbleAttachPopupView
import com.lxj.xpopup.core.CenterPopupView

/**
 * 周期选择弹窗
 */
class ContactPeriodPop(
    context: Context,
    private val onConfirmAction: ((txt: String) -> Unit)? = null,
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.contact_period_pop
    }

    private val adapter by lazy {
        ContactPeriodAdapter(mutableListOf())
    }

    private val list by lazy {
        val list = mutableListOf<ContactPeriodBean>()
        list.add(ContactPeriodBean("Germination", false))
        list.add(ContactPeriodBean("Vegetation", false))
        list.add(ContactPeriodBean("Flowering", false))
        list.add(ContactPeriodBean("Flushing", false))
        list.add(ContactPeriodBean("Drying", false))
        // list.add(ContactPeriodBean("Auto", false))
        list
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<ContactPeriodPopBinding>(popupImplView)?.apply {
            rvPeriod.layoutManager = LinearLayoutManager(context)
            rvPeriod.adapter = this@ContactPeriodPop.adapter
            this@ContactPeriodPop.adapter.setList(list)


            this@ContactPeriodPop.adapter.addChildClickViewIds(R.id.check_period)
            this@ContactPeriodPop.adapter.setOnItemChildClickListener { adapter, view, position ->
                val item = adapter.getItem(position) as ContactPeriodBean
                if (view.id == R.id.check_period) {
                    this@ContactPeriodPop.adapter.data.indexOfFirst {
                        it.isSelected
                    }.apply {
                        if (this != -1) {
                            this@ContactPeriodPop.adapter.data[this].isSelected = false
                            this@ContactPeriodPop.adapter.notifyItemChanged(this)
                        }
                    }
                    item.isSelected = !(item.isSelected ?: false)
                    this@ContactPeriodPop.adapter.notifyItemChanged(position)
                    
                    onConfirmAction?.invoke(item.period)
                    dismiss()
                }
            }
        }
    }
}