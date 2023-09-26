package com.cl.modules_contact.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cn.mtjsoft.barcodescanning.extentions.dp
import cn.mtjsoft.barcodescanning.utils.SoundPoolUtil
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.refresh.ClassicsHeader
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.scroll.behavior.BehavioralScrollListener
import com.cl.common_base.widget.scroll.behavior.BehavioralScrollView
import com.cl.common_base.widget.scroll.behavior.BottomSheetLayout
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.OtherJourneyAdapter
import com.cl.modules_contact.databinding.ContactOtherJourneyBinding
import com.cl.modules_contact.pop.CommentPop
import com.cl.modules_contact.pop.ContactEnvPop
import com.cl.modules_contact.pop.ContactPotionPop
import com.cl.modules_contact.pop.ContactReportPop
import com.cl.common_base.pop.RewardPop
import com.cl.modules_contact.request.ContactEnvData
import com.cl.modules_contact.request.DeleteReq
import com.cl.common_base.bean.LikeReq
import com.cl.modules_contact.request.MyMomentsReq
import com.cl.modules_contact.request.ReportReq
import com.cl.common_base.bean.RewardReq
import com.cl.common_base.ext.safeToInt
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.viewmodel.MyJourneyViewModel
import com.cl.modules_contact.widget.emoji.BitmapProvider
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.scwang.smart.refresh.footer.ClassicsFooter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 其他人的空间
 */
@AndroidEntryPoint
class OtherJourneyActivity : BaseActivity<ContactOtherJourneyBinding>() {

    @Inject
    lateinit var viewModel: MyJourneyViewModel

    private val userId by lazy {
        intent.getStringExtra(KEY_USER_ID)
    }

    /*private val nickName by lazy {
        intent.getStringExtra(KEY_NICK_NAME)
    }

    private val avatar by lazy {
        intent.getStringExtra(KEY_AVATAR)
    }*/

    private val adapter by lazy {
        OtherJourneyAdapter(mutableListOf())
    }


    private val floatingHeight = 200.dp
    private fun updateFloatState() {
        if (binding.bottomSheet.indexOfChild(binding.rvLinkageBottom) >= 0) {
            if (binding.linkageScroll.scrollY >= floatingHeight) {
                binding.bottomSheet.visibility = View.GONE
                binding.bottomSheet.removeView(binding.rvLinkageBottom)
                if (binding.layoutBottom.indexOfChild(binding.rvLinkageBottom) < 0) {
                    binding.layoutBottom.addView(binding.rvLinkageBottom)
                }
                binding.linkageScroll.bottomScrollTarget = { binding.rvLinkageBottom }
            }
        } else {
            if (binding.linkageScroll.scrollY < floatingHeight) {
                binding.linkageScroll.bottomScrollTarget = null
                if (binding.layoutBottom.indexOfChild(binding.rvLinkageBottom) >= 0) {
                    binding.layoutBottom.removeView(binding.rvLinkageBottom)
                }
                if (binding.bottomSheet.indexOfChild(binding.rvLinkageBottom) < 0) {
                    binding.bottomSheet.addView(binding.rvLinkageBottom)
                }
                binding.bottomSheet.visibility = View.VISIBLE
            }
        }
    }

    override fun ContactOtherJourneyBinding.initBinding() {
        lifecycleOwner = this@OtherJourneyActivity
        viewModel = this@OtherJourneyActivity.viewModel
        executePendingBindings()
    }

    override fun initView() {
        binding.linkageScroll.topScrollTarget = { binding.rvLinkageTop }
        binding.linkageScroll.listeners.add(object : BehavioralScrollListener {
            override fun onScrollChanged(v: BehavioralScrollView, from: Int, to: Int) {
                updateFloatState()
            }
        })

        binding.bottomSheet.setup(BottomSheetLayout.POSITION_MID, 400.dp, 550.dp)/*binding.bottomSheet.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            var alphaProgress = abs(100.div(scrollY.toFloat()))
            if (alphaProgress < 0.3) alphaProgress = 0f
            logI("123123123: $scrollY, $oldScrollY , ${abs(100.div(scrollY.toFloat()))}")
            binding.title.background.alpha = abs(255.times(alphaProgress).toInt())
        }*/
        updateFloatState()

        binding.superLikeLayout.provider = BitmapProvider.Builder(this@OtherJourneyActivity).setDrawableArray(
            intArrayOf(
                R.mipmap.emoji_one,
                R.mipmap.emoji_two,
                R.mipmap.emoji_three,
                R.mipmap.emoji_four,
                R.mipmap.emoji_five,
                R.mipmap.emoji_six,
            )
        ).build()


        // 获取我的动态
        viewModel.getMyPage(
            MyMomentsReq(
                current = 1, size = REFRESH_SIZE, userId = userId
            )
        )

        binding.refreshLayout.apply {
            ClassicsFooter.REFRESH_FOOTER_LOADING = "Updating" //"正在刷新...";
            ClassicsFooter.REFRESH_FOOTER_REFRESHING = "Updating" //"正在加载...";
            ClassicsFooter.REFRESH_FOOTER_NOTHING = "No more data"
            ClassicsFooter.REFRESH_FOOTER_FINISH = "Loading completed"
            ClassicsFooter.REFRESH_FOOTER_FAILED = "Loading failed"

            // 刷新监听
            setOnRefreshListener { // 重新加载数据
                logI("setOnRefreshListener: refresh")
                viewModel.updateCurrent(1)
                viewModel.getMyPage(MyMomentsReq(current = viewModel.updateCurrent.value, size = ContactFragment.REFRESH_SIZE, userId = userId))
            } // 加载更多监听
            setOnLoadMoreListener {
                val current = (viewModel.updateCurrent.value ?: 1) + 1
                logI("setOnLoadMoreListener: loadMore Current : $current")
                viewModel.updateCurrent(current)
                viewModel.getMyPage(MyMomentsReq(current = current, size = ContactFragment.REFRESH_SIZE, userId = userId))
            } // 刷新头部局
            setRefreshHeader(ClassicsHeader(this@OtherJourneyActivity))
            setRefreshFooter(ClassicsFooter(this@OtherJourneyActivity).setFinishDuration(0)) // 刷新高度
            setHeaderHeight(60f) // 自动刷新
            // autoRefresh()
        }

        binding.rvJourney.apply { // 添加分割线
            //添加自定义分割线
            val divider = DividerItemDecoration(this@OtherJourneyActivity, DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(context, com.cl.common_base.R.drawable.custom_divider)!!)
            addItemDecoration(divider)
            layoutManager = LinearLayoutManager(this@OtherJourneyActivity)
            adapter = this@OtherJourneyActivity.adapter
        }
    }

    override fun observe() {
        viewModel.apply {

            wallpaperList.observe(this@OtherJourneyActivity, resourceObserver {
                error { errorMsg, code -> ToastUtil.shortShow(errorMsg) }
                success {
                    if (data.isNullOrEmpty()) return@success

                    // 替换背景
                    val wallId = userDetail.value?.data?.wallId
                    data?.firstOrNull { it.id == wallId }?.let { bean ->
                        Glide.with(this@OtherJourneyActivity).load(bean.address)
                            .placeholder(com.cl.common_base.R.mipmap.my_bg)
                            .into(binding.rvLinkageTop)
                    }
                }
            })

            userDetail.observe(this@OtherJourneyActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success { // 数据相关
                    // 壁纸
                    when (data?.wallAddress) {
                        "banner01" -> {
                            binding.rvLinkageTop.background = ContextCompat.getDrawable(
                                this@OtherJourneyActivity, com.cl.common_base.R.mipmap.banner01
                            )
                        }

                        "banner02" -> {
                            binding.rvLinkageTop.background = ContextCompat.getDrawable(
                                this@OtherJourneyActivity, com.cl.common_base.R.mipmap.banner02
                            )
                        }

                        "banner03" -> {
                            binding.rvLinkageTop.background = ContextCompat.getDrawable(
                                this@OtherJourneyActivity, com.cl.common_base.R.mipmap.banner03
                            )
                        }

                        else -> viewModel.wallpaperList()
                    }
                }
            })

            myPageData.observe(this@OtherJourneyActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success { // 刷新相关
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.finishRefresh()
                    }
                    if (binding.refreshLayout.isLoading) {

                        // 没有加载了、或者加载完毕
                        if ((data?.records?.size ?: 0) <= 0) {
                            binding.refreshLayout.finishLoadMore()
                        } else {
                            binding.refreshLayout.finishLoadMore()
                        }
                    }
                    if (null == this.data) return@success


                    // 数据相关
                    data?.let {
                        val current = viewModel.updateCurrent.value
                        if (current == 1) { // 刷新数据
                            adapter.setList(it.records)
                        } else { // 追加数据
                            it.records?.let { it1 -> adapter.addData(adapter.data.size, it1) }
                        }
                    }

                    // 显示与隐藏
                    ViewUtils.setVisible(adapter.data.size != 0, binding.rvJourney)
                    ViewUtils.setVisible(adapter.data.size == 0, binding.rvEmpty)
                }
            })

            // 打赏
            rewardData.observe(this@OtherJourneyActivity, resourceObserver {
                error { errorMsg, _ ->
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    val oxygenNum = viewModel.rewardOxygen.value
                    val position = viewModel.currentPosition.value ?: -1
                    if (oxygenNum == 0) return@success
                    if (position == -1) return@success

                    val data = adapter.data[position]
                    data.reward = oxygenNum?.let { data.reward?.plus(it) }
                    data.isReward = 1

                    // 刷新当前
                    adapter.notifyItemChanged(position)
                }
            })

            // 点赞
            likeData.observe(this@OtherJourneyActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success { // 点赞成功、更新adapter
                    val position = viewModel.currentPosition.value ?: -1
                    if (position == -1) return@success

                    val item = adapter.data[position] as? NewPageData.Records
                    item?.let {
                        it.isPraise = 1
                        it.praise = it.praise?.plus(1)
                        adapter.notifyItemChanged(position)
                    }
                }
            }) // 取消点赞
            unlikeData.observe(this@OtherJourneyActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success { // 点赞成功、更新adapter
                    val position = viewModel.currentPosition.value ?: -1
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
            deleteData.observe(this@OtherJourneyActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success {
                    val position = viewModel.currentPosition.value ?: -1
                    if (position == -1) return@success
                    adapter.removeAt(position)
                }
            })

            // 是否公开
            // 在这个界面都是公开的
            publicData.observe(this@OtherJourneyActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success {
                    val position = viewModel.currentPosition.value ?: -1
                    if (position == -1) return@success
                    adapter.removeAt(position)
                }
            })
        }
    }

    override fun initData() { // 获取他人用户信息
        userId?.let {
            viewModel.userDetail(it)
        }
        initAdapterClick()
    }

    private fun initAdapterClick() {
        adapter.addChildClickViewIds(R.id.tv_link, R.id.cl_env, R.id.cl_love, R.id.cl_gift, R.id.cl_chat, R.id.rl_point, R.id.tv_to_chat)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as? NewPageData.Records
            viewModel.updateCurrentPosition(position)
            when (view.id) {
                R.id.tv_link -> { // 跳转网页
                    val intent = Intent(this@OtherJourneyActivity, WebActivity::class.java)
                    intent.putExtra(WebActivity.KEY_WEB_URL, item?.link)
                    this@OtherJourneyActivity.startActivity(intent)
                }

                R.id.cl_env -> { // 点击环境信息
                    val envInfoData = GSON.parseObjectList(item?.environment, ContactEnvData::class.java).toMutableList() // 弹出环境信息
                    XPopup.Builder(this@OtherJourneyActivity).dismissOnTouchOutside(false).isDestroyOnDismiss(false).asCustom(ContactEnvPop(this@OtherJourneyActivity, envInfoData, item?.nickName, item?.avatarPicture)).show()
                }

                R.id.cl_love -> { // 点赞
                    if (item?.isPraise == 0) {
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
                        viewModel.like(LikeReq(learnMoreId = item.learnMoreId, likeId = item.id.toString(), type = "moments"))

                    } else {
                        viewModel.unlike(LikeReq(learnMoreId = item?.learnMoreId, likeId = item?.id.toString(), type = "moments"))
                    }
                        // 震动
                        SoundPoolUtil.instance.startVibrator(this@OtherJourneyActivity)
                }

                R.id.cl_gift -> { //  打赏
                    if (item?.userId == viewModel.userinfoBean?.userId) {
                        extracted(view.findViewById(R.id.curing_box_gift))
                        return@setOnItemChildClickListener
                    }
                    XPopup.Builder(this@OtherJourneyActivity).isDestroyOnDismiss(false).dismissOnTouchOutside(true).asCustom(
                        RewardPop(this@OtherJourneyActivity, onRewardListener = { oxygenNum ->
                            viewModel.updateRewardOxygen(oxygenNum.safeToInt())
                            viewModel.reward(
                                RewardReq(
                                    momentsId = item?.id.toString(), oxygenNum = oxygenNum, type = ContactCommentActivity.KEY_MOMENTS, relationId = item?.id.toString()
                                )
                            )
                        })
                    ).show()
                }

                R.id.cl_chat -> { // 聊天
                    toCommentPop(item, position, adapter)
                }

                R.id.tv_to_chat -> { //  跳转到更多聊天记录弹窗
                    toCommentPop(item, position, adapter)
                }

                R.id.rl_point -> { // 点击三个点
                    XPopup.Builder(this@OtherJourneyActivity).popupPosition(PopupPosition.Left).dismissOnTouchOutside(true).isClickThrough(false)  //点击透传
                        .hasShadowBg(true) // 去掉半透明背景
                        //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                        .atView(view).isCenterHorizontal(false).asCustom(this@OtherJourneyActivity.let {
                            ContactPotionPop(
                                it, isShowShareToPublic = item?.syncTrend != 1,
                                deleteAction = { //  删除
                                    viewModel.delete(DeleteReq(momentId = item?.id.toString()))
                                },
                                reportAction = { // 举报弹窗
                                    XPopup.Builder(it).isDestroyOnDismiss(false).dismissOnTouchOutside(false).asCustom(
                                        ContactReportPop(it, onConfirmAction = { txt -> // 举报
                                            viewModel.report(ReportReq(momentId = item?.id.toString(), reportContent = txt))
                                        })
                                    ).show()
                                },
                                itemSwitchAction = { isCheck -> // 关闭分享
                                    viewModel.public(syncTrend = if (isCheck) 1 else 0, momentId = item?.id.toString())
                                },
                                isShowReport = item?.userId.toString() == viewModel.userinfoBean?.userId,
                            ).setBubbleBgColor(Color.WHITE) //气泡背景
                                .setArrowWidth(XPopupUtils.dp2px(this@OtherJourneyActivity, 3f)).setArrowHeight(
                                    XPopupUtils.dp2px(
                                        this@OtherJourneyActivity, 3f
                                    )
                                ) //.setBubbleRadius(100)
                                .setArrowRadius(
                                    XPopupUtils.dp2px(
                                        this@OtherJourneyActivity, 2f
                                    )
                                )
                        }).show()
                }
            }
        }
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

    private fun toCommentPop(item: NewPageData.Records?, position: Int, adapter: BaseQuickAdapter<*, *>) {
        XPopup.Builder(this@OtherJourneyActivity).isDestroyOnDismiss(false).enableDrag(false).dismissOnTouchOutside(false).moveUpToKeyboard(false).maxHeight((XPopupUtils.getScreenHeight(this@OtherJourneyActivity) * 0.9f).safeToInt()).asCustom(
            CommentPop(this@OtherJourneyActivity, item?.userId == viewModel.userinfoBean?.userId, item?.id, onDismissAction = { commentListData -> // 更新当前position
                val commentsList = this@OtherJourneyActivity.adapter.data[position].comments
                if (commentListData?.size == 0) return@CommentPop
                if (commentListData?.size == commentsList?.size) return@CommentPop // 实行替换操作
                val newCommentsList = mutableListOf<NewPageData.Records.Comments>()
                commentListData?.forEach { data ->
                    val comment = NewPageData.Records.Comments()
                    comment.commentName = data.commentName
                    comment.comment = data.comment
                    newCommentsList.add(comment)
                } // 更新聊天数目集合
                this@OtherJourneyActivity.adapter.data[position].comments = newCommentsList // 更新聊天数量
                this@OtherJourneyActivity.adapter.data[position].comment = commentListData?.size
                adapter.notifyItemChanged(position)
            })
        ).show()
    }

    companion object {
        const val REFRESH_SIZE = 10
        const val KEY_USER_ID = "key_user_id"
    }
}