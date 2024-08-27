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
            appendLine(getString(com.cl.common_base.R.string.string_1621))
            appendLine(getString(com.cl.common_base.R.string.string_1622))
            append(getString(com.cl.common_base.R.string.string_1623))
            append(getString(com.cl.common_base.R.string.string_1624))
            color(ContextCompat.getColor(this@PairFailActivity, R.color.mainColor)) {
                appendClickable(getString(com.cl.common_base.R.string.string_1625)) {
                    startActivity(Intent(this@PairFailActivity, PairReconnectActivity::class.java))
                }
            }
            appendLine()
            appendLine(getString(com.cl.common_base.R.string.string_1626))
            appendLine(getString(com.cl.common_base.R.string.string_1627))
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