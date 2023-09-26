package com.cl.modules_home.activity

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbgo.module_home.databinding.HomePlantProfileBinding
import com.cl.common_base.adapter.StrainNameSearchAdapter
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.bean.UpPlantInfoReq
import com.cl.common_base.constants.Constants
import com.cl.common_base.ext.*
import com.cl.common_base.net.ServiceCreators
import com.cl.common_base.pop.BaseCenterPop
import com.cl.common_base.pop.activity.BasePopActivity
import com.cl.common_base.service.BaseApiService
import com.cl.common_base.util.Prefs
import com.cl.common_base.util.SoftInputUtils
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.common_base.base.KnowMoreActivity
import com.cl.modules_home.viewmodel.HomePlantProfileViewModel
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * 选择植物属性页面
 */
@AndroidEntryPoint
class HomePlantProfileActivity : BaseActivity<HomePlantProfileBinding>() {

    @Inject
    lateinit var mViewModel: HomePlantProfileViewModel

    // 带过来的属性名字
    private val plantName by lazy {
        intent.getStringExtra(KEY_PLANT_NAME)
    }

    // 获取植物ID
    private val plantId by lazy {
        Prefs.getString(Constants.Global.KEY_PLANT_ID)
    }

    private val pop by lazy {
        XPopup.Builder(this).isDestroyOnDismiss(false).dismissOnTouchOutside(false)
    }

    private var category: Int? = null
    override fun initView() {
        binding.btnSuccess.setOnClickListener {
            category = if (binding.checkSeed.isChecked) {
                100001
            } else if (binding.checkClone.isChecked) {
                100003
            } else if (binding.autoCheckSeed.isChecked) {
                100002
            } else if (binding.autoCheckClone.isChecked) {
                100004
            } else {
                100001
            }

            /*if (binding.etEmail.text.toString().isEmpty()) {
                ToastUtil.shortShow("Please enter a strain name")
                return@setOnClickListener
            }*/

            kotlin.runCatching {
                mViewModel.updatePlantInfo(
                    UpPlantInfoReq(
                        plantId = plantId.safeToInt(),
                        plantName = plantName ?: "",
                        strainName = binding.etEmail.text.toString(),
                        categoryCode = "$category"
                    )
                )
            }

        }

        binding.clHow.setOnClickListener {
            pop.asCustom(
                BaseCenterPop(
                    this,
                    titleText = "Photo vs Auto",
                    content = "Photoperiod strains are more common. Unless you’ve confirmed with your retailer that it is autoflower, please select “photoperiod” here.\n" + "\n" + "Hey abby can generate an algorithm to tailor for either type.",
                    isShowCancelButton = false
                )
            ).show()
        }

        binding.clNot.setOnClickListener {
            pop.asCustom(
                BaseCenterPop(
                    this,
                    titleText = "Not Sure?",
                    content = "Please check with your local retailer or seed bank. You can also usually find this information in your order invoice. \n\n Note: choosing the wrong setting can delay flowering. We strongly encourage you to confirm with your provider first. ",
                    isShowCancelButton = false
                )
            ).show()
        }


        binding.checkClone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.checkSeed.isChecked = false
                binding.autoCheckSeed.isChecked = false
                binding.autoCheckClone.isChecked = false
                binding.tvAuto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.color_c4))
                binding.tvPhoto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.mainColor))
            }

            if (!isChecked) {
                if (!binding.checkSeed.isChecked && !binding.autoCheckSeed.isChecked && !binding.autoCheckClone.isChecked) {
                    binding.checkClone.isChecked = true
                }
            }
        }
        binding.checkSeed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.checkClone.isChecked = false
                binding.autoCheckSeed.isChecked = false
                binding.autoCheckClone.isChecked = false
                binding.tvAuto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.color_c4))
                binding.tvPhoto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.mainColor))
            }

            if (!isChecked) {
                if (!binding.checkClone.isChecked && !binding.autoCheckSeed.isChecked && !binding.autoCheckClone.isChecked) {
                    binding.checkSeed.isChecked = true
                }
            }
        }
        binding.autoCheckClone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.checkClone.isChecked = false
                binding.checkSeed.isChecked = false
                binding.autoCheckSeed.isChecked = false
                binding.tvAuto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.mainColor))
                binding.tvPhoto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.color_c4))
            }

            if (!isChecked) {
                if (!binding.checkClone.isChecked && !binding.checkSeed.isChecked && !binding.autoCheckSeed.isChecked) {
                    binding.autoCheckClone.isChecked = true
                }
            }
        }
        binding.autoCheckSeed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.checkClone.isChecked = false
                binding.checkSeed.isChecked = false
                binding.autoCheckClone.isChecked = false
                binding.tvAuto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.mainColor))
                binding.tvPhoto.setTextColor(ContextCompat.getColor(this, com.cl.common_base.R.color.color_c4))
            }

            if (!isChecked) {
                if (!binding.checkClone.isChecked && !binding.checkSeed.isChecked && !binding.autoCheckClone.isChecked) {
                    binding.autoCheckSeed.isChecked = true
                }
            }
        }

        binding.ivClearCode.setOnClickListener {
            binding.etEmail.setText("")
        }
    }

    override fun observe() {
        mViewModel.apply {
            updatePlantInfo.observe(this@HomePlantProfileActivity, resourceObserver {
                loading {
                    showProgressLoading()
                }
                error { errorMsg, code ->
                    hideProgressLoading()
                    ToastUtil.shortShow(errorMsg)
                }
                success {
                    hideProgressLoading()

                    // 看是否是选了clone、那么直接进入移植
                    if (category == 100003 || category == 100004) {
                        val intent = Intent(this@HomePlantProfileActivity, BasePopActivity::class.java)
                        intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_CLONE_CHECK)
                        intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_TRANSPLANT_CLONE_CHECK)
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                        intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                        intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, "I am ready")
                        intent.putExtra(BasePopActivity.KEY_CATEGORYCODE, "$category")
                        intent.putExtra(BasePopActivity.KEY_TITLE_COLOR, "#006241")
                        startActivity(intent)
                        return@success
                    }

                    // 跳转富文本界面
                    val intent = Intent(this@HomePlantProfileActivity, KnowMoreActivity::class.java)
                    intent.putExtra(Constants.Global.KEY_TXT_ID, Constants.Fixed.KEY_FIXED_ID_PREPARE_THE_SEED)
                    intent.putExtra(BasePopActivity.KEY_FIXED_TASK_ID, Constants.Fixed.KEY_FIXED_ID_PREPARE_THE_SEED)
                    intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON, true)
                    intent.putExtra(BasePopActivity.KEY_INTENT_JUMP_PAGE, true)
                    intent.putExtra(BasePopActivity.KEY_IS_SHOW_BUTTON_TEXT, "Done")
                    startActivity(intent)
                }
            })
        }
    }

    override fun initData() {
        // 搜索列表
        binding.rvSearch.layoutManager = LinearLayoutManager(this)
        binding.rvSearch.adapter = searchAdapter

        searchAdapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data[position].toString() == searching[0]) return@setOnItemClickListener
            binding.etEmail.setText(adapter.data[position].toString())
            binding.etEmail.setSelection(binding.etEmail.text?.length ?: 0)
            binding.rvSearch.setVisible(false)
            SoftInputUtils.hideSoftInput(this@HomePlantProfileActivity, binding.etEmail)
        }

        binding.etEmail.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_NEXT || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                SoftInputUtils.hideSoftInput(this@HomePlantProfileActivity, binding.etEmail)
                // 输入范围为1～24字节
                if (getTextLength(binding.etEmail.text.toString()) < 1 || getTextLength(binding.etEmail.text.toString()) > 24) {
                    ToastUtil.shortShow(getString(com.cl.common_base.R.string.strain_name_desc))
                } else {
                    //  输入完成
                    category = if (binding.checkSeed.isChecked) {
                        100001
                    } else if (binding.checkClone.isChecked) {
                        100003
                    } else if (binding.autoCheckSeed.isChecked) {
                        100002
                    } else if (binding.autoCheckClone.isChecked) {
                        100004
                    } else {
                        100001
                    }

                    /* if (binding.etEmail.text.toString().isEmpty()) {
                         ToastUtil.shortShow("Please enter a strain name")
                     } else {*/
                    kotlin.runCatching {
                        mViewModel.updatePlantInfo(
                            UpPlantInfoReq(
                                plantId = plantId.safeToInt(),
                                plantName = plantName ?: "",
                                strainName = binding.etEmail.text.toString(),
                                categoryCode = "$category"
                            )
                        )
                    }
                }
            }
            false
        }

        binding.etEmail.textChangeFlow() // 构建输入框文字变化流
            .filter { it.isNotEmpty() } // 过滤空内容，避免无效网络请求
            .debounce(300) // 300ms防抖
            .flatMapLatest { searchFlow(it.toString()) } // 新搜索覆盖旧搜索
            .flowOn(Dispatchers.IO) // 让搜索在异步线程中执行
            .onEach { updateUi(it) } // 获取搜索结果并更新界面
            .launchIn(lifecycleScope) // 在主线程收集搜索结果

    }


    // 构建输入框文字变化流
    private fun EditText.textChangeFlow(): Flow<Editable> = callbackFlow {
        // 构建输入框监听器
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // 在文本变化后向流发射数据
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.rvSearch.setVisible(!s.isNullOrEmpty())
                if (!s.isNullOrEmpty()) searchAdapter.setList(searching)
                // 点击按钮状态监听
                // binding.btnSuccess.isEnabled = !s.isNullOrEmpty()
                trySend(text)
            }
        }
        addTextChangedListener(watcher) // 设置输入框监听器
        awaitClose { removeTextChangedListener(watcher) } // 阻塞以保证流一直运行
    }

    // 将搜索关键词转换成搜索结果流
    private fun searchFlow(key: String) = flow { emit(search(key)) }

    // 访问网络进行搜索
    private suspend fun search(key: String): List<String> {
        return getStrainNameList(key)
    }

    // 更新界面
    private fun updateUi(it: List<String>) {
        searchAdapter.setList(it)
        binding.neSc.post {
            binding.neSc.smoothScrollTo(0, binding.neSc.bottom)
        }

        // 在滚动之后，再次让 EditText 获取焦点（如果需要）
        binding.etEmail.postDelayed({
            binding.etEmail.requestFocus()
        }, 300)  // 300 毫秒的延迟，或者你认为合适的其他时间
    }

    // 搜索时
    private val searching: MutableList<String> = mutableListOf("Searching...")
    private val searchAdapter by lazy {
        StrainNameSearchAdapter(mutableListOf())
    }

    private fun getTextLength(text: CharSequence): Int {
        var length = 0
        for (element in text) {
            if (element.code > 255) {
                length += 2
            } else {
                length++
            }
        }
        return length
    }

    private val service = ServiceCreators.create(BaseApiService::class.java)
    private suspend fun getStrainNameList(txt: String): MutableList<String> {
        val mutableList = mutableListOf<String>()
        service.getStrainName(txt).map {
            if (it.code != Constants.APP_SUCCESS) {
                Resource.DataError(
                    it.code, it.msg
                )
            } else {
                Resource.Success(it.data)
            }
        }.flowOn(Dispatchers.IO).onStart {
            emit(Resource.Loading())
        }.catch {
            logD("catch $it")
            emit(
                Resource.DataError(
                    -1, "$it"
                )
            )
        }.collectLatest {
            logI(it.toString())
            it.data?.let { it1 -> mutableList.addAll(it1) }
        }
        return mutableList
    }

    companion object {
        const val KEY_PLANT_NAME = "key_plant_name"
    }
}