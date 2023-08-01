package com.cl.modules_contact.pop

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.hardware.camera2.params.DeviceStateSensorOrientationMap
import androidx.databinding.DataBindingUtil
import com.cl.common_base.util.ViewUtils
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactDeletePopBinding
import com.lxj.xpopup.core.BubbleAttachPopupView
import com.lxj.xpopup.core.CenterPopupView


/**
 * 删除和Copy弹窗
 */
class ContactDeletePop(
    context: Context,
    private val isShowDelete: Boolean? = true,
    private val onCopyAction: (() -> Unit)? = null,
    private val onDeleteAction: (() -> Unit)? = null,
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.contact_delete_pop
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<ContactDeletePopBinding>(popupImplView)?.apply {
            ViewUtils.setVisible(isShowDelete == true, tvDelete, vv)

            tvDelete.setOnClickListener {
                onDeleteAction?.invoke()
                dismiss()
            }

            tvCopy.setOnClickListener {
                onCopyAction?.invoke()
                dismiss()
            }
        }
    }
}