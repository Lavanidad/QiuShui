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

    private Path strokePath;


    /**
     * 测试数据设置
     */
    private int strokeRealColor = Color.BLACK;//画笔实际颜色
    private int strokeColor = Color.BLACK;//画笔颜色
    private int strokeAlpha = 255;//画笔透明度
    private float strokeSize = 3.0f;


    public SketchpadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
        invalidate();
    }

    private void init(Context context) {
        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);//抗锯齿
        strokePaint.setDither(true);//防抖动
        strokePaint.setColor(strokeRealColor);//画笔颜色
        strokePaint.setStyle(Paint.Style.STROKE);//线冒样式
        strokePaint.setStrokeJoin(Paint.Join.ROUND);//线段连接处样式
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(strokeSize);
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


    private void drawRecord(Canvas canvas, boolean isDrawBoard) {
        if (curSketchpadData != null) {
            //画矩形区域本体
            for (RectRecord record : curSketchpadData.rectRecordList) {
                if (record != null) {
                    Log.e(TAG, "drawRecord" + record.bitmap.getWidth() + record.bitmap.getHeight());
                    record.bitmap.setWidth(520);
                    record.bitmap.setHeight(520);
                    canvas.drawBitmap(record.bitmap, record.matrix, null);
                }
            }
        }
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


    @IntDef({DrawMode.TYPE_STROKE, DrawMode.TYPE_ERASER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DrawMode {

        int TYPE_STROKE = 0x00;

        int TYPE_ERASER = 0x03;
    }

    public void setSketchData(SketchpadData sketchpadData) {
        this.curSketchpadData = sketchpadData;
        curRectRecord = null;
    }

    /**
     * 添加矩形区域，
     *
     * @param bitmap 矩形区域的外观bitmap
     */
    public void addRectRecord(Bitmap bitmap, int width, int height) {
        if (bitmap != null) {
            Bitmap bitmap1 = getNewBitmap(bitmap, width, height);

            RectRecord rectRecord = initRectRecord(bitmap1);
            setRectRecord(rectRecord);
        } else {
            Toast.makeText(context, "bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap getNewBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        // 获得图片的宽高.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newBitmap;
    }

    /**
     * 初始化数据
     *
     * @param bitmap
     * @return
     */
    @NonNull
    private RectRecord initRectRecord(Bitmap bitmap) {
        RectRecord rectRecord = new RectRecord();
        rectRecord.bitmap = bitmap;
        rectRecord.rectSrc = new RectF(0, 0, rectRecord.bitmap.getWidth(), rectRecord.bitmap.getHeight());
        rectRecord.scaleMax = getMaxScale(rectRecord.rectSrc);
        rectRecord.matrix = new Matrix();
        rectRecord.matrix.postTranslate(getWidth() / 2 - bitmap.getWidth() / 2, getHeight() / 2 - bitmap.getHeight() / 2);
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
     * 设置矩形区域，下一步来绘制
     *
     * @param rectRecord
     */
    private void setRectRecord(RectRecord rectRecord) {
        curSketchpadData.rectRecordList.remove(rectRecord);
        curSketchpadData.rectRecordList.add(rectRecord);
        curRectRecord = rectRecord;
        invalidate();
    }
}
