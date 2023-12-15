package com.cl.common_base.util.device

import com.thingclips.smart.sdk.api.IThingDevice

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
    fun getCurrentDevice(): IThingDevice?

    /**
     * 排水
     */
    fun pumpWater(startOrStop: Boolean)

    /**
     * 旋钮开门锁
     */
    fun doorLock(openOrClose: Boolean)

    /**
     * 童锁开关
     */
    fun childLock(startOrStop: Boolean, devId: String? = null)

    /**
     * 夜间模式
     */
    fun nightMode(startOrStop: String, devId: String? = null)

    /**
     * 开灯时间
     */
    fun lightTime(time: Int)

    /**
     * 关灯时间
     */
    fun closeLightTime(time: Int)

    /**
     * 进气风扇操作
     */
    fun fanIntake(gear: Int)

    /**
     * 排气风扇操作
     */
    fun fanExhaust(gear: Int)

    /**
     * 光的强度
     */
    fun lightIntensity(gear: Int)

    /**
     * 气泵
     */
    fun airPump(startOrStop: Boolean)

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


    /**
     * 一次性发送多个dp点
     * https://cz84t38aha.feishu.cn/docx/UnYBd35TJoXMcrxrD5ncNgPqnmc
     */
    fun sendDps(dpsJson: String): DeviceControlImpl

    companion object {
        fun get() = DeviceControlImpl()
    }
}