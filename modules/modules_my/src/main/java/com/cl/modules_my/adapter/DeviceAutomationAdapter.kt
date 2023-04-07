package com.cl.modules_my.adapter

import android.text.Html
import android.text.Spanned
import androidx.core.text.toSpannable
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cl.common_base.bean.AutomationListBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.util.Prefs
import com.cl.common_base.widget.FeatureItemSwitch
import com.cl.modules_my.R
import com.cl.modules_my.databinding.MyDeviceAutoListItemBinding
import java.util.regex.Matcher
import java.util.regex.Pattern

class DeviceAutomationAdapter(data: MutableList<AutomationListBean.AutoBean>?, private val switchListener: ((automationId: String, isCheck: Boolean) -> Unit)? = null) :
    BaseQuickAdapter<AutomationListBean.AutoBean, BaseViewHolder>(R.layout.my_device_auto_list_item, data) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<MyDeviceAutoListItemBinding>(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: AutomationListBean.AutoBean) {
        // 获取 Binding
        val binding: MyDeviceAutoListItemBinding? = holder.getBinding()
        binding?.apply {
            data = item
            adapter = this@DeviceAutomationAdapter
            executePendingBindings()
        }

        holder.getView<FeatureItemSwitch>(R.id.ft_check).apply {
            setSwitchCheckedChangeListener { _, isChecked ->
                switchListener?.invoke(item.automationId.toString(), isChecked)
            }
        }
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

    /**
     * 根据供应值来解析文本
     * 默认是英制
     */
    private val isF by lazy {
        val weightUnit = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)
        weightUnit
    }

    fun parseText(txt: String?): Spanned? {
        return  txt?.let {
            val realText = getRealText(it, isF)
            Html.fromHtml(realText)
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

}