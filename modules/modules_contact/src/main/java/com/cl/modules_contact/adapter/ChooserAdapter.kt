package com.cl.modules_contact.adapter

import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemChooserAddBinding
import com.cl.modules_contact.databinding.ContactItemChooserPicBinding
import com.cl.modules_contact.response.ChoosePicBean

class ChooserAdapter(
    data: MutableList<ChoosePicBean>?,
    private val onItemLongClick: ((iewHolder: BaseViewHolder, position: Int, v: ImageView) -> Unit)? = null
) :
    BaseMultiItemQuickAdapter<ChoosePicBean, BaseViewHolder>(data) {

    init {
        addItemType(ChoosePicBean.KEY_TYPE_ADD, R.layout.contact_item_chooser_add)
        addItemType(ChoosePicBean.KEY_TYPE_PIC, R.layout.contact_item_chooser_pic)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when (holder.itemViewType) {
            ChoosePicBean.KEY_TYPE_ADD -> {
                val binding = DataBindingUtil.bind<ContactItemChooserAddBinding>(holder.itemView)
                // 设置数据
                binding?.executePendingBindings()
            }

            ChoosePicBean.KEY_TYPE_PIC -> {
                val binding = DataBindingUtil.bind<ContactItemChooserPicBinding>(holder.itemView)
                // 设置数据
                binding?.data = data[position]
                binding?.executePendingBindings()
            }
        }
    }

    override fun convert(holder: BaseViewHolder, item: ChoosePicBean) {
        when (holder.itemViewType) {
            ChoosePicBean.KEY_TYPE_ADD -> {
                val binding = DataBindingUtil.bind<ContactItemChooserAddBinding>(holder.itemView)
                // 设置数据
                binding?.executePendingBindings()
            }

            ChoosePicBean.KEY_TYPE_PIC -> {
                holder.getView<ImageView>(R.id.iv_chooser_select).apply {
                    Glide.with(context)
                        .load(item.picAddress)
                        .centerCrop()
                        .placeholder(R.drawable.sp_loading)
                        .into(this)


                    setOnLongClickListener {
                        onItemLongClick?.invoke(holder, holder.layoutPosition, this)
                        true
                    }
                }
            }
        }
    }
}