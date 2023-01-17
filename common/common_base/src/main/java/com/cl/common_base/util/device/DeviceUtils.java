package com.cl.common_base.util.device;

import static android.content.Context.INPUT_SERVICE;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.input.InputManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;


import java.lang.reflect.Method;

public final class DeviceUtils {

    private static final String AMAZON_FEATURE_FIRE_TV = "amazon.hardware.fire_tv";
    private static Boolean isTV = null;
    private static Boolean isFireTV = null;

    /*
     * Devices that do not support media tunneling
     */
    // Formuler Z8 Pro, Z8, CC, Z Alpha, Z+ Neo
    private static final boolean HI3798MV200 = Build.VERSION.SDK_INT == 24
            && Build.DEVICE.equals("Hi3798MV200");
    // Zephir TS43UHD-2
    private static final boolean CVT_MT5886_EU_1G = Build.VERSION.SDK_INT == 24
            && Build.DEVICE.equals("cvt_mt5886_eu_1g");
    // Hilife TV
    private static final boolean REALTEKATV = Build.VERSION.SDK_INT == 25
            && Build.DEVICE.equals("RealtekATV");
    // Philips QM16XE
    private static final boolean QM16XE_U = Build.VERSION.SDK_INT == 23
            && Build.DEVICE.equals("QM16XE_U");

    private DeviceUtils() {
    }



    public static boolean isConfirmKey(final int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                return true;
            default:
                return false;
        }
    }

    public static int dpToPx(@Dimension(unit = Dimension.DP) final int dp,
                             @NonNull final Context context) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics());
    }

    public static int spToPx(@Dimension(unit = Dimension.SP) final int sp,
                             @NonNull final Context context) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * Some devices have broken tunneled video playback but claim to support it.
     * See https://github.com/TeamNewPipe/NewPipe/issues/5911
     * @return false if affected device
     */
    public static boolean shouldSupportMediaTunneling() {
        return !HI3798MV200
                && !CVT_MT5886_EU_1G
                && !REALTEKATV
                && !QM16XE_U;
    }

    public static boolean isLandscape(final Context context) {
        return context.getResources().getDisplayMetrics().heightPixels < context.getResources()
                .getDisplayMetrics().widthPixels;
    }

    public static boolean isInMultiWindow(final AppCompatActivity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode();
    }

    public static boolean hasAnimationsAnimatorDurationEnabled(final Context context) {
        return Settings.System.getFloat(
                context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1F) != 0F;
    }

}
