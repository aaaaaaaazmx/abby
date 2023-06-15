package com.cl.common_base.adapter

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.cl.common_base.R
import com.cl.common_base.bean.AdvertisingData
import com.cl.common_base.databinding.BaseItemPumpWaterBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.video.videoUiHelp
import com.cl.common_base.video.SampleCoverVideo

/**
 * 排水适配器
 * @author 李志军 2022-08-10 15:32
 */
class PumpWaterAdapter(data: MutableList<AdvertisingData>?) :
    BaseQuickAdapter<AdvertisingData, BaseDataBindingHolder<BaseItemPumpWaterBinding>>(R.layout.base_item_pump_water, data) {

    override fun convert(holder: BaseDataBindingHolder<BaseItemPumpWaterBinding>, item: AdvertisingData) {

        holder.dataBinding?.apply {
            data = item
            executePendingBindings()
        }
    }



    /*override fun convert(helper: BaseViewHolder, item: AdvertisingData) {
        // 获取 Binding
        val binding: BaseItemPumpWaterBinding? = helper.getBinding()
        if (binding != null) {
            // 设置数据
            binding.data = item
            binding.executePendingBindings()
        }
        // 视频播放
        item.video?.let {
            helper.getView<SampleCoverVideo>(R.id.video_item_player).apply {
                videoUiHelp(item.video, helper.layoutPosition)
                setIsShowProgressBar(false)
                //设置全屏按键功能
//                fullscreenButton.setOnClickListener {
//                    val intent = Intent(context, GSYPlayVideoActivity::class.java)
//                    intent.putExtra("url", item.video)
//                    context.startActivity(intent)
//                }
            }
        }

    }*/
}
