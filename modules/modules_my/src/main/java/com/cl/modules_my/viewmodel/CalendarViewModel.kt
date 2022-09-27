package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.CalendarData
import com.cl.common_base.bean.GuideInfoData
import com.cl.common_base.bean.UpdateReq
import com.cl.common_base.bean.UserinfoBean
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.util.calendar.CalendarUtil
import com.cl.modules_my.repository.MyRepository
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.http.Body
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Inject

@ActivityRetainedScoped
class CalendarViewModel @Inject constructor(private val repository: MyRepository) :
    ViewModel() {

    // 今天
    val mCurrentDate by lazy {
        val mCurrentDate = com.cl.common_base.util.calendar.Calendar()
        val d = Date()
        mCurrentDate.year = CalendarUtil.getDate("yyyy", d)
        mCurrentDate.month = CalendarUtil.getDate("MM", d)
        mCurrentDate.day = CalendarUtil.getDate("dd", d)
        mCurrentDate.isCurrentDay = true
        mCurrentDate
    }

    /**
     * 获取用户信息
     */
    private val _userDetail = MutableLiveData<Resource<UserinfoBean.BasicUserBean>>()
    val userDetail: LiveData<Resource<UserinfoBean.BasicUserBean>> = _userDetail
    fun userDetail() = viewModelScope.launch {
        repository.userDetail()
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
                _userDetail.value = it
            }
    }


    /**
     * 获取日历任务
     */
    private val _getCalendar = MutableLiveData<Resource<MutableList<CalendarData>>>()
    val getCalendar: LiveData<Resource<MutableList<CalendarData>>> = _getCalendar
    fun getCalendar(startDate: String, endDate: String) = viewModelScope.launch {
        repository.getCalendar(startDate, endDate)
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
                if (it.data.isNullOrEmpty()) return@collectLatest
                _getCalendar.value = it
            }
    }

    /**
     * 更新日历任务
     */
    private val _updateTask = MutableLiveData<Resource<BaseBean>>()
    val updateTask: LiveData<Resource<BaseBean>> = _updateTask
    fun updateTask(body: UpdateReq) = viewModelScope.launch {
        repository.updateTask(body)
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
                _updateTask.value = it
            }
    }


    /**
     * 现在直接传任务类型就好了
     */
    private val _getGuideInfo = MutableLiveData<Resource<GuideInfoData>>()
    val getGuideInfo: LiveData<Resource<GuideInfoData>> = _getGuideInfo
    fun getGuideInfo(req: String) {
        viewModelScope.launch {
            repository.getGuideInfo(req)
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
                    _getGuideInfo.value = it
                }
        }
    }

    /**
     * 解锁花期
     */
    private val _unlockJourney = MutableLiveData<Resource<BaseBean>>()
    val unlockJourney: LiveData<Resource<BaseBean>> = _unlockJourney
    fun unlockJourney(name: String, weight: String? = null) {
        viewModelScope.launch {
            repository.unlockJourney(name, weight)
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
                    _unlockJourney.value = it
                }
        }
    }


    /**
     * 获取当前年月日-- 后面跟着英文的th
     * 如 9 12th 2022
     */
    fun getYmdForEn(dateTime: Date? = null, time: Long? = null): String {
        dateTime?.let {
            val mm = CalendarUtil.getFormat("MMM").format(dateTime.time)
            val dd = CalendarUtil.getFormat("dd").format(dateTime.time) + CalendarUtil.getDaySuffix(
                dateTime
            )
            val yyyy = CalendarUtil.getFormat("yyyy").format(dateTime.time)
            return "$mm $dd $yyyy"
        }

        time?.let {
            val mm = CalendarUtil.getFormat("MMM").format(time)
            val date = Date()
            date.time = time
            val dd = CalendarUtil.getFormat("dd").format(time) + CalendarUtil.getDaySuffix(date)
            val yyyy = CalendarUtil.getFormat("yyyy").format(time)
            return "$mm $dd $yyyy"
        }
        return ""
    }


    // 本地日历数据
    private val _localCalendar =
        MutableLiveData<MutableList<com.cl.common_base.util.calendar.Calendar>>(mutableListOf())
    val localCalendar: LiveData<MutableList<com.cl.common_base.util.calendar.Calendar>> =
        _localCalendar

    fun setLocalCalendar() {
        _localCalendar.value = null
    }

    /**
     * 数据组装
     * 本地日历
     */
    fun getLocalCalendar(
        startMonth: Int? = 1,
        endMonth: Int? = 12,
        year: Int
    ) {
        viewModelScope.launch {
            val list = mutableListOf<com.cl.common_base.util.calendar.Calendar>()
            for (i in startMonth!!..endMonth!!) {
                list += CalendarUtil.initCalendarForMonthView(
                    year,
                    i,
                    mCurrentDate,
                    Calendar.SUNDAY
                )
            }
            _localCalendar.value = list
        }
    }

    /**
     * 滑动时加载时的月份
     */
    private val _scrollMonth = MutableLiveData<Int>(-1)
    val scrollMonth: LiveData<Int> = _scrollMonth
    fun setScrollMonth(month: Int?) {
        _scrollMonth.value = month
    }

    /**
     * 点击Go-> guideInfoStatus
     */
    private val _guideInfoStatus =
        MutableLiveData<String>()
    val guideInfoStatus: LiveData<String> =
        _guideInfoStatus

    fun setGuideInfoStatus(guideInfoStatus: String) {
        _guideInfoStatus.value = guideInfoStatus
    }
}