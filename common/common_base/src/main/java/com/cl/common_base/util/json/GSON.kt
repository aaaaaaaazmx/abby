package com.cl.common_base.util.json

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import java.lang.reflect.Type
import android.os.Handler
import android.os.Looper
import com.cl.common_base.ext.logI
import com.cl.common_base.util.CoroutineFlowUtils
import kotlinx.coroutines.flow.flow
import kotlin.concurrent.thread

object GSON {

    private val gson = Gson()
    private val jsonParser = JsonParser()
    private const val TAG = "GSON_UTIL"

    /**
     * json to javabean
     *
     * @param json
     */
    fun <T> parseObject(json: String?, clazz: Class<T>?): T? {
        if (json.isNullOrEmpty()) {
            Log.e(TAG, "JSON string is null or empty")
            return null
        }
        return try {
            gson.fromJson(json, clazz)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Failed to parse JSON: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while parsing JSON: ${e.message}")
            null
        }
    }

    fun <T> parseObjectType(json: String?, clazz: Type?): T? {
        if (json.isNullOrEmpty()) {
            Log.e(TAG, "JSON string is null or empty")
            return null
        }
        return try {
            gson.fromJson(json, clazz)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Failed to parse JSON: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while parsing JSON: ${e.message}")
            null
        }
    }

    /**
     * json字符串转List集合
     */
    fun <T> parseObjectList(json: String?, cls: Class<T>?): List<T> {
        if (json.isNullOrEmpty()) {
            Log.e(TAG, "JSON string is null or empty")
            return emptyList()
        }
        return try {
            val jsonArray = jsonParser.parse(json).asJsonArray
            jsonArray.mapNotNull { jsonElement ->
                try {
                    gson.fromJson(jsonElement, cls)
                } catch (e: JsonSyntaxException) {
                    Log.e(TAG, "Failed to parse JSON element: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error while parsing JSON element: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while parsing JSON array: ${e.message}")
            emptyList()
        }
    }

    /**
     * 转成json字符串
     *
     * @param t
     * @return
     */
    fun toJson(t: Any?): String {
        return if (t == null) {
            Log.e(TAG, "Object to convert to JSON is null")
            ""
        } else {
            try {
                gson.toJson(t)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error while converting object to JSON: ${e.message}")
                ""
            }
        }
    }

    /**
     * 使用后台线程将对象转换为JSON字符串
     *
     * @param t
     * @param callback
     */
    fun toJsonInBackground(t: Any?, callback: (String) -> Unit) {
        CoroutineFlowUtils.executeInBackground(
            task = {
                toJson(t)
            },
            onSuccess = {
                callback(it)
            }
        )
    }

    /**
     * 使用后台线程解析json
     *
     * @param json
     * @param clazz
     * @param callback
     */
    fun <T> parseObjectInBackground(json: String?, clazz: Class<T>?, callback: (T?) -> Unit) {
        CoroutineFlowUtils.executeInBackground(
            task = { parseObject(json, clazz) },
            onSuccess = {
                callback(it)
            }
        )
    }

    /**
     * 使用后台线程解析json字符串转List集合
     *
     * @param json
     * @param cls
     * @param callback
     */
    fun <T> parseObjectListInBackground(json: String?, cls: Class<T>?, callback: (List<T>) -> Unit) {
        CoroutineFlowUtils.executeInBackground(
            task = {
                parseObjectList(json, cls)
            },
            onSuccess = {
                callback(it)
            }
        )
    }
}
