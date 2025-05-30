package com.cl.common_base.bean

data class DigitalAssetData(
    val achievements: List<Achievement>,
    val basicInfo: BasicInfo,
    val frames: List<Frame>,
    val oxygen: Int,
    val oxygenByMonth: Int,
    val userFlags: List<UserFlag>,
    val wallpapers: List<Wallpaper>,
)