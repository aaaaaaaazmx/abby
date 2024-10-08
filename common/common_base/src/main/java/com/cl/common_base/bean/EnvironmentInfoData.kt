package com.cl.common_base.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.cl.common_base.BaseBean

/**
 * 获取植物的环境信息
 *
 * @author 李志军 2022-08-11 18:04
 */
class EnvironmentInfoData(
    val healthStatus: String? = null,
    val environmentLowCount: Int? = null,
    var proMode: String? = null, // 专业模式
    var fanAuto: Int? = null, // 风扇是否自动
    var environments: MutableList<Environment>? = null,
) : BaseBean() {
    data class Environment(
        val isBurnProof: Int? = null, // 是否开启防烧模式
        var fanIntake: Int? = null,
        var fanExhaust: Int? = null,
        val detectionValue: String? = null,
        val healthStatus: String? = null,
        val value: String? = null,
        val currentSwitch: String? = null,
        val explain: String? = null,
        val articleId: String? = null,
        val roomData: String? = null,
        val environmentType: String? = null, // 根据这个来判断是否是其他itemType
        val type: String? = null, // 根据这个来判断是否是其他itemType
        val alert: Int? = null,
        val runningInfo: String? = null,
        var runningModelShow: String? = null,
        var runningModel: String? = null,
        var isProMode: Boolean? = false, // 是否是ProMode
        var waterPump: Boolean? = false, // 是否有水泵
        val articleDetails: String? = null,
        var step: String? = null,
        var templateId: String? = null,
        var dripKit: Boolean? = null,
        var airPump: Boolean? = null,
        var stepShow: String? = null,
        var additionalData:MutableList<Environment>? = null,
        var textDesc: String? = null, // 文字描述
        var automation: Int? = null, // 是否是自动模式
    ) : BaseBean(), MultiItemEntity {
        override val itemType: Int
            get() = when (environmentType) {
                KEY_TYPE_FAN_TYPE -> KEY_TYPE_FAN
                KEY_TYPE_LIGHT_TYPE -> KEY_TYPE_LIGHT
                KEY_SPACE_TYPE_TEXT -> KEY_TYPE_TEXT
                KEY_SPACE_TYPE_GRID -> KEY_TYPE_GRID
                KEY_TYPE_DRIP_TYPE -> KEY_DRIP
                else -> KEY_TYPE_NORMAL
            }
    }

    companion object {
        // 这是Activity页面的标题
        const val KEY_TYPE_NORMAL = 0

        // 这是内容的标题
        const val KEY_TYPE_FAN = 1

        // 这是开关灯状态的Item
        const val KEY_TYPE_LIGHT = 2

        // 滴灌的Item
        const val KEY_DRIP = 5

        // 文字描述类型，自己新增
        const val KEY_SPACE_TYPE_TEXT = "text"
        // type grid
        const val KEY_SPACE_TYPE_GRID = "grid"

         // type fan
        const val KEY_TYPE_FAN_TYPE = "fan"
        // type light
        const val KEY_TYPE_LIGHT_TYPE = "light"
        // type humidity
        const val KEY_TYPE_HUMIDITY_TYPE = "humidity"
        // type water_level
        const val KEY_TYPE_WATER_LEVEL_TYPE = "water_level"
        // type water_temperature
        const val KEY_TYPE_WATER_TEMPERATURE_TYPE = "water_temperature"
        // type temperature
        const val KEY_TYPE_TEMPERATURE_TYPE = "temperature"

        // type Drip
        const val KEY_TYPE_DRIP_TYPE = "Drip"


        // 新增的1个类型。文字描述
        const val KEY_TYPE_TEXT = 3

        // KEY_TYPE_GRID
        const val KEY_TYPE_GRID = 4
    }
}