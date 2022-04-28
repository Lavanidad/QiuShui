package com.lavanidad.qiushui.map.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;


public class DotRecord {

    public Bitmap bitmap;
    public Matrix matrix;
    public RectF rectOrigin = new RectF();
    public String name;
    public boolean isChecked = false;
    public boolean canSelected = true;//是否能操作
}
