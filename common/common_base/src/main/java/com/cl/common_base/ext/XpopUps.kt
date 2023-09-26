package com.cl.common_base.ext

import android.content.Context
import com.lxj.xpopup.XPopup

/**
 * This is a short description.
 *
 * @author 李志军 2023-09-17 21:03
 */
fun xpopup(context: Context, block: XPopup.Builder.() -> Unit) {
    XPopup.Builder(context).block()
}