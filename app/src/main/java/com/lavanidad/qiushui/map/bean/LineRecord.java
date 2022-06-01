package com.lavanidad.qiushui.map.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * @Author : fzy
 * @Date : 2022/4/11
 * @Description :
 */
public class LineRecord {

    public Bitmap bitmap;
    public Matrix matrix;
    public RectF lineOrigin = new RectF();
    public float scaleMax = 3;
    public String name;
    public boolean isChecked = false;
    public boolean canSelected = true;//是否能操作

    public float curLineLength;
}
