package com.lavanidad.qiushui.map.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;


public class DrawRecord {

    public Bitmap bitmap;
    public Matrix matrix = new Matrix();
    public RectF rectOrigin = new RectF();
    public Rect rect = new Rect();
    public float mRotation = 0;
    public String name;
    public boolean isChecked = false;
    public int type;//0 rect  1:line 2:water 3:drain

}
