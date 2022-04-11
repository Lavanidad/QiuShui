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

public class MainActivity extends AppCompatActivity {

    Button bt_none, bt_rect, bt_add_rect;
    SketchpadView pad;
    SketchpadView1 pad1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_none = findViewById(R.id.bt_none);
        bt_rect = findViewById(R.id.bt_rect);
        bt_add_rect = findViewById(R.id.bt_add_rect);
        pad = findViewById(R.id.pad);
        pad1 = findViewById(R.id.pad1);

        //init
        SketchpadData sketchpadData = new SketchpadData();
        SketchpadData1 sketchpadData1 = new SketchpadData1();
        pad.setSketchData(sketchpadData);
        pad1.setSketchData(sketchpadData1);

        bt_none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_add_rect.setVisibility(View.INVISIBLE);
                pad.setDrawMode(SketchpadView.DrawMode.TYPE_NONE);
                pad1.setDrawMode(SketchpadView.DrawMode.TYPE_NONE);
            }
        });

        bt_rect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_add_rect.setVisibility(View.VISIBLE);
                //step1
                pad.setDrawMode(SketchpadView.DrawMode.TYPE_RECT);
                pad1.setDrawMode(SketchpadView.DrawMode.TYPE_RECT);
            }
        });

        bt_add_rect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.rect_area);
                //step2
                pad.addRectRecord(bitmap, 400, 400);
                pad1.addRectRecord(400, 400);
               // pad1.addRectRecord(400, 400);
            }
        });

    }
}