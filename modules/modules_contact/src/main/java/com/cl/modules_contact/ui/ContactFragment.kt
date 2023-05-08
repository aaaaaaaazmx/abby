package com.cl.modules_contact.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.CheckBox
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import cn.mtjsoft.barcodescanning.utils.SoundPoolUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.refresh.ClassicsHeader
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.TagsAdapter
import com.cl.modules_contact.adapter.TrendListAdapter
import com.cl.modules_contact.databinding.FragmentContactBinding
import com.cl.modules_contact.pop.CommentPop
import com.cl.modules_contact.pop.ContactEnvPop
import com.cl.modules_contact.pop.ContactPeriodPop
import com.cl.modules_contact.pop.ContactPotionPop
import com.cl.modules_contact.pop.ContactReportPop
import com.cl.modules_contact.pop.RewardPop
import com.cl.modules_contact.request.ContactEnvData
import com.cl.modules_contact.request.DeleteReq
import com.cl.modules_contact.request.LikeReq
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.request.ReportReq
import com.cl.modules_contact.request.RewardReq
import com.cl.modules_contact.request.SyncTrendReq
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.response.TagsBean
import com.cl.modules_contact.viewmodel.ContactViewModel
import com.cl.modules_contact.widget.emoji.BitmapProvider
import com.cl.modules_contact.widget.nineview.NineGridImageView
import com.cl.modules_contact.widget.nineview.OnImageItemClickListener
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.lxj.xpopup.util.XPopupUtils
import com.scwang.smart.refresh.footer.ClassicsFooter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * 联系人首页
 */
@Route(path = RouterPath.Contact.PAGE_CONTACT)
@AndroidEntryPoint
class ContactFragment : BaseFragment<FragmentContactBinding>(), OnImageItemClickListener {
    @Inject
    lateinit var mViewMode: ContactViewModel

    // 朋友圈适配器
    private val adapter by lazy {
        TrendListAdapter(mutableListOf(), this@ContactFragment)
    }

    // 标签适配器
    private val tagAdapter by lazy {
        TagsAdapter(mutableListOf())
    }

    override fun initView(view: View) {
        // 接口调用
        mViewMode.tagList()
        mViewMode.getNewPage(NewPageReq(current = 1, size = 10))

        binding.superLikeLayout.provider = BitmapProvider.Builder(context)
            .setDrawableArray(
                intArrayOf(
                    R.mipmap.emoji_one,
                    R.mipmap.emoji_two,
                    R.mipmap.emoji_three,
                    R.mipmap.emoji_four,
                    R.mipmap.emoji_five,
                    R.mipmap.emoji_six,
                )
            )
            .build()

        // 数量的显示
        ViewUtils.setVisible(mViewMode.userinfoBean?.eventCount != 0, binding.vvMsgNumber)
        ViewUtils.setVisible(TextUtils.isEmpty(mViewMode.userinfoBean?.avatarPicture), binding.noheadShow)
        ViewUtils.setVisible(!TextUtils.isEmpty(mViewMode.userinfoBean?.avatarPicture), binding.ivAvatar)
        context?.let {
            Glide.with(it).load(mViewMode.userinfoBean?.avatarPicture)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivAvatar)
        }
        binding.vvMsgNumber.text = (mViewMode.userinfoBean?.eventCount ?: 0).toString()
        binding.noheadShow.text = mViewMode.userinfoBean?.nickName?.substring(0, 1)

        binding.rvWxCircle.apply {
            /* (itemAnimator as? SimpleItemAnimator)?.apply {
                 changeDuration = 0
                 supportsChangeAnimations = false
             }*/
            layoutManager = LinearLayoutManager(activity)
            // 添加分割线
            //添加自定义分割线
            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.custom_divider)!!)
            addItemDecoration(divider)
            /*this@ContactFragment.adapter.setHasStableIds(true)*/
            this@ContactFragment.adapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn)
            adapter = this@ContactFragment.adapter
        }

        binding.rvTags.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            adapter = this@ContactFragment.tagAdapter
        }

        // refresh
        binding.refreshLayout.apply {
            ClassicsFooter.REFRESH_FOOTER_LOADING = "Updating" //"正在刷新...";
            ClassicsFooter.REFRESH_FOOTER_REFRESHING = "Updating" //"正在加载...";
            ClassicsFooter.REFRESH_FOOTER_NOTHING = "No more data"
            ClassicsFooter.REFRESH_FOOTER_FINISH = "Loading completed"
            ClassicsFooter.REFRESH_FOOTER_FAILED = "Loading failed"

            // 刷新监听
            setOnRefreshListener {
                // 重新加载数据
                logI("setOnRefreshListener: refresh")
                mViewMode.updateCurrent(1)
                mViewMode.getNewPage(NewPageReq(current = mViewMode.updateCurrent.value, size = REFRESH_SIZE))
            }
            // 加载更多监听
            setOnLoadMoreListener {
                val current = (mViewMode.updateCurrent.value ?: 1) + 1
                logI("setOnLoadMoreListener: loadMore Current : $current")
                mViewMode.updateCurrent(current)
                mViewMode.getNewPage(NewPageReq(current = current, size = REFRESH_SIZE))
            }
            // 刷新头部局
            setRefreshHeader(ClassicsHeader(context))
            setRefreshFooter(ClassicsFooter(context).setFinishDuration(0))
            // 刷新高度
            setHeaderHeight(60f)
            // 自动刷新
            // autoRefresh()
        }

        // adapter条目点击时间
        initAdapterClick()
        initClick()
    }

    private val choosePeriodPop by lazy {
        XPopup.Builder(context).popupPosition(PopupPosition.Bottom).dismissOnTouchOutside(true).isClickThrough(false)  //点击透传
            .hasShadowBg(true) // 去掉半透明背景
            //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
            .atView(binding.tvTrend).isCenterHorizontal(false).asCustom(context?.let {
                ContactPeriodPop(it, onConfirmAction = { period ->
                    mViewMode.updateCurrent(1)
                    mViewMode.updateCurrentPeriod(period = period)
                    // 需要清空当前选中的tags,并且刷新
                    tagAdapter.data.indexOfFirst { it.isSelected }.apply {
                        if (this != -1) {
                            tagAdapter.data[this].isSelected = false
                            tagAdapter.notifyItemChanged(this)
                        }
                    }
                    mViewMode.getNewPage(
                        NewPageReq(
                            current = mViewMode.updateCurrent.value, size = REFRESH_SIZE, period = mViewMode.currentPeriod.value
                        )
                    )
                }).setBubbleBgColor(Color.WHITE) //气泡背景
                    .setArrowWidth(XPopupUtils.dp2px(context, 6f)).setArrowHeight(
                        XPopupUtils.dp2px(
                            context, 6f
                        )
                    ) //.setBubbleRadius(100)
                    .setArrowRadius(
                        XPopupUtils.dp2px(
                            context, 3f
                        )
                    )
            })

    }
    private fun initClick() {
        binding.tvTrend.setOnClickListener {
            // 弹窗显示植物周期
            choosePeriodPop.show()
        }

        // floatbutton
        binding.flButton.setOnClickListener {
            // 跳转到发布动态页面
            // ToastUtil.shortShow("FLAT")
            context?.let {
                startActivityLauncher.launch(Intent(it, PostActivity::class.java))
            }
        }

        // 头像点击
        binding.clAvatar.setOnClickListener {
            // 个人头像点击啊
            context?.let {
                startActivity(Intent(it, MyJourneyActivity::class.java))
            }
        }

        // 消息点击
        binding.ivBells.setOnClickListener {
            // 消息点击啊
            context?.let {
                it.startActivity(Intent(it, ContactNotificationActivity::class.java))
            }
        }
    }

    /**
     * 条目点击事件
     */
    private fun initAdapterClick() {
        adapter.addChildClickViewIds(R.id.tv_link, R.id.cl_avatar, R.id.cl_env, R.id.cl_love, R.id.cl_gift, R.id.cl_chat, R.id.rl_point, R.id.tv_to_chat)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as? NewPageData.Records
            mViewMode.updateCurrentPosition(position)
            when (view.id) {
                R.id.tv_link -> {
                    // 跳转网页
                    val intent = Intent(context, WebActivity::class.java)
                    intent.putExtra(WebActivity.KEY_WEB_URL, item?.link)
                    context?.startActivity(intent)
                }

                R.id.cl_avatar -> {
                    // todo 点击头像、跳转到自己的空间， 用userID来区别是跳转到自己的，还是别人的
                }

                R.id.cl_env -> {
                    // 点击环境信息
                    val envInfoData = GSON.parseObjectList(item?.environment, ContactEnvData::class.java).toMutableList()
                    // 弹出环境信息
                    XPopup.Builder(context).dismissOnTouchOutside(false).isDestroyOnDismiss(false).asCustom(context?.let { ContactEnvPop(it, envInfoData, item?.nickName, item?.avatarPicture) }).show()
                }

                R.id.cl_love -> {
                    // 点赞
                    if (item?.isPraise == 0) {
                        mViewMode.like(LikeReq(learnMoreId = item.learnMoreId, likeId = item.id.toString(), type = "moments"))
                    } else {
                        mViewMode.unlike(LikeReq(learnMoreId = item?.learnMoreId, likeId = item?.id.toString(), type = "moments"))
                    }

                    //  点赞效果
                    val itemPosition = IntArray(2)
                    val superLikePosition = IntArray(2)
                    view.getLocationOnScreen(itemPosition)
                    binding.superLikeLayout.getLocationOnScreen(superLikePosition)
                    val x: Int = itemPosition[0] + view.width / 2
                    val y: Int = itemPosition[1] - superLikePosition[1] + view.height / 2
                    logI("x = $x, y = $y")
                    logI("width = ${view.width}, height = ${view.height}")
                    binding.superLikeLayout.launch(x, y)

                    // 震动
                    SoundPoolUtil.instance.startVibrator(context = context)
                }

                R.id.cl_gift -> {
                    //  打赏
                    if (item?.userId == mViewMode.userinfoBean?.userId) {
                        extracted(view.findViewById(R.id.curing_box_gift))
                        return@setOnItemChildClickListener
                    }
                    XPopup.Builder(context)
                        .isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(true)
                        .asCustom(
                            context?.let {
                                RewardPop(it, onRewardListener = { oxygenNum ->
                                    mViewMode.updateRewardOxygen(oxygenNum.toInt())
                                    mViewMode.reward(
                                        RewardReq(
                                            momentsId = item?.id.toString(),
                                            oxygenNum = oxygenNum,
                                            type = ContactCommentActivity.KEY_MOMENTS,
                                            relationId = item?.id.toString()
                                        )
                                    )
                                })
                            }
                        ).show()
                }

                R.id.cl_chat -> {
                    // 聊天
                    toCommentPop(item, position, adapter)
                }

                R.id.rl_point -> {
                    // 点击三个点
                    XPopup.Builder(context)
                        .popupPosition(PopupPosition.Left)
                        .dismissOnTouchOutside(true)
                        .isClickThrough(false)  //点击透传
                        .hasShadowBg(true) // 去掉半透明背景
                        //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                        .atView(view)
                        .isCenterHorizontal(false)
                        .asCustom(
                            context?.let {
                                ContactPotionPop(
                                    it,
                                    deleteAction = {
                                        //  删除
                                        mViewMode.delete(DeleteReq(momentId = item?.id.toString()))
                                    },
                                    reportAction = {
                                        // 举报弹窗
                                        XPopup.Builder(it)
                                            .isDestroyOnDismiss(false)
                                            .dismissOnTouchOutside(false)
                                            .asCustom(
                                                ContactReportPop(
                                                    it,
                                                    onConfirmAction = { txt ->
                                                        // 举报
                                                        mViewMode.report(ReportReq(momentId = item?.id.toString(), reportContent = txt))
                                                    })
                                            ).show()
                                    },
                                    itemSwitchAction = { isCheck ->
                                        // 关闭分享
                                        mViewMode.public(SyncTrendReq(syncTrend = if (isCheck) 1 else 0, momentId = item?.id.toString()))
                                    },
                                    isShowReport = item?.userId.toString() == mViewMode.userinfoBean?.userId
                                )
                                    .setBubbleBgColor(Color.WHITE) //气泡背景
                                    .setArrowWidth(XPopupUtils.dp2px(context, 3f))
                                    .setArrowHeight(
                                        XPopupUtils.dp2px(
                                            context,
                                            3f
                                        )
                                    )
                                    //.setBubbleRadius(100)
                                    .setArrowRadius(
                                        XPopupUtils.dp2px(
                                            context,
                                            2f
                                        )
                                    )
                            }
                        ).show()
                }

                R.id.tv_to_chat -> {
                    //  跳转到更多聊天记录弹窗
                    toCommentPop(item, position, adapter)
                }
            }
        }


        tagAdapter.addChildClickViewIds(R.id.check_tag)
        tagAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as? TagsBean
            when (view.id) {
                R.id.check_tag -> {
                    tagAdapter.data.indexOfFirst {
                        it.isSelected
                    }.apply {
                        if (this != -1) {
                            tagAdapter.data[this].isSelected = false
                            tagAdapter.notifyItemChanged(this)
                        }
                    }
                    item?.isSelected = !(item?.isSelected ?: false)
                    tagAdapter.notifyItemChanged(position)

                    // 选中之后需要刷新动态
                    mViewMode.updateCurrent(1)
                    mViewMode.getNewPage(
                        NewPageReq(
                            current = mViewMode.updateCurrent.value,
                            size = REFRESH_SIZE,
                            period = mViewMode.currentPeriod.value,
                            tags = item?.number
                        )
                    )
                }
            }
        }
    }

    private fun toCommentPop(item: NewPageData.Records?, position: Int, adapter: BaseQuickAdapter<*, *>) {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .dismissOnTouchOutside(false)
            .moveUpToKeyboard(false)
            .maxHeight((XPopupUtils.getScreenHeight(context) * 0.9f).toInt())
            .asCustom(
                context?.let {
                    CommentPop(it, item?.id, onDismissAction = { commentListData ->
                        // 更新当前position
                        val commentsList = this@ContactFragment.adapter.data[position].comments
                        if (commentListData?.size == 0) return@CommentPop
                        if (commentListData?.size == commentsList?.size) return@CommentPop
                        // 实行替换操作
                        val newCommentsList = mutableListOf<NewPageData.Records.Comments>()
                        commentListData?.forEach { data ->
                            val comment = NewPageData.Records.Comments()
                            comment.commentName = data.commentName
                            comment.comment = data.comment
                            newCommentsList.add(comment)
                        }
                        // 更新聊天数目集合
                        this@ContactFragment.adapter.data[position].comments = newCommentsList
                        // 更新聊天数量
                        this@ContactFragment.adapter.data[position].comment = commentListData?.size
                        adapter.notifyItemChanged(position)
                    })
                }
            ).show()
    }


    /**
     * 差值器 左右抖动 动画
     */
    private fun extracted(checkBox: CheckBox) {
        ObjectAnimator.ofFloat(checkBox, "translationX", 0f, -20f, 0f, 20f, 0f).apply {
            duration = 100
            repeatCount = 3
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.RESTART
        }.start()
    }

    override fun lazyLoad() {
    }

    override fun observe() {
        mViewMode.apply {
            // 获取标签列表
            tagListData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success {
                    val list = mutableListOf<TagsBean>()
                    data?.forEachIndexed { _, s ->
                        list.add(TagsBean(s, false))
                    }
                    tagAdapter.setList(list)
                }
            })

            // 获取聊天评论列表
            commentListData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                }
            })
            // 打赏
            rewardData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    val oxygenNum = mViewMode.rewardOxygen.value
                    val position = mViewMode.currentPosition.value ?: -1
                    if (oxygenNum == 0) return@success
                    if (position == -1) return@success

                    val data = adapter.data[position]
                    data.reward = oxygenNum?.let { data.reward?.plus(it) }
                    data.isReward = 1

                    // 刷新当前
                    adapter.notifyItemChanged(position)
                }
            })

            // 获取动态
            newPageData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.finishRefresh()
                    }
                    if (binding.refreshLayout.isLoading) {
                        binding.refreshLayout.finishLoadMore()
                    }
                }
                success {
                    // 刷新相关
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.finishRefresh()
                    }
                    if (binding.refreshLayout.isLoading) {

                        // 没有加载了、或者加载完毕
                        if ((data?.records?.size ?: 0) <= 0) {
                            binding.refreshLayout.finishLoadMoreWithNoMoreData()
                        } else {
                            binding.refreshLayout.finishLoadMore()
                        }
                    }
                    if (null == this.data) return@success


                    // 数据相关
                    data?.let {
                        val current = mViewMode.updateCurrent.value
                        if (current == 1) {
                            // 刷新数据
                            adapter.setList(it.records)
                        } else {
                            // 追加数据
                            it.records?.let { it1 -> adapter.addData(adapter.data.size, it1) }
                        }
                    }
                }
            })

            // 点赞
            likeData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 点赞成功、更新adapter
                    val position = mViewMode.currentPosition.value ?: -1
                    if (position == -1) return@success

                    val item = adapter.data[position] as? NewPageData.Records
                    item?.let {
                        it.isPraise = 1
                        it.praise = it.praise?.plus(1)
                        adapter.notifyItemChanged(position)
                    }
                }
            })
            // 取消点赞
            unlikeData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 点赞成功、更新adapter
                    val position = mViewMode.currentPosition.value ?: -1
                    if (position == -1) return@success

                    val item = adapter.data[position] as? NewPageData.Records
                    item?.let {
                        it.isPraise = 0
                        it.praise = it.praise?.minus(1)
                        adapter.notifyItemChanged(position)
                    }
                }
            })

            // 删除
            deleteData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    val position = mViewMode.currentPosition.value ?: -1
                    if (position == -1) return@success
                    adapter.removeAt(position)
                }
            })

            // 是否公开
            // 在这个界面都是公开的
            publicData.observe(viewLifecycleOwner, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    val position = mViewMode.currentPosition.value ?: -1
                    if (position == -1) return@success
                    adapter.removeAt(position)
                }
            })

        }
    }


    override fun FragmentContactBinding.initBinding() {

    }

    override fun onResume() {
        super.onResume()
        // 添加状态蓝高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.clRoot) { v, insets ->
            binding.clRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }



    /**
     * 回调刷新页面
     */
    private val startActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == AppCompatActivity.RESULT_OK) {
            // 重新请求数据
            mViewMode.getNewPage(NewPageReq(current = 1, size = 10))
        }
    }


    companion object {
        const val REFRESH_SIZE = 10
    }

    /**
     * 图片点击，九宫格图片
     */
    override fun onClick(nineGridView: NineGridImageView, imageView: ImageView, url: String, urlList: List<String>, externalPosition: Int, position: Int) {
        // 图片浏览
        XPopup.Builder(context)
            .asImageViewer(
                imageView,
                position,
                urlList.toList(),
                OnSrcViewUpdateListener { _, _ -> },
                SmartGlideImageLoader()
            )
            .show()
    }
}