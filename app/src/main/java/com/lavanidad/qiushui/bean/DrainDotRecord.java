package com.lavanidad.qiushui.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * @Author : fzy
 * @Date : 2022/4/12
 * @Description :
 */
public class DrainDotRecord {

    public Bitmap bitmap;
    public Matrix matrix;
    public RectF rectOrigin = new RectF();

    @Override
    public String toString() {
        return "DrainDotRecord{" +
                "bitmap=" + bitmap +
                ", matrix=" + matrix +
                ", rectOrigin=" + rectOrigin +
                '}';
    }
}
