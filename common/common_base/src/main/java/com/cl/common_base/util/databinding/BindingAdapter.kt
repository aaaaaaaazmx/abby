package com.cl.common_base.util.databinding

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.cl.common_base.R
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.safeToInt

/**
 * BindingAdapter extensions.
 */

@BindingAdapter("selected")
fun setSelected(view: View, selected: Boolean) {
    view.isSelected = selected
}

@BindingAdapter("adapter")
fun setAdapter(viewPager: ViewPager, adapter: PagerAdapter?) {
    viewPager.adapter = adapter
}

@BindingAdapter("backgroundDrawableId")
fun setBackgroundDrawableId(view: View, drawableId: Int?) {
    if (null != drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.background = ContextCompat.getDrawable(view.context, drawableId)
        }
    }
}

/**
 * 通过dataBinding的方式绑定RecyclerView的adapter
 *
 * @param recyclerView 目标[RecyclerView]
 * @param adapter [RecyclerView.Adapter]
 * @param isVertical Recycleview是否是垂直布局还是横向布局
 */
@BindingAdapter("adapter")
fun setAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
    recyclerView.adapter = adapter
}

/**
 * 通过dataBinding的方式绑定[RecyclerView]的[RecyclerView.LayoutManager]
 *
 * @param recyclerView 目标[RecyclerView]
 * @param layoutManager [RecyclerView.LayoutManager]
 */
@BindingAdapter("layoutManager")
fun bindLayoutManager(recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager?) {
    recyclerView.layoutManager = layoutManager
}

/**
 * 设置View是否选中
 */
@BindingAdapter("isSelected")
fun setTextViewSelected(view: View, isSelected: Boolean) {
    view.isSelected = isSelected
}

/**
 * 设置图片
 * @param url 图片地址
 */
@BindingAdapter(value = ["urlNoPlaceholder"], requireAll = false)
fun setImageUrlNoPlaceholder(imageView: ImageView, url: String?) {
    val requestOptions = RequestOptions().apply {
        //        error(R.drawable.bitmap_occupation_tview)
    }
    Glide.with(imageView.context).load(url)
        .apply(requestOptions)
        .into(imageView)
}

/**
 * 设置图片
 * @param url 图片地址
 */
@BindingAdapter(value = ["url"], requireAll = false)
fun setImageUrl(imageView: ImageView, url: String?) {
    url?.let {
        var requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.placeholder)
        requestOptions.error(R.mipmap.errorholder)
        // requestOptions.override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL, SIZE_ORIGINAL)
        Glide.with(imageView.context).load(url)
            .apply(requestOptions)
            .into(imageView)
    }
}

@SuppressLint("CheckResult")
@BindingAdapter(value = ["logUrl"], requireAll = false)
fun setLogUrl(imageView: ImageView, url: String?) {
    url?.let {
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.placeholder)
        requestOptions.error(R.mipmap.errorholder)
        Glide.with(imageView.context).load(url)
            .apply(requestOptions)
            .centerCrop()
            .into(imageView)
    }
}

@BindingAdapter(value = ["plantPhotoUrl"], requireAll = false)
fun setPlantPhotoUrl(imageView: ImageView, url: String?) {
    val requestOptions = RequestOptions()
    requestOptions.placeholder(R.mipmap.placeholder)
    requestOptions.error(R.mipmap.errorholder)
    if (url.isNullOrEmpty()) {
        Glide.with(imageView.context).load(
            ContextCompat.getDrawable(
                imageView.context, R.mipmap.contact_add_pic
            )
        ).apply(requestOptions).centerCrop().into(imageView)
    } else {
        runCatching {
            val urlArray = url.split("--------")
            if (urlArray.isNotEmpty() && urlArray.size == 1) {
                Glide.with(imageView.context).load(urlArray[0]).apply(requestOptions).centerCrop().into(imageView)
            } else {
                Glide.with(imageView.context).load(urlArray[0]).apply(requestOptions).centerCrop().into(imageView)
            }
        }.onFailure {
            Glide.with(imageView.context).load(url).apply(requestOptions).centerCrop().into(imageView)
        }
    }
}

/**
 * 设置图片
 * @param resID 资源ID
 */
@BindingAdapter(value = ["url"], requireAll = false)
fun setImageUrl(imageView: ImageView, resID: Int?) {
    var requestOptions = RequestOptions()
    //    requestOptions.placeholder(R.drawable.bitmap_occupation_tview)
    //    requestOptions.error(R.drawable.bitmap_occupation_tview)
    Glide.with(imageView.context).load(resID)
        .apply(requestOptions)
        .into(imageView)
}

// 设置圆形图片
@BindingAdapter("circleUrl")
fun setCircleImageUrl(imageView: ImageView, src: String?) {
    var requestOptions = RequestOptions()
    //    requestOptions.placeholder(R.drawable.bitmap_occupation_tview)
    //    requestOptions.error(R.drawable.bitmap_occupation_tview)
    Glide.with(imageView.context).load(src)
        .apply(RequestOptions.circleCropTransform())
        .into(imageView)
}

// 设置圆角图片
@BindingAdapter("circleCornerUrl")
fun setCircleCornerImageUrl(imageView: ImageView, src: String?) {
    Glide.with(imageView.context).load(src).apply(RequestOptions.bitmapTransform(RoundedCorners(100))).into(imageView);//四周都是圆角的圆角矩形图片。
}


/**
 * 添加删除线
 */
@BindingAdapter("strikeThruText")
fun setStrikeThruText(tv: TextView, enable: Boolean) {
    if (enable) {
        tv.paintFlags = tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        tv.paintFlags = tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}

/**
 * 设置单次点击事件，避免快速重复点击误操作。检测时间间隔为400ms，400ms以内的点击将被视为无效点击。
 *
 * @param view 目标视图
 * @param onClickListener 点击回调
 */
/*@BindingAdapter("onSingleClick")
fun setOnSingleClick(view: View, onClickListener: View.OnClickListener) {
    view.setOnSingleClickListener {
        onClickListener.onClick(it)
    }
}*/

/**
 * 设置视图的可见性，如果设置不可见，将完全从屏幕中消失。
 *
 * @param view 目标视图
 * @param visible true 可见 false 不可见
 */
@BindingAdapter("visible")
fun setVisible(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("layout_marginTop")
fun setTopMargin(view: View, topMargin: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(
        layoutParams.leftMargin, topMargin.safeToInt(),
        layoutParams.rightMargin, layoutParams.bottomMargin
    )
    view.layoutParams = layoutParams
}

@BindingAdapter("textSize")
fun setTextSize(view: TextView, sp: Int) {
    view.textSize = sp.toFloat()
}

@BindingAdapter("isBold")
fun setBold(view: TextView, isBold: Boolean) {
    if (isBold) {
        view.setTypeface(null, Typeface.BOLD)
    } else {
        view.setTypeface(null, Typeface.NORMAL)
    }
}

@BindingAdapter("isEnable")
fun setIsEnable(et: EditText, enable: Boolean) {
    et.isFocusableInTouchMode = enable
    et.isFocusable = enable

    if (enable) {
        et.requestFocus()
    }
}


@BindingAdapter("gravityText")
fun setGravityText(view: TextView, gravity: String) {
    val layoutParams = view.layoutParams as? LinearLayout.LayoutParams
    layoutParams?.gravity = when (gravity) {
        "left" -> Gravity.START
        "right" -> Gravity.END
        "center" -> Gravity.CENTER
        else -> Gravity.START
    }
}

@BindingAdapter("colorText")
fun setColorText(textView: TextView, color: String) {
    textView.setTextColor(Color.parseColor(color))
}

@BindingAdapter("typeInput")
fun setTypeInput(editText: EditText, type: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
        when (type) {
            "Number" ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER

            "String" -> {
                editText.inputType = InputType.TYPE_CLASS_TEXT
            }
        }
    }
}

@BindingAdapter("sizeText")
fun setSizeText(textView: TextView, size: String) {
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
}