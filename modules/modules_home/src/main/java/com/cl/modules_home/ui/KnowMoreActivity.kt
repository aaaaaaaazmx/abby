package com.cl.modules_home.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.mtjsoft.barcodescanning.extentions.dp2px
import com.alibaba.android.arouter.facade.annotation.Route
import com.bbgo.module_home.databinding.HomeKnowMoreLayoutBinding
import com.cl.common_base.R
import com.cl.common_base.adapter.HomeKnowMoreAdapter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.FinishTaskReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.easeui.EaseUiHelper
import com.cl.common_base.easeui.ui.videoUiHelp
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.sp2px
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_home.viewmodel.KnowMoreViewModel
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.shuyu.gsyvideoplayer.GSYVideoManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * 统一图文接口
 */
@Route(path = RouterPath.Home.PAGE_KNOW)
@AndroidEntryPoint
class KnowMoreActivity : BaseActivity<HomeKnowMoreLayoutBinding>() {
    @Inject
    lateinit var mViewMode: KnowMoreViewModel

    // 富文本适配器
    private val adapter by lazy {
        HomeKnowMoreAdapter(mutableListOf())
    }
    private val linearLayoutManager by lazy {
        LinearLayoutManager(this@KnowMoreActivity)
    }

    private val txtId by lazy {
        intent.getStringExtra(Constants.Global.KEY_TXT_ID)
    }

    private val txtType by lazy {
        intent.getStringExtra(Constants.Global.KEY_TXT_TYPE)
    }

    private val taskId by lazy {
        intent.getStringExtra(Constants.Global.KEY_TASK_ID)
    }

    override fun initView() {
        binding.ivBack.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        binding.rvKnow.layoutManager = linearLayoutManager
        binding.rvKnow.adapter = adapter
        // mViewMode.getRichText(txtId = "516c590993a041309912ebe16c2eb856")
        // mViewMode.getRichText(txtId = "c3eeb4d2f1332f4869erwqfa912557ae")
        mViewMode.getRichText(txtId = txtId, type = txtType)
    }

    /**
     * 初始化Video
     */
    private fun initVideo(url: String, autoPlay: Boolean) {
        binding.videoItemPlayer.apply {
            videoUiHelp(url, -1)
            if (autoPlay) startPlayLogic()
        }
    }

    override fun observe() {
        mViewMode.apply {
            // 完成任务
            finishTask.observe(this@KnowMoreActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    setResult(Activity.RESULT_OK)
                    this@KnowMoreActivity.finish()
                }
            })

            richText.observe(this@KnowMoreActivity, resourceObserver {
                loading { showProgressLoading() }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()
                    if (null == data) return@success

                    // 初始化头部Video
                    data?.topPage?.firstOrNull { it.type == "video" }?.apply {
                        value?.url?.let { initVideo(it, value?.autoplay == true) }
                    }
                    data?.bar?.let {
                        // todo 设置标题
                        binding.tvTitle.text = it
                    }

                    // 动态添加按钮
                    // 不是video的都需要添加
                    val list = data?.topPage?.filter { it.type != "video" }
                    list?.forEachIndexed { index, topPage ->
                        val tv = TextView(this@KnowMoreActivity)
                        tv.setBackgroundResource(R.drawable.create_state_button)
                        tv.isEnabled = true
                        tv.text = topPage.value?.txt
                        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(60))
                        lp.setMargins(dp2px(20), dp2px(5), dp2px(20), dp2px(5))
                        tv.layoutParams = lp
                        tv.gravity = Gravity.CENTER
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp2px(18f).toFloat())
                        tv.setTextColor(Color.WHITE)
                        binding.flRoot.addView(tv)
                    }
                    binding.flRoot.children.forEach {
                        val tv = (it as? TextView)
                        tv?.setOnClickListener {
                            list?.firstOrNull { data -> data.value?.txt == tv.text.toString() }?.apply {
                                when (type) {
                                    "pageClose" -> this@KnowMoreActivity.finish()
                                    "pageDown" -> {
                                        // 跳转下一页
                                        val intent = Intent(this@KnowMoreActivity, KnowMoreActivity::class.java)
                                        intent.putExtra(Constants.Global.KEY_TXT_ID, value?.txtId)
                                        startActivity(intent)
                                    }
                                    "finishTask" -> {
                                        // 完成任务
                                        mViewMode.finishTask(FinishTaskReq(taskId = taskId))
                                    }
                                }
                            }
                        }

                    }

                    // 适配器设置数据
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
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
                val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
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
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    /**
     * 适配器的点击事件
     */
    private fun adapterClickEvent() {
        adapter.apply {
            addChildClickViewIds(com.cl.common_base.R.id.iv_pic, R.id.tv_html, R.id.tv_learn, R.id.cl_go_url, R.id.cl_support, R.id.cl_discord, R.id.cl_learn)
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
                    R.id.cl_go_url,
                    com.cl.common_base.R.id.tv_html -> {
                        val intent = Intent(context, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_WEB_URL, bean.value?.url)
                        intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, bean.value?.title)
                        context.startActivity(intent)
                    }

                    // 阅读更多
                    R.id.cl_learn,
                    R.id.tv_learn -> {
                        // todo 请求id
                        bean.value?.txtId?.let {
                            // 继续请求弹窗
                            val intent = Intent(context, KnowMoreActivity::class.java)
                            intent.putExtra(Constants.Global.KEY_TXT_ID, it)
                            context.startActivity(intent)
                        }
                    }

                    // 跳转到客服
                    R.id.cl_support -> {
                        // 如果是会员、那么直接跳转过去
                        if (mViewMode.userInfo?.isVip == 1) {
                            // 跳转聊天界面
                            EaseUiHelper.getInstance().startChat(null)
                        } else {
                            // todo 不是会员那么显示弹窗、和日历界面一样
                        }
                    }

                    // 跳转到Discord
                    R.id.cl_discord -> {
                        val intent = Intent(context, WebActivity::class.java)
                        if (bean.value?.url.isNullOrEmpty()) {
                            intent.putExtra(WebActivity.KEY_WEB_URL, "https://discord.gg/FCj6UGCNtU")
                        } else {
                            intent.putExtra(WebActivity.KEY_WEB_URL, bean.value?.url)
                        }
                        intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "hey abby")
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

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
        // 添加背景高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.vvRoot) { v, insets ->
            binding.vvRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                height = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        setResult(Activity.RESULT_OK)
        finish()
    }


    companion object {
        const val KEY_COLLEGE_TXT_ID = "516c590993a041309912ebe16c2eb856"
    }
}