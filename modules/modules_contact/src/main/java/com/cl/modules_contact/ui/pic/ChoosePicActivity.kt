package com.cl.modules_contact.ui.pic

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.ext.logI
import com.cl.common_base.ext.resourceObserver
import com.cl.common_base.refresh.ClassicsHeader
import com.cl.common_base.widget.toast.ToastUtil
import com.cl.modules_contact.R
import com.cl.modules_contact.databinding.ContactChooserPicActivityBinding
import com.cl.common_base.widget.decoraion.FullyGridLayoutManager
import com.cl.common_base.widget.decoraion.GridSpaceItemDecoration
import com.cl.modules_contact.request.TrendPictureReq
import com.cl.common_base.bean.ChoosePicBean
import com.cl.modules_contact.ui.ReelPostActivity
import com.cl.modules_contact.viewmodel.ChoosePicViewModel
import com.luck.picture.lib.utils.DensityUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.widget.SmartDragLayout
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable
import javax.inject.Inject


@AndroidEntryPoint
class ChoosePicActivity : BaseActivity<ContactChooserPicActivityBinding>() {


    @Inject
    lateinit var mViewMode: ChoosePicViewModel

    // 接收传递过来的图片,并且转换成String类型
    private val selectedImages by lazy {
        val mutableList = mutableListOf<String>()
        (intent.getSerializableExtra(ReelPostActivity.KEY_PIC_LIST_RESULT) as? MutableList<*> ?: mutableListOf<ChoosePicBean>()).forEach {
            if (it is ChoosePicBean) {
                mutableList.add(it.picAddress ?: "")
            }
        }
        mutableList
    }

    override fun initView() {
        binding.smart.setDuration(XPopup.getAnimationDuration())
        binding.smart.enableDrag(false)
        binding.smart.dismissOnTouchOutside(false)
        binding.smart.isThreeDrag(false)
        binding.smart.open()
        binding.smart.setOnCloseListener(callback)

        binding.ivClose.setOnClickListener { finish() }

        binding.btnNext.setOnClickListener {
            // 这个才点击返回图片
            directShutdown()
        }


        val adapter = MyPagerAdapter(supportFragmentManager)
        val netWorkFragment = NetworkImagesFragment()
        adapter.addFragment(netWorkFragment, getString(com.cl.common_base.R.string.string_1941))
        adapter.addFragment(LocalImagesFragment(), getString(com.cl.common_base.R.string.string_1443))
        binding.viewPager.adapter = adapter

        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, com.cl.common_base.R.color.mainColor))
        /*for (i in 0 until binding.tabLayout.tabCount) {
            binding.tabLayout.getTabAt(i)?.let {
                var text = it.text.toString()
                text = text.uppercase(Locale.getDefault())
                text = text.lowercase(Locale.getDefault())
                it.text = text
            }
        }*/
        // binding.tabLayout.setSelectedTabIndicatorHeight(resources.getDimensionPixelSize(R.dimen.tab_indicator_height));


        mViewMode.trendHistoryPic.observe(this@ChoosePicActivity, resourceObserver {
            error { errorMsg, code ->
                ToastUtil.shortShow(errorMsg)
                if (netWorkFragment.refreshLayout.isRefreshing) {
                    netWorkFragment.refreshLayout.finishRefresh()
                }
                if (netWorkFragment.refreshLayout.isLoading) {
                    netWorkFragment.refreshLayout.finishLoadMore()
                }
            }

            success {
                // 刷新相关
                if (netWorkFragment.refreshLayout.isRefreshing) {
                    netWorkFragment.refreshLayout.finishRefresh()
                }
                if (netWorkFragment.refreshLayout.isLoading) {
                    // 没有加载了、或者加载完毕
                    if ((data?.records?.size ?: 0) <= 0) {
                        netWorkFragment.refreshLayout.finishLoadMoreWithNoMoreData()
                    } else {
                        netWorkFragment.refreshLayout.finishLoadMore()
                    }
                }
                if (null == this.data) return@success


                logI("123123123: ${data.toString()}")
                //  添加数据相关
                data?.let {
                    val list = mutableListOf<String>()
                    it.records.forEach { records ->
                        list.add(records.imageUrl)
                    }
                    if (list.isEmpty()) return@success
                    netWorkFragment.adapter.setImages(list, it.current)
                }
            }
        })
    }

    // 直接关闭
    private fun directShutdown() {
        if (selectedImages.size != 0) {
            setResult(RESULT_OK, intent.putExtra(ReelPostActivity.KEY_PIC_LIST, selectedImages as? Serializable))
            finish()
        } else {
            finish()
        }
    }


    private val callback by lazy {
        object : SmartDragLayout.OnCloseListener {
            override fun onClose() {
                directShutdown()
            }

            override fun onDrag(y: Int, percent: Float, isScrollUp: Boolean) {
                // binding.smart.alpha = percent
            }

            override fun onOpen() {
            }
        }
    }

    override fun observe() {
    }

    override fun initData() {
    }


    class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val fragments = mutableListOf<Fragment>()
        private val titles = mutableListOf<String>()

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }
    }

    class NetworkImagesFragment : Fragment() {

        private lateinit var recyclerView: RecyclerView
        lateinit var adapter: ImageAdapter
        lateinit var refreshLayout: SmartRefreshLayout
        private var page = 1

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.contact_fragment_network_images, container, false)
            recyclerView = view.findViewById(R.id.networkRecyclerView)
            refreshLayout = view.findViewById(R.id.refreshLayout)
            // 设置refresh的规则
            refreshLayout.apply {
                ClassicsFooter.REFRESH_FOOTER_LOADING = getString(com.cl.common_base.R.string.string_255) //"正在刷新...";
                ClassicsFooter.REFRESH_FOOTER_REFRESHING = getString(com.cl.common_base.R.string.string_255) //"正在加载...";
                ClassicsFooter.REFRESH_FOOTER_NOTHING = getString(com.cl.common_base.R.string.string_256)
                ClassicsFooter.REFRESH_FOOTER_FINISH = getString(com.cl.common_base.R.string.string_257)
                ClassicsFooter.REFRESH_FOOTER_FAILED = getString(com.cl.common_base.R.string.string_258)

                // 刷新监听
                setOnRefreshListener {
                    // 重新加载数据
                    logI("setOnRefreshListener: refresh")
                    page = 1
                    loadImages()
                }
                // 加载更多监听
                setOnLoadMoreListener {
                    page += 1
                    loadImages()
                }
                // 刷新头部局
                setRefreshHeader(ClassicsHeader(context))
                setRefreshFooter(ClassicsFooter(context).setFinishDuration(0))
                // 刷新高度
                setHeaderHeight(60f)
                // 自动刷新
                // autoRefresh()
            }

            recyclerView.layoutManager = FullyGridLayoutManager(context, 4)
            recyclerView.addItemDecoration(
                GridSpaceItemDecoration(
                    4,
                    DensityUtil.dip2px(context, 8f), DensityUtil.dip2px(context, 8f)
                )
            )
            val selectedImages = (activity as? ChoosePicActivity)?.selectedImages ?: mutableListOf()
            adapter = ImageAdapter(requireContext(), selectedImages) { image ->
                if (selectedImages.size < MAX_IMAGE_COUNT || selectedImages.contains(image)) {
                    if (selectedImages.contains(image)) {
                        selectedImages.remove(image)
                        (activity as? ChoosePicActivity)?.updateSelectedIndexes(adapter)
                    } else {
                        selectedImages.add(image)
                        (activity as? ChoosePicActivity)?.updateSelectedIndexes(adapter)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "The number of selected photos cannot exceed $MAX_IMAGE_COUNT.", Toast.LENGTH_SHORT).show()
                }
            }
            recyclerView.adapter = adapter
            // 加载网络图片 Trend
            loadImages()
            return view
        }

        fun loadImages() {
            // 加载
            (activity as? ChoosePicActivity)?.mViewMode?.apply {
                trendHistoryPic(TrendPictureReq(current = page, size = 10))
            }

            /*val images = listOf(
                "https://img1.baidu.com/it/u=1960110688,1786190632&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=281",
                "https://img1.baidu.com/it/u=1960110688,1786190632&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=281",
                "https://img1.baidu.com/it/u=1960110688,1786190632&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=281",
                "https://img1.baidu.com/it/u=1960110688,1786190632&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=281",
                "https://img1.baidu.com/it/u=1960110688,1786190632&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=281",
                "https://img1.baidu.com/it/u=1960110688,1786190632&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=281",
                "https://img1.baidu.com/it/u=1960110688,1786190632&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=281",
                "https://img1.baidu.com/it/u=1960110688,1786190632&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=281",
                "https://img1.baidu.com/it/u=1960110688,1786190632&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=281",
                "https://img1.baidu.com/it/u=1960110688,1786190632&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=281",
            )
            adapter.setImages(images)*/
        }
    }

    class LocalImagesFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

        private lateinit var recyclerView: RecyclerView
        private lateinit var adapter: ImageAdapter

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            logI("onCreateView")
            val view = inflater.inflate(R.layout.contact_fragment_local_images, container, false)
            recyclerView = view.findViewById(R.id.localRecyclerView)
            recyclerView.layoutManager = FullyGridLayoutManager(context, 4)
            recyclerView.addItemDecoration(
                GridSpaceItemDecoration(
                    4,
                    DensityUtil.dip2px(context, 8f), DensityUtil.dip2px(context, 8f)
                )
            )
            val selectedImages = (activity as? ChoosePicActivity)?.selectedImages ?: mutableListOf()
            adapter = ImageAdapter(requireContext(), selectedImages) { image ->
                if (selectedImages.size < MAX_IMAGE_COUNT || selectedImages.contains(image)) {
                    if (selectedImages.contains(image)) {
                        selectedImages.remove(image)
                        (activity as? ChoosePicActivity)?.updateSelectedIndexes(adapter)
                    } else {
                        selectedImages.add(image)
                        (activity as? ChoosePicActivity)?.updateSelectedIndexes(adapter)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "The number of selected photos cannot exceed $MAX_IMAGE_COUNT.", Toast.LENGTH_SHORT).show()
                }
            }
            recyclerView.adapter = adapter
            loadImages()
            return view
        }

        private fun loadImages() {
            logI("loadImages")
            loaderManager.initLoader(0, null, this)
        }

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            logI("onCreateLoader")
            val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
            val selection = "${MediaStore.Images.Media.MIME_TYPE}!='image/gif'"
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            return CursorLoader(requireContext(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  projection,
                selection,
                null,
                sortOrder)
        }

        @SuppressLint("Range")
        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
            val images = mutableListOf<String>()
            if (data != null) {
                while (data.moveToNext()) {
                    // val path = data.getString(data.getColumnIndex(MediaStore.Images.Media.DATA))
                    val path = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                    images.add(path)
                }
            }
            // 游标向前查找过一次，不能进行二次查询
            if (images.size > 0) {
                // 倒序
                images.reverse()
                adapter.setImages(images)
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            logI("onLoaderReset")
            adapter.setImages(mutableListOf())
        }
    }

    class ImageAdapter(private val context: Context, private val selectedImages: List<String>, private val onClickListener: (String) -> Unit) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        private var images = mutableListOf<String>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.contact_imageview, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val image = images[position]
            val requestOptions = RequestOptions()
                .signature(ObjectKey(image))
                .placeholder(com.cl.common_base.R.mipmap.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
            Glide.with(context).load(image).apply(requestOptions).into(holder.imageView)
            if (selectedImages.contains(image)) {
                // holder.imageView.setBackgroundResource(R.drawable.ic_launcher_background)
                holder.tvNumber.visibility = View.VISIBLE
                holder.tvNumber.text = (selectedImages.indexOf(image) + 1).toString()
            } else {
                holder.tvNumber.visibility = View.GONE
                holder.imageView.setBackgroundResource(0)
            }
            holder.itemView.setOnClickListener {
                onClickListener(image)
            }
        }

        override fun getItemCount(): Int {
            return images.size
        }

        fun setImages(images: MutableList<String>) {
            this.images = images
            notifyDataSetChanged()
        }

        fun setImages(images: MutableList<String>, page: Int) {
            if (page == 1) {
                this.images = images
            } else {
                this.images.addAll(images)
            }

            notifyDataSetChanged()
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.iv_imageview)
            val tvNumber: TextView = itemView.findViewById(R.id.tv_number)
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            super.onViewAttachedToWindow(holder)
            Glide.with(context).resumeRequests()
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            Glide.with(context).pauseRequests()
        }
    }

    private fun updateSelectedIndexes(adapter: ImageAdapter) {
        selectedImages.forEachIndexed { index, position ->
            adapter.notifyItemChanged(index)
        }
    }

    companion object {
        // 相片最大选中数量
        const val MAX_IMAGE_COUNT = 16
    }
}