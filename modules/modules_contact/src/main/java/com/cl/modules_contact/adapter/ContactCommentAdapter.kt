package com.cl.modules_contact.adapter

import android.graphics.Color
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.text.SpannedString
import android.widget.CheckBox
import androidx.camera.core.impl.utils.CameraOrientationUtil
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactItemCommentBinding
import com.cl.modules_contact.pop.ContactDeletePop
import com.cl.modules_contact.response.CommentByMomentData
import com.cl.modules_contact.response.NewPageData
import com.cl.modules_contact.response.ReplyData
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils

/**
 * 评论适配器
 *
 * @param replyAction 内部RecyclerView的点击时间
 */
class ContactCommentAdapter(
    data: MutableList<CommentByMomentData>?,
    private val isSelfTrend: Boolean? = false,
    private val replyAction: ((replyData: CommentByMomentData.Replys) -> Unit)? = null,
    private val likeAction: ((replyData: CommentByMomentData.Replys) -> Unit)? = null,
    private val giftAction: ((replyData: CommentByMomentData.Replys, view: CheckBox) -> Unit)? = null,
    private val onDeleteAction: ((replyData: CommentByMomentData.Replys) -> Unit)? = null,
    private val onCopyAction: ((replyData: CommentByMomentData.Replys) -> Unit)? = null,
) : BaseQuickAdapter<CommentByMomentData, BaseDataBindingHolder<ContactItemCommentBinding>>(R.layout.contact_item_comment, data) {

    val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }

    override fun convert(holder: BaseDataBindingHolder<ContactItemCommentBinding>, item: CommentByMomentData) {
        holder.dataBinding?.apply {
            data = item
            adapter = this@ContactCommentAdapter
            lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
            executePendingBindings()
        }


        // 设置富文本
        holder.setText(R.id.tvDesc, getContents(item.commentName, item.parentComentId, item.comment, holder.layoutPosition))
        holder.setText(R.id.tv_create_time, convertTime(item.createTime))

        // 设置回复的适配器
        holder.getView<RecyclerView>(R.id.rv_reply).apply {
            layoutManager = LinearLayoutManager(context)
            val replyAdapter = ContactReplyAdapter(item.replys)
            adapter = replyAdapter

            replyAdapter.addChildClickViewIds(R.id.cl_reply_chat, R.id.cl_reply_gift, R.id.cl_reply_love)
            replyAdapter.setOnItemChildClickListener { adapter, view, position ->
                val reply = adapter.data[position] as CommentByMomentData.Replys
                when (view.id) {
                    R.id.cl_reply_chat -> { // 跳转到聊天界面
                        replyAction?.invoke(reply)
                    }

                    R.id.cl_reply_love -> { // 回复点赞
                        likeAction?.invoke(reply)
                    }

                    R.id.cl_reply_gift -> { // 回复打赏
                        giftAction?.invoke(reply, view.findViewById(R.id.curing_box_gift))
                    }
                }
            }
            replyAdapter.addChildLongClickViewIds(R.id.tvDesc)
            replyAdapter.setOnItemChildLongClickListener { adapter, view, position ->
                val reply = adapter.data[position] as CommentByMomentData.Replys
                when (view.id) {
                    R.id.tvDesc -> { // 长按弹窗啥的。
                       /* if (reply.userId != userinfoBean?.userId && isSelfTrend == false) {
                            return@setOnItemChildLongClickListener false
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
                            .offsetX(- (view.measuredWidth / 2.2).safeToInt())
                            .atView(view)
                            .asCustom(
                                ContactDeletePop(context,
                                    isShowDelete = !(reply.userId != userinfoBean?.userId && isSelfTrend == false),
                                    onDeleteAction = {
                                    onDeleteAction?.invoke(reply)
                                }, onCopyAction = {
                                    onCopyAction?.invoke(reply)
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
                true
            }
        }

    }

    private fun convertTime(createTime: String? = null): String {
        var text = ""
        createTime?.let { // 2023-04-20 10:04:52
            DateHelper.getTimestamp(it, "yyyy-MM-dd HH:mm:ss").apply {
                text = DateHelper.convert((this)).toString()
            }
        }
        return text
    }

    /**
     * 获取内容
     * @param commentName 回复者的名字
     * @param parentComentId 查看是否是有在上一个回复中回复的
     * @param comment 回复的内容
     */
    private fun getContents(commentName: String?, parentComentId: String?, comment: String?, position: Int?): SpannedString {/*parentComentId?.let {
            buildSpannedString {
                bold { append(commentName) }
                color(context.getColor(com.cl.common_base.R.color.mainColor)) {
                    // 添加上一级的回复者名字
                    append()
                }

            }
        } ?: run {
            buildSpannedString {
                bold { append(commentName) }
                append(comment)
            }
        }*/

        return buildSpannedString {
            bold { append(commentName) }
            append(" $comment")
        }
    }
}