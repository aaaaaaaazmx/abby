package com.cl.common_base.pop.activity

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
import com.cl.common_base.R
import com.cl.common_base.adapter.HomeKnowMoreAdapter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.FinishTaskReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.BasePopActivityBinding
import com.cl.common_base.easeui.EaseUiHelper
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.ext.sp2px
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.XPopup.getAnimationDuration
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.lxj.xpopup.widget.SmartDragLayout
import com.shuyu.gsyvideoplayer.GSYVideoManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 通用弹窗
 */
@AndroidEntryPoint
class BasePopActivity : BaseActivity<BasePopActivityBinding>() {
    override fun initView() {
        // 添加状态蓝高度
//        ViewCompat.setOnApplyWindowInsetsListener(binding.smart) { v, insets ->
//            binding.smart.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                topMargin = insets.systemWindowInsetTop
//            }
//            return@setOnApplyWindowInsetsListener insets
//        }
        binding.smart.setDuration(getAnimationDuration())
        binding.smart.enableDrag(true)
        binding.smart.dismissOnTouchOutside(false)
        binding.smart.isThreeDrag(false)
        binding.smart.open()
        binding.smart.setOnCloseListener(callback)

         binding.ivClose.setOnClickListener {  acFinish() }
    }

    private val callback by lazy {
        object : SmartDragLayout.OnCloseListener {
            override fun onClose() {
                acFinish()
            }

            override fun onDrag(y: Int, percent: Float, isScrollUp: Boolean) {
                // binding.smart.alpha = percent
            }

            override fun onOpen() {
            }
        }
    }

    override fun observe() {
        mViewModel.apply {
            richText.observe(this@BasePopActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    hideProgressLoading()
                }
                success {
                    hideProgressLoading()
                    if (null == data) return@success

                    // 标题
                    data.bar?.let {
                        binding.tvTitle.text = it
                    }

                    // 动态添加按钮
                    // 不是video的都需要添加
                    val list = data?.topPage?.filter { it.type != "video" }
                    list?.forEachIndexed { index, topPage ->
                        val tv = TextView(this@BasePopActivity)
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
                                    "pageClose" -> this@BasePopActivity.acFinish()
                                    "pageDown" -> {
                                        // 跳转下一页
                                        val intent = Intent(this@BasePopActivity, BasePopActivity::class.java)
                                        intent.putExtra(Constants.Global.KEY_TXT_ID, value?.txtId)
                                        startActivity(intent)
                                    }
                                    "finishTask" -> {
                                        // 完成任务
                                        mViewModel.finishTask(FinishTaskReq(taskId = taskId))
                                    }
                                }
                            }
                        }

                    }

                    // 适配器设置数据
                    adapter.setList(data?.page)
                }
            })

            // 完成任务
            finishTask.observe(this@BasePopActivity, resourceObserver {
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }

                success {
                    hideProgressLoading()
                }
            })
        }
    }

    override fun initData() {
        mViewModel.getRichText(txtId = txtId, type = txtType)

        binding.rvList.layoutManager = linearLayoutManager
        binding.rvList.adapter = adapter

        scrollListener()
        adapterClickEvent()
    }

    private fun scrollListener() {
        binding.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                        if (!GSYVideoManager.isFullState(this@BasePopActivity)) {
                            adapter.data[position].videoPosition = GSYVideoManager.instance().currentPosition
                            // 不释放全部
                            // GSYVideoManager.instance().setListener(this@KnowMoreActivity)
                            // GSYVideoManager.onPause()
                            // 释放全部
                            GSYVideoManager.releaseAllVideos()
                            adapter.notifyItemChanged(position)
                        }
                    }
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

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
                            val intent = Intent(context, BasePopActivity::class.java)
                            intent.putExtra(Constants.Global.KEY_TXT_ID, it)
                            context.startActivity(intent)
                        }
                    }

                    // 跳转到客服
                    R.id.cl_support -> {
                        // 如果是会员、那么直接跳转过去
                        if (mViewModel.userInfo?.isVip == 1) {
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

    override fun BasePopActivityBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@BasePopActivity
            viewModel = mViewModel
            executePendingBindings()
        }
    }

    // 富文本适配器
    private val adapter by lazy {
        HomeKnowMoreAdapter(mutableListOf())
    }
    private val linearLayoutManager by lazy {
        LinearLayoutManager(this@BasePopActivity)
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

    @Inject
    lateinit var mViewModel: BaseViewModel


    // 系统返回键
    override fun onBackPressed() {
        acFinish()
    }

    // 关闭页面的回调
    private fun acFinish() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}