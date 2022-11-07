package com.cl.common_base.easeui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hyphenate.chat.AgoraMessage;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.chat.VecConfig;


/**
 * Created by liyuzhao on 11/01/2017.
 */

public class CallReceiver extends BroadcastReceiver {
    boolean mIsOnLine;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ChatClient.getInstance().isLoggedInBefore()){
            return;
        }
        String action = intent.getAction();
        if ("calling.state".equals(action)){
            // 防止正在通话中，又新发来视频请求，isOnLine代表是否接通通话中
            mIsOnLine = intent.getBooleanExtra("state", false);
        }else {
            // 电话
            //call type
//            String type = intent.getStringExtra("type");
//            if ("video".equals(type)){// video call
//                if (!mIsOnLine){
//                    // 新版vec视频客服
//                    if (VecConfig.newVecConfig().isVecVideo()){
//                        VECKitCalling.callingResponse(context, intent);
//                    }else {
//                        // 旧版在线视频
//                        Calling.callingResponse(context, intent);
//                    }
//                }
//            }else if (AgoraMessage.TYPE_ENQUIRYINVITE.equalsIgnoreCase(type)){
//                // 满意度评价
//                VECKitCalling.callingRetry(context, intent.getStringExtra("content"));
//            }

        }

    }
}
