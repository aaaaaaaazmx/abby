package com.cl.common_base.help

import android.content.Context
import androidx.core.content.ContextCompat
import com.cl.common_base.R
import com.cl.common_base.ext.dp2px
import com.cl.common_base.pop.*
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
    fun showGuidePop(): Boolean {
        var isUnlock: Boolean = false
        pop.asCustom(
            // onePop
            HomePlantFourPop(context = context, onNextAction = {
                pop.asCustom(
                    HomePlantFivePop(context, onNextAction = {
                        pop.asCustom(
                            BaseBottomPop(
                                context, ContextCompat.getDrawable(context, R.drawable.base_dot_red),
                                context.getString(R.string.base_fertilizer),
                                context.getString(R.string.my_next)
                            ) {
                                pop.asCustom(HomePlantEightPop(context, onNextAction = {
                                    pop.asCustom(
                                        BaseBottomPop(
                                            context,
                                            ContextCompat.getDrawable(context, R.mipmap.base_seed_to_veg_bg),
                                            context.getString(R.string.base_seed_to_veg),
                                            buttonText = context.getString(R.string.home_done),
                                            onNextAction = {
                                                // 这个用来返回数据
                                                isUnlock = true
                                            })
                                    ).show()
                                })).show()
                            }
                        ).show()
                    })
                ).show()
            })
        ).show()
        return isUnlock
    }

}