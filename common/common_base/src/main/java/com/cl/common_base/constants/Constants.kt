package com.cl.common_base.constants

import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON

/**
 *  常量管理
 */
object Constants {
    const val DB_INSTANCE = "DB_INSTANCE"
    const val TOKEN = "token"
    const val USER_ID = "userid"
    const val USER_NAME = "username"
    const val PASSWORD = "password"
    const val APP_TAG = "AbbyAbby"

    // 网络常量
    const val APP_SUCCESS = 200

    // 服务器错误
    const val APP_SERVER = 500

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
//        const val FORMAL_URL = "https://52.9.50.27:9330/"
        const val FORMAL_URL = "http://52.53.154.192:9330/"

        // 开发
        const val DEVELOPMENT_URL = "http://192.168.3.101:9330/"

        // 外网
        const val OUTER_ANG_URL = "https://2549m9264b.zicp.fun/"
    }

    // Login信息相关
    object Login {
        // UserinfoBean
        // 登录信息\用户信息
        const val KEY_LOGIN_DATA = "key_login_data"

        // Token
        const val KEY_LOGIN_DATA_TOKEN = "key_login_data_token"

        // 账号
        const val KEY_LOGIN_ACCOUNT = "key_login_account"

        // 密码 加密后的
        const val KEY_LOGIN_PSD = "key_login_psd"

        // 涂鸦相关信息
        const val KEY_TU_YA_INFO = "key_tuya_info"

        // 记录当天刷新的时间，在同一天之内是能刷新一次。
        const val KEY_REFRESH_TIME = "key_refresh_time"
    }

    object Plant {
        // plantId
        const val KEY_PLANT_ID = "key_plant_id"
    }

    // 我的界面相关
    object My {
        // 公英制
        // false 英制 inc、 true 公制 cm
        const val KEY_MY_WEIGHT_UNIT = "key_my_weight_unit"
    }

    object Contact {
        const val KEY_SHARE_TO_PUBLIC = "key_share_to_public"
        const val KEY_PLANT_DATA_IS_VISIBLE = "key_plant_data_is_visible"
        // timelapse提示是否只展示一次
        const val KEY_TIMELAPSE_TIP_IS_SHOW = "key_timelapse_tip_is_show"
        // 关注提醒是否只展示一次
        const val KEY_FOLLOW_TIP_IS_SHOW = "key_follow_tip_is_show"
    }

    // 隐私合规相关
    object PrivacyPolicy {
        const val KEY_PRIVACY_POLICY_IS_AGREE = "key_privacy_policy_is_agree"
    }

    // 配对都wifi名字和wifi密码
    object Pair {
        const val KEY_PAIR_WIFI_NAME = "key_pair_wifi_name"
        const val KEY_PAIR_WIFI_PASSWORD = "key_pair_wifi_password"

        // 不同的wifi设备类型
        const val KEY_PAIR_WIFI_DEVICE = "key_pair_wifi_device"
    }

    // 涂鸦相关
    object Tuya {
        // 用户信息 tuya用户信息类
        const val KEY_DEVICE_USER = "key_deviceUser"

        // 用户的家庭ID
        const val KEY_HOME_ID = "key_home_id"

        // 用户的第0个设备数据 HomeBean[0].getDeviceList() =  DeviceBean
        // 这个才是真的tuya相关类
        const val KEY_DEVICE_DATA = "key_device_data"

        // 设备端给APP端下发的信息
        const val KEY_THING_DEVICE_TO_APP = "key_tuya_device_to_app"
    }

    /**
     * InterCome
     */
    object InterCome {
        // 未读消息监听
        const val KEY_INTER_COME_UNREAD_MESSAGE = "key_inter_come_unread_message"

        // 氧气币文章
        const val KEY_INTER_COME_OXYGEN_COIN = "8192180"

        // 服务说明
        const val KEY_INTER_COME_SERVICE = "8192210"

        // 夜间模式说明
        const val KEY_INTER_COME_NIGHT_MODE = "8099171"

        // 童锁说明
        const val KEY_INTER_COME_CHILD_LOCK = "8192703"

        // 植物高度说明
        const val KEY_INTER_COME_PLANT_HEIGHT = "8192750"

        // 气泵说明
        const val KEY_INTER_COME_AIR_PUMP = "8193071"

        // 防烧模式说明
        const val KEY_INTER_COME_BURN_PROOF = "8153317"

        // 绑定摄像头相关问题说明
        const val KEY_INTER_COME_BIND_CAMERA = "8074254"

        // dirp文章
        const val KEY_INTER_COME_DRIP = "8407831"
    }

    // APP应用内消息
    object APP {
        const val KEY_IN_APP = "key_in_app"

        const val KEY_IN_APP_VIP = "key_in_app_vip"

        // 开始种植了。
        const val KEY_IN_APP_START_RUNNING = "key_in_app_start_running"
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
        // 蓝牙类型
        const val KEY_BLE_TYPE = "key_ble_type"
        const val TYPE_PH = "type_ph"
        // 蓝牙最大连接数量
        const val KEY_BLE_MAX_CONNECT = 2
        // 是否是第一次绑定
        const val KEY_BLE_IS_FIRST_BIND = "key_ble_is_first_bind"

        // 蓝牙笔的特征值，基于这个特征值的UUID来读数据和写数据
        const val KEY_BLE_PH_CHARACTERISTIC_UUID = "0000ff02"
        const val KEY_PH_DEVICE_NAME = "BLE-9908"
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

        // 土培设备
        const val KEY_DEVICE_TYPE_O1_SOIL = "O1_soil"
    }

    object DebugTest {
        const val KEY_TEST_URL = "key_test_url"
    }


    /**
     * 全局相关
     */
    object Global {
        // 配对设备Abby
        const val KEY_GLOBAL_PAIR_DEVICE_ABBY = "key_global_pair_device_abby"
        // 配对设备Camera
        const val KEY_GLOBAL_PAIR_DEVICE_CAMERA = "key_global_pair_device_camera"
        // 配对设备排插
        const val KEY_GLOBAL_PAIR_DEVICE_OUTLETS = "key_global_pair_device_outlets"
        // 不带显示器的温湿度传感器
        const val KEY_GLOBAL_PAIR_DEVICE_BOX = "key_global_pair_device_box"
        // 带显示器的温湿度传感器
        const val KEY_GLOBAL_PAIR_DEVICE_VIEW = "key_global_pair_device_view"

        // pro_model 配置
        const val KEY_GLOBAL_PRO_MODEL = "key_global_pro_model"
        // 是否Load了配置
        const val KEY_LOAD_CONFIGURED = "key_load_configured"
        // 灯光的预设值
        const val KEY_LIGHT_PRESET_VALUE = "key_light_preset_value"

        // 是否是第一次注册登录、并且是从未绑定过设备
        const val KEY_GLOBAL_PLANT_FIRST_LOGIN_AND_NO_DEVICE =
            "key_global_plant_first_login_and_no_device"

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

        // 是否展示肥料掉落弹窗
        const val KEY_IS_SHOW_FEET_POP = "key_is_show_feet_pop"

        // 是否在fan关闭时提示用户，
        const val KEY_IS_SHOW_FAN_CLOSE_TIP = "key_is_show_fan_close_tip"

        // 是否在fan调整到7之后提示用户
        const val KEY_IS_SHOW_FAN_SEVEN_TIP = "key_is_show_fan_seven_tip"

        // 是否展示植物种植完成界面
        const val KEY_IS_SHOW_COMPLETE = "key_is_show_complete"

        // 跳转选择界面师傅哦直接弹窗
        const val KEY_USER_NO_STRAIN_NAME = "key_user_no_strain_name"
        const val KEY_USER_NO_ATTRIBUTE = "key_user_no_attribute"
        const val KEY_REFRESH_PLANT_INFO = "key_refresh_plant_info"
        const val KEY_IS_SHOW_CHOOSER_TIPS = "key_is_show_chooser_tips"

        // 植物ID
        const val KEY_PLANT_ID = "key_plant_id"

        // 学院页面的TXid
        const val KEY_TXT_ID = "key_txt_id"
        const val KEY_TXT_TYPE = "key_txt_type"
        const val KEY_TASK_ID = "key_task_id"

        // 只展示一次的消息气泡
        var KEY_IS_ONLY_ONE_SHOW = true

        // 从主页面、离线页面跳转到设备列表界面，设备列表界面切换了设备。
        var KEY_IS_SWITCH_DEVICE = "key_is_switch_device"

        // 只展示一次引导
        var KEY_IS_SHOW_GUIDE_POP = "key_is_show_guide_pop"

        // 当前植物的标记ID
        var KEY_CATEGORYCODE = "categoryCode"

        // 是否是手动模式
        const val KEY_MANUAL_MODE = "key_manual_mode"

        // 设备类型
        const val KEY_DEVICE_TYPE = "key_device_type"

        // 注册或者是忘记密码  true 是注册 false忘记密码
        const val KEY_REGISTER_OR_FORGET_PASSWORD = "key_register_or_forget_password"

        // gif的宽度
        const val KEY_GIF_WIDTH = 828f

        // gif的高度
        const val KEY_GIF_HEIGHT = 1124f

        // wifi配对链接界面的返回key
        const val KEY_WIFI_PAIRING_BACK = 20

        // wifi配对链接界面的携带参数key
        const val KEY_WIFI_PAIRING_PARAMS = "key_wifi_pairing_params"

        // cameraId
        const val INTENT_DEV_ID = "intent_devId"

        // 分享Trend的类型
        const val KEY_SHARE_TYPE = "key_share_type"
        const val KEY_SHARE_CONTENT = "key_share_content"
        const val KEY_SHARE_TEXT  = "key_share_text"

        // 是否开启了延时摄影模式
        /*const val KEY_TIME_LAPSE = "key_time_lapse"*/

        // 摄像头当天是否已经截图了
        // lastOperationDate
        /*const val KEY_IS_LAST_OPERATION_DATE = "key_is_take_photo_today"*/

        // 是否显示camera
        const val KEY_IS_SHOW_CAMERA = "key_is_show_camera"
    }

    // Fixed Id
    object Fixed {
        /**
         *  日历中记录旅程，发送trend	page_not_purchased	516c590993a041309912ebe16c2eb856
        刚注册,未绑定设备，未购买页	record_journey	6140e4e5df774d60a2a4029ebff6e1d3
        如何挑选Strain	how_to_pick_strain	790f39c0b0dd485a86c5f6abd714d65b
        关于解锁Seed	about_check_transplant	9f87272af9384c9d82136e1dd56587ce
        关于解锁花期	about_check_flowering	e1e2e7c57e684fe5b7fab82dddd27ea5
        关于解锁冲刷期	about_check_flushing	24b10272b84a418c9a59fef242324152
        关于解锁干燥期	about_check_drying	794eac2672074d92816772104ae3dbeb
        关于解锁装罐	about_check_curing	6ae6f4b03d274898a9c3816e3c4362b8
        关于解锁AutoFlowering	about_check_auto_flowering	efbec74793ec4fceae3afdd694127022
         */
        // 刚注册,未绑定设备，未购买页
        const val KEY_FIXED_ID_PAGE_NOT_PURCHASED = "6140e4e5df774d60a2a4029ebff6e1d3"

        // 日历记录旅程
        const val KEY_FIXED_ID_RECORD_JOURNEY = "516c590993a041309912ebe16c2eb856"

        // 如何挑选Strain
        const val KEY_FIXED_ID_HOW_TO_PICK_STRAIN = "790f39c0b0dd485a86c5f6abd714d65b"

        // 关于解锁Seed
        const val KEY_FIXED_ID_ABOUT_CHECK_TRANSPLANT = "9f87272af9384c9d82136e1dd56587ce"
        const val KEY_FIXED_ID_ABOUT_CHECK_FLOWERING = "e1e2e7c57e684fe5b7fab82dddd27ea5"
        const val KEY_FIXED_ID_ABOUT_CHECK_FLUSHING = "24b10272b84a418c9a59fef242324152"
        const val KEY_FIXED_ID_ABOUT_CHECK_DRYING = "794eac2672074d92816772104ae3dbeb"
        const val KEY_FIXED_ID_ABOUT_CHECK_CURING = "6ae6f4b03d274898a9c3816e3c4362b8"
        const val KEY_FIXED_ID_ABOUT_CHECK_AUTO_FLOWERING = "efbec74793ec4fceae3afdd694127022"

        // SeedKit 关于纸杯子还是塑料杯子的引导Id
        const val KEY_FIXED_ID_SEED_KIT_CUP_TYPE = "c11f05670d574634b7d19a8d3abf6192"

        // 推荐种植箱页
        const val KEY_FIXED_ID_A_FEW_TIPS = "0aad68f4fd654d86aea07fbcd032abb5"
        const val KEY_FIXED_ID_PREPARE_THE_SEED = "6a766f22ace142eaa6a95de34ece12c2"
        const val KEY_FIXED_ID_SEED_GERMINATION_PREVIEW = "bfbc92f6611b489caecfcc4c7476ec00"
        const val KEY_FIXED_ID_ACTION_NEEDED = "8020472470434bf4a62ea29b007f8631"
        const val KEY_FIXED_ID_WATER_CHANGE_GERMINATION = "8b30222d881143c58d4863f9d59d1d47"
        const val KEY_FIXED_ID_TRANSPLANT_SEED_CHECK = "f7b4364638ae4f93a88b2375a02742dd"
        const val KEY_FIXED_ID_TRANSPLANT_CLONE_CHECK = "b7b1fd6d4c854339a1e97016c52d2e52"
        const val KEY_FIXED_ID_TRANSPLANT_1 = "4daf5ac597ec4114acacd00758b71998"
        const val KEY_FIXED_ID_TRANSPLANT_2 = "4c9f93dae8da4e7dbc780f0eeed74b21"
        const val KEY_FIXED_ID_TRANSPLANT_3 = "6b05ffbadee746e6979a314613df75b2"
        const val KEY_FIXED_ID_VEGETATIVE_STAGE_PREVIEW = "98c5c81ab5d142f8a6e439628c9c9f39"
        const val KEY_FIXED_ID_AUTOFLOWERING_STAGE_PREVIEW = "d966091a08c048db962b710212eb223d"
        // soil_transplant_clone_check
        const val KEY_FIXED_ID_SOIL_TRANSPLANT_CLONE_CHECK = "soil_transplant_clone_check"

        // 去拍照界面
        // KEY_FIXED_TASK_ID
        const val KEY_FIXED_ID_GO_TO_CAMERA = "go_to_camera"


        // 提前解锁周期
        const val KEY_FIXED_ID_PREPARE_UNLOCK_PERIOD = "dc2d55199a45b60fd324d6c4c75de0e3"

        // 新增配件
        const val KEY_FIXED_ID_NEW_ACCESSORIES = "xxxxx"

        //router_settings
        const val KEY_FIXED_ID_ROUTER_SETTINGS = "router_settings"

        // 手动模式自动模式
        const val KEY_FIXED_ID_MANUAL_MODE = "6f5c5f54f3515fe9c744980e39583e95"
    }

    object FragmentIndex {
        const val HOME_INDEX = 0
        const val PLANT_LOG = 1
        const val CONTACT_INDEX = 2
        const val SHOP_INDEX = 3
        const val MY_INDEX = 4
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