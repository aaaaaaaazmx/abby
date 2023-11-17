package com.cl.common_base.adapter

import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.R
import com.cl.common_base.bean.FolowerData
import com.cl.common_base.constants.RouterPath
import com.cl.common_base.databinding.FollowPopBinding

class FollowerAndFolleringAdapter (data: MutableList<FolowerData>?) :
    BaseQuickAdapter<FolowerData, BaseDataBindingHolder<FollowPopBinding>>(R.layout.follow_pop, data) {


    override fun convert(holder: BaseDataBindingHolder<FollowPopBinding>, item: FolowerData) {
        holder.dataBinding?.apply {
            data = item
            executePendingBindings()

            // 跳转到他的用户详情
            clRoot.setOnClickListener {
                ARouter.getInstance().build(RouterPath.Contact.PAGE_OTHER_JOURNEY)
                    .withString("key_user_id", item.userId.toString())
                    .withString("key_user_name", item.nickName)
                    .navigation()
            }
        }
    }
}