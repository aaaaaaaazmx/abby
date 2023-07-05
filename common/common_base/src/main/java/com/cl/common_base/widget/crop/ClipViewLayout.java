package com.cl.common_base.widget.crop;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.exifinterface.media.ExifInterface;

import com.cl.common_base.R;
import com.cl.common_base.handler.GlobalHandler;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

/**
 * @date on 2019-07-16.
 */
public class ClipViewLayout extends RelativeLayout {
    /**
     * 裁剪原图
     */
    private ImageView imageView;
    //裁剪框
    private ClipView clipView;
    //裁剪框水平方向间距，xml布局文件中指定
    private float mHorizontalPadding;
    //裁剪框的宽度
    private int mClipViewWidth;
    //裁剪框的高度
    private int mClipViewHeight;
    //裁剪框垂直方向间距，计算得出
    private float mVerticalPadding;
    //图片缩放、移动操作矩阵
    private Matrix matrix = new Matrix();
    //图片原来已经缩放、移动过的操作矩阵
    private Matrix savedMatrix = new Matrix();
    //动作标志：无
    private static final int NONE = 0;
    //动作标志：拖动
    private static final int DRAG = 1;
    //动作标志：缩放
    private static final int ZOOM = 2;
    //初始化动作标志
    private int mode = NONE;
    //记录起始坐标
    private PointF start = new PointF();
    //记录缩放时两指中间点坐标
    private PointF mid = new PointF();
    private float oldDist = 1f;
    //用于存放矩阵的9个值
    private final float[] matrixValues = new float[9];
    //最小缩放比例
    private float minScale;
    //最大缩放比例
    private float maxScale = 4;
    private int mClipType;

    private ProgressBar progressBar;

    public ClipViewLayout(Context context) {
        this(context, null);
    }

    public ClipViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    //初始化控件自定义的属性
    public void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.UserClipViewLayout);

        //获取剪切框距离左右的边距, 默认为50dp
        mHorizontalPadding = array.getDimensionPixelSize(R.styleable.UserClipViewLayout_userHorizontalPadding,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
        //获取裁剪框边框宽度，默认1dp
        int clipBorderWidth = array.getDimensionPixelSize(R.styleable.UserClipViewLayout_userClipBorderWidth,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        //裁剪框类型(圆或者矩形)
        mClipType = array.getInt(R.styleable.UserClipViewLayout_userClipType, 1);

        //获取剪切框的宽度, 默认为120dp
        mClipViewWidth = array.getDimensionPixelSize(R.styleable.UserClipViewLayout_userRectangleWidth,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics()));
        //获取剪切框的高度, 默认为240dp
        mClipViewHeight = array.getDimensionPixelSize(R.styleable.UserClipViewLayout_userRectangleHeight,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, getResources().getDisplayMetrics()));
        //回收
        array.recycle();
        clipView = new ClipView(context);
        //设置裁剪框类型
        clipView.setClipType(mClipType == 1 ? ClipView.ClipType.CIRCLE : ClipView.ClipType.RECTANGLE);
        //设置剪切框边框
        clipView.setClipBorderWidth(clipBorderWidth);
        //设置剪切框水平间距
        clipView.setmHorizontalPadding(mHorizontalPadding);
        //设置剪切框的宽高
        clipView.setClipWidthAndHeight(mClipViewWidth, mClipViewHeight);

        imageView = new ImageView(context);

        progressBar = new ProgressBar(context);

        // progressBar.setVisibility(View.GONE);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        progressBar.setLayoutParams(params);
        this.addView(progressBar, params);

        //相对布局布局参数
        android.view.ViewGroup.LayoutParams lp = new LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(imageView, lp);
        this.addView(clipView, lp);

    }

    public void setClipViewWidthAndHeiht(int clipViewWidth, int clipViewHeight) {
        this.mClipViewWidth = clipViewWidth;
        this.mClipViewHeight = clipViewHeight;
        if (clipView != null) {
            clipView.setClipWidthAndHeight(mClipViewWidth, mClipViewHeight);
        }
    }

    /**
     * 初始化图片
     */
    public void setImageSrc1(final Uri uri) {
        //需要等到imageView绘制完毕再初始化原图
        ViewTreeObserver observer = imageView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initSrcPic1(uri);
                imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }


    public void setImageSrc2(final Uri uri, OnLoadFinishListener listener) {
        this.setImageSrc2(uri, true, listener);
    }

    public void setImageSrc2(final Uri uri, boolean reScale, OnLoadFinishListener listener) {
        //需要等到imageView绘制完毕再初始化原图
        if (getWidth() > 0) {
            initSrcPic2(uri, reScale, listener);
        } else {
            ViewTreeObserver observer = imageView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    initSrcPic2(uri, reScale, listener);
                    imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }
    }

    /**
     * 初始化图片
     * step 1: decode 出 720*1280 左右的照片  因为原图可能比较大 直接加载出来会OOM
     * step 2: 将图片缩放 移动到imageView 中间
     */
    public void initSrcPic1(Uri uri) {
        if (uri == null) {
            return;
        }

        //原图可能很大，现在手机照出来都3000*2000左右了，直接加载可能会OOM
        //这里decode出720*1280 左右的照片
        Bitmap bitmap = decodeSampledBitmap(getContext(), uri, 720, 1280);
        if (bitmap == null) {
            return;
        }

        //竖屏拍照的照片，直接使用的话，会旋转90度，下面代码把角度旋转过来
        //查询旋转角度
        int rotation = getExifOrientation(getContext(), uri);
        Matrix m = new Matrix();
        m.setRotate(rotation);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

        //图片的缩放比
        float scale;
        //宽图
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            scale = (float) imageView.getWidth() / bitmap.getWidth();
            //如果高缩放后小于裁剪区域 则将裁剪区域与高的缩放比作为最终的缩放比
            Rect rect = clipView.getClipRect1();
            //高的最小缩放比
            minScale = rect.height() / (float) bitmap.getHeight();
            if (scale < minScale) {
                scale = minScale;
            }
        } else {//高图
            //高的缩放比
            scale = (float) imageView.getHeight() / bitmap.getHeight();
            //如果宽缩放后小于裁剪区域 则将裁剪区域与宽的缩放比作为最终的缩放比
            Rect rect = clipView.getClipRect1();
            //宽的最小缩放比
            minScale = rect.width() / (float) bitmap.getWidth();
            if (scale < minScale) {
                scale = minScale;
            }
        }
        // 缩放
        matrix.postScale(scale, scale);
        // 平移,将缩放后的图片平移到imageview的中心
        //imageView的中心x
        int midX = imageView.getWidth() / 2;
        //imageView的中心y
        int midY = imageView.getHeight() / 2;
        //bitmap的中心x
        int imageMidX = (int) (bitmap.getWidth() * scale / 2);
        //bitmap的中心y
        int imageMidY = (int) (bitmap.getHeight() * scale / 2);
        matrix.postTranslate(midX - imageMidX, midY - imageMidY);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        imageView.setImageMatrix(matrix);
        imageView.setImageBitmap(bitmap);
    }


    public void initSrcPic2(Uri uri, boolean reScale, OnLoadFinishListener listener) {
        if (uri == null) {
            return;
        }

        Handler workerHandler = GlobalHandler.getGlobalWorkerHandler();
        Handler mainHandler = GlobalHandler.getGlobalUiHandler();

        workerHandler.post(() -> {
            Bitmap bitmap = getSrcPicOnWorkThread(uri);
            if (bitmap != null) {
                mainHandler.post(() -> {
                    setBitmapInImageView(bitmap, reScale);
                    progressBar.setVisibility(View.GONE);
                    if (listener != null) {
                        listener.onLoadFinish();
                    }
                });
            }
        });
        progressBar.setVisibility(View.VISIBLE);
    }

    public void setLoading() {
        imageView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    // 在子线程中进行图片处理
    private Bitmap getSrcPicOnWorkThread(Uri uri) {
        //原图可能很大，现在手机照出来都3000*2000左右了，直接加载可能会OOM
        //这里decode出720*1280 左右的照片
        Bitmap bitmap = decodeSampledBitmap(getContext(), uri, 720, 1280);
        if (bitmap == null) {
            return null;
        }

        // 竖屏拍照的照片，直接使用的话，会旋转90度，下面代码把角度旋转过来
        int rotation = getExifOrientation(getContext(), uri); //查询旋转角度
        Matrix m = new Matrix();
        m.setRotate(rotation);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        return bitmap;
    }

    // 在主线程中现实图片
    private void setBitmapInImageView(Bitmap bitmap, boolean reScale) {
        //图片的缩放比
        float scale;
        Rect clipRect2 = clipView.getClipRect2();
        // 上面这个取的最小缩放度是基于正方形的裁剪框，所以随便取宽高的任何一个比例来进行缩放都会正常
        // 但是咱们这个不是正方形的裁剪框,是高大于宽的长方形
        int clipHeight = clipRect2.height();
        int clipWidth = clipRect2.width();
        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();
        if (bitmap.getWidth() >= bitmap.getHeight()) {//bitmap是宽图
            scale = (float) imageView.getWidth() / bitmap.getWidth();
            //如果高缩放后小于裁剪区域 则将裁剪区域与高的缩放比作为最终的缩放比
            //高的最小缩放比
            minScale = clipRect2.height() / (float) bitmap.getHeight();

            if ((minScale * bitmapWidth) < clipWidth) {
                // 如果小于咱们就取他的高度为最小缩放比
                minScale = clipRect2.width() / (float) bitmap.getWidth();
            }
        } else {//bitmap是高图
            //高的缩放比
            scale = (float) imageView.getHeight() / bitmap.getHeight();
            //如果宽缩放后小于裁剪区域 则将裁剪区域与宽的缩放比作为最终的缩放比
            //宽的最小缩放比
            minScale = clipRect2.width() / (float) bitmap.getWidth();

            // 宽肯定是适应这个长方形的，因为他取的是宽的缩放比
            // 所以咱们需要判断高在缩放之后是否小于这个裁剪框的高度
            // 进行高的适配，然后在来选择到底是取哪边的缩放比
            if ((minScale * bitmapHeight) < clipHeight) {
                // 如果小于咱们就取他的高度为最小缩放比
                minScale = clipRect2.height() / (float) bitmap.getHeight();
            }
        }

        Log.i("ClipViewLayout", "setBitmapInImageView: minScale: " + minScale + ",,," + bitmap.getWidth() + ",,," + bitmap.getHeight());
        Log.i("ClipViewLayout", "setBitmapInImageView: " + clipView.getClipRect2().width() + ",,," + clipView.getClipRect2().height());


        if (scale < minScale) {
            scale = minScale;
        }

        if (reScale) {
            matrix.reset();
            // 缩放
            // 设置最小缩放大小
            // 设置默认缩放大小：scale
            matrix.postScale(minScale, minScale);
        }
        // 平移,将缩放后的图片平移到imageview的中心
        //imageView的中心x
        int midX = imageView.getWidth() / 2;
        //imageView的中心y
        int midY = imageView.getHeight() / 2;
        //bitmap的中心x
        int imageMidX = (int) (bitmap.getWidth() * minScale / 2);
        //bitmap的中心y
        int imageMidY = (int) (bitmap.getHeight() * minScale / 2);
        matrix.postTranslate(midX - imageMidX, midY - imageMidY);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        imageView.setImageMatrix(matrix);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * 查询图片旋转角度
     *
     * @param uri
     * @return
     */
    public static int getExifOrientation(Context context, Uri uri) {// YOUR MEDIA PATH AS STRING
        int degree = 0;
        ExifInterface exif = null;
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                exif = new ExifInterface(parcelFileDescriptor.getFileDescriptor());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                parcelFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }


    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                //设置开始点位置
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //开始放下时候两手指间的距离
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) { //拖动
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    if (mClipType == 1) {
                        mVerticalPadding = clipView.getClipRect1().top;
                        mHorizontalPadding = clipView.getClipRect1().left;
                    } else {
                        mVerticalPadding = clipView.getClipRect2().top;
                        mHorizontalPadding = clipView.getClipRect2().left;
                    }

                    matrix.postTranslate(dx, dy);
                    //检查边界
                    checkBorder();
                } else if (mode == ZOOM) { //缩放
                    //缩放后两手指间的距离
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        //手势缩放比例
                        float scale = newDist / oldDist;
                        if (scale < 1) { //缩小
                            if (getScale() > minScale) {
                                matrix.set(savedMatrix);
                                if (mClipType == 1) {
                                    mVerticalPadding = clipView.getClipRect1().top;
                                    mHorizontalPadding = clipView.getClipRect1().left;
                                } else {
                                    mVerticalPadding = clipView.getClipRect2().top;
                                    mHorizontalPadding = clipView.getClipRect2().left;
                                }
                                matrix.postScale(scale, scale, mid.x, mid.y);
                                //缩放到最小范围下面去了，则返回到最小范围大小
                                while (getScale() < minScale) {
                                    //返回到最小范围的放大比例
                                    scale = 1 + 0.01F;
                                    matrix.postScale(scale, scale, mid.x, mid.y);
                                }
                            }
                            //边界检查
                            checkBorder();
                        } else { //放大
                            if (getScale() <= maxScale) {
                                matrix.set(savedMatrix);
                                mVerticalPadding = clipView.getClipRect2().top;
                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                        }
                    }
                }
                imageView.setImageMatrix(matrix);
                break;
        }
        return true;
    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     */
    private RectF getMatrixRectF(Matrix matrix) {
        RectF rect = new RectF();
        Drawable d = imageView.getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    /**
     * 边界检测
     */
    private void checkBorder() {
        RectF rect = getMatrixRectF(matrix);
        float deltaX = 0;
        float deltaY = 0;
        int width = imageView.getWidth();
        int height = imageView.getHeight();
        // 如果宽或高大于屏幕，则控制范围 ; 这里的0.001是因为精度丢失会产生问题，但是误差一般很小，所以我们直接加了一个0.01
        if (rect.width() + 0.01 >= width - 2 * mHorizontalPadding) {
            if (rect.left > mHorizontalPadding) {
                deltaX = -rect.left + mHorizontalPadding;
            }
            if (rect.right < width - mHorizontalPadding) {
                deltaX = width - mHorizontalPadding - rect.right;
            }
        }
        if (rect.height() + 0.01 >= height - 2 * mVerticalPadding) {
            if (rect.top > mVerticalPadding) {
                deltaY = -rect.top + mVerticalPadding;
            }
            if (rect.bottom < height - mVerticalPadding) {
                deltaY = height - mVerticalPadding - rect.bottom;
            }
        }
        matrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 获得当前的缩放比例
     */
    public final float getScale() {
        matrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }


    /**
     * 多点触控时，计算最先放下的两指距离
     */
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 多点触控时，计算最先放下的两指中心坐标
     */
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    /**
     * 获取剪切图
     */
    public Bitmap clip1() {
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Rect rect = clipView.getClipRect1();
        Bitmap cropBitmap = null;
        Bitmap zoomedCropBitmap = null;
        try {
            cropBitmap = Bitmap.createBitmap(imageView.getDrawingCache(), rect.left, rect.top, rect.width(), rect.height());
            zoomedCropBitmap = zoomBitmap(cropBitmap, 200, 200);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cropBitmap != null) {
            cropBitmap.recycle();
        }
        // 释放资源
        imageView.destroyDrawingCache();
        return zoomedCropBitmap;
    }

    public Bitmap clip2() {
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Rect rect = clipView.getClipRect2();
        Bitmap cropBitmap = null;
        Bitmap zoomedCropBitmap = null;
        try {
            cropBitmap = Bitmap.createBitmap(imageView.getDrawingCache(), rect.left, rect.top, rect.width(), rect.height());
            zoomedCropBitmap = zoomBitmap(cropBitmap, mClipViewWidth, mClipViewHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cropBitmap != null) {
            cropBitmap.recycle();
        }
        // 释放资源
        imageView.destroyDrawingCache();
        return zoomedCropBitmap;
    }


    /**
     * 图片等比例压缩
     *
     * @param filePath
     * @param reqWidth  期望的宽
     * @param reqHeight 期望的高
     * @return
     */
    public static Bitmap decodeSampledBitmap(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
        //bitmap is null
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 图片等比例压缩
     *
     * @param filePath
     * @param reqWidth  期望的宽
     * @param reqHeight 期望的高
     * @return
     */
    public static Bitmap decodeSampledBitmap(Context context, Uri filePath, int reqWidth, int reqHeight) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(filePath, "r");
            if (parcelFileDescriptor != null) {

                final BitmapFactory.Options options = new BitmapFactory.Options();
                //只读取图片信息，不读取实际数据
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor(), null, options);
                //计算缩放比
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                //再按缩放比读取实际数据
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor(), null, options);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }
        return null;
    }

    /**
     * 计算InSampleSize
     * 宽的压缩比和高的压缩比的较小值  取接近的2的次幂的值
     * 比如宽的压缩比是3 高的压缩比是5 取较小值3  而InSampleSize必须是2的次幂，取接近的2的次幂4
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            int ratio = heightRatio < widthRatio ? heightRatio : widthRatio;
            // inSampleSize只能是2的次幂  将ratio就近取2的次幂的值
            if (ratio < 3) {
                inSampleSize = ratio;
            } else if (ratio < 6.5) {
                inSampleSize = 4;
            } else if (ratio < 8) {
                inSampleSize = 8;
            } else {
                inSampleSize = ratio;
            }
        }

        return inSampleSize;
    }

    /**
     * 图片缩放到指定宽高
     * <p/>
     * 非等比例压缩，图片会被拉伸
     *
     * @param bitmap 源位图对象
     * @param w      要缩放的宽度
     * @param h      要缩放的高度
     * @return 新Bitmap对象
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        return newBmp;
    }


    public static FileDescriptor getRealFilePathFromUri(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        try {
            return Objects.requireNonNull(context.getContentResolver().openFileDescriptor(uri, "r")).getFileDescriptor();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static interface OnLoadFinishListener {
        public void onLoadFinish();
    }

    public ClipView getClipView() {
        return clipView;
    }

    public void setClipView(ClipView clipView) {
        this.clipView = clipView;
    }
}