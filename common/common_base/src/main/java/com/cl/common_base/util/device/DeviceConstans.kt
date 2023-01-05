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
        // 灯光强度
        const val KEY_DEVICE_BRIGHT_VALUE = "5"
        // 水位
        const val KEY_DEVICE_WATER_LEVEL = "105"
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