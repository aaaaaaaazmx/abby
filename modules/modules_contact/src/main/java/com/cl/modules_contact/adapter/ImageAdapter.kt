package com.cl.modules_contact.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cl.common_base.R
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.util.BannerUtils

class ImageAdapter(val imageUrls: List<String>, val context: Context) : BannerAdapter<String, ImageAdapter.ImageHolder>(imageUrls) {


    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ImageHolder {
        val imageView = ImageView(parent!!.context)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        imageView.layoutParams = params
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        //通过裁剪实现圆角
        BannerUtils.setBannerRound(imageView, 20f)
        return ImageHolder(imageView)
    }

    override fun onBindView(holder: ImageHolder, data: String, position: Int, size: Int) {
        Glide.with(holder.itemView)
            .load(data)
            .apply(RequestOptions()).placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            // 图片浏览
            XPopup.Builder(context)
                .asImageViewer(
                    (it as? ImageView),
                    position,
                    imageUrls.toList(),
                    OnSrcViewUpdateListener { _, _ -> },
                    SmartGlideImageLoader()
                ).isShowSaveButton(false)
                .show()
        }
    }


    class ImageHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView = view as ImageView
    }

}
