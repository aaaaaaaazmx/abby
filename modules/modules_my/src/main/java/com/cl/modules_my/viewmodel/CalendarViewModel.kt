package com.cl.modules_my.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cl.common_base.BaseBean
import com.cl.common_base.bean.*
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.Resource
import com.cl.common_base.ext.logD
import com.cl.common_base.ext.logI
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.calendar.CalendarUtil
import com.cl.common_base.util.device.TuYaDeviceConstants
import com.cl.common_base.util.json.GSON
import com.cl.modules_my.repository.MyRepository
import com.tuya.smart.android.user.bean.User
import com.tuya.smart.sdk.bean.DeviceBean
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.http.Body
import java.time.OffsetDateTime
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


    val tuYaUser by lazy {
        val bean = Prefs.getString(Constants.Tuya.KEY_DEVICE_USER)
        GSON.parseObject(bean, User::class.java)
    }

    val tuyaDeviceBean by lazy {
        val homeData = Prefs.getString(Constants.Tuya.KEY_DEVICE_DATA)
        GSON.parseObject(homeData, DeviceBean::class.java)
    }

    private val getDeviceDps by lazy {
        tuyaDeviceBean?.dps
    }

    // 水的容积。=， 多少升
    private val _getWaterVolume =
        MutableLiveData(getDeviceDps?.filter { status -> status.key == TuYaDeviceConstants.KEY_DEVICE_WATER_STATUS }
            ?.get(TuYaDeviceConstants.KEY_DEVICE_WATER_STATUS).toString())
    val getWaterVolume: LiveData<String> = _getWaterVolume
    fun setWaterVolume(volume: String) {
        _getWaterVolume.value = volume
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

    private val _advertising = MutableLiveData<Resource<MutableList<AdvertisingData>>>()
    val advertising: LiveData<Resource<MutableList<AdvertisingData>>> = _advertising
    fun advertising(type: String? = "0") {
        viewModelScope.launch {
            repository.advertising(type ?: "0")
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
                    _advertising.value = it
                }
        }
    }


    /**
     * 上报排水结束
     */
    private val _deviceOperateFinish = MutableLiveData<Resource<BaseBean>>()
    val deviceOperateFinish: LiveData<Resource<BaseBean>> = _deviceOperateFinish
    fun deviceOperateFinish(type: String) {
        viewModelScope.launch {
            repository.deviceOperateFinish(type)
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
                    _deviceOperateFinish.value = it
                }
        }
    }

    /**
     * 日历 完成任务、解锁周期
     */
    private val _finishTask = MutableLiveData<Resource<BaseBean>>()
    val finishTask: LiveData<Resource<BaseBean>> = _finishTask
    fun finishTask(body: FinishTaskReq) {
        viewModelScope.launch {
            repository.finishTask(body)
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
                    _finishTask.value = it
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
                val data = CalendarUtil.initCalendarForMonthView(
                    year,
                    i,
                    mCurrentDate,
                    Calendar.SUNDAY
                )
                // 处理日期前面没有相差的天数
                if (startMonth == i) {
                    // 一行7个，没有相差，那么在12月的最后一行会自动添加1-7，那么在1月时，不需要添加1-7
                    if (data[0].day == 1) {
                        data.filter { it.day > 7 }.let {
                            list += it
                        }
                    } else {
                        list += data
                    }
                } else {
                    list += data
                }
            }
            _localCalendar.value = list
        }
    }

    /**
     * 跳过换水上报
     */
    private val _taskId = MutableLiveData<String>()
    val taskId: LiveData<String> = _taskId
    fun setTaskId(taskId: String) {
        _taskId.value = taskId
    }

    private val _deviceOperateStart = MutableLiveData<Resource<BaseBean>>()
    val deviceOperateStart: LiveData<Resource<BaseBean>> = _deviceOperateStart
    fun deviceOperateStart(taskId: String, type: String) {
        viewModelScope.launch {
            repository.deviceOperateStart(taskId, type)
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
                    _deviceOperateStart.value = it
                }
        }
    }


    /**
     * 用来表示只加载一次的标记位
     */
    private val _onlyFirstLoad = MutableLiveData<Boolean>(true)
    val onlyFirstLoad: LiveData<Boolean> = _onlyFirstLoad
    fun setOnlyFirstLoad(boolean: Boolean) {
        _onlyFirstLoad.postValue(boolean)
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

    private val _guideInfoTaskTime =
        MutableLiveData<String>()
    val guideInfoTaskTime: LiveData<String> =
        _guideInfoTaskTime

    fun setGuideInfoTime(guideInfoTaskTime: String) {
        _guideInfoTaskTime.value = guideInfoTaskTime
    }
}