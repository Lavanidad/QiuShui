package com.lavanidad.qiushui.bean;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import java.util.Arrays;

/**
 * @Author : fzy
 * @Date : 2022/4/12
 * @Description :
 */
public class StrokeRecord {

    public Paint paint;//笔类
    public Path path;//画笔路径数据
    public PointF[] linePoints; //线数据
   // public Matrix matrix;

    public StrokeRecord() {
    }

    @Override
    public String toString() {
        return "StrokeRecord{" +
                "paint=" + paint.toString() +
                ", path=" + path.toString() +
                ", linePoints=" + Arrays.toString(linePoints) +
                '}';
    }
}
