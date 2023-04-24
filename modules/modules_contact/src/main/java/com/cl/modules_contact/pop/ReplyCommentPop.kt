package com.cl.modules_contact.pop

import android.content.Context
import android.text.SpannedString
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.util.ViewUtils
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.EmojiAdapter
import com.cl.modules_contact.databinding.ContactPopReplyCommentBinding
import com.lxj.xpopup.core.BottomPopupView


/**
 * 回复评论弹窗
 */
class ReplyCommentPop(
    context: Context,
    val headPic: String? = "",
    val nickName: String? = "",
    val commentContent: SpannedString? = null,
    val commentText: String? = null,
    private val postAction: ((commentTxt: String) -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.contact_pop_reply_comment
    }

    private val emojiAdapter by lazy {
        EmojiAdapter(mutableListOf())
    }

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

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<ContactPopReplyCommentBinding>(popupImplView)?.apply {
            lifecycleOwner = this@ReplyCommentPop
            headPic = this@ReplyCommentPop.headPic
            nickName = this@ReplyCommentPop.nickName
            commentTxt = this@ReplyCommentPop.commentText
            executePendingBindings()
            ViewUtils.setVisible(commentContent != null, clCommentContent)

            // todo commentTxt 带过来的内容需要显示SpannableString 也就是@
            tvDesc.text = commentContent

            rvEmoji.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rvEmoji.adapter = emojiAdapter
            emojiAdapter.setList(emojiList)

            emojiAdapter.addChildClickViewIds(R.id.cl_emoji)
            emojiAdapter.setOnItemChildClickListener { adapter, view, position ->
                val emoji = emojiAdapter.getItem(position)
                when (view.id) {
                    R.id.cl_emoji -> {
                        tvCommentTxt.setText(tvCommentTxt.text.toString().plus(emoji))
                        // 将光标设置到新增完表情的右侧
                        tvCommentTxt.setSelection(tvCommentTxt.text.length)
                    }
                }
            }

            tvPost.setOnClickListener {
                postAction?.invoke(tvCommentTxt.text.toString())
                dismiss()
            }

            ivClose.setOnClickListener {
                dismiss()
            }

        }
    }
}