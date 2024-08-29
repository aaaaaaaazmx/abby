package com.cl.common_base.pop

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.cl.common_base.R
import com.cl.common_base.bean.UpPlantInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.BaseChooserPeriodPopBinding
import com.cl.common_base.databinding.BaseSelectPeriodPopBinding
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.ext.xpopup
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ChooserPeriodPop(
    context: Context,
    private val period: String,
    private val timeString: String,
    private val plantId: String,
    private val selectAction: ((String, String) -> Unit)? = null
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.base_chooser_period_pop
    }

    // 直接借口调用
    private val service = ServiceCreators.create(BaseApiService::class.java)
    private lateinit var binding: BaseChooserPeriodPopBinding


    private val loadingPopup by lazy {
        XPopup.Builder(context).asLoading(context.getString(R.string.string_216))
    }

    private var week: String = ""
    private var day: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind<BaseChooserPeriodPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@ChooserPeriodPop
            executePendingBindings()

            etCode.text = period
            val extractNumbers1 = extractNumbers(timeString)
            week = extractNumbers1.first.toString()
            day = extractNumbers1.second.toString()
            etEmail.text = "${context.getString(R.string.week)} ${extractNumbers1.first.toString()} ${context.getString(R.string.day)} ${extractNumbers1.second.toString()}"


            ivClose.setSafeOnClickListener { dismiss() }

            clPeriod.setSafeOnClickListener {
                // 跳转到周期选择弹窗
                xpopup(context) {
                    isDestroyOnDismiss(false)
                    dismissOnTouchOutside(false)
                    asCustom(SelectPeriodPop(context, etCode.text.toString(), plantId, selectAction = {
                        // 返回过来的周期。
                        etCode.text = it
                    })).show()
                }
            }

            clTime.setSafeOnClickListener {
                // 跳转到周期选择弹窗
                xpopup(context) {
                    isDestroyOnDismiss(false)
                    dismissOnTouchOutside(false)
                    asCustom(SelectPeriodTimePop(context, etEmail.text.toString(), selectAction = { week, day ->
                        val extractNumbers = extractNumbers("$week $day")
                        this@ChooserPeriodPop.week = extractNumbers.first.toString()
                        this@ChooserPeriodPop.day = extractNumbers.second.toString()
                        // 返回过来的周期。
                        etEmail.text = "$week $day"
                    })).show()
                }
            }

            btnSuccess.setSafeOnClickListener {
                val period = etCode.text.toString()
                // 更新植物信息接口
                messageConfig(week, day, period, plantId)
            }
        }!!
    }

    private fun extractNumbers(input: String): Pair<Int, Int> {
        // 定义正则表达式，匹配 "Week X Day Y" 其中 X 和 Y 为数字，周围可能有多余的空格
        val regex = """${context.getString(R.string.week)}\s+(\d+)\s+${context.getString(R.string.day)}\s+(\d+)""".toRegex()
        // 在输入字符串中查找第一个匹配项
        val matchResult = regex.find(input.trim())  // 使用 trim() 移除输入字符串两端的空白字符

        // 如果找到匹配，提取数字并返回；否则返回 (0, 0)
        return if (matchResult != null) {
            val (weekNumber, dayNumber) = matchResult.destructured
            Pair(weekNumber.toInt(), dayNumber.toInt())
        } else {
            Pair(0, 0)  // 如果没有找到匹配，返回 (0, 0)
        }
    }


    // 设置配置
    private fun messageConfig(week: String, day: String, period: String, plantId: String) = lifecycleScope.launch {
        service.updatePlantInfo(UpPlantInfoReq(week = week.safeToInt(), day = day.safeToInt(), period = period, plantId = plantId.safeToInt())).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            when (it) {
                is Resource.Success -> {
                    loadingPopup.dismiss()
                    val time = binding.etEmail.text.toString()
                    selectAction?.invoke(period, time)
                    dismiss()
                }

                is Resource.DataError -> {
                    loadingPopup.dismiss()
                    ToastUtil.shortShow(it.errorMsg)
                }

                is Resource.Loading -> {
                    loadingPopup.show()
                }
            }
        }
    }
}