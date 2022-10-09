package com.cl.common_base.constants

/**
 *  常量管理
 */
object Constants {
    const val DB_INSTANCE = "DB_INSTANCE"
    const val TOKEN = "token"
    const val USER_ID = "userid"
    const val USER_NAME = "username"
    const val PASSWORD = "password"
    const val APP_TAG = "Abby"

    // 网络常量
    const val APP_SUCCESS = 200

    // H5相关
    object H5 {
        private const val WEB_URL_MAIN = HttpUrl.WEB_PRODUCTION_URL
        const val PRIVACY_POLICY_URL = "${WEB_URL_MAIN}/protocol/privacyPolicy.html"
        const val PERSONAL_URL = "${WEB_URL_MAIN}/protocol/licenseAgreement.html"

        // APP官网
        const val ABBY_OFFICIAL_WEBSITE = "https://heyabby.com/"
    }

    // 服务器地址
    object HttpUrl {
        // WebView 生产服务器
        const val WEB_PRODUCTION_URL = "https://www.beheyabby.com:9998/"

        // 测试环境
        const val TEST_URL = "http://192.168.3.6:9997/"

        // 本地环境
        const val BD_URL = "http://192.168.3.101:9330/"

        // 正式环境
        const val FORMAL_URL = "https://www.beheyabby.com:9330/"

        // 开发
        const val DEVELOPMENT_URL = "http://192.168.3.101:9330/"

        // 外网
        const val OUTER_ANG_URL = "https://5c8730c912.oicp.vip/"
    }

    // Login信息相关
    object Login {
        // UserinfoBean
        const val KEY_LOGIN_DATA = "key_login_data"

        // Token
        const val KEY_LOGIN_DATA_TOKEN = "key_login_data_token"

        // 账号
        const val KEY_LOGIN_ACCOUNT = "key_login_account"

        // 密码 加密后的
        const val KEY_LOGIN_PSD = "key_login_psd"
    }

    // 隐私合规相关
    object PrivacyPolicy {
        const val KEY_PRIVACY_POLICY_IS_AGREE = "key_privacy_policy_is_agree"
    }

    // 配对都wifi名字和wifi密码
    object Pair {
        const val KEY_PAIR_WIFI_NAME = "key_pair_wifi_name"
        const val KEY_PAIR_WIFI_PASSWORD = "key_pair_wifi_password"
    }

    // 涂鸦相关
    object Tuya {
        // 用户信息
        const val KEY_DEVICE_USER = "key_deviceUser"

        // 用户的家庭ID
        const val KEY_HOME_ID = "key_home_id"

        // 用户的第0个设备数据 HomeBean[0].getDeviceList() =  DeviceBean
        const val KEY_DEVICE_DATA = "key_device_data"

        // 设备端给APP端下发的信息
        const val KEY_TUYA_DEVICE_TO_APP = "key_tuya_device_to_app"
    }

    // 极光
    object Jpush {
        // 设备内消息
        const val KEY_IN_APP_MESSAGE = "key_in_app_message"
    }

    /**
     * 蓝牙监听相关
     */
    object Ble {
        const val KEY_BLE_STATE = "key_ble_state"
        const val KEY_BLE_OFF = "key_ble_off"
        const val KEY_BLE_ON = "key_ble_on"
    }

    /**
     * 设备状态监听
     */
    object Device {
        // 离线
        const val KEY_DEVICE_OFFLINE = "key_device_offline"

        // 在线
        const val KEY_DEVICE_ONLINE = "key_device_online"

        // 移除设备
        const val KEY_DEVICE_REMOVE = "key_device_remove"

        // 设备端给APP端下发的信息
        const val KEY_DEVICE_TO_APP = "key_device_to_app"
    }


    /**
     * 全局相关
     */
    object Global {
        // 引导状态
        const val KEY_GLOBAL_PLANT_GUIDE_FLAG = "key_global_plant_guide_flag"

        // 当前种植窗台
        const val KEY_GLOBAL_PLANT_PLANT_STATE = "key_global_plant_plant_state"

        // 当前是否是离线状态
        const val KEY_GLOBAL_PLANT_DEVICE_IS_OFF_LINE = "key_global_plant_device_is_off_line"

        // 固件升级是否是强制升级
        const val KEY_GLOBAL_MANDATORY_UPGRADE = "key_mandatory_upgrade"

        // 是否选择了继承
        const val KEY_IS_CHOOSE_CLONE = "key_is_choose_clone"

        // 是否选择了Seed
        const val KEY_IS_CHOOSE_SEED = "key_is_choose_seed"

        // 跳转选择界面师傅哦直接弹窗
        const val KEY_USER_NO_STRAIN_NAME = "key_user_no_strain_name"
        const val KEY_USER_NO_ATTRIBUTE = "key_user_no_attribute"
        const val KEY_REFRESH_PLANT_INFO = "key_refresh_plant_info"

        // 植物ID
        const val KEY_PLANT_ID = "key_plant_id"
    }

    /**
     * url key
     */
    const val CONTENT_URL_KEY = "url"

    /**
     * title key
     */
    const val CONTENT_TITLE_KEY = "title"

    /**
     * id key
     */
    const val CONTENT_ID_KEY = "id"

    /**
     * id key
     */
    const val CONTENT_CID_KEY = "cid"

    /**
     * share key
     */
    const val CONTENT_SHARE_TYPE = "text/plain"

    const val POSITION = "position"

    const val COLLECT = "isCollect"

    const val ROUTER_PATH = "routerPath"


    object CollectType {
        const val COLLECT = "COLLECT"
        const val UNCOLLECT = "UNCOLLECT"
        const val UNKNOWN = "UNKNOWN"
    }

    object FragmentIndex {
        const val HOME_INDEX = 0
        const val CONTACT_INDEX = 1
        const val MY_INDEX = 2
    }

    // 视频后缀
    val videoList = mutableListOf(
        "FLV",
        "MOV",
        "MP4",
        "WMV",
        "AVI",
        "flv",
        "mov",
        "mp4",
        "wmv",
        "avi"
    )
}