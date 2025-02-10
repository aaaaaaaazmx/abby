package com.cl.modules_home.request

data class MultiPlantListData(
    var growSpaceName: String? = null,
    var multiplantId: Int? = null,
    var number: Int? = null,
    var plantId: Int? = null,
    var strainName: String? = null,
    var isSyncStrainCheck: Boolean? = false
)