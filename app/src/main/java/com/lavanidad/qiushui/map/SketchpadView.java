package com.lavanidad.qiushui.map;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ColorUtils;
import com.lavanidad.qiushui.utils.DrawUtil;
import com.lavanidad.qiushui.R;
import com.lavanidad.qiushui.map.bean.BackgroundRecord;
import com.lavanidad.qiushui.map.bean.DotRecord;
import com.lavanidad.qiushui.map.bean.LineRecord;
import com.lavanidad.qiushui.map.bean.RectRecord;
import com.lavanidad.qiushui.map.bean.SketchpadData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class SketchpadView extends View {

    public static final String TAG = SketchpadView.class.getSimpleName();

    /**
     * 确认,取消,顶点 图标
     */
    private Bitmap confirmMarkBM;
    private Bitmap cancelMarkBM;

    private Bitmap confirmMarkBM20;
    private Bitmap cancelMarkBM20;
    private Bitmap rotateMarkBM20;

    private Bitmap dotMarkBM;
    private Bitmap rotateMarkBM;


    /**
     * 点确认,取消 图标区域
     */
    private RectF dotConfirmMarkRect;
    private RectF dotCancelMarkRect;

    /**
     * 矩形四个顶点的区域：用于判断是否被选中
     */
    private RectF rectUpperLeftRect;
    private RectF rectUpperRightRect;
    private RectF rectLowerRightRect;
    private RectF rectLowerLeftRect;

    /**
     * 线段的四个端点区域
     */
    private RectF lineUpperLeftRect;
    private RectF lineUpperRightRect;
    private RectF lineLowerRightRect;
    private RectF lineLowerLeftRect;

    private float rectRecFSize;
    private float rectDotSize;

    private float lineDotSize;
    private float lineRecFSize;

    private float dotSize;

    private Context context;

    private SketchpadData curSketchpadData;//记录画板上的元素
    private RectRecord curRectRecord;//矩形区域的属性
    private LineRecord curLineRecord;//线区域的属性
    private DotRecord curDotRecord;//排水 补水点属性
    private BackgroundRecord curBackgroundRecord;//背景图属性


    private int mWidth, mHeight;

    private int[] location = new int[2];  //当前的绝对坐标

    private float curX, curY;//当前的坐标X Y
    private float preX, preY;//需要移动的坐标
    private float downX, downY;//触摸下的X Y

    private int drawDensity = 2;//绘制密度,数值越高图像质量越低、性能越好


    //画矩形边框的画笔
    private Paint rectFramePaint;
    private Paint pathPaint;

    //辅助onTouch事件，判断当前选中模式
    private int actionMode;
    public static final int ACTION_NONE = 0x00;
    /**
     * 矩形的选中事件
     */
    public static final int ACTION_SELECT_RECT_DOT = 0x01;
    public static final int ACTION_SELECT_RECT_INSIDE = 0x02;
    public static final int ACTION_SELECT_RECT_CONFIRM = 0x03;
    public static final int ACTION_SELECT_RECT_CANCEL = 0x04;
    public static final int ACTION_SELECT_RECT_ROTATE = 0x104;
    public static final int ACTION_DRAW_RECT = 0x105;

    /**
     * 线段的选中事件
     */
    public static final int ACTION_SELECT_LINE_ROTATE = 0x05;
    public static final int ACTION_SELECT_LINE_INSIDE = 0x06;
    public static final int ACTION_SELECT_LINE_CONFIRM = 0x07;
    public static final int ACTION_SELECT_LINE_CANCEL = 0x08;
    public static final int ACTION_DRAW_LINE = 0x106;

    /**
     * 补水点，排水点的选中事件
     */
    private static final int ACTION_SELECT_DOT_INSIDE = 0x09;
    private static final int ACTION_SELECT_DOT_CONFIRM = 0x10;
    private static final int ACTION_SELECT_DOT_CANCEL = 0x11;
    public static final int ACTION_DRAW_DOT = 0x107;

    private static final int ACTION_SELECT_BACKGROUND_SCALE = 0x15;

    //细分具体选中了哪
    private int selectedPos = -1;
    /**
     * 矩形的四个顶点
     */
    private static final int RECT_UPPER_LEFT = 0x01;
    private static final int RECT_UPPER_RIGHT = 0x02;
    private static final int RECT_LOWER_RIGHT = 0x03;
    private static final int RECT_LOWER_LEFT = 0x04;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector = null;
    private OnConfirmListener onConfirmListener;

    /**
     * 测试用数据设置
     */
    private static float SCALE_MAX = 1.5f;
    private static float SCALE_MIN = 0.2f;
    private static float SCALE_MIN_LEN;
    float scaleFactor;
    private float initBGLength = 0;//初始背景图的对角线长度
    private float len;
    Paint paint = new Paint();


    public SketchpadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initSize();
        init(context);
        invalidate();
    }

    /**
     * 用来初始化各种绘制大小
     */
    private void initSize() {

        //test
        paint.setColor(getResources().getColor(R.color.antiquewhite));
        paint.setStyle(Paint.Style.FILL);

        /**
         * 矩形
         */
        rectDotSize = 20;
        rectRecFSize = 40;//是矩形图片的一半
        rotateMarkBM = setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.round_rotate), rectDotSize, rectDotSize);
        dotMarkBM = setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.round_dot), rectDotSize, rectDotSize);
        confirmMarkBM = setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.round_confirm), rectDotSize, rectDotSize);
        cancelMarkBM = setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.round_cancel), rectDotSize, rectDotSize);

        rectUpperLeftRect = new RectF(0, 0, rectRecFSize, rectRecFSize);
        rectUpperRightRect = new RectF(0, 0, rectRecFSize, rectRecFSize);
        rectLowerRightRect = new RectF(0, 0, rectRecFSize, rectRecFSize);
        rectLowerLeftRect = new RectF(0, 0, rectRecFSize, rectRecFSize);

        /**
         * line
         */
        lineDotSize = 20;
        lineRecFSize = 20;
        confirmMarkBM20 = setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.round_confirm), lineDotSize, lineDotSize);
        cancelMarkBM20 = setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.round_cancel), lineDotSize, lineDotSize);
        rotateMarkBM20 = setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.round_rotate), lineDotSize, lineDotSize);

        lineUpperLeftRect = new RectF(0, 0, lineRecFSize, lineRecFSize);
        lineUpperRightRect = new RectF(0, 0, lineRecFSize, lineRecFSize);
        lineLowerLeftRect = new RectF(0, 0, lineDotSize, lineDotSize);
        lineLowerRightRect = new RectF(0, 0, lineDotSize, lineDotSize);

        /**
         * 点
         */
        dotSize = 20;
        dotConfirmMarkRect = new RectF(0, 0, dotSize, dotSize);
        dotCancelMarkRect = new RectF(0, 0, dotSize, dotSize);
    }

    private void init(Context context) {

        //矩形边框画笔 暂时没用到
        rectFramePaint = new Paint();
        rectFramePaint.setColor(ColorUtils.getColor(R.color.orange));
        rectFramePaint.setStrokeWidth(2f);
        rectFramePaint.setStyle(Paint.Style.STROKE);

        pathPaint = new Paint();
        pathPaint.setColor(Color.GRAY);
        pathPaint.setStrokeWidth(2f * scaleFactor);
        pathPaint.setStyle(Paint.Style.STROKE);

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (curSketchpadData.drawMode != DrawMode.TYPE_STROKE) {
                    onScaleAction(detector);
                }
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                Log.e("detector", "" + detector.getScaleFactor());
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                Log.e("detector", "end:" + detector.getScaleFactor());
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawRecord(canvas);
    }

    public void drawBackground(Canvas canvas) {
        if (curSketchpadData.backgroundRecord != null && curBackgroundRecord != null) {
            canvas.drawBitmap(curBackgroundRecord.bitmap, curBackgroundRecord.matrix, null);
        }
    }

    /**
     * 绘制的主要方法
     *
     * @param canvas
     */
    private void drawRecord(Canvas canvas) {
        if (curSketchpadData != null) {
            //画矩形区域本体:设置的图片
            for (RectRecord record : curSketchpadData.rectRecordList) {
                if (record != null) {
                    canvas.drawBitmap(record.bitmap, record.matrix, null);
                }
            }
            //画线段
            for (LineRecord record : curSketchpadData.lineRecordList) {
                if (record != null) {
                    canvas.drawBitmap(record.bitmap, record.matrix, null);
                }
            }
            //画点
            for (DotRecord record : curSketchpadData.dotRecordList) {
                if (record != null) {
                    canvas.drawBitmap(record.bitmap, record.matrix, null);
                }
            }
            //如果是矩形模式
            if (curSketchpadData.drawMode == DrawMode.TYPE_RECT && curRectRecord != null) {
                //计算图片四个角点和中心点
                float[] rectCorners = DrawUtil.calculateCorners(curRectRecord);
                //绘制顶点
                if (actionMode != ACTION_NONE && curRectRecord.canSelected) {
                    drawRectDot(canvas, rectCorners);
                }
            }
            //如果是线段模式
            if (curSketchpadData.drawMode == DrawMode.TYPE_LINE && curLineRecord != null) {
                float[] lineCorners = DrawUtil.calculateLine(curLineRecord);
                if (actionMode != ACTION_NONE && curLineRecord.canSelected) {
                    //绘制端点
                    drawLineDot(canvas, lineCorners);
                    //绘制边框
                    drawLineFrame(canvas, lineCorners);
                }
            }
            //如果是点
            if (curSketchpadData.drawMode == DrawMode.TYPE_DOT && curDotRecord != null) {
                float[] dotCorners = DrawUtil.calculateDot(curDotRecord);
                if (actionMode != ACTION_NONE && curDotRecord.canSelected) {
                    //绘制确认/取消
                    drawDot(canvas, dotCorners);
                    //绘制边框
                    drawDotFrame(canvas, dotCorners);
                }
            }
        }
    }

    /**
     * 绘制线段顶点
     *
     * @param canvas
     * @param lineCorners
     */
    private void drawLineDot(Canvas canvas, float[] lineCorners) {
        float x;
        float y;
        //0,1代表左点XY 2,3代表右点XY 4,5代表中点XY
        x = lineCorners[0] - lineDotSize / 2;
        y = lineCorners[1] - lineDotSize / 2;
        lineUpperLeftRect.offsetTo(x, y);//调整至实际绘制区域
        canvas.drawBitmap(cancelMarkBM20, x, y, null);

        x = lineCorners[2] - lineDotSize / 2;
        y = lineCorners[3] - lineDotSize / 2;
        lineUpperRightRect.offsetTo(x, y);
        canvas.drawBitmap(confirmMarkBM20, x, y, null);

        x = lineCorners[4] - lineDotSize / 2;
        y = lineCorners[5] - lineDotSize / 2;
        lineLowerRightRect.offsetTo(x, y);
        canvas.drawBitmap(rotateMarkBM20, x, y, null);

        canvas.drawRect(lineUpperLeftRect, paint);
        canvas.drawRect(lineUpperRightRect, paint);
        canvas.drawRect(lineLowerRightRect, paint);
    }

    /**
     * 绘制补水点顶点
     *
     * @param canvas
     * @param dotCorners
     */
    private void drawDot(Canvas canvas, float[] dotCorners) {
        float x;
        float y;
        //0,1代表左点XY 2,3代表右点XY 4,5代表中点XY
        x = dotCorners[0] - dotSize / 2;
        y = dotCorners[1] - dotSize / 2;
        dotCancelMarkRect.offsetTo(x, y);//调整至实际绘制区域
        canvas.drawBitmap(cancelMarkBM20, x, y, null);

        x = dotCorners[2] - dotSize / 2;
        y = dotCorners[3] - dotSize / 2;
        dotConfirmMarkRect.offsetTo(x, y);
        canvas.drawBitmap(confirmMarkBM20, x, y, null);


        canvas.drawRect(dotCancelMarkRect, paint);
        canvas.drawRect(dotConfirmMarkRect, paint);
    }

    /**
     * 绘制矩形边框，利用4个顶点用Path绘制边线
     *
     * @param canvas
     * @param lineCorners
     */
    private void drawLineFrame(Canvas canvas, float[] lineCorners) {
        Path rectFramePath = new Path();
        //0,1代表左上角点XY 2,3代表右上角点XY
        //6,7代表左下角点XY 4,5代表右下角点XY    //8,9代表中心点XY

        rectFramePath.moveTo(lineCorners[0], lineCorners[1]);
        rectFramePath.lineTo(lineCorners[2], lineCorners[3]);
        rectFramePath.lineTo(lineCorners[4], lineCorners[5]);
        rectFramePath.lineTo(lineCorners[6], lineCorners[7]);
        rectFramePath.close();
        canvas.drawPath(rectFramePath, rectFramePaint);
    }

    private void drawDotFrame(Canvas canvas, float[] dotCorners) {
        Path rectFramePath = new Path();
        //0,1代表左上角点XY 2,3代表右上角点XY
        //6,7代表左下角点XY 4,5代表右下角点XY    //8,9代表中心点XY

        rectFramePath.moveTo(dotCorners[0], dotCorners[1]);
        rectFramePath.lineTo(dotCorners[2], dotCorners[3]);
        rectFramePath.lineTo(dotCorners[4], dotCorners[5]);
        rectFramePath.lineTo(dotCorners[6], dotCorners[7]);
        rectFramePath.close();
        canvas.drawPath(rectFramePath, rectFramePaint);
    }

    /**
     * 绘制矩形顶点
     * //0,1代表左上角点XY 2,3代表右上角点XY
     * //6,7代表左下角点XY 4,5代表右下角点XY    //8,9代表中心点XY
     *
     * @param canvas
     * @param rectCorners
     */
    private void drawRectDot(Canvas canvas, float[] rectCorners) {
        //矩形区域的X,Y，触摸区域
        float x;
        float y;

        //bitmap绘制起点
        float bitmapx;
        float bitmapy;

        x = rectCorners[0] - rectUpperLeftRect.width() + rectUpperLeftRect.width() / 2;
        y = rectCorners[1] - rectUpperLeftRect.height() + rectUpperLeftRect.height() / 2;
        rectUpperLeftRect.offsetTo(x, y);//调整至实际绘制区域
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(dotMarkBM, bitmapx, bitmapy, null);

        x = rectCorners[2] - rectUpperRightRect.width() / 2;
        y = rectCorners[3] - rectUpperRightRect.height() + rectUpperRightRect.height() / 2;
        rectUpperRightRect.offsetTo(x, y);
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(confirmMarkBM, bitmapx, bitmapy, null);

        x = rectCorners[4] - rectLowerRightRect.width() / 2;
        y = rectCorners[5] - rectLowerRightRect.width() / 2;
        rectLowerRightRect.offsetTo(x, y);
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(rotateMarkBM, bitmapx, bitmapy, null);

        x = rectCorners[6] - rectLowerLeftRect.width() + rectLowerLeftRect.width() / 2;
        y = rectCorners[7] - rectLowerLeftRect.height() / 2;
        rectLowerLeftRect.offsetTo(x, y);
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(cancelMarkBM, bitmapx, bitmapy, null);


        canvas.drawRect(rectUpperLeftRect, paint);
        canvas.drawRect(rectUpperRightRect, paint);
        canvas.drawRect(rectLowerRightRect, paint);
        canvas.drawRect(rectLowerLeftRect, paint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取在当前窗口内的绝对坐标
        getLocationInWindow(location);
        curX = (event.getRawX() - location[0]) / drawDensity;
        curY = (event.getRawY() - location[1]) / drawDensity;

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchDown();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() >= 2) {
                    float downDistance = spacing(event);
                    if (actionMode == ACTION_NONE && downDistance > 10) {
                        actionMode = ACTION_SELECT_BACKGROUND_SCALE;
                    }
                }
                break;
        }
        preX = curX;
        preY = curY;

        return true;
    }

    private void touchDown() {
        downX = curX;
        downY = curY;
        //如果当前是矩形模式
        if (curSketchpadData.drawMode == DrawMode.TYPE_RECT && curRectRecord != null) {
            float[] downPoint = new float[]{downX * drawDensity, downY * drawDensity};
            if (curRectRecord.canSelected) {
                //如果触摸了点
                if (isInRectDot(downPoint)) {
                    return;
                }
                //如果触摸矩形内部区域
                if (isInRect(curRectRecord, downPoint)) {
                    actionMode = ACTION_SELECT_RECT_INSIDE;
                    return;
                }
            }
            //判断是否点击了其他图片
            selectOtherRect(downPoint);
        }
        //如果当前是线段模式
        if (curSketchpadData.drawMode == DrawMode.TYPE_LINE && curLineRecord != null) {
            float[] downPoint = new float[]{downX * drawDensity, downY * drawDensity};
            if (curLineRecord.canSelected) {
                //如果触摸了点
                if (isInLineDot(downPoint)) {
                    return;
                }
                //如果触摸内部区域
                if (isInLine(curLineRecord, downPoint)) {
                    actionMode = ACTION_SELECT_LINE_INSIDE;
                    return;
                }
            }
            selectOtherLine(downPoint);
        }
        //如果当前是点
        if (curSketchpadData.drawMode == DrawMode.TYPE_DOT && curDotRecord != null) {
            float[] downPoint = new float[]{downX * drawDensity, downY * drawDensity};
            if (curDotRecord.canSelected) {
                //如果触摸内部区域
                if (isInDot(curDotRecord, downPoint)) {
                    actionMode = ACTION_SELECT_DOT_INSIDE;
                    return;
                }
                //判断是否点击了确认/取消区域
                if (isInDotButtonRect(downPoint)) {
                    return;
                }
            }
            selectOtherDot(downPoint);
        }
    }

    private void touchMove(MotionEvent event) {
        if (actionMode == ACTION_SELECT_BACKGROUND_SCALE) {
            mScaleGestureDetector.onTouchEvent(event);
        }
//        if (curBackgroundRecord != null && curSketchpadData.drawMode == DrawMode.TYPE_NONE) {
//            onZoomMove((curX - preX) * drawDensity, (curY - preY) * drawDensity);
//        }
        //如果是矩形
        if (curSketchpadData.drawMode == DrawMode.TYPE_RECT && curRectRecord != null && curRectRecord.canSelected) {
            if (actionMode == ACTION_SELECT_RECT_DOT) {
                //选中边角点临边移动：拖动
                rectDotMove(curRectRecord, (curX - preX) * drawDensity, (curY - preY) * drawDensity);
            } else if (actionMode == ACTION_SELECT_RECT_INSIDE) {
                //选中内部则整体移动：拖拽
                rectMove((curX - preX) * drawDensity, (curY - preY) * drawDensity);
            } else if (actionMode == ACTION_SELECT_RECT_ROTATE) {
                rectRotate(curRectRecord);
            }
        }
        //如果是线段
        if (curSketchpadData.drawMode == DrawMode.TYPE_LINE && curLineRecord != null && curLineRecord.canSelected) {
            if (actionMode == ACTION_SELECT_LINE_ROTATE) {
                lineRotate(curLineRecord);
            } else if (actionMode == ACTION_SELECT_LINE_INSIDE) {
                //选中内部则整体移动：拖拽
                lineMove((curX - preX) * drawDensity, (curY - preY) * drawDensity);
            }
        }
        //如果是点
        if (curSketchpadData.drawMode == DrawMode.TYPE_DOT && curDotRecord != null && curDotRecord.canSelected) {
            if (actionMode == ACTION_SELECT_DOT_INSIDE) {
                //拖动
                dotMove((curX - preX) * drawDensity, (curY - preY) * drawDensity);
            }
        }
    }

    public void onScaleAction(ScaleGestureDetector detector) {
        float[] bgCorners = DrawUtil.calculateBGCorners(curBackgroundRecord);

        //背景缩放倍数
        float scaleMax = 1.5f;
        float scaleMin = 0.8f;

        len = (float) Math.sqrt(Math.pow(bgCorners[0] - bgCorners[4], 2) + Math.pow(bgCorners[1] - bgCorners[5], 2));
        double photoLen = Math.sqrt(Math.pow(curBackgroundRecord.photoRectSrc.width(), 2) + Math.pow(curBackgroundRecord.photoRectSrc.height(), 2));
        scaleFactor = detector.getScaleFactor();

        if ((scaleFactor < 1 && len >= photoLen * scaleMin && len >= SCALE_MIN_LEN) ||
                (scaleFactor > 1 && len <= photoLen * scaleMax)) {
            //背景
            if (curBackgroundRecord != null) {
                curBackgroundRecord.matrix.postScale(scaleFactor, scaleFactor, bgCorners[8], bgCorners[9]);
            }
            //矩形
            if (curSketchpadData.rectRecordList.size() > 0) {
                for (int i = 0; i < curSketchpadData.rectRecordList.size(); i++) {
                    if (curSketchpadData.rectRecordList.get(i) != null)
                        curSketchpadData.rectRecordList.get(i).matrix.postScale(scaleFactor, scaleFactor, bgCorners[8], bgCorners[9]);
                }
            }
            //线
            if (curSketchpadData.lineRecordList.size() > 0) {
                for (int i = 0; i < curSketchpadData.lineRecordList.size(); i++) {
                    if (curSketchpadData.lineRecordList.get(i) != null)
                        curSketchpadData.lineRecordList.get(i).matrix.postScale(scaleFactor, scaleFactor, bgCorners[8], bgCorners[9]);
                }
            }
            //补水点
            if (curSketchpadData.dotRecordList.size() > 0) {
                for (int i = 0; i < curSketchpadData.dotRecordList.size(); i++) {
                    if (curSketchpadData.dotRecordList.get(i) != null)
                        curSketchpadData.dotRecordList.get(i).matrix.postScale(scaleFactor, scaleFactor, bgCorners[8], bgCorners[9]);
                }
            }
        }
    }

    /**
     * 线段整体旋转 缩放
     *
     * @param lineRecord
     */
    private void lineRotate(LineRecord lineRecord) {
        float[] corners = DrawUtil.calculateLine(lineRecord);
        //放大
        //目前触摸点与图片显示中心距离,curX*drawDensity为还原缩小密度点数值
        float a = (float) Math.sqrt(Math.pow(curX * drawDensity - corners[8], 2) + Math.pow(curY * drawDensity - corners[9], 2));
        //目前上次旋转图标与图片显示中心距离
        float b = (float) Math.sqrt(Math.pow(corners[4] - corners[0], 2) + Math.pow(corners[5] - corners[1], 2)) / 2;
        //设置Matrix缩放参数
        double photoLen = Math.sqrt(Math.pow(lineRecord.lineOrigin.width(), 2) + Math.pow(lineRecord.lineOrigin.height(), 2));
        if (a >= photoLen / 2 * SCALE_MIN && a >= SCALE_MIN_LEN && a <= photoLen / 2 * SCALE_MAX) {
            //这种计算方法可以保持旋转图标坐标与触摸点同步缩放
            float scale = a / b;
            lineRecord.matrix.postScale(scale, scale, corners[8], corners[9]);
        }
        //旋转
        //根据移动坐标的变化构建两个向量，以便计算两个向量角度.
        PointF preVector = new PointF();
        PointF curVector = new PointF();
        preVector.set((preX * drawDensity - corners[8]), preY * drawDensity - corners[9]);//旋转后向量
        curVector.set(curX * drawDensity - corners[8], curY * drawDensity - corners[9]);//旋转前向量
        //计算向量长度
        double preVectorLen = getVectorLength(preVector);
        double curVectorLen = getVectorLength(curVector);
        //计算两个向量的夹角.
        double cosAlpha = (preVector.x * curVector.x + preVector.y * curVector.y)
                / (preVectorLen * curVectorLen);
        //由于计算误差，可能会带来略大于1的cos，例如
        if (cosAlpha > 1.0f) {
            cosAlpha = 1.0f;
        }
        //本次的角度已经计算出来。
        double dAngle = Math.acos(cosAlpha) * 180.0 / Math.PI;
        // 判断顺时针和逆时针.
        //判断方法其实很简单，这里的v1v2其实相差角度很小的。
        //先转换成单位向量
        preVector.x /= preVectorLen;
        preVector.y /= preVectorLen;
        curVector.x /= curVectorLen;
        curVector.y /= curVectorLen;
        //作curVector的逆时针垂直向量。
        PointF verticalVec = new PointF(curVector.y, -curVector.x);

        //判断这个垂直向量和v1的点积，点积>0表示俩向量夹角锐角。=0表示垂直，<0表示钝角
        float vDot = preVector.x * verticalVec.x + preVector.y * verticalVec.y;
        if (vDot > 0) {
            //v2的逆时针垂直向量和v1是锐角关系，说明v1在v2的逆时针方向。
        } else {
            dAngle = -dAngle;
        }
        lineRecord.matrix.postRotate((float) dAngle, corners[8], corners[9]);
    }

    private void rectRotate(RectRecord rectRecord) {
        float[] corners = DrawUtil.calculateCorners(rectRecord);

        //旋转
        //根据移动坐标的变化构建两个向量，以便计算两个向量角度.
        PointF preVector = new PointF();
        PointF curVector = new PointF();
        preVector.set((preX * drawDensity - corners[8]), preY * drawDensity - corners[9]);//旋转后向量
        curVector.set(curX * drawDensity - corners[8], curY * drawDensity - corners[9]);//旋转前向量
        //计算向量长度
        double preVectorLen = getVectorLength(preVector);
        double curVectorLen = getVectorLength(curVector);
        //计算两个向量的夹角.
        double cosAlpha = (preVector.x * curVector.x + preVector.y * curVector.y)
                / (preVectorLen * curVectorLen);
        //由于计算误差，可能会带来略大于1的cos，例如
        if (cosAlpha > 1.0f) {
            cosAlpha = 1.0f;
        }
        //本次的角度已经计算出来。
        double rectAngle = Math.acos(cosAlpha) * 180.0 / Math.PI;

        // 判断顺时针和逆时针.
        //判断方法其实很简单，这里的v1v2其实相差角度很小的。
        //先转换成单位向量
        preVector.x /= preVectorLen;
        preVector.y /= preVectorLen;
        curVector.x /= curVectorLen;
        curVector.y /= curVectorLen;
        //作curVector的逆时针垂直向量。
        PointF verticalVec = new PointF(curVector.y, -curVector.x);

        //判断这个垂直向量和v1的点积，点积>0表示俩向量夹角锐角。=0表示垂直，<0表示钝角
        float vDot = preVector.x * verticalVec.x + preVector.y * verticalVec.y;
        if (vDot > 0) {
            //v2的逆时针垂直向量和v1是锐角关系，说明v1在v2的逆时针方向。
        } else {
            rectAngle = -rectAngle;
        }
        rectRecord.matrix.postRotate((float) rectAngle, corners[8], corners[9]);
    }

    /**
     * 获取p1到p2的线段的长度
     *
     * @return
     */
    public double getVectorLength(PointF vector) {
        return Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }

    /**
     * 拖动矩形四角移动
     *
     * @param rectRecord
     * @param x
     * @param y
     */
    private void rectDotMove(RectRecord rectRecord, float x, float y) {
        float newX;
        float newY;
        float scaleX;
        float scaleY;
        float minX = 50f;
        float minY = 50f;

        float[] corners = DrawUtil.calculateCorners(rectRecord);

        switch (selectedPos) {
            case RECT_UPPER_LEFT://左上角移动
                newX = Math.max(rectRecord.curRectWidth - x, minX);
                newY = Math.max(rectRecord.curRectHeight - y, minY);
                scaleX = newX / rectRecord.curRectWidth;
                scaleY = newY / rectRecord.curRectHeight;
                rectRecord.curRectWidth = newX;
                rectRecord.curRectHeight = newY;
                rectRecord.matrix.postScale(scaleX, scaleY, corners[4], corners[5]);
                break;
            case RECT_UPPER_RIGHT:
                newX = Math.max(rectRecord.curRectWidth + x, minX);
                newY = Math.max(rectRecord.curRectHeight - y, minY);
                scaleX = newX / rectRecord.curRectWidth;
                scaleY = newY / rectRecord.curRectHeight;
                rectRecord.curRectWidth = newX;
                rectRecord.curRectHeight = newY;
                rectRecord.matrix.postScale(scaleX, scaleY, corners[6], corners[7]);
                break;
            case RECT_LOWER_LEFT:
                newX = Math.max(rectRecord.curRectWidth - x, minX);
                newY = Math.max(rectRecord.curRectHeight + y, minY);
                scaleX = newX / rectRecord.curRectWidth;
                scaleY = newY / rectRecord.curRectHeight;
                rectRecord.curRectWidth = newX;
                rectRecord.curRectHeight = newY;
                rectRecord.matrix.postScale(scaleX, scaleY, corners[2], corners[3]);
                break;
            case RECT_LOWER_RIGHT:
                newX = Math.max(rectRecord.curRectWidth + x, minX);
                newY = Math.max(rectRecord.curRectHeight + y, minY);
                scaleX = newX / rectRecord.curRectWidth;
                scaleY = newY / rectRecord.curRectHeight;
                rectRecord.curRectWidth = newX;
                rectRecord.curRectHeight = newY;
                rectRecord.matrix.postScale(scaleX, scaleY, corners[0], corners[1]);
                break;
        }
    }

    /**
     * 矩形移动
     *
     * @param distanceX 移动距离
     * @param distanceY
     */
    private void rectMove(float distanceX, float distanceY) {
        curRectRecord.matrix.postTranslate((int) distanceX, (int) distanceY);
    }

    /**
     * 线段移动
     *
     * @param distanceX
     * @param distanceY
     */
    private void lineMove(float distanceX, float distanceY) {
        curLineRecord.matrix.postTranslate((int) distanceX, (int) distanceY);
    }

    /**
     * 判断是否选中了矩形顶点
     *
     * @param downPoint
     */
    private boolean isInRectDot(float[] downPoint) {
        //如果选中了顶点，
        if (rectUpperLeftRect.contains(downPoint[0], downPoint[1])) {//移动
            actionMode = ACTION_SELECT_RECT_DOT;
            selectedPos = RECT_UPPER_LEFT;
            return true;
        } else if (rectUpperRightRect.contains(downPoint[0], downPoint[1])) {//确认
            actionMode = ACTION_SELECT_RECT_CONFIRM;
            selectedPos = RECT_UPPER_RIGHT;
            if (onConfirmListener != null && curRectRecord != null) {
                float[] origin = DrawUtil.calculateCorners(curRectRecord);
                onConfirmListener.onRectConfirm(curRectRecord, getPhysicsPoints(origin));
                actionMode = ACTION_NONE;
            }
            return true;
        } else if (rectLowerRightRect.contains(downPoint[0], downPoint[1])) {//旋转
            actionMode = ACTION_SELECT_RECT_ROTATE;
            selectedPos = RECT_LOWER_RIGHT;
            return true;
        } else if (rectLowerLeftRect.contains(downPoint[0], downPoint[1])) {//取消
            selectedPos = RECT_LOWER_LEFT;
            actionMode = ACTION_SELECT_RECT_CANCEL;
            curSketchpadData.rectRecordList.remove(curRectRecord);
            if (onConfirmListener != null) {
                onConfirmListener.onRectCancel(curRectRecord);
                setRectRecord(null);
                actionMode = ACTION_NONE;
            }
            return true;
        }
        selectedPos = -1;
        return false;
    }

    /**
     * 判断触摸点是否在矩形内部
     *
     * @param rectRecord
     * @param downPoint
     * @return
     */
    private boolean isInRect(RectRecord rectRecord, float[] downPoint) {
        if (rectRecord != null) {
            float[] invertPoint = new float[2];
            Matrix invertMatrix = new Matrix();
            rectRecord.matrix.invert(invertMatrix);
            invertMatrix.mapPoints(invertPoint, downPoint);
            return rectRecord.rectOrigin.contains(invertPoint[0], invertPoint[1]);
        }
        return false;
    }

    /**
     * 判断触摸点是否在线段端点
     *
     * @param downPoint
     * @return
     */
    private boolean isInLineDot(float[] downPoint) {
        if (lineUpperLeftRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_LINE_CANCEL;
            curSketchpadData.lineRecordList.remove(curLineRecord);
            if (onConfirmListener != null) {
                onConfirmListener.onLineCancel(curLineRecord);
                setLineRecord(null);
                actionMode = ACTION_NONE;
            }
            return true;
        } else if (lineUpperRightRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_LINE_CONFIRM;
            if (onConfirmListener != null) {
                float[] origin = DrawUtil.calculateLineCenter(curLineRecord);
                onConfirmListener.onLineConfirm(curLineRecord, getPhysicsPoints(origin));
                actionMode = ACTION_NONE;
            }
            return true;
        } else if (lineLowerRightRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_LINE_ROTATE;
            return true;
        }
        selectedPos = -1;
        return false;
    }

    /**
     * 判断是否在线段区域内部
     *
     * @param lineRecord
     * @param downPoint
     * @return
     */
    private boolean isInLine(LineRecord lineRecord, float[] downPoint) {
        if (lineRecord != null) {
            float[] invertPoint = new float[2];
            Matrix invertMatrix = new Matrix();
            lineRecord.matrix.invert(invertMatrix);
            invertMatrix.mapPoints(invertPoint, downPoint);
            return lineRecord.lineOrigin.contains(invertPoint[0], invertPoint[1]);
        }
        return false;
    }

    /**
     * 是否触摸到点内部
     *
     * @param dotRecord
     * @param downPoint
     * @return
     */
    private boolean isInDot(DotRecord dotRecord, float[] downPoint) {
        if (dotRecord != null) {
            float[] invertPoint = new float[2];
            Matrix invertMatrix = new Matrix();
            dotRecord.matrix.invert(invertMatrix);
            invertMatrix.mapPoints(invertPoint, downPoint);
            return dotRecord.rectOrigin.contains(invertPoint[0], invertPoint[1]);
        }
        return false;
    }

    private boolean isInDotButtonRect(float[] downPoint) {
        if (dotConfirmMarkRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_DOT_CONFIRM;
            if (onConfirmListener != null) {
                float[] origin = DrawUtil.calculateDot(curDotRecord);
                onConfirmListener.onDotConfirm(curDotRecord, getPhysicsPoints(origin));
            }
            return true;
        }
        if (dotCancelMarkRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_DOT_CANCEL;
            curSketchpadData.dotRecordList.remove(curDotRecord);
            setDotRecord(null);
            actionMode = ACTION_NONE;
            return true;
        }
        return false;
    }


    private void dotMove(float distanceX, float distanceY) {
        curDotRecord.matrix.postTranslate((int) distanceX, (int) distanceY);
    }

    private void selectOtherRect(float[] downPoint) {
        RectRecord clickRecord = null;
        for (int i = curSketchpadData.rectRecordList.size() - 1; i >= 0; i--) {
            RectRecord record = curSketchpadData.rectRecordList.get(i);
            if (isInRect(record, downPoint)) {
                clickRecord = record;
                break;
            }
        }
        if (clickRecord != null) {
            setRectRecord(clickRecord);
            actionMode = ACTION_SELECT_RECT_INSIDE;
        } else {
            actionMode = ACTION_NONE;
        }
    }

    private void selectOtherLine(float[] downPoint) {
        LineRecord clickRecord = null;
        for (int i = curSketchpadData.lineRecordList.size() - 1; i >= 0; i--) {
            LineRecord record = curSketchpadData.lineRecordList.get(i);
            if (isInLine(record, downPoint)) {
                clickRecord = record;
                break;
            }
        }
        if (clickRecord != null) {
            setLineRecord(clickRecord);
            actionMode = ACTION_SELECT_LINE_INSIDE;
        } else {
            actionMode = ACTION_NONE;
        }
    }

    private void selectOtherDot(float[] downPoint) {
        DotRecord clickRecord = null;
        for (int i = curSketchpadData.dotRecordList.size() - 1; i >= 0; i--) {
            DotRecord record = curSketchpadData.dotRecordList.get(i);
            if (isInDot(record, downPoint)) {
                clickRecord = record;
                break;
            }
        }
        if (clickRecord != null) {
            setDotRecord(clickRecord);
            actionMode = ACTION_SELECT_DOT_INSIDE;
        } else {
            actionMode = ACTION_NONE;
        }
    }


    @IntDef({DrawMode.TYPE_NONE, DrawMode.TYPE_RECT, DrawMode.TYPE_DOT,
            DrawMode.TYPE_LINE, DrawMode.TYPE_STROKE, DrawMode.TYPE_DRAW_PATH,})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DrawMode {

        int TYPE_NONE = 0x00;//未选择模式

        int TYPE_RECT = 0x01;//绘制矩形

        int TYPE_LINE = 0x02;//绘制线

        int TYPE_DOT = 0x03;//点

        int TYPE_STROKE = 0x04;//画笔

        int TYPE_DRAW_PATH = 0x05;//画路径
    }

    /**
     * 设置画板数据 初始化操作
     *
     * @param sketchpadData
     */
    public void setSketchData(SketchpadData sketchpadData) {
        this.curSketchpadData = sketchpadData;
        curRectRecord = null;
        curLineRecord = null;
        curDotRecord = null;
        curBackgroundRecord = null;
    }

    /**
     * 设置背景
     *
     * @param bitmap
     * @param width
     * @param height
     */
    public void addBackgroundBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap != null) {
            BackgroundRecord backgroundRecord = initBackgroundRecord(setBitmapWH(bitmap, width, height));
            curSketchpadData.backgroundRecord = backgroundRecord;
            curBackgroundRecord = backgroundRecord;
            float[] bgCorners = DrawUtil.calculateBGCorners(curBackgroundRecord);
            initBGLength = (float) Math.sqrt(Math.pow(bgCorners[0] - bgCorners[4], 2) + Math.pow(bgCorners[1] - bgCorners[5], 2));
            len = initBGLength;
            invalidate();
        } else {
            Toast.makeText(context, "background bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 添加矩形区域外观图片
     *
     * @param bitmap 矩形区域的外观bitmap
     */
    public void addRectRecord(Bitmap bitmap, int width, int height) {
        if (bitmap != null) {
            RectRecord rectRecord = initRectRecord(setBitmapWH(bitmap, width, height));
            setRectRecord(rectRecord);
        } else {
            Toast.makeText(context, "rect bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 添加禁行区
     *
     * @param bitmap
     * @param width
     * @param height
     */
    public void addLineRecord(Bitmap bitmap, int width, int height) {
        if (bitmap != null) {
            LineRecord lineRecord = initLineRecord(setBitmapWH(bitmap, width, height));
            setLineRecord(lineRecord);
        } else {
            Toast.makeText(context, "line bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 添加点
     *
     * @param bitmap
     * @param width
     * @param height
     */
    public void addDotRecord(Bitmap bitmap, int width, int height) {
        if (bitmap != null) {
            DotRecord dotRecord = initDotRecord(setBitmapWH(bitmap, width, height));
            setDotRecord(dotRecord);
        } else {
            Toast.makeText(context, "water dot bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }


    private Bitmap setBitmapWH(Bitmap bitmap, float newWidth, float newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = (newWidth) / width;
        float scaleHeight = (newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    int i = 0;

    @NonNull
    private RectRecord initRectRecord(Bitmap bitmap) {
        RectRecord rectRecord = new RectRecord();
        rectRecord.bitmap = bitmap;
        rectRecord.rectOrigin = new RectF(0, 0, rectRecord.bitmap.getWidth(), rectRecord.bitmap.getHeight());

        rectRecord.name = "禁行区" + i++;
        if (curSketchpadData.rectRecordList.size() > 0 && curRectRecord != null) {//有多个图片
            rectRecord.matrix = new Matrix(curRectRecord.matrix);
            rectRecord.matrix.postTranslate(50, 50);
            rectRecord.curRectWidth = curRectRecord.curRectWidth;
            rectRecord.curRectHeight = curRectRecord.curRectHeight;
        } else {
            rectRecord.matrix = new Matrix();
            rectRecord.matrix.postTranslate(getWidth() / 2 - bitmap.getWidth() / 2, getHeight() / 2 - bitmap.getHeight() / 2);
            rectRecord.curRectWidth = rectRecord.rectOrigin.width();
            rectRecord.curRectHeight = rectRecord.rectOrigin.height();
        }
        return rectRecord;
    }

    int j = 0;

    @NonNull
    private LineRecord initLineRecord(Bitmap bitmap) {
        LineRecord lineRecord = new LineRecord();
        lineRecord.bitmap = bitmap;
        lineRecord.lineOrigin = new RectF(0, 0, lineRecord.bitmap.getWidth(), lineRecord.bitmap.getHeight());
        lineRecord.name = "虚拟墙" + j++;
        if (curSketchpadData.lineRecordList.size() > 0 && curLineRecord != null) {//有多个图片
            lineRecord.matrix = new Matrix(curLineRecord.matrix);
            lineRecord.matrix.postTranslate(30, 30);
            lineRecord.curLineLength = curLineRecord.curLineLength;
        } else {
            lineRecord.matrix = new Matrix();
            lineRecord.matrix.postTranslate(getWidth() / 2 - lineRecord.bitmap.getWidth() / 2,
                    getHeight() / 2 - lineRecord.bitmap.getHeight() / 2);
            lineRecord.curLineLength = lineRecord.lineOrigin.width();
        }
        return lineRecord;
    }

    int k = 0;

    @NonNull
    private DotRecord initDotRecord(Bitmap bitmap) {
        DotRecord dotRecord = new DotRecord();
        dotRecord.bitmap = bitmap;
        dotRecord.rectOrigin = new RectF(0, 0, dotRecord.bitmap.getWidth(), dotRecord.bitmap.getHeight());
        dotRecord.name = "点位" + k++;
        if (curSketchpadData.dotRecordList.size() > 0 && curDotRecord != null) {//有多个图片
            dotRecord.matrix = new Matrix(curDotRecord.matrix);
            dotRecord.matrix.postTranslate(30, 30);//单位PX
        } else {
            dotRecord.matrix = new Matrix();
            dotRecord.matrix.postTranslate(getWidth() / 2 - bitmap.getWidth(), getHeight() / 2 - bitmap.getHeight());
        }
        return dotRecord;
    }


    @NonNull
    public BackgroundRecord initBackgroundRecord(Bitmap bitmap) {
        BackgroundRecord newRecord = new BackgroundRecord();
        newRecord.bitmap = bitmap;
        newRecord.photoRectSrc = new RectF(0, 0, newRecord.bitmap.getWidth(), newRecord.bitmap.getHeight());
        newRecord.matrix = new Matrix();
        return newRecord;
    }

    private void setRectRecord(RectRecord rectRecord) {
        curSketchpadData.rectRecordList.remove(rectRecord);
        curSketchpadData.rectRecordList.add(rectRecord);
        curRectRecord = rectRecord;
        invalidate();
    }

    private void setLineRecord(LineRecord lineRecord) {
        curSketchpadData.lineRecordList.remove(lineRecord);
        curSketchpadData.lineRecordList.add(lineRecord);
        curLineRecord = lineRecord;
        invalidate();
    }

    private void setDotRecord(DotRecord dotRecord) {
        curSketchpadData.dotRecordList.remove(dotRecord);
        curSketchpadData.dotRecordList.add(dotRecord);
        curDotRecord = dotRecord;
        invalidate();
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float[] getPhysicsPoints(float[] origin) {
        float[] result = new float[origin.length];
        Matrix imageMatrix = curBackgroundRecord.matrix;
        Matrix inverseMatrix = new Matrix();
        imageMatrix.invert(inverseMatrix);
        inverseMatrix.mapPoints(result, origin);
        return result;
    }


    public void setDrawMode(int drawMode) {
        this.curSketchpadData.drawMode = drawMode;
        invalidate();
    }

    public void setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
    }

    public void onZoomMove(float distanceX, float distanceY) {
        curBackgroundRecord.matrix.postTranslate((int) distanceX, (int) distanceY);

        //矩形
        if (curSketchpadData.rectRecordList.size() > 0) {
            for (int i = 0; i < curSketchpadData.rectRecordList.size(); i++) {
                if (curSketchpadData.rectRecordList.get(i) != null)
                    curSketchpadData.rectRecordList.get(i).matrix.postTranslate((int) distanceX, (int) distanceY);
            }
        }
        //线
        if (curSketchpadData.lineRecordList.size() > 0) {
            for (int i = 0; i < curSketchpadData.lineRecordList.size(); i++) {
                if (curSketchpadData.lineRecordList.get(i) != null)
                    curSketchpadData.lineRecordList.get(i).matrix.postTranslate((int) distanceX, (int) distanceY);
            }
        }
        //点
        if (curSketchpadData.dotRecordList.size() > 0) {
            for (int i = 0; i < curSketchpadData.dotRecordList.size(); i++) {
                if (curSketchpadData.dotRecordList.get(i) != null)
                    curSketchpadData.dotRecordList.get(i).matrix.postTranslate((int) distanceX, (int) distanceY);
            }
        }
    }

    /**
     * TODO 测试旋转 整体暂不需要旋转
     *
     * @param degress
     */
    public void setMapRotate(float degress) {
        float[] bgCorners = DrawUtil.calculateBackground(curBackgroundRecord);
        if (curBackgroundRecord != null) {
            curSketchpadData.backgroundRecord.matrix.postRotate(degress, bgCorners[8], bgCorners[9]);
            invalidate();
        }
        //矩形
//        if (curSketchpadData.rectRecordList.size() > 0) {
//            for (int i = 0; i < curSketchpadData.rectRecordList.size(); i++) {
//                if (curSketchpadData.rectRecordList.get(i) != null)
//                    curSketchpadData.rectRecordList.get(i).matrix.postRotate(degress, bgCorners[8], bgCorners[9]);
//            }
//        }
        //线
        if (curSketchpadData.lineRecordList.size() > 0) {
            for (int i = 0; i < curSketchpadData.lineRecordList.size(); i++) {
                if (curSketchpadData.lineRecordList.get(i) != null)
                    curSketchpadData.lineRecordList.get(i).matrix.postRotate(degress, bgCorners[8], bgCorners[9]);
            }
        }
        //点
        if (curSketchpadData.dotRecordList.size() > 0) {
            for (int i = 0; i < curSketchpadData.dotRecordList.size(); i++) {
                if (curSketchpadData.dotRecordList.get(i) != null)
                    curSketchpadData.dotRecordList.get(i).matrix.postRotate(degress, bgCorners[8], bgCorners[9]);
            }
        }
    }

    /**
     * TODO 测试旋转重置
     *
     * @param
     */
    public void setMapReset(float degress) {
        float[] bgCorners = DrawUtil.calculateBGCorners(curBackgroundRecord);
        if (curBackgroundRecord != null) {
            curBackgroundRecord.matrix.reset();

            len = (float) Math.sqrt(Math.pow(bgCorners[0] - bgCorners[4], 2) + Math.pow(bgCorners[1] - bgCorners[5], 2));
        }

        //线
        if (curSketchpadData.lineRecordList.size() > 0) {
            for (int i = 0; i < curSketchpadData.lineRecordList.size(); i++) {
                if (curSketchpadData.lineRecordList.get(i) != null)
                    curSketchpadData.lineRecordList.get(i).matrix.postRotate(360 - degress, bgCorners[8], bgCorners[9]);
            }
        }
        //点
        if (curSketchpadData.dotRecordList.size() > 0) {
            for (int i = 0; i < curSketchpadData.dotRecordList.size(); i++) {
                if (curSketchpadData.dotRecordList.get(i) != null)
                    curSketchpadData.dotRecordList.get(i).matrix.postRotate(360 - degress, bgCorners[8], bgCorners[9]);
            }
        }
        invalidate();
    }

    public void setActionMode(int actionMode) {
        this.actionMode = actionMode;
    }

    public void refreshView() {
        actionMode = ACTION_NONE;
        invalidate();
    }

    /**
     * 对外接口
     */
    public interface OnConfirmListener {
        void onRectConfirm(RectRecord record, float[] phyPoints);

        void onRectCancel(RectRecord record);

        void onLineConfirm(LineRecord lineRecord, float[] phyPoints);

        void onLineCancel(LineRecord lineRecord);

        void onDotConfirm(DotRecord dotRecord, float[] phyPoints);

        void onDotCancel(DotRecord dotRecord);
    }
}
