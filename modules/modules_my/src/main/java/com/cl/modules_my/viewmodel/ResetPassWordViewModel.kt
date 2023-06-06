package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.modules_my.repository.MyRepository
import com.cl.modules_my.request.ResetPwdReq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

class ResetPassWordViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {

    private val _passWordState = MutableLiveData<Boolean>(true)
    val passWordState: LiveData<Boolean> = _passWordState

    fun setPassWordState(state: Boolean) {
        _passWordState.value = state
    }

    /**
     * 修改密码
     */
    private val _resetPwd = MutableLiveData<Resource<Boolean>>()
    val resetPwd: LiveData<Resource<Boolean>> = _resetPwd
    fun resetPwd(req: ResetPwdReq) = viewModelScope.launch {
        repository.resetPwd(req).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(true)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _resetPwd.value = it
        }
    }



}