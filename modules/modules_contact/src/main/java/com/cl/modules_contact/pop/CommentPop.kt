package com.cl.modules_contact.pop

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.SpannedString
import android.text.TextUtils
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.CheckBox
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.mtjsoft.barcodescanning.utils.SoundPoolUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildLongClickListener
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.util.json.GSON
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ContactCommentAdapter
import com.cl.modules_contact.adapter.EmojiAdapter
import com.cl.modules_contact.databinding.ContactPopCommentBinding
import com.cl.modules_contact.request.CommentByMomentReq
import com.cl.common_base.bean.LikeReq
import com.cl.modules_contact.request.PublishReq
import com.cl.modules_contact.request.ReplyReq
import com.cl.common_base.bean.RewardReq
import com.cl.common_base.pop.RewardPop
import com.cl.modules_contact.response.CommentByMomentData
import com.cl.modules_contact.response.PublishData
import com.cl.modules_contact.service.HttpContactApiService
import com.cl.modules_contact.ui.ContactCommentActivity
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * 动态首页聊天弹窗
 *
 * @param isSelfTrend 是否是自己发布的帖子
 * @param momentId 动态ID
 * @param onDismissAction 弹窗消失监听
 */
class CommentPop(
    context: Context,
    private val isSelfTrend: Boolean? = false,
    private val momentId: Int? = null,
    private val onDismissAction: ((data: MutableList<CommentByMomentData>?) -> Unit)? = null
) : BottomPopupView(context) {
    private val service = ServiceCreators.create(HttpContactApiService::class.java)
    val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    override fun getImplLayoutId(): Int {
        return R.layout.contact_pop_comment
    }

    private var binding: ContactPopCommentBinding? = null

    private val emojiAdapter by lazy {
        EmojiAdapter(mutableListOf())
    }

    /**
     * 表情集合
     */
    private val emojiList by lazy {
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

    private val commentAdapter by lazy {
        // 包括内部的评论回复点击
        ContactCommentAdapter(mutableListOf(), isSelfTrend,
            replyAction = { replyData ->
                // 点击回复
                XPopup.Builder(context)
                    .isDestroyOnDismiss(false)
                    .dismissOnTouchOutside(true)
                    .autoOpenSoftInput(true)
                    .hasShadowBg(false)
                    .moveUpToKeyboard(true)
                    .asCustom(ReplyCommentPop(
                        context = context,
                        headPic = userinfoBean?.avatarPicture,
                        nickName = userinfoBean?.nickName,
                        commentContent = SpannedString.valueOf(replyData.comment),
                        commentText = null
                    ) {
                        // 回复 评论
                        binding?.tvCommentTxt?.text = it
                        if (TextUtils.isEmpty(binding?.tvCommentTxt?.text)) return@ReplyCommentPop
                        reply(ReplyReq(comment = binding?.tvCommentTxt?.text.toString(), commentId = replyData.commentId, replyId = replyData.replyId))
                    }).show()
            },
            likeAction = {
                updateLikeData(LikeReq(learnMoreId = null, likeId = it.replyId, type = ContactCommentActivity.KEY_REPLY))
                if (it.isPraise == 0) {
                    likeReq.value?.let { req -> like(req) }
                } else {
                    likeReq.value?.let { req -> unlike(req) }
                }

                SoundPoolUtil.instance.startVibrator(context)
            },
            giftAction = { replyData, checkBox ->
                if (replyData.userId == userinfoBean?.userId) {
                    // 指定差值器动画
                    extracted(checkBox)
                    return@ContactCommentAdapter
                }
                XPopup.Builder(context)
                    .isDestroyOnDismiss(false)
                    .dismissOnTouchOutside(true)
                    .asCustom(
                        RewardPop(context, onRewardListener = {
                            reward(
                                RewardReq(
                                    oxygenNum = it,
                                    type = ContactCommentActivity.KEY_REPLY,
                                    relationId = replyData.replyId
                                )
                            )
                        })
                    ).show()
            },
            onDeleteAction = {
                //  删除评论, 需要是自己发的帖子
                it.replyId?.let { it1 -> deleteReply(it1) }
            },
            onCopyAction = {
                // 复制评论，需要是自己发的帖子
                // 复制内容
                val cm: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                // 创建普通字符型ClipData
                val mClipData = ClipData.newPlainText("Connect", it.comment)
                // 将ClipData内容放到系统剪贴板里。
                cm?.setPrimaryClip(mClipData)
            }
        )
    }

    /**
     * 删除回复
     */
    private val _deleteReplyData = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val deleteReplyData: LiveData<Resource<com.cl.common_base.BaseBean>> = _deleteReplyData
    fun deleteReply(replyId: String) = lifecycleScope.launch {
        service.deleteReply(replyId).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            _deleteReplyData.value = Resource.Loading()
        }.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _deleteReplyData.value = it
            when (it) {
                is Resource.Success -> {
                    // 点赞成功
                    commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                }

                else -> {}
            }
        }
    }


    /**
     * 删除评论
     */
    private val _deleteCommentData = MutableLiveData<Resource<com.cl.common_base.BaseBean>>()
    val deleteCommentData: LiveData<Resource<com.cl.common_base.BaseBean>> = _deleteCommentData
    fun deleteComment(commentId: String) = lifecycleScope.launch {
        service.deleteComment(commentId).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            _deleteCommentData.value = Resource.Loading()
        }.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _deleteCommentData.value = it
            when (it) {
                is Resource.Success -> {
                    // 点赞成功
                    commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                }

                else -> {}
            }
        }
    }


    private val _commentListData = MutableLiveData<Resource<MutableList<CommentByMomentData>>>()
    private val commentListData: LiveData<Resource<MutableList<CommentByMomentData>>> = _commentListData
    private fun commentList(req: CommentByMomentReq) = lifecycleScope.launch {
        service.getCommentByMomentId(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _commentListData.value = it
        }
    }

    private fun like(req: LikeReq) = lifecycleScope.launch {
        service.like(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            if (it is Resource.Success) {
                likeReq.value?.let { likeReq ->
                    when (likeReq.type) {
                        ContactCommentActivity.KEY_COMMENT -> {
                            // 点赞成功
                            commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                        }

                        ContactCommentActivity.KEY_MOMENTS -> {
                            // 点赞成功
                            /*binding.curingBoxLove.isChecked = true
                            binding.tvLoveNum.text = (binding.tvLoveNum.text.toString().toInt() + 1).toString()
                            updateCurrentPosition(1)*/
                        }

                        ContactCommentActivity.KEY_REPLY -> {
                            commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun reward(req: RewardReq) = lifecycleScope.launch {
        service.reward(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            if (it is Resource.Success) {
                // 打赏成功
                commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
            }
        }
    }


    private fun unlike(req: LikeReq) = lifecycleScope.launch {
        service.unlike(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {

            if (it is Resource.Success) {
                // 取消点赞成功
                // unLike
                likeReq.value?.let { likeReq ->
                    when (likeReq.type) {
                        ContactCommentActivity.KEY_COMMENT -> {
                            commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                        }

                        ContactCommentActivity.KEY_MOMENTS -> {
                            // 取消点赞成功
                            /*binding.curingBoxLove.isChecked = false
                            binding.tvLoveNum.text = (binding.tvLoveNum.text.toString().toInt() - 1).toString()
                            updateCurrentPosition(0)*/
                        }

                        ContactCommentActivity.KEY_REPLY -> {
                            commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                        }

                        else -> {}
                    }
                }
            }
        }
    }


    fun reply(req: ReplyReq) = lifecycleScope.launch {
        service.reply(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            if (it is Resource.Success) {
                // reply
                commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
                binding?.tvCommentTxt?.text = ""
            }
        }
    }

    /**
     * 发表评论 publish
     */
    private val _publishData = MutableLiveData<Resource<PublishData>>()
    val publishData: LiveData<Resource<PublishData>> = _publishData
    private fun publish(req: PublishReq) = lifecycleScope.launch {
        service.publish(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _publishData.value = it
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


    /**
     * likeData
     */
    private val _likeReq = MutableLiveData<LikeReq>()
    val likeReq: LiveData<LikeReq> = _likeReq
    private fun updateLikeData(req: LikeReq) {
        _likeReq.value = req
    }

    // 获取当前的点中的position
    private val _currentPosition = MutableLiveData<Int>(0)
    val currentPosition: LiveData<Int> = _currentPosition
    private fun updateCurrentPosition(position: Int) {
        _currentPosition.value = position
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<ContactPopCommentBinding>(popupImplView)?.apply {
            avatarPicture = userinfoBean?.avatarPicture
            nickName = userinfoBean?.nickName
            lifecycleOwner = this@CommentPop
            executePendingBindings()

            // 请求一次评论列表
            commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))


            rvComment.apply {
                layoutManager = LinearLayoutManager(context)
                rvComment.adapter = commentAdapter
            }


            rvEmoji.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = emojiAdapter
                emojiAdapter.setList(emojiList)
            }

            ivClose.setOnClickListener { dismiss() }

            initObserver()
            initAdapter()

            tvPost.setOnClickListener {
                logI("12312312 tvPost")
                if (TextUtils.isEmpty(tvCommentTxt.text)) return@setOnClickListener
                //  发表评论
                publish(PublishReq(comment = tvCommentTxt.text.toString(), learnMoreId = null, momentId = momentId.toString()))
            }

            tvCommentTxt.setOnClickListener {
                showSoft()
            }
        }
    }

    /**
     * 弹出软键盘、评论
     */
    private fun ContactPopCommentBinding.showSoft() {
        XPopup.Builder(context)
            .isDestroyOnDismiss(false)
            .dismissOnTouchOutside(true)
            .autoOpenSoftInput(true)
            .hasShadowBg(false)
            .moveUpToKeyboard(true)
            .asCustom(ReplyCommentPop(
                context = context, headPic = userinfoBean?.avatarPicture, nickName = userinfoBean?.nickName,
                commentContent = null, commentText = tvCommentTxt.text.toString()
            ) {
                // 发表评论
                tvCommentTxt.text = it
                if (TextUtils.isEmpty(tvCommentTxt.text)) return@ReplyCommentPop
                publish(PublishReq(comment = tvCommentTxt.text.toString(), learnMoreId = null, momentId = momentId.toString()))
            }).show()
    }

    private fun initClick() {

    }

    private fun initObserver() {
        commentListData.observe(this@CommentPop, resourceObserver {
            error { errorMsg, _ ->
                ToastUtil.shortShow(errorMsg)
            }

            success {
                // 隐藏和显示空背景
                if (null == data) return@success
                ViewUtils.setVisible(data?.size == 0, binding?.ivEmptyBg)
                if (data?.size == 0) {
                    binding?.showSoft()
                }

                val list = data
                // 遍历data
                list?.forEach { item ->
                    // 设置富文本
                    item.nickName = userinfoBean?.nickName
                    item.replys?.forEach { reply ->
                        reply.nickName = userinfoBean?.nickName
                    }
                }
                // 设置添加了nickName的数据
                commentAdapter.setList(list)
            }
        })


        // 发表评论
        publishData.observe(this@CommentPop, resourceObserver {
            error { errorMsg, _ -> ToastUtil.shortShow(errorMsg) }
            success {
                binding?.tvCommentTxt?.text = ""
                commentList(CommentByMomentReq(momentId = momentId, learnMoreId = null, size = 50, current = 1))
            }
        })
    }

    private fun initAdapter() {
        // 表情适配器
        emojiAdapter.addChildClickViewIds(R.id.cl_emoji)
        emojiAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as? String
            when (view.id) {
                // 点击表情,
                R.id.cl_emoji -> {
                    binding?.tvCommentTxt?.text = binding?.tvCommentTxt?.text.toString().plus(item)
                }
            }
        }

        // 回复适配器
        commentAdapter.addChildClickViewIds(R.id.cl_reply_chat, R.id.cl_reply_gift, R.id.cl_reply_chat, R.id.cl_reply_love)
        commentAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as? CommentByMomentData
            when (view.id) {
                // 点击回复
                R.id.cl_reply_chat -> {
                    XPopup.Builder(context)
                        .isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(true)
                        .autoOpenSoftInput(true)
                        .hasShadowBg(false)
                        .moveUpToKeyboard(true)
                        .asCustom(ReplyCommentPop(
                            context = context,
                            headPic = userinfoBean?.avatarPicture,
                            nickName = userinfoBean?.nickName,
                            commentContent = SpannedString.valueOf(item?.comment),
                            commentText = null
                        ) {
                            // 回复 评论
                            binding?.tvCommentTxt?.text = it
                            if (TextUtils.isEmpty(binding?.tvCommentTxt?.text)) return@ReplyCommentPop
                            reply(ReplyReq(comment = binding?.tvCommentTxt?.text.toString(), commentId = item?.commentId, replyId = null))
                        }).show()
                }

                // 打赏
                R.id.cl_reply_gift -> {
                    if (item?.userId == userinfoBean?.userId) {
                        // 指定差值器动画
                        extracted(view.findViewById(R.id.curing_box_gift))
                        return@setOnItemChildClickListener
                    }
                    XPopup.Builder(context)
                        .isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(true)
                        .asCustom(
                            RewardPop(context, onRewardListener = {
                                reward(
                                    RewardReq(
                                        oxygenNum = it,
                                        type = ContactCommentActivity.KEY_COMMENT,
                                        relationId = item?.commentId
                                    )
                                )
                            })
                        ).show()
                }

                // 点赞
                R.id.cl_reply_love -> {
                    updateLikeData(LikeReq(learnMoreId = null, likeId = item?.commentId, type = ContactCommentActivity.KEY_COMMENT))
                    if (item?.isPraise == 0) {
                        likeReq.value?.let { like(it) }
                    } else {
                        likeReq.value?.let { unlike(it) }
                    }
                    SoundPoolUtil.instance.startVibrator(context)
                }
            }
        }

        // 长按删除和回复的适配器
        commentAdapter.addChildLongClickViewIds(R.id.tvDesc)
        commentAdapter.setOnItemChildLongClickListener(OnItemChildLongClickListener { adapter, view, position ->
            // 判断当前
            val item = adapter.getItem(position) as? CommentByMomentData
            when (view.id) {
                R.id.tvDesc -> {
                   /* if (item?.userId != userinfoBean?.userId && isSelfTrend == false) {
                        return@OnItemChildLongClickListener false
                    }*/
                    // 长按弹窗
                    XPopup.Builder(context)
                        .popupPosition(PopupPosition.Top)
                        .isDestroyOnDismiss(false)
                        .dismissOnTouchOutside(true)
                        .isCenterHorizontal(true)
                        .isClickThrough(false)  //点击透传
                        .hasShadowBg(false) // 去掉半透明背景
                        .offsetY(0)
                        .offsetX(-(view.measuredWidth / 2.2).toInt())
                        .atView(view)
                        .asCustom(
                            ContactDeletePop(context,
                                isShowDelete = !(item?.userId != userinfoBean?.userId && isSelfTrend == false),
                                onDeleteAction = {
                                //  删除评论
                                item?.commentId?.let { deleteComment(it) }
                            }, onCopyAction = {
                                //  复制评论
                                // 复制内容
                                val cm: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                // 创建普通字符型ClipData
                                val mClipData = ClipData.newPlainText("Connect", item?.comment)
                                // 将ClipData内容放到系统剪贴板里。
                                cm?.setPrimaryClip(mClipData)
                            }).setBubbleBgColor(context.getColor(com.cl.common_base.R.color.mainColor)) //气泡背景
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
                        ).show()
                }
            }
            return@OnItemChildLongClickListener true
        })
    }


    override fun beforeDismiss() {
        super.beforeDismiss()
        // 当消失的时候、就直接更新评论列表
        onDismissAction?.invoke(commentListData.value?.data)
    }

}