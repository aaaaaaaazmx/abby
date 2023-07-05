package com.cl.common_base.util.lcoation;

import android.content.Context;
import android.location.LocationManager;

import androidx.core.location.LocationManagerCompat;

public class LocationUtil {

    /**
     * 判断GPS是否正常
     *
     * @param context
     * @return
     */
    public static boolean isGpsEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    /**
     * 判断定位服务是否开启
     *
     * @param
     * @return true 表示开启
     */
    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }
}