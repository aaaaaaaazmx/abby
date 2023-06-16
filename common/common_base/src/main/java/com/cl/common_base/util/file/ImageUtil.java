package com.cl.common_base.util.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.view.View;

import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.cl.common_base.constants.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {

    /**
     * Uri转bitmap
     *
     * @param context
     * @param uri
     * @return
     */
    public static Bitmap decodeBitmap(Context context, Uri uri) throws Exception {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        ParcelFileDescriptor parcelFileDescriptor = null;
        parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        if (parcelFileDescriptor != null) {

            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor(), null, options);

            //判断是否需要旋转90度（三星手机拍照或者选择照片后返回来的图片居然转了90度）
            int degree = 0;
            ExifInterface exif = new ExifInterface(parcelFileDescriptor.getFileDescriptor());
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
                    default:
                        degree = 0;
                }
            }
            if (degree > 0) {
                bitmap = rotateBitmapByDegree(bitmap, degree);
            }
            parcelFileDescriptor.close();
            return bitmap;
        }
        return null;
    }


    /**
     * Uri转bitmap
     *
     * @param context
     * @param uri
     * @return
     */
    public static Bitmap rotationZoomingDecodeBitmap(Context context, Uri uri) throws Exception {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        //判断是否需要旋转90度（三星手机拍照或者选择照片后返回来的图片居然转了90度）
        int degree = getExifOrientation(context, uri);
        if (degree > 0) {
            bitmap = rotateBitmapByDegree(bitmap, degree);
        }

        if (degree > 0) {
            return getTargetBitmap(bitmap, Constants.Global.KEY_GIF_WIDTH, Constants.Global.KEY_GIF_HEIGHT);
        } else {
            // 缩放
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();
            if (imageWidth < Constants.Global.KEY_GIF_WIDTH || imageHeight < Constants.Global.KEY_GIF_HEIGHT) {
                return bitmap;
            } else {
                // 需要缩小的
                float scaleFactor = Math.min(1f, Math.min(Constants.Global.KEY_GIF_WIDTH / imageWidth, Constants.Global.KEY_GIF_HEIGHT / imageHeight));
                Matrix matrix = new Matrix();
                matrix.postScale(scaleFactor, scaleFactor);
                return Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageHeight, matrix, true);
            }
        }
    }

    private static int getExifOrientation(Context context, Uri uri) throws IOException {
        ExifInterface exif = new ExifInterface(context.getContentResolver().openInputStream(uri));
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    private static Bitmap getTargetBitmap(Bitmap bitmap, int targetWidth, int targetHeight) {
        float scaleWidth = ((float) targetWidth) / bitmap.getWidth();
        float scaleHeight = ((float) targetHeight) / bitmap.getHeight();
        float scaleFactor = Math.min(scaleWidth, scaleHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleFactor, scaleFactor);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return scaledBitmap;
    }


    /**
     * 查询图片旋转角度
     *
     * @param uri
     * @return
     */
    public static int getBitmapDegree(Context context, Uri uri) {
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
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
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
                    default:
                }

            }
        }
        return degree;
    }


    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * 保存bitmap到本地
     *
     * @param bitmap
     * @param path
     * @return
     */
    public static boolean saveBitmap(Bitmap bitmap, String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        } else {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 矩形将图片的四角圆化
     *
     * @param bitmap 传入Bitmap对象
     * @param radius 角度
     * @return 圆角化后的Bitmap
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float radius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        // 画布
        Canvas canvas = new Canvas(output);
        // 将画布的四角圆化
        final int color = Color.RED;
        final Paint paint = new Paint();
        // 得到与图像相同大小的区域 由构造的四个值决定区域的位置以及大小
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // drawRoundRect的第2,3个参数一样则画的是正圆的一角，如果数值不同则是椭圆的一角
        canvas.drawRoundRect(rectF, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * 使用Matrix
     *
     * @param bitmap 传入Bitmap对象
     * @param width  目标宽度
     * @param height 目标高度
     * @return 缩放后的Bitmap
     */
    public static Bitmap getTargetBitmap(Bitmap bitmap, float width, float height) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float scaleW = width / w;
        float scaleH = height / h;

        Matrix matrix = new Matrix();
        // 长和宽放大缩小的比例
        matrix.postScale(scaleW, scaleH);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }


    public static void captureView(View view, String savePath) {
        view.setDrawingCacheEnabled(true);
        try {
            File myCaptureFile = new File(savePath);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            view.getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        view.setDrawingCacheEnabled(false);
    }


    public static Bitmap captureView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        Drawable bgDrawable = v.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(c);
        } else {
            c.drawColor(Color.TRANSPARENT);
        }
        v.draw(c);
        return b;
    }


    /**
     * 叠加bitmap
     *
     * @param background 背景
     * @param foreground 前景
     * @param with       目标宽度
     * @param height     目标高度
     * @return
     */
    public static Bitmap combineBitmap(Bitmap background, Bitmap foreground, int with, int height) {
        if (background == null || foreground == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(with, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(background, (with - background.getWidth()) / 2f, (height - background.getHeight()) / 2f, null);
        canvas.drawBitmap(foreground, (with - foreground.getWidth()) / 2f, (height - foreground.getHeight()) / 2f, null);
        canvas.save();
        canvas.restore();
        return bitmap;
    }


    /**
     * 叠加bitmap
     *
     * @param bit
     * @param with   目标宽度
     * @param height 目标高度
     * @return
     */
    public static Bitmap copyBitmap(Bitmap bit, int with, int height) {
        if (bit == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(with, height, bit.getConfig());
        for (int x = 0; x < bit.getWidth(); x++) {
            for (int y = 0; y < bit.getHeight(); y++) {
                bitmap.setPixel(x, y, bit.getPixel(x, y));
            }
        }
        return bitmap;
    }

}