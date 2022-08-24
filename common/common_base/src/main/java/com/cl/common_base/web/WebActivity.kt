package com.cl.common_base.web


class WebActivity : BaseWebActivity() {

    private val webUrl by lazy {
        intent.getStringExtra(KEY_WEB_URL)
    }

    private val webTitleName by lazy {
        intent.getStringExtra(KEY_WEB_TITLE_NAME)
    }

    override fun getUrl(): String {
        return webUrl ?: ""
    }

    override fun getName(): String {
        return webTitleName ?: ""
    }

    companion object {
        const val KEY_WEB_URL = "key_web_url"
        const val KEY_WEB_TITLE_NAME = "key_web_title_name"
    }
}