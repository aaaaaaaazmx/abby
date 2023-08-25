package com.cl.modules_planting_log.widget

import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cl.common_base.util.SoftInputUtils
import com.cl.common_base.util.ViewUtils
import com.cl.modules_planting_log.R
import com.cl.modules_planting_log.adapter.EditTextValueChangeListener
import com.cl.modules_planting_log.adapter.LogTypeChooseAdapter
import com.cl.modules_planting_log.adapter.ShowOrHideTypeChangListener
import com.cl.modules_planting_log.request.LogTypeListDataItem

/**
 * 自定义横屏布局
 */
class CustomViewGroup : LinearLayout {
    private var textView1: TextView? = null
    private var editText1: EditText? = null
    private var textView2: TextView? = null
    private var rvChoose: RecyclerView? = null
    private var rlRoot: ConstraintLayout? = null
    private var llRoot: LinearLayout? = null
    var listener: EditTextValueChangeListener? = null
    var showOrHideListener: ShowOrHideTypeChangListener? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        inflate(context, R.layout.planting_custom_layout, this)
        textView1 = findViewById(R.id.textView1)
        editText1 = findViewById(R.id.editText1)
        textView2 = findViewById(R.id.textView2)
        rvChoose = findViewById(R.id.rv_choose)
        rlRoot = findViewById(R.id.rl_roots)
        llRoot = findViewById(R.id.ll_root)
        if (attrs != null) {
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomViewGroup)
            val textView1Text = a.getString(R.styleable.CustomViewGroup_textView1Text)
            val editText1Text = a.getString(R.styleable.CustomViewGroup_editText1Text)
            val textView2Text = a.getString(R.styleable.CustomViewGroup_textView2Text)
            val editText1HintText = a.getString(R.styleable.CustomViewGroup_editText1HintText)
            val textView2Visibility = a.getBoolean(R.styleable.CustomViewGroup_textView2Visibility, true)
            a.recycle()
            initRv()
            setEditeTextViewListener()
            setTextView1Text(textView1Text)
            setEditText1HintText(editText1HintText)
            setEditText1Text(tag, editText1Text)
            setTextView2Text(textView2Text)
            setTextView2Visibility(textView2Visibility)
        }
    }

    private val chooserAdapter by lazy {
        LogTypeChooseAdapter(mutableListOf())
    }

    private fun initRv() {
        rvChoose?.apply {
            layoutManager = GridLayoutManager(
                context,
                2, GridLayoutManager.VERTICAL, false
            )
            adapter = chooserAdapter

            chooserAdapter.addChildClickViewIds(R.id.check_period_chooser)
            chooserAdapter.setOnItemChildClickListener { adapter, view, position ->
                if (view.id != R.id.check_period_chooser) return@setOnItemChildClickListener

                val data = adapter.data as? MutableList<LogTypeListDataItem> ?: return@setOnItemChildClickListener
                val previousSelectedIndex = data.indexOfFirst { it.isSelected }
                var beforeShowUiText: String? = null

                // 如果找到上一个选中的项，则取消选中
                if (previousSelectedIndex != -1 && previousSelectedIndex != position) {
                    beforeShowUiText = data[previousSelectedIndex].showUiText
                    data[previousSelectedIndex].isSelected = false
                    adapter.notifyItemChanged(previousSelectedIndex)
                }

                // 选中当前项
                data[position].isSelected = true
                adapter.notifyItemChanged(position)

                // 显示文案
                editText1?.setText(data[position].showUiText)

                // 选中后隐藏
                visibility = View.GONE

                // 提供出接口，暴露相对应的visible的布局
                // 爆出showUiText
                // 选择相对应的logType之后，需要显示相对应的type，同时也需要隐藏相对应的type
                showOrHideListener?.showOrHide(previousSelectedIndex, position, beforeShowUiText, data[position].showUiText, this@CustomViewGroup)
            }

        }
    }

    fun setRvListData(logTypeList: MutableList<LogTypeListDataItem>, isShow: Boolean = false) {
        ViewUtils.setVisible(isShow, rvChoose)
        chooserAdapter.setList(logTypeList)
    }

    fun getRvListData(): Boolean {
        ViewUtils.setVisible(chooserAdapter.data.isNotEmpty(), rvChoose)
        return chooserAdapter.data.isNotEmpty()
    }

    /**
     * 设置EditText的点击事件
     * noKeyboard 键盘是否有焦点，是否可响应，true 不可点击，false 可点击
     */
    private var noKeyboard: Boolean = false
    fun setNoKeyboard(noKeyboard: Boolean) {
        this.noKeyboard = noKeyboard
        // 防止数据错乱
        if (noKeyboard) {
            editText1?.isFocusable = false
            editText1?.isFocusableInTouchMode = false
            editText1?.isClickable = true
        } else {
            editText1?.isFocusable = true
            editText1?.isFocusableInTouchMode = true
            editText1?.isClickable = true
        }
    }

    private fun setEditeTextViewListener() {
        editText1?.gravity = (Gravity.RIGHT or Gravity.CENTER_VERTICAL)
        rlRoot?.setOnClickListener {
            // 将光标设置到文本的末尾
            editText1?.setSelection(editText1?.text.toString().length)

            // 使EditText获取焦点
            editText1?.requestFocus()

            // 区分是可点击的，还是不可点击的。
            if (noKeyboard) {
                listener?.onEditTextClick(tag as Int, editText1!!, this)
                return@setOnClickListener
            }

            // 显示软键盘
            SoftInputUtils.showSoftInput(context, editText1)
        }
        editText1?.setOnClickListener {
            if (noKeyboard) {
                listener?.onEditTextClick(tag as Int, editText1!!, this)
                return@setOnClickListener
            }
        }
        editText1?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { // 检查是否已经有一位小数点，如果没有则添加
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                listener?.onValueChanged(tag as Int, editText1?.text.toString())
            }
        })

        editText1?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { // 当EditText失去焦点时
                if (editText1?.inputType == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL) {
                    val value = editText1?.text.toString()
                    val floatValue = value.toDoubleOrNull()
                    // 检查是否整数，如果是，则添加".0"
                    if (floatValue != null && !value.contains(".") && floatValue == floatValue.toInt().toDouble()) {
                        val formattedValue = "$value.0"
                        editText1?.setText(formattedValue)
                    }
                }
            }
        }

        editText1?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (editText1?.inputType == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL) {
                    val value = editText1?.text.toString()
                    if (value.isNotEmpty() && !value.contains(".")) {
                        // 如果值是整数并且不包含小数点，则添加".0"
                        val formattedValue = "$value.0"
                        editText1?.setText(formattedValue)
                    }
                }
            }
        }
    }

    fun setTextView1Text(text: String?) {
        textView1!!.text = text
    }

    fun setEditText1Text(tags: Any, text: String?) {
        if (tags == tag) {
                if (editText1?.inputType == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL) {
                    editText1?.setText(text?.toDoubleOrNull()?.toString() ?: "")
                } else {
                    editText1?.setText(text)
                }
        }

    }

    fun setEditText1HintText(text: String?) {
        editText1!!.hint = text
    }

    fun setTextView2Text(text: String?) {
        textView2!!.text = text
    }

    fun setTextView2Visibility(isVisible: Boolean) {
        textView2!!.visibility = if (isVisible) VISIBLE else GONE
    }

    fun setRootVisible(isVisible: Boolean) {
        llRoot!!.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun setInputType(type: String) {
        when (type) {
            TYPE_CLASS_TEXT -> editText1?.inputType = InputType.TYPE_CLASS_TEXT

            TYPE_NUMBER_FLAG_DECIMAL -> {
                editText1?.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                // editText1?.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(1))
                editText1?.filters = arrayOf<InputFilter>(InputFilter { source, start, end, dest, dstart, dend ->
                    if (source.isNotEmpty() && ".".contentEquals(source) && dest.toString().contains(".")) {
                        // 不允许输入多个小数点
                        return@InputFilter ""
                    }
                    if (source == "." && dstart == 0) {
                        // 不允许以小数点开头
                        return@InputFilter "0."
                    }
                    if (dest.toString().contains(".")) {
                        val dotIndex = dest.indexOf(".")
                        if (dend > dotIndex && dest.substring(dotIndex + 1).isNotEmpty()) {
                            // 不允许输入超过一位小数
                            return@InputFilter ""
                        }
                    }
                    null
                })
            }
        }
    }

    companion object {
        // 文字类型
        const val TYPE_CLASS_TEXT = "TYPE_CLASS_TEXT"

        // 数字类型
        const val TYPE_NUMBER_FLAG_DECIMAL = "TYPE_NUMBER_FLAG_DECIMAL"
    }
}
