package com.cl.common_base.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.tencent.mmkv.MMKV

object Prefs {

    private val mmkv = MMKV.defaultMMKV()!!

    fun putInt(key: String, value: Int) {
        mmkv.encode(key, value)
    }

    fun getInt(key: String, defaultValue: Int = -1): Int {
        return mmkv.decodeInt(key, defaultValue)
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    fun putIntAsync(key: String, value: Int) {
        mmkv.putInt(key, value).apply()
    }

    fun putString(key: String, value: String) {
        mmkv.encode(key, value)
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    fun putStringAsync(key: String, value: String) {
        mmkv.putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return mmkv.decodeString(key, defaultValue) ?: defaultValue
    }

    fun putDouble(key: String, value: Double) {
        mmkv.encode(key, value)
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    fun putDoubleAsync(key: String, value: Float) {
        mmkv.putFloat(key, value).apply()
    }

    fun getDouble(key: String, defaultValue: Double = 0.00): Double {
        return mmkv.decodeDouble(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        mmkv.encode(key, value)
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    fun putLongAsync(key: String, value: Long) {
        mmkv.putLong(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return mmkv.decodeLong(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        mmkv.encode(key, value)
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    fun putBooleanAsync(key: String, value: Boolean) {
        mmkv.putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return mmkv.decodeBool(key, defaultValue)
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    fun clear() {
        mmkv.clearAll()
        mmkv.clear().apply()
    }

    // 删除单个key
    fun removeKey(key: String) {
        mmkv.removeValueForKey(key)
    }

    /*****************************************************************************/

    /**
     * dataStore 存入数据默认就是异步，没有同步方法
     * 取数据异步通过Flow实现
     */
//    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
//
//    fun put(key: String, value: Any) {
//
//    }

}