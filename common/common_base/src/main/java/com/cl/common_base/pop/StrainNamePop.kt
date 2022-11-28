package com.cl.common_base.pop

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.cl.common_base.R
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.StrainNameBinding
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.google.common.base.Strings.isNullOrEmpty
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.AttachPopupView
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * StrainName Pop
 */
class StrainNamePop(
    context: Context,
    private val onConfirmAction: ((strainName: String) -> Unit)? = null,
    private val onCancelAction: (() -> Unit)? = null,
    private val isNoStrainName: Boolean? = false
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.strain_name
    }

    private var binding: StrainNameBinding? = null
    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<StrainNameBinding>(popupImplView)?.apply {
            tvHow.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            ViewUtils.setGone(ivClose, isNoStrainName ?: false)

            ivClose.setOnClickListener {
                onCancelAction?.invoke()
                dismiss()
                btnSuccess.isEnabled = false
            }

            strainName.textChangeFlow() // 构建输入框文字变化流
                .filter { it.isNotEmpty() } // 过滤空内容，避免无效网络请求
                .debounce(300) // 300ms防抖
                .flatMapLatest { searchFlow(it.toString()) } // 新搜索覆盖旧搜索
                .flowOn(Dispatchers.IO) // 让搜索在异步线程中执行
                .onEach { updateUi(it) } // 获取搜索结果并更新界面
                .launchIn(lifecycleScope) // 在主线程收集搜索结果

            // 清空输入内容
            curingDelete.setOnClickListener {
                strainName.setText("")
                btnSuccess.isEnabled = false
            }

            tvHow.setOnClickListener {
                XPopup.Builder(context)
                    .isDestroyOnDismiss(false)
                    .isDestroyOnDismiss(false)
                    .asCustom(BaseCenterPop(context, content = context.getString(R.string.seed_strain_name), isShowCancelButton = false))
                    .show()
            }

            clNotKnow.setOnClickListener {
                // 跳过的话，默认名字
                onConfirmAction?.invoke("I don’t know")
            }

            btnSuccess.setOnClickListener {
                // 输入范围为1～24字节
                if (getTextLength(strainName.text.toString()) < 1 || getTextLength(strainName.text.toString()) > 24) {
                    ToastUtil.shortShow(context.getString(R.string.strain_name_desc))
                    return@setOnClickListener
                }
                onConfirmAction?.invoke(strainName.text.toString())
            }

        }
    }

    // 构建输入框文字变化流
    private fun EditText.textChangeFlow(): Flow<Editable> = callbackFlow {
        // 构建输入框监听器
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // 在文本变化后向流发射数据
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 点击按钮状态监听
                binding?.btnSuccess?.isEnabled = !s.isNullOrEmpty()
                trySend(text)
            }
        }
        addTextChangedListener(watcher) // 设置输入框监听器
        awaitClose { removeTextChangedListener(watcher) } // 阻塞以保证流一直运行
    }

    private val service = ServiceCreators.create(BaseApiService::class.java)
    private suspend fun getStrainNameList(txt: String): MutableList<String> {
        val mutableList = mutableListOf<String>()
        service.getStrainName(txt)
            .map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code,
                        it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }
            .flowOn(Dispatchers.IO)
            .onStart {
                emit(Resource.Loading())
            }
            .catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1,
                        "$it"
                    )
                )
            }.collectLatest {
                logI(it.toString())
                it.data?.let { it1 -> mutableList.addAll(it1) }
            }
        return mutableList
    }

    // 更新界面
    fun updateUi(it: List<String>) {
        XPopup.Builder(context)
            .hasShadowBg(false) //                        .isRequestFocus(false)
            .isCoverSoftInput(true) //                        .isViewMode(true)
            //                        .popupAnimation(PopupAnimation.ScrollAlphaFromTop)
            //                        .isClickThrough(true)
            //                        .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            //                        .isDarkTheme(true)
            //                        .popupAnimation(PopupAnimation.ScrollAlphaFromTop) //NoAnimation表示禁用动画
            //                        .isCenterHorizontal(true) //是否与目标水平居中对齐
            //                        .offsetY(60)
            //                        .offsetX(80)
            //                        .popupPosition(PopupPosition.Top) //手动指定弹窗的位置
            //                        .popupWidth(500)
            .atView(binding?.strainName) // 依附于所点击的View，内部会自动判断在上方或者下方显示
            .asAttachList(
                it.toTypedArray(), null,
                { position, text -> binding?.strainName?.setText(text) }, 0, 0 /*, Gravity.LEFT*/
            ).show()
    }

    // 访问网络进行搜索
    suspend fun search(key: String): List<String> {
        return getStrainNameList(key)
    }

    // 将搜索关键词转换成搜索结果流
    fun searchFlow(key: String) = flow { emit(search(key)) }

    /**
     * 屏蔽手机系统返回键
     */
    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onDismiss() {
        onCancelAction?.invoke()
        super.onDismiss()
    }

    private fun getTextLength(text: CharSequence): Int {
        var length = 0
        for (element in text) {
            if (element.code > 255) {
                length += 2
            } else {
                length++
            }
        }
        return length
    }
}