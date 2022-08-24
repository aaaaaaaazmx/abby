package com.cl.modules_pairing_connection.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.modules_pairing_connection.repository.PairRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SN修复
 *
 * @author 李志军 2022-08-19 17:05
 */
class PairFrontScanCodeViewModel @Inject constructor(private val repository: PairRepository) :
    ViewModel() {

    /**
     * 检查用户SN是否正确
     */
    private val _checkSN = MutableLiveData<Resource<BaseBean>>()
    val checkSN: LiveData<Resource<BaseBean>> = _checkSN
    fun checkSN(deviceSn: String) = viewModelScope.launch {
        repository.checkSN(deviceSn)
            .map {
                if (it.code != Constants.APP_SUCCESS) {
                    Resource.DataError(
                        it.code,
                        it.msg
                    )
                } else {
                    Resource.Success(it.data)
                }
            }
            .flowOn(Dispatchers.IO)
            .onStart {
                emit(Resource.Loading())
            }
            .catch {
                logD("catch $it")
                emit(
                    Resource.DataError(
                        -1,
                        "$it"
                    )
                )
            }.collectLatest {
                _checkSN.value = it
            }
    }

}