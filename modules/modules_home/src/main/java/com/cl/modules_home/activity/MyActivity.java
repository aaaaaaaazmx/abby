package com.cl.modules_home.activity;

import static com.cl.common_base.ext.DensityKt.dp2px;
import static com.cl.common_base.ext.LogKt.logI;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bbgo.module_home.R;

/**
 * This is a short description.
 *
 * @author 李志军 2023-06-15 21:32
 */
public class MyActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CenterLayoutManager layoutManager;
    private MyAdapter adapter;

    interface OnScrollListener {
        void onScrolled(int position);
    }

    private OnScrollListener scrollListener = new OnScrollListener() {
        @Override
        public void onScrolled(int position) {
            adapter.setFocusedPosition(position);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_my);

        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = new CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        String[] letters = {"动图", "视频", "照片", "文字", "文字"};
        int recyclerViewWidth = recyclerView.getLayoutParams().width;
        if (recyclerViewWidth == -1) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            recyclerViewWidth = displayMetrics.widthPixels;
        }

        int targetPosition = 1;
        adapter = new MyAdapter(letters, this, recyclerViewWidth, recyclerView);
        SnapHelper snapHelper = new LinearSnapHelper();
        recyclerView.setAdapter(adapter);
        adapter.setFocusedPosition(targetPosition);
        snapHelper.attachToRecyclerView(recyclerView);


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE  && !recyclerView.isComputingLayout()) {
                    View centerView = snapHelper.findSnapView(layoutManager);
                    int pos = layoutManager.getPosition(centerView);
                    scrollListener.onScrolled(pos);
                    logI("onScrollStateChanged: pos = " + pos);
                }
            }
        });
    }
}
 class CenterLayoutManager extends LinearLayoutManager {
    public CenterLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    private static class CenterSmoothScroller extends LinearSmoothScroller {

        CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }
    }
}


class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String[] letters;
    private static final int VIEW_TYPE_BLANK = 0;
    private static final int VIEW_TYPE_LETTER = 1;
    private int itemWidth;

    private int blankWidth;

    private RecyclerView recyclerView;
    private int focusedPosition = -1;

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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_BLANK) {
            View view = new View(parent.getContext());
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(itemWidth - dp2px(20), parent.getHeight());
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
                letterView.setTextColor(Color.parseColor("#006241"));  // Change color to red
            } else {
                letterView.setTextColor(Color.BLACK);  // Original color
            }

            // Add click listener to smoothly scroll to clicked item
            holder.itemView.setOnClickListener(v -> {
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






