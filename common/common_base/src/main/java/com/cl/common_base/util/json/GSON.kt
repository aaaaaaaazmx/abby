package com.cl.common_base.util.json

import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.lang.reflect.Type

object GSON {

    /**
     * json to javabean
     *
     * @param json
     */
    @JvmStatic
    fun <T> parseObject(json: String?, clazz: Class<T>?): T? {
        return if (TextUtils.isEmpty(json)) {
            null
        } else Gson().fromJson(json, clazz)
    }


    /**
     * json to javabean
     *
     * @param json
     */
    @JvmStatic
    fun <T> parseObject(json: String?, type: Type?): T {
        return Gson().fromJson(json, type)
    }

    /**
     * json字符串转List集合
     */
    fun <T> parseObjectList(json: String?, cls: Class<T>?): List<T> {
        if (!TextUtils.isEmpty(json)) {
            val list: MutableList<T> = ArrayList()
            try {
                val gson = Gson()
                val jsonArray = JsonParser().parse(json).asJsonArray
                for (jsonElement in jsonArray) {
                    list.add(gson.fromJson(jsonElement, cls))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }
        return ArrayList()
    }


    /**
     * 转成json字符串
     *
     * @param t
     * @return
     */
    fun toJson(t: Any?): String? {
        return if (t != null) {
            Gson().toJson(t)
        } else ""
    }
}