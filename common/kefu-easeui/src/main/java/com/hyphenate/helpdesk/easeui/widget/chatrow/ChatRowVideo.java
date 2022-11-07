package com.hyphenate.helpdesk.easeui.widget.chatrow;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.ChatClient;
import com.hyphenate.helpdesk.R;
import com.hyphenate.chat.Message;
import com.hyphenate.helpdesk.easeui.ImageCache;
import com.hyphenate.helpdesk.easeui.ui.ShowVideoActivity;
import com.hyphenate.helpdesk.easeui.util.CommonUtils;
import com.hyphenate.helpdesk.util.Log;
import com.hyphenate.util.DateUtils;
import com.hyphenate.util.DensityUtil;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.ImageUtils;
import com.hyphenate.util.TextFormater;
import com.hyphenate.util.UriUtils;

import java.io.File;
import java.io.IOException;

public class ChatRowVideo extends ChatRowFile {

    private ImageView imageView;
    private TextView sizeView;
    private TextView timeLengthView;
    private ImageView playView;

    public ChatRowVideo(Context context, Message message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflatView() {
        inflater.inflate(message.direct() == Message.Direct.RECEIVE ?
                R.layout.hd_row_received_video : R.layout.hd_row_sent_video, this);
    }

    @Override
    protected void onFindViewById() {
        imageView = ((ImageView) findViewById(R.id.chatting_content_iv));
        sizeView = (TextView) findViewById(R.id.chatting_size_iv);
        timeLengthView = (TextView) findViewById(R.id.chatting_length_iv);
        playView = (ImageView) findViewById(R.id.chatting_status_btn);
        percentageView = (TextView) findViewById(R.id.percentage);
    }

    @Override
    protected void onSetUpView() {
        EMVideoMessageBody videoBody = (EMVideoMessageBody) message.body();
        // final File image=new File(PathUtil.getInstance().getVideoPath(),
        // videoBody.getFileName());
        String localThumb = videoBody.getLocalThumb();

        if (localThumb != null) {

            showVideoThumbView(localThumb, imageView, videoBody.getThumbnailUrl(), message);
        }
        if (videoBody.getDuration() > 0) {
            String time = DateUtils.toTime(videoBody.getDuration());
            timeLengthView.setText(time);
        }
//        playView.setImageResource(R.drawable.video_play_btn_small_nor);

        if (message.direct() == Message.Direct.RECEIVE) {
            if (videoBody.getVideoFileLength() > 0) {
                String size = TextFormater.getDataSize(videoBody.getVideoFileLength());
                sizeView.setText(size);
            }
        } else {
            if (videoBody.getLocalUrl() != null && new File(videoBody.getLocalUrl()).exists()) {
                String size = TextFormater.getDataSize(new File(videoBody.getLocalUrl()).length());
                sizeView.setText(size);
            }
        }

        Log.d(TAG, "video thumbnailStatus:" + videoBody.thumbnailDownloadStatus());
        if (message.direct() == Message.Direct.RECEIVE) {
            if (videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
                    videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING) {
                imageView.setImageResource(R.drawable.hd_default_image);
                setMessageReceiveCallback();
            } else {
                // System.err.println("!!!! not back receive, show image directly");
                imageView.setImageResource(R.drawable.hd_default_image);
                if (localThumb != null) {
                    showVideoThumbView(localThumb, imageView, videoBody.getThumbnailUrl(), message);
                }

            }

            return;
        }
        //处理发送方消息
        handleSendMessage();
    }

    @Override
    protected void onBubbleClick() {
        EMVideoMessageBody videoBody = (EMVideoMessageBody) message.body();
        Log.d(TAG, "video view is on click");
        Intent intent = new Intent(context, ShowVideoActivity.class);
        intent.putExtra("msg", message);
        activity.startActivity(intent);
    }

    /**
     * 展示视频缩略图
     *
     * @param localThumb   本地缩略图路径
     * @param iv
     * @param thumbnailUrl 远程缩略图路径
     * @param message
     */
    @SuppressLint("StaticFieldLeak")
    private void showVideoThumbView(final String localThumb, final ImageView iv, String thumbnailUrl, final Message message) {
        // first check if the thumbnail image already loaded into cache
        Uri localFullSizePath = Uri.parse(localThumb);
        Uri thumbernailPath = Uri.parse(thumbnailUrl);
        Bitmap bitmap = ImageCache.getInstance().get(localThumb);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);

        } else {
            final int width = DensityUtil.dip2px(getContext(), 150);
            new AsyncTask<Void, Void, Bitmap>() {
                private Bitmap getCacheBitmap(Uri fileUri) {
                    String filePath = UriUtils.getFilePath(context, fileUri);
                    EMLog.d(TAG, "fileUri = " + fileUri);
                    if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                        return ImageUtils.decodeScaleImage(filePath, width, width);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            return ImageUtils.decodeScaleImage(context, fileUri, width, width);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }

                @Override
                protected Bitmap doInBackground(Void... params) {
//                    if (new File(localThumb).exists()) {
//                        return ImageUtils.decodeScaleImage(localThumb, 160, 160);
//                    } else {
//                        return null;
//                    }
                    if (UriUtils.isFileExistByUri(context, thumbernailPath)) {
                        return getCacheBitmap(thumbernailPath);
                    } else if(UriUtils.isFileExistByUri(context, localFullSizePath)) {
                        return getCacheBitmap(localFullSizePath);
                    } else {
                        if (message.direct() == Message.Direct.SEND) {
                            if (UriUtils.isFileExistByUri(context, localFullSizePath)) {
                                String filePath = UriUtils.getFilePath(context, localFullSizePath);
                                if(!TextUtils.isEmpty(filePath)) {
                                    return ImageUtils.decodeScaleImage(filePath, width, width);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    try {
                                        return ImageUtils.decodeScaleImage(context, localFullSizePath, width, width);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        return null;
                                    }
                                }
                            }
                            return null;
                        }
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Bitmap result) {
                    super.onPostExecute(result);
                    if (result != null) {
                        ImageCache.getInstance().put(localThumb, result);
                        iv.setImageBitmap(result);

                    } else {
                        if (message.status() == Message.Status.SUCCESS) {
                            if (CommonUtils.isNetWorkConnected(activity)) {
                                ChatClient.getInstance().chatManager().downloadThumbnail(message);
                            }
                        }

                    }
                }
            }.execute();
        }

    }


}