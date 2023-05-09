package com.cl.modules_my.adapter

import android.text.SpannedString
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.strikeThrough
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.bean.WallpaperListBean
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyWallItemBinding

class WallAdapter(
    data: MutableList<WallpaperListBean>?,
) : BaseQuickAdapter<WallpaperListBean, BaseDataBindingHolder<MyWallItemBinding>>(R.layout.my_wall_item, data) {

    override fun convert(holder: BaseDataBindingHolder<MyWallItemBinding>, item: WallpaperListBean) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()
        }

        holder.setText(R.id.tv_price, convertText(item))

        when (item.address) {
            "banner01" -> {
                holder.setBackgroundResource(R.id.iv_wall, com.cl.common_base.R.mipmap.banner01)
            }

            "banner02" -> {
                holder.setBackgroundResource(R.id.iv_wall, com.cl.common_base.R.mipmap.banner02)
            }

            "banner03" -> {
                holder.setBackgroundResource(R.id.iv_wall, com.cl.common_base.R.mipmap.banner03)
            }

            else -> {
                Glide.with(context).load(item.address)
                    .into(holder.getView(R.id.iv_wall))
            }
        }
    }

    private fun convertText(item: WallpaperListBean): SpannedString {
        val price = item.price
        val freePrice = item.freePrice
        return if (price == freePrice) {
            buildSpannedString {
                strikeThrough {
                    append("$price")
                }
                append("  Free")
            }
        } else {
            buildSpannedString {
                bold {
                    append("$price")
                }
            }
        }
    }

}