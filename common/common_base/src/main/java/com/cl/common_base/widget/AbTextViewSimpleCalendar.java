package com.cl.common_base.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class AbTextViewSimpleCalendar extends androidx.appcompat.widget.AppCompatTextView {


    public AbTextViewSimpleCalendar(Context context) {
        super(context);
    }

    public AbTextViewSimpleCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbTextViewSimpleCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setTypeface(@Nullable Typeface tf) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Gilroy-Semibold.ttf");
        super.setTypeface(tf);
    }

}