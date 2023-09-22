package com.cl.common_base.help

import android.content.Context
import android.icu.text.NumberingSystem
import android.os.Build
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.cl.common_base.R
import com.cl.common_base.databinding.BaseNumberPickPopBinding
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.util.ViewUtils
import com.lxj.xpopup.core.CenterPopupView
import io.intercom.android.sdk.sheets.SheetWebViewAction
import kotlin.math.abs

/**
 * 数字选择
 */
class BaseNumberPickPop(
    context: Context,
    private val showNumber: Int? = 1,
    private val onConfirm: ((number: Int) -> Unit)? = null,
    private val dataNumber: Int = 1,
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_number_pick_pop
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<BaseNumberPickPopBinding>(popupImplView)?.apply {
            ViewUtils.setVisible(showNumber == 4, numberPicker2, numberPicker3, numberPicker4)
            numberPicker1.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            numberPicker2.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            numberPicker3.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            numberPicker4.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            if (showNumber == 1) {
                numberPicker1.minValue = 1
                numberPicker1.maxValue = 12
                numberPicker1.textSize = 60f
                numberPicker1.wrapSelectorWheel = false
                numberPicker1.textColor = ContextCompat.getColor(context, R.color.mainColor)
            }
            if (showNumber == 4) {
                numberPicker1.minValue = 0
                numberPicker1.maxValue = 9
                numberPicker1.textSize = 60f
                numberPicker1.wrapSelectorWheel = false
                numberPicker1.textColor = ContextCompat.getColor(context, R.color.mainColor)

                numberPicker2.minValue = 0
                numberPicker2.maxValue = 9
                numberPicker2.textSize = 60f
                numberPicker2.wrapSelectorWheel = false
                numberPicker2.textColor = ContextCompat.getColor(context, R.color.mainColor)

                numberPicker3.minValue = 0
                numberPicker3.maxValue = 9
                numberPicker3.textSize = 60f
                numberPicker3.wrapSelectorWheel = false
                numberPicker3.textColor = ContextCompat.getColor(context, R.color.mainColor)

                numberPicker4.minValue = 0
                numberPicker4.maxValue = 9
                numberPicker4.textSize = 60f
                numberPicker4.wrapSelectorWheel = false
                numberPicker4.textColor = ContextCompat.getColor(context, R.color.mainColor)
            }

            if (showNumber == 1) {
                numberPicker1.value = dataNumber
            }

            if (showNumber == 4) {
                runCatching {
                    val numberOfDigits = dataNumber.toString().map { it.toString().safeToInt() }
                    when(numberOfDigits.size) {
                        1 -> {
                            numberPicker4.value = numberOfDigits[0]
                        }
                        2 -> {
                            numberPicker3.value = numberOfDigits[0]
                            numberPicker4.value = numberOfDigits[1]
                        }
                        3 -> {
                            numberPicker2.value = numberOfDigits[0]
                            numberPicker3.value = numberOfDigits[1]
                            numberPicker4.value = numberOfDigits[2]
                        }
                        4 -> {
                            numberPicker1.value = numberOfDigits[0]
                            numberPicker2.value = numberOfDigits[1]
                            numberPicker3.value = numberOfDigits[2]
                            numberPicker4.value = numberOfDigits[3]
                        }
                    }
                }
            }

            tvCancel.setOnClickListener { dismiss() }
            tvConfirm.setOnClickListener {
                if (showNumber == 1) {
                    onConfirm?.invoke(numberPicker1.value)
                }

                if (showNumber == 4) {
                    onConfirm?.invoke("${numberPicker1.value}${numberPicker2.value}${numberPicker3.value}${numberPicker4.value}".safeToInt())
                }
                dismiss()
            }
        }
    }
}