package com.cl.common_base.util.device

import com.tuya.smart.sdk.api.ITuyaDevice

/**
 * 设备接口
 *
 * @author 李志军 2022-08-09 13:53
 */
interface DeviceControl {
    /**
     * 控制设备成功
     */
    fun success(action: (DeviceControlImpl.() -> Unit)): DeviceControlImpl

    /**
     * 控制设备失败
     */
    fun error(action: (DeviceControlImpl.(code: String?, error: String?) -> Unit)): DeviceControlImpl

    /**
     * 获取当前设备
     */
    fun getCurrentDevice(): ITuyaDevice?

    /**
     * 排水
     */
    fun pumpWater(startOrStop: Boolean)

    /**
     *  134
         Time to feed abby
        timetofeedabby	可下发可上报（rw）	布尔型（Bool）
     */
    fun feedAbby(startOrStop: Boolean): DeviceControlImpl

    /**
     * 修复SN上报
     */
    fun repairSN(isRepair: String): DeviceControlImpl

    companion object {
        fun get() = DeviceControlImpl()
    }
}