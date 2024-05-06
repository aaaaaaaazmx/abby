package com.cl.common_base.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.cl.common_base.BaseBean
import com.google.gson.annotations.SerializedName

data class RichTextData(
    val flushingWeigh: String? = null, // 冲刷期重量
    val name: String? = null, // 文档名称
    val txtId: String? = null, // 文本ID
    val testType: String? = null, // 文本ID
    val bar: String? = null, // 文本标题
    val page: MutableList<Page>? = null,
    val topPage: MutableList<TopPage>? = null,
) : BaseBean() {

    data class Page(
        val id: String? = null,
        val type: String? = null,
        val value: Value? = null,
        var videoTag: Boolean = false,
        var videoPosition: Long? = null,
        var isPreview: Boolean? = false, // 手动添加字段，在预览期间不要显示延迟item （not ready, remind me later）
    ) : BaseBean(), MultiItemEntity {
        override val itemType: Int
            get() = when (type) {
                "Bar" -> KEY_TYPE_BAR
                "title" -> KEY_TYPE_TITLE
                "txt" -> KEY_TYPE_TXT
                "picture" -> KEY_TYPE_PICTURE
                "url" -> KEY_TYPE_URL
                "video" -> KEY_TYPE_VIDEO
                "pageDown" -> KEY_TYPE_PAGE_DOWN
                "pageClose" -> KEY_TYPE_PAGE_CLOSE
                "customerService" -> KEY_TYPE_CUSTOMER_SERVICE
                "imageTextJump" -> KEY_TYPE_IMAGE_TEXT_JUMP
                "Discord" -> KEY_TYPE_DISCORD
                "finishTask" -> KEY_TYPE_FINISH_TASK
                "flushingWeigh" -> KEY_TYPE_FLUSHING_WEIGH
                "dryingWeigh" -> KEY_TYPE_DRYING_WEIGH
                "buttonJump" -> KEY_TYPE_BUTTON_JUMP
                "option" -> KEY_TYPE_CHECK_BOX
                "inputBox" -> KEY_TYPE_INPUT_BOX
                "snooze" -> KEY_TYPE_DELAY_TASK
                "intercom" -> KEY_TYPE_TXT
                "usb_port_detail" -> KEY_TYPE_USB_PORT_DETAIL
                "usb_port" -> KEY_TYPE_USB_PORT
                else -> KEY_TYPE_BAR
            }
    }

    data class TopPage(
        val id: String? = null,
        val type: String? = null,
        val value: Value? = null,
    )

    data class Value(
        val height: String? = null, // 高度
        val taskCategory: String? = null, // 任务种类
        val taskId: String? = null, // 任务ID
        val taskType: String? = null, // 任务类型
        val title: String? = null, // 标题
        val top: String? = null, // 是否悬浮
        val txt: String? = null, // 文本
        val inputType: String? = null, // 文本输入类型
        val inputSize: Int? = null, // 文本输入框长度
        val txtId: String? = null, // 文本ID
        val url: String? = null, // URL
        val width: String? = null, // 宽度
        val icon: String? = null, // 按钮图标
        val autoplay: Boolean? = null, // 自动播放
        var isCheck: Boolean? = false, // 是否选中
        @SerializedName("colour")
        var color: String? = null,  // 字体颜色
        var size: String? = null,   // 字体大小
        @SerializedName("textAlign")
        var left: String? = null,   // 左边距
        var bold: Boolean? = false, // 字体是否加粗
        var bolds: MutableList<String>? = null,
        var articleId: String? = null, // 文章ID
        var dynamicData: String? = null, // usb 按钮详情
    ) : BaseBean()


    companion object {
        // 这是Activity页面的标题
        const val KEY_TYPE_BAR = 1

        // 这是内容的标题
        const val KEY_TYPE_TITLE = 2

        // 这是内容文本
        const val KEY_TYPE_TXT = 3

        // 图片
        const val KEY_TYPE_PICTURE = 4

        // 设置视频的url类型、以文本的形式展示
        const val KEY_TYPE_URL = 5

        // 这是视频展示类型、以视频样式展示
        const val KEY_TYPE_VIDEO = 6

        // 跳下页
        const val KEY_TYPE_PAGE_DOWN = 7

        // 关闭本页
        const val KEY_TYPE_PAGE_CLOSE = 8

        // 客服类型
        const val KEY_TYPE_CUSTOMER_SERVICE = 9

        // 图文跳转
        const val KEY_TYPE_IMAGE_TEXT_JUMP = 10

        // Discord 类型
        const val KEY_TYPE_DISCORD = 11

        // 任务完成
        const val KEY_TYPE_FINISH_TASK = 12

        // 清洗期
        const val KEY_TYPE_FLUSHING_WEIGH = 13

        // 干燥器重量
        const val KEY_TYPE_DRYING_WEIGH = 14

        // 跳转到webView
        const val KEY_TYPE_BUTTON_JUMP = 15

        // 设置checkbox勾选项类型
        const val KEY_TYPE_CHECK_BOX = 16

        // 设置文本类型
        const val KEY_TYPE_PAGE_TXT = 17

        // 这是个不知名的类型，ui展示为一个输入框
        const val KEY_TYPE_INPUT_BOX = 18

        // 推迟延迟任务类型文案
        const val KEY_TYPE_DELAY_TASK = 19

        // usb_port_detail
        const val KEY_TYPE_USB_PORT_DETAIL = 20

        // usb_port
        const val KEY_TYPE_USB_PORT = 21

        // 自己创建的类型
        // 与商民的无关
        const val KEY_BAR = "Bar"
    }
}