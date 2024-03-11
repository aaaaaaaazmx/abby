package com.cl.modules_contact.pop

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.safeToFloat
import com.cl.common_base.ext.safeToInt
import com.cl.common_base.ext.setSafeOnClickListener
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.ViewUtils
import com.cl.common_base.web.WebActivity
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.adapter.ContactNewEnvAdapter
import com.cl.modules_contact.databinding.ContactNewEnvPopBinding
import com.cl.modules_contact.request.ContactEnvData
import com.cl.modules_contact.response.NewPageData
import com.lxj.xpopup.core.BottomPopupView

/**
 * Trend 环境信息弹窗
 */
class ContactNewEnvPop(private val context: Context, private val envInfoData: MutableList<ContactEnvData>, val record: NewPageData.Records? = null) : BottomPopupView(context) {

    // 是否是公制
    // true 是C false F
    val isMetric = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)

    override fun getImplLayoutId(): Int {
        return R.layout.contact_new_env_pop
    }

    private val adapter by lazy {
        ContactNewEnvAdapter(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.bind<ContactNewEnvPopBinding>(popupImplView)?.apply {
            lifecycleOwner = this@ContactNewEnvPop
            executePendingBindings()


            // 名字
            tvName.text =  record?.nickName
            // strainNema
            tvStrainName.text = record?.strainName
            // 设备名字
            tvOgEdition.text = record?.deviceName
            // 头像
            ViewUtils.setGone(ivAvatar,TextUtils.isEmpty(record?.avatarPicture))
            ViewUtils.setVisible(TextUtils.isEmpty(record?.avatarPicture), noheadShow)
            Glide.with(context).load(record?.avatarPicture)
                .apply(RequestOptions.circleCropTransform())
                .into(ivAvatar)

            noheadShow.text = record?.nickName?.substring(0,1)

            // 设备的图片
            Glide.with(context).load(record?.deviceImage).into(ivOgEdition)

            // 设备型号名字
            tvOgEdition.text = record?.deviceModelName

            // 关闭按钮
            ivClose.setOnClickListener {
                dismiss()
            }


            // 右边信息部分
            // Grow 盒子部分
            // tv_grow_chamber_temperature
            tvGrowChamberTemperature.text = if (isMetric) {
                "Temperature ℃)"
            } else {
                "Temperature (℉)"
            }

            envInfoData.firstOrNull { it.detectionValue == "Grow Chamber Temperture" }?.let {
                // 返回的默认是F
                // 去要区别是否是公制
                tvGrowChamberTemperatureValue.text = temperatureConversion(it.value.safeToFloat(), isMetric)
                tvGrowChamberTemperatureStatus.text = it.healthStatus
            }

            envInfoData.firstOrNull { it.detectionValue ==  "Grow Chamber Humidity" }?.let {
                tvGrowChamberHumidityValue.text = it.value
                tvGrowChamberHumidityStatus.text = it.healthStatus
            }

            // 水温 以及ph tds
            //tv_water_tank_temperatureContactEnvAdapter
            tvWaterTankTemperature.text = if (isMetric) {
                "Temperature (℃)"
            } else {
                "Temperature (℉)"
            }

            envInfoData.firstOrNull { it.detectionValue ==  "Water Tank Temperture" }?.let {
                tvWaterTankTemperatureValue.text = temperatureConversion(it.value.safeToFloat(), isMetric)
                tvWaterTankTemperatureStatus.text = it.healthStatus
            }

            envInfoData.firstOrNull { it.detectionValue == "pH" }?.let {
                tvWaterTankPhValue.text = it.value
            }

            envInfoData.firstOrNull { it.detectionValue == "TDS" }?.let {
                tvWaterTankTdsValue.text = it.value
            }


            // 配件列表
            rvList.layoutManager = LinearLayoutManager(context)
            rvList.adapter = this@ContactNewEnvPop.adapter
            this@ContactNewEnvPop.adapter.setList(record?.accessorys)

            // 购买设备链接
            tvBugDevice.setSafeOnClickListener {
                val intent = Intent(context, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_WEB_URL, record?.deviceBuyLink)
                intent.putExtra(WebActivity.KEY_WEB_TITLE_NAME, "hey abby")
                context.startActivity(intent)
            }
        }
    }

  private  fun temperatureConversion(value: Float, isMetric: Boolean): String {
        // 默认是摄氏度 true是摄氏度、false是华氏度
        if (value == 0f) return "0"
        val result = if (isMetric) value.div(10) else (value.div(10)).times(9f).div(5f).plus(32)
        return if (result == 0f) "" else result.safeToInt().toString()
    }
}