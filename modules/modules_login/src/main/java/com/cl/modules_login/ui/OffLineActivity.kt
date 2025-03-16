package com.cl.modules_login.ui

import android.content.Intent
import android.text.method.LinkMovementMethod
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.alibaba.android.arouter.launcher.ARouter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.Constants
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.span.appendClickable
import com.cl.modules_login.R
import com.cl.modules_login.databinding.LoginOfflineActivityBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OffLineActivity: BaseActivity<LoginOfflineActivityBinding>() {
    private val devId = {
        Prefs.getString(Constants.Tuya.KEY_DEVICE_ID)
    }

    override fun initView() {
        // 设置当前span文字
        binding.tvSpan.movementMethod = LinkMovementMethod.getInstance() // 设置了才能点击
        binding.tvSpan.highlightColor = ResourcesCompat.getColor( // 设置之后点击才不会出现背景颜色
            resources, com.cl.common_base.R.color.transparent, this@OffLineActivity.theme
        )
        binding.tvSpan.text = buildSpannedString {
            appendLine(getString(com.cl.common_base.R.string.string_1348))
            appendLine(getString(com.cl.common_base.R.string.string_1349))
            appendLine(getString(com.cl.common_base.R.string.string_1350))
            append(getString(com.cl.common_base.R.string.string_1351))
            this@OffLineActivity.let { context ->
                ContextCompat.getColor(
                    context, com.cl.common_base.R.color.mainColor
                )
            }.let { color ->
                color(
                    color
                ) {
                    appendClickable(getString(com.cl.common_base.R.string.string_1352)) {
                        // 跳转到ReconnectActivity
                        ARouter.getInstance().build(RouterPath.PairConnect.KEY_PAIR_RECONNECTING)
                            .navigation()
                    }
                }
            }
        }

        binding.title.setRightButtonImg(com.cl.common_base.R.mipmap.base_setting_select_bg).setRightClickListener {
            // 跳转到设备列表
            startActivity(Intent(this, OffLineSettingActivity::class.java))
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }

}