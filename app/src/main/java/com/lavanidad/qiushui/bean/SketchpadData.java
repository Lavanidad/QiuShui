package com.lavanidad.qiushui.bean;

import com.lavanidad.qiushui.SketchpadView;

import java.util.ArrayList;
import java.util.List;

public class SketchpadData {

    public List<RectRecord> rectRecordList;
    public List<LineRecord> lineRecordList;
    public List<WaterDotRecord> waterDotRecordList;
    public List<DrainDotRecord> drainDotRecordList;
    public int drawMode;

    public SketchpadData() {
        rectRecordList = new ArrayList<>();
        lineRecordList = new ArrayList<>();
        waterDotRecordList = new ArrayList<>();
        drainDotRecordList = new ArrayList<>();
        drawMode = SketchpadView.DrawMode.TYPE_NONE;
    }

    public int getDrawMode() {
        return drawMode;
    }

    public void setDrawMode(int drawMode) {
        this.drawMode = drawMode;
    }
}
