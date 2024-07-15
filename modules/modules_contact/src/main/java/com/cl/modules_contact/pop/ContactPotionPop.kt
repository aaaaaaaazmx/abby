package com.cl.modules_contact.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.util.ViewUtils
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactPotionPopBinding
import com.lxj.xpopup.core.BubbleAttachPopupView

class ContactPotionPop(
    private val context: Context,
    private val permission: String? = null, // 是否是管理员
    private val isShowReport: Boolean = false,
    private val isShowShareToPublic: Boolean = false,
    private val fisItemSwitchIsCheck: Boolean = true,
    private val isFollow: Boolean = false,
    private val deleteAction: (() -> Unit)? = null,
    private val reportAction: (() -> Unit)? = null,
    private val shareAction: (() -> Unit)? = null,
    private val itemSwitchAction: ((isCheck: Boolean) -> Unit)? = null,
    private val followAction: (() -> Unit)? = null,
    private val buryAction: (() -> Unit)? = null,
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.contact_potion_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<ContactPotionPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@ContactPotionPop
            executePendingBindings()

            // 不是举报就隐藏，其实就是查看trend是不是自己的
            ViewUtils.setVisible(!isShowReport, clReport, clFollow, vv1, vv3)
            // 显示帖子下称按钮
            ViewUtils.setVisible(!isShowReport && permission.safeToInt() == 1, clDown)

            // 是举报就显示，删除和分享
            ViewUtils.setVisible(isShowReport, clDelete, clShare, vv)
            // 是分享，其实就是查看trend是不是自己的
            ViewUtils.setVisible(isShowShareToPublic, clShare)

            tvFollow.text = if (isFollow) "Unfollow" else "Follow"

            clDelete.setOnClickListener {
                deleteAction?.invoke()
                dismiss()
            }

            clReport.setOnClickListener {
                reportAction?.invoke()
                dismiss()
            }

            clShare.setOnClickListener {
                shareAction?.invoke()
                dismiss()
            }

            clFollow.setOnClickListener {
                followAction?.invoke()
                dismiss()
            }

            clDown.setSafeOnClickListener {
                buryAction?.invoke()
                dismiss()
            }

            // 是否开启分享
            fisItemSwitch.isChecked = fisItemSwitchIsCheck

            fisItemSwitch.setOnCheckedChangeListener { _, isChecked ->
                itemSwitchAction?.invoke(isChecked)
                dismiss()
            }
        }
    }
}