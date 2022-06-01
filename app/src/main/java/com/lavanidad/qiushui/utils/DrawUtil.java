package com.lavanidad.qiushui.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.lavanidad.qiushui.map.bean.BackgroundRecord;
import com.lavanidad.qiushui.map.bean.DotRecord;
import com.lavanidad.qiushui.map.bean.DrawRecord;
import com.lavanidad.qiushui.map.bean.LineRecord;
import com.lavanidad.qiushui.map.bean.RectRecord;



public class DrawUtil {

    /**
     * 计算矩形四个角点和中心点
     *
     * @param record
     * @return
     */
    public static float[] calculateCorners(RectRecord record) {
        float[] rectCornersSrc = new float[10];
        float[] rectCorners = new float[10];
        RectF rectF = record.rectOrigin;
        //0,1代表左上角点XY
        rectCornersSrc[0] = rectF.left;
        rectCornersSrc[1] = rectF.top;
        //2,3代表右上角点XY
        rectCornersSrc[2] = rectF.right;
        rectCornersSrc[3] = rectF.top;
        //4,5代表右下角点XY
        rectCornersSrc[4] = rectF.right;
        rectCornersSrc[5] = rectF.bottom;
        //6,7代表左下角点XY
        rectCornersSrc[6] = rectF.left;
        rectCornersSrc[7] = rectF.bottom;
        //8,9代表中心点XY
        rectCornersSrc[8] = rectF.centerX();
        rectCornersSrc[9] = rectF.centerY();
        record.matrix.mapPoints(rectCorners, rectCornersSrc);
        return rectCorners;
    }

    public static float[] calculateRectOrigin(DrawRecord record) {
        float[] rectCornersSrc = new float[4];
        RectF rectF = record.rectOrigin;
        rectCornersSrc[0] = rectF.left;
        rectCornersSrc[1] = rectF.top;
        rectCornersSrc[2] = rectF.right;
        rectCornersSrc[3] = rectF.bottom;
        return rectCornersSrc;
    }

    public static float[] calculateDotOrigin(DrawRecord record) {
        float[] rectCornersSrc = new float[4];
        //float[] rectCorners = new float[4];
        RectF rectF = record.rectOrigin;
        rectCornersSrc[0] = rectF.left;
        rectCornersSrc[1] = rectF.top;
        rectCornersSrc[2] = rectF.right;
        rectCornersSrc[3] = rectF.bottom;
        //record.matrix.mapPoints(rectCorners, rectCornersSrc);
        //return rectCorners;
        return rectCornersSrc;
    }

    /**
     * TODO  废弃的方法 OLD
     * @param record
     * @return
     */
    public static float[] calculateRectResult(DrawRecord record) {
        float[] rectCornersSrc = new float[8];
        float[] rectCorners = new float[8];
        RectF rectF = record.rectOrigin;
        rectCornersSrc[0] = rectF.left;
        rectCornersSrc[1] = rectF.top;
        rectCornersSrc[2] = rectF.right;
        rectCornersSrc[3] = rectF.top;
        rectCornersSrc[4] = rectF.right;
        rectCornersSrc[5] = rectF.bottom;
        rectCornersSrc[6] = rectF.left;
        rectCornersSrc[7] = rectF.bottom;
        record.matrix.mapPoints(rectCorners, rectCornersSrc);
        return rectCorners;
    }

//    public static float[] calculateRectResult(DrawRecord record) {
//        float[] rectCornersSrc = new float[8];
//        RectF rectF = record.rectOrigin;
//        rectCornersSrc[0] = rectF.left;
//        rectCornersSrc[1] = rectF.top;
//        rectCornersSrc[2] = rectF.right;
//        rectCornersSrc[3] = rectF.top;
//        rectCornersSrc[4] = rectF.right;
//        rectCornersSrc[5] = rectF.bottom;
//        rectCornersSrc[6] = rectF.left;
//        rectCornersSrc[7] = rectF.bottom;
//        return rectCornersSrc;
//    }

    public static float[] calculateLineResult(DrawRecord record) {
        float[] rectCornersSrc = new float[4];
        float[] rectCorners = new float[4];
        RectF rectF = record.rectOrigin;
        rectCornersSrc[0] = rectF.left;
        rectCornersSrc[1] = rectF.centerY();
        rectCornersSrc[2] = rectF.right;
        rectCornersSrc[3] = rectF.centerY();
        record.matrix.mapPoints(rectCorners, rectCornersSrc);
        return rectCorners;
        //return rectCornersSrc;
    }

    public static float[] calculateDotResult(DrawRecord record) {
        float[] rectCornersSrc = new float[2];
        float[] rectCorners = new float[2];
        RectF rectF = record.rectOrigin;
        rectCornersSrc[0] = rectF.centerX();
        rectCornersSrc[1] = rectF.bottom;
        record.matrix.mapPoints(rectCorners, rectCornersSrc);
        return rectCorners;
        //return rectCornersSrc;
    }

    public static float[] calculateRectCorners(DrawRecord record) {
        float[] rectCornersSrc = new float[18];
        float[] rectCorners = new float[18];
        RectF rectF = record.rectOrigin;
        //0,1代表左上角点XY
        rectCornersSrc[0] = rectF.left;
        rectCornersSrc[1] = rectF.top;
        //2,3代表右上角点XY
        rectCornersSrc[2] = rectF.right;
        rectCornersSrc[3] = rectF.top;
        //4,5代表右下角点XY
        rectCornersSrc[4] = rectF.right;
        rectCornersSrc[5] = rectF.bottom;
        //6,7代表左下角点XY
        rectCornersSrc[6] = rectF.left;
        rectCornersSrc[7] = rectF.bottom;
        //8,9代表中心点XY
        rectCornersSrc[8] = rectF.centerX();
        rectCornersSrc[9] = rectF.centerY();

        //上中
        rectCornersSrc[10] = rectF.centerX();
        rectCornersSrc[11] = rectF.top;

        //右中
        rectCornersSrc[12] = rectF.right;
        rectCornersSrc[13] = rectF.centerY();

        //下中
        rectCornersSrc[14] = rectF.centerX();
        rectCornersSrc[15] = rectF.bottom;

        //左中
        rectCornersSrc[16] = rectF.left;
        rectCornersSrc[17] = rectF.centerY();

        record.matrix.mapPoints(rectCorners, rectCornersSrc);

        // Log.e("draw", "left:" + rectF.left + ",top:" + rectF.top + ",right:" + rectF.right + "bottom" + rectF.bottom);
        return rectCorners;
    }

    public static float[] calculateBGCorners(BackgroundRecord record) {
        float[] bgCornersSrc = new float[10];//0,1代表左上角点XY，2,3代表右上角点XY，4,5代表右下角点XY，6,7代表左下角点XY，8,9代表中心点XY
        float[] bgCorners = new float[10];//0,1代表左上角点XY，2,3代表右上角点XY，4,5代表右下角点XY，6,7代表左下角点XY，8,9代表中心点XY
        RectF rectF = record.photoRectSrc;
        bgCornersSrc[0] = rectF.left;
        bgCornersSrc[1] = rectF.top;
        bgCornersSrc[2] = rectF.right;
        bgCornersSrc[3] = rectF.top;
        bgCornersSrc[4] = rectF.right;
        bgCornersSrc[5] = rectF.bottom;
        bgCornersSrc[6] = rectF.left;
        bgCornersSrc[7] = rectF.bottom;
        bgCornersSrc[8] = rectF.centerX();
        bgCornersSrc[9] = rectF.centerY();
        record.matrix.mapPoints(bgCorners, bgCornersSrc);
        return bgCorners;
    }

    /**
     * 以矩形的方式计算
     *
     * @param lineRecord
     * @return
     */
    public static float[] calculateLine(LineRecord lineRecord) {
        float[] lineCornersSrc = new float[12];
        float[] lineCorners = new float[12];
        RectF rectF = lineRecord.lineOrigin;
        lineCornersSrc[0] = rectF.left;
        lineCornersSrc[1] = rectF.top;
        lineCornersSrc[2] = rectF.right;
        lineCornersSrc[3] = rectF.top;
        lineCornersSrc[4] = rectF.right;
        lineCornersSrc[5] = rectF.bottom;
        lineCornersSrc[6] = rectF.left;
        lineCornersSrc[7] = rectF.bottom;
        lineCornersSrc[8] = rectF.centerX();
        lineCornersSrc[9] = rectF.centerY();
        lineRecord.matrix.mapPoints(lineCorners, lineCornersSrc);
        return lineCorners;
    }

    public static float[] calculateLineCenter(LineRecord lineRecord) {
        float[] lineCornersSrc = new float[4];
        float[] lineCorners = new float[4];
        RectF rectF = lineRecord.lineOrigin;
        lineCornersSrc[0] = rectF.left;
        lineCornersSrc[1] = (rectF.top + rectF.bottom) / 2;
        lineCornersSrc[2] = rectF.right;
        lineCornersSrc[3] = (rectF.top + rectF.bottom) / 2;
        lineRecord.matrix.mapPoints(lineCorners, lineCornersSrc);
        return lineCorners;
    }

    /**
     * 计算排水点需要用到的点位
     *
     * @param
     * @return
     */
    public static float[] calculateDot(DotRecord dotRecord) {
        float[] dotCornersSrc = new float[10];
        float[] dotCorners = new float[10];
        RectF rectF = dotRecord.rectOrigin;

        dotCornersSrc[0] = rectF.left;
        dotCornersSrc[1] = rectF.top;
        dotCornersSrc[2] = rectF.right;
        dotCornersSrc[3] = rectF.top;
        dotCornersSrc[4] = rectF.right;
        dotCornersSrc[5] = rectF.bottom;
        dotCornersSrc[6] = rectF.left;
        dotCornersSrc[7] = rectF.bottom;
        dotCornersSrc[8] = rectF.centerX();
        dotCornersSrc[9] = rectF.centerY();
        dotRecord.matrix.mapPoints(dotCorners, dotCornersSrc);
        return dotCorners;
    }


    public static float[] calculateBackground(BackgroundRecord record) {
        float[] rectCornersSrc = new float[10];
        float[] rectCorners = new float[10];
        RectF rectF = record.photoRectSrc;
        //0,1代表左上角点XY
        rectCornersSrc[0] = rectF.left;
        rectCornersSrc[1] = rectF.top;
        //2,3代表右上角点XY
        rectCornersSrc[2] = rectF.right;
        rectCornersSrc[3] = rectF.top;
        //4,5代表右下角点XY
        rectCornersSrc[4] = rectF.right;
        rectCornersSrc[5] = rectF.bottom;
        //6,7代表左下角点XY
        rectCornersSrc[6] = rectF.left;
        rectCornersSrc[7] = rectF.bottom;
        //8,9代表中心点XY
        rectCornersSrc[8] = rectF.centerX();
        rectCornersSrc[9] = rectF.centerY();
        record.matrix.mapPoints(rectCorners, rectCornersSrc);
        return rectCorners;
    }


    public static float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float calculatePointDistance(float[] pt1, float[] pt2) {
        return (float) Math.sqrt(Math.pow(pt1[0] - pt2[0], 2) + Math.pow(pt1[1] - pt2[1], 2));
    }

    public static double calculateAngleBetweenPoints(float[] pt1, float[] pt2) {
        float dx = pt2[0] - pt1[0];
        float dy = pt2[1] - pt1[1];
        if (dx == 0 && dy == 0) {
            return 0d;
        }
        double angle;
        angle = Math.toDegrees(Math.atan2(dx, dy));
        if (angle < 0) {
            angle = 360d + angle % (-360d);
        } else {
            angle = angle % 360d;
        }
        return angle;
    }

    public static Bitmap setBitmapWH(Bitmap bitmap, float newWidth, float newHeight) {
        Bitmap bitmapTemp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        int width = bitmapTemp.getWidth();
        int height = bitmapTemp.getHeight();
        float scaleWidth = (newWidth) / width;
        float scaleHeight = (newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmapTemp, 0, 0, width, height, matrix, true);
    }
}
