package com.cl.abby

import com.alibaba.fastjson.TypeReference
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.util.json.GSON
import com.cl.modules_contact.request.ContactEnvData
import com.google.gson.Gson
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
        "2023-04-24 09:45:31".let {
            // 2023-04-20 10:04:52
            // 2023-04-24 09:45:31
            println("1231232: ${convertTime("2023-04-24 09:45:31")}")
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