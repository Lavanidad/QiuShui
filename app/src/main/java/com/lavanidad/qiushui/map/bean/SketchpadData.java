package com.lavanidad.qiushui.map.bean;


import com.lavanidad.qiushui.map.SketchpadView;

import java.util.ArrayList;
import java.util.List;

public class SketchpadData {

    public List<RectRecord> rectRecordList;
    public List<LineRecord> lineRecordList;
    public List<DotRecord> dotRecordList;
    public BackgroundRecord backgroundRecord;
    public int drawMode;

    public SketchpadData() {
        rectRecordList = new ArrayList<>();
        lineRecordList = new ArrayList<>();
        dotRecordList = new ArrayList<>();
        backgroundRecord = null;
        drawMode = SketchpadView.DrawMode.TYPE_NONE;
    }
}
