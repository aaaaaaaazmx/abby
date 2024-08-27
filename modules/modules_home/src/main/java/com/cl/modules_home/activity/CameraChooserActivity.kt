package com.cl.modules_home.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cl.modules_home.R
import com.cl.modules_home.databinding.HomeCameraChooserBinding
import com.bumptech.glide.Glide
import com.cl.common_base.base.BaseActivity
import com.cl.common_base.video.GSYPlayVideoActivity
import com.cl.common_base.widget.decoraion.FullyGridLayoutManager
import com.cl.common_base.widget.decoraion.GridSpaceItemDecoration
import com.google.android.material.tabs.TabLayoutMediator
import com.luck.picture.lib.utils.DensityUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader
import java.io.File

/**
 * camera 相册和视频预览界面
 */
class CameraChooserActivity : BaseActivity<HomeCameraChooserBinding>() {

    // 摄像头设备id
    val cameraDevId by lazy {
        intent.getStringExtra("devId")
    }

    // 本地照片文件目录，
    val sdCardPath by lazy {
        intent.getStringExtra("sdCardPath")
    }

    // 是否是保存sdcard还是相册里面
    val isSaveSdcard by lazy {
        intent.getBooleanExtra("isSaveAlbum", false)
    }

    // 设备的sn号
    val sn by lazy {
        intent.getStringExtra("sn")
    }


    override fun initView() {
        setupViewPagerAndTabs()
    }

    private fun setupViewPagerAndTabs() {
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) getString(com.cl.common_base.R.string.string_1443) else getString(com.cl.common_base.R.string.string_1444)
        }.attach()
    }

    override fun observe() {
    }

    override fun initData() {
    }
}

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            ImageFragment()
        } else {
            VideoFragment()
        }
    }
}


class ImageFragment : Fragment() {

    private lateinit var imageAdapter: ImageAdapter
    private val activity by lazy {
        getActivity() as? CameraChooserActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.home_fragment_image, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        val layoutManager = FullyGridLayoutManager(activity, 4)
        recyclerView.addItemDecoration(
            GridSpaceItemDecoration(
                4,
                DensityUtil.dip2px(context, 4f), DensityUtil.dip2px(context, 2f)
            )
        )
        recyclerView.layoutManager = layoutManager

        // Specify the directory
        val picPath = if (activity?.isSaveSdcard == true) activity?.sdCardPath ?: "" else activity?.sn ?: ""
        imageAdapter = ImageAdapter(picPath)
        recyclerView.adapter = imageAdapter

        return view
    }
}

class VideoFragment : Fragment() {

    private lateinit var videoAdapter: VideoAdapter
    private val activity by lazy {
        getActivity() as? CameraChooserActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.home_fragment_video, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        val layoutManager = FullyGridLayoutManager(activity, 4)
        recyclerView.addItemDecoration(
            GridSpaceItemDecoration(
                4,
                DensityUtil.dip2px(context, 4f), DensityUtil.dip2px(context, 2f)
            )
        )
        recyclerView.layoutManager = layoutManager

        // Specify the directory
        val picPath = if (activity?.isSaveSdcard == true) activity?.sdCardPath ?: "" else activity?.sn ?: ""
        videoAdapter = VideoAdapter(picPath, activity)
        recyclerView.adapter = videoAdapter

        return view
    }
}

class ImageAdapter(directoryPath: String) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private val imagePaths: MutableList<String> = mutableListOf()

    /**
     * 从相册中读取图片或者视频
     */
    private fun fetchImagesAndVideosFromSpecificFolder(folderName: String): List<File> {
        val directory = Environment.getExternalStorageDirectory()
        val file = File(directory, "/Pictures/$folderName")
        val files = file.listFiles { dir, name ->
            name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".mp4")
        }
        return files?.toList() ?: emptyList()
    }

    init {
        if (directoryPath.contains("cache")) {
            val directory = File(directoryPath)
            val files = directory.listFiles()

            files?.let {
                for (file in it) {
                    if (isImageFile(file.path)) {
                        imagePaths.add(file.absolutePath)
                    }
                }
            }
        } else {
            val files = fetchImagesAndVideosFromSpecificFolder(directoryPath)
            for (file in files) {
                if (isImageFile(file.path)) {
                    imagePaths.add(file.absolutePath)
                }
            }
        }
        // 倒序
        imagePaths.reverse()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imagePath = imagePaths[position]
        Glide.with(holder.itemView)
            .load(imagePath)
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            // 图片浏览
            XPopup.Builder(it.context)
                .asImageViewer(
                    (it as? ImageView),
                    position,
                    imagePaths.toList(),
                    OnSrcViewUpdateListener { _, _ -> },
                    SmartGlideImageLoader()
                ).isShowSaveButton(false)
                .show()
        }
    }

    override fun getItemCount(): Int {
        return imagePaths.size
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
    }

    private fun isImageFile(path: String): Boolean {
        val imageExtensions = arrayOf("jpg", "png", "gif", "jpeg")
        val extension = path.substring(path.lastIndexOf(".") + 1)
        return imageExtensions.contains(extension)
    }
}

class VideoAdapter(directoryPath: String, val context: Context?) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private val videoPaths: MutableList<String> = mutableListOf()

    /**
     * 从相册中读取图片或者视频
     */
    private fun fetchImagesAndVideosFromSpecificFolder(folderName: String): List<File> {
        val directory = Environment.getExternalStorageDirectory()
        val file = File(directory, "/Pictures/$folderName")
        val files = file.listFiles { dir, name ->
            name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".mp4")
        }
        return files?.toList() ?: emptyList()
    }


    init {
        if (directoryPath.contains("cache")) {
            // 表示是存在本地的
            val directory = File(directoryPath)
            val files = directory.listFiles()

            files?.let {
                for (file in it) {
                    if (isVideoFile(file.path)) {
                        videoPaths.add(file.absolutePath)
                    }
                }
            }
        } else {
            fetchImagesAndVideosFromSpecificFolder(directoryPath).forEach {
                if (isVideoFile(it.path)) {
                    videoPaths.add(it.absolutePath)
                }
            }
        }
        videoPaths.reverse()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoPath = videoPaths[position]
        // You need to use a library or some other method to load video thumbnails
        Glide.with(holder.itemView)
            .load(videoPath) // Uri of the video
            .into(holder.videoView)

        holder.videoView.setOnClickListener {
            // url
            context?.let {
                it.startActivity(Intent(it, GSYPlayVideoActivity::class.java).apply {
                    // putExtra("url", "https://res.exexm.com/cw_145225549855002")
                    putExtra("url", videoPath)
                })
            }
        }
    }

    override fun getItemCount(): Int {
        return videoPaths.size
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoView: ImageView = itemView.findViewById(R.id.video_view)
    }

    private fun isVideoFile(path: String): Boolean {
        val videoExtensions = arrayOf("mp4", "3gp", "mkv", "avi", "flv")
        val extension = path.substring(path.lastIndexOf(".") + 1)
        return videoExtensions.contains(extension)
    }
}
