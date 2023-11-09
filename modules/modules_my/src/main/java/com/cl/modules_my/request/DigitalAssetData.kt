package com.cl.modules_my.request

import com.cl.common_base.bean.BasicInfo
import com.cl.common_base.bean.UserFlag

data class DigitalAssetData(
    val achievements: List<Achievement>,
    val basicInfo: BasicInfo,
    val frames: List<Frame>,
    val oxygen: Int,
    val oxygenByMonth: Int,
    val userFlags: List<UserFlag>,
    val wallpapers: List<Wallpaper>,
)