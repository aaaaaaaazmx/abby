package com.cl.common_base.util.file;


import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.util.Objects;

/**
 * 各种目录
 */
public class SDCard {


    /**
     * 获取OTA下载目录
     *
     * @return
     */
    public static String getOtaDownloadDir(Context context, int pid) {
        return getCacheDir(context) + File.separator + "ota" + File.separator + pid + File.separator;
    }

    /**
     * 获取资源修复下载目录
     *
     * @return
     */
    public static String getRepairDownloadDir(Context context) {
        return getCacheDir(context) + File.separator + "repair" + File.separator;
    }


    /**
     * 获取apk下载目录
     *
     * @return
     */
    public static String getApkDownloadDir(Context context) {
        return getCacheDir(context) + File.separator + "app_update" + File.separator;
    }


    /**
     * 获取内部或外部缓存目录
     *
     * @param context
     * @return
     */
    public static String getCacheDir(Context context) {
        String path = null;
        if (isExistSdCard()) {
            File dir = context.getExternalCacheDir();
            if (dir != null) {
                path = dir.getAbsolutePath();
            }
        }

        if (TextUtils.isEmpty(path)) {
            path = context.getCacheDir().getAbsolutePath();
        }
        return path;
    }


    /**
     * 应用下的图片目录
     *
     * @param context
     * @return
     */
    public static String getContextPictureDir(Context context) {
        return getExternalFilesDir(context, Environment.DIRECTORY_PICTURES);
    }


    /**
     * 应用下的视频目录
     *
     * @param context
     * @return
     */
    public static String getContextMoviesDir(Context context) {
        return getExternalFilesDir(context, Environment.DIRECTORY_MOVIES);
    }

    /**
     * 应用下的音乐目录
     *
     * @param context
     * @return
     */
    public static String getContextMusicDir(Context context) {
        return getExternalFilesDir(context, Environment.DIRECTORY_MUSIC);
    }


    /**
     * 应用缓存目录
     *
     * @return
     */
    public static String getContextFileDir(Context context, String dir) {
        return context.getFilesDir().getPath() + File.separator + dir;
    }


    /**
     * 应用指定文件类型目录
     *
     * @return
     */
    public static String getExternalFilesDir(Context context, String type) {
        return Objects.requireNonNull(context.getExternalFilesDir(type)).getAbsolutePath() + File.separator;
    }


    /**
     * 外存储公共下载目录
     *
     * @return
     */
    public static String getPublicDownloadDir() {
        return getExternalStoragePublicDir(Environment.DIRECTORY_DOWNLOADS);
    }


    /**
     * 外部存储根目录
     *
     * @return
     */
    public static String getPublicStorageDir() {
        if (isExistSdCard()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        }
        return null;
    }


    /**
     * 外部存储根目录
     *
     * @return
     */
    public static String getExternalStorageRootDir() {
        if (isExistSdCard()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        }
        return null;
    }


    /**
     * 公共指定文件类型目录
     *
     * @return
     */
    private static String getExternalStoragePublicDir(String type) {
        if (isExistSdCard()) {
            return Environment.getExternalStoragePublicDirectory(type).getPath() + File.separator;
        }
        return null;
    }


    /**
     * 获取指定目录
     *
     * @return
     */
    /**
     * 获取指定目录
     *
     * @param path 指定目录 不加"/" 如需要SD卡下的"/aaa/bbb/ccc"目录，传入"aaa/bbb/ccc"
     * @return
     */
    public static String getExternalStorageDir(String path) {
        if (isExistSdCard()) {
            File dir = new File(getExternalStorageRootDir() + path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir.getAbsolutePath() + File.separator;
        }
        return null;
    }


    /**
     * 判断SD卡是否存在
     *
     * @return
     */
    private static boolean isExistSdCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

}
