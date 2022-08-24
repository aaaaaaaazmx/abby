package com.cl.common_base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class BaseUiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_ui)
    }
}