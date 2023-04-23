package com.cl.modules_contact.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.SpannedString
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.R
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.adapter.ContactCommentAdapter
import com.cl.modules_contact.adapter.NineGridAdapter
import com.cl.modules_contact.databinding.ContactAddCommentBinding
import com.cl.modules_contact.pop.ContactPotionPop
import com.cl.modules_contact.pop.ContactReportPop
import com.cl.modules_contact.request.CommentByMomentReq
import com.cl.modules_contact.request.DeleteReq
import com.cl.modules_contact.request.LikeReq
import com.cl.modules_contact.request.SyncTrendReq
import com.cl.modules_contact.viewmodel.ContactCommentViewModel
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 聊天页面
 */
@AndroidEntryPoint
class ContactCommentActivity : BaseActivity<ContactAddCommentBinding>() {
    private val momentId by lazy {
        intent.getIntExtra(KEY_MOMENT_ID, 0)
    }

    @Inject
    lateinit var mViewModel: ContactCommentViewModel

    private val commentAdapter by lazy {
        ContactCommentAdapter(mutableListOf())
    }

    override fun initView() {
        // 评论适配器
        binding.rvComment.apply {
            layoutManager = LinearLayoutManager(this@ContactCommentActivity)
            adapter = commentAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    override fun observe() {
        mViewModel.apply {
            commentListData.observe(this@ContactCommentActivity, resourceObserver {
                success {
                   if (null == data) return@success

                    val list = data
                    // 遍历data
                    list?.forEach { item ->
                        // 设置富文本
                        item.nickName = mViewModel.userinfoBean?.nickName
                        item.replys?.forEach {reply ->
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
                        val isPraise =  mViewModel.currentPosition.value
                        if (isPraise == 0) {
                            mViewModel.like(LikeReq(learnMoreId = data?.learnMoreId, likeId = data?.id.toString(), type = "moments"))
                        } else {
                            mViewModel.unlike(LikeReq(learnMoreId = data?.learnMoreId, likeId = data?.id.toString(), type = "moments"))
                        }
                    }
                    binding.clGift.setOnClickListener {
                        // todo 打赏
                    }
                    binding.clChat.setOnClickListener {
                        // todo 聊天
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
                                                        onConfirmAction = {txt ->
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
                    // 点赞成功
                    binding.curingBoxLove.isChecked = true
                    binding.tvLoveNum.text = (binding.tvLoveNum.text.toString().toInt() + 1).toString()
                    mViewModel.updateCurrentPosition(1)
                }
            })
            
            unlikeData.observe(this@ContactCommentActivity, resourceObserver { 
                error { errorMsg, code ->
                    ToastUtil.shortShow(errorMsg)
                }
                
                success {
                    // 取消点赞成功
                    binding.curingBoxLove.isChecked = false
                    binding.tvLoveNum.text = (binding.tvLoveNum.text.toString().toInt() - 1).toString()
                    mViewModel.updateCurrentPosition(0)
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
            it.nickName?.let { nickName ->
                contents = contents.replace("$nickName", "").trim()
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
        const val KEY_NICK_NAME = "nickName"
        const val KEY_AVATAR = "avatar"
    }
}