package com.cl.common_base.widget.slidetoconfirmlib;

public interface ISlideListener {

    void onSlideStart();

    void onSlideMove(float percent);

    void onSlideCancel();

    void onSlideDone();
}
