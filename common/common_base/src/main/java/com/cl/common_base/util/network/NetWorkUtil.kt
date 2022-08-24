package com.cl.common_base.util.network

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.cl.common_base.ext.logI
import java.io.IOException
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.SocketException
import java.net.URL


/**
 * Created by chenxz on 2018/4/21.
 * 检查网络是否可用
 */
class NetWorkUtil {
    companion object {

        var NET_CNNT_BAIDU_OK = 1 // NetworkAvailable
        var NET_CNNT_BAIDU_TIMEOUT = 2 // no NetworkAvailable
        var NET_NOT_PREPARE = 3 // Net no ready
        var NET_ERROR = 4 //net error
        private val TIMEOUT = 3000 // TIMEOUT

        /**
         * check NetworkAvailable
         *
         * @param context
         * @return
         */
        @JvmStatic
        fun isNetworkAvailable(context: Context): Boolean {
            val manager = context.applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager
            val info = manager.activeNetworkInfo
            return !(null == info || !info.isAvailable)
        }

        /**
         * check NetworkConnected
         *
         * @param context
         * @return
         */
        fun isNetworkConnected(context: Context): Boolean {
            val manager =
                context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = manager.activeNetworkInfo
            return !(null == info || !info.isConnected)
        }

        /**
         * 得到ip地址
         *
         * @return
         */
        @JvmStatic
        fun getLocalIpAddress(): String {
            var ret = ""
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val enumIpAddress = en.nextElement().inetAddresses
                    while (enumIpAddress.hasMoreElements()) {
                        val netAddress = enumIpAddress.nextElement()
                        if (!netAddress.isLoopbackAddress) {
                            ret = netAddress.hostAddress.toString()
                        }
                    }
                }
            } catch (ex: SocketException) {
                ex.printStackTrace()
            }

            return ret
        }


        /**
         * ping "http://www.baidu.com"
         *
         * @return
         */
        @JvmStatic
        private fun pingNetWork(): Boolean {
            var result = false
            var httpUrl: HttpURLConnection? = null
            try {
                httpUrl = URL("http://www.baidu.com")
                    .openConnection() as HttpURLConnection
                httpUrl.connectTimeout = TIMEOUT
                httpUrl.connect()
                result = true
            } catch (e: IOException) {
            } finally {
                if (null != httpUrl) {
                    httpUrl.disconnect()
                }
            }
            return result
        }

        /**
         * check is3G
         *
         * @param context
         * @return boolean
         */
        @JvmStatic
        fun is3G(context: Context): Boolean {
            val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetInfo = connectivityManager.activeNetworkInfo
            return activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_MOBILE
        }

        /**
         * isWifi
         *
         * @param context
         * @return boolean
         */
        @JvmStatic
        fun isWifi(context: Context): Boolean {
            val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetInfo = connectivityManager.activeNetworkInfo
            return activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI
        }

        /**
         * is2G
         *
         * @param context
         * @return boolean
         */
        @RequiresApi(Build.VERSION_CODES.CUPCAKE)
        @JvmStatic
        fun is2G(context: Context): Boolean {
            val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetInfo = connectivityManager.activeNetworkInfo
            return activeNetInfo != null && (activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EDGE
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_GPRS || activeNetInfo
                .subtype == TelephonyManager.NETWORK_TYPE_CDMA)
        }


        /**
         * 判断MOBILE网络是否可用
         */
        fun isMobile(context: Context?): Boolean {
            if (context != null) {
                //获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
                val manager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                //获取NetworkInfo对象
                val networkInfo = manager.activeNetworkInfo
                //判断NetworkInfo对象是否为空 并且类型是否为MOBILE
                if (null != networkInfo && networkInfo.type == ConnectivityManager.TYPE_MOBILE)
                    return networkInfo.isAvailable
            }
            return false
        }

        /**
         * 获取wifi名字
         */
        fun getConnectWifiSsid(context: Context): String {
            val wifiManager =
                context.getSystemService(WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            logI("wifiInfo==$wifiInfo")
            logI("SSID===${wifiInfo.ssid}")
            return wifiInfo.ssid.replace("\"", "")
        }

        /**
         * 获取wifi名字第一种方案
         */
        fun oneWifi(context: Context): String? {
            val wifiManager =
                (context.getSystemService(WIFI_SERVICE) as WifiManager)
            val wifiInfo = wifiManager.connectionInfo
            val SSID = wifiInfo.ssid
            return SSID.replace("\"", "")
        }

        /**
         * 获取wifi名字到第二种方案
         */
        fun twoWifi(context: Context): String {
            val wifiManager =
                (context.getSystemService(WIFI_SERVICE) as WifiManager)
            val wifiInfo = wifiManager.connectionInfo
            var SSID = wifiInfo.ssid
            val networkId = wifiInfo.networkId
            val configuredNetworks = wifiManager.configuredNetworks
            for (wifiConfiguration in configuredNetworks) {
                if (wifiConfiguration.networkId == networkId) {
                    SSID = wifiConfiguration.SSID
                }
            }
            return SSID.replace("\"", "")
        }


    }
}