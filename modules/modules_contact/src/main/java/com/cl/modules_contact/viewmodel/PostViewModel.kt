package com.cl.modules_contact.viewmodel

import androidx.lifecycle.ViewModel
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.json.GSON
import com.cl.modules_contact.repository.ContactRepository
import javax.inject.Inject

class PostViewModel @Inject constructor(private val repository: ContactRepository) : ViewModel() {
    val userinfoBean by lazy {
        val bean = Prefs.getString(Constants.Login.KEY_LOGIN_DATA)
        GSON.parseObject(bean, UserinfoBean::class.java)
    }
}