package com.cl.common_base.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.cl.common_base.bean.AccessoryListBean
import com.cl.common_base.bean.PresetData
import com.cl.common_base.constants.Constants
import com.cl.common_base.util.json.GSON
import com.google.gson.reflect.TypeToken
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

    @JvmStatic
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

    // 添加然后删除别名
    fun addObject(devId: String, newObj: PresetData) {
        val objects = getObjects()?.toMutableList()
        //如果他们相等 那么就是修改
        val currentDevice = objects?.indexOfFirst { it.id == devId }
        if (-1 == currentDevice) {
            // 新添加的strainName
            objects.add(newObj) // 添加新对象到末尾
        } else {
            currentDevice?.let { removeObject(it, objects) }
            objects?.add(newObj)
        }
        saveObjects(objects)
    }

    // 删除配件、删除别名使用。
    fun removeObject(obj: Int, objects: MutableList<PresetData>? = null) {
        val objs = objects ?: getObjects()?.toMutableList()
        if (objs?.isEmpty() == true) return
        objs?.removeAt(obj) // 删除指定对象
    }

    // 删除设备
    fun removeObjectForDevice(obj: Int) {
        val objs = getObjects()?.toMutableList()
        if (objs?.isEmpty() == true) return
        objs?.removeAt(obj) // 删除指定对象
        saveObjects(objs)
    }

    // 删除配件
    fun removeObjectAccessory(obj: PresetData, b: AccessoryListBean) {
        obj.accessoryList?.indexOfFirst { it.accessoryType == b.accessoryType }?.let {
            if (it != -1) {
                obj.accessoryList?.removeAt(it)
            }
        }
        // 更新当前
        addObject(obj.id.toString(), obj)
    }

    // 修改配件
    fun modifyObjectAccessory(
        devId: String,
        obj: PresetData,
        b: AccessoryListBean,
        layoutPosition: Int
    ) {
        synchronized(devId) {
            obj.accessoryList?.get(layoutPosition)?.let {
                obj.accessoryList?.set(layoutPosition, b)
            }
            // 更新当前
            addObject(obj.id.toString(), obj)
        }
    }

    // xigai

    private fun saveObjects(objects: List<PresetData>?) {
      GSON.toJsonInBackground(objects) {
            mmkv.putString(Constants.Global.KEY_GLOBAL_PRO_MODEL, it)
        }
    }

    fun getObjects(): List<PresetData>? {
        val json = mmkv.getString(Constants.Global.KEY_GLOBAL_PRO_MODEL, null) ?: return emptyList()
        val type = object : TypeToken<List<PresetData>>() {}.type
        return GSON.parseObjectType(json, type)
    }
}