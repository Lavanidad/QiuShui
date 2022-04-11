package com.lavanidad.qiushui;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lavanidad.qiushui.bean.SketchpadData;
import com.lavanidad.qiushui.test.SketchpadData1;
import com.lavanidad.qiushui.test.SketchpadView1;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button bt_none, bt_rect, bt_add_rect, bt_line, bt_add_line;
    SketchpadView pad;
    SketchpadView1 pad1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_none = findViewById(R.id.bt_none);
        bt_rect = findViewById(R.id.bt_rect);
        bt_add_rect = findViewById(R.id.bt_add_rect);
        bt_line = findViewById(R.id.bt_line);
        bt_add_line = findViewById(R.id.bt_add_line);
        pad = findViewById(R.id.pad);
        pad1 = findViewById(R.id.pad1);

        //init
        SketchpadData sketchpadData = new SketchpadData();
        SketchpadData1 sketchpadData1 = new SketchpadData1();
        pad.setSketchData(sketchpadData);
        pad1.setSketchData(sketchpadData1);

        bt_none.setOnClickListener(this);
        bt_rect.setOnClickListener(this);
        bt_add_rect.setOnClickListener(this);
        bt_line.setOnClickListener(this);
        bt_add_line.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_none:
                bt_add_rect.setVisibility(View.INVISIBLE);
                bt_add_line.setVisibility(View.INVISIBLE);
                pad.setDrawMode(SketchpadView.DrawMode.TYPE_NONE);
                pad1.setDrawMode(SketchpadView.DrawMode.TYPE_NONE);
                break;
            case R.id.bt_rect:
                bt_add_rect.setVisibility(View.VISIBLE);
                bt_add_line.setVisibility(View.INVISIBLE);
                //step1
                pad.setDrawMode(SketchpadView.DrawMode.TYPE_RECT);
                pad1.setDrawMode(SketchpadView.DrawMode.TYPE_RECT);
                break;
            case R.id.bt_add_rect:
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.rect_area);
                //step2
                pad.addRectRecord(bitmap, 400, 400);
                pad1.addRectRecord(400, 400);
                break;
            case R.id.bt_line:
                pad.setDrawMode(SketchpadView.DrawMode.TYPE_LINE);
                bt_add_line.setVisibility(View.VISIBLE);
                bt_add_rect.setVisibility(View.INVISIBLE);
                //step1
                break;
            case R.id.bt_add_line:

                Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.line_area1);
                //pad.addLineRecord(400);
                pad.addLineRecord(bitmap1, 400);
                break;
        }
    }
}