package com.cl.common_base.web


class WebActivity : BaseWebActivity() {

    private val webUrl by lazy {
        intent.getStringExtra(KEY_WEB_URL)
    }

    private val webTitleName by lazy {
        intent.getStringExtra(KEY_WEB_TITLE_NAME)
    }

    private val showCar by lazy {
        intent.getBooleanExtra(KEY_IS_SHOW_CAR, false)
    }

    override fun getUrl(): String {
        return webUrl ?: ""
    }

    override fun getName(): String {
        return webTitleName ?: ""
    }

    override fun showCar(): Boolean {
        return showCar ?: false
    }

    companion object {
        const val KEY_WEB_URL = "key_web_url"
        const val KEY_IS_SHOW_CAR = "key_is_show_car"
        const val KEY_WEB_TITLE_NAME = "key_web_title_name"
    }
}