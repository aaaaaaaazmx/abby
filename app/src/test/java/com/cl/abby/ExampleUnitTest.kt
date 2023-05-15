package com.cl.abby

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import com.cl.common_base.ext.DateHelper
import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        //
       /* "2023-05-05 17:35:59".let {
            // 2023-04-20 10:04:52
            DateHelper.getTimestamp(it, "yyyy-MM-dd HH:mm:ss").apply {
                println("1231232: ${DateHelper.formatTime(this, "ddMMM", Locale.US)}")
            }
        }*/
    }

    private fun convertTime(createTime: String? = null): String {
        return createTime?.let {
            DateHelper.getTimestamp(it, "yyyy-MM-dd HH:mm:ss").let {
                DateHelper.convert((it)).toString()
            }.toString()
        } ?: ""
    }

}