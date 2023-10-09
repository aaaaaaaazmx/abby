package com.cl.modules_planting_log.request

import com.cl.common_base.BaseBean

data class LogSaveOrUpdateReq(
    var co2Concentration: String? = null,
    var driedWeight: String? = null,
    var humidity: String? = null,
    var lightingSchedule: String? = null,
    var logId: String? = null,
    var logTime: String? = null,
    var logType: String? = null,
    var notes: String? = null,
    var period: String? = null,
    var ph: String? = null,
    var plantHeight: String? = null,
    var plantId: String? = null,
    var plantPhoto: MutableList<String?>? = null,
    var showType: String? = null,
    var spaceTemp: String? = null,
    var tdsEc: String? = null,
    var trainingAfterPhoto: String? = null,
    var trainingBeforePhoto: String? = null,
    var vpd: String? = null,
    var waterTemp: String? = null,
    var wetWeight: String? = null,
    var lightingOff: String? = null,
    var lightingOn: String? = null,
    var waterType: String? = null,
    var volume: String? = null,
    var feedingType: String? = null,
    var repellentType: String? = null,
    var declareDeathType: String? = null,
    var inchMetricMode: String? = null,
    var syncPost: Boolean? = null,
    var syncPlants: Boolean? = null, // 帐篷内同步所有植物
): BaseBean() {
    companion object {
        const val KEY_LOG_PH = "ph"
        const val KEY_LOG_TIME = "logTime"
        const val KEY_LIGHTING_ON = "lightingOn"
        const val KEY_LIGHTING_OFF = "lightingOff"
        const val KEY_SPACE_TEMP  = "spaceTemp"
        const val KEY_WATER_TEMP  = "waterTemp"
        const val KEY_PLANT_HEIGHT  = "plantHeight"
        const val KEY_DRIED_WEIGHT  = "driedWeight"
        const val KEY_WET_WEIGHT  = "wetWeight"
        const val KEY_LOG_TYPE = "logType"
        const val KEY_LOG_TYPE_WATER_TYPE = "waterType"
        const val KEY_LOG_TYPE_FEEDING = "feedingType"
        const val KEY_LOG_TYPE_REPELLENT = "repellentType"
        const val KEY_LOG_TYPE_DECLARE_DEATH = "declareDeathType"
    }
}