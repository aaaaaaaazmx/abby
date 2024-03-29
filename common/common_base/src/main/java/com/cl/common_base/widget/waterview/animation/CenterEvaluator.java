package com.cl.common_base.widget.waterview.animation;

import android.animation.TypeEvaluator;
import android.graphics.Point;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * @author wodx521
 * @date on 2018/10/24
 */
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class CenterEvaluator implements TypeEvaluator {
    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        Point startPoint = (Point) startValue;
        Point endPoint = (Point) endValue;
        float mX = startPoint.x + fraction * (endPoint.x - startPoint.x);
        float mY = startPoint.y + fraction * (endPoint.y - startPoint.y);
        //      这里会产生从开始画滑动到结束的所有坐标
        return new Point((int)mX, (int)mY);
    }
}
