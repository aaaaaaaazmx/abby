package com.cl.common_base.help

import android.content.Context
import androidx.core.content.ContextCompat
import com.cl.common_base.R
import com.cl.common_base.ext.dp2px
import com.cl.common_base.pop.*
import com.cl.common_base.util.device.DeviceControl
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.XPopup

/**
 * Seed解锁Veg周期的引导弹窗帮助类
 */
class SeedGuideHelp(val context: Context) {

    private val pop by lazy {
        XPopup.Builder(context)
            .dismissOnTouchOutside(false)
            .maxHeight(dp2px(700f))
    }

    /**
     * 返回是否可以解锁的判断
     * true 表示可以解锁
     */
    fun showGuidePop(onNextAction: (() -> Unit)? = null) {
        pop.asCustom(
            // onePop
            HomePlantFourPop(context = context, onNextAction = {
                pop.asCustom(
                    HomePlantFivePop(context, onNextAction = {
                        pop.asCustom(
                            BaseBottomPop(
                                context, ContextCompat.getDrawable(context, R.mipmap.base_six_bg),
                                context.getString(R.string.string_1956).trimIndent(),
                                context.getString(R.string.my_next),
                                onNextAction = {
                                    pop.asCustom(HomePlantEightPop(context, onNextAction = {
                                        pop.asCustom(
                                            BaseBottomPop(
                                                context,
                                                ContextCompat.getDrawable(context, R.mipmap.base_seed_to_veg_bg),
                                                context.getString(R.string.base_seed_to_veg),
                                                buttonText = context.getString(R.string.home_done),
                                                onNextAction = {
                                                    // 这个用来返回数据
                                                    onNextAction?.invoke()
                                                })
                                        ).show()
                                    })).show()
                                    // 加肥指令
                                    /*DeviceControl.get()
                                        .success {
                                            pop.asCustom(HomePlantEightPop(context, onNextAction = {
                                                pop.asCustom(
                                                    BaseBottomPop(
                                                        context,
                                                        ContextCompat.getDrawable(context, R.mipmap.base_seed_to_veg_bg),
                                                        context.getString(R.string.base_seed_to_veg),
                                                        buttonText = context.getString(R.string.home_done),
                                                        onNextAction = {
                                                            // 这个用来返回数据
                                                            onNextAction?.invoke()
                                                        })
                                                ).show()
                                            })).show()
                                        }
                                        .error { code, error ->
                                            ToastUtil.shortShow(
                                                """
                                                    feedAbby:
                                                    code-> $code
                                                    errorMsg-> $error
                                                """.trimIndent()
                                            )
                                        }
                                        .feedAbby(true)*/
                                }
                            )
                        ).show()
                    })
                ).show()
            })
        ).show()
    }

}