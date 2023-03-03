package com.cl.common_base.util.device

/**
 * DeviceConstansPIDs
 * @author 李志军 2022-08-09 14:12
 *
 * 设备DP点
 */
class TuYaDeviceConstants {

    companion object {
        // 排水
        //pump_water	可下发可上报（rw）	布尔型（Bool）
        const val KAY_PUMP_WATER = "113"

        // 108 喂食
        const val KAY_FEED_ABBY = "108"

        // 排水结束
        const val KAY_PUMP_WATER_FINISHED = "131"

        // 105，设备返回当前还有多少水，0，1，2，3。
        const val KEY_DEVICE_WATER_STATUS = "105"

        // SN的状态 ， NG表示需要修复，ok表示不需要修复
        const val KEY_DEVICE_REPAIR_SN = "135"

        // 重启复位状态
        const val KEY_DEVICE_REPAIR_REST_STATUS = "120"

        // 水温
        const val KEY_DEVICE_WATER_TEMPERATURE = "104"
        // 排气扇
        const val KEY_DEVICE_VENTILATION =  "103"
        // 环境温度
        const val KEY_DEVICE_TEMP_CURRENT =  "6"
        // 进气扇
        const val KEY_DEVICE_INPUT_AIR_FLOW =  "115"
        // 湿度
        const val KEY_DEVICE_HUMIDITY_CURRENT =  "7"
        // 灯光亮度
        const val KEY_DEVICE_BRIGHT_VALUE = "5"
        // 水位
        const val KEY_DEVICE_WATER_LEVEL = "105"

        // 气泵 布尔型（Bool）
        const val KEY_DEVICE_AIR_PUMP = "118"

        // 门的开光状态 (Bool)
        const val KEY_DEVICE_DOOR = "106"

        // 旋钮开门锁
        const val KEY_DEVICE_DOOR_LOOK = "119"

        // 童锁
        const val KEY_DEVICE_CHILD_LOCK = "142"

        // 夜间模式
        const val KEY_DEVICE_NIGHT_MODE = "141"

        // 风干期开门迎宾模式打开
        const val KEY_DEVICE_FAN_ENABLE= "132"

        // 植物高度 0-800 int
        const val KEY_DEVICE_PLANT_HEIGHT = "107"

        // 湿度  Humidity 0-200 int
        const val KEY_DEVICE_HUMIDITY = "7"

        // 进气扇  int 0-10
        const val KEY_DEVICE_INTAKE = "115"

        // 静音模式
        const val KEY_DEVICE_SILENT_MODE = "141"

        // 设备开关
        const val KEY_DEVICE_SWITCH = "1"

        // 关灯
        const val KEY_DEVICE_TURN_OFF_LIGHT = "117"

        // 开灯
        const val KEY_DEVICE_TURN_ON_THE_LIGHT = "116"
    }

    /**
     * 涂鸦设备返回的，和从服务器获取的不一样。
     * 设备推的是指令，
     * 服务器返回的是dp点
     */
    object DeviceInstructions {
        const val KAY_PUMP_WATER_INSTRUCTIONS = "pump_water"
        const val KAY_FEED_ABBY_INSTRUCTIONS = "Add_fertilizer"
        const val KAY_PUMP_WATER_FINISHED_INSTRUCTION = "pump_water_finished"
        const val KEY_DEVICE_WATER_STATUS_INSTRUCTIONS = "water_level"
        const val KEY_DEVICE_REPAIR_SN_INSTRUCTION = "app_rewrite_sn"
        const val KEY_DEVICE_REPAIR_REST_STATUS_INSTRUCTION = "device_status"
        const val KEY_DEVICE_BRIGHT_VALUE_INSTRUCTION = "bright_value" // 灯光强度
        const val KEY_DEVICE_HUMIDITY_CURRENT_INSTRUCTION = "humidity_current" // 湿度
        const val KEY_DEVICE_INPUT_AIR_FLOW_INSTRUCTION = "input_air_flow" // 进气扇
        const val KEY_DEVICE_TEMP_CURRENT_INSTRUCTION = "temp_current" // 环境温度
        const val KEY_DEVICE_VENTILATION_INSTRUCTION = "Ventilation" // 排气扇
        const val KEY_DEVICE_WATER_TEMPERATURE_INSTRUCTION = "water_temperature" // 水温
        const val KEY_DEVICE_CHILD_LOCK_INSTRUCT = "auto_lock" // 童锁
        const val KEY_DEVICE_DOOR_LOOK_INSTRUCT = "child_lock" // 打开门
        const val KEY_DEVICE_DOOR = "door" // 门是否是开的， false关闭。

        /**
         * brightValue	灯光强度		false
        integer
        deviceId	设备ID		false
        string
        humidityCurrent	湿度		false
        integer
        			false
        integer
        			false
        integer
        			false
        integer
        	水位		false
        string
        			false
        integer
         */

    }
}