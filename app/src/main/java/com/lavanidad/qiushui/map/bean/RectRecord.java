package com.lavanidad.qiushui.map.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;


public class RectRecord {

    public Bitmap bitmap;
    public Matrix matrix;
    public RectF rectOrigin = new RectF();
    public float curRectWidth;
    public float curRectHeight;
    public String name;
    public boolean isChecked = false;
    public boolean canSelected = true;//是否能操作
}
