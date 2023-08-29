package com.cl.modules_planting_log.adapter

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.cl.common_base.ext.DateHelper
import com.cl.common_base.ext.logI
import com.cl.common_base.intercome.InterComeHelp
import com.cl.modules_planting_log.R
import com.cl.modules_planting_log.databinding.PlantingLogListItemBinding
import com.cl.modules_planting_log.request.CardInfo
import com.cl.modules_planting_log.request.LogListDataItem
import com.cl.modules_planting_log.request.PlantLogTypeBean
import com.cl.modules_planting_log.ui.PlantingLogActivity
import com.cl.modules_planting_log.widget.PlantChooseLogTypePop
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.util.XPopupUtils

class PlantLogListAdapter(data: MutableList<LogListDataItem>?, private val onDeleteInterComeCard: ((period: String) -> Unit)? = null, private val onEditCard: ((period: String, logId: String, showType: String) -> Unit)? = null) :
    BaseQuickAdapter<LogListDataItem, BaseDataBindingHolder<PlantingLogListItemBinding>>(R.layout.planting_log_list_item, data) {
    override fun convert(holder: BaseDataBindingHolder<PlantingLogListItemBinding>, item: LogListDataItem) {
        holder.dataBinding?.apply {
            adapter = this@PlantLogListAdapter
            bean = item
            lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
            executePendingBindings()
        }

        // 卡片适配器
        holder.getView<RecyclerView>(R.id.rv_log_card).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = PlantLogCardAdapter(item.list).apply {
                addChildClickViewIds(R.id.cl_card_root, R.id.iv_edit, R.id.iv_close, R.id.iv_notes)
                setOnItemChildClickListener { adapter, view, position ->
                    val cardInfoData = item.list[position]
                    when (view.id) {
                        R.id.cl_card_root -> {
                            // 跳转interCome
                            if (cardInfoData.showType == CardInfo.TYPE_TIPS) {
                                cardInfoData.intercomId?.let { InterComeHelp.INSTANCE.openInterComeSpace(space = InterComeHelp.InterComeSpace.Article, id = cardInfoData.intercomId) }
                            } else {
                                // 跳转编辑界面
                                logI("click Edit")
                                // 获取日志详情，然后跳转到日志界面
                                onEditCard?.invoke(item.period, cardInfoData.logId.toString(), cardInfoData.showType)
                            }
                        }

                        R.id.iv_edit -> {
                            // 弹窗、分享到朋友圈还是去编辑
                            XPopup.Builder(context).popupPosition(PopupPosition.Bottom).dismissOnTouchOutside(true).isClickThrough(false)  //点击透传
                                .hasShadowBg(true) // 去掉半透明背景
                                //.offsetX(XPopupUtils.dp2px(this@MainActivity, 10f))
                                .atView(view).isCenterHorizontal(false).asCustom(context.let {
                                    PlantChooseLogTypePop(it,
                                        list = mutableListOf(
                                            PlantLogTypeBean("Edit", false),
                                            PlantLogTypeBean("Share", false),
                                        ), onConfirmAction = { txt ->
                                            // 跳转到相对应的界面
                                            when (txt) {
                                                "Edit" -> {
                                                    logI("click Edit")
                                                    // 获取日志详情，然后跳转到日志界面
                                                    onEditCard?.invoke(item.period, cardInfoData.logId.toString(), cardInfoData.showType)
                                                }

                                                "Share" -> {
                                                    // todo 分享到朋友圈
                                                    logI("click Share")
                                                }
                                            }
                                        }).setBubbleBgColor(Color.WHITE) //气泡背景
                                        .setArrowWidth(XPopupUtils.dp2px(context, 6f)).setArrowHeight(
                                            XPopupUtils.dp2px(
                                                context, 6f
                                            )
                                        ) //.setBubbleRadius(100)
                                        .setArrowRadius(
                                            XPopupUtils.dp2px(
                                                context, 3f
                                            )
                                        )
                                }).show()
                        }

                        R.id.iv_notes -> {
                            // todo 编辑备注弹窗
                            // 获取日志详情，然后跳转到日志界面
                            onEditCard?.invoke(item.period, cardInfoData.logId.toString(), cardInfoData.showType)
                        }

                        R.id.iv_close -> {
                            // 调用接口，然后移除
                            removeAt(position)
                            onDeleteInterComeCard?.invoke(item.period)
                        }
                    }
                }
            }
        }
    }

    fun formatText(dataFormat: String?): String {
        return dataFormat?.let {
            val currentDate = DateHelper.formatTime(System.currentTimeMillis(), "MM/dd/yy")
            if (currentDate == it) "Today" else it
        } ?: ""
    }
}