package com.tencent.qcloud.tuikit.tuichat.util;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewParent;

public class ViewUtils {
 
    /**
     * 扩大点击区域
     * @param view 要扩展点击区域的视图
     * @param size 扩展的大小
     */
    public static void expandTouchArea(View view, int size) {
        ViewParent parentView = view.getParent();
        if (parentView instanceof View) {
            ((View) parentView).post(new Runnable() {
                @Override
                public void run() {
                    Rect rect = new Rect();
                    view.getHitRect(rect);
                    rect.top -= size;
                    rect.bottom += size;
                    rect.left -= size;
                    rect.right += size;
                    ((View) parentView).setTouchDelegate(new TouchDelegate(rect, view));
                }
            });
        }
    }

    /**
     * 扩大View的触摸和点击响应范围,最大不超过其父View范围
     * 若想在一个父布局扩大多个View的点击区域，可以在view嵌套一个RelativeLayout或LinearLayout布局，这样就一个父布局对应一个View
     * 如果控件的位置有变化，那要在变化之后再调用此方法
     * @param view
     * @param top
     * @param bottom
     * @param left
     * @param right
     */
    public static void expandViewTouchDelegate(final View view, final int top,
                                               final int bottom, final int left, final int right) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);

                bounds.top -= top;
                bounds.bottom += bottom;
                bounds.left -= left;
                bounds.right += right;
                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);
                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }
}