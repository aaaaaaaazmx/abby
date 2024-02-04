package com.cl.modules_contact.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.view.animation.LinearInterpolator
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cn.mtjsoft.barcodescanning.utils.SoundPoolUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.refresh.ClassicsHeader
import com.cl.common_base.util.json.GSON
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.MyJourneyAdapter
import com.cl.modules_contact.databinding.ContactMyJourneyActivityBinding
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
import com.cl.common_base.constants.RouterPath
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
 * 个人空间
 */
@Route(path = RouterPath.My.PAGE_MY_JOURNEY)
@AndroidEntryPoint
class MyJourneyActivity : BaseActivity<ContactMyJourneyActivityBinding>() {


    @Inject
    lateinit var viewModel: MyJourneyViewModel

    private val adapter by lazy {
        MyJourneyAdapter(mutableListOf())
    }

    override fun initView() {
        binding.superLikeLayout.provider = BitmapProvider.Builder(this@MyJourneyActivity)
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

        binding.title.setRightButtonImg(R.mipmap.contact_my_journey_add)
            .setRightClickListener {
                // 添加
                startActivity(Intent(this@MyJourneyActivity, PostActivity::class.java))
            }

        binding.rvMyJourney.apply {
            // 添加分割线
            //添加自定义分割线
            val divider = DividerItemDecoration(this@MyJourneyActivity, DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(context, com.cl.common_base.R.drawable.custom_divider)!!)
            addItemDecoration(divider)
            layoutManager = LinearLayoutManager(this@MyJourneyActivity)
            adapter = this@MyJourneyActivity.adapter
        }

        // 获取我的动态
        viewModel.getMyPage(
            MyMomentsReq(
                current = 1,
                size = REFRESH_SIZE,
                userId = viewModel.userinfoBean?.userId
            )
        )

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
                viewModel.updateCurrent(1)
                viewModel.getMyPage(MyMomentsReq(current = viewModel.updateCurrent.value, size = ContactFragment.REFRESH_SIZE, userId = viewModel.userinfoBean?.userId))
            }
            // 加载更多监听
            setOnLoadMoreListener {
                val current = (viewModel.updateCurrent.value ?: 1) + 1
                logI("setOnLoadMoreListener: loadMore Current : $current")
                viewModel.updateCurrent(current)
                viewModel.getMyPage(MyMomentsReq(current = current, size = ContactFragment.REFRESH_SIZE, userId = viewModel.userinfoBean?.userId))
            }
            // 刷新头部局
            setRefreshHeader(ClassicsHeader(this@MyJourneyActivity))
            setRefreshFooter(ClassicsFooter(this@MyJourneyActivity).setFinishDuration(0))
            // 刷新高度
            setHeaderHeight(60f)
            // 自动刷新
            // autoRefresh()
        }

    }

    override fun observe() {
        viewModel.apply {
            myPageData.observe(this@MyJourneyActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
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
                        val current = viewModel.updateCurrent.value
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

            // 打赏
            rewardData.observe(this@MyJourneyActivity, resourceObserver {
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
            likeData.observe(this@MyJourneyActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 点赞成功、更新adapter
                    val position = viewModel.currentPosition.value ?: -1
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
            unlikeData.observe(this@MyJourneyActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success {
                    // 点赞成功、更新adapter
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
            deleteData.observe(this@MyJourneyActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success {
                    val position = viewModel.currentPosition.value ?: -1
                    if (position == -1) return@success
                    adapter.removeAt(position)
                }
            })

            // 是否公开
            // 在这个界面都是公开的
            publicData.observe(this@MyJourneyActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success {
                    val position = viewModel.currentPosition.value ?: -1
                    if (position == -1) return@success
                    adapter.removeAt(position)
                }
            })
        }
    }

    override fun initData() {
        initAdapterClick()
    }

    private fun initAdapterClick() {
        adapter.addChildClickViewIds(R.id.tv_link, R.id.cl_env, R.id.cl_love, R.id.cl_gift, R.id.cl_chat, R.id.rl_point)
        adapter.setOnItemChildClickListener {adapter, view, position ->
            val item = adapter.data[position] as? NewPageData.Records
            viewModel.updateCurrentPosition(position)
            when(view.id) {
                R.id.tv_link -> {
                    // 跳转网页
                    val intent = Intent(this@MyJourneyActivity, WebActivity::class.java)
                    intent.putExtra(WebActivity.KEY_WEB_URL, item?.link)
                    this@MyJourneyActivity.startActivity(intent)
                }

                R.id.cl_env -> {
                    // 点击环境信息
                    val envInfoData = GSON.parseObjectList(item?.environment, ContactEnvData::class.java).toMutableList()
                    // 弹出环境信息
                    XPopup.Builder(this@MyJourneyActivity).dismissOnTouchOutside(false).isDestroyOnDismiss(false).asCustom(ContactEnvPop(this@MyJourneyActivity, envInfoData, item?.nickName, item?.avatarPicture)).show()
                }

                R.id.cl_love -> {
                    // 点赞
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
                        SoundPoolUtil.instance.startVibrator(this@MyJourneyActivity)
                }

                R.id.cl_gift -> {
                    //  打赏
                    if (item?.userId == viewModel.userinfoBean?.userId) {
                        extracted(view.findViewById(R.id.curing_box_gift))
                        return@setOnItemChildClickListener
                    }
                    XPopup.Builder(this@MyJourneyActivity)
                        .isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(true)
                        .asCustom(
                            RewardPop(this@MyJourneyActivity, onRewardListener = { oxygenNum ->
                                viewModel.updateRewardOxygen(oxygenNum.safeToInt())
                                viewModel.reward(
                                    RewardReq(
                                        momentsId = item?.id.toString(),
                                        oxygenNum = oxygenNum,
                                        type = ContactCommentActivity.KEY_MOMENTS,
                                        relationId = item?.id.toString()
                                    )
                                )
                            })
                        ).show()
                }

                R.id.cl_chat -> {
                    // 聊天
                    toCommentPop(item, position, adapter)
                }

                R.id.rl_point -> {
                    // 点击三个点
                    XPopup.Builder(this@MyJourneyActivity)
                        .popupPosition(PopupPosition.Left)
                        .dismissOnTouchOutside(true)
                        .isClickThrough(false)  //点击透传
                        .hasShadowBg(true) // 去掉半透明背景
                        //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                        .atView(view)
                        .isCenterHorizontal(false)
                        .asCustom(
                            this@MyJourneyActivity.let {
                                ContactPotionPop(
                                    it,
                                    isShowShareToPublic = item?.syncTrend == 0,
                                    fisItemSwitchIsCheck = item?.syncTrend == 1,
                                    deleteAction = {
                                        //  删除
                                        viewModel.delete(DeleteReq(momentId = item?.id.toString()))
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
                                                        viewModel.report(ReportReq(momentId = item?.id.toString(), reportContent = txt))
                                                    })
                                            ).show()
                                    },
                                    itemSwitchAction = { isCheck ->
                                        // 关闭分享
                                        viewModel.public(syncTrend = if (isCheck) 1 else 0, momentId = item?.id.toString())
                                    },
                                    isShowReport = item?.userId.toString() == viewModel.userinfoBean?.userId
                                )
                                    .setBubbleBgColor(Color.WHITE) //气泡背景
                                    .setArrowWidth(XPopupUtils.dp2px(this@MyJourneyActivity, 3f))
                                    .setArrowHeight(
                                        XPopupUtils.dp2px(
                                            this@MyJourneyActivity,
                                            3f
                                        )
                                    )
                                    //.setBubbleRadius(100)
                                    .setArrowRadius(
                                        XPopupUtils.dp2px(
                                            this@MyJourneyActivity,
                                            2f
                                        )
                                    )
                            }
                        ).show()
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
        XPopup.Builder(this@MyJourneyActivity)
            .isDestroyOnDismiss(false)
            .dismissOnTouchOutside(false)
            .moveUpToKeyboard(false)
            .enableDrag(false)
            .maxHeight((XPopupUtils.getScreenHeight(this@MyJourneyActivity) * 0.9f).safeToInt())
            .asCustom(
                    CommentPop(this@MyJourneyActivity, item?.userId == viewModel.userinfoBean?.userId, item?.id, onDismissAction = { commentListData ->
                        // 更新当前position
                        val commentsList = this@MyJourneyActivity.adapter.data[position].comments
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
                        this@MyJourneyActivity.adapter.data[position].comments = newCommentsList
                        // 更新聊天数量
                        // Initialize commentSize to 0
                        var commentSize = 0

                        // Add the size of commentListData to commentSize
                        commentSize += commentListData?.size ?: 0

                        // Add the sum of sizes of all replies lists to commentSize
                        commentListData?.forEach { comment ->
                            commentSize += comment.replys?.size ?: 0
                        }
                        // Set the calculated total comment size to the adapter
                        this@MyJourneyActivity.adapter.data[position].comment = commentSize
                        adapter.notifyItemChanged(position)
                    })
            ).show()
    }

    companion object {
        const val REFRESH_SIZE = 10
    }
}