package com.cl.modules_pairing_connection.ui

import android.content.Intent
import android.text.method.LinkMovementMethod
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.util.span.appendClickable
import com.cl.modules_pairing_connection.R
import com.cl.modules_pairing_connection.databinding.PairFailBinding

/**
 * 配网失败展示界面
 *
 * @author 李志军 2022-08-04 14:30
 */
class PairFailActivity : BaseActivity<PairFailBinding>() {
    override fun initView() {
        binding.tvSpan.text = buildSpannedString {
            appendLine("1. Confirm that the Wi-Fi address and password you entered are correct.")
            appendLine("2. Check your Wi-Fi network. Currently, abby can only support 2.4G Wi-Fi.")
            append("3.")
            append("Reboot abby, then try ")
            color(ContextCompat.getColor(this@PairFailActivity, R.color.mainColor)) {
                appendClickable("reconnecting") {
                    startActivity(Intent(this@PairFailActivity, PairReconnectActivity::class.java))
                }
            }
            appendLine()
            appendLine("4. Try to unplug abby, then plug it back in")
            appendLine("5. Try restarting the iOS/Android device that needs to be paired with abby. Then try pairing again. ")
        }
        binding.tvSpan.movementMethod = LinkMovementMethod.getInstance() // 设置了才能点击
        binding.tvSpan.highlightColor = ResourcesCompat.getColor( // 设置之后点击才不会出现背景颜色
            resources,
            com.cl.common_base.R.color.transparent,
            theme
        )
    }

    override fun observe() {
    }

    override fun initData() {
        binding.btnSuccess.setOnClickListener { finish() }
    }
}