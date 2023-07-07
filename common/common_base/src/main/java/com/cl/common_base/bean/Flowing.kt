package com.cl.common_base.bean

import com.cl.common_base.bean.RunnerWater

data class Flowing(
    val expense: String,
    val income: String,
    val list: List<RunnerWater>,
    val totalOxygen: String,
    val yearMonth: String
)