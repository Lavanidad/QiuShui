package com.lavanidad.qiushui.bean;

import com.lavanidad.qiushui.SketchpadView;

import java.util.ArrayList;
import java.util.List;

public class SketchpadData {

    public List<RectRecord> rectRecordList;
    public List<LineRecord> lineRecordList;
    public int drawMode;

    public SketchpadData() {
        rectRecordList = new ArrayList<>();
        lineRecordList = new ArrayList<>();
        drawMode = SketchpadView.DrawMode.TYPE_NONE;
    }


    public List<RectRecord> getRectRecordList() {
        return rectRecordList;
    }

    public void setRectRecordList(List<RectRecord> rectRecordList) {
        this.rectRecordList = rectRecordList;
    }

    public int getDrawMode() {
        return drawMode;
    }

    public void setDrawMode(int drawMode) {
        this.drawMode = drawMode;
    }
}
