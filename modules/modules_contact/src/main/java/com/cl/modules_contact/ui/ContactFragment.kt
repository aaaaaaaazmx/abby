package com.cl.modules_contact.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.CheckBox
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cn.mtjsoft.barcodescanning.utils.SoundPoolUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.cl.common_base.base.BaseFragment
import com.cl.common_base.bean.AutomaticLoginReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.intercome.InterComeHelp
import com.cl.common_base.refresh.ClassicsHeader
import com.cl.common_base.util.Prefs
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
import com.cl.common_base.pop.RewardPop
import com.cl.modules_contact.request.ContactEnvData
import com.cl.modules_contact.request.DeleteReq
import com.cl.common_base.bean.LikeReq
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.request.ReportReq
import com.cl.common_base.bean.RewardReq
import com.cl.common_base.bean.UpdateFollowStatusReq
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.xpopup
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.BaseThreeTextPop
import com.cl.common_base.web.VideoPLayActivity
import com.cl.modules_contact.databinding.ContactChooserTipPopBinding
import com.cl.modules_contact.pop.ContactChooseTipPop
import com.cl.modules_contact.pop.ContactDeletePop
import com.cl.modules_contact.pop.ContactNewEnvPop
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.response.TagsBean
import com.cl.modules_contact.viewmodel.ContactViewModel
import com.cl.modules_contact.widget.emoji.BitmapProvider
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import com.scwang.smart.refresh.footer.ClassicsFooter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * 联系人首页
 */
@Route(path = RouterPath.Contact.PAGE_CONTACT)
@AndroidEntryPoint
class ContactFragment : BaseFragment<FragmentContactBinding>() {
    @Inject
    lateinit var mViewMode: ContactViewModel

    // 朋友圈适配器
    private val adapter by lazy {
        TrendListAdapter(mutableListOf())
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

        binding.rvWxCircle.apply {
            /* (itemAnimator as? SimpleItemAnimator)?.apply {
                 changeDuration = 0
                 supportsChangeAnimations = false
             }*/
            layoutManager = LinearLayoutManager(activity)
            // 添加分割线
            //添加自定义分割线
            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(context, com.cl.common_base.R.drawable.custom_divider)!!)
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
                mViewMode.getNewPage(NewPageReq(current = mViewMode.updateCurrent.value, size = REFRESH_SIZE, period = mViewMode.currentPeriod.value, tags = mViewMode.currentTag.value))
            }
            // 加载更多监听
            setOnLoadMoreListener {
                val current = (mViewMode.updateCurrent.value ?: 1) + 1
                logI("setOnLoadMoreListener: loadMore Current : $current")
                mViewMode.updateCurrent(current)
                mViewMode.getNewPage(NewPageReq(current = current, size = REFRESH_SIZE, period = mViewMode.currentPeriod.value, tags = mViewMode.currentTag.value))
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
                    mViewMode.updateCurrentTag(null)
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
        binding.clTrend.setOnClickListener {
            // 弹窗显示植物周期
            choosePeriodPop.show()
        }

        // floatbutton
        binding.flButton.setOnClickListener {
            // 跳转到发布动态页面
            // ToastUtil.shortShow("FLAT")
            context?.let { context ->
                XPopup.Builder(context)
                    .popupPosition(PopupPosition.Left)
                    .dismissOnTouchOutside(true)
                    .isClickThrough(false)  //点击透传
                    .hasShadowBg(true) // 去掉半透明背景
                    //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                    .atView(it)
                    .isCenterHorizontal(false)
                    .asCustom(
                        ContactChooseTipPop(
                            context,
                            onPhotoPostAction = {
                                startActivityLauncher.launch(Intent(context, PostActivity::class.java))
                            },
                            onReelPostAction = {
                                startActivityLauncher.launch(Intent(context, ReelPostActivity::class.java))
                            }
                        ).setBubbleBgColor(Color.WHITE) //气泡背景
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
                                    3f
                                )
                            )
                    ).show()
            }
        }

        // 头像点击
        binding.clAvatar.setOnClickListener {
            // 个人头像点击啊
            /*context?.let {
                startActivity(Intent(it, MyJourneyActivity::class.java))
            }*/
            // 跳转到资产界面
            ARouter
                .getInstance()
                .build(RouterPath.My.PAGE_DIGITAL)
                .navigation(context)
        }

        // 消息点击
        binding.ivBells.setOnClickListener {
            // 消息点击啊
            context?.let {
                startActivityForNotificationLauncher.launch(Intent(it, ContactNotificationActivity::class.java))
            }
        }
    }

    /**
     * 条目点击事件
     */
    private fun initAdapterClick() {
        adapter.addChildClickViewIds(R.id.tv_link, R.id.tv_live_link, R.id.cl_avatar, R.id.cl_env, R.id.cl_love, R.id.cl_gift, R.id.cl_chat, R.id.rl_point, R.id.tv_to_chat, R.id.tv_learn_more)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as? NewPageData.Records
            mViewMode.updateCurrentPosition(position)
            when (view.id) {
                // 跳转直播网页
                R.id.tv_live_link -> {
                    val intent = Intent(context, VideoPLayActivity::class.java)
                    intent.putExtra(WebActivity.KEY_WEB_URL, item?.liveLink)
                    intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Live")
                    startActivity(intent)
                }

                R.id.tv_link -> {
                    // 跳转网页
                    val intent = Intent(context, WebActivity::class.java)
                    intent.putExtra(WebActivity.KEY_WEB_URL, item?.link)
                    context?.startActivity(intent)
                }

                R.id.cl_avatar -> {
                    // todo 点击头像、跳转到自己的空间， 用userID来区别是跳转到自己的，还是别人的
                    if (item?.userId == mViewMode.userinfoBean?.userId) {
                        // context?.startActivity(Intent(context, MyJourneyActivity::class.java))
                        // 跳转到资产界面
                        ARouter
                            .getInstance()
                            .build(RouterPath.My.PAGE_DIGITAL)
                            .navigation(context)
                    } else {
                        mViewMode.updatePosition(position)
                        refreshPositionLauncher.launch(Intent(context, OtherJourneyActivity::class.java).apply {
                            putExtra(OtherJourneyActivity.KEY_USER_ID, item?.userId)
                            putExtra(OtherJourneyActivity.KEY_USER_NAME, item?.nickName)
                        })
                    }
                }

                R.id.cl_env -> {
                    // 点击环境信息
                    val envInfoData = GSON.parseObjectList(item?.environment, ContactEnvData::class.java).toMutableList()
                    // 弹出环境信息
                    //XPopup.Builder(context).dismissOnTouchOutside(false).isDestroyOnDismiss(false).asCustom(context?.let { ContactEnvPop(it, envInfoData, item?.nickName, item?.avatarPicture) }).show()
                    // 弹出修改后的环境信息
                    if (item?.deviceModelName.isNullOrEmpty() || item?.deviceModelName == "Tent") {
                        return@setOnItemChildClickListener
                    }
                    context?.let {
                        xpopup(it) {
                            dismissOnTouchOutside(false)
                            isDestroyOnDismiss(false)
                            asCustom(ContactNewEnvPop(it, envInfoData, item)).show()
                        }
                    }
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
                        mViewMode.like(LikeReq(learnMoreId = item.learnMoreId, likeId = item.id.toString(), type = "moments"))
                    } else {
                        mViewMode.unlike(LikeReq(learnMoreId = item?.learnMoreId, likeId = item?.id.toString(), type = "moments"))
                    }
                    // 震动
                    SoundPoolUtil.instance.startVibrator(context = context)

                    // 是否展示关注弹窗
                    isShowFollowDialog(item, adapter, position, false)
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
                                    mViewMode.updateRewardOxygen(oxygenNum.safeToInt())
                                    mViewMode.reward(
                                        RewardReq(
                                            momentsId = item?.id.toString(),
                                            oxygenNum = oxygenNum,
                                            type = ContactCommentActivity.KEY_MOMENTS,
                                            relationId = item?.id.toString()
                                        )
                                    )

                                    // 是否展示关注弹窗
                                    isShowFollowDialog(item, adapter, position, true)
                                })
                            }
                        ).show()
                }

                R.id.cl_chat -> {
                    // 聊天
                    toCommentPop(item, position, adapter)
                }

                R.id.rl_point -> {
                    logI("1231231231: ${item?.userId}")
                    logI("1231231231: ${item?.isFollow}")
                    logI("1231231231111: ${mViewMode.userinfoBean?.userId}")
                    logI("12312312311111111: ${mViewMode.userinfoBean?.userId == item?.userId}")
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
                            context?.let { cc ->
                                ContactPotionPop(
                                    cc,
                                    deleteAction = {
                                        //  删除
                                        mViewMode.delete(DeleteReq(momentId = item?.id.toString()))
                                    },
                                    reportAction = {
                                        // 举报弹窗
                                        XPopup.Builder(cc)
                                            .isDestroyOnDismiss(false)
                                            .dismissOnTouchOutside(false)
                                            .asCustom(
                                                ContactReportPop(
                                                    cc,
                                                    onConfirmAction = { txt ->
                                                        // 举报
                                                        mViewMode.report(ReportReq(momentId = item?.id.toString(), reportContent = txt))
                                                    })
                                            ).show()
                                    },
                                    itemSwitchAction = { isCheck ->
                                        // 关闭分享
                                        mViewMode.public(syncTrend = if (isCheck) 1 else 0, momentId = item?.id.toString())
                                    },
                                    isShowReport = item?.userId.toString() == mViewMode.userinfoBean?.userId,
                                    isShowShareToPublic = item?.userId.toString() == mViewMode.userinfoBean?.userId,
                                    isFollow = item?.isFollow == true,
                                    followAction = {
                                        // 跟随
                                        xpopup(cc) {
                                            isDestroyOnDismiss(false)
                                            dismissOnTouchOutside(false)
                                            asCustom(
                                                BaseCenterPop(
                                                    cc,
                                                    confirmText = if (item?.isFollow == true) "Unfollow" else "Follow",
                                                    isShowCancelButton = true,
                                                    cancelText = "Cancel",
                                                    content = if (item?.isFollow == true) "Unfollow this grower" else "Do you want to follow this grower?",
                                                    onConfirmAction = {
                                                        if (item?.isFollow == true) {
                                                            mViewMode.updateFollowStatus(UpdateFollowStatusReq(false, item.userId.toString()))
                                                        } else {
                                                            mViewMode.updateFollowStatus(UpdateFollowStatusReq(true, item?.userId.toString()))
                                                        }
                                                        // 更新当前follow状态
                                                        item?.isFollow = !(item?.isFollow ?: false)
                                                        adapter.notifyItemChanged(position)
                                                    })
                                            ).show()
                                        }
                                    }
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

                R.id.tv_learn_more -> {
                    InterComeHelp.INSTANCE.openInterComeSpace(space = InterComeHelp.InterComeSpace.Article, id = item?.articleId.toString())
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

                    // 设置当前标签
                    item?.number?.let { mViewMode.updateCurrentTag(it) }

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

    private fun isShowFollowDialog(item: NewPageData.Records?, adapter: BaseQuickAdapter<*, *>, position: Int, isGifts: Boolean) {
        if (item?.userId == mViewMode.userinfoBean?.userId) return
        if (!isGifts) {
            if (item?.isPraise == 1) return
        }
        // 获取KEY_FOLLOW_TIP_IS_SHOW的值
        val isShow = Prefs.getBoolean(Constants.Contact.KEY_FOLLOW_TIP_IS_SHOW, false)
        if (!isShow) {
            // 判断是否关注，弹出需要关注的弹窗
            if (item?.isFollow == false) {
                context?.let {
                    xpopup(it) {
                        isDestroyOnDismiss(false)
                        dismissOnTouchOutside(true)
                        asCustom(
                            BaseThreeTextPop(
                                it,
                                content = "Do you want to follow this grower?",
                                oneLineText = "Don't show it again",
                                twoLineText = "Follow",
                                threeLineText = "Cancel",
                                twoLineCLickEventAction = {
                                    // 关注
                                    mViewMode.updateFollowStatus(UpdateFollowStatusReq(true, item.userId.toString()))
                                    item.isFollow = !(item.isFollow ?: false)
                                    adapter.notifyItemChanged(position)
                                }, oneLineCLickEventAction = {
                                    // 保存到本地，以后都不再提问
                                    Prefs.putBoolean(Constants.Contact.KEY_FOLLOW_TIP_IS_SHOW, true)
                                })
                        ).show()
                    }
                }
            }
        }
    }

    private fun toCommentPop(item: NewPageData.Records?, position: Int, adapter: BaseQuickAdapter<*, *>) {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .dismissOnTouchOutside(false)
            .moveUpToKeyboard(false)
            .enableDrag(false)
            .maxHeight((XPopupUtils.getScreenHeight(context) * 0.9f).safeToInt())
            .asCustom(
                context?.let {
                    CommentPop(it, item?.userId == mViewMode.userinfoBean?.userId, item?.id, onDismissAction = { commentListData ->
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
                        // Initialize commentSize to 0
                        var commentSize = 0

                        // Add the size of commentListData to commentSize
                        commentSize += commentListData?.size ?: 0

                        // Add the sum of sizes of all replies lists to commentSize
                        commentListData?.forEach { comment ->
                            commentSize += comment.replys?.size ?: 0
                        }
                        // Set the calculated total comment size to the adapter
                        this@ContactFragment.adapter.data[position].comment = commentSize
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
            userDetail.observe(viewLifecycleOwner, resourceObserver {
                success {

                    data?.let {
                        // 数量的显示
                        ViewUtils.setVisible(it.eventCount != 0, binding.vvMsgNumber)
                        ViewUtils.setVisible(TextUtils.isEmpty(it.avatarPicture), binding.noheadShow)
                        ViewUtils.setVisible(!TextUtils.isEmpty(it.avatarPicture), binding.ivAvatar)
                        context?.let { context ->
                            Glide.with(context).load(it.avatarPicture)
                                .apply(RequestOptions.circleCropTransform())
                                .into(binding.ivAvatar)
                        }
                        binding.vvMsgNumber.text = (it.eventCount ?: 0).toString()
                        binding.noheadShow.text = it.nickName?.substring(0, 1)
                    }
                }
            })
            /*refreshToken.observe(viewLifecycleOwner, resourceObserver {
                success {

                    data?.let {
                        // 数量的显示
                        ViewUtils.setVisible(it.eventCount != 0, binding.vvMsgNumber)
                        ViewUtils.setVisible(TextUtils.isEmpty(it.avatarPicture), binding.noheadShow)
                        ViewUtils.setVisible(!TextUtils.isEmpty(it.avatarPicture), binding.ivAvatar)
                        context?.let { context ->
                            Glide.with(context).load(it.avatarPicture)
                                .apply(RequestOptions.circleCropTransform())
                                .into(binding.ivAvatar)
                        }
                        binding.vvMsgNumber.text = (it.eventCount ?: 0).toString()
                        binding.noheadShow.text = it.nickName?.substring(0, 1)
                    }
                }

            })*/

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
                    if (adapter.data.isEmpty()) return@success

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
                    if (adapter.data.isEmpty()) return@success

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
                    if (adapter.data.isEmpty()) return@success

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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            mViewMode.userDetail()
        }
    }


    /**
     * 回调刷新页面
     */
    private val startActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == AppCompatActivity.RESULT_OK) {
            mViewMode.updateCurrent(1)
            // 重新请求数据
            mViewMode.getNewPage(NewPageReq(current = 1, size = 10, period = mViewMode.currentPeriod.value, tags = mViewMode.currentTag.value))
        }
    }

    /**
     * 刷新单个position
     */
    private val refreshPositionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == AppCompatActivity.RESULT_OK) {
            val isFollow = it.data?.getBooleanExtra(KEY_FOLLOW_STATUS, false)
            val position = mViewMode.position.value ?: -1
            if (position == -1) return@registerForActivityResult
            if (adapter.data.isEmpty()) return@registerForActivityResult
            val item = adapter.data[position] as? NewPageData.Records
            item?.isFollow = isFollow
            adapter.notifyItemChanged(position)
        }
    }

    private val startActivityForNotificationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == AppCompatActivity.RESULT_OK) {
            // 重新请求数据
            mViewMode.userDetail()
        }
    }


    companion object {
        const val REFRESH_SIZE = 10

        // KEY_FOLLOW_STATUS
        const val KEY_FOLLOW_STATUS = "KEY_FOLLOW_STATUS"
    }
}