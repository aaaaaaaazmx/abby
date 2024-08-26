package com.cl.common_base.pop

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.R
import com.cl.common_base.adapter.StrainNameSearchAdapter
import com.cl.common_base.constants.Constants
import com.cl.common_base.databinding.StrainNameBinding
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.setVisible
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.SoftInputUtils
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

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

    // 搜索时
    private val searching: MutableList<String> = mutableListOf(context.getString(R.string.string_240))

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

            strainName.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_NEXT || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    SoftInputUtils.hideSoftInput(context as Activity, strainName)
                    // 输入范围为1～24字节
                    if (getTextLength(strainName.text.toString()) < 1 || getTextLength(strainName.text.toString()) > 24) {
                        ToastUtil.shortShow(context.getString(R.string.strain_name_desc))
                    } else {
                        onConfirmAction?.invoke(strainName.text.toString())
                    }
                }
                false
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
                XPopup.Builder(context).isDestroyOnDismiss(false).isDestroyOnDismiss(false)
                    .asCustom(
                        BaseCenterPop(
                            context,
                            content = context.getString(R.string.seed_strain_name),
                            isShowCancelButton = false
                        )
                    ).show()
            }

            clNotKnow.setOnClickListener {
                // 跳过的话，默认名字
                onConfirmAction?.invoke(context.getString(R.string.string_241))
            }

            btnSuccess.setOnClickListener {
                // 输入范围为1～24字节
                if (getTextLength(strainName.text.toString()) < 1 || getTextLength(strainName.text.toString()) > 24) {
                    ToastUtil.shortShow(context.getString(R.string.strain_name_desc))
                    return@setOnClickListener
                }
                onConfirmAction?.invoke(strainName.text.toString())
            }

            // 搜索列表
            rvSearch.layoutManager = LinearLayoutManager(context)
            rvSearch.adapter = searchAdapter

            searchAdapter.setOnItemClickListener { adapter, view, position ->
                if (adapter.data[position].toString() == searching[0]) return@setOnItemClickListener
                binding?.strainName?.setText(adapter.data[position].toString())
                binding?.strainName?.setSelection(binding?.strainName?.text?.length ?: 0)
                binding?.rvSearch?.setVisible(false)
                binding?.clStrain?.setVisible(true)
                SoftInputUtils.hideSoftInput(context as Activity, binding?.strainName)
            }
        }
    }

    private val searchAdapter by lazy {
        StrainNameSearchAdapter(mutableListOf())
    }

    // 构建输入框文字变化流
    private fun EditText.textChangeFlow(): Flow<Editable> = callbackFlow {
        // 构建输入框监听器
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // 在文本变化后向流发射数据
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding?.rvSearch?.setVisible(!s.isNullOrEmpty())
                binding?.clStrain?.setVisible(s.isNullOrEmpty())
                if (!s.isNullOrEmpty()) searchAdapter.setList(searching)
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
        service.getStrainName(txt).map {
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
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            logI(it.toString())
            it.data?.let { it1 -> mutableList.addAll(it1) }
        }
        return mutableList
    }

    // 更新界面
    private fun updateUi(it: List<String>) {
        searchAdapter.setList(it)
//        if (it.size > 1) {
//            binding?.nes?.post {
//                //滚到底部
//                binding?.nes?.fullScroll(View.FOCUS_DOWN)
//            }
//        }
//        SoftInputUtils.showSoftInput(context as Activity, binding?.strainName)
//        binding?.strainName?.isFocusable = true
//        binding?.strainName?.isFocusableInTouchMode = true
//        binding?.strainName?.requestFocus()
//        binding?.strainName?.setSelection(binding?.strainName?.text?.length ?: 0)
    }

    // 访问网络进行搜索
    private suspend fun search(key: String): List<String> {
        return getStrainNameList(key)
    }

    // 将搜索关键词转换成搜索结果流
    private fun searchFlow(key: String) = flow { emit(search(key)) }

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