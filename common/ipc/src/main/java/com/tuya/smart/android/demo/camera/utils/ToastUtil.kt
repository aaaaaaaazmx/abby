package com.tuya.smart.android.demo.camera.utils

import android.content.Context
import android.text.TextUtils
import android.widget.Toast

/**

 * TODO feature

 *

 * @author houqing <a href="mailto:developer@tuya.com"/>

 * @since 2021/7/26 3:38 PM

 */
class ToastUtil {
    companion object{
        fun shortToast(context: Context?, tips: String?) {
            if (TextUtils.isEmpty(tips)) return
            Toast.makeText(context, tips, Toast.LENGTH_SHORT).show()
        }
    }
}