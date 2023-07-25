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
        val alert: Int? = null,
        val articleDetails: String? = null,
        var automation: Int? = null, // 是否是自动模式
    ) : BaseBean(), MultiItemEntity {
        override val itemType: Int
            get() = when (detectionValue) {
                "Fan" -> KEY_TYPE_FAN
                "Grow light" -> KEY_TYPE_LIGHT
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
    }
}