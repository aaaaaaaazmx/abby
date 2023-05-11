package com.cl.modules_contact.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.common_base.util.ViewUtils
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactPotionPopBinding
import com.lxj.xpopup.core.BubbleAttachPopupView

class ContactPotionPop(
    private val context: Context,
    private val isShowReport: Boolean = false,
    private val isShowShareToPublic: Boolean = true,
    private val deleteAction: (() -> Unit)? = null,
    private val reportAction: (() -> Unit)? = null,
    private val shareAction: (() -> Unit)? = null,
    private val itemSwitchAction: ((isCheck: Boolean) -> Unit)? = null,
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.contact_potion_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<ContactPotionPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@ContactPotionPop
            executePendingBindings()

            ViewUtils.setVisible(!isShowReport, clReport)
            ViewUtils.setVisible(isShowReport, clDelete, clShare, vv)
            // ViewUtils.setVisible(isShowShareToPublic, clShare)

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

            fisItemSwitch.setOnCheckedChangeListener { _, isChecked ->
                itemSwitchAction?.invoke(isChecked)
                dismiss()
            }
        }
    }
}