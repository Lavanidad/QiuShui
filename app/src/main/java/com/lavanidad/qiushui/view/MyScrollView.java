package com.lavanidad.qiushui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Create By XHD On 2022/04/12
 * Description：
 **/
public class MyScrollView extends ConstraintLayout {

    public MyScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private float downX, downY;
    private float disX, disY;

    //子view不消费事件，就交给容器处理，点击空白处即可拖动
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX() + disX;
                downY = event.getY() + disY;
                break;
            case MotionEvent.ACTION_MOVE:
                disX = downX - event.getX();
                disY = downY - event.getY();
                scrollTo((int) disX, (int) disY);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }
    //此处案例直接屏蔽所有子view事件，如果需要传事件给子view，根据自己需求调这个方法super.dispatchTouchEvent(event)
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                downX = event.getX() + disX;
//                downY = event.getY() + disY;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                disX = downX - event.getX();
//                disY = downY - event.getY();
//                scrollTo((int) disX, (int) disY);
//                break;
//            case MotionEvent.ACTION_UP:
//                break;
//        }
//        return true;
//    }
}
