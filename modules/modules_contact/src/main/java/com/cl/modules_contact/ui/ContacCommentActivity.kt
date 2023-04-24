package com.cl.modules_contact.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.text.SpannedString
import android.text.TextUtils
import android.view.animation.LinearInterpolator
import android.widget.CheckBox
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.adapter.ContactCommentAdapter
import com.cl.modules_contact.adapter.EmojiAdapter
import com.cl.modules_contact.adapter.NineGridAdapter
import com.cl.modules_contact.databinding.ContactAddCommentBinding
import com.cl.modules_contact.pop.ContactPotionPop
import com.cl.modules_contact.pop.ContactReportPop
import com.cl.modules_contact.pop.ReplyCommentPop
import com.cl.modules_contact.pop.RewardPop
import com.cl.modules_contact.request.CommentByMomentReq
import com.cl.modules_contact.request.DeleteReq
import com.cl.modules_contact.request.LikeReq
import com.cl.modules_contact.request.PublishReq
import com.cl.modules_contact.request.ReplyReq
import com.cl.modules_contact.request.RewardReq
import com.cl.modules_contact.request.SyncTrendReq
import com.cl.modules_contact.response.CommentByMomentData
import com.cl.modules_contact.viewmodel.ContactCommentViewModel
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 动态详情页面 Content
 */
@AndroidEntryPoint
class ContactCommentActivity : BaseActivity<ContactAddCommentBinding>() {
    private val momentId by lazy {
        intent.getIntExtra(KEY_MOMENT_ID, 0)
    }

    @Inject
    lateinit var mViewModel: ContactCommentViewModel

    private val commentAdapter by lazy {
        // 包括内部的评论回复点击
        ContactCommentAdapter(mutableListOf(),
            replyAction = { replyData ->
            // 点击回复
            XPopup.Builder(this@ContactCommentActivity)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(true)
                .autoOpenSoftInput(true)
                .hasShadowBg(false)
                .moveUpToKeyboard(true)
                .asCustom(ReplyCommentPop(
                    context = this@ContactCommentActivity,
                    headPic = mViewModel.momentDetailData.value?.data?.avatarPicture,
                    nickName = mViewModel.momentDetailData.value?.data?.nickName,
                    commentContent = SpannedString.valueOf(replyData.comment),
                    commentText = null
                ) {
                    // 回复 评论
                    binding.tvCommentTxt.text = it
                    if (TextUtils.isEmpty(binding.tvCommentTxt.text)) return@ReplyCommentPop
                    mViewModel.reply(ReplyReq(comment = binding.tvCommentTxt.text.toString(), commentId = replyData.commentId, replyId = replyData.replyId))
                }).show()
        },
          likeAction = {
              mViewModel.updateLikeData(LikeReq(learnMoreId = null, likeId = it.replyId, type = KEY_REPLY))
              mViewModel.likeReq.value?.let { it1 -> mViewModel.like(it1) }
          },
          giftAction = { replyData, checkBox ->
              if (replyData.userId == mViewModel.userinfoBean?.userId) {
                  // 指定差值器动画
                  extracted(checkBox)
                  return@ContactCommentAdapter
              }
              XPopup.Builder(this@ContactCommentActivity)
                  .isDestroyOnDismiss(false)
                  .dismissOnTouchOutside(true)
                  .asCustom(
                      RewardPop(this@ContactCommentActivity, onRewardListener = {
                          mViewModel.reward(
                              RewardReq(
                                  oxygenNum = it,
                                  type = KEY_REPLY,
                                  relationId = replyData.replyId
                              )
                          )
                      })
                  ).show()
          }
        )
    }

    private val emojiAdapter by lazy {
        EmojiAdapter(mutableListOf())
    }

    /**
     * 表情集合
     */
    private val list by lazy {
        val emojiArray = mutableListOf("2764", "1F525", "1F44F", "1F603", "1F619", "1F918", "1F914", "1F62A", "1F62F", "1F633", "1F60E", "1F63A", "1F631")
        val a = mutableSetOf<String>()
        emojiArray.forEach {
            val hex = Integer.parseInt(it, 16)
            //将当前 16 进制数转换成字符数组
            val chars = Character.toChars(hex)
            //将当前字符数组转换成 TextView 可加载的 String 字符串
            val mEmojiString = String(chars)
            a.add(mEmojiString)
        }
        a
    }

    override fun initView() {
        // 评论适配器
        binding.rvComment.apply {
            layoutManager = LinearLayoutManager(this@ContactCommentActivity)
            adapter = commentAdapter
        }

        binding.rvEmoji.apply {
            layoutManager = LinearLayoutManager(this@ContactCommentActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = emojiAdapter
            emojiAdapter.setList(list)
        }

        initAdapter()
        initClick()
    }

    private fun initClick() {
        binding.tvPost.setOnClickListener {
            if (TextUtils.isEmpty(binding.tvCommentTxt.text)) return@setOnClickListener
            //  发表评论
            mViewModel.publish(PublishReq(comment = binding.tvCommentTxt.text.toString(), learnMoreId = null, momentId = mViewModel.momentDetailData.value?.data?.id.toString()))
        }

        binding.tvCommentTxt.setOnClickListener {
            XPopup.Builder(this@ContactCommentActivity)
                .isDestroyOnDismiss(false)
                .dismissOnTouchOutside(true)
                .autoOpenSoftInput(true)
                .hasShadowBg(false)
                .moveUpToKeyboard(true)
                .asCustom(ReplyCommentPop(
                    context = this@ContactCommentActivity, headPic = mViewModel.momentDetailData.value?.data?.avatarPicture, nickName = mViewModel.momentDetailData.value?.data?.nickName,
                    commentContent = null, commentText = binding.tvCommentTxt.text.toString()
                ) {
                    // 发表评论
                    binding.tvCommentTxt.text = it
                    if (TextUtils.isEmpty(binding.tvCommentTxt.text)) return@ReplyCommentPop
                    mViewModel.publish(PublishReq(comment = binding.tvCommentTxt.text.toString(), learnMoreId = null, momentId = mViewModel.momentDetailData.value?.data?.id.toString()))
                }).show()
        }
    }

    private fun initAdapter() {
        // 表情适配器
        emojiAdapter.addChildClickViewIds(com.cl.modules_contact.R.id.cl_emoji)
        emojiAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as? String
            when (view.id) {
                // 点击表情,
                com.cl.modules_contact.R.id.cl_emoji -> {
                    binding.tvCommentTxt.text = binding.tvCommentTxt.text.toString().plus(item)
                }
            }
        }

        // 回复适配器
        commentAdapter.addChildClickViewIds(com.cl.modules_contact.R.id.cl_reply_chat, com.cl.modules_contact.R.id.cl_reply_gift, com.cl.modules_contact.R.id.cl_reply_chat, com.cl.modules_contact.R.id.cl_reply_love)
        commentAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as? CommentByMomentData
            when (view.id) {
                // 点击回复
                com.cl.modules_contact.R.id.cl_reply_chat -> {
                    XPopup.Builder(this@ContactCommentActivity)
                        .isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(true)
                        .autoOpenSoftInput(true)
                        .hasShadowBg(false)
                        .moveUpToKeyboard(true)
                        .asCustom(ReplyCommentPop(
                            context = this@ContactCommentActivity,
                            headPic = mViewModel.momentDetailData.value?.data?.avatarPicture,
                            nickName = mViewModel.momentDetailData.value?.data?.nickName,
                            commentContent = SpannedString.valueOf(item?.comment),
                            commentText = null
                        ) {
                            // 回复 评论
                            binding.tvCommentTxt.text = it
                            if (TextUtils.isEmpty(binding.tvCommentTxt.text)) return@ReplyCommentPop
                            mViewModel.reply(ReplyReq(comment = binding.tvCommentTxt.text.toString(), commentId = item?.commentId, replyId = null))
                        }).show()
                }

                // 打赏
                com.cl.modules_contact.R.id.cl_reply_gift -> {
                    if (item?.userId == mViewModel.userinfoBean?.userId) {
                        // 指定差值器动画
                        extracted(view.findViewById<CheckBox>(com.cl.modules_contact.R.id.curing_box_gift))
                        return@setOnItemChildClickListener
                    }
                    XPopup.Builder(this@ContactCommentActivity)
                        .isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(true)
                        .asCustom(
                            RewardPop(this@ContactCommentActivity, onRewardListener = {
                                mViewModel.reward(
                                    RewardReq(
                                        oxygenNum = it,
                                        type = KEY_COMMENT,
                                        relationId = item?.commentId
                                    )
                                )
                            })
                        ).show()
                }

                // 点赞
                com.cl.modules_contact.R.id.cl_reply_love -> {
                    mViewModel.updateLikeData(LikeReq(learnMoreId = null, likeId = item?.commentId, type = KEY_COMMENT))
                    mViewModel.likeReq.value?.let { mViewModel.like(it) }
                }
            }
        }
    }

    /**
     * 差值器 左右抖动 动画
     */
    private fun extracted(checkBox: CheckBox) {
        ObjectAnimator.ofFloat(checkBox, "translationX", 0f, -100f, 0f, 100f, 0f).apply {
            duration = 1000
            repeatCount = 10
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.RESTART
        }.start()
    }

    @SuppressLint("SetTextI18n")
    override fun observe() {
        mViewModel.apply {
            // 打赏
            rewardData.observe(this@ContactCommentActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success {
                    mViewModel.commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                }
            })

            // 回复评论
            replyData.observe(this@ContactCommentActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success {
                    binding.tvCommentTxt.text = ""
                    mViewModel.commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                }
            })

            // 发表评论
            publishData.observe(this@ContactCommentActivity, resourceObserver {
                error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
                success {
                    binding.tvCommentTxt.text = ""
                    mViewModel.commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                }
            })


            commentListData.observe(this@ContactCommentActivity, resourceObserver {
                success {
                    if (null == data) return@success

                    val list = data
                    // 遍历data
                    list?.forEach { item ->
                        // 设置富文本
                        item.nickName = mViewModel.userinfoBean?.nickName
                        item.replys?.forEach { reply ->
                            reply.nickName = mViewModel.userinfoBean?.nickName
                        }
                    }
                    // 设置添加了nickName的数据
                    commentAdapter.setList(list)
                }
            })

            momentDetailData.observe(this@ContactCommentActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }

                success {
                    // 是否点赞
                    mViewModel.updateCurrentPosition(data?.isPraise ?: 0)

                    /**
                     * 实现九宫格里面相关的内容
                     */
                    // 设置富文本
                    binding.tvDesc.text = getContents(data?.content, data?.mentions)
                    binding.tvNum.text = convertTime(data?.createTime)
                    binding.nineGridView.apply {
                        adapter = data?.imageUrls?.let {
                            // 手动添加图片集合
                            val urlList = mutableListOf<String>()
                            it.forEach { data -> data.imageUrl.let { it1 -> urlList.add(it1) } }
                            NineGridAdapter(this@ContactCommentActivity, urlList)
                        }
                    }

                    /**
                     * 实现各种相关的点击事件
                     */
                    binding.clLove.setOnClickListener {
                        // 点赞
                        val isPraise = mViewModel.currentPosition.value
                        mViewModel.updateLikeData(LikeReq(learnMoreId = data?.learnMoreId, likeId = data?.id.toString(), type = KEY_MOMENTS))
                        if (isPraise == 0) {
                            mViewModel.likeReq.value?.let { it1 -> mViewModel.like(it1) }
                        } else {
                            mViewModel.likeReq.value?.let { it1 -> mViewModel.unlike(it1) }
                        }
                    }
                    binding.clGift.setOnClickListener {
                        //  打赏
                        if (mViewModel.momentDetailData.value?.data?.userId == mViewModel.userinfoBean?.userId) {
                            extracted(binding.curingBoxGift)
                            return@setOnClickListener
                        }
                        XPopup.Builder(this@ContactCommentActivity)
                            .isDestroyOnDismiss(false)
                            .dismissOnTouchOutside(true)
                            .asCustom(
                                RewardPop(this@ContactCommentActivity, onRewardListener = {
                                    mViewModel.reward(
                                        RewardReq(
                                            momentsId = mViewModel.momentDetailData.value?.data?.id?.toString(),
                                            oxygenNum = it,
                                            type = KEY_MOMENTS,
                                            relationId = mViewModel.momentDetailData.value?.data?.id.toString()
                                        )
                                    )
                                })
                            ).show()
                    }
                    binding.clChat.setOnClickListener {
                        // 聊天
                        XPopup.Builder(this@ContactCommentActivity)
                            .isDestroyOnDismiss(false)
                            .dismissOnTouchOutside(true)
                            .autoOpenSoftInput(true)
                            .hasShadowBg(false)
                            .moveUpToKeyboard(true)
                            .asCustom(ReplyCommentPop(
                                context = this@ContactCommentActivity,
                                headPic = mViewModel.momentDetailData.value?.data?.avatarPicture,
                                nickName = mViewModel.momentDetailData.value?.data?.nickName,
                                commentText = binding.tvCommentTxt.text.toString(),
                                commentContent = SpannedString.valueOf(binding.tvDesc.text)
                            ) {
                                // 发表评论
                                binding.tvCommentTxt.text = it
                                if (TextUtils.isEmpty(binding.tvCommentTxt.text)) return@ReplyCommentPop
                                mViewModel.publish(PublishReq(comment = binding.tvCommentTxt.text.toString(), learnMoreId = null, momentId = mViewModel.momentDetailData.value?.data?.id.toString()))
                            }).show()
                    }
                    binding.rlPoint.setOnClickListener {
                        //  三个点
                        XPopup.Builder(this@ContactCommentActivity)
                            .popupPosition(PopupPosition.Left)
                            .dismissOnTouchOutside(true)
                            .isClickThrough(false)  //点击透传
                            .hasShadowBg(true) // 去掉半透明背景
                            //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                            .atView(binding.rlPoint)
                            .isCenterHorizontal(false)
                            .asCustom(
                                this@ContactCommentActivity.let {
                                    ContactPotionPop(
                                        it,
                                        deleteAction = {
                                            //  删除
                                            mViewModel.delete(DeleteReq(momentId = data?.id.toString()))
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
                                                            /*mViewModel.report(ReportReq(momentId = data?.id.toString(), reportContent = txt))*/
                                                        })
                                                ).show()
                                        },
                                        itemSwitchAction = { isCheck ->
                                            // 关闭分享
                                            mViewModel.public(SyncTrendReq(syncTrend = if (isCheck) 1 else 0, momentId = data?.id.toString()))
                                        },
                                        isShowReport = data?.userId.toString() == mViewModel.userinfoBean?.userId
                                    )
                                        .setBubbleBgColor(Color.WHITE) //气泡背景
                                        .setArrowWidth(XPopupUtils.dp2px(this@ContactCommentActivity, 3f))
                                        .setArrowHeight(
                                            XPopupUtils.dp2px(
                                                this@ContactCommentActivity,
                                                3f
                                            )
                                        )
                                        //.setBubbleRadius(100)
                                        .setArrowRadius(
                                            XPopupUtils.dp2px(
                                                this@ContactCommentActivity,
                                                2f
                                            )
                                        )
                                }
                            ).show()
                    }
                    binding.clAvatar.setOnClickListener {
                        // todo 点击头像、跳转到自己的发帖列表里面
                    }

                }
            })

            likeData.observe(this@ContactCommentActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }

                success {
                    mViewModel.likeReq.value?.let {
                        when(it.type) {
                            KEY_COMMENT -> {
                                // 点赞成功
                                binding.curingBoxLove.isChecked = true
                                binding.tvLoveNum.text = (binding.tvLoveNum.text.toString().toInt() + 1).toString()
                                mViewModel.updateCurrentPosition(1)
                            }
                            KEY_MOMENTS -> {
                                mViewModel.commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                            }
                            
                            KEY_REPLY -> {
                                mViewModel.commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                            }
                        }
                    }

                }
            })

            unlikeData.observe(this@ContactCommentActivity, resourceObserver {
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }

                success {
                    mViewModel.likeReq.value?.let {
                        when(it.type) {
                            KEY_COMMENT -> {
                                // 取消点赞成功
                                binding.curingBoxLove.isChecked = false
                                binding.tvLoveNum.text = (binding.tvLoveNum.text.toString().toInt() - 1).toString()
                                mViewModel.updateCurrentPosition(0)
                            }
                            KEY_MOMENTS -> {
                                mViewModel.commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                            }
                            
                            KEY_REPLY -> {
                                mViewModel.commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                            }
                        }
                    }
                }
            })

            deleteData.observe(this@ContactCommentActivity, resourceObserver {
                error { errorMsg, _ ->
                    ToastUtil.shortShow(errorMsg)
                }

                success {
                    // 删除成功
                    finish()
                }
            })

        }
    }


    override fun initData() {
        mViewModel.momentDetail(momentsId = momentId)
        mViewModel.commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
    }

    override fun ContactAddCommentBinding.initBinding() {
        binding.apply {
            lifecycleOwner = this@ContactCommentActivity
            viewModel = mViewModel
            executePendingBindings()
        }
    }

    private fun convertTime(createTime: String? = null): String {
        var text = ""
        createTime?.let {
            // 2023-04-20 10:04:52
            DateHelper.getTimestamp(it, "yyyy-MM-dd HH:mm:ss").apply {
                text = DateHelper.convert((this)).toString()
            }
        }
        return text
    }

    /**
     * 获取内容
     */
    private fun getContents(content: String?, mentions: MutableList<com.cl.modules_contact.response.Mention>?): SpannedString {
        var contents = content ?: ""
        mentions?.forEach {
            it.nickName.let { nickName ->
                contents = contents.replace(nickName, "").trim()
            }
        }

        return buildSpannedString {
            color(getColor(R.color.mainColor)) {
                mentions?.forEach {
                    append("${it.nickName} ")
                }
            }
            append(contents)
        }

    }


    companion object {
        const val KEY_MOMENT_ID = "momentId"
        const val KEY_LEARN_MORE_ID = "learnMoreId"

        // 动态
        const val KEY_MOMENTS = "moments"
        // 评论
        const val KEY_COMMENT = "comment"
        // 回复
        const val KEY_REPLY = "reply"
    }
}