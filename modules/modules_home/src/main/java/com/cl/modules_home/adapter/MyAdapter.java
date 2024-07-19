package com.cl.modules_home.adapter;

import static android.graphics.Paint.FAKE_BOLD_TEXT_FLAG;
import static com.cl.common_base.ext.DensityKt.dp2px;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cl.modules_home.R;
import com.cl.common_base.widget.toast.ToastUtil;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String[] letters;
    private static final int VIEW_TYPE_BLANK = 0;
    private static final int VIEW_TYPE_LETTER = 1;
    private int itemWidth;

    private int blankWidth;

    private RecyclerView recyclerView;
    private int focusedPosition = -1;

    private boolean shouldDisableClick = true;

    public boolean isShouldDisableClick() {
        return shouldDisableClick;
    }

    public void setShouldDisableClick(boolean shouldDisableClick) {
        this.shouldDisableClick = shouldDisableClick;
    }

    public MyAdapter(String[] letters, final Context context, int recyclerViewWidth, RecyclerView recyclerView) {
        this.letters = letters;
        this.itemWidth = recyclerViewWidth / 2;
        this.recyclerView = recyclerView;
    }

    public void setFocusedPosition(int focusedPosition) {
        if (this.focusedPosition != focusedPosition) {
            int previousFocus = this.focusedPosition;
            this.focusedPosition = focusedPosition;
            if (previousFocus >= 0) notifyItemChanged(previousFocus);
            notifyItemChanged(focusedPosition);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == letters.length + 1) {
            return VIEW_TYPE_BLANK;
        } else {
            return VIEW_TYPE_LETTER;
        }
    }

    public int getFocusedPosition() {
        return focusedPosition;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_BLANK) {
            View view = new View(parent.getContext());
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(itemWidth - dp2px(35), parent.getHeight());
            view.setLayoutParams(params);
            return new RecyclerView.ViewHolder(view) {
            };
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item_letter, parent, false);
            return new LetterViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_LETTER) {
            TextView letterView = ((LetterViewHolder) holder).letterView;
            letterView.setText(letters[position - 1]);
            if (position == focusedPosition) {
                letterView.setTextColor(Color.parseColor("#4CD964"));  // Change color to red
                // Set the font to the TextView

                // Set the text to bold
                letterView.setPaintFlags(letterView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            } else {
                letterView.setTextColor(Color.WHITE);  // Original color
            }

            // Add click listener to smoothly scroll to clicked item
            holder.itemView.setOnClickListener(v -> {
                if (!shouldDisableClick) {
                    ToastUtil.shortShow("Please stop the current mode first");
                    return;
                }
                recyclerView.smoothScrollToPosition(position);
                setFocusedPosition(position);
            });

        }
    }

    @Override
    public int getItemCount() {
        return letters.length + 2;
    }

    public String getLetter(int position) {
        if (position > 0 && position <= letters.length) {
            return letters[position - 1];
        } else {
            return null;
        }
    }

    static class LetterViewHolder extends RecyclerView.ViewHolder {
        TextView letterView;

        LetterViewHolder(View view) {
            super(view);
            letterView = view.findViewById(R.id.letter_view);
        }
    }
}
