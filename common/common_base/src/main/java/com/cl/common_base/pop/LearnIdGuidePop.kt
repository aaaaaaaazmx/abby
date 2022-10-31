package com.cl.common_base.pop

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.cl.common_base.R
import com.cl.common_base.adapter.LearnFinishPopAdapter
import com.cl.common_base.bean.DetailByLearnMoreIdData
import com.cl.common_base.bean.GuideInfoData
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.HomeFinishGuideBinding
import com.cl.common_base.util.glide.GlideEngine
import com.cl.common_base.web.WebActivity
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.entity.LocalMedia
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.SmartGlideImageLoader

/**
 * 种植完成之后的通用弹窗
 */
class LearnIdGuidePop(
    context: Context,
    var list: MutableList<DetailByLearnMoreIdData.ItemBean>? = null,
    var datas: DetailByLearnMoreIdData? = null
) : BottomPopupView(context) {
    private val adapter by lazy {
        LearnFinishPopAdapter(mutableListOf())
    }

    override fun getImplLayoutId(): Int {
        return R.layout.home_finish_guide
    }

    fun setData(data: DetailByLearnMoreIdData?) {
        datas = data
        list = data?.items
        binding?.tvTitle?.text = data?.title
        adapter.setList(list)
    }

    override fun beforeShow() {
        super.beforeShow()
        adapter.setList(list)
        binding?.tvTitle?.text = datas?.title
    }

    var binding: HomeFinishGuideBinding? = null
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<HomeFinishGuideBinding>(popupImplView)?.apply {
            rvList.layoutManager = LinearLayoutManager(context)
            rvList.adapter = adapter

            ivClose.setOnClickListener { dismiss() }

            adapter.addChildClickViewIds(R.id.tv_html, R.id.iv_pic)
            adapter.setOnItemChildClickListener { adapter, view, position ->
                val data = adapter.data[position] as? DetailByLearnMoreIdData.ItemBean
                when (view.id) {
                    // 跳转网络链接
                    R.id.tv_html -> {
                        Constants.videoList.forEach {
                            if (data?.content?.endsWith(it) == true) {
                                val mediaIntent = Intent(Intent.ACTION_VIEW)
                                mediaIntent.setDataAndType(Uri.parse(data.content), "video/*")
                                context?.startActivity(mediaIntent)
                                return@setOnItemChildClickListener
                            }
                        }

                        val intent = Intent(context, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_WEB_URL, data?.content)
                        intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, data?.extend?.title)
                        context?.startActivity(intent)
                    }
                    R.id.iv_pic -> {
                        (adapter.data[position] as? DetailByLearnMoreIdData.ItemBean)?.apply {
                            XPopup.Builder(context)
                                .asImageViewer(
                                    (view as? ImageView),
                                    content,
                                    SmartGlideImageLoader()
                                )
                                .show()
                        }
                    }
                }
            }
        }
    }
}