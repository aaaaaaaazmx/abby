package com.cl.modules_login.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.cl.common_base.adapter.HomeKnowMoreAdapter.ButtonState
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.util.ViewUtils
import com.cl.modules_login.R
import com.cl.modules_login.databinding.LoginItemUsbPortBinding
import com.lxj.xpopup.core.CenterPopupView

class OffLineUsbPop(context: Context, private val usbNumber: MutableList<Int?>? = null, private val usbChooseAction: ((Int) -> Unit)? = null) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.login_item_usb_port
    }

    private var usbPort: Int = -1
    private lateinit var usbMapping: Map<Int, Pair<RelativeLayout, FrameLayout>>

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate() {
        super.onCreate()


        DataBindingUtil.bind<LoginItemUsbPortBinding>(popupImplView)?.apply {
            lifecycleOwner = this@OffLineUsbPop
            executePendingBindings()

            // 全集与剩余数字计算（便于调试或后续扩展）
            val allNumbers = setOf(1, 2, 3)
            val remainingNumbers = allNumbers - (usbNumber?.toSet() ?: emptySet()).toSet()
            logI("剩余数字: $remainingNumbers")

            // 建立 USB 端口与对应视图的映射关系
            // 请根据实际布局将 rlOne 与 flOne 替换为对应的 RelativeLayout 与 FrameLayout
            usbMapping = mapOf(
                1 to Pair(rlOne, usbOneFrame),
                2 to Pair(rlTwo, usbTwoFrame),
                3 to Pair(rlThree, usbThFrame)
            )
            // 初始化时刷新一次按钮状态
            refreshUsbButtons()

            // 为每个 USB 按钮绑定点击事件，点击时更新 usbPort 并刷新 UI
            usbMapping.forEach { (num, pair) ->
                val (relativeLayout, _) = pair
                relativeLayout.setSafeOnClickListener {
                    usbPort = num
                    refreshUsbButtons()
                }
            }

            // 成功按钮点击回调
            btnSuccess.setSafeOnClickListener {
                if (usbPort == -1) {
                    dismiss()
                    return@setSafeOnClickListener
                }
                usbChooseAction?.invoke(usbPort)
                dismiss()
            }
            // 关闭按钮点击
            ivClose.setSafeOnClickListener { dismiss() }
        }
    }

    // 定义一个方法，用于根据当前状态刷新所有按钮的 UI
    private fun refreshUsbButtons() {
        usbMapping.forEach { (num, pair) ->
            val (relativeLayout, frameLayout) = pair
            // 构造状态：
            // - disable 根据实际业务逻辑判断，这里默认 false
            // - bind：表示该端口是否已存在（usbNumber 包含该数字）
            // - select：表示是否当前选中
            val state = ButtonState(
                disable = false,
                bind = usbNumber?.contains(num) == true,
                select = (usbPort == num),
                usbId = ""
            )
            applyBackground(state, relativeLayout, frameLayout)
        }
    }

    private fun applyBackground(data: ButtonState, relativeLayout: RelativeLayout, frameLayout: FrameLayout) {
        // 根据 disable 状态隐藏或显示控件（此处方法根据实际需求调用）
        ViewUtils.setInvisible(relativeLayout, data.disable)

        // 若已绑定，则设置成未选中状态（背景和透明度调整）
        if (data.bind) {
            relativeLayout.setBackgroundResource(com.cl.common_base.R.drawable.background_usb_un_bind_bg_r5)
            frameLayout.setBackgroundResource(com.cl.common_base.R.drawable.background_button_usb_uncheck_r180)
            frameLayout.alpha = 0.5f
        } else {
            // 未绑定时，背景根据是否选中进行设置
            relativeLayout.setBackgroundResource(com.cl.common_base.R.drawable.background_usb_bg_r5)
            if (data.select) {
                frameLayout.setBackgroundResource(com.cl.common_base.R.drawable.background_button_usb_check_r180)
                frameLayout.alpha = 1.0f
            } else {
                frameLayout.setBackgroundResource(com.cl.common_base.R.drawable.background_button_usb_uncheck_r180)
                frameLayout.alpha = 1.0f
            }
        }
    }
}