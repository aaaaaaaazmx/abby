package com.cl.modules_pairing_connection.ui

import android.content.Intent
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.help.PermissionHelp
import com.cl.modules_pairing_connection.R
import com.cl.modules_pairing_connection.databinding.PairReconnectBinding
import com.cl.modules_pairing_connection.widget.ReconnectTipsPop
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * 重新连接提示页面
 *
 * @author 李志军 2022-08-03 17:50
 */
@Route(path = RouterPath.PairConnect.KEY_PAIR_RECONNECTING)
@AndroidEntryPoint
class PairReconnectActivity : BaseActivity<PairReconnectBinding>() {
    /**
     * true -> guideOne
     * false -> bleTimeOut
     */
    private val extra by lazy {
        intent.getBooleanExtra(KEY_BLE_TIME_OUT_OR_GUIDE_CLICK, false)
    }

    private val tipsPop by lazy {
        // 弹窗
        XPopup.Builder(this@PairReconnectActivity)
            .popupPosition(PopupPosition.Top)
            .isDestroyOnDismiss(false) //对于只使用一次的弹窗，推荐设置这个
            .atView(binding.cbBox)
            .isClickThrough(true)
            .dismissOnTouchOutside(false)
            .hasShadowBg(false) // 去掉半透明背景
            .offsetY(XPopupUtils.dp2px(this@PairReconnectActivity, 6f))
            .asCustom(
                ReconnectTipsPop(this@PairReconnectActivity)
                    .setBubbleBgColor(
                        ContextCompat.getColor(
                            this@PairReconnectActivity,
                            R.color.mainColor
                        )
                    ) //气泡背景
                    .setArrowWidth(XPopupUtils.dp2px(this@PairReconnectActivity, 7f))
                    .setArrowHeight(
                        XPopupUtils.dp2px(
                            this@PairReconnectActivity,
                            6f
                        )
                    )
                    .setArrowRadius(XPopupUtils.dp2px(this@PairReconnectActivity, 2f))
            )
    }

    override fun initView() {
        tipsPop.show()
    }

    override fun observe() {
    }

    override fun initData() {
        binding.cbBox.setOnCheckedChangeListener { _, b ->
            binding.btnSuccess.isEnabled = b
            if (b) tipsPop.dismiss() else {
                if (!tipsPop.isShow) tipsPop.show()
            }
        }

        binding.btnSuccess.setOnClickListener {
            PermissionHelp().checkConnectForTuYaBle(
                this@PairReconnectActivity,
                object :
                    PermissionHelp.OnCheckResultListener {
                    override fun onResult(result: Boolean) {
                        if (!result) return
                        // 如果权限都已经同意了,
                        // 具体跳准到哪个页面
                        startActivity(
                            Intent(
                                this@PairReconnectActivity,
                                PairOnePageActivity::class.java
                            )
                        )
                    }
                })

        }
    }


    companion object {
        // 这个页面有2个地方会跳转过来
        // 需要判断是从哪个地方跳转过来到
        const val KEY_BLE_TIME_OUT_OR_GUIDE_CLICK = "KEY_BLE_TIME_OUT_OR_GUIDE_CLICK"
    }

}