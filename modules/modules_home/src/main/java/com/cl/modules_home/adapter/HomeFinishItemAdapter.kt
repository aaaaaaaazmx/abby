package com.cl.modules_home.adapter

import com.bbgo.module_home.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.bean.FinishPageData

class HomeFinishItemAdapter(data: MutableList<FinishPageData.ListBean>?) :
    BaseQuickAdapter<FinishPageData.ListBean, BaseViewHolder>(
        R.layout.home_finish_title_item,
        data
    ) {

    override fun convert(helper: BaseViewHolder, item: FinishPageData.ListBean) {
        helper.setText(R.id.item_title, item.title)
//        helper.getView<FeatureItemView>(R.id.complete_start).setItemTitle(item.title)
    }
}