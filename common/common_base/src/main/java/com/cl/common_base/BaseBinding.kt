package com.cl.common_base

import androidx.databinding.ViewDataBinding

interface BaseBinding<VB : ViewDataBinding> {
    fun VB.initBinding()
}