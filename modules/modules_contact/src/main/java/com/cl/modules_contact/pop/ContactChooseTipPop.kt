package com.cl.modules_contact.pop

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactChooserTipPopBinding
import com.lxj.xpopup.core.BubbleAttachPopupView

class ContactChooseTipPop(
    context: Context,
    private val onPhotoPostAction: (() -> Unit)? = null,
    private val onReelPostAction: (() -> Unit)? = null,
): BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.contact_chooser_tip_pop
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<ContactChooserTipPopBinding>(popupImplView)?.apply {
           tvPhotoPost.setOnClickListener {
               onPhotoPostAction?.invoke()
               dismiss()
           }

            tvReelPost.setOnClickListener {
                onReelPostAction?.invoke()
                dismiss()
            }
        }
    }
}