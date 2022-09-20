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
        // 重新连接界面
        const val KEY_PAIR_RECONNECTING = "/pairReconnecting/$PAIR_CONNECT"
        // 扫码界面
        const val PAGE_SCAN_CODE = "/pairScanCode/$PAIR_CONNECT"
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
    }

    /**
     * 联系人 组件
     */
    object Contact {
        private const val CONTACT = "modules_contact"
        const val PAGE_CONTACT = "/contact/$CONTACT"
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