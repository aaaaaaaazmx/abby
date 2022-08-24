package com.cl.common_base.util;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.cl.common_base.R;

/**
 * 去掉下划线的点击span
 */
public class NoUnderlineClickSpan extends ClickableSpan {

    @Override
    public void onClick(View v) {

    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setUnderlineText(false);
    }
}