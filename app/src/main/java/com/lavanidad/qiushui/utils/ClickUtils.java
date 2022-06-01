package com.lavanidad.qiushui.utils;

import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;

public class ClickUtils {
    private static long lastClickTime = 0;
    private static long DIFF = 800;
    private static int lastButtonId = -1;

    /**
     * 判断两次点击的间隔，如果小于1000，则认为是多次无效点击
     *
     * @return
     */
    public static boolean isFastDoubleClick() {
        return isFastDoubleClick(-1, DIFF);
    }

    /**
     * 判断两次点击的间隔，如果小于1000，则认为是多次无效点击
     *
     * @return
     */
    public static boolean isFastDoubleClick(int viewId) {
        return isFastDoubleClick(viewId, DIFF);
    }

    /**
     * 判断两次点击的间隔，如果小于diff，则认为是多次无效点击
     *
     * @param diff
     * @return
     */
    public static boolean isFastDoubleClick(int viewId, long diff) {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (lastButtonId == viewId && lastClickTime > 0 && timeD < diff) {
            ToastUtils.showShort("点击过快，稍后再试");
            Log.v("onClick", "isFastDoubleClick短时间内按钮多次触发");
            return true;
        }
        lastClickTime = time;
        lastButtonId = viewId;
        return false;
    }
}