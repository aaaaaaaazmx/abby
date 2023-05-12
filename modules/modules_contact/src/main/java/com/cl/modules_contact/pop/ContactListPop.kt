package com.cl.modules_contact.pop

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.dp2px
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.setVisible
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.util.SoftInputUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ContactListAdapter
import com.cl.modules_contact.databinding.ContactListPopBinding
import com.cl.modules_contact.request.CommentByMomentReq
import com.cl.modules_contact.request.RewardReq
import com.cl.modules_contact.response.MentionData
import com.cl.modules_contact.service.HttpContactApiService
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.KeyboardUtils
import com.lxj.xpopup.util.XPopupUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * Trend 选择@人联系人列表
 *
 * @param alreadyCheckedData 已经勾选的人集合
 * @param onConfirmAction 确认按钮点击事件
 */
class ContactListPop(
    context: Context,
    private val alreadyCheckedData: MutableList<MentionData> = mutableListOf(),
    private val onConfirmAction: ((list: MutableList<MentionData>) -> Unit)? = null
) : BottomPopupView(context) {
    private val service = ServiceCreators.create(HttpContactApiService::class.java)

    override fun getImplLayoutId(): Int {
        return R.layout.contact_list_pop
    }

    private val adapter by lazy {
        ContactListAdapter(mutableListOf())
    }

    private var binding: ContactListPopBinding? = null

    override fun onCreate() {
        super.onCreate()

        binding = DataBindingUtil.bind<ContactListPopBinding>(popupImplView)?.apply {
            // 设置数据
            executePendingBindings()

            rvContactList.layoutManager = LinearLayoutManager(context)
            rvContactList.adapter = this@ContactListPop.adapter

            /* tvCommentTxt.setOnEditorActionListener { v, actionId, event ->
                 if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_NEXT || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                     SoftInputUtils.hideSoftInput(context as Activity, tvCommentTxt)
                     // 输入范围为1～24字节
                     *//*if (getTextLength(strainName.text.toString()) < 1 || getTextLength(strainName.text.toString()) > 24) {
                        ToastUtil.shortShow(context.getString(com.cl.common_base.R.string.strain_name_desc))
                    } else {
                        onConfirmAction?.invoke(strainName.text.toString())
                    }*//*
                }
                false
            }*/

            tvCommentTxt.textChangeFlow() // 构建输入框文字变化流
                .debounce(300) // 300ms防抖
                .flatMapLatest { searchFlow(it.toString()) } // 新搜索覆盖旧搜索
                .flowOn(Dispatchers.IO) // 让搜索在异步线程中执行
                .onEach { updateUi(it) } // 获取搜索结果并更新界面
                .launchIn(lifecycleScope) // 在主线程收集搜索结果

            aasdasd("")
            initAdapter()


            btnSuccess.setOnClickListener {
                val list = this@ContactListPop.adapter.data.filter { data -> data.isSelect == true }
                onConfirmAction?.invoke(list.toMutableList())
                dismiss()
            }

            ivClose.setOnClickListener {
                dismiss()
            }
        }
    }

    //    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
    //        super.onWindowFocusChanged(hasWindowFocus)
    //        KeyboardUtils.hideSoftInput(binding?.tvCommentTxt)
    //    }

    private fun initAdapter() {
        adapter.addChildClickViewIds(R.id.cl_root)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.cl_root -> {
                    val item = adapter.getItem(position) as MentionData
                    // 添加的时候需要判断，取消的时候不需要判断
                    if (item.isSelect == false) {
                        if (((adapter.data as? MutableList<MentionData>)?.filter { it.isSelect == true }?.size ?: 0) >= 9) {
                            ToastUtil.shortShow("can’t mention select more than 9 users")
                            return@setOnItemChildClickListener
                        }
                    }
                    item.isSelect = !(item.isSelect ?: false)
                    if (alreadyCheckedData.isNotEmpty() && alreadyCheckedData.size > position) {
                        alreadyCheckedData[position].isSelect = !(alreadyCheckedData[position].isSelect ?: false)
                    }
                    adapter.notifyItemChanged(position)
                }
            }
        }
    }

    // 更新界面
    private fun updateUi(it: List<MentionData>) {
        adapter.setList(filterSelectFriends(alreadyCheckedData, it.toMutableList()))
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
    private suspend fun search(key: String): List<MentionData> {
        return getMentionList(key)
    }

    // 将搜索关键词转换成搜索结果流
    private fun searchFlow(key: String) = flow { emit(search(key)) }

    // 构建输入框文字变化流
    private fun EditText.textChangeFlow(): Flow<Editable> = callbackFlow {
        // 构建输入框监听器
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                trySend(text)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // 在文本变化后向流发射数据
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        }
        addTextChangedListener(watcher) // 设置输入框监听器
        awaitClose { removeTextChangedListener(watcher) } // 阻塞以保证流一直运行
    }

    private suspend fun getMentionList(searchName: String): MutableList<MentionData> {
        val mutableList = mutableListOf<MentionData>()
        service.getMentionList(searchName).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            if (it is Resource.Success) {
                // 搜索成功
                mutableList.addAll(it.data ?: mutableListOf())
            }
        }
        return mutableList
    }

    private fun aasdasd(searchName: String) = lifecycleScope.launch {
        service.getMentionList(searchName).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            if (it is Resource.Success) {
                // 搜索成功
                adapter.setList(filterSelectFriends(alreadyCheckedData, it.data ?: mutableListOf()))
            }
        }
    }

    private fun filterSelectFriends(already: MutableList<MentionData>, data: MutableList<MentionData>): MutableList<MentionData> {
        if (already.isEmpty()) {
            return data
        }

        val diffList = already.filter { item1 ->
            data.any { item2 ->
                item1.isSelect != item2.isSelect
            }
        }

        diffList.forEach { item ->
            data.forEach { item2 ->
                if (item.userId == item2.userId) {
                    item2.isSelect = item.isSelect
                }
            }
        }
        return data
    }

    override fun getPopupHeight(): Int {
        return dp2px(700f)
    }
}