package com.lavanidad.qiushui.map.bean;



import com.lavanidad.qiushui.map.SketchpadView2;

import java.util.ArrayList;
import java.util.List;

public class SketchpadData2 {

    public List<DrawRecord> drawRecordList;
    public List<DrawRecord> selectedDrawRecord;
    public BackgroundRecord backgroundRecord;
    public int touchMode;
    public int drawMode;

    public SketchpadData2() {
        drawRecordList = new ArrayList<>();
        selectedDrawRecord = new ArrayList<>();
        backgroundRecord = null;
        touchMode = SketchpadView2.TouchMode.TOUCH_NONE;
    }
}
