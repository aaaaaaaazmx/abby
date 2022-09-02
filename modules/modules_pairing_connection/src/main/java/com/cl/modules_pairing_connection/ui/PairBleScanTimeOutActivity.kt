package com.cl.modules_pairing_connection.ui

import android.content.Intent
import android.text.method.LinkMovementMethod
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.help.PermissionHelp
import com.cl.common_base.util.span.appendClickable
import com.cl.modules_pairing_connection.R
import com.cl.modules_pairing_connection.databinding.PairBleScanTimeOutBinding

/**
 * 连接超时页面
 *
 * 蓝牙扫描5分钟超时，就会跳准到这个界面来
 *
 * @author 李志军 2022-08-03 18:25
 */
class PairBleScanTimeOutActivity : BaseActivity<PairBleScanTimeOutBinding>() {
    override fun initView() {
        binding.title.setLeftText("Cancel")
            .setOnClickListener { finish() }

        //span
        /**
         * 1. Make sure that the phone's Bluetooturned
        2.Place the phone close to the device and
        pair it again.
        3.Reconnect the device.
         */
        binding.tvFour.text = buildSpannedString {
            appendLine("1. Make sure your phone's Bluetooth is turned on")
            appendLine("2. Place the phone close to the device ")
            append("3. Try to ")
            color(ContextCompat.getColor(this@PairBleScanTimeOutActivity, R.color.mainColor)) {
                appendClickable("Reconnect") {
                    // 检查是否有定位权限
                    PermissionHelp().checkConnectForTuYaBle(
                        this@PairBleScanTimeOutActivity,
                        object :
                            PermissionHelp.OnCheckResultListener {
                            override fun onResult(result: Boolean) {
                                if (!result) return
                                startActivity(
                                    Intent(
                                        this@PairBleScanTimeOutActivity,
                                        PairReconnectActivity::class.java
                                    )
                                )
                            }
                        })
                }
            }
            append(" (hyperlink)")
        }

        binding.tvFour.movementMethod = LinkMovementMethod.getInstance() // 设置了才能点击
        binding.tvFour.highlightColor = ResourcesCompat.getColor( // 设置之后点击才不会出现背景颜色
            resources,
            com.cl.common_base.R.color.transparent,
            theme
        )
    }

    override fun observe() {
    }

    override fun initData() {

        binding.btnSuccess.setOnClickListener {
            startActivity(Intent(this@PairBleScanTimeOutActivity, PairOnePageActivity::class.java))
        }
    }
}