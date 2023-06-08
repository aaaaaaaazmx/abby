package com.cl.modules_contact.util;

import android.content.Context;

import com.cl.common_base.util.file.SDCard;

import java.io.File;


/**
 * 设备模块一些常量值
 *
 * @author lijiewen
 * @date on 2021/10/13
 */
public class DeviceConstants {

    public static final String TAG_DEVICE = "groot-device";


    /**
     * 云端文件缓存根目录
     *
     * @param context
     * @return
     */
    public static String getCloudFolder(Context context) {
        return SDCard.getCacheDir(context) + File.separator + "cloud" + File.separator;
    }

    /**
     * 安装表盘时临时文件跟目录
     *
     * @param context
     * @return
     */
    public static String getDialFolder(Context context) {
        return SDCard.getCacheDir(context) + File.separator + "dial" + File.separator;
    }


    /**
     * 风格图片保存目录
     *
     * @param context
     * @return
     */
    public static String getCloudDialStyleImageFolder(Context context) {
        return getCloudFolder(context)  + "styleImages" + File.separator;
    }

    /**
     * 手环云端表盘图片保存目录
     *
     * @param context
     * @return
     */
    public static String getCloudDialBandImageFolder(Context context) {
        return getCloudFolder(context)  + "band" + File.separator;
    }


    /**
     * 计算美学目录
     *
     * @param context
     * @return
     */
    public static String getDialStyleFolder(Context context) {
        return getDialFolder(context) + "style" + File.separator;
    }


    /**
     * 模块化目录
     *
     * @param context
     * @return
     */
    public static String getDialModuleFolder(Context context) {
        return getDialFolder(context) + "module" + File.separator;
    }


    /**
     * 云端基础表盘目录
     *
     * @param context
     * @return
     */
    public static String getDialCloudBasicFolder(Context context) {
        return getDialFolder(context) + "basic" + File.separator;
    }

    /**
     * 自定义表盘目录
     *
     * @param context
     * @return
     */
    public static String getDialCustomFolder(Context context) {
        return getDialFolder(context) + "custom" + File.separator;
    }


    /**
     * 裁剪图目录
     *
     * @param context
     * @return
     */
    public static String getDialCropFolder(Context context) {
        return getDialFolder(context) + "crop" + File.separator;
    }


    /**
     * 云端表盘tar包保存目录
     *
     * @param context
     * @return
     */
    public static String getDialCloudTarDownloadFolder(Context context) {
        return getDialCloudBasicFolder(context) + "tar" + File.separator;
    }


    /**
     * 自定义表盘预览图目录
     *
     * @param context
     * @return
     */
    public static String getDialThumbPath(Context context) {
        return getDialCustomFolder(context) + "clock_face" + File.separator + "Thumb.png";
    }

    /**
     * 获取表盘预览缩略图地址
     *
     * @param context
     * @return
     */
    public static String getDialStrokeThumbPath(Context context) {
        return getDialCustomFolder(context) + "clock_face" + File.separator + "StrokeThumb.png";
    }

    /**
     * 获取表盘预览缩略图ZIP地址
     *
     * @param context
     * @return
     */
    public static String getDialStrokeThumbZipPath(Context context) {
        return getDialCustomFolder(context) + "clock_face_zip" + File.separator + "StrokeThumb.zip";
    }

    /**
     * 获取表盘预览ZIP解压之后的地址
     *
     * @param context
     * @return
     */
    public static String getDialZipStrokeThumbPath(Context context) {
        return getDialCustomFolder(context) + "clock_face_zip" + File.separator + "StrokeThumb" +  File.separator +  "StrokeThumb.png";
    }

    /**
     * 获取表盘相册多图地址
     *
     * @param context
     * @return
     */
    public static String getDialPhotoPath(Context context) {
        return getDialCustomFolder(context) + "clock_face_photo";
    }

    /**
     * 获取表盘存储视频地址
     *
     * @param context
     * @return
     */
    public static String getDialVideoPath(Context context) {
        return getDialCustomFolder(context) + "clock_face_video" + File.separator + "pngToGif";
    }

    /**
     * 获取自定义表盘tar地址
     *
     * @param context
     * @return
     */
    public static String getDialCustomTar(Context context) {
        return getDialCustomFolder(context) + "clock_face_tar" + File.separator;
    }

    /**
     * 获取自定义表盘gif地址
     *
     * @param context
     * @return
     */
    public static String getDialCustomGif(Context context) {
        return getDialCustomFolder(context) + "clock_face_gif" + File.separator;
    }

    /**
     * 获取自定义表盘gif生成设备所需格式地址
     *
     * @param context
     * @return
     */
    public static String getDialCustomGifAnim(Context context) {
        return getDialCustomFolder(context) + "clock_face_gif" + File.separator + "robot.anim";
    }
}
