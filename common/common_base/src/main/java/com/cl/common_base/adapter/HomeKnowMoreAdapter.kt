package com.cl.common_base.adapter

import android.text.Html
import android.text.Spanned
import androidx.core.text.toSpannable
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.R
import com.cl.common_base.bean.RichTextData
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.*
import com.cl.common_base.video.videoUiHelp
import com.cl.common_base.util.Prefs
import com.cl.common_base.video.SampleCoverVideo
import com.cl.common_base.widget.FeatureTitleBar
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * 富文本
 * @author 李志军 2022-08-06 18:44
 */
class HomeKnowMoreAdapter(data: MutableList<RichTextData.Page>?) :
    BaseMultiItemQuickAdapter<RichTextData.Page, BaseViewHolder>(data) {

    init {
        addItemType(RichTextData.KEY_TYPE_BAR, R.layout.home_bar_item)  // activity、页面的标题
        addItemType(RichTextData.KEY_TYPE_TITLE, R.layout.home_title_item)
        addItemType(RichTextData.KEY_TYPE_TXT, R.layout.home_txt_item)
        addItemType(RichTextData.KEY_TYPE_PICTURE, R.layout.home_picture_item) // todo 需要动态适配宽高
        addItemType(RichTextData.KEY_TYPE_URL, R.layout.home_url_item) // 视频以连接的形式
        addItemType(
            RichTextData.KEY_TYPE_VIDEO, R.layout.home_video_item
        ) // 视频以视频的形式  // todo 需要动态适配宽高
        addItemType(RichTextData.KEY_TYPE_PAGE_DOWN, R.layout.home_page_down_item) // 跳转下一页按钮
        addItemType(RichTextData.KEY_TYPE_PAGE_CLOSE, R.layout.home_page_close_item) // 关闭页面按钮
        addItemType(RichTextData.KEY_TYPE_CUSTOMER_SERVICE, R.layout.home_service_item) // 客服
        addItemType(
            RichTextData.KEY_TYPE_IMAGE_TEXT_JUMP, R.layout.home_image_text_jump_item
        ) // 图文跳转 // todo 未出图
        addItemType(RichTextData.KEY_TYPE_DISCORD, R.layout.home_discord_item) // 论坛跳转 // todo 未出图
        addItemType(
            RichTextData.KEY_TYPE_FINISH_TASK, R.layout.home_finis_task_item
        ) // 关闭页面按钮 // 未出图
        addItemType(RichTextData.KEY_TYPE_FLUSHING_WEIGH, R.layout.home_item_edit_pop) // 清洗期、重量
        addItemType(RichTextData.KEY_TYPE_DRYING_WEIGH, R.layout.home_item_curing_pop) // 干燥期、重量
        addItemType(RichTextData.KEY_TYPE_BUTTON_JUMP, R.layout.home_itme_button_jump) // 按钮跳转
        addItemType(RichTextData.KEY_TYPE_CHECK_BOX, R.layout.home_item_chexk_box)
        addItemType(RichTextData.KEY_TYPE_INPUT_BOX, R.layout.home_item_input_box)
        addItemType(RichTextData.KEY_TYPE_DELAY_TASK, R.layout.home_item_delay_task)
        /*addItemType(RichTextData.KEY_TYPE_PAGE_TXT, R.layout.home_itme_page_txt)*/
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when (holder.itemViewType) {
            RichTextData.KEY_TYPE_BAR -> {
                val binding = DataBindingUtil.bind<HomeBarItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            RichTextData.KEY_TYPE_DELAY_TASK -> {
                val binding = DataBindingUtil.bind<HomeItemDelayTaskBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }

            RichTextData.KEY_TYPE_TITLE -> {
                val binding = DataBindingUtil.bind<HomeTitleItemBinding>(holder.itemView)
                if (binding != null) {
                    // 设置数据
                    binding.data = data[position]
                    binding.executePendingBindings()
                }
            }
            RichTextData.KEY_TYPE_PICTURE -> {
                DataBindingUtil.bind<HomePictureItemBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }

            RichTextData.KEY_TYPE_TXT -> {
                DataBindingUtil.bind<HomeTxtItemBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.adapter = this@HomeKnowMoreAdapter
                    it.executePendingBindings()
                }
            }

            RichTextData.KEY_TYPE_URL -> {
                DataBindingUtil.bind<HomeUrlItemBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }

            RichTextData.KEY_TYPE_VIDEO -> {
                DataBindingUtil.bind<HomeVideoItemBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }

            RichTextData.KEY_TYPE_PAGE_DOWN -> {
                DataBindingUtil.bind<HomePageDownItemBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }

            RichTextData.KEY_TYPE_PAGE_CLOSE -> {
                DataBindingUtil.bind<HomePageCloseItemBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }
            RichTextData.KEY_TYPE_CUSTOMER_SERVICE -> {
                DataBindingUtil.bind<HomeServiceItemBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }
            RichTextData.KEY_TYPE_IMAGE_TEXT_JUMP -> {
                DataBindingUtil.bind<HomeImageTextJumpItemBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }
            RichTextData.KEY_TYPE_DISCORD -> {
                DataBindingUtil.bind<HomeDiscordItemBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }
            RichTextData.KEY_TYPE_FINISH_TASK -> {
                DataBindingUtil.bind<HomeFinisTaskItemBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }
            RichTextData.KEY_TYPE_FLUSHING_WEIGH -> {
                DataBindingUtil.bind<HomeItemPopBinding>(holder.itemView)?.let {
                    it.datas = data[position]
                    it.executePendingBindings()
                }
            }
            RichTextData.KEY_TYPE_DRYING_WEIGH -> {
                DataBindingUtil.bind<HomeItemCuringPopBinding>(holder.itemView)?.let {
                    it.datas = data[position]
                    it.executePendingBindings()
                }
            }
            RichTextData.KEY_TYPE_BUTTON_JUMP -> {
                DataBindingUtil.bind<HomeItmeButtonJumpBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }
            RichTextData.KEY_TYPE_CHECK_BOX -> {
                DataBindingUtil.bind<HomeItemChexkBoxBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }
            RichTextData.KEY_TYPE_INPUT_BOX -> {
                DataBindingUtil.bind<HomeItemInputBoxBinding>(holder.itemView)?.let {
                    it.datas = data[position]
                    it.executePendingBindings()
                }
            }
            /*RichTextData.KEY_TYPE_PAGE_TXT -> {
                DataBindingUtil.bind<HomeItmePageTxtBinding>(holder.itemView)?.let {
                    it.data = data[position]
                    it.executePendingBindings()
                }
            }*/
        }
    }

    override fun convert(helper: BaseViewHolder, item: RichTextData.Page) {
        // 获取 Binding
        //        val binding: HomeFinishGuideItemBinding? = helper.getBinding()
        //        if (binding != null) {
        //            binding.data = item
        //            binding.executePendingBindings()
        //        }

        when (helper.itemViewType) {
            RichTextData.KEY_TYPE_BAR -> {
                val tvTitle =
                    helper.itemView.findViewById<FeatureTitleBar>(com.cl.common_base.R.id.title)
                tvTitle.setTitle(item.value?.txt)
            }

            // 视频播放器设置
            RichTextData.KEY_TYPE_VIDEO -> {
                /*if (item.videoTag) {
                    // 第一帧显示的图
                    val url = item.value?.url
                    helper.getView<SampleCoverVideo>(R.id.video_item_player).apply {
                        loadCoverImage(url, R.mipmap.placeholder)
                        setUp(url, true, null, null, item.value?.title)
                        // 暂停状态下显示封面
                        isShowPauseCover = true
                        seekOnStart = item.videoPosition ?: 0L
                    }
                    return
                }*/
                helper.getView<SampleCoverVideo>(R.id.video_item_player).apply {
                    item.videoTag = true
                    item.value?.url?.let {
                        videoUiHelp(it, helper.layoutPosition)
                        // 暂停状态下显示封面
                        isShowPauseCover = true
                        seekOnStart = item.videoPosition ?: 0L
                        if (item.value.autoplay == true) startPlayLogic()
                    }
                }
            }

            // 动态设置宽高
            //            RichTextData.KEY_TYPE_PICTURE -> {
            //                logI(
            //                    """
            //                    windwo:
            //                    ${AppUtil.getWindowWidth()}
            //                    ${AppUtil.getWindowHeight()}
            //                """.trimIndent()
            //                )
            //
            //                kotlin.runCatching {
            //                    letMultiple(item.extend?.width, item.extend?.height) { width, height ->
            //                        val widthProportion = width.toInt().div(height.toInt())
            //                        val heightProportion = height.toInt().div(width.toInt())
            //
            //                        val ivImg = helper.itemView.findViewById<ImageView>(com.cl.common_base.R.id.iv_pic)
            //                        val layoutParams = ivImg.layoutParams
            //                        layoutParams.height = heightProportion * AppUtil.getWindowHeight()
            //                        // layoutParams.width = -1
            //                        ivImg.layoutParams = layoutParams
            //                    }
            //                }
            //            }
        }

    }

    /**
     * 根据供应值来解析文本
     * 默认是英制
     */
    private val isF by lazy {
        val weightUnit = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        weightUnit
    }

    private fun getRealText(txt: String, isF: Boolean): String {
        kotlin.runCatching {
            val matching = getAllSatisfyStr(txt, """(\[\[)InchMetricMode:.*?(\]\])""")?.toMutableList()
            if (matching.isNullOrEmpty()) return txt
            val matcherList = getAllSatisfyStr(txt, """(?<=\[{2}InchMetricMode:).+?(?=]{2})""")

            if (matcherList?.isEmpty() == true) return txt
            if (matching.size != matcherList?.size) return txt
            var text: String? = txt
            matcherList.forEachIndexed { index, value ->
                val split = matcherList[index].split(",")
                if (split.size < 2) {
                    text = txt
                    return@forEachIndexed
                }
                /*println("45454545: ${text?.replaceFirst(matching[index], if (!isF) split[0] else split[1])}")*/
                text = text?.replaceFirst(matching[index], if (!isF) split[0] else split[1])
            }
            return text.toString()
        }.getOrElse {
            return txt
        }
        /*println("45454545: $text")*/
    }

    fun parseText(txt: String?, bolds: MutableList<String>?): Spanned? {
        return  txt?.let {
            val realText = getRealText(it, isF)
            var str = realText
            val targets = bolds
            if (targets.isNullOrEmpty()) return str.toSpannable()
            val boldStart = "<b>"
            val boldEnd = "</b>"
            for (target in targets) {
                if (str.contains(target)) {
                    val parts = str.split(target.toRegex()).toTypedArray()
                    val builder = StringBuilder()
                    builder.append(parts[0])
                    for (i in 1 until parts.size) {
                        builder.append(boldStart)
                        builder.append(target)
                        builder.append(boldEnd)
                        builder.append(parts[i])
                    }
                    str = builder.toString()
                }
            }
            Html.fromHtml(str)
        }
    }

    /**
     * 获取所有满足正则表达式的字符串
     * @param str 需要被获取的字符串
     * @param regex 正则表达式
     * @return 所有满足正则表达式的字符串
     */
    private fun getAllSatisfyStr(str: String, regex: String): ArrayList<String>? {
        if (str == null || str.isEmpty()) {
            return null
        }
        val allSatisfyStr: ArrayList<String> = ArrayList()
        if (regex == null || regex.isEmpty()) {
            allSatisfyStr.add(str)
            return allSatisfyStr
        }
        val pattern: Pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(str)
        while (matcher.find()) {
            allSatisfyStr.add(matcher.group())
        }
        return allSatisfyStr
    }


    // 设置

    companion object {
        const val TAG = "ListNormalAdapter22"
    }
}