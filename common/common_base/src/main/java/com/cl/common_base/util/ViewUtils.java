package com.cl.common_base.util;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;


/**
 * View工具类
 * <p>
 * Created by Vin on 2015/12/31.
 */
public class ViewUtils {

    /**
     * 设置View的GONE
     *
     * @param views
     * @param <V>
     * @return
     */
    public static <V extends View> void setGone(final V... views) {
        if (views != null) {
            for (View view : views) {
                if (View.GONE != view.getVisibility()) {
                    view.setVisibility(View.GONE);
                }
            }


        }
    }


    /**
     * 设置View的GONE
     *
     * @param view
     * @param <V>
     * @return
     */
    public static <V extends View> V setGone(final V view, boolean gone) {
        view.setVisibility(gone ? View.GONE : View.VISIBLE);
        return view;
    }


    /**
     * 设置View的INVISIBLE
     *
     * @param views
     * @param <V>
     * @return
     */
    public static <V extends View> void setInvisible(final V... views) {
        if (views != null) {
            for (View view : views) {
                if (View.INVISIBLE != view.getVisibility()) {
                    view.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    /**
     * 设置View的INVISIBLE
     *
     * @param view
     * @param <V>
     * @return
     */
    public static <V extends View> V setInvisible(final V view, boolean invisible) {
        if (view != null) {
            view.setVisibility(invisible ? View.INVISIBLE : View.VISIBLE);
        }
        return view;
    }

    /**
     * 设置View的VISIBLE
     *
     * @param views
     * @param <V>
     * @return
     */
    @SafeVarargs
    public static <V extends View> void setVisible(final V... views) {
        if (views != null) {
            for (View view : views) {
                if (view == null) {
                    continue;
                }
                if (View.VISIBLE != view.getVisibility()) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    /**
     * 设置view是否可见
     *
     * @param visible
     * @param views
     * @param <V>
     */
    public static <V extends View> void setVisible(boolean visible, final V... views) {
        if (views != null) {
            for (View view : views) {
                if (view != null) {
                    view.setVisibility(visible ? View.VISIBLE : View.GONE);
                }
            }
        }
    }


    /**
     * 设置view可用
     *
     * @param enable
     * @param views
     * @param <V>
     */
    public static <V extends View> void setEnable(boolean enable, final V... views) {
        if (views != null) {
            for (View view : views) {
                if (view == null) {
                    continue;
                }
                view.setEnabled(enable);
            }
        }
    }


    /**
     * 设置焦点
     *
     * @param view
     */
    public static void getFocus(View view) {
        if (view != null) {
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.requestFocus();
        }
    }

    /**
     * 输入框错误提示
     *
     * @param view
     * @param error
     */
    public static void setError(TextView view, CharSequence error) {
        setError(view, error, 0);
    }


    /**
     * 输入框错误提示
     *
     * @param view
     * @param error
     * @param indicator_input_error 修改默认错误icon
     */
    public static void setError(TextView view, CharSequence error, int indicator_input_error) {
        if (error == null) {
            view.setError(null, null);
        } else {
            if (indicator_input_error == 0) {
                view.setError(error);
            } else {
                Drawable dr = view.getContext().getResources().getDrawable(indicator_input_error);
                dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                view.setError(error, dr);
            }
        }
    }


    public static void removeOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        if (Build.VERSION.SDK_INT < 16) {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
        } else {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    /**
     * 禁止EditText输入空格和换行符
     *
     * @param editText EditText输入框
     */
    public static void setEditTextInputSpace(EditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.equals(" ") || source.toString().contentEquals("\n")) {
                    return "";
                } else {
                    return null;
                }
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }
}
