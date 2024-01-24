package com.cl.modules_my.repository

/**
 * "corF": 0,
 *   "deviceId": "",
 *   "tapeLights": true,
 *   "usb1": true,
 *   "usb2": true,
 *   "usb3": true
 */
data class UsbSwitchReq(
    val deviceId: String?,
    val usb1: Boolean,
    val usb2: Boolean,
    val usb3: Boolean,
    val corF: Int,
    val tapeLights: Boolean,
)