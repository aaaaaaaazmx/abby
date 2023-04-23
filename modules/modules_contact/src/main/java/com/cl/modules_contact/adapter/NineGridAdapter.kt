package com.cl.modules_contact.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.cl.common_base.ext.logI
import com.cl.modules_contact.R
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.widget.NineGridView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader

class NineGridAdapter(
    private val context: Context? = null,
    private val urlList: MutableList<String>,
) : NineGridView.Adapter() {

    override fun getItemCount(): Int {
        return urlList.size
    }

    /**
     * 不能发视频
     */
    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_IMAGE
    }

    override fun onCreateItemView(parent: ViewGroup, viewType: Int): View {
        val layoutInflater = LayoutInflater.from(parent.context)
        if (viewType == VIEW_TYPE_VIDEO) {
            return layoutInflater.inflate(R.layout.item_video, parent, false)
        }
        return layoutInflater.inflate(R.layout.item_image, parent, false)
    }

    override fun onBindItemView(itemView: View, viewType: Int, position: Int) {
        when (viewType) {
            VIEW_TYPE_VIDEO -> {
                itemView.findViewById<ImageView>(R.id.ivPlay)?.setOnClickListener {
                    logI("播放视频")
                }
            }

            VIEW_TYPE_IMAGE -> {
                /**
                 * 需要预览图片
                 */
                val imageView = itemView.findViewById<ImageView>(R.id.imageView)
                Glide.with(itemView)
                    .load(urlList[position])
                    .centerCrop()
                    .placeholder(R.drawable.sp_loading)
                    .into(imageView)
                itemView.setOnClickListener {
                    // 图片浏览
                    XPopup.Builder(context)
                        .asImageViewer(
                            (it as? ImageView),
                            position,
                            urlList.toList(),
                            OnSrcViewUpdateListener { _, _ ->  },
                            SmartGlideImageLoader()
                        )
                        .show()
                }
            }
        }
    }

    //
    override fun onCreateSingleView(parent: ViewGroup, viewType: Int): View? {
        return when (viewType) {
            VIEW_TYPE_VIDEO -> {
                return LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
            }

            VIEW_TYPE_IMAGE -> {
                return LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
            }

            else -> {
                LayoutInflater.from(parent.context).inflate(R.layout.item_single, parent, false)
            }
        }
    }

    //
    override fun onCreateExtraView(parent: ViewGroup, viewType: Int): View? {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_extra, parent, false)
    }

    override fun onBindExtraView(extraView: View, viewType: Int, position: Int) {
        val tvExtra = extraView.findViewById<TextView>(R.id.tvExtra)
        val extraCount = urlList.size - position
        tvExtra.text = String.format("+%s", extraCount)
        extraView.setOnClickListener {
            logI("ExtraView click itemSize = ${urlList.size}")
        }
    }

    override fun onBindSingleView(singleView: View, viewType: Int, position: Int) {
        when (viewType) {
            VIEW_TYPE_VIDEO -> {
                singleView.findViewById<ImageView>(R.id.ivPlay)?.setOnClickListener {
                    logI("播放视频")
                }
            }

            VIEW_TYPE_IMAGE -> {
                /**
                 * 点击图片、需要弹出来预览
                 */
                val imageView = singleView.findViewById<ImageView>(R.id.imageView)
                Glide.with(singleView)
                    .load(urlList[position])
                    .centerCrop()
                    .placeholder(R.drawable.sp_loading)
                    .into(imageView)
                singleView.setOnClickListener {
                    // 图片浏览
                    XPopup.Builder(context)
                        .asImageViewer(
                            (it as? ImageView),
                            position,
                            urlList.toList(),
                            OnSrcViewUpdateListener { _, _ ->  },
                            SmartGlideImageLoader()
                        )
                        .show()
                }
            }
        }
    }


    companion object {
        const val VIEW_TYPE_IMAGE = 1
        const val VIEW_TYPE_VIDEO = 2
    }
}