package com.cl.modules_home.request

data class EnvParamListBeanItem(
    var brightValue: Int,
    var day: Int,
    var envName: String,
    var envId: String? = null,
    var isDeleted: Boolean,
    var runningOn: Boolean,
    var turnOffLight: Int,
    var turnOnLight: Int,
    val ppfdTxtId: String,
    var week: Int,
    var sweek: Int,
    var sday: Int,
)