package com.lavanidad.qiushui.bean;

import android.graphics.Bitmap;

import com.lavanidad.qiushui.SketchpadView;

import java.util.ArrayList;
import java.util.List;

public class SketchpadData {

    public List<RectRecord> rectRecordList;
    public List<LineRecord> lineRecordList;
    public List<WaterDotRecord> waterDotRecordList;
    public List<DrainDotRecord> drainDotRecordList;
    public List<StrokeRecord> strokeRecordList;
    public BackgroundRecord backgroundRecord;
    public int drawMode;

    public SketchpadData() {
        rectRecordList = new ArrayList<>();
        lineRecordList = new ArrayList<>();
        waterDotRecordList = new ArrayList<>();
        drainDotRecordList = new ArrayList<>();
        strokeRecordList = new ArrayList<>();
        backgroundRecord = null;
        drawMode = SketchpadView.DrawMode.TYPE_NONE;
    }
}
