package com.cl.common_base.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class AbTextViewCalendar extends androidx.appcompat.widget.AppCompatTextView {


    public AbTextViewCalendar(Context context) {
        super(context);
    }

    public AbTextViewCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbTextViewCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setTypeface(@Nullable Typeface tf) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Gilroy-Medium.ttf");
        super.setTypeface(tf);
    }

}