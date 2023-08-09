package com.cl.modules_planting_log.widget

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.cl.modules_planting_log.R

/**
 * 自定义横屏布局
 */
class CustomViewGroup : LinearLayout {
    private var textView1: TextView? = null
    private var editText1: EditText? = null
    private var textView2: TextView? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        inflate(context, R.layout.planting_custom_layout, this)
        textView1 = findViewById<TextView>(R.id.textView1)
        editText1 = findViewById<EditText>(R.id.editText1)
        textView2 = findViewById<TextView>(R.id.textView2)
        if (attrs != null) {
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomViewGroup)
            val textView1Text = a.getString(R.styleable.CustomViewGroup_textView1Text)
            val editText1Text = a.getString(R.styleable.CustomViewGroup_editText1Text)
            val textView2Text = a.getString(R.styleable.CustomViewGroup_textView2Text)
            val editText1HintText = a.getString(R.styleable.CustomViewGroup_editText1HintText)
            val textView2Visibility = a.getBoolean(R.styleable.CustomViewGroup_textView2Visibility, true)
            a.recycle()
            setTextView1Text(textView1Text)
            setEditText1HintText(editText1HintText)
            setEditText1Text(editText1Text)
            setTextView2Text(textView2Text)
            setTextView2Visibility(textView2Visibility)
        }
    }

    fun setTextView1Text(text: String?) {
        textView1!!.text = text
    }

    fun setEditText1Text(text: String?) {
        editText1!!.setText(text)
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
}
