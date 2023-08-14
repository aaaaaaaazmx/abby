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

        val url =
            "https://heyabbytest.s3.us-west-1.amazonaws.com/user/trend/a94e2d2227de4189b760a6f8ecbbee84.jpeg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20230814T082719Z&X-Amz-SignedHeaders=host&X-Amz-Expires=518400&X-Amz-Credential=AKIA5UEWHT7XNCDKTYWY%2F20230814%2Fus-west-1%2Fs3%2Faws4_request&X-Amz-Signature=257f3d9ebb3911fa0f925b3fc1998285095b92f9f97650689f23e693cd4a7b6c"
        val searchString = "user/trend/"
        val startIndex = url.indexOf(searchString)

        if (startIndex != -1) {
            val endIndex = url.indexOf(".jpeg", startIndex) + 5 // 5 is the length of ".jpeg"
            val result = url.substring(startIndex, endIndex)
            println("123123123123: $result") // Output: user/trend/a94e2d2227de4189b760a6f8ecbbee84.jpeg
        } else {
            println("1231231231: Search string not found in URL.")
        }

    }

    private fun convertTime(createTime: String? = null): String {
        return createTime?.let {
            DateHelper.getTimestamp(it, "yyyy-MM-dd HH:mm:ss").let {
                DateHelper.convert((it)).toString()
            }.toString()
        } ?: ""
    }

}