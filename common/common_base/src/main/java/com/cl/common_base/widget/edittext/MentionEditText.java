package com.cl.common_base.widget.edittext;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;

import com.cl.common_base.widget.edittext.bean.FormatItemResult;
import com.cl.common_base.widget.edittext.bean.FormatRange;
import com.cl.common_base.widget.edittext.bean.FormatResult;
import com.cl.common_base.widget.edittext.bean.MentionTopic;
import com.cl.common_base.widget.edittext.bean.MentionUser;
import com.cl.common_base.widget.edittext.bean.Range;
import com.cl.common_base.widget.edittext.listener.EditDataListener;
import com.cl.common_base.widget.edittext.listener.InsertData;
import com.cl.common_base.widget.edittext.listener.MentionInputConnection;
import com.cl.common_base.widget.edittext.listener.MentionTextWatcher;
import com.cl.common_base.widget.edittext.listener.SpanClickHelper;
import com.cl.common_base.widget.edittext.util.ClipboardHelper;
import com.cl.common_base.widget.edittext.util.FormatRangeManager;
import com.cl.common_base.widget.edittext.util.RangeManager;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 发布页编辑框
 * 可插入@用户、#话题
 */
public class MentionEditText extends AppCompatEditText {
    private final static String TAG = MentionEditText.class.toString();

    private Runnable mAction;

    private boolean mIsSelected;
    private ClipboardManager mClipboardManager;
    private MentionInputConnection mentionInputConnection;
    private GestureDetector gestureDetector;
    private MotionEvent upMotionEvent;
    private EditDataListener mEditDataListener;

    /**
     * 高亮区域能否编辑 true：高亮区域能编辑（编辑后不再高亮）  false：高亮区域不能编辑（光标不能移到高亮区域）
     */
    private static final boolean isEnableEditRange = false;

    /**
     * 高亮区域删除时 是否先选中再删除
     * isEnableEditRange 为 false 时生效
     */
    private static final boolean isEnableSelectionByDelete = false;

    /**
     * 高亮区域点击是否选中
     * isEnableEditRange 为 false 时生效
     */
    private static final boolean isEnableSelection = false;

    public MentionEditText(Context context) {
        super(context);
        init();
    }

    public MentionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MentionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setEditDataListener(EditDataListener listener) {
        this.mEditDataListener = listener;
    }

    /**
     * 监听删除输入事件
     *
     * @param outAttrs
     * @return
     */
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        mentionInputConnection = new MentionInputConnection(super.onCreateInputConnection(outAttrs), true, this, isEnableEditRange, isEnableSelectionByDelete);
        return mentionInputConnection;
    }

    @Override
    public void setText(final CharSequence text, BufferType type) {
        super.setText(text, type);
        //hack, put the cursor at the end of text after calling setText() method
        if (mAction == null) {
            mAction = new Runnable() {
                @Override
                public void run() {
                    setSelection(getText().length());
                }
            };
        }
        post(mAction);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (null != mRangeManager && !mRangeManager.isEqual(selStart, selEnd)) {
            Range closestRange = mRangeManager.getRangeOfClosestMentionString(selStart, selEnd);
            if (closestRange != null && closestRange.getTo() == selEnd) {
                mIsSelected = false;
            }

            if (!isEnableEditRange) {
                //WT 后面可能需要 禁止游标移动到字符串中
                Range nearbyRange = mRangeManager.getRangeOfNearbyMentionString(selStart, selEnd);
                if (null != nearbyRange) {
                    // 禁止游标移动到字符串中
                    if (selStart == selEnd) {
                        setSelection(nearbyRange.getAnchorPosition(selStart));
                    } else {
                        if (selStart >= nearbyRange.getFrom() && selEnd < nearbyRange.getTo()) {
                            setSelection(nearbyRange.getFrom(), nearbyRange.getTo());
                        } else if (selEnd < nearbyRange.getTo()) {
                            setSelection(selStart, nearbyRange.getTo());
                        } else if (selStart > nearbyRange.getFrom()) {
                            setSelection(nearbyRange.getFrom(), selEnd);
                        }
                    }
                }
            }
        }
    }

    /**
     * 原有文字区域替换为高亮
     */
    public void insertRecover(InsertData insertData, int start, int length) {
        if (null == insertData) {
            return;
        }

        CharSequence charSequence = insertData.charSequence();
        Editable editable = getText();
        int end = start + length;

        FormatRange.FormatData format = insertData.formatData();
        final FormatRange range = new FormatRange(start, end);
        range.setInsertData(insertData);
        range.setConvert(format);
        range.setRangeCharSequence(charSequence);
        mRangeManager.add(range);

        //设置部分文字点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Log.d(TAG, "-->>点击了话题");
                widget.post(new Runnable() {
                    @Override
                    public void run() {
                        // 这里不能使用 start 和 end，因为选中再进行输入操作时，有可能会改变range的的范围
                        if (!isEnableEditRange && isEnableSelection) {
                            //WT 后面可能需要 点击选中
                            setSelection(range.getFrom(), range.getTo());
                        }
                    }
                });
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
        editable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int color = insertData.color();
        editable.setSpan(new ForegroundColorSpan(color), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    public void remove(InsertData insertData) {
        if (insertData == null) {
            return;
        }
        Editable editable = getText();
        if (editable == null) {
            return;
        }

        String mentionChar = getText().toString();
        if (insertData instanceof MentionUser) {
            int start = mentionChar.indexOf((String) ("@" + ((MentionUser) insertData).getUserName()));
            int end = mentionChar.indexOf(" ", start);
            editable.delete(start, end + 1);// 多删除后面的空格，不然会导致多个空格
            for (int i = 0; i < mRangeManager.get().size(); i++) {
                if (mRangeManager.get().get(i).getInsertData() instanceof MentionUser) {
                    MentionUser insertData1 = (MentionUser) mRangeManager.get().get(i).getInsertData();
                    if (insertData1.getUserId() == ((MentionUser) insertData).getUserId()) {
                        mRangeManager.get().remove(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 光标位置插入高亮
     */
    public void insert(InsertData insertData) {
        if (insertData == null) {
            return;
        }

        CharSequence charSequence = insertData.charSequence();
        Editable editable = getText();
        if (editable == null) {
            return;
        }
        int start = getSelectionStart();
        final int oldEnd = getSelectionEnd();
        int end = start + charSequence.length();

        // 如果是选中状态
        if (start != oldEnd) {
            replaceSelection(insertData);
            return;
        }

        String mentionChar = getText().toString();
        mentionChar = mentionChar.substring(0, start);

        //删除
        if (insertData instanceof MentionTopic) {
            //#话题
            if (mentionChar.endsWith("#")) {
                start = start - 1;
                end = start + charSequence.length();
                editable.delete(start, start + 1);
            }
        } else if (insertData instanceof MentionUser) {
            //@用户
            String[] atList = mentionChar.split("@");
            if (mentionChar.endsWith("@")) {
                //最后一位是@
                start = start - 1;
                end = start + charSequence.length();
                editable.delete(start, start + 1);
            } else if (!atList[atList.length - 1].contains(" ") && !atList[atList.length - 1].contains("\n") && !atList[atList.length - 1].contains("#")) {
                //最后一位 不是空、换行、#
                start = mentionChar.lastIndexOf(atList[atList.length - 1]) - 1;
                end = start + charSequence.length();
                editable.delete(start, start + atList[atList.length - 1].length() + 1);
            }
        }
        //插入
        editable.insert(start, charSequence);

        FormatRange.FormatData format = insertData.formatData();
        final FormatRange range = new FormatRange(start, end);
        range.setInsertData(insertData);
        range.setConvert(format);
        range.setRangeCharSequence(charSequence);
        mRangeManager.add(range);

        //设置部分文字点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Log.d("TAG", "-->>点击了话题");
                widget.post(new Runnable() {
                    @Override
                    public void run() {
                        // 这里不能使用 start 和 end，因为选中再进行输入操作时，有可能会改变range的的范围
                        if (!isEnableEditRange && isEnableSelection) {
                            //WT 后面可能需要 点击选中
                            setSelection(range.getFrom(), range.getTo());
                        }
                    }
                });
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
        editable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int color = insertData.color();
        editable.setSpan(new ForegroundColorSpan(color), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    }

    /**
     * 艾特用户的数量
     */
    public int getUserLength() {
        int i = 0;
        ArrayList<? extends Range> ranges = mRangeManager.get();
        for (Range range : ranges) {
            if (range instanceof FormatRange) {
                FormatRange formatRange = (FormatRange) range;
                if (formatRange.getInsertData() instanceof MentionUser) {
                    i++;
                }
            }
        }
        return i;
    }

    /**
     * 话题的数量
     */
    public int getTopicLength() {
        int i = 0;
        ArrayList<? extends Range> ranges = mRangeManager.get();
        for (Range range : ranges) {
            if (range instanceof FormatRange) {
                FormatRange formatRange = (FormatRange) range;
                if (formatRange.getInsertData() instanceof MentionTopic) {
                    i++;
                }
            }
        }
        return i;
    }

    private void replaceSelection(InsertData insertData) {
        CharSequence charSequence = insertData.charSequence();
        Editable editable = getText();
        final int start = getSelectionStart();
        final int oldEnd = getSelectionEnd();
        final int end = start + charSequence.length();
        // 先删除 后插入
        editable.delete(start, oldEnd);
        editable.insert(start, charSequence);
        Log.d(TAG, "-->>" + editable);

        FormatRange.FormatData format = insertData.formatData();
        FormatRange range = new FormatRange(start, end);
        range.setInsertData(insertData);
        range.setConvert(format);
        range.setRangeCharSequence(charSequence);
        mRangeManager.add(range);

        //设置部分文字点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                widget.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isEnableEditRange && isEnableSelection) {
                            //WT 后面可能需要 点击选中
                            setSelection(start, end);
                        }
                    }
                });
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
        editable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int color = insertData.color();
        editable.setSpan(new ForegroundColorSpan(color), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        setSelection(start, end);
    }

    public void insert(CharSequence charSequence) {
        insert(new Default(charSequence));
    }

    class Default implements InsertData {

        private final CharSequence charSequence;

        public Default(CharSequence charSequence) {
            this.charSequence = charSequence;
        }

        @Override
        public CharSequence charSequence() {
            return charSequence;
        }

        @Override
        public FormatRange.FormatData formatData() {
            return new DEFAULT();
        }

        @Override
        public int color() {
            return Color.RED;
        }

        class DEFAULT implements FormatRange.FormatData {
            @Override
            public FormatItemResult formatResult() {
                return null;
            }
        }
    }

    /**
     * 结果 文字的所有集合
     */
    public FormatResult getFormatResult() {
        String text = getText().toString();
        return mRangeManager.getFormatResult(text);
    }

    public void clear() {
        mRangeManager.clear();
        setText("");
    }

    protected FormatRangeManager mRangeManager;

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mClipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        }
        mRangeManager = new FormatRangeManager();
        //disable suggestion
        addTextChangedListener(new MentionTextWatcher(this));

        // 长按监听 去除复制的格式
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "-->>edittext 长按监听");
                String clipText = ClipboardHelper.getInstance(getContext()).getClipText(getContext());
                if (!TextUtils.isEmpty(clipText)) {
                    // 保存无格式的 text
                    ClipData simple_text = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        simple_text = ClipData.newPlainText("simple_text", clipText);
                        mClipboardManager.setPrimaryClip(simple_text);
                    }
                }
                // 返回true，将拦截长按复制、粘贴功能
                return false;
            }
        });

        // 点击话题选中后，点击一次edittext不响应点击事件 ，再点一次才会响应
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "-->>onClick");
                setSelected(false);
                // 重置索引保护
                if (mentionInputConnection != null) {
                    mentionInputConnection.resetLastIndex();
                }

                if (upMotionEvent != null) {
                    SpanClickHelper.spanClickHandle(MentionEditText.this, upMotionEvent);
                    upMotionEvent = null;
                }

                Log.d(TAG, "-->>selStart=" + getSelectionStart() + ",selEnd=" + getSelectionEnd());
            }
        });

        // 无法实现需求，因为onTouch的up事件返回true时，不会进入onTouchEvent方法，无法取消长按的监听，所以就触发了onLongClick
//        setOnTouchListener(new LinkTouchMethod());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            gestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    //                upMotionEvent = e;
                    Log.d(TAG, "-->>点击up " + e.toString());
                    return false;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    return false;
                }
            });
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            action = event.getActionMasked();
        }
        if (action == MotionEvent.ACTION_UP) {
            // 必须保存MotionEvent副本，因为会被回收
            this.upMotionEvent = MotionEvent.obtain(event);
        }
        /*if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }*/
        return super.onTouchEvent(event);
    }

    public RangeManager getRangeManager() {
        return mRangeManager;
    }

    public MentionInputConnection getMentionInputConnection() {
        return mentionInputConnection;
    }

    public EditDataListener getEditDataListener() {
        return mEditDataListener;
    }

    @Override
    public boolean isSelected() {
        return mIsSelected;
    }

    @Override
    public void setSelected(boolean selected) {
        mIsSelected = selected;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        if (id == android.R.id.paste) {
            //拦截粘贴文本样式
            ClipboardManager clipboardManager = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            }
            assert clipboardManager != null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (clipboardManager.hasPrimaryClip()) {
                    String text = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
                    final int start = getSelectionStart();
                    final int oldEnd = getSelectionEnd();
                    // 先删除 后插入
                    Editable editable = getText();
                    if (start != oldEnd) {
                        editable.delete(start, oldEnd);
                    }
                    editable.insert(start, text);
                }
            }
            return true;
        }
        return super.onTextContextMenuItem(id);
    }

    public void restore(String content, List<Range> ranges) {
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        Collections.sort(ranges);
        for (Range range : ranges) {
            String matchTag = range.getInsertData().charSequence().toString();
            String substring = content.substring(range.getFrom(), range.getTo());
            if (TextUtils.equals(matchTag, substring)) {
                Log.d(TAG, "-->> " + range.getInsertData().charSequence() + " , start=" + range.getFrom() + ", end=" + range.getTo());
                mRangeManager.add(range);
                // 设置颜色
                int color = range.getInsertData().color();
                builder.setSpan(new ForegroundColorSpan(color), range.getFrom(), range.getTo(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        setText(builder);
    }

    public void restore(String content) {
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        ArrayList<? extends Range> ranges = mRangeManager.get();
        Collections.sort(ranges);
        for (Range range : ranges) {
            String matchTag = range.getInsertData().charSequence().toString();
            String substring = content.substring(range.getFrom(), range.getTo());
            if (TextUtils.equals(matchTag, substring)) {
                Log.d(TAG, "-->> " + range.getInsertData().charSequence() + " , start=" + range.getFrom() + ", end=" + range.getTo());
                mRangeManager.add(range);
                // 设置颜色
                int color = range.getInsertData().color();
                builder.setSpan(new ForegroundColorSpan(color), range.getFrom(), range.getTo(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        setText(builder);
    }
}