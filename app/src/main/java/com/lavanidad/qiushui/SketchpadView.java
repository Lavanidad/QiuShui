package com.lavanidad.qiushui;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
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

import com.lavanidad.qiushui.bean.RectRecord;
import com.lavanidad.qiushui.bean.SketchpadData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SketchpadView extends View {

    public static final String TAG = SketchpadView.class.getSimpleName();

    /**
     * 确认,取消,顶点 图标
     */
    private Bitmap confirmMarkBM = BitmapFactory.decodeResource(getResources(), R.mipmap.round_confirm);
    private Bitmap cancelMarkBM = BitmapFactory.decodeResource(getResources(), R.mipmap.round_cancel);
    private Bitmap dotMarkBM = BitmapFactory.decodeResource(getResources(), R.mipmap.round_dot);

    /**
     * 确认,取消,顶点 图标区域
     */
    private RectF confirmMarkRect = new RectF(0, 0, confirmMarkBM.getWidth(), confirmMarkBM.getHeight());
    private RectF cancelMarkRect = new RectF(0, 0, cancelMarkBM.getWidth(), cancelMarkBM.getHeight());
    private RectF dotMarkRect = new RectF(0, 0, dotMarkBM.getWidth(), dotMarkBM.getHeight());

    private Context context;

    private SketchpadData curSketchpadData;//记录画板上的元素
    private RectRecord curRectRecord;//矩形区域的属性


    private int mWidth, mHeight;//？宽高

    private int[] location = new int[2];  //当前的绝对坐标（X,Y）-> [0，1]

    private float curX, curY;//当前的坐标X Y

    private float downX, downY;//触摸下的X Y

    private int drawDensity = 2;//绘制密度,数值越高图像质量越低、性能越好


    //画笔
    private Paint strokePaint;

    //画矩形边框的画笔
    private Paint rectFramePaint;


    /**
     * 测试数据设置
     */
    private int strokeRealColor = Color.BLACK;//画笔实际颜色
    private int strokeColor = Color.BLACK;//画笔颜色
    private int strokeAlpha = 255;//画笔透明度
    private float strokeSize = 13.0f;
    private static float SCALE_MAX = 4.0f;


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
        drawRecord(canvas);
    }

    private void drawRecord(Canvas canvas) {
        drawRecord(canvas, true);
    }


    /**
     * 绘制的主要方法
     *
     * @param canvas
     * @param isDrawBoard
     */
    private void drawRecord(Canvas canvas, boolean isDrawBoard) {
        if (curSketchpadData != null) {
            //画矩形区域本体:设置的图片
            for (RectRecord record : curSketchpadData.rectRecordList) {//TODO 可优化，不使用list
                if (record != null) {
                    canvas.drawBitmap(record.bitmap, record.matrix, null);
                }
            }
            //画矩形选中框
            if (curSketchpadData.drawMode != DrawMode.TYPE_NONE && curSketchpadData.drawMode == DrawMode.TYPE_RECT
                    && curRectRecord != null) {
                //放大最大值
                SCALE_MAX = curRectRecord.scaleMax;
                //计算图片四个角点和中心点
                float[] rectCorners = calculateCorners(curRectRecord);
                //绘制边框
                //TODO drawRectFrame(canvas, rectCorners);
                //绘制顶点
                drawRectDot(canvas, rectCorners);
                //绘制确认/取消
                drawRectButton(canvas, rectCorners);
            }
        }
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

        x = rectCorners[0] - dotMarkRect.width() / 2 + 5;
        y = rectCorners[1] - dotMarkRect.height() / 2 + 5;
        dotMarkRect.offset(x, y);
        canvas.drawBitmap(dotMarkBM, x, y, null);

        x = rectCorners[2] - dotMarkRect.width() / 2 - 5;
        y = rectCorners[3] - dotMarkRect.height() / 2 + 5;
        dotMarkRect.offset(x, y);
        canvas.drawBitmap(dotMarkBM, x, y, null);

        x = rectCorners[4] - dotMarkRect.width() / 2 - 5;
        y = rectCorners[5] - dotMarkRect.height() / 2 - 5;
        dotMarkRect.offset(x, y);
        canvas.drawBitmap(dotMarkBM, x, y, null);

        x = rectCorners[6] - dotMarkRect.width() / 2 + 5;
        y = rectCorners[7] - dotMarkRect.height() / 2 - 5;
        dotMarkRect.offset(x, y);
        canvas.drawBitmap(dotMarkBM, x, y, null);
    }

    /**
     * 绘制确认/取消按钮
     *
     * @param canvas
     * @param rectCorners
     */
    private void drawRectButton(Canvas canvas, float[] rectCorners) {
        //位置在中点的延长线上
        float x;
        float y;

        x = rectCorners[8] + curRectRecord.rectOrigin.width() / 2 + 20;
        y = rectCorners[9] - confirmMarkRect.height() / 2;
        canvas.drawBitmap(confirmMarkBM, x, y, null);

        x = rectCorners[8] - curRectRecord.rectOrigin.width() / 2 - cancelMarkRect.width() - 20;
        y = rectCorners[9] - cancelMarkRect.height() / 2;
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
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:

                break;
        }
        return true;
    }


    private void touchDown() {
        downX = curX;
        downY = curY;
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
        Log.e(TAG, "list size1:" + curSketchpadData.rectRecordList.size());
        RectRecord rectRecord = new RectRecord();
        rectRecord.bitmap = bitmap;
        rectRecord.rectOrigin = new RectF(0, 0, rectRecord.bitmap.getWidth(), rectRecord.bitmap.getHeight());
        rectRecord.scaleMax = getMaxScale(rectRecord.rectOrigin);
        if (curSketchpadData.rectRecordList.size() > 0) {//当前有多个图片
            rectRecord.matrix = new Matrix(curRectRecord.matrix);
            rectRecord.matrix.postTranslate(50, 50);//TODO 单位PX
        } else {
            rectRecord.matrix = new Matrix();
            rectRecord.matrix.postTranslate(getWidth() / 2 - bitmap.getWidth() / 2, getHeight() / 2 - bitmap.getHeight() / 2);

        }

        return rectRecord;
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
        curSketchpadData.rectRecordList.add(rectRecord);
        curRectRecord = rectRecord;
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
