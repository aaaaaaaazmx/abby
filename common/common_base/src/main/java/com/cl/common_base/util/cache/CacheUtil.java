package com.cl.common_base.util.cache;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.shuyu.gsyvideoplayer.GSYVideoManager;

import java.io.File;

public class CacheUtil {
    /**
     * 获取整体缓存大小
     *
     * @param context
     * @return
     * @throws Exception
     */
    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public static String getTotalCacheSize(Context context) throws Exception {
        long cacheSize = getFolderSize(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.getExternalCacheDir());
        }
        return getFormatSize(cacheSize);
    }

    /**
     * 获取文件
     * Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
     * Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化单位
     *
     * @param size
     */
    public static String getFormatSize(long size) {
        long kb = size / 1024;
        int m = (int) (kb / 1024);
        int kbs = (int) (kb % 1024);
        // 保留一位小数
        if (kbs < 10) {
            return m + "M";
        } else if (kbs >= 10) {
            String kbsString = String.valueOf(kbs);
            if (kbsString.length() == 1) {
                return m + "." + kbsString + "M";
            } else {
                String substring = kbsString.substring(0, 1);
                return m + "." + substring + "M";
            }
        }
        return null;
    }

    /**
     * 清空方法
     *
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public static void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir());
        }
    }

    /**
     * 删除视频缓存
     */
    public static void clearVideoCache(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            deleteDir(new File(context.getCacheDir().toPath() + "/video-cache"));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public static String getVideoCache(Context context) throws Exception {
        long cacheSize = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            cacheSize = getFolderSize(new File(context.getCacheDir().toPath() + "/exo"));
        }
        return getFormatSize(cacheSize);
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}