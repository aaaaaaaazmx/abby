package com.cl.common_base.bean

import com.cl.common_base.BaseBean

data class PresetData(
    var id: Int? = null,
    var name: String? = null,
    var note: String? = null,
    var fanIntake: String? = null,
    var fanExhaust: String? = null,
    var lightSchedule: String? = null,
    var lightIntensity: String? = null,
    var muteOn: String? = null,
    var muteOff: String? = null,
): BaseBean()