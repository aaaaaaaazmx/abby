package com.cl.common_base.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.cl.common_base.BaseBean
import com.luck.picture.lib.entity.LocalMedia

data class ChoosePicBean(
    val type: Int? = KEY_TYPE_ADD,
    val picAddress: String? = null,
    var isUploading: Boolean? = false,
    var compressPicAddress: String? = null,
    var localMedia: LocalMedia? = null,
) : BaseBean(), MultiItemEntity {
    override val itemType: Int
        get() = when(type) {
            KEY_TYPE_ADD -> KEY_TYPE_ADD
            KEY_TYPE_PIC -> KEY_TYPE_PIC
            else -> KEY_TYPE_ADD
        }


    companion object {
        const val KEY_TYPE_ADD = 0
        const val KEY_TYPE_PIC = 1
    }
}