package com.cl.modules_contact.pop

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import com.cl.common_base.web.WebActivity
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactTdsPopBinding
import com.lxj.xpopup.core.CenterPopupView

/**
 * TDS 弹窗
 */
class TdsPop(
    context: Context,
    private val txt: String? = null,
    private val onConfirmAction: ((txt: String) -> Unit)? = null,
) : CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.contact_tds_pop
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<ContactTdsPopBinding>(popupImplView)?.apply {
            tvBuy.setOnClickListener {
                val intent = Intent(context, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_WEB_URL, "https://www.heyabby.com/products/4-in-1-digital-ph-meter?utm_source=app&utm_medium=heyabby")
                intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Abby")
                context.startActivity(intent)
            }
            tvCommentTxt.setText(txt)

            tvConfirm.setOnClickListener {
                onConfirmAction?.invoke(tvCommentTxt.text.toString())
                dismiss()
            }
        }
    }
}