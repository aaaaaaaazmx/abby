package com.cl.modules_contact.pop

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import com.cl.common_base.web.WebActivity
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactPhPopBinding
import com.cl.modules_contact.databinding.ContactTdsPopBinding
import com.lxj.xpopup.core.CenterPopupView
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams

/**
 * PH 弹窗
 */
class ContactPhPop(
    context: Context,
    private val txt: Float? = null,
    private val onConfirmAction: ((txt: String?) -> Unit)? = null,
) : CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.contact_ph_pop
    }

    override fun onCreate() {
        super.onCreate()

        DataBindingUtil.bind<ContactPhPopBinding>(popupImplView)?.apply {
            tvBuy.setOnClickListener {
                val intent = Intent(context, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_WEB_URL, "https://www.heyabby.com/products/4-in-1-digital-ph-meter?utm_source=app&utm_medium=heyabby")
                intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "Abby")
                context.startActivity(intent)
            }
            txt?.let { phSeekbar.setProgress(it) } ?: phSeekbar.setProgress(7.0f)
            tvProgress.text = phSeekbar.progressFloat.toString()

            tvConfirm.setOnClickListener {
                onConfirmAction?.invoke(phSeekbar.progressFloat.toString())
                dismiss()
            }
            phSeekbar.onSeekChangeListener = object : OnSeekChangeListener{
                override fun onSeeking(p0: SeekParams?) {
                     tvProgress.text = p0?.progressFloat.toString()
                }

                override fun onStartTrackingTouch(p0: IndicatorSeekBar?) {
                }

                override fun onStopTrackingTouch(bar: IndicatorSeekBar?) {

                }
            }

            tvCancel.setOnClickListener {
                phSeekbar.setProgress(7.0f)
                tvProgress.text = phSeekbar.progressFloat.toString()
                onConfirmAction?.invoke(null)
                dismiss()
            }
        }
    }
}