package com.jxw.basemodel.utils;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.jxw.basemodel.R;
import com.jxw.validate.utils.LauncherDataUtils;


public class NotchUtils {
    private static String TAG = NotchUtils.class.getSimpleName();

    /**
     * 用于获取状态栏的高度。 使用Resource对象获取（推荐这种方式）
     *
     * @return 返回状态栏高度的像素值。
     */
    private static int getStatusBarHeight(Context context) {

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * 杂牌刘海屏判断
     * Has notch at miscellaneous boolean.
     *
     * @return the boolean
     */
    public static boolean hasNotchAtMiscellaneous() {
        return LauncherDataUtils.cutoutType != 0;
    }

    public static int getMiscellaneousNotchHeightIfHasNotch(Context context) {
        if (!hasNotchAtMiscellaneous()) {
            return 0;
        }
        int notchHeight = getMiscellaneousNotchHeight(context);
        int statusBarHeight = getStatusBarHeight(context);
        Log.i(TAG, "日志--------杂牌手机------> notchHeight:" + notchHeight + "------>statusBarHeight:" + statusBarHeight);
        if (notchHeight < statusBarHeight) {
            notchHeight = statusBarHeight;
        }
        return notchHeight;
    }

    public static void addFlags(Activity activity, FlagCallback callback) {
        if (LauncherDataUtils.cutoutType == 1) {
            ImmersionBar bar = ImmersionBar.with(activity);
            ImmersionBar.showStatusBar(activity.getWindow());
            bar.transparentStatusBar();
            bar.statusBarDarkFont(true);
            bar.statusBarColor(R.color.white);
            bar.hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR);
            bar.init();
        } else if (LauncherDataUtils.cutoutType == 2) {
            ImmersionBar.
                    with(activity).
                    hideBar(BarHide.FLAG_HIDE_BAR).
                    init();
        } else {
            if (callback != null) {
                callback.onNotchCommon();
            }
        }
    }

    /**
     * 获得魅族刘海屏高度
     * Gets meizu notch height.
     *
     * @param context the context
     * @return the meizu notch height
     */
    private static int getMiscellaneousNotchHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.miscellaneous_phone_notch_height);
    }

    public interface FlagCallback {
        void onNotchCommon();
    }
}
