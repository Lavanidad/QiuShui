package com.lavanidad.qiushui.map.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * @Author : fzy
 * @Date : 2022/4/25
 * @Description :
 */
public class DrawRecord {

    public Bitmap bitmap;
    public Matrix matrix = new Matrix();
    public RectF rectOrigin = new RectF();
    public String name;
    public boolean isChecked = false;
    public float mRotation = 0;
    public int type;//用于区分绘制的图形 0：rect  1:line 2:water 3:drain
}
