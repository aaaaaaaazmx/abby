package com.cl.modules_my.widget

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyPopAutomationEditBinding
import com.lxj.xpopup.core.BubbleAttachPopupView

class AutomationEditPop(
    context: Context,
    private val onEditClick: () -> Unit,
    private val onDeleteClick: () -> Unit
) : BubbleAttachPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.my_pop_automation_edit
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<MyPopAutomationEditBinding>(popupImplView)?.apply {
            tvEdit.setOnClickListener {
                onEditClick.invoke()
                dismiss()
            }
            tvDelete.setOnClickListener {
                onDeleteClick.invoke()
                dismiss()
            }
        }
    }
}