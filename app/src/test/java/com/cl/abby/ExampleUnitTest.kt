package com.cl.abby

import org.junit.Test
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        /*assertEquals(4, 2 + 2)*/
        val time = "11:00 PM-12:00 AM"
        val pattern: Pattern = Pattern.compile("(PM|AM)")
        val matcher: Matcher = pattern.matcher(time)

        while (matcher.find()) {
            println("1221123123 " + matcher.group())
        }
    }

}