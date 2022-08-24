package com.cl.common_base.util.compress;

import android.content.Context;
import android.net.Uri;

import com.luck.picture.lib.engine.CompressFileEngine;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;

import java.util.ArrayList;



public class ImageFileCompressEngine implements CompressFileEngine {

    @Override
    public void onStartCompress(Context context, ArrayList<Uri> source, OnKeyValueResultCallbackListener call) {
//        Luban.with(context).load(source).ignoreBy(100).setRenameListener(new OnRenameListener() {
//            @Override
//            public String rename(String filePath) {
//                int indexOf = filePath.lastIndexOf(".");
//                String postfix = indexOf != -1 ? filePath.substring(indexOf) : ".jpg";
//                return DateUtils.getCreateFileName("CMP_") + postfix;
//            }
//        }).setCompressListener(new OnCompressListener() {
//            @Override
//            public void onSuccess(File file) {
//                if (call != null) {
//                    call.onCallback(file.getAbsolutePath(), file.getAbsolutePath());
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                if (call != null) {
//                    call.onCallback(null, null);
//                }
//            }
//
//            @Override
//            public void onStart() {
//
//            }
//        }).launch();
    }
}