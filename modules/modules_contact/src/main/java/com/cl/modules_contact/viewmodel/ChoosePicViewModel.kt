package com.cl.modules_contact.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.modules_contact.repository.ContactRepository
import com.cl.modules_contact.request.NewPageReq
import com.cl.modules_contact.request.TrendPictureReq
import com.cl.modules_contact.response.MessageListData
import com.cl.modules_contact.response.TrendPictureData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChoosePicViewModel @Inject constructor(private val repository: ContactRepository) : ViewModel() {

    /**
     * 加载Trend历史图片
     */
    private val _trendHistoryPic = MutableLiveData<Resource<TrendPictureData>>()
    val trendHistoryPic: LiveData<Resource<TrendPictureData>> = _trendHistoryPic
    fun trendHistoryPic(body: TrendPictureReq) = viewModelScope.launch {
        repository.getTrendPicture(body).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {}.catch {
            logD("catch ${it.message}")
            emit(
                Resource.DataError(
                    -1, "${it.message}"
                )
            )
        }.collectLatest {
            _trendHistoryPic.value = it
        }
    }
    

}