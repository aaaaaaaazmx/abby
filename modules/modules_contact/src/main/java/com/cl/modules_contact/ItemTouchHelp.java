package com.cl.modules_contact;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cl.modules_contact.adapter.ChooserAdapter;
import com.cl.modules_contact.response.ChoosePicBean;
import com.cl.modules_contact.ui.PostActivity;

import java.util.Collections;
import java.util.List;

public class ItemTouchHelp extends ItemTouchHelper.Callback {

    private ChooserAdapter mAdapter;
    private boolean needScaleBig = true;
    private boolean needScaleSmall = false;
    private boolean isHasLiftDelete;

    public ItemTouchHelp(ChooserAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int itemViewType = viewHolder.getItemViewType();
        if (itemViewType != ChoosePicBean.KEY_TYPE_ADD) {
            viewHolder.itemView.setAlpha(0.7f);
        }
        return makeMovementFlags(ItemTouchHelper.DOWN | ItemTouchHelper.UP
                | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        try {
            //得到item原来的position
            int fromPosition = viewHolder.getAbsoluteAdapterPosition();
            //得到目标position
            int toPosition = target.getAbsoluteAdapterPosition();
            int itemViewType = target.getItemViewType();
            if (itemViewType != ChoosePicBean.KEY_TYPE_ADD) {
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(mAdapter.getData(), i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(mAdapter.getData(), i, i - 1);
                    }
                }
                if (mListener != null) {
                    mListener.onItemSwap(fromPosition, toPosition);
                }
                mAdapter.notifyItemMoved(fromPosition, toPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dx, float dy, int actionState, boolean isCurrentlyActive) {
        int itemViewType = viewHolder.getItemViewType();
        if (itemViewType != ChoosePicBean.KEY_TYPE_ADD) {
            if (needScaleBig) {
                needScaleBig = false;
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(
                        ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.0F, 1.1F),
                        ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.0F, 1.1F));
                animatorSet.setDuration(50);
                animatorSet.setInterpolator(new LinearInterpolator());
                animatorSet.start();
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        needScaleSmall = true;
                    }
                });
            }
                /*int targetDy = tvDeleteText.getTop() - viewHolder.itemView.getBottom();
                if (dy >= targetDy) {
                    //拖到删除处
                    mDragListener.deleteState(true);
                    if (isHasLiftDelete) {
                        //在删除处放手，则删除item
                        viewHolder.itemView.setVisibility(View.INVISIBLE);
                        mAdapter.delete(viewHolder.getAbsoluteAdapterPosition());
                        resetState();
                        return;
                    }
                } else {
                    //没有到删除处
                    if (View.INVISIBLE == viewHolder.itemView.getVisibility()) {
                        //如果viewHolder不可见，则表示用户放手，重置删除区域状态
                        mDragListener.dragState(false);
                    }
                    mDragListener.deleteState(false);
                }*/
            super.onChildDraw(c, recyclerView, viewHolder, dx, dy, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        int itemViewType = viewHolder != null ? viewHolder.getItemViewType() : ChoosePicBean.KEY_TYPE_ADD;
        if (itemViewType != ChoosePicBean.KEY_TYPE_ADD) {
            if (ItemTouchHelper.ACTION_STATE_DRAG == actionState) {
                // mDragListener.dragState(true);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }
    }

    @Override
    public long getAnimationDuration(@NonNull RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
        isHasLiftDelete = true;
        return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int itemViewType = viewHolder.getItemViewType();
        if (itemViewType != ChoosePicBean.KEY_TYPE_ADD) {
            viewHolder.itemView.setAlpha(1.0F);
            if (needScaleSmall) {
                needScaleSmall = false;
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(
                        ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.1F, 1.0F),
                        ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.1F, 1.0F));
                animatorSet.setInterpolator(new LinearInterpolator());
                animatorSet.setDuration(50);
                animatorSet.start();
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        needScaleBig = true;
                    }
                });
            }
            super.clearView(recyclerView, viewHolder);
            mAdapter.notifyItemChanged(viewHolder.getAbsoluteAdapterPosition());
            resetState();
        }
    }

    private void resetState() {
        isHasLiftDelete = false;
        /*mDragListener.deleteState(false);
        mDragListener.dragState(false);*/
    }

    // 添加交换的事件接口
    public interface OnItemSwapListener {
        void onItemSwap(int fromPosition, int toPosition);
    }
    private OnItemSwapListener mListener;
    public void setOnItemSwapListener(OnItemSwapListener listener) {
        mListener = listener;
    }
}

