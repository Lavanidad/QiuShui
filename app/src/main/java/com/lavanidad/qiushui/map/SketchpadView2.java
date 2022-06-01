package com.lavanidad.qiushui.map;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
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
import com.lavanidad.qiushui.R;
import com.lavanidad.qiushui.map.bean.BackgroundRecord;
import com.lavanidad.qiushui.map.bean.DrawRecord;
import com.lavanidad.qiushui.map.bean.SketchpadData2;
import com.lavanidad.qiushui.utils.DrawUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SketchpadView2 extends View {

    public static final String TAG = SketchpadView2.class.getSimpleName();

    /**
     * 确认，取消，旋转，缩放，4个拉动点
     */
    private Bitmap confirmMarkBM;
    private Bitmap cancelMarkBM;
    private Bitmap dotMarkBM;
    private Bitmap rotateMarkBM;
    private Bitmap scaleMarkBM;

    /**
     * 对应上述区域：用于判断是否被选中
     */
    private RectF rectUpperLeftRect;
    private RectF rectUpperRightRect;
    private RectF rectLowerRightRect;
    private RectF rectLowerLeftRect;
    private RectF rectUpperMidRect;
    private RectF rectBottomMidRect;
    private RectF rectLeftMidRect;
    private RectF rectRightMidRect;

    private RectF dotConfirmMarkRect;
    private RectF dotCancelMarkRect;

    private float rectRecFSize;//区域的大小
    private float rectDotSize;//端点的大小

    private int[] absLocation = new int[2];//绝对坐标
    private float curX, curY;//当前的坐标X Y
    private float preX, preY;
    private static int minWidth = 50;//涉及拉伸缩放，给绘制对象设置的最小宽高
    private static int minHeight = 50;
    private static float SCALE_MAX = 1.8f;//涉及拉伸缩放，给背景设置的最小宽高
    private static float SCALE_MIN = 0.8f;
    private RectF tempRect;//辅助
    private float mRatio = 1;

    private SketchpadData2 curSketchpadData;//记录画板上的元素
    private BackgroundRecord curBackgroundRecord;//当前底图
    private DrawRecord curDrawRecord;//当前绘制对象
    private Context context;

    private Paint rectFramePaint;//矩形边框
    private Paint linePaint;//线段边框
    private Paint dotPaint;//点位


    //辅助onTouch事件，判断当前选中模式
    private int actionMode;
    public static final int ACTION_NONE = 0x00;
    public static final int ACTION_DRAW = 0x01;
    private static final int ACTION_SELECT_DOT_INSIDE = 0x02;


    private static final int ACTION_SELECT_BACKGROUND_SCALE = 0x05;

    //手势监听
    private GestureDetector mGestureDetector = null;
    private ScaleGestureDetector mScaleGestureDetector = null;

    //对外接口
    private OnDrawControlListener onDrawControlListener;

    private boolean canBackGroundMove = true;

    /**
     * 测试用数据设置
     */
    Paint paint = new Paint();


    public SketchpadView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initSize();//用来初始化各种绘制大小
        initParam(context);
        invalidate();
    }


    private void initSize() {


        rectRecFSize = 36;
        rectDotSize = 18;
        rotateMarkBM = DrawUtil.setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_rotate), rectDotSize, rectDotSize);
        dotMarkBM = DrawUtil.setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_dot), rectDotSize, rectDotSize);
        confirmMarkBM = DrawUtil.setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_confirm), rectDotSize, rectDotSize);
        cancelMarkBM = DrawUtil.setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_cancel), rectDotSize, rectDotSize);
        scaleMarkBM = DrawUtil.setBitmapWH(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_scale), rectDotSize, rectDotSize);


        rectUpperLeftRect = new RectF(0, 0, rectRecFSize, rectRecFSize);
        rectUpperRightRect = new RectF(0, 0, rectRecFSize, rectRecFSize);
        rectLowerRightRect = new RectF(0, 0, rectRecFSize, rectRecFSize);
        rectLowerLeftRect = new RectF(0, 0, rectRecFSize, rectRecFSize);

        rectUpperMidRect = new RectF(0, 0, rectRecFSize, rectRecFSize);
        rectBottomMidRect = new RectF(0, 0, rectRecFSize, rectRecFSize);
        rectLeftMidRect = new RectF(0, 0, rectRecFSize, rectRecFSize);
        rectRightMidRect = new RectF(0, 0, rectRecFSize, rectRecFSize);

        dotConfirmMarkRect = new RectF(0, 0, rectDotSize, rectDotSize);
        dotCancelMarkRect = new RectF(0, 0, rectDotSize, rectDotSize);


        //矩形边框画笔
        rectFramePaint = new Paint();
        rectFramePaint.setColor(ColorUtils.getColor(R.color.orangered));
        rectFramePaint.setStrokeWidth(2f);
        rectFramePaint.setStyle(Paint.Style.STROKE);
        rectFramePaint.setStyle(Paint.Style.FILL);

        //线段画笔
        linePaint = new Paint();
        linePaint.setColor(getResources().getColor(R.color.red));
        linePaint.setStrokeWidth(3f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setPathEffect(new DashPathEffect(new float[]{6, 6}, 0));


        dotPaint = new Paint();
        dotPaint.setColor(getResources().getColor(R.color.red));
        dotPaint.setStrokeWidth(3f);
        dotPaint.setStyle(Paint.Style.STROKE);
        dotPaint.setPathEffect(new DashPathEffect(new float[]{6, 6}, 0));

    }

    private void initParam(Context context) {
        mGestureDetector = new GestureDetector(context, simpleOnGestureListener);
        mScaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
        tempRect = new RectF();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mWidth = MeasureSpec.getSize(widthMeasureSpec);
        int mHeight = MeasureSpec.getSize(heightMeasureSpec);
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

    private void drawRecord(Canvas canvas) {
        if (curSketchpadData == null) {
            return;
        }
        //绘制本体
        for (DrawRecord record : curSketchpadData.drawRecordList) {
            if (record.type == 0) {
                float[] rectCorners = DrawUtil.calculateRectCorners(record);
                drawRectFrame(canvas, rectCorners);



            } else if (record.type == 1) {
                float[] lineCorners = DrawUtil.calculateRectCorners(record);
                drawLineFrame(canvas, lineCorners);
            } else {
                //绘制点
                canvas.drawBitmap(record.bitmap, record.matrix, null);
            }
        }
        //绘制选中状态
        if (curDrawRecord != null && (actionMode == ACTION_DRAW || actionMode == ACTION_SELECT_DOT_INSIDE)) {
            if (curDrawRecord.type == 2 || curDrawRecord.type == 3) {
                float[] dotCorners = DrawUtil.calculateRectCorners(curDrawRecord);
                drawDot(canvas, dotCorners);
                drawDotFrame(canvas, dotCorners);
            } else {
                float[] rectCorners2 = DrawUtil.calculateRectCorners(curDrawRecord);
                drawRectDot(canvas, rectCorners2);
            }
        }

        //if (选中) {
        for (DrawRecord record : curSketchpadData.selectedDrawRecord) {
            if (record.type == 2 || record.type == 3) {
                float[] dotCorners = DrawUtil.calculateRectCorners(record);
                drawDot(canvas, dotCorners);
                drawDotFrame(canvas, dotCorners);
            } else {
                float[] rectCorners2 = DrawUtil.calculateRectCorners(record);
                drawRectDot(canvas, rectCorners2);
            }
        }
        //  }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

//TODO 0515        getLocationInWindow(absLocation);
//        curX = (event.getRawX() - absLocation[0]);
//        curY = (event.getRawY() - absLocation[1]);
        curX = event.getX();
        curY = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchDown();
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
                    float downDistance = DrawUtil.getDistance(event);
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
        float[] downPoint = new float[]{curX, curY};

        selectOtherDraw(downPoint);
        if (curDrawRecord == null) {
            return;
        }
        if (curDrawRecord.type == 3 || curDrawRecord.type == 2) {
            //是否触摸内部区域
            if (isInDrawInside(curDrawRecord, downPoint)) {
                actionMode = ACTION_SELECT_DOT_INSIDE;
                return;
            }
            //是否触摸顶点区域
            if (isInDrawDot(downPoint)) {
                canBackGroundMove = false;
                return;
            }
        } else {
            if (isInRectDot(downPoint)) {
                canBackGroundMove = false;
                return;
            }
        }
    }


    private void touchMove(MotionEvent event) {
        //TODO 0515
        if (curBackgroundRecord != null && canBackGroundMove && actionMode != ACTION_SELECT_BACKGROUND_SCALE) {
            onZoomMove((curX - preX), (curY - preY));
        }
        if (actionMode == ACTION_SELECT_BACKGROUND_SCALE) {
            mScaleGestureDetector.onTouchEvent(event);
        }
        if (curDrawRecord != null && (curDrawRecord.type == 2 || curDrawRecord.type == 3)) {
            if (actionMode == ACTION_SELECT_DOT_INSIDE) {
                //拖动
                curDrawRecord.matrix.postTranslate((int) (curX - preX), (int) (curY - preY));
            }
        }
    }

    //TODO 0515
    private void onZoomMove(float distanceX, float distanceY) {
        curBackgroundRecord.photoRectSrc.offset((int) distanceX, (int) distanceY);
        float left = curBackgroundRecord.photoRectSrc.left;
        float top = curBackgroundRecord.photoRectSrc.top;
        float right = curBackgroundRecord.photoRectSrc.right;
        float bottom = curBackgroundRecord.photoRectSrc.bottom;
        curBackgroundRecord.matrix.postTranslate((int) distanceX, (int) distanceY);


        if (curSketchpadData.drawRecordList.size() > 0) {
            for (int i = 0; i < curSketchpadData.drawRecordList.size(); i++) {
                if (curSketchpadData.drawRecordList.get(i) != null &&
                        (curSketchpadData.drawRecordList.get(i).type == 0 || curSketchpadData.drawRecordList.get(i).type == 1)) {
                    curSketchpadData.drawRecordList.get(i).rectOrigin.offset((int) distanceX, (int) distanceY);
                    curSketchpadData.drawRecordList.get(i).matrix.reset();
                    curSketchpadData.drawRecordList.get(i).matrix.postTranslate(-curSketchpadData.drawRecordList.get(i).rectOrigin.centerX(), -curSketchpadData.drawRecordList.get(i).rectOrigin.centerY());
                    curSketchpadData.drawRecordList.get(i).matrix.postRotate(curSketchpadData.drawRecordList.get(i).mRotation);
                    curSketchpadData.drawRecordList.get(i).matrix.postTranslate(curSketchpadData.drawRecordList.get(i).rectOrigin.centerX(), curSketchpadData.drawRecordList.get(i).rectOrigin.centerY());
                } else if (curSketchpadData.drawRecordList.get(i) != null &&
                        (curSketchpadData.drawRecordList.get(i).type == 2 || curSketchpadData.drawRecordList.get(i).type == 3)) {
                    curSketchpadData.drawRecordList.get(i).matrix.postTranslate((int) distanceX, (int) distanceY);
                }
            }
        }
        invalidate();
    }

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            onMouseMove(curSketchpadData.touchMode, e2.getX(), e2.getY(), distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }
    };

    private ScaleGestureDetector.OnScaleGestureListener scaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            //onScaleAction(scaleGestureDetector);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

        }
    };

    /**
     * 画矩形本体
     *
     * @param canvas
     * @param lineCorners
     */
    private Drawable mainDrawable;
    private void drawRectFrame(Canvas canvas, float[] lineCorners) {
        mainDrawable = getResources().getDrawable(R.mipmap.icon_eraser_pressed);
        mainDrawable.setBounds((int) curDrawRecord.rectOrigin.left, (int) curDrawRecord.rectOrigin.top,
                (int) curDrawRecord.rectOrigin.right, (int) curDrawRecord.rectOrigin.bottom);
        mainDrawable.draw(canvas);
    }

    private void drawLineFrame(Canvas canvas, float[] lineCorners) {

        Path rectFramePath = new Path();
        rectFramePath.moveTo(lineCorners[0], lineCorners[1]);
        rectFramePath.lineTo(lineCorners[2], lineCorners[3]);
        rectFramePath.lineTo(lineCorners[4], lineCorners[5]);
        rectFramePath.lineTo(lineCorners[6], lineCorners[7]);

        Path rectFramePath2 = new Path();
        rectFramePath2.moveTo(lineCorners[12], lineCorners[13]);
        rectFramePath2.lineTo(lineCorners[16], lineCorners[17]);

        canvas.drawPath(rectFramePath2, linePaint);
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
        float x;
        float y;

        float bitmapx;
        float bitmapy;

        //左上
        x = rectCorners[0] - rectUpperLeftRect.width() + rectUpperLeftRect.width() / 2;
        y = rectCorners[1] - rectUpperLeftRect.height() + rectUpperLeftRect.height() / 2;
        rectUpperLeftRect.offsetTo(x, y);//调整至实际绘制区域
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(scaleMarkBM, bitmapx, bitmapy, null);

        //右上
        x = rectCorners[2] - rectUpperRightRect.width() / 2;
        y = rectCorners[3] - rectUpperRightRect.height() + rectUpperRightRect.height() / 2;
        rectUpperRightRect.offsetTo(x, y);
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(confirmMarkBM, bitmapx, bitmapy, null);

        //右下
        x = rectCorners[4] - rectLowerRightRect.width() / 2;
        y = rectCorners[5] - rectLowerRightRect.width() / 2;
        rectLowerRightRect.offsetTo(x, y);
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(rotateMarkBM, bitmapx, bitmapy, null);

        //左下
        x = rectCorners[6] - rectLowerLeftRect.width() + rectLowerLeftRect.width() / 2;
        y = rectCorners[7] - rectLowerLeftRect.height() / 2;
        rectLowerLeftRect.offsetTo(x, y);
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(cancelMarkBM, bitmapx, bitmapy, null);


        //上中
        x = rectCorners[10] - rectUpperLeftRect.width() + rectUpperLeftRect.width() / 2;
        y = rectCorners[11] - rectUpperLeftRect.height() + rectUpperLeftRect.height() / 2;
        rectUpperMidRect.offsetTo(x, y);
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(dotMarkBM, bitmapx, bitmapy, null);

        //下中
        x = rectCorners[14] - rectLowerRightRect.width() / 2;
        y = rectCorners[15] - rectLowerRightRect.width() / 2;
        rectBottomMidRect.offsetTo(x, y);
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(dotMarkBM, bitmapx, bitmapy, null);


        //右中
        x = rectCorners[12] - rectUpperRightRect.width() / 2;
        y = rectCorners[13] - rectUpperRightRect.height() + rectUpperRightRect.height() / 2;
        rectRightMidRect.offsetTo(x, y);
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(dotMarkBM, bitmapx, bitmapy, null);


        //左中
        x = rectCorners[16] - rectLowerLeftRect.width() + rectLowerLeftRect.width() / 2;
        y = rectCorners[17] - rectLowerLeftRect.height() / 2;
        rectLeftMidRect.offsetTo(x, y);
        bitmapx = x + rectDotSize / 2;
        bitmapy = y + rectDotSize / 2;
        canvas.drawBitmap(dotMarkBM, bitmapx, bitmapy, null);
    }

    private void drawDot(Canvas canvas, float[] dotCorners) {
        float x;
        float y;
        //0,1代表左点XY 2,3代表右点XY 4,5代表中点XY
        x = dotCorners[0] - rectDotSize / 2;
        y = dotCorners[1] - rectDotSize / 2;
        dotCancelMarkRect.offsetTo(x, y);//调整至实际绘制区域
        canvas.drawBitmap(cancelMarkBM, x, y, null);

        x = dotCorners[2] - rectDotSize / 2;
        y = dotCorners[3] - rectDotSize / 2;
        dotConfirmMarkRect.offsetTo(x, y);
        canvas.drawBitmap(confirmMarkBM, x, y, null);
    }

    private void drawDotFrame(Canvas canvas, float[] dotCorners) {
        Path rectFramePath = new Path();

        rectFramePath.moveTo(dotCorners[0], dotCorners[1]);
        rectFramePath.lineTo(dotCorners[2], dotCorners[3]);
        rectFramePath.lineTo(dotCorners[4], dotCorners[5]);
        rectFramePath.lineTo(dotCorners[6], dotCorners[7]);
        rectFramePath.close();
        canvas.drawPath(rectFramePath, dotPaint);
    }

    private void selectOtherDraw(float[] downPoint) {
        DrawRecord clickRecord = null;
        for (int i = curSketchpadData.drawRecordList.size() - 1; i >= 0; i--) {
            DrawRecord record = curSketchpadData.drawRecordList.get(i);
            if (isInDrawInside(record, downPoint)) {
                clickRecord = record;
                break;
            }
        }
        if (clickRecord != null) {
            setDrawRecord(clickRecord);
            actionMode = ACTION_DRAW;
            canBackGroundMove = false;
            curSketchpadData.touchMode = TouchMode.TOUCH_NONE;
        } else {
            actionMode = ACTION_NONE;
            canBackGroundMove = true;
            curSketchpadData.touchMode = TouchMode.TOUCH_NONE;
        }
    }

    private boolean isInDrawInside(DrawRecord dotRecord, float[] downPoint) {
        if (dotRecord != null) {
            float[] invertPoint = new float[2];
            Matrix invertMatrix = new Matrix();
            dotRecord.matrix.invert(invertMatrix);
            invertMatrix.mapPoints(invertPoint, downPoint);
            return dotRecord.rectOrigin.contains(invertPoint[0], invertPoint[1]);
        }
        return false;
    }

    private boolean isInDrawDot(float[] downPoint) {
        if (dotConfirmMarkRect.contains(downPoint[0], downPoint[1])) {
            curSketchpadData.touchMode = TouchMode.TOUCH_NONE;
            if (onDrawControlListener != null && curDrawRecord != null) {
                float[] origin = DrawUtil.calculateRectCorners(curDrawRecord);
                onDrawControlListener.onDrawConfirm(curDrawRecord, getPhysicsPoints(origin));
                actionMode = ACTION_NONE;
            }
            return true;
        }
        if (dotCancelMarkRect.contains(downPoint[0], downPoint[1])) {
            curSketchpadData.touchMode = TouchMode.TOUCH_NONE;
            curSketchpadData.drawRecordList.remove(curDrawRecord);
            if (onDrawControlListener != null) {
                onDrawControlListener.onDrawCancel(curDrawRecord);
                setDrawRecord(null);
                actionMode = ACTION_NONE;
            }
            return true;
        }
        return false;
    }

    /**
     * 判断是否选中了矩形顶点
     *
     * @param downPoint
     */
    private boolean isInRectDot(float[] downPoint) {
        if (rectUpperLeftRect.contains(downPoint[0], downPoint[1])) {//左上：等比例
            curSketchpadData.touchMode = TouchMode.TOUCH_SCALE;
            actionMode = ACTION_DRAW;
            return true;
        } else if (rectUpperRightRect.contains(downPoint[0], downPoint[1])) {//右上：确认
            curSketchpadData.touchMode = TouchMode.TOUCH_NONE;
            if (onDrawControlListener != null && curDrawRecord != null) {
                float[] origin = DrawUtil.calculateRectCorners(curDrawRecord);
                onDrawControlListener.onDrawConfirm(curDrawRecord, getPhysicsPoints(origin));
                actionMode = ACTION_NONE;
            }
            return true;
        } else if (rectLowerRightRect.contains(downPoint[0], downPoint[1])) {//右下：旋转
            curSketchpadData.touchMode = TouchMode.TOUCH_ROTATE;
            actionMode = ACTION_DRAW;
            return true;
        } else if (rectLowerLeftRect.contains(downPoint[0], downPoint[1])) {//左下：取消
            curSketchpadData.touchMode = TouchMode.TOUCH_NONE;
            curSketchpadData.drawRecordList.remove(curDrawRecord);
            if (onDrawControlListener != null) {
                onDrawControlListener.onDrawCancel(curDrawRecord);
                setDrawRecord(null);
                actionMode = ACTION_NONE;
            }
            return true;
        } else if (rectUpperMidRect.contains(downPoint[0], downPoint[1])) {//上中，上拉
            curSketchpadData.touchMode = TouchMode.TOUCH_TOP_STRETCH;
            actionMode = ACTION_DRAW;
            return true;
        } else if (rectRightMidRect.contains(downPoint[0], downPoint[1])) {//右中
            curSketchpadData.touchMode = TouchMode.TOUCH_RIGHT_STRETCH;
            actionMode = ACTION_DRAW;
            return true;
        } else if (rectBottomMidRect.contains(downPoint[0], downPoint[1])) {
            curSketchpadData.touchMode = TouchMode.TOUCH_BOTTOM_STRETCH;
            actionMode = ACTION_DRAW;
            return true;
        } else if (rectLeftMidRect.contains(downPoint[0], downPoint[1])) {
            curSketchpadData.touchMode = TouchMode.TOUCH_LEFT_STRETCH;
            actionMode = ACTION_DRAW;
            return true;
        } else if (curDrawRecord.rectOrigin.contains(downPoint[0], downPoint[1])) {
            curSketchpadData.touchMode = TouchMode.TOUCH_MOVE;
            actionMode = ACTION_DRAW;
            return true;
        }
        curSketchpadData.touchMode = TouchMode.TOUCH_NONE;
        actionMode = ACTION_NONE;
        return false;
    }

    private void onMouseMove(int mode, float triggerX, float triggerY, float dx, float dy) {
        if (mode == TouchMode.TOUCH_NONE) {
            return;
        }
        if (mode == TouchMode.TOUCH_MOVE) {
            onMove(dx, dy);
        } else if (mode == TouchMode.TOUCH_ROTATE) {
            onRotate(triggerX, triggerY);
        } else if (mode == TouchMode.TOUCH_SCALE) {
            onScale(dx, dy);
        } else if (mode == TouchMode.TOUCH_LEFT_STRETCH
                || mode == TouchMode.TOUCH_RIGHT_STRETCH
                || mode == TouchMode.TOUCH_TOP_STRETCH
                || mode == TouchMode.TOUCH_BOTTOM_STRETCH) {
            onStretch(mode, dx, dy);
        }
    }

    private void onMove(float dx, float dy) {
        curDrawRecord.rectOrigin.offset(-dx, -dy);
        invalidateMatrix();
        invalidate();
    }

    private void onRotate(float triggerX, float triggerY) {
        float[] pt1 = new float[]{curDrawRecord.rectOrigin.centerX(), curDrawRecord.rectOrigin.centerY()};
        float[] pt2 = new float[]{curDrawRecord.rectOrigin.right, curDrawRecord.rectOrigin.bottom};
        float[] pt3 = new float[]{triggerX, triggerY};
        double angel1 = DrawUtil.calculateAngleBetweenPoints(pt2, pt1);
        double angel2 = DrawUtil.calculateAngleBetweenPoints(pt3, pt1);
        curDrawRecord.mRotation = (float) (angel1 - angel2);
        invalidateMatrix();
        invalidate();
    }

    private void onScale(float dx, float dy) {
        float[] pt1 = new float[]{curDrawRecord.rectOrigin.centerX(), curDrawRecord.rectOrigin.centerY()};
        float[] pt2 = new float[]{curDrawRecord.rectOrigin.left, curDrawRecord.rectOrigin.top};
        float[] pt3 = new float[]{curDrawRecord.rectOrigin.left + dx, curDrawRecord.rectOrigin.top + dy};
        float distance1 = DrawUtil.calculatePointDistance(pt1, pt2);
        float distance2 = DrawUtil.calculatePointDistance(pt1, pt3);
        float distance = distance1 - distance2;
        if (!checkCanScale(distance)) {
            return;
        }
        curDrawRecord.rectOrigin.inset(-distance, -distance / mRatio);
        invalidateMatrix();
        invalidate();
    }

    private void onStretch(int mode, float dx, float dy) {
        float distance = calculateStretchDistance(dx, dy, mode);
        RectF rectF = new RectF(curDrawRecord.rectOrigin);
        if (mode == TouchMode.TOUCH_LEFT_STRETCH) {
            //映射矩形
            rectF.left += distance;
        } else if (mode == TouchMode.TOUCH_RIGHT_STRETCH) {
            rectF.right -= distance;
        } else if (mode == TouchMode.TOUCH_TOP_STRETCH) {
            rectF.top += distance;
        } else if (mode == TouchMode.TOUCH_BOTTOM_STRETCH) {
            rectF.bottom -= distance;
        }
        if (!checkCanStretch(rectF)) {
            return;
        }
        invalidateAfterStretch(mode, rectF);
        invalidate();
    }

    private void invalidateMatrix() {
        curDrawRecord.matrix.reset();
        curDrawRecord.matrix.postTranslate(-curDrawRecord.rectOrigin.centerX(), -curDrawRecord.rectOrigin.centerY());
        curDrawRecord.matrix.postRotate(curDrawRecord.mRotation);
        curDrawRecord.matrix.postTranslate(curDrawRecord.rectOrigin.centerX(), curDrawRecord.rectOrigin.centerY());
        invalidate();
    }

    private boolean checkCanScale(float distance) {
        tempRect.set(curDrawRecord.rectOrigin);
        tempRect.inset(-distance, -distance / mRatio);
        return !(tempRect.width() < minWidth) && !(tempRect.height() < minHeight);
    }

    private float calculateStretchDistance(float dx, float dy, int mode) {
        //中心点
        float[] pt1 = new float[]{curDrawRecord.rectOrigin.centerX(), curDrawRecord.rectOrigin.centerY()};
        //源rect上 edge上的圆点
        float[] pt2;

        if (mode == TouchMode.TOUCH_RIGHT_STRETCH) {
            pt2 = new float[]{curDrawRecord.rectOrigin.right, curDrawRecord.rectOrigin.centerY()};
        } else if (mode == TouchMode.TOUCH_LEFT_STRETCH) {
            pt2 = new float[]{curDrawRecord.rectOrigin.left, curDrawRecord.rectOrigin.centerY()};
        } else if (mode == TouchMode.TOUCH_TOP_STRETCH) {
            pt2 = new float[]{curDrawRecord.rectOrigin.centerX(), curDrawRecord.rectOrigin.top};
        } else {
            pt2 = new float[]{curDrawRecord.rectOrigin.centerX(), curDrawRecord.rectOrigin.bottom};
        }

        float[] points = new float[]{dx, dy};

        Matrix rotateMatrix = new Matrix();
        rotateMatrix.postRotate(-curDrawRecord.mRotation);
        rotateMatrix.mapPoints(points);
        //映射上角度后 实际的dx,dy
        dx = points[0];
        dy = points[1];

        //result rect上 edge上的圆点
        float[] pt3;
        if (mode == TouchMode.TOUCH_RIGHT_STRETCH) {
            pt3 = new float[]{curDrawRecord.rectOrigin.right + dx, curDrawRecord.rectOrigin.centerY() + dy};
        } else if (mode == TouchMode.TOUCH_LEFT_STRETCH) {
            pt3 = new float[]{curDrawRecord.rectOrigin.left + dx, curDrawRecord.rectOrigin.centerY() + dy};
        } else if (mode == TouchMode.TOUCH_TOP_STRETCH) {
            pt3 = new float[]{curDrawRecord.rectOrigin.centerX() + dx, curDrawRecord.rectOrigin.top + dy};
        } else {
            pt3 = new float[]{curDrawRecord.rectOrigin.centerX() + dx, curDrawRecord.rectOrigin.bottom + dy};
        }

        double distance1 = DrawUtil.calculatePointDistance(pt1, pt2);
        double distance2 = DrawUtil.calculatePointDistance(pt1, pt3);

        return (float) (distance2 - distance1);
    }

    private boolean checkCanStretch(RectF rectF) {
        tempRect.set(rectF);
        return !(tempRect.width() < minWidth) && !(tempRect.height() < minHeight);
    }

    private void invalidateAfterStretch(int mode, RectF newRect) {
        //新的中心
        float x, y;
        //老的中心
        float xOld = curDrawRecord.rectOrigin.centerX();
        float yOld = curDrawRecord.rectOrigin.centerY();
        //新的宽高
        float width = newRect.width();
        float height = newRect.height();
        float length;
        if (mode == TouchMode.TOUCH_RIGHT_STRETCH) {
            //以right实验  算出right的拉伸
            length = (newRect.right - curDrawRecord.rectOrigin.right) / 2;
            x = (float) (xOld + length * Math.cos(Math.toRadians(curDrawRecord.mRotation)));
            y = (float) (yOld + length * Math.sin(Math.toRadians(curDrawRecord.mRotation)));
        } else if (mode == TouchMode.TOUCH_LEFT_STRETCH) {
            length = -(newRect.left - curDrawRecord.rectOrigin.left) / 2;
            x = (float) (xOld - length * Math.cos(Math.toRadians(curDrawRecord.mRotation)));
            y = (float) (yOld - length * Math.sin(Math.toRadians(curDrawRecord.mRotation)));
        } else if (mode == TouchMode.TOUCH_TOP_STRETCH) {
            length = -(newRect.top - curDrawRecord.rectOrigin.top) / 2;
            x = (float) (xOld + length * Math.sin(Math.toRadians(curDrawRecord.mRotation)));
            y = (float) (yOld - length * Math.cos(Math.toRadians(curDrawRecord.mRotation)));
        } else {
            length = (newRect.bottom - curDrawRecord.rectOrigin.bottom) / 2;
            x = (float) (xOld - length * Math.sin(Math.toRadians(curDrawRecord.mRotation)));
            y = (float) (yOld + length * Math.cos(Math.toRadians(curDrawRecord.mRotation)));
        }
        //新的矩形
        float right = (2 * x + width) / 2;
        float left = (2 * x - width) / 2;
        float bottom = (2 * y + height) / 2;
        float top = (2 * y - height) / 2;
        curDrawRecord.rectOrigin.set(left, top, right, bottom);
        if (curDrawRecord.rectOrigin.height() > 0) {
            mRatio = curDrawRecord.rectOrigin.width() / curDrawRecord.rectOrigin.height();
        }
        curDrawRecord.matrix.reset();
        curDrawRecord.matrix.postTranslate(-x, -y);
        curDrawRecord.matrix.postRotate(curDrawRecord.mRotation);
        curDrawRecord.matrix.postTranslate(x, y);
    }

    public void onScaleAction(ScaleGestureDetector detector) {
        float[] bgCorners = DrawUtil.calculateBGCorners(curBackgroundRecord);

        //背景缩放倍数
        float scaleMax = SCALE_MAX;
        float scaleMin = SCALE_MIN;

        float len = (float) Math.sqrt(Math.pow(bgCorners[0] - bgCorners[4], 2) + Math.pow(bgCorners[1] - bgCorners[5], 2));
        double photoLen = Math.sqrt(Math.pow(curBackgroundRecord.photoRectSrc.width(), 2) + Math.pow(curBackgroundRecord.photoRectSrc.height(), 2));
        float scaleFactor = detector.getScaleFactor();


        if ((scaleFactor < 1 && len >= photoLen * scaleMin) || (scaleFactor > 1 && len <= photoLen * scaleMax)) {
            //背景
            if (curBackgroundRecord != null) {
                curBackgroundRecord.matrix.postScale(scaleFactor, scaleFactor, bgCorners[8], bgCorners[9]);
            }

            //矩形
            if (curSketchpadData.drawRecordList.size() > 0) {
                for (int i = 0; i < curSketchpadData.drawRecordList.size(); i++) {
                    if (curSketchpadData.drawRecordList.get(i) != null) {
                        curSketchpadData.drawRecordList.get(i).matrix.postScale(scaleFactor, scaleFactor, bgCorners[8], bgCorners[9]);
                    }
                }
            }

            invalidateMatrix();
            invalidate();
        }
    }


    @NonNull
    public BackgroundRecord initBackgroundRecord(Bitmap bitmap) {
        BackgroundRecord newRecord = new BackgroundRecord();
        newRecord.bitmap = bitmap;
        newRecord.photoRectSrc = new RectF(0, 0, newRecord.bitmap.getWidth(), newRecord.bitmap.getHeight());
        newRecord.matrix = new Matrix();
        return newRecord;
    }


    int i = 0;

    @NonNull
    private DrawRecord initDrawRecord(Bitmap bitmap, int type) {
        DrawRecord record = new DrawRecord();
        record.bitmap = bitmap;
        record.type = type;
        record.mRotation = 0;
        String name = "";
        if (type == 0) {
            name = "禁行区";
        } else if (type == 1) {
            name = "虚拟墙";
        } else if (type == 2) {
            name = "补水点";
        } else if (type == 3) {
            name = "排水点";
        }
        record.name = name + i++;
        record.matrix = new Matrix();

// TODO        if (curSketchpadData.drawRecordList.size() > 0 && curDrawRecord != null) {//有多个图片
//            record.rectOrigin = new RectF((getWidth() / 2 - bitmap.getWidth() / 2) + 25 * curSketchpadData.drawRecordList.size(),
//                    (getHeight() / 2 - bitmap.getHeight() / 2) + 25 * curSketchpadData.drawRecordList.size(),
//                    (getWidth() / 2 + bitmap.getWidth() / 2) + 25 * curSketchpadData.drawRecordList.size(),
//                    (getHeight() / 2 + bitmap.getHeight() / 2) + 25 * curSketchpadData.drawRecordList.size());
//            Log.e("draw", "w:" + getWidth() / 2 + "h:" + getHeight()/2);
//        } else {
//            record.rectOrigin = new RectF((getWidth() / 2 - bitmap.getWidth() / 2),
//                    (getHeight() / 2 - bitmap.getHeight() / 2),
//                    (getWidth() / 2 + bitmap.getWidth() / 2),
//                    (getHeight() / 2 + bitmap.getHeight() / 2));
//            Log.e("draw", "2w:" + getWidth() / 2 + "h:" + getHeight()/2);
//        }
        record.rectOrigin = new RectF((300 - bitmap.getWidth() / 2),
                (300 - bitmap.getHeight() / 2),
                (300 + bitmap.getWidth() / 2),
                (300 + bitmap.getHeight() / 2));
        Log.e("boo", "rect D:" + record.rectOrigin.left + "," + record.rectOrigin.top + "," + record.rectOrigin.right + "," + record.rectOrigin.bottom);
        Log.e("boo", "rect E:" + record.rectOrigin.width() + "," + record.rectOrigin.height());
        return record;
    }

    @NonNull
    private DrawRecord initDrawRecordByPoint(Bitmap bitmap, int left, int top, int right, int bottom, int type, float rotation, String name) {
        DrawRecord record = new DrawRecord();
        record.bitmap = bitmap;
        record.type = type;
        record.mRotation = rotation;
        record.name = name;
        record.matrix = new Matrix();
        record.rectOrigin = new RectF(left, top, right, bottom);
        Log.e("boo", "rect B:" + record.rectOrigin.left + "," + record.rectOrigin.top + "," + record.rectOrigin.right + "," + record.rectOrigin.bottom);
        Log.e("boo", "rect:" + left + "," + top + "," + right + "," + bottom);
        Log.e("boo", "rect A:" + record.rectOrigin.width() + "," + record.rectOrigin.height());
        //record.mRotation = 30;

        return record;
    }

    private void setDrawRecord(DrawRecord record) {
        curSketchpadData.drawRecordList.remove(record);
        if (record != null) {
            curSketchpadData.drawRecordList.add(record);
            //Log.e("test", "size:" + curSketchpadData.drawRecordList.size());
        }
        curDrawRecord = record;
        invalidate();
    }

    @NonNull
    private DrawRecord initDotRecord(Bitmap bitmap, int type) {
        DrawRecord dotRecord = new DrawRecord();
        dotRecord.bitmap = bitmap;
        dotRecord.type = type;
        String name = "";
        if (type == 0) {
            name = "禁行区";
        } else if (type == 1) {
            name = "虚拟墙";
        } else if (type == 2) {
            name = "补水点";
        } else if (type == 3) {
            name = "排水点";
        }
        dotRecord.name = name + i++;
        dotRecord.rectOrigin = new RectF(0, 0, dotRecord.bitmap.getWidth(), dotRecord.bitmap.getHeight());
        dotRecord.matrix = new Matrix(curBackgroundRecord.matrix);
        //TODO 0504
        // dotRecord.matrix.postTranslate(getWidth() / 4 + bitmap.getWidth(), getHeight() / 4 + bitmap.getHeight());
        dotRecord.matrix.postTranslate(getWidth() / 2 + (i * 20), getHeight() / 2 + (i * 20));
        return dotRecord;
    }

    @NonNull
    private DrawRecord initDotRecordByPoint(Bitmap bitmap, int left, int top, int type, String name) {
        DrawRecord dotRecord = new DrawRecord();
        dotRecord.bitmap = bitmap;
        dotRecord.type = type;
        dotRecord.name = name;
        dotRecord.rectOrigin = new RectF(0, 0, dotRecord.bitmap.getWidth(), dotRecord.bitmap.getHeight());
        dotRecord.matrix = new Matrix(curBackgroundRecord.matrix);
        dotRecord.matrix.postTranslate(left, top);

        return dotRecord;
    }

    public void setOnDrawControlListener(OnDrawControlListener onDrawControlListener) {
        this.onDrawControlListener = onDrawControlListener;
    }

    /**
     * 获取实际坐标
     *
     * @param origin
     * @return
     */
    public float[] getPhysicsPoints(float[] origin) {
        float[] result = new float[origin.length];
        Matrix imageMatrix = curBackgroundRecord.matrix;
        Matrix inverseMatrix = new Matrix();
        imageMatrix.invert(inverseMatrix);
        inverseMatrix.mapPoints(result, origin);
        int[] test = new int[origin.length];
//        for (int j = 0; j < result.length; j++) {
//            test[j] = (int) result[j];
//        }
        return result;
    }

    public int[] getPhysicsPoints2(float[] origin) {
        float[] result = new float[origin.length];
        Matrix imageMatrix = curBackgroundRecord.matrix;
        Matrix inverseMatrix = new Matrix();
        imageMatrix.invert(inverseMatrix);
        inverseMatrix.mapPoints(result, origin);
        int[] test = new int[origin.length];
        for (int j = 0; j < result.length; j++) {
            test[j] = (int) result[j];
        }
        return test;
    }

    //**********************************对外方法

    @IntDef({TouchMode.TOUCH_NONE, TouchMode.TOUCH_ROTATE, TouchMode.TOUCH_MOVE,
            TouchMode.TOUCH_SCALE, TouchMode.TOUCH_LEFT_STRETCH, TouchMode.TOUCH_RIGHT_STRETCH,
            TouchMode.TOUCH_TOP_STRETCH, TouchMode.TOUCH_BOTTOM_STRETCH,})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TouchMode {
        int TOUCH_NONE = 0x00;

        int TOUCH_ROTATE = 0X01;

        int TOUCH_MOVE = 0x02;

        int TOUCH_SCALE = 0x03;

        int TOUCH_LEFT_STRETCH = 0x04;

        int TOUCH_RIGHT_STRETCH = 0x05;

        int TOUCH_TOP_STRETCH = 0x06;

        int TOUCH_BOTTOM_STRETCH = 0x07;
    }


    public void addBackgroundBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap != null) {
            BackgroundRecord backgroundRecord = initBackgroundRecord(DrawUtil.setBitmapWH(bitmap, width, height));
            curSketchpadData.backgroundRecord = backgroundRecord;
            curBackgroundRecord = backgroundRecord;
            invalidate();
        } else {
            Toast.makeText(context, "background bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }


    //add -> init -> set -> draw
    public void addDrawRecord(Bitmap bitmap, int width, int height, int type) {
        if (bitmap != null) {
            DrawRecord record = initDrawRecord(DrawUtil.setBitmapWH(bitmap, width, height), type);
            setDrawRecord(record);
        } else {
            Toast.makeText(context, "draw bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 根据点位
     *
     * @param bitmap
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param type
     */
    public void addDrawRecordByPoint(Bitmap bitmap, int left, int top, int right, int bottom, int type, float rotate, String name) {
        if (bitmap != null) {
            Bitmap tempBitmap = DrawUtil.setBitmapWH(bitmap, (right - left), (bottom - top));
            if (left > right) {
                int temp = right;
                right = left;
                left = temp;
            }
            if (top > bottom) {
                int temp = bottom;
                bottom = top;
                top = temp;
            }
            DrawRecord record = initDrawRecordByPoint(tempBitmap, left, top, right, bottom, type, rotate, name);
            setDrawRecord(record);


            record.matrix.reset();
            record.matrix.postTranslate(-record.rectOrigin.centerX(), -record.rectOrigin.centerY());
            record.matrix.postRotate(record.mRotation);
            record.matrix.postTranslate(record.rectOrigin.centerX(), record.rectOrigin.centerY());

            if (onDrawControlListener != null) {
                float[] origin = DrawUtil.calculateRectCorners(record);
                onDrawControlListener.onDrawConfirm(record, getPhysicsPoints(origin));
                // TODO actionMode = ACTION_NONE;
            }
        } else {
            Toast.makeText(context, "draw bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }

    public void addDotRecordByPoint(Bitmap bitmap, int left, int top, int right, int bottom, int type, String name) {
        if (bitmap != null) {
            Bitmap tempBitmap = DrawUtil.setBitmapWH(bitmap, (right - left), (bottom - top));
            DrawRecord record = initDotRecordByPoint(tempBitmap, left, top, type, name);
            setDrawRecord(record);
            if (onDrawControlListener != null) {
                float[] origin = DrawUtil.calculateRectCorners(record);
                onDrawControlListener.onDrawConfirm(record, getPhysicsPoints(origin));
                //TODO actionMode = ACTION_NONE;
            }
        } else {
            Toast.makeText(context, "dot bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }

    public void addDotRecord(Bitmap bitmap, int width, int height, int type) {
        if (bitmap != null) {
            DrawRecord record = initDotRecord(DrawUtil.setBitmapWH(bitmap, width, height), type);
            setDrawRecord(record);
        } else {
            Toast.makeText(context, "dot bitmap can not be null", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 刷新状态
     */
    public void refreshView() {
        actionMode = ACTION_NONE;
        setDrawRecord(null);
        invalidate();
    }

    public void selectDrawItems(DrawRecord record) {
        //   actionMode = ACTION_DRAW;
        curSketchpadData.selectedDrawRecord.add(record);
        invalidate();
    }

    public void unSelectDrawItems(DrawRecord record) {
        //   actionMode = ACTION_NONE;
        curSketchpadData.selectedDrawRecord.remove(record);
        invalidate();
    }

    /**
     * 设置当前模式
     *
     * @param actionMode
     */
    public void setActionMode(int actionMode) {
        this.actionMode = actionMode;
    }

    /**
     * 初始化数据
     *
     * @param sketchpadData
     */
    public void setSketchData(SketchpadData2 sketchpadData) {
        this.curSketchpadData = sketchpadData;
        curDrawRecord = null;
        curBackgroundRecord = null;
    }


    /**
     * 对外接口
     */
    public interface OnDrawControlListener {
        void onDrawConfirm(DrawRecord record, float[] phyPoints);

        void onDrawCancel(DrawRecord record);
    }
}
