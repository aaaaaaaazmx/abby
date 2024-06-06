package com.cl.modules_my.adapter

import android.annotation.SuppressLint
import android.widget.RelativeLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.R
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.modules_my.databinding.MyExchangeItemBinding
import com.tuya.smart.android.demo.camera.utils.ToastUtil

class ExchangeAdapter(data: MutableList<String>?, var oxygen: String? = null, var chooserOxygen: ((String)->Unit)? = null) :
    BaseQuickAdapter<String, BaseDataBindingHolder<MyExchangeItemBinding>>(com.cl.modules_my.R.layout.my_exchange_item, data) {

     var newSelectIndex = -1
     var oldSelectIndex = -1

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseDataBindingHolder<MyExchangeItemBinding>, item: String) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()

            val currentOxyGen = oxygen.safeToInt()
            val itemOxygen = item.safeToInt()
            val isSelected = holder.layoutPosition == newSelectIndex
            val isSelectable = currentOxyGen >= itemOxygen

            usbOneFrame.setBackgroundResource(
                when {
                    !isSelectable -> R.drawable.background_button_usb_disable_bord_r180
                    isSelected -> R.drawable.background_button_usb_bord_check_r180
                    else -> R.drawable.background_button_usb_uncheck_bord_r180
                }
            )
            tvOne.setTextColor(
                context.resources.getColor(
                    when {
                        !isSelectable -> R.color.white
                        isSelected -> R.color.white
                        else -> R.color.mainColor
                    }
                )
            )

            rlOne.setSafeOnClickListener {
                if (currentOxyGen < itemOxygen) {
                    // ToastUtil.show(context, "Oxygen is not sufficient")
                    return@setSafeOnClickListener
                }
                if (newSelectIndex == holder.layoutPosition) {
                    return@setSafeOnClickListener
                }

                oldSelectIndex = newSelectIndex
                newSelectIndex = holder.layoutPosition

                chooserOxygen?.invoke(item)
                notifyItemChanged(oldSelectIndex)
                notifyItemChanged(newSelectIndex)
            }
        }
    }
}
