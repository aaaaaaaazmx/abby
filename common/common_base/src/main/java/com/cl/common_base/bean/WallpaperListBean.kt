package com.cl.common_base.bean

import com.cl.common_base.BaseBean

/**
 * 壁纸列表
 */
data class WallpaperListBean(
    val address: String? = null,
    val freePrice: Int? = null,
    val id: Int? = null,
    val isDefault: Int? = null,
    val name: String? = null,
    val price: Int? = null,
    val use: Int? = null,
): BaseBean()
