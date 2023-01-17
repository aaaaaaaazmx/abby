/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cl.common_base.easeui.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.cl.common_base.R;
import com.cl.common_base.bean.UserinfoBean;
import com.cl.common_base.constants.Constants;
import com.cl.common_base.easeui.help.DemoMessageHelper;
import com.cl.common_base.util.Prefs;
import com.cl.common_base.util.json.GSON;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.chat.Conversation;
import com.hyphenate.helpdesk.callback.Callback;
import com.hyphenate.helpdesk.easeui.Constant;
import com.hyphenate.helpdesk.easeui.util.Config;
import com.hyphenate.helpdesk.easeui.util.IntentBuilder;
import com.hyphenate.helpdesk.easeui.widget.ToastHelper;
import com.hyphenate.helpdesk.model.VisitorInfo;


public class EaseUiActivity extends AppCompatActivity {

	private static final String TAG = "LoginActivity";

	private boolean progressShow;
	private ProgressDialog progressDialog;
	private String message;
	private UserinfoBean userinfoBean;
	//	private int selectedIndex = Constant.INTENT_CODE_IMG_SELECTED_DEFAULT;
//	private int messageToIndex = Constant.MESSAGE_TO_DEFAULT;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Intent intent = getIntent();
		message = intent.getStringExtra(Config.EXTRA_MESSAGE);
//		selectedIndex = intent.getIntExtra(Constant.INTENT_CODE_IMG_SELECTED_KEY,
//				Constant.INTENT_CODE_IMG_SELECTED_DEFAULT);
//		messageToIndex = intent.getIntExtra(Constant.MESSAGE_TO_INTENT_EXTRA, Constant.MESSAGE_TO_DEFAULT);
		String string = Prefs.INSTANCE.getString(Constants.Login.KEY_LOGIN_DATA, "");
		userinfoBean = GSON.parseObject(string, UserinfoBean.class);

		//ChatClient.getInstance().isLoggedInBefore() 可以检测是否已经登录过环信，如果登录过则环信SDK会自动登录，不需要再次调用登录操作
		if (ChatClient.getInstance().isLoggedInBefore()) {
			progressDialog = getProgressDialog();
			progressDialog.setMessage(getResources().getString(R.string.is_contact_customer));
			progressDialog.show();
			toChatActivity();
		} else {
			// todo 登录环信、随机创建一个用户并登录环信服务器
			if (null != userinfoBean) {
				login(userinfoBean.getEasemobUserName(), userinfoBean.getEasemobPassword());
			}
		}

	}



	private ProgressDialog getProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(EaseUiActivity.this);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					progressShow = false;
				}
			});
		}
		return progressDialog;
	}

	private void login(final String uname, final String upwd) {
		progressShow = true;
		progressDialog = getProgressDialog();
		progressDialog.setMessage(getResources().getString(R.string.is_contact_customer));
		if (!progressDialog.isShowing()) {
			if (isFinishing()){
				return;
			}
			progressDialog.show();
		}
		// login huanxin server
		ChatClient.getInstance().login(uname, upwd, new Callback() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "demo login success!");
				if (!progressShow) {
					return;
				}
				toChatActivity();
			}

			@Override
			public void onError(int code, String error) {
				Log.e(TAG, "login fail,code:" + code + ",error:" + error);
				if (!progressShow) {
					return;
				}
				runOnUiThread(new Runnable() {
					public void run() {
						progressDialog.dismiss();
						ToastHelper.show(getBaseContext(), R.string.is_contact_customer_failure_seconed);
						finish();
					}
				});
			}

			@Override
			public void onProgress(int progress, String status) {

			}
		});
	}

	private void toChatActivity() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!EaseUiActivity.this.isFinishing())
					progressDialog.dismiss();

				// 获取华为 HMS 推送 token
				// HMSPushHelper.getInstance().getHMSToken(EaseUiActivity.this);

				//此处演示设置技能组,如果后台设置的技能组名称为[shouqian|shouhou],这样指定即分配到技能组中.
				//为null则不按照技能组分配,同理可以设置直接指定客服scheduleAgent
				String queueName = "shouhou";
//				switch (messageToIndex){
//					case Constant.MESSAGE_TO_AFTER_SALES:
//						queueName = "shouhou";
//						break;
//					case Constant.MESSAGE_TO_PRE_SALES:
//						queueName = "shouqian";
//						break;
//					default:
//						break;
//				}
				Bundle bundle = new Bundle();
//				bundle.putInt(Constant.INTENT_CODE_IMG_SELECTED_KEY, selectedIndex);
			    // 设置点击通知栏跳转事件
				Conversation conversation = ChatClient.getInstance().chatManager().getConversation(Constants.EaseUi.DEFAULT_CUSTOMER_ACCOUNT);
				String titleName = null;
				if (conversation.officialAccount() != null){
					titleName = conversation.officialAccount().getName();
				}
				// 访客信息
				VisitorInfo visitorInfo = new VisitorInfo();
				visitorInfo.name(userinfoBean.getEmail());
				visitorInfo.nickName(userinfoBean.getEmail());
				visitorInfo.email(userinfoBean.getEmail());
				String desc = userinfoBean.isVip() == 1 ? "This is an Android user and he is a subscriber" : "This is an Android user and he is not is a subscriber";
				visitorInfo.description(desc);

				// 进入主页面
				Intent intent = new IntentBuilder(EaseUiActivity.this)
						.setTargetClass(ChatActivity.class)
						//.setVisitorInfo(DemoMessageHelper.createVisitorInfo()) // 轨迹消息
						.setServiceIMNumber(Constants.EaseUi.DEFAULT_CUSTOMER_ACCOUNT)
						.setScheduleQueue(DemoMessageHelper.createQueueIdentity(queueName))
						.setTitleName("1 on 1 support") // 设置标题
//						.setScheduleAgent(DemoMessageHelper.createAgentIdentity("ceshiok1@qq.com"))
						.setShowUserNick(false)
						.setSendMessage(message)
						.setVisitorInfo(visitorInfo)
						.setBundle(bundle)
						.build();
				startActivity(intent);
				finish();

			}
		});
	}

}
