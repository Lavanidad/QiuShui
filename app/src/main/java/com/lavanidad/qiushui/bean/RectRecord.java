package com.lavanidad.qiushui.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;


public class RectRecord {

    public Bitmap bitmap;
    public Matrix matrix;
    public RectF rectSrc = new RectF();
    public float scaleMax = 3;

    @Override
    public String toString() {
        return "RectRecord{" +
                "bitmap=" + bitmap +
                ", matrix=" + matrix +
                ", rectSrc=" + rectSrc +
                ", scaleMax=" + scaleMax +
                '}';
    }
}
