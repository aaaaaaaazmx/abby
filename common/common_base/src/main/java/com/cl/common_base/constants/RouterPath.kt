package com.cl.common_base.constants

/**
 * @Description: Arouter路由表
 * 不同的module，一级目录必须不能相同
 */
class RouterPath {

    /**
     * 登录注册 组件
     */
    object LoginRegister {
        private const val LOGIN_REGISTER = "module_login"
        const val PAGE_LOGIN = "/login/$LOGIN_REGISTER"
        const val PAGE_REGISTER = "/register/$LOGIN_REGISTER"

        const val SERVICE_LOGOUT = "/logout/$LOGIN_REGISTER"
        // 修改密码页面
        const val PAGE_CHANGE_PASSWORD = "/changePassword/$LOGIN_REGISTER"
    }

    /**
     * 欢迎页面
     */
    object Welcome {
        private const val WELCOME_MODEL = "module_app"
        const val PAGE_SPLASH = "/splash/${WELCOME_MODEL}"
    }

    /**
     * 检查与配对模块
     * 配网模块
     */
    object PairConnect {
        private const val PAIR_CONNECT = "module_pair_connect"
        // 配对设备界面
        const val PAGE_PLANT_CHECK = "/plant/$PAIR_CONNECT"
        // 设备列表界面
        const val PAGE_PLANT_SCAN = "/plantScan/$PAIR_CONNECT"
        // 重新连接界面
        const val KEY_PAIR_RECONNECTING = "/pairReconnecting/$PAIR_CONNECT"
        // 扫码界面
        const val PAGE_SCAN_CODE = "/pairScanCode/$PAIR_CONNECT"
        // wifi链接界面
        const val PAGE_WIFI_CONNECT = "/pairWifiConnect/$PAIR_CONNECT"

        // 涂鸦设备wifi配对界面
        // 设备列表界面
        const val PAGE_WIFI_DEVICE_SCAN = "/plantWifiScan/$PAIR_CONNECT"
    }

    /**
     * 主页
     */
    object Main {
        private const val MAIN = "main"
        const val PAGE_MAIN = "/main/$MAIN"

        const val SERVICE_BANNER = "/banner/$MAIN"
        const val SERVICE_COLLECT = "/collect/$MAIN"
    }

    /**
     * 内容 web 组件
     */
    object Content {
        private const val CONTENT = "module_content"
        const val PAGE_CONTENT = "/content/$CONTENT"
    }

    /**
     * 首页 组件
     */
    object Home {
        private const val HOME = "modules_home"
        const val PAGE_HOME = "/home/$HOME"
        const val PAGE_BLACK_HOME = "/blackHome/$HOME"
        const val PAGE_KNOW = "/knowMore/$HOME" // 学院
        const val PAGE_PLANT_NAME = "/plantName/$HOME"
        // 摄像头显示界面
        const val PAGE_CAMERA = "/camera/$HOME"
        // 新用户第一次进入的界面
        const val PAGE_FIRST_JOIN = "/firstJoin/$HOME"
        const val PAGE_HOME_TASK_SET = "/taskSet/$HOME"
        const val PAGE_HOME_PRO_MODE_START = "/proModeStart/$HOME"
        // tentKitPlantSetup
        const val PAGE_TENT_KIT_PLANT_SETUP = "/tentKitPlantSetup/$HOME"
        // PAGE_GROW_MODE
        const val PAGE_GROW_MODE = "/growMode/$HOME"
    }

    /**
     * 联系人 组件
     */
    object Contact {
        private const val CONTACT = "modules_contact"
        // 商城首页
        const val PAGE_SHOP = "/shop/$CONTACT"
        const val PAGE_CONTACT = "/contact/$CONTACT"
        // 发Trend页面
        const val PAGE_TREND = "/trend/$CONTACT"
        // 发布gif页面
        const val PAGE_GIF = "/gif/$CONTACT"
        // PAGE_OTHER_JOURNEY
        const val PAGE_OTHER_JOURNEY = "/otherJourney/$CONTACT"
    }

    /**
     * 我的 组件
     */
    object My {
        private const val MY = "modules_my"
        const val PAGE_MY = "/my/$MY"
        const val PAGE_MY_FIRMWARE_UPDATE = "/firmwareUpdate/$MY"
        const val PAGE_MT_CLONE_SEED = "/cloneSeed/$MY"
        const val PAGE_MY_CALENDAR = "/calendar/$MY"
        const val PAGE_MY_GUIDE_SEED = "/guideSeed/$MY"
        const val PAGE_MY_DEVICE_LIST = "/deviceList/$MY"
        const val PAGE_MY_DEVICE_AUTOMATION = "/deviceAutomation/$MY"
        const val PAGE_MY_DEVICE_SETTING = "/deviceSetting/$MY"
        // 摄像头二维码配对界面
        const val PAGE_CAMERA_QR_CODE = "/cameraQrCode/$MY"
        // 添加配件页面
        const val PAGE_ADD_ACCESSORY = "/AddAccessory/$MY"
        // 添加帐篷界面
        const val PAGE_ADD_TENT = "/AddTent/$MY"
        // 资产界面DIGITAL
        const val PAGE_DIGITAL = "/digital/$MY"
        // PAGE_MY_JOURNEY
        const val PAGE_MY_JOURNEY = "/journey/$MY"
        // WIFI_PAIR
        const val WIFI_PAIR = "/wifiPair/$MY"
    }

    /**
     * 种植日志 组件
     */
    object Plant {
        private const val PLANT = "modules_plant"
        const val PAGE_PLANT = "/planting/$PLANT"
    }


    /**
     * 体系 组件
     */
    object Square {
        private const val SQUARE = "module_square"
        const val PAGE_SQUARE = "/square/$SQUARE"
    }

    /**
     * 项目 组件
     */
    object Project {
        private const val PROJECT = "module_project"
        const val PAGE_PROJECT = "/project/$PROJECT"
    }

    /**
     * compose 组件
     */
    object Compose {
        private const val COMPOSE = "module_compose"
        const val PAGE_COMPOSE = "/compose/$COMPOSE"
    }

}