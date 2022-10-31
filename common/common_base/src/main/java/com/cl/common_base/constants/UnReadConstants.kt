package com.cl.common_base.constants

/**
 * 未读消息
 *
 * @author 李志军 2022-08-15 15:10
 */
object UnReadConstants {

    /**
     * 	类型	字段
    种植相关	种植	0
    Vegetation	1
    Flowering	2
    Flushing	3
    Drying	5
    Harvest	6
    Curing	7（请求图文时id转换为int）
    换水提醒	changing_water
    加水提醒	add_water
    加肥料提醒	add_manure
    设备故障	进气风扇故障	fan_in_fault
    排气风扇故障	fan_out_fault
    环境温度传感器故障	sensor_rh_fault
    水箱温度传感器故障	sensor_water_fault
    水位传感器故障	sensor_level_fault
    水泵故障	water_pump_fault
    气泵故障	gas_pump_fault
    植物高度传感器故障	sensor_height_fault
    机箱温度异常	environment_fault
    水箱温度异常	water_fault
    机箱湿度异常	humidity_fault
    自定义	自定义类型	user_defined
    其他	童锁	child_lock
     */

    /**
     * 种植相关
     */
    object Plant {
        const val KEY_PLANT = "0"
        const val KEY_VEGETATION = "1"
        const val KEY_FLOWERING = "2"
        const val KEY_FLUSHING = "3"
        const val KEY_DRYING = "5"
        const val KEY_HARVEST = "6"
        const val KEY_CURING = "7"
        const val KEY_INCUBATION = "8"
    }

    /**
     * 日历点击问号相关
     */
    object CalendarAsk {
        const val KEY_SEED_EXPLAIN = "seed_explain"
        const val KEY_VEGETATION_EXPLAIN = "vegetation_explain"
        const val KEY_BLOOM_EXPLAIN = "bloom_explain"
        const val KEY_FLUSHING_EXPLAIN = "flushing_explain"
        const val KEY_HARVEST_EXPLAIN = "harvest_explain"
        const val KEY_DRYING_EXPLAIN = "drying_explain"
        const val KEY_CURING_EXPLAIN = "curing_explain"
        const val KEY_UNEARNED_SUBSCRIPTION_EXPLAIN = "unearned_subscription_explain"
        const val KEY_LST = "lst"
        const val KEY_TOPPING = "topping"
        const val KEY_TRIM = "trim"
    }

    /**
     * 种植状态
     */
    object PlantStatus {
        const val TASK_TYPE_CHECK_TRANSPLANT = "check_transplant"
        const val TASK_TYPE_CHECK_CHECK_AUTOFLOWERING = "check_autoflowering"
        const val TASK_TYPE_CHECK_CHECK_FLOWERING = "check_flowering"
        const val TASK_TYPE_CHECK_CHECK_FLUSHING = "check_flushing"
        const val TASK_TYPE_CHECK_CHECK_HARVEST = "check_harvest"
        const val TASK_TYPE_CHECK_CHECK_DRYING = "check_drying"
        const val TASK_TYPE_CHECK_CHECK_CURING = "check_curing"
        const val TASK_TYPE_CHECK_CHECK_FINISH = "check_finish"
    }

    /**
     * 周期状态
     */
    object PeriodStatus {
        const val KEY_SEED = "Seed"
        const val KEY_GERMINATION = "Germination"
        const val KEY_VEGETATION = "Vegetation"
        const val KEY_FLOWERING = "Flowering"
        const val KEY_AUTOFLOWERING = "AutoFlowering"
        const val KEY_FLUSHING = "Flushing"
        const val KEY_HARVEST = "Harvest"
        const val KEY_CURING = "Curing"
        const val KEY_DRYING = "Drying"
    }

    /**
     * 设备提醒相关
     */
    object Device {
        const val KEY_CHANGING_WATER = "change_water"
        const val KEY_CHANGE_CUP_WATER = "change_cup_water"
        const val KEY_ADD_WATER = "add_water"
        const val KEY_ADD_MANURE = "add_manure"

        // 这个和下面那个特别一下，不显示气泡，显示弹窗
        const val KEY_FAN_IN_FAULT = "fan_in_fault"
        const val KEY_FAN_OUT_FAULT = "fan_out_fault"
        const val KEY_SENSOR_RH_FAULT = "sensor_rh_fault"
        const val KEY_SENSOR_WATER_FAULT = "sensor_water_fault"
        const val KEY_SENSOR_LEVEL_FAULT = "sensor_level_fault"
        const val KEY_SWATER_PUMP_FAULT = "water_pump_fault"
        const val KEY_GAS_PUMP_FAULT = "gas_pump_fault"
        const val KEY_SENSOR_HEIGHT_FAULT = "sensor_height_fault"

        // 异常
        const val KEY_ENVIRONMENT_FAULT = "environment_fault"
        const val KEY_WATER_FAULT = "water_fault"
        const val KEY_HUMIDITY_FAULT = "humidity_fault"

        const val KEY_USER_DEFINED = "user_defined"
        const val KEY_CHILD_LOCK = "child_lock"
        const val KEY_ACTIVITY_IN_TREND = "activity_in_trend"
    }

    /**
     * 上报三布局
     */
    object StatusManager {
        const val VALUE_STATUS_PUMP_WATER = "pump_water"
        const val VALUE_STATUS_ADD_WATER = "add_water"
        const val VALUE_STATUS_ADD_MANURE = "add_manure"
        const val VALUE_STATUS_SKIP_CHANGING_WATERE = "skip_changing_water"
    }


    /**
     * 跳转相关
     */
    object JumpType {
        const val KEY_NONE = "none"
        const val KEY_FEED_BACK = "feed_back"
        const val KEY_GUIDE = "guide"
        const val KEY_LEARN_MORE = "learn_more"
        const val KEY_TREND = "trend"
    }

    /**
     * extension 消息
     */
    object Extension {
        const val KEY_EXTENSION_CONTINUE = "continue"
        const val KEY_EXTENSION_CONTINUE_ONE = "continue1"
        const val KEY_EXTENSION_CONTINUE_TWO = "continue2"
        const val KEY_EXTENSION_CONTINUE_THREE = "continue3"
    }


    /**
     * 种植状态集合
     *
     *  这个地方需要删除、以后不能这样判断了。
     */
    var plantStatus = mutableListOf(
        PlantStatus.TASK_TYPE_CHECK_TRANSPLANT,
        PlantStatus.TASK_TYPE_CHECK_CHECK_FLOWERING,
        PlantStatus.TASK_TYPE_CHECK_CHECK_FLUSHING,
        PlantStatus.TASK_TYPE_CHECK_CHECK_HARVEST,
        PlantStatus.TASK_TYPE_CHECK_CHECK_DRYING,
        PlantStatus.TASK_TYPE_CHECK_CHECK_CURING,
        PlantStatus.TASK_TYPE_CHECK_CHECK_FINISH,
    )

    /**
     *
     */

    /**
     * 无取消按钮状态集合
     *   NSArray *nocancelArr = @[@"changing_water",@"add_water",@"add_manure",@"activity_in_trend"];
     */
    val noCancel = mutableListOf(
        Device.KEY_CHANGING_WATER,
        Device.KEY_ADD_WATER,
        Device.KEY_ADD_MANURE,
        Device.KEY_ACTIVITY_IN_TREND
    )

    // 故障列表
    val malfunction = mutableListOf(
        Device.KEY_FAN_IN_FAULT,
        Device.KEY_FAN_OUT_FAULT,
        Device.KEY_SENSOR_RH_FAULT,
        Device.KEY_SENSOR_WATER_FAULT,
        Device.KEY_SENSOR_LEVEL_FAULT,
        Device.KEY_SWATER_PUMP_FAULT,
        Device.KEY_GAS_PUMP_FAULT,
        Device.KEY_SENSOR_HEIGHT_FAULT,
    )
}