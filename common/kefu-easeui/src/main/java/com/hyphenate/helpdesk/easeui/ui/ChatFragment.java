package com.hyphenate.helpdesk.easeui.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.AgoraMessage;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.chat.ChatManager;
import com.hyphenate.chat.Conversation;
import com.hyphenate.chat.Message;
import com.hyphenate.helpdesk.R;
import com.hyphenate.helpdesk.easeui.UIProvider;
import com.hyphenate.helpdesk.easeui.glide.GlideEngine;
import com.hyphenate.helpdesk.easeui.mesanbox.MeSandboxFileEngine;
import com.hyphenate.helpdesk.easeui.provider.CustomChatRowProvider;
import com.hyphenate.helpdesk.easeui.recorder.MediaManager;
import com.hyphenate.helpdesk.easeui.runtimepermission.PermissionsManager;
import com.hyphenate.helpdesk.easeui.runtimepermission.PermissionsResultAction;
import com.hyphenate.helpdesk.easeui.util.CommonUtils;
import com.hyphenate.helpdesk.easeui.util.Config;
import com.hyphenate.helpdesk.easeui.util.uri.UriUtil;
import com.hyphenate.helpdesk.easeui.widget.AlertDialog;
import com.hyphenate.helpdesk.easeui.widget.AlertDialog.AlertDialogUser;
import com.hyphenate.helpdesk.easeui.widget.EaseChatInputMenu;
import com.hyphenate.helpdesk.easeui.widget.EaseChatInputMenu.ChatInputMenuListener;
import com.hyphenate.helpdesk.easeui.widget.ExtendMenu.EaseChatExtendMenuItemClickListener;
import com.hyphenate.helpdesk.easeui.widget.MessageList;
import com.hyphenate.helpdesk.easeui.widget.MessageList.MessageListItemClickListener;
import com.hyphenate.helpdesk.easeui.widget.ToastHelper;
import com.hyphenate.helpdesk.emojicon.Emojicon;
import com.hyphenate.helpdesk.manager.EmojiconManager;
import com.hyphenate.helpdesk.model.AgentIdentityInfo;
import com.hyphenate.helpdesk.model.QueueIdentityInfo;
import com.hyphenate.helpdesk.model.VisitorInfo;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.PathUtil;
import com.hyphenate.util.UriUtils;
import com.hyphenate.util.VersionUtils;
import com.luck.lib.camerax.CameraImageEngine;
import com.luck.lib.camerax.SimpleCameraX;
import com.luck.lib.camerax.listener.OnSimpleXPermissionDeniedListener;
import com.luck.lib.camerax.listener.OnSimpleXPermissionDescriptionListener;
import com.luck.lib.camerax.permissions.SimpleXPermissionUtil;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.dialog.RemindDialog;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.MediaExtraInfo;
import com.luck.picture.lib.interfaces.OnCameraInterceptListener;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.language.LanguageConfig;
import com.luck.picture.lib.permissions.PermissionConfig;
import com.luck.picture.lib.style.BottomNavBarStyle;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.utils.DensityUtil;
import com.luck.picture.lib.utils.MediaUtils;
import com.luck.picture.lib.utils.ToastUtils;
import com.luck.picture.lib.widget.MediumBoldTextView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

/**
 * 可以直接new出来使用的聊天对话页面fragment，
 * 使用时需调用setArguments方法传入IM服务号
 * app也可继承此fragment续写
 * 参数传入示例可查看demo里的ChatActivity
 */
public class ChatFragment extends BaseFragment implements ChatManager.MessageListener, EmojiconManager.EmojiconManagerDelegate {

    protected static final String TAG = ChatFragment.class.getSimpleName();
    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    protected static final int REQUEST_CODE_CAMERA = 1;
    protected static final int REQUEST_CODE_LOCAL = 2;
    private static final int REQUEST_CODE_SELECT_VIDEO = 3;

    public static final int REQUEST_CODE_EVAL = 5;
    public static final int REQUEST_CODE_SELECT_FILE = 6;
    /**
     * 传入fragment的参数
     */
    protected Bundle fragmentArgs;
    protected String toChatUsername;
    protected boolean showUserNick;
    protected MessageList messageList;
    protected EaseChatInputMenu inputMenu;

    protected Conversation conversation;
    protected InputMethodManager inputManager;
    protected ClipboardManager clipboard;
    protected String cameraFilePath = null;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected ListView listView;

    protected boolean isloading;
    protected boolean haveMoreData = true;
    protected int pagesize = 20;
    protected Message contextMenuMessage;

    protected static final int ITEM_TAKE_PICTURE = 1;
    protected static final int ITEM_PICTURE = 2;
    protected static final int ITEM_VIDEO = 3;
    protected static final int ITEM_FILE = 4;

//    protected int[] itemStrings = {R.string.attach_take_pic, R.string.attach_picture, R.string.attach_video, R.string.attach_file};
//    protected int[] itemdrawables = {R.drawable.hd_chat_takepic_selector, R.drawable.hd_chat_image_selector, R.drawable.hd_chat_video_selector, R.drawable.hd_chat_file_selector};

    protected int[] itemStrings = {R.string.attach_picture, R.string.attach_video};
    protected int[] itemdrawables = {R.drawable.hd_chat_image_selector, R.drawable.hd_chat_video_selector};

    protected int[] itemIds = {ITEM_TAKE_PICTURE, ITEM_PICTURE, ITEM_VIDEO, ITEM_FILE};
    protected int[] itemResIds = {R.id.chat_menu_take_pic, R.id.chat_menu_pic, R.id.chat_menu_video, R.id.chat_menu_file};
    private boolean isMessageListInited;
    protected MyMenuItemClickListener extendMenuItemClickListener;
    private VisitorInfo visitorInfo;
    private AgentIdentityInfo agentIdentityInfo;
    private QueueIdentityInfo queueIdentityInfo;
    private String titleName;
    protected TextView tvTipWaitCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean isSupportedHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportedHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
        ChatClient.getInstance().emojiconManager().addDelegate(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.hd_fragment_chat, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        fragmentArgs = getArguments();
        // IM服务号
        toChatUsername = fragmentArgs.getString(Config.EXTRA_SERVICE_IM_NUMBER);
        // 是否显示用户昵称
        showUserNick = fragmentArgs.getBoolean(Config.EXTRA_SHOW_NICK, false);
        //指定技能组
        queueIdentityInfo = fragmentArgs.getParcelable(Config.EXTRA_QUEUE_INFO);
        //指定客服
        agentIdentityInfo = fragmentArgs.getParcelable(Config.EXTRA_AGENT_INFO);
        visitorInfo = fragmentArgs.getParcelable(Config.EXTRA_VISITOR_INFO);

        titleName = fragmentArgs.getString(Config.EXTRA_TITLE_NAME);
        //在父类中调用了initView和setUpView两个方法
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            cameraFilePath = savedInstanceState.getString("cameraFilePath");
        }
        ChatClient.getInstance().chatManager().bindChat(toChatUsername);
        ChatClient.getInstance().chatManager().addAgentInputListener(agentInputListener);

        // 为测试获取账号用，无实际意义
        setUserNameView();
    }

    private void setUserNameView() {
        if (ChatClient.getInstance().isLoggedInBefore()) {
            String currentUsername = ChatClient.getInstance().currentUserName();
            if (getView() != null) {
                TextView tvUname = (TextView) getView().findViewById(R.id.tv_username);
                if (tvUname != null) {
                    tvUname.setText(currentUsername);
                }
            }
        }
    }

    /**
     * init view
     */
    @Override
    protected void initView() {
        // 消息列表layout
        messageList = (MessageList) getView().findViewById(R.id.message_list);
        messageList.setShowUserNick(showUserNick);
        listView = messageList.getListView();
        tvTipWaitCount = (TextView) getView().findViewById(R.id.tv_tip_waitcount);
        extendMenuItemClickListener = new MyMenuItemClickListener();
        inputMenu = (EaseChatInputMenu) getView().findViewById(R.id.input_menu);
        registerExtendMenuItem();
        // init input menu
        inputMenu.init();
        inputMenu.setChatInputMenuListener(new ChatInputMenuListener() {

            @Override
            public void onSendMessage(String content) {
                // 发送文本消息
                sendTextMessage(content);
            }

            @Override
            public void onBigExpressionClicked(Emojicon emojicon) {
                if (!TextUtils.isEmpty(emojicon.getBigIconRemotePath())) {
                    sendCustomEmojiMessage(emojicon.getBigIconRemotePath());
                } else if (!TextUtils.isEmpty(emojicon.getIconRemotePath())) {
                    sendCustomEmojiMessage(emojicon.getIconRemotePath());
                } else if (!TextUtils.isEmpty(emojicon.getBigIconPath())) {
                    sendImageMessage(emojicon.getBigIconPath());
                } else if (!TextUtils.isEmpty(emojicon.getIconPath())) {
                    sendImageMessage(emojicon.getIconPath());
                }
            }

            @Override
            public void onRecorderCompleted(float seconds, String filePath) {
                // 发送语音消息
                int time = seconds > 1 ? (int) seconds : 1;
                sendVoiceMessage(filePath, time);
            }
        });
        inputMenu.setHasSendButton(true);

        swipeRefreshLayout = messageList.getSwipeRefreshLayout();
        swipeRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_light,
                R.color.holo_orange_light, R.color.holo_red_light);

        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        ChatClient.getInstance().chatManager().addVisitorWaitListener(visitorWaitListener);
    }

    ChatManager.VisitorWaitListener visitorWaitListener = new ChatManager.VisitorWaitListener() {
        @Override
        public void waitCount(final int num) {
            if (getActivity() == null) {
                return;
            }
//            EMLog.d(TAG, "waitCount--num:" + num);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (num > 0) {
                        tvTipWaitCount.setVisibility(View.VISIBLE);
                        tvTipWaitCount.setText(getString(R.string.current_wait_count, num));
                    } else {
                        tvTipWaitCount.setVisibility(View.GONE);
                    }
                }
            });
        }
    };

    ChatManager.AgentInputListener agentInputListener = new ChatManager.AgentInputListener() {
        @Override
        public void onInputState(final String input) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (input != null) {
                        titleBar.setTitle(input);
                    } else {
                        if (!TextUtils.isEmpty(titleName)) {
                            titleBar.setTitle(titleName);
                        } else {
                            titleBar.setTitle(toChatUsername);
                        }
                    }
                }
            });

        }
    };

    /**
     * 设置属性，监听等
     */
    @Override
    protected void setUpView() {
        if (!TextUtils.isEmpty(titleName)) {
            titleBar.setTitle(titleName);
        } else {
            titleBar.setTitle(toChatUsername);
        }

        titleBar.setRightImageResource(R.drawable.hd_mm_title_remove);

        onConversationInit();
        onMessageListInit();


        // 设置标题栏点击事件
        titleBar.setLeftLayoutClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
        titleBar.setRightLayoutClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                emptyHistory();
            }
        });
        setRefreshLayoutListener();

        // test api
//        ChatClient.getInstance().chatManager().getTransferGuideMenu(toChatUsername, new ValueCallBack<JSONObject>() {
//            @Override
//            public void onSuccess(JSONObject value) {
//                EMLog.d(TAG, "onsuccess" + value.toString());
//                Message message = Message.createReceiveMessage(Message.Type.TXT);
//                message.setBody(new EMTextMessageBody("test guide"));
//                message.setMsgId(UUID.randomUUID().toString());
//                message.setStatus(Message.Status.SUCCESS);
//                message.setFrom(toChatUsername);
//                message.setMessageTime(System.currentTimeMillis());
//                try {
//                    message.setAttribute(Message.KEY_MSGTYPE, value.getJSONObject(Message.KEY_MSGTYPE));
//                    ChatClient.getInstance().chatManager().saveMessage(message);
//                    messageList.refreshSelectLast();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//
//            @Override
//            public void onError(int error, String errorMsg) {
//
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ChatClient.getInstance().emojiconManager().removeDelegate(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ChatClient.getInstance().chatManager().unbindChat();
        ChatClient.getInstance().chatManager().removeAgentInputListener(agentInputListener);
        ChatClient.getInstance().chatManager().removeVisitorWaitListener(visitorWaitListener);
    }

    /**
     * 注册底部菜单扩展栏item; 覆盖此方法时如果不覆盖已有item，item的id需大于3
     */
    protected void registerExtendMenuItem() {
        for (int i = 0; i < itemStrings.length; i++) {
            inputMenu.registerExtendMenuItem(itemStrings[i], itemdrawables[i], itemIds[i], itemResIds[i], extendMenuItemClickListener);
        }
    }

    protected void onConversationInit() {
        // 获取当前conversation对象
        conversation = ChatClient.getInstance().chatManager().getConversation(toChatUsername);
        if (conversation != null) {
            // 把此会话的未读数置为0
            // 已读所有
            conversation.markAllMessagesAsRead();
            ChatClient.getInstance().chatManager().markAllConversationsAsRead();
            final List<Message> msgs = conversation.getAllMessages();
            int msgCount = msgs != null ? msgs.size() : 0;
            if (msgCount < conversation.getAllMsgCount() && msgCount < pagesize) {
                String msgId = null;
                if (msgs != null && msgs.size() > 0) {
                    msgId = msgs.get(0).messageId();
                }
                conversation.loadMessages(msgId, pagesize - msgCount);
                conversation.markMessageAsRead(msgId);
            }
        }
    }

    protected void onMessageListInit() {
        messageList.init(toChatUsername, chatFragmentListener != null ?
                chatFragmentListener.onSetCustomChatRowProvider() : null);
        //设置list item里的控件的点击事件
        setListItemClickListener();

        messageList.getListView().setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!inputMenu.isVoiceRecording()) {//录音时，点击列表不做操作
                    hideKeyboard();
                    inputMenu.hideExtendMenuContainer();
                }
                return false;
            }
        });

        isMessageListInited = true;
    }

    protected void setListItemClickListener() {
        messageList.setItemClickListener(new MessageListItemClickListener() {

            @Override
            public void onUserAvatarClick(String username) {
                if (chatFragmentListener != null) {
                    chatFragmentListener.onAvatarClick(username);
                }
            }

            @Override
            public void onResendClick(final Message message) {
                new AlertDialog(getActivity(), R.string.resend, R.string.confirm_resend, null, new AlertDialogUser() {
                    @Override
                    public void onResult(boolean confirmed, Bundle bundle) {
                        if (!confirmed) {
                            return;
                        }
                        ChatClient.getInstance().chatManager().resendMessage(message);
                    }
                }, true).show();
            }

            @Override
            public void onBubbleLongClick(Message message) {
                contextMenuMessage = message;
                if (chatFragmentListener != null) {
                    chatFragmentListener.onMessageBubbleLongClick(message);
                }
            }

            @Override
            public boolean onBubbleClick(Message message) {
                if (chatFragmentListener != null) {
                    return chatFragmentListener.onMessageBubbleClick(message);
                }
                return false;
            }

            @Override
            public void onMessageItemClick(Message message, MessageList.ItemAction action) {
                contextMenuMessage = message;
                if (chatFragmentListener != null) {
                    chatFragmentListener.onMessageItemClick(message, action);
                }
            }
        });
    }

    protected void setRefreshLayoutListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (getActivity() == null || getActivity().isFinishing()) {
                            return;
                        }
                        if (listView.getFirstVisiblePosition() == 0 && !isloading && haveMoreData) {
                            List<Message> messages = null;
                            try {
                                messages = conversation.loadMessages(messageList.getItem(0).messageId(),
                                        pagesize);
                            } catch (Exception e1) {
                                swipeRefreshLayout.setRefreshing(false);
                                return;
                            }
                            if (messages != null && messages.size() > 0) {
                                messageList.refreshSeekTo(messages.size() - 1);
                                if (messages.size() != pagesize) {
                                    haveMoreData = false;
                                }
                            } else {
                                haveMoreData = false;
                            }

                            isloading = false;

                        } else {
                            ToastHelper.show(getActivity(), R.string.no_more_messages);
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 600);
            }
        });
    }

    private void analyticalSelectResults(ArrayList<LocalMedia> result) {
        for (LocalMedia media : result) {
            if (media.getWidth() == 0 || media.getHeight() == 0) {
                // 如果是图片
                if (PictureMimeType.isHasImage(media.getMimeType())) {
                    MediaExtraInfo imageExtraInfo = MediaUtils.getImageSize(getContext(), media.getPath());
                    media.setWidth(imageExtraInfo.getWidth());
                    media.setHeight(imageExtraInfo.getHeight());
                } else if (PictureMimeType.isHasVideo(media.getMimeType())) {
                    MediaExtraInfo videoExtraInfo = MediaUtils.getVideoSize(getContext(), media.getPath());
                    media.setWidth(videoExtraInfo.getWidth());
                    media.setHeight(videoExtraInfo.getHeight());
                }
            }
        }

        // 展示图片
        LocalMedia media = result.get(0);
        String path = media.getPath(); // uri
        if (PictureMimeType.isHasImage(media.getMimeType())) {
            // 发送图片
            sendPicByUri(Uri.parse(path));
        }

    }


    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片
                sendImageMessage(cameraFilePath);
            } else if (requestCode == REQUEST_CODE_SELECT_FILE) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        sendFileByUri(uri);
                    }
                }
            } else if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                // 发送本地视频
                // 发送本地图片
                ArrayList<LocalMedia> result = PictureSelector.obtainSelectorList(data);
                if (null == result || result.size() == 0) return;
                analyticalSelectResults(result);
            }
//            else if (requestCode == REQUEST_CODE_SELECT_VIDEO) {
//                if (data != null) {
//                    int duration = data.getIntExtra("dur", 0);
//                    String videoPath = data.getStringExtra("path");
//                    String uriString = data.getStringExtra("uri");
//                    EMLog.d(TAG, "path = " + videoPath + " uriString = " + uriString);
//
//                    if (!TextUtils.isEmpty(videoPath)) {
//                        sendVideoMessage(Uri.parse(videoPath), duration);
//                    } else {
//                        Uri videoUri = UriUtils.getLocalUriFromString(uriString);
//                        sendVideoMessage(videoUri, duration);
//                    }
//                }
//            }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMessageListInited)
            messageList.refresh();
        MediaManager.resume();
        UIProvider.getInstance().pushActivity(getActivity());
        // register the event listener when enter the foreground
        ChatClient.getInstance().chatManager().addMessageListener(this);
    }


    @Override
    public void onStop() {
        super.onStop();
        // unregister this event listener when this activity enters the
        // background
        ChatClient.getInstance().chatManager().removeMessageListener(this);
        // 把此activity 从foreground activity 列表里移除
        UIProvider.getInstance().popActivity(getActivity());
    }


    public void onBackPressed() {
        inputMenu.onBackPressed();
    }

    /**
     * 自定义表情更新、不要提示
     */
    @Override
    public void onEmojiconChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 自定义表情更新
                // ToastHelper.show(getActivity(), R.string.emoji_icon_update);
                if (inputMenu != null) {
                    inputMenu.onEmojiconChanged();
                }
            }
        });
    }

    /**
     * 扩展菜单栏item点击事件
     */
    class MyMenuItemClickListener implements EaseChatExtendMenuItemClickListener {

        @Override
        public void onExtendMenuItemClick(int itemId, View view) {
            if (getActivity() == null) {
                return;
            }
            if (chatFragmentListener != null) {
                if (chatFragmentListener.onExtendMenuItemClick(itemId, view)) {
                    return;
                }
            }
            switch (itemId) {
                case ITEM_TAKE_PICTURE: // 拍照
                    PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(ChatFragment.this, new String[]{Manifest.permission.CAMERA}, new PermissionsResultAction() {
                        @Override
                        public void onGranted() {
                            selectPicFromLocal(); // 图库选择图片
                        }

                        @Override
                        public void onDenied(String permission) {

                        }
                    });
                    break;
                case ITEM_PICTURE:
                    PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(ChatFragment.this, new String[]{Manifest.permission.CAMERA}, new PermissionsResultAction() {
                        @Override
                        public void onGranted() {
                            selectVideoFromLocal();
                        }

                        @Override
                        public void onDenied(String permission) {

                        }
                    });
                    break;
                case ITEM_VIDEO:
                    PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(ChatFragment.this, new String[]{Manifest.permission.CAMERA}, new PermissionsResultAction() {
                        @Override
                        public void onGranted() {
                            selectVideoFromLocal();
                        }

                        @Override
                        public void onDenied(String permission) {

                        }
                    });

                    break;
                case ITEM_FILE:
                    //一般文件
                    //demo这里是通过系统api选择文件，实际app中最好是做成qq那种选择发送文件
                    selectFileFromLocal();
                    break;
                default:
                    break;
            }
        }

    }

    private void selectVideoFromLocal() {
//        Intent intent = new Intent(getActivity(), ImageGridActivity.class);
//        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);

//        PictureSelectorStyle pictureSelectorStyle = new PictureSelectorStyle();
//        BottomNavBarStyle bottomNavBarStyle = new BottomNavBarStyle();
//        pictureSelectorStyle.setBottomBarStyle(bottomNavBarStyle);
//        // 选择照片
//        // 选择视频，不显示角标
        PictureSelector.create(getContext())
                .openGallery(SelectMimeType.ofVideo())
                .setImageEngine(GlideEngine.createGlideEngine())
//                            .setCompressEngine(ImageFileCompressEngine()) //是否压缩
                .setSandboxFileEngine(new MeSandboxFileEngine()) // Android10 沙盒文件
                .isOriginalControl(false)// 原图功能
                .isDisplayTimeAxis(true)// 资源轴
                .setEditMediaInterceptListener(null)// 是否开启图片编辑功能
                .isMaxSelectEnabledMask(true) // 是否显示蒙层
                .isDisplayCamera(true)//是否显示摄像
                .setLanguage(LanguageConfig.ENGLISH) //显示英语
                .setSelectMaxDurationSecond(30) // 只显示30秒的
                .setMaxSelectNum(1)
                .setCameraInterceptListener(getCustomCameraEvent())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        if (null == result || result.size() == 0) return;
                        LocalMedia media = result.get(0);
                        // todo 大于10M的视频 都需要进行视频压缩
                        if (media.getSize() >= 10 * 1024 * 100) {
                            // 判断当前视频的原始宽高
                            try {
                                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                retriever.setDataSource(getActivity(), Uri.parse(media.getPath()));
                                int originWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                                int originHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                                if (originWidth > 720 && originHeight > 1080) {
                                    executeScaleVideo(media.getAvailablePath(), Integer.parseInt(String.valueOf(media.getDuration())), 720, 1080);
                                } else {
                                    executeScaleVideo(media.getAvailablePath(), Integer.parseInt(String.valueOf(media.getDuration())), originWidth, originHeight);
                                }
                            }catch (Exception e) {
                                Log.i(TAG, "onResult: 发生异常了。");
                            }
                            // 视频压缩
                            return;
                        }
                        // 发送视频
                        sendVideoMessage(Uri.parse(media.getPath()), Integer.parseInt(String.valueOf(media.getDuration())));
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private File getTempMovieDir(){
        File movie = new File(getContext().getCacheDir(), "movie");
        movie.mkdirs();
        return movie;
    }

    /**
     * 压缩视频
     */
    private void executeScaleVideo(String path, int dur, int width , int height) {
        ProgressDialog  progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("please wait......");
        File moviesDir = getTempMovieDir();
        progressDialog.show();
        String filePrefix = "scale_video";
        String fileExtn = ".mp4";
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }
       String filePath = dest.getAbsolutePath();

        EpVideo epVideo = new EpVideo(path);
        //输出选项，参数为输出文件路径(目前仅支持mp4格式输出)
        EpEditor.OutputOption outputOption = new EpEditor.OutputOption(filePath);
        outputOption.setWidth(width);//输出视频宽，如果不设置则为原始视频宽高
        outputOption.setHeight(height);//输出视频高度
        outputOption.frameRate = 30;//输出视频帧率,默认30
        outputOption.bitRate = 2;//输出视频码率,默认10
        EpEditor.exec(epVideo, outputOption, new OnEditorListener(){
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Uri uri = UriUtil.getUris(getContext(), new File(filePath));
                        sendVideoMessage(uri, dur);
                    }
                });

            }

            @Override
            public void onFailure() {
                progressDialog.dismiss();
            }

            @Override
            public void onProgress(float progress) {
                Log.e("123123123", "progress: " + progress);
            }
        });
    }


    /**
     * 自定义视频录像
     */
    private OnCameraInterceptListener getCustomCameraEvent() {
        return  new MeOnCameraInterceptListener();
    }

    private class MeOnCameraInterceptListener implements OnCameraInterceptListener {

        @Override
        public void openCamera(Fragment fragment, int cameraMode, int requestCode) {
            SimpleCameraX camera = SimpleCameraX.of();
            camera.isAutoRotation(true);
            camera.setCameraMode(cameraMode);
            camera.setVideoFrameRate(20);
            camera.setRecordVideoMaxSecond(30);
            camera.setRecordVideoMinSecond(5);
            camera.setVideoBitRate(2 * 1024 * 1024);
            camera.isDisplayRecordChangeTime(true);
            camera.isManualFocusCameraPreview(true);
            camera.isZoomCameraPreview(true);
            camera.setOutputPathDir(getSandboxCameraOutputPath());
            camera.setPermissionDeniedListener(getSimpleXPermissionDeniedListener());
            camera.setPermissionDescriptionListener(getSimpleXPermissionDescriptionListener());
            camera.setImageEngine(new CameraImageEngine() {
                @Override
                public void loadImage(Context context, String url, ImageView imageView) {
                    Glide.with(context).load(url).into(imageView);
                }
            });
            camera.start(fragment.requireActivity(), fragment, requestCode);
        }
    }

    private OnSimpleXPermissionDescriptionListener getSimpleXPermissionDescriptionListener() {
        return new MeOnSimpleXPermissionDescriptionListener();
    }

    private static class MeOnSimpleXPermissionDescriptionListener implements OnSimpleXPermissionDescriptionListener {

        @Override
        public void onPermissionDescription(Context context, ViewGroup viewGroup, String permission) {
            addPermissionDescription(true, viewGroup, new String[]{permission});
        }

        @Override
        public void onDismiss(ViewGroup viewGroup) {
            removePermissionDescription(viewGroup);
        }
    }


    /**
     * 添加权限说明
     *
     * @param viewGroup
     * @param permissionArray
     */
    private static void addPermissionDescription(boolean isHasSimpleXCamera, ViewGroup viewGroup, String[] permissionArray) {
        int dp10 = DensityUtil.dip2px(viewGroup.getContext(), 10);
        int dp15 = DensityUtil.dip2px(viewGroup.getContext(), 15);
        MediumBoldTextView view = new MediumBoldTextView(viewGroup.getContext());
        view.setTag(TAG_EXPLAIN_VIEW);
        view.setTextSize(14);
        view.setTextColor(Color.parseColor("#333333"));
        view.setPadding(dp10, dp15, dp10, dp15);

        String title;
        String explain;

        if (TextUtils.equals(permissionArray[0], PermissionConfig.CAMERA[0])) {
            title = viewGroup.getContext().getString(R.string.chat_camera);
            explain = viewGroup.getContext().getString(R.string.chat_camera_desc);
        } else if (TextUtils.equals(permissionArray[0], Manifest.permission.RECORD_AUDIO)) {
            if (isHasSimpleXCamera) {
                title = viewGroup.getContext().getString(R.string.chat_microp);
                explain = viewGroup.getContext().getString(R.string.chat_microp_desc);
            } else {
                title = viewGroup.getContext().getString(R.string.chat_record);
                explain = viewGroup.getContext().getString(R.string.chat_record_desc);
            }
        } else {
            title = viewGroup.getContext().getString(R.string.chat_storage);
            explain = viewGroup.getContext().getString(R.string.chat_storage_desc);
        }
        int startIndex = 0;
        int endOf = startIndex + title.length();
        SpannableStringBuilder builder = new SpannableStringBuilder(explain);
        builder.setSpan(new AbsoluteSizeSpan(DensityUtil.dip2px(viewGroup.getContext(), 16)), startIndex, endOf, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(0xFF333333), startIndex, endOf, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setText(builder);
        view.setBackground(ContextCompat.getDrawable(viewGroup.getContext(), R.drawable.ps_demo_permission_desc_bg));

        if (isHasSimpleXCamera) {
            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = DensityUtil.getStatusBarHeight(viewGroup.getContext());
            layoutParams.leftMargin = dp10;
            layoutParams.rightMargin = dp10;
            viewGroup.addView(view, layoutParams);
        } else {
            ConstraintLayout.LayoutParams layoutParams =
                    new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.topToBottom = R.id.title_bar;
            layoutParams.leftToLeft = ConstraintSet.PARENT_ID;
            layoutParams.leftMargin = dp10;
            layoutParams.rightMargin = dp10;
            viewGroup.addView(view, layoutParams);
        }
    }

    /**
     * 移除权限说明
     *
     * @param viewGroup
     */
    private final static String TAG_EXPLAIN_VIEW = "TAG_EXPLAIN_VIEW";
    private static void removePermissionDescription(ViewGroup viewGroup) {
        View tagExplainView = viewGroup.findViewWithTag(TAG_EXPLAIN_VIEW);
        viewGroup.removeView(tagExplainView);
    }

    private String getSandboxCameraOutputPath() {
        if (true) {
            File externalFilesDir = getContext().getExternalFilesDir("");
            File customFile = new File(externalFilesDir.getAbsolutePath(), "Sandbox");
            if (!customFile.exists()) {
                customFile.mkdirs();
            }
            return customFile.getAbsolutePath() + File.separator;
        } else {
            return "";
        }
    }

    private OnSimpleXPermissionDeniedListener getSimpleXPermissionDeniedListener() {
       return new MeOnSimpleXPermissionDeniedListener();
    }

    /**
     * SimpleCameraX添加权限说明
     */
    private static class MeOnSimpleXPermissionDeniedListener implements OnSimpleXPermissionDeniedListener {

        @Override
        public void onDenied(Context context, String permission, int requestCode) {
            String tips;
            if (TextUtils.equals(permission, Manifest.permission.RECORD_AUDIO)) {
                tips = context.getString(R.string.chat_microp_tips);
            } else {
                tips = context.getString(R.string.chat_camera_tips);
            }
            RemindDialog dialog = RemindDialog.buildDialog(context, tips);
            dialog.setButtonText(context.getString(R.string.chat_gosetting));
            dialog.setButtonTextColor(0xFF7D7DFF);
            dialog.setContentTextColor(0xFF333333);
            dialog.setOnDialogClickListener(new RemindDialog.OnDialogClickListener() {
                @Override
                public void onClick(View view) {
                    SimpleXPermissionUtil.goIntentSetting((Activity) context, requestCode);
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }


    /**
     * 选择文件
     */
    protected void selectFileFromLocal() {
        Intent intent = new Intent();
        if (VersionUtils.isTargetQ(getActivity())) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }


    /**
     * 根据图库图片uri发送图片
     *
     * @param selectedImage
     */
    protected void sendPicByUri(Uri selectedImage) {
        sendImageMessage(selectedImage);
    }

    /**
     * 根据uri发送文件
     *
     * @param uri
     */
    protected void sendFileByUri(Uri uri) {
        sendFileMessage(uri);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
        if (cameraFilePath != null) {
            outState.putString("cameraFile", cameraFilePath);
        }
    }

    /**
     * 照相获取图片
     */
    protected void selectPicFromCamera() {
        if (!CommonUtils.isExitsSdcard()) {
            ToastHelper.show(getActivity(), R.string.sd_card_does_not_exist);
            return;
        }
        try {
            File cameraFile = new File(PathUtil.getInstance().getImagePath(), ChatClient.getInstance().currentUserName()
                    + System.currentTimeMillis() + ".jpg");
            cameraFilePath = cameraFile.getAbsolutePath();
            if (!cameraFile.getParentFile().exists()) {
                cameraFile.getParentFile().mkdirs();
            }
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
            } else {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getContext().getApplicationContext(), getContext().getPackageName() + ".fileProvider", cameraFile));
            }
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从图库获取图片
     */
    protected void selectPicFromLocal() {
//        Intent intent = null;
//        if(VersionUtils.isTargetQ(getActivity())) {
//            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//        }else {
//            if (Build.VERSION.SDK_INT < 19) {
//                intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//            } else {
//                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            }
//        }
//        intent.setType("image/*");
//        startActivityForResult(intent, REQUEST_CODE_LOCAL);

        PictureSelectorStyle pictureSelectorStyle = new PictureSelectorStyle();
        BottomNavBarStyle bottomNavBarStyle = new BottomNavBarStyle();
        pictureSelectorStyle.setBottomBarStyle(bottomNavBarStyle);
        // 选择照片
        // 选择照片，不显示角标
        PictureSelector.create(getContext())
                .openGallery(SelectMimeType.ofImage())
                .setImageEngine(GlideEngine.createGlideEngine())
//                            .setCompressEngine(ImageFileCompressEngine()) //是否压缩
                .setSandboxFileEngine(new MeSandboxFileEngine()) // Android10 沙盒文件
                .isOriginalControl(false)// 原图功能
                .isDisplayTimeAxis(true)// 资源轴
                .setEditMediaInterceptListener(null)// 是否开启图片编辑功能
                .isMaxSelectEnabledMask(true) // 是否显示蒙层
                .isDisplayCamera(true)//是否显示摄像
                .setLanguage(LanguageConfig.ENGLISH) //显示英语
                .setMaxSelectNum(1)
                .setSelectorUIStyle(pictureSelectorStyle)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        if (null == result || result.size() == 0) return;
                        LocalMedia media = result.get(0);
                        // 发送本地图片
                        analyticalSelectResults(result);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }


    /**
     * 点击清空聊天记录
     */
    protected void emptyHistory() {
        String msg = getResources().getString(R.string.Whether_to_empty_all_chats);
        new AlertDialog(getActivity(), null, msg, null, new AlertDialogUser() {

            @Override
            public void onResult(boolean confirmed, Bundle bundle) {
                if (confirmed) {
                    MediaManager.release();
                    ChatClient.getInstance().chatManager().clearConversation(toChatUsername);
                    messageList.refresh();
                }
            }
        }, true).show();
    }


    /**
     * 隐藏软键盘
     */
    protected void hideKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    protected EaseChatFragmentListener chatFragmentListener;

    public void setChatFragmentListener(EaseChatFragmentListener chatFragmentListener) {
        this.chatFragmentListener = chatFragmentListener;
    }

    public interface EaseChatFragmentListener {
        /**
         * 用户头像点击事件
         *
         * @param username
         */
        void onAvatarClick(String username);

        /**
         * 消息气泡框点击事件
         */
        boolean onMessageBubbleClick(Message message);

        /**
         * 消息气泡框长按事件
         */
        void onMessageBubbleLongClick(Message message);

        /**
         * 扩展输入栏item点击事件,如果要覆盖EaseChatFragment已有的点击事件，return true
         *
         * @param view
         * @param itemId
         * @return
         */
        boolean onExtendMenuItemClick(int itemId, View view);

        /**
         * 菜单消息被点击有具体的跳转要求时执行下面的回调
         */
        void onMessageItemClick(Message message, MessageList.ItemAction action);

        /**
         * 设置自定义chatrow提供者
         *
         * @return
         */
        CustomChatRowProvider onSetCustomChatRowProvider();
    }

    @Override
    public void onMessage(List<Message> msgs) {

        for (Message message : msgs) {
            String username = null;
            username = message.from();
            // 如果是当前会话的消息，刷新聊天页面
            if (username != null && username.equals(toChatUsername)) {
                // 已读一条消息
                Conversation conversation = ChatClient.getInstance().chatManager().getConversation(toChatUsername);
                conversation.markMessageAsRead(message.messageId());
                messageList.refreshSelectLast();
                // 声音和震动提示有新消息
                UIProvider.getInstance().getNotifier().viberateAndPlayTone(message);
            } else {
                // 如果消息不是和当前聊天ID的消息
                UIProvider.getInstance().getNotifier().onNewMsg(message);
            }
        }

    }

    @Override
    public void onCmdMessage(List<Message> msgs) {

    }


    @Override
    public void onMessageStatusUpdate() {
        messageList.refreshSelectLast();
    }

    @Override
    public void onMessageSent() {
        messageList.refreshSelectLast();
    }

    // 发送消息方法
    //=============================================
    protected void sendTextMessage(String content) {
        if (content != null && content.length() > 1500) {
            ToastHelper.show(getActivity(), R.string.message_content_beyond_limit);
            return;
        }
        Message message = Message.createTxtSendMessage(content, toChatUsername);
        attachMessageAttrs(message);
        ChatClient.getInstance().chatManager().sendMessage(message);
        messageList.refreshSelectLast();
    }

    protected void sendVoiceMessage(String filePath, int length) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        Message message = Message.createVoiceSendMessage(filePath, length, toChatUsername);
        attachMessageAttrs(message);
        ChatClient.getInstance().chatManager().sendMessage(message);
        messageList.refreshSelectLast();
    }

    protected void sendImageMessage(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return;
        }
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            return;
        }

        Message message = Message.createImageSendMessage(imagePath, false, toChatUsername);
        attachMessageAttrs(message);
        ChatClient.getInstance().chatManager().sendMessage(message);
        messageList.refreshSelectLastDelay(MessageList.defaultDelay);
    }

    protected void sendImageMessage(Uri imageUri) {
        Message message = Message.createImageSendMessage(imageUri, false, toChatUsername);
        if (message != null) {
            attachMessageAttrs(message);
            ChatClient.getInstance().chatManager().sendMessage(message);
            messageList.refreshSelectLastDelay(MessageList.defaultDelay);
        }
    }

    protected void sendCustomEmojiMessage(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return;
        }

        Message message = Message.createCustomEmojiSendMessage(imagePath, toChatUsername);
        message.setMessageTime(System.currentTimeMillis());
        attachMessageAttrs(message);
        ChatClient.getInstance().chatManager().sendMessage(message);
        messageList.refreshSelectLastDelay(MessageList.defaultDelay);
    }

    protected void sendFileMessage(String filePath) {
        Message message = Message.createFileSendMessage(filePath, toChatUsername);
        attachMessageAttrs(message);
        ChatClient.getInstance().chatManager().sendMessage(message);
        messageList.refreshSelectLastDelay(MessageList.defaultDelay);
    }

    protected void sendFileMessage(Uri fileUri) {
        Message message = Message.createFileSendMessage(fileUri, toChatUsername);
        attachMessageAttrs(message);
        ChatClient.getInstance().chatManager().sendMessage(message);
        messageList.refreshSelectLastDelay(MessageList.defaultDelay);
    }

    protected void sendLocationMessage(double latitude, double longitude, String locationAddress, String toChatUsername) {
        Message message = Message.createLocationSendMessage(latitude, longitude, locationAddress, toChatUsername);
        attachMessageAttrs(message);
        ChatClient.getInstance().chatManager().sendMessage(message);
        messageList.refreshSelectLastDelay(MessageList.defaultDelay);
    }

    protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength) {
        Message message = Message.createVideoSendMessage(videoPath, thumbPath, videoLength, toChatUsername);
        attachMessageAttrs(message);
        ChatClient.getInstance().chatManager().sendMessage(message);
        messageList.refreshSelectLastDelay(MessageList.defaultDelay);
    }

    protected void sendVideoMessage(Uri videoUri, int videoLength) {
        String thumbPath = getThumbPath(videoUri);
        Message message = Message.createVideoSendMessage(videoUri, thumbPath, videoLength, toChatUsername);
        attachMessageAttrs(message);

        // send message
        ChatClient.getInstance().chatManager().sendMessage(message);
        messageList.refreshSelectLastDelay(MessageList.defaultDelay);
    }

    public void attachMessageAttrs(Message message) {
        if (visitorInfo != null) {
            message.addContent(visitorInfo);
        }
        if (queueIdentityInfo != null) {
            message.addContent(queueIdentityInfo);
        }
        if (agentIdentityInfo != null) {
            message.addContent(agentIdentityInfo);
        }
    }

    /**
     * 获取视频封面
     *
     * @param videoUri
     * @return
     */
    private String getThumbPath(Uri videoUri) {
        if (!UriUtils.isFileExistByUri(getContext(), videoUri)) {
            return "";
        }
        String filePath = UriUtils.getFilePath(getContext(), videoUri);
        File file = new File(PathUtil.getInstance().getVideoPath(), "thvideo" + System.currentTimeMillis() + ".jpeg");
        boolean createSuccess = true;
        if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                Bitmap ThumbBitmap = ThumbnailUtils.createVideoThumbnail(filePath, 3);
                ThumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                EMLog.e(TAG, e.getMessage());
                createSuccess = false;
            }
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                MediaMetadataRetriever media = new MediaMetadataRetriever();
                media.setDataSource(getContext(), videoUri);
                Bitmap frameAtTime = media.getFrameAtTime();
                frameAtTime.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                EMLog.e(TAG, e.getMessage());
                createSuccess = false;
            }
        }
        return createSuccess ? file.getAbsolutePath() : "";
    }

    @Override
    public void onPause() {
        super.onPause();
        MediaManager.pause();
    }


}
