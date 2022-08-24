package com.cl.modules_pairing_connection.request

import androidx.annotation.Keep
import com.cl.common_base.BaseBean

/**
 * 蓝牙数据类
 * @author 李志军 2022-08-04 11:21
 */
@Keep
data class PairBleData(
    var name: String? = null,
    var subName: String? = null,
    var icon: String? = null,
    var bleData: MyScanDeviceBean? = null
) : BaseBean() {
    /**
     *   String id;
    String name;
    String providerName;
    String data;
    String configType;
    String productId;
    String uuid;
    String mac;
    String address;
    int deviceType;
    boolean isbind = false;
    int flag = 0;
     */
    data class MyScanDeviceBean(
        var id: String? = null,
        var name: String? = null,
        var providerName: String? = null,
        var data: String? = null,
        var configType: String? = null,
        var productId: String? = null,
        var uuid: String? = null,
        var mac: String? = null,
        var address: String? = null,
        var deviceType: Int? = null,
        var isbind: Boolean? = null,
        var flag: Int? = null,
    ) : BaseBean()
}