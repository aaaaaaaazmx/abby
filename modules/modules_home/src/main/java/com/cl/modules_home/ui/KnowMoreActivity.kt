package com.cl.modules_home.ui

import android.content.Intent
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bbgo.module_home.databinding.HomeKnowMoreLayoutBinding
import com.cl.common_base.adapter.HomeKnowMoreAdapter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.RichTextData
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.viewmodel.KnowMoreViewModel
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import dagger.hilt.android.AndroidEntryPoint
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import javax.inject.Inject


/**
 * 统一图文接口
 */
@AndroidEntryPoint
class KnowMoreActivity : BaseActivity<HomeKnowMoreLayoutBinding>(), GSYMediaPlayerListener {
    @Inject
    lateinit var mViewMode: KnowMoreViewModel

    // 富文本适配器
    private val adapter by lazy {
        HomeKnowMoreAdapter(mutableListOf())
    }
    private val linearLayoutManager by lazy {
        LinearLayoutManager(this@KnowMoreActivity)
    }

    override fun initView() {
        // EXO模式
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        binding.rvKnow.layoutManager = linearLayoutManager
        binding.rvKnow.adapter = adapter
        // mViewMode.getRichText(txtId = "516c590993a041309912ebe16c2eb856")
        mViewMode.getRichText(txtId = "c3eeb4d2f1332f4869erwqfa912557ae")

        val bu = Button(this@KnowMoreActivity)
        bu.text = "123"
        binding.flRoot.addView(
            bu
        )
    }

    override fun onResume() {
        super.onResume()
        // 添加背景高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.vvRoot) { v, insets ->
            binding.vvRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                height = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }

    override fun observe() {
        mViewMode.apply {
            richText.observe(this@KnowMoreActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    if (null == data) return@success

                    // val list = mutableListOf<>()
                    // data.topPage
                    // 适配器设置数据
                    data?.bar?.let {
                        data?.page?.add(0, RichTextData.Page(type = RichTextData.KEY_BAR, value = RichTextData.Value(txt = it)))
                    }
                    adapter.setList(data?.page)
                }
            })
        }
    }

    override fun initData() {
        scrollListener()
        adapterClickEvent()
    }

    private fun scrollListener() {
        binding.rvKnow.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var firstVisibleItem = 0
            var lastVisibleItem = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                //大于0说明有播放
                if (GSYVideoManager.instance().playPosition >= 0) {
                    //当前播放的位置
                    val position = GSYVideoManager.instance().playPosition
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().playTag == "$position" && (position < firstVisibleItem || position > lastVisibleItem)) {
                        //如果滑出去了上面和下面就是否，和今日头条一样
                        //是否全屏
                        if (!GSYVideoManager.isFullState(this@KnowMoreActivity)) {
                            adapter.data[position].videoPosition = GSYVideoManager.instance().currentPosition
                            // 不释放全部
                            // GSYVideoManager.instance().setListener(this@KnowMoreActivity)
                            // GSYVideoManager.onPause()
                            // 释放全部
                            GSYVideoManager.releaseAllVideos()
                            // adapter.notifyItemChanged(position)
                        }
                    }
                }
            }
        })
    }

    /**
     * 适配器的点击事件
     */
    private fun adapterClickEvent() {
        adapter.apply {
            addChildClickViewIds(com.cl.common_base.R.id.iv_pic, com.cl.common_base.R.id.tv_html)
            setOnItemChildClickListener { _, view, position ->
                val bean = data[position]
                when (view.id) {
                    com.cl.common_base.R.id.iv_pic -> {
                        // 弹出图片
                        XPopup.Builder(context)
                            .asImageViewer(
                                (view as? ImageView),
                                bean.value?.url,
                                SmartGlideImageLoader()
                            )
                            .show()
                    }

                    // 跳转HTML
                    com.cl.common_base.R.id.tv_html -> {
                        val intent = Intent(context, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_WEB_URL, bean.value?.url)
                        intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, bean.value?.title)
                        context.startActivity(intent)
                    }

                }
            }
        }
    }

    override fun HomeKnowMoreLayoutBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@KnowMoreActivity
            viewModel = mViewMode
            executePendingBindings()
        }
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.releaseAllVideos()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }

    override fun onPrepared() {
    }

    override fun onAutoCompletion() {
    }

    override fun onCompletion() {
    }

    override fun onBufferingUpdate(percent: Int) {
    }

    override fun onSeekComplete() {
    }

    override fun onError(what: Int, extra: Int) {
    }

    override fun onInfo(what: Int, extra: Int) {
    }

    override fun onVideoSizeChanged() {
    }

    override fun onBackFullscreen() {
    }

    override fun onVideoPause() {
        // ToastUtil.shortShow("${GSYVideoManager.instance().playPosition}")
    }

    override fun onVideoResume() {
    }

    override fun onVideoResume(seek: Boolean) {
    }

}