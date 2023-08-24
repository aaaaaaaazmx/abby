package com.cl.abby

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

        val url = "https://heyabbytest.s3.us-west-1.amazonaws.com/user/trend/1f705984a8f64.jpg?X-Amz-Algorit"
        val prefix = "https://heyabbytest.s3.us-west-1.amazonaws.com/"
        val startIndex = prefix.length
        val endIndex = url.indexOf('?', startIndex) // 找到问号的位置，如果没有问号可以使用url.length
        val result = url.substring(startIndex, if (endIndex == -1) url.length else endIndex)
        println("12312312312: $result")
    }

    private fun convertTime(createTime: String? = null): String {
        return createTime?.let {
            DateHelper.getTimestamp(it, "yyyy-MM-dd HH:mm:ss").let {
                DateHelper.convert((it)).toString()
            }.toString()
        } ?: ""
    }

}