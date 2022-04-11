package com.lavanidad.qiushui;


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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lavanidad.qiushui.bean.LineRecord;
import com.lavanidad.qiushui.bean.RectRecord;
import com.lavanidad.qiushui.bean.SketchpadData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public class SketchpadView extends View {

    public static final String TAG = SketchpadView.class.getSimpleName();

    /**
     * 确认,取消,顶点 图标
     */
    private Bitmap confirmMarkBM = BitmapFactory.decodeResource(getResources(), R.mipmap.round_confirm);
    private Bitmap cancelMarkBM = BitmapFactory.decodeResource(getResources(), R.mipmap.round_cancel);
    private Bitmap dotMarkBM = BitmapFactory.decodeResource(getResources(), R.mipmap.round_dot);

    /**
     * 矩形确认,取消,顶点 图标区域
     */
    private RectF rectConfirmMarkRect = new RectF(0, 0, confirmMarkBM.getWidth(), confirmMarkBM.getHeight());
    private RectF rectCancelMarkRect = new RectF(0, 0, cancelMarkBM.getWidth(), cancelMarkBM.getHeight());

    /**
     * 线段确认,取消,顶点 图标区域
     */
    private RectF lineConfirmMarkRect = new RectF(0, 0, confirmMarkBM.getWidth(), confirmMarkBM.getHeight());
    private RectF lineCancelMarkRect = new RectF(0, 0, cancelMarkBM.getWidth(), cancelMarkBM.getHeight());

    /**
     * 四个顶点的矩形区域：用于判断是否被选中
     */
    private RectF dotUpperLeftRect = new RectF(0, 0, dotMarkBM.getWidth(), dotMarkBM.getHeight());
    private RectF dotUpperRightRect = new RectF(0, 0, dotMarkBM.getWidth(), dotMarkBM.getHeight());
    private RectF dotLowerRightRect = new RectF(0, 0, dotMarkBM.getWidth(), dotMarkBM.getHeight());
    private RectF dotLowerLeftRect = new RectF(0, 0, dotMarkBM.getWidth(), dotMarkBM.getHeight());

    /**
     * 线段的两个端点区域
     */
    private RectF dotLeftLineRect = new RectF(0, 0, dotMarkBM.getWidth(), dotMarkBM.getHeight());
    private RectF dotRightLineRect = new RectF(0, 0, dotMarkBM.getWidth(), dotMarkBM.getHeight());

    private Context context;

    private SketchpadData curSketchpadData;//记录画板上的元素
    private RectRecord curRectRecord;//矩形区域的属性
    private LineRecord curLineRecord;//线区域的属性


    private int mWidth, mHeight;//？宽高

    private int[] location = new int[2];  //当前的绝对坐标（X,Y）-> [0，1]

    private float curX, curY;//当前的坐标X Y
    private float preX, preY;//需要移动的坐标

    private float downX, downY;//触摸下的X Y

    private int drawDensity = 2;//绘制密度,数值越高图像质量越低、性能越好


    //画笔
    private Paint strokePaint;

    //画矩形边框的画笔
    private Paint rectFramePaint;

    //辅助onTouch事件，判断当前选中模式
    private int actionMode;
    private static final int ACTION_NONE = 0x100;
    private static final int ACTION_SELECT_RECT_DOT = 0x101;
    private static final int ACTION_SELECT_RECT_INSIDE = 0x102;
    private static final int ACTION_SELECT_RECT_CONFIRM = 0x103;
    private static final int ACTION_SELECT_RECT_CANCEL = 0x104;
    private static final int ACTION_SELECT_LINE_DOT = 0x105;
    private static final int ACTION_SELECT_LINE_INSIDE = 0x106;
    private static final int ACTION_SELECT_LINE_CONFIRM = 0x107;
    private static final int ACTION_SELECT_LINE_CANCEL = 0x108;

    private int selectedPos;
    private static final int UpperLeft = 0x200;
    private static final int UpperRight = 0x201;
    private static final int LowerRight = 0x202;
    private static final int LowerLeft = 0x203;
    private static final int LineLeft = 0x204;
    private static final int LineRight = 0x205;

    /**
     * 测试数据设置
     */
    private int strokeRealColor = Color.BLACK;//画笔实际颜色
    private int strokeColor = Color.BLACK;//画笔颜色
    private int strokeAlpha = 255;//画笔透明度
    private float strokeSize = 13.0f;
    private static float SCALE_MAX = 4.0f;
    private static float SCALE_MIN = 0.2f;
    private static float SCALE_MIN_LEN;
    private Canvas canvas;

    public SketchpadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
        invalidate();
    }

    private void init(Context context) {
        //暂时没用到
        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);//抗锯齿
        strokePaint.setDither(true);//防抖动
        strokePaint.setColor(strokeRealColor);//画笔颜色
        strokePaint.setStyle(Paint.Style.STROKE);//线冒样式
        strokePaint.setStrokeJoin(Paint.Join.ROUND);//线段连接处样式
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(strokeSize);

        //矩形边框画笔
        rectFramePaint = new Paint();
        rectFramePaint.setColor(Color.GRAY);
        rectFramePaint.setStrokeWidth(2f);
        rectFramePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas = canvas;
        //drawBackground(canvas);
        drawRecord(canvas);
        //X:671 Y:917
        Log.e("test", TAG);
        Log.e("test", "左上:" + dotUpperLeftRect.centerX() + "," + dotUpperLeftRect.centerY());
        Log.e("test", "右上:" + dotUpperRightRect.centerX() + "," + dotUpperRightRect.centerY());
        Log.e("test", "右下:" + dotLowerRightRect.centerX() + "," + dotLowerRightRect.centerY());
        Log.e("test", "左下:" + dotLowerLeftRect.centerX() + "," + dotLowerLeftRect.centerY());

    }

    public void drawBackground(Canvas canvas) {
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test);
//            Matrix matrix = new Matrix();
//            float wScale = (float) canvas.getWidth() / bitmap.getWidth();
//            float hScale = (float) canvas.getHeight() / bitmap.getHeight();
//            matrix.postScale(wScale, hScale);
//            canvas.drawBitmap(bitmap, matrix, null);
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
            //画矩形框
            if (curSketchpadData.drawMode == DrawMode.TYPE_RECT && curRectRecord != null) {
                //放大最大值
                SCALE_MAX = curRectRecord.scaleMax;
                //计算图片四个角点和中心点
                float[] rectCorners = calculateCorners(curRectRecord);
                Log.e("test", "cor:" + Arrays.toString(rectCorners));
                //绘制边框
                //drawRectFrame(canvas, rectCorners);
                //绘制顶点
                drawRectDot(canvas, rectCorners);
                //绘制确认/取消
                drawRectButton(canvas, rectCorners);
            }
            //TODO
            if (curSketchpadData.drawMode == DrawMode.TYPE_LINE && curLineRecord != null) {
                //放大最大值
                SCALE_MAX = curLineRecord.scaleMax;
                float[] lineCorners = calculateLine(curLineRecord);
                //绘制端点
                drawLineDot(canvas, lineCorners);
                //绘制确认/取消
                drawLineButton(canvas, lineCorners);
            }
        }
    }

    /**
     * 计算线段上需要使用到的点
     *
     * @param lineRecord
     * @return
     */
    private float[] calculateLine(LineRecord lineRecord) {
        float[] lineCornersSrc = new float[6];
        float[] lineCorners = new float[6];
        RectF rectF = lineRecord.lineOrigin;
        //0,1代表左点XY
        lineCornersSrc[0] = rectF.left;
        lineCornersSrc[1] = (rectF.top + rectF.bottom) / 2;
        //2,3代表右点XY
        lineCornersSrc[2] = rectF.right;
        lineCornersSrc[3] = (rectF.top + rectF.bottom) / 2;
        //4,5代表中点XY
        lineCornersSrc[4] = rectF.centerX();
        lineCornersSrc[5] = rectF.centerY();
        curLineRecord.matrix.mapPoints(lineCorners, lineCornersSrc);
        return lineCorners;
    }

    /**
     * 绘制线段左右顶点
     *
     * @param canvas
     * @param lineCorners
     */
    private void drawLineDot(Canvas canvas, float[] lineCorners) {
        float x;
        float y;
        //0,1代表左点XY 2,3代表右点XY 4,5代表中点XY

        x = lineCorners[0] - dotLeftLineRect.width() / 2;
        y = lineCorners[1] - dotLeftLineRect.height() / 2;
        dotLeftLineRect.offsetTo(x, y);//调整至实际绘制区域
        canvas.drawBitmap(dotMarkBM, x, y, null);

        x = lineCorners[2] - dotRightLineRect.width() / 2;
        y = lineCorners[3] - dotRightLineRect.height() / 2;
        dotRightLineRect.offsetTo(x, y);
        canvas.drawBitmap(dotMarkBM, x, y, null);
    }

    /**
     * 计算矩形四个角点和中心点
     *
     * @param record
     * @return
     */
    private float[] calculateCorners(RectRecord record) {
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
        curRectRecord.matrix.mapPoints(rectCorners, rectCornersSrc);
        return rectCorners;
    }

    /**
     * 绘制矩形边框，利用4个顶点用Path绘制边线
     *
     * @param canvas
     * @param rectCorners
     */
    private void drawRectFrame(Canvas canvas, float[] rectCorners) {
        Path rectFramePath = new Path();
        //0,1代表左上角点XY 2,3代表右上角点XY
        //6,7代表左下角点XY 4,5代表右下角点XY    //8,9代表中心点XY

        rectFramePath.moveTo(rectCorners[0], rectCorners[1]);
        rectFramePath.lineTo(rectCorners[2], rectCorners[3]);
        rectFramePath.lineTo(rectCorners[4], rectCorners[5]);
        rectFramePath.lineTo(rectCorners[6], rectCorners[7]);
        rectFramePath.close();
        rectFramePath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
        canvas.drawPath(rectFramePath, rectFramePaint);
    }


    /**
     * 绘制矩形顶点
     *
     * @param canvas
     * @param rectCorners
     */
    private void drawRectDot(Canvas canvas, float[] rectCorners) {
        float x;
        float y;
        //0,1代表左上角点XY 2,3代表右上角点XY
        //6,7代表左下角点XY 4,5代表右下角点XY    //8,9代表中心点XY

        x = rectCorners[0] - dotUpperLeftRect.width() / 2 + 5;
        y = rectCorners[1] - dotUpperLeftRect.height() / 2 + 5;
        dotUpperLeftRect.offsetTo(x, y);//调整至实际绘制区域
        canvas.drawBitmap(dotMarkBM, x, y, null);

        x = rectCorners[2] - dotUpperRightRect.width() / 2 - 5;
        y = rectCorners[3] - dotUpperRightRect.height() / 2 + 5;
        dotUpperRightRect.offsetTo(x, y);
        canvas.drawBitmap(dotMarkBM, x, y, null);

        x = rectCorners[4] - dotLowerRightRect.width() / 2 - 5;
        y = rectCorners[5] - dotLowerRightRect.height() / 2 - 5;
        dotLowerRightRect.offsetTo(x, y);
        canvas.drawBitmap(dotMarkBM, x, y, null);

        x = rectCorners[6] - dotLowerLeftRect.width() / 2 + 5;
        y = rectCorners[7] - dotLowerLeftRect.height() / 2 - 5;
        dotLowerLeftRect.offsetTo(x, y);
        canvas.drawBitmap(dotMarkBM, x, y, null);
    }

    /**
     * 绘制矩形确认/取消按钮
     *
     * @param canvas
     * @param rectCorners
     */
    private void drawRectButton(Canvas canvas, float[] rectCorners) {
        //利用边线计算按钮的位置
        float x;
        float y;

//        x = rectCorners[8] + curRectRecord.rectOrigin.width() / 2 + 20;
//        y = rectCorners[9] - confirmMarkRect.height() / 2;
        x = rectCorners[2] + 20;
        y = rectCorners[9] - rectConfirmMarkRect.height() / 2;
        rectConfirmMarkRect.offsetTo(x, y);
        canvas.drawBitmap(confirmMarkBM, x, y, null);

//        x = rectCorners[8] - curRectRecord.rectOrigin.width() / 2 - cancelMarkRect.width() - 20;
//        y = rectCorners[9] - cancelMarkRect.height() / 2;
        x = rectCorners[0] - rectCancelMarkRect.width() - 20;
        y = rectCorners[9] - rectCancelMarkRect.height() / 2;
        rectCancelMarkRect.offsetTo(x, y);
        canvas.drawBitmap(cancelMarkBM, x, y, null);
    }

    /**
     * TODO 需要一个计算公式，绘制线段确认/取消按钮
     *
     * @param canvas
     * @param lineCorners
     */
    private void drawLineButton(Canvas canvas, float[] lineCorners) {
        float x;
        float y;
        Log.e("btn", "center:" + lineCorners[4] + "," + lineCorners[5]);
        Log.e("btn", "left:" + lineCorners[0] + "," + lineCorners[1]);
        Log.e("btn", "right:" + lineCorners[2] + "," + lineCorners[3]);
        Log.e("btn", "width:" + lineConfirmMarkRect.width() + "height:" + lineConfirmMarkRect.height());
        Log.e("btn", "1111:" + curLineRecord.lineOrigin.width() + "22222:" + curLineRecord.lineOrigin.height());

        x = lineCorners[4] - lineConfirmMarkRect.width() / 2;
        y = lineCorners[5] - lineConfirmMarkRect.height() - 30;
        lineConfirmMarkRect.offsetTo(x, y);
        canvas.drawBitmap(confirmMarkBM, x, y, null);

        x = lineCorners[4] - lineCancelMarkRect.width() / 2;
        y = lineCorners[5] + lineCancelMarkRect.height() / 2 - 20;
        lineCancelMarkRect.offsetTo(x, y);
        canvas.drawBitmap(cancelMarkBM, x, y, null);

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
                touchMove();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:

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
        if (curSketchpadData.drawMode == DrawMode.TYPE_RECT) {
            //触摸点
            float[] downPoint = new float[]{downX * drawDensity, downY * drawDensity};
            //如果触摸了点
            if (isInRectDot(downPoint)) {
                return;
            }
            //如果触摸矩形内部区域
            if (isInRect(curRectRecord, downPoint)) {
                actionMode = ACTION_SELECT_RECT_INSIDE;
                return;
            }
            //判断是否点击了确认/取消区域
            if (isInButtonRect(downPoint)) {
                return;
            }
            //判断是否点击了其他图片
            selectOtherRect(downPoint);
        }
        //如果当前是线段模式
        if (curSketchpadData.drawMode == DrawMode.TYPE_LINE) {
            //触摸点
            float[] downPoint = new float[]{downX * drawDensity, downY * drawDensity};
            //如果触摸了点
            if (isInLineDot(downPoint)) {
                return;
            }
            //如果触摸内部区域
            if (isInLine(curLineRecord, downPoint)) {
                actionMode = ACTION_SELECT_LINE_INSIDE;
                return;
            }
            //判断是否点击了确认/取消区域
            if (isInLineButtonRect(downPoint)) {
                return;
            }
            selectOtherLine(downPoint);
        }
    }

    private void touchMove() {
        //如果是矩形
        if (curSketchpadData.drawMode == DrawMode.TYPE_RECT && curRectRecord != null) {
            if (actionMode == ACTION_SELECT_RECT_DOT) {
                //选中边角点临边移动：拖动
                rectDotMove(curRectRecord, (curX - preX) * drawDensity, (curY - preY) * drawDensity);
            } else if (actionMode == ACTION_SELECT_RECT_INSIDE) {
                //选中内部则整体移动：拖拽
                rectMove((curX - preX) * drawDensity, (curY - preY) * drawDensity);
            }
        }
        //如果是线段
        if (curSketchpadData.drawMode == DrawMode.TYPE_LINE && curLineRecord != null) {
            if (actionMode == ACTION_SELECT_LINE_DOT) {
                //拉长缩短+旋转
                lineMoveAndRotate(curLineRecord, (curX - preX) * drawDensity, (curY - preY) * drawDensity);

            } else if (actionMode == ACTION_SELECT_LINE_INSIDE) {
                //选中内部则整体移动：拖拽
                lineMove((curX - preX) * drawDensity, (curY - preY) * drawDensity);
            }
        }
    }

    private void touchUp() {

    }

    private void lineMoveAndRotate(LineRecord lineRecord, float x, float y) {
        float[] corners = calculateLine(lineRecord);

        /**
         * 旋转用参数
         */
        PointF preVector = new PointF();
        PointF curVector = new PointF();
        PointF verticalVec;
        double preVectorLen;
        double curVectorLen;
        double cosAlpha;
        double dAngle;
        double vDot;

        /**
         * 移动参数
         */
        float newX;
        float newY;
        float xscale;
        float yscale;

        switch (selectedPos) {
            case LineLeft:
                //旋转
                preVector.set((preX * drawDensity - corners[2]), preY * drawDensity - corners[3]);//旋转后向量
                curVector.set(curX * drawDensity - corners[2], curY * drawDensity - corners[3]);//旋转前向量
                preVectorLen = getVectorLength(preVector);
                curVectorLen = getVectorLength(curVector);
                cosAlpha = (preVector.x * curVector.x + preVector.y * curVector.y)
                        / (preVectorLen * curVectorLen);
                if (cosAlpha > 1.0f) {
                    cosAlpha = 1.0f;
                }
                dAngle = Math.acos(cosAlpha) * 180.0 / Math.PI;
                preVector.x /= preVectorLen;
                preVector.y /= preVectorLen;
                curVector.x /= curVectorLen;
                curVector.y /= curVectorLen;
                verticalVec = new PointF(curVector.y, -curVector.x);
                vDot = preVector.x * verticalVec.x + preVector.y * verticalVec.y;
                if (vDot > 0) {
                } else {
                    dAngle = -dAngle;
                }
                lineRecord.matrix.postRotate((float) dAngle, corners[2], corners[3]);
                break;
            case LineRight:
                preVector.set((preX * drawDensity - corners[0]), preY * drawDensity - corners[1]);//旋转后向量
                curVector.set(curX * drawDensity - corners[0], curY * drawDensity - corners[1]);//旋转前向量
                preVectorLen = getVectorLength(preVector);
                curVectorLen = getVectorLength(curVector);
                cosAlpha = (preVector.x * curVector.x + preVector.y * curVector.y)
                        / (preVectorLen * curVectorLen);
                if (cosAlpha > 1.0f) {
                    cosAlpha = 1.0f;
                }
                dAngle = Math.acos(cosAlpha) * 180.0 / Math.PI;
                preVector.x /= preVectorLen;
                preVector.y /= preVectorLen;
                curVector.x /= curVectorLen;
                curVector.y /= curVectorLen;
                verticalVec = new PointF(curVector.y, -curVector.x);
                vDot = preVector.x * verticalVec.x + preVector.y * verticalVec.y;
                if (vDot > 0) {
                } else {
                    dAngle = -dAngle;
                }
                lineRecord.matrix.postRotate((float) dAngle, corners[0], corners[1]);
                break;
        }
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
     * TODO 选中顶点，临近的两边移动, bug：移动的距离和实际手指距离不同，问题在于newX=中的rectRecord.rectOrigin本体没变化
     *
     * @param
     */
    private void rectDotMove(RectRecord rectRecord, float x, float y) {
        float newX;
        float newY;
        float xscale;
        float yscale;

        float[] corners = calculateCorners(rectRecord);
        //rectRecord.rectOrigin.set(0, 0, newX, newY);
        switch (selectedPos) {
            case UpperLeft:
                newX = rectRecord.rectOrigin.width() - x;
                newY = rectRecord.rectOrigin.height() - y;
                xscale = newX / rectRecord.rectOrigin.width();
                yscale = newY / rectRecord.rectOrigin.height();
                rectRecord.matrix.postScale(xscale, yscale, corners[4], corners[5]);
                Log.e("tag", "test:" + rectRecord.rectOrigin.width() + ",height:" + rectRecord.rectOrigin.height());
                break;
            case UpperRight:
                newX = rectRecord.rectOrigin.width() + x;
                newY = rectRecord.rectOrigin.height() - y;
                xscale = newX / rectRecord.rectOrigin.width();
                yscale = newY / rectRecord.rectOrigin.height();
                rectRecord.matrix.postScale(xscale, yscale, corners[6], corners[7]);
                //rectRecord.matrix.postScale(xscale, 1, corners[6], corners[7]);
                //rectRecord.rectOrigin.set(rectRecord.rectOrigin.left, rectRecord.rectOrigin.top, rectRecord.rectOrigin.right * xscale, rectRecord.rectOrigin.bottom);
                Log.e("tag", "test:" + rectRecord.rectOrigin.width() + ",height:" + rectRecord.rectOrigin.height());
                break;
            case LowerLeft:
                newX = rectRecord.rectOrigin.width() - x;
                newY = rectRecord.rectOrigin.height() + y;
                xscale = newX / rectRecord.rectOrigin.width();
                yscale = newY / rectRecord.rectOrigin.height();
                rectRecord.matrix.postScale(xscale, yscale, corners[2], corners[3]);
                break;
            case LowerRight:
                newX = rectRecord.rectOrigin.width() + x;
                newY = rectRecord.rectOrigin.height() + y;
                xscale = newX / rectRecord.rectOrigin.width();
                yscale = newY / rectRecord.rectOrigin.height();
                rectRecord.matrix.postScale(xscale, yscale, corners[0], corners[1]);
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
        if (dotUpperLeftRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_RECT_DOT;
            selectedPos = UpperLeft;
            return true;
        } else if (dotUpperRightRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_RECT_DOT;
            selectedPos = UpperRight;
            return true;
        } else if (dotLowerLeftRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_RECT_DOT;
            selectedPos = LowerLeft;
            return true;
        } else if (dotLowerRightRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_RECT_DOT;
            selectedPos = LowerRight;
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
        if (dotLeftLineRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_LINE_DOT;
            selectedPos = LineLeft;
            return true;
        } else if (dotRightLineRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_LINE_DOT;
            selectedPos = LineRight;
            return true;
        }
        selectedPos = -1;
        return false;
    }

    /**
     * 判断是否在线段内部 TODO bug精准度，不好选中的问题
     *
     * @param lineRecord
     * @param downPoint
     * @return
     */
    private boolean isInLine(LineRecord lineRecord, float[] downPoint) {
        //容差值
        float tol = 2.0f;
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
     * 判断触摸点是否在外部按钮上
     *
     * @param downPoint
     * @return
     */
    private boolean isInButtonRect(float[] downPoint) {
        if (rectConfirmMarkRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_RECT_CONFIRM;
            Toast.makeText(context, "确认顶点，调用JS方法", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (rectCancelMarkRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_RECT_CANCEL;
            curSketchpadData.rectRecordList.remove(curRectRecord);
            setRectRecord(null);//TODO addNull可能会导致的问题
            actionMode = ACTION_NONE;
            return true;
        }
        return false;
    }

    private boolean isInLineButtonRect(float[] downPoint) {
        if (lineConfirmMarkRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_LINE_CONFIRM;
            Toast.makeText(context, "确认顶点，调用JS方法", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (lineCancelMarkRect.contains(downPoint[0], downPoint[1])) {
            actionMode = ACTION_SELECT_LINE_CANCEL;
            curSketchpadData.lineRecordList.remove(curLineRecord);
            setLineRecord(null);//TODO addNull可能会导致的问题
            actionMode = ACTION_NONE;
            return true;
        }
        return false;
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


    @IntDef({DrawMode.TYPE_NONE, DrawMode.TYPE_RECT,
            DrawMode.TYPE_LINE, DrawMode.TYPE_ERASER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DrawMode {

        int TYPE_NONE = 0x00;//未选择模式

        int TYPE_RECT = 0x01;//绘制矩形

        int TYPE_LINE = 0x02;//绘制线

        int TYPE_ERASER = 0x03;//橡皮擦
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
    }

    /**
     * 添加矩形区域外观图片
     *
     * @param bitmap 矩形区域的外观bitmap
     */
    public void addRectRecord(Bitmap bitmap, int width, int height) {
        if (bitmap != null) {
            if (width < 100 || height < 100) {
                Toast.makeText(context, "宽高设置请大于100", Toast.LENGTH_SHORT).show();
                return;
            }
            RectRecord rectRecord = initRectRecord(setBitmapWH(bitmap, width, height));
            setRectRecord(rectRecord);
        } else {
            Toast.makeText(context, "bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 添加线
     *
     * @param length
     */
    public void addLineRecord(Bitmap bitmap, int length) {
        if (bitmap != null) {
            LineRecord lineRecord = initLineRecord(setBitmapWH(bitmap, length, 8), length);
            setLineRecord(lineRecord);
        } else {
            Toast.makeText(context, "bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置最后绘制出的bitmap的宽高
     *
     * @param bitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    private Bitmap setBitmapWH(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        //取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    /**
     * 初始化矩形数据
     *
     * @param bitmap
     * @return
     */
    @NonNull
    private RectRecord initRectRecord(Bitmap bitmap) {
        RectRecord rectRecord = new RectRecord();
        rectRecord.bitmap = bitmap;
        rectRecord.rectOrigin = new RectF(0, 0, rectRecord.bitmap.getWidth(), rectRecord.bitmap.getHeight());
        rectRecord.scaleMax = getMaxScale(rectRecord.rectOrigin);
        if (curSketchpadData.rectRecordList.size() > 0 && curRectRecord != null) {//有多个图片
            rectRecord.matrix = new Matrix(curRectRecord.matrix);
            rectRecord.matrix.postTranslate(50, 50);//单位PX
        } else {
            rectRecord.matrix = new Matrix();
            rectRecord.matrix.postTranslate(getWidth() / 2 - bitmap.getWidth() / 2, getHeight() / 2 - bitmap.getHeight() / 2);

        }
        return rectRecord;
    }

    /**
     * 初始线段数据
     *
     * @param bitmap
     * @return
     */
    @NonNull
    private LineRecord initLineRecord(Bitmap bitmap, int length) {
        LineRecord lineRecord = new LineRecord();
        lineRecord.bitmap = bitmap;
        lineRecord.lineOrigin = new RectF(0, 0, lineRecord.bitmap.getWidth(), lineRecord.bitmap.getHeight());
        lineRecord.scaleMax = getMaxScale(lineRecord.lineOrigin);
        if (curSketchpadData.lineRecordList.size() > 0 && curLineRecord != null) {//有多个图片
            lineRecord.matrix = new Matrix(curLineRecord.matrix);
            lineRecord.matrix.postTranslate(0, 80);//单位PX
        } else {
            lineRecord.matrix = new Matrix();
            lineRecord.matrix.postTranslate(getWidth() / 2 - bitmap.getWidth() / 2, getHeight() / 2 - length);
        }
        return lineRecord;
    }

    /**
     * 获取放大倍数
     *
     * @param rectF
     * @return
     */
    private float getMaxScale(RectF rectF) {
        return Math.max(getWidth(), getHeight()) / Math.max(rectF.width(), rectF.height());
    }

    /**
     * 设置矩形区域，数据已暂存给 curRectRecord，下一步来绘制
     *
     * @param rectRecord
     */
    private void setRectRecord(RectRecord rectRecord) {
        curSketchpadData.rectRecordList.remove(rectRecord);
        curSketchpadData.rectRecordList.add(rectRecord);//TODO 是否是问题存疑：当add(null)的时候，list.size=1 但是对象不存在
        curRectRecord = rectRecord;
        invalidate();
    }


    /**
     * 设置线区域，数据已暂存给 curLineRecord，下一步来绘制
     *
     * @param lineRecord
     */
    private void setLineRecord(LineRecord lineRecord) {
        curSketchpadData.lineRecordList.remove(lineRecord);
        curSketchpadData.lineRecordList.add(lineRecord);
        curLineRecord = lineRecord;
        invalidate();
    }


    /**
     * 获取当前绘画模式
     *
     * @return
     */
    public int getDrawMode() {
        return curSketchpadData.drawMode;
    }

    /**
     * 设置当前绘画模式
     *
     * @param drawMode
     */
    public void setDrawMode(int drawMode) {
        this.curSketchpadData.drawMode = drawMode;
        invalidate();
    }
}
