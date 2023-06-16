package com.cl.common_base.util;

import android.text.TextUtils;

/**
 * 判断是否是邮箱
 */
public class EmailUtil {
    public static boolean isEmail(String email) {
        String emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
        if (TextUtils.isEmpty(email)) {
            return false;
        } else {
            return email.matches(emailRegex);
        }
    }
}