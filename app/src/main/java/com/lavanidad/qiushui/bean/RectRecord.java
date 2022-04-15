package com.lavanidad.qiushui.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;


public class RectRecord {

    public Bitmap bitmap;
    public Matrix matrix;
    public RectF rectOrigin = new RectF();
    public float scaleMax = 3;

    public float curRectWidth;
    public float curRectHeight;

    @Override
    public String toString() {
        return "RectRecord{" +
                "bitmap=" + bitmap +
                ", matrix=" + matrix +
                ", rectOrigin=" + rectOrigin +
                ", scaleMax=" + scaleMax +
                '}';
    }
}
